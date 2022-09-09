package hn.com.tigo.da.listener;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.tigo.josm.gateway.services.order.additionalparameterdto.v1.AdditionalParameters;
import com.tigo.josm.gateway.services.order.additionalparameterdto.v1.Parameter;
import com.tigo.josm.gateway.services.order.orderresponse.v1.OrderResponse;
import com.tigo.josm.gateway.services.order.simpleorderrequest.v1.SimpleOrderRequest;

import hn.com.tigo.core.da.dto.DABankDeleteAccountDTO;
import hn.com.tigo.core.da.manager.DAManager;
import hn.com.tigo.core.invoice.utils.States;
import hn.com.tigo.da.listener.model.DeleteAccountJson;
import hn.com.tigo.da.listener.util.DAListenerConstants;
import hn.com.tigo.da.listener.util.DAListenerUtils;
import hn.com.tigo.josm.gateway.services.gateway.ExecuteOrderService;
import hn.com.tigo.josm.gateway.services.gateway.Order;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJB;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJBLocal;
import hn.com.tigo.josm.persistence.exception.PersistenceException;

/**
 * DeleteMPBankThread.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:21:19 AM 2022
 */
public class DeleteMPBankThread extends Thread {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(DeleteMPBankThread.class);

	/** The executor service. */
	private ThreadPoolExecutor executorService;

	/** The working queue. */
	private BlockingQueue<Runnable> workingQueue;

	/** The state. */
	private States state;

	/** The config params. */
	private HashMap<String, String> configParams;

	/**
	 * Instantiates a new delete MP bank thread.
	 */
	public DeleteMPBankThread() {
		try {
			initialize();
		} catch (Exception e) {
			state = States.SHUTTINGDOWN;
			LOGGER.error("Unable to initialize : " + e.getMessage(), e);
		}
	}

	/**
	 * Initialize.
	 */
	public void initialize() {
		workingQueue = new ArrayBlockingQueue<Runnable>(100);
		LOGGER.info("workingQueue correctly");
		executorService = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS, workingQueue);
		state = States.STARTED;
		LOGGER.info("Iinitialize Finalized.");
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		state = States.SHUTTINGDOWN;
		executorService.shutdownNow();
	}

	/**
	 * Run.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		while (state == States.STARTED) {
			DAManager manager = null;
			String iD = "";

			try {
				ServiceSessionEJBLocal<DAManager> serviceSession = ServiceSessionEJB.getInstance();
				manager = (DAManager) serviceSession.getSessionDataSource(DAManager.class, DAListenerConstants.JNDI);
				configParams = manager.listAllParam();

				final List<DABankDeleteAccountDTO> listDeleteAcct = manager
						.selectDABankDelAcct(DAListenerConstants.STATUS_0);

				if (listDeleteAcct.size() > 0) {
					for (int a = 0; a < listDeleteAcct.size(); a++) {

						long startTime = System.nanoTime();
						Gson gson = new Gson();
						DeleteAccountJson jsonDel = null;
						iD = listDeleteAcct.get(a).getId();

						if (listDeleteAcct.get(a).getJson() != null) {
							jsonDel = gson.fromJson(listDeleteAcct.get(a).getJson(), DeleteAccountJson.class);
						}
						if (jsonDel.getDeleteList() != null) {

							// servicio order
							URL url = null;
							Order order = null;

							try {
								url = new URL(configParams.get(DAListenerConstants.WSDL_EXOR_COMPLEX));
								order = new ExecuteOrderService(url).getExecuteOrderPort();
							} catch (Exception e1) {
								LOGGER.error(
										"ERROR en eliminacion de cuenta: Error creacion de consumo ExecuteOrderService: "
												+ e1.getMessage());
								e1.printStackTrace();
								String uuidEr = UUID.randomUUID().toString();
								manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR1,
										"ERROR en eliminacion de cuenta: Error creacion de consumo ExecuteOrderService: "
												+ e1.getMessage(),
										listDeleteAcct.get(a).getId(), "");
							}

							for (int b = 0; b < jsonDel.getDeleteList().size(); b++) {

								String acctCode = jsonDel.getDeleteList().get(b).getAccount();
								List<Parameter> parameter = obtainParameters(acctCode);
								AdditionalParameters additionalParameters = DAListenerUtils
										.getAdditionalParameters(parameter);
								SimpleOrderRequest request = null;
								OrderResponse orderResponse = null;
								String uuid = UUID.randomUUID().toString();
								JSONObject jsonObject = null;

								try {

									request = DAListenerUtils.getRequestOrder(acctCode, additionalParameters,
											configParams.get(DAListenerConstants.COMMENTS_DELETE),
											DAListenerConstants.CHANNEL_ID_98,
											Long.valueOf(configParams.get(DAListenerConstants.PRODUCT_ID_DELETE)));
									jsonObject = new JSONObject(request);

									// Se hace llamado de servicio ExecuteOrderService
									orderResponse = order.deactivateProduct(request);

								} catch (Exception e) {

									LOGGER.error(
											"ERROR en eliminacion de cuenta: Error en consumo de servicio ExecuteOrderService: "
													+ e.getLocalizedMessage());
									manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
											"ERROR en eliminacion de cuenta: Error en Consumo Servicio ExecuteOrderService: "
													+ acctCode + " ==> " + e.getMessage(),
											listDeleteAcct.get(a).getId(), jsonObject.toString());
								}

								if (orderResponse != null) {
									if (orderResponse.getOrderResponseDetail().size() > 0) {

										String responseCode = orderResponse.getOrderResponseDetail().get(0)
												.getParameters().getParameter().get(0).getValue();
										LOGGER.info(responseCode);
										String responseMessage = orderResponse.getOrderResponseDetail().get(0)
												.getParameters().getParameter().get(1).getValue();
										LOGGER.info(responseMessage);

										// Se valida la respuesta de BPMN
										if (!responseCode.equals("0")) {

											String uuidEr = UUID.randomUUID().toString();
											manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR2,
													"Se presento un error en la eliminacion de la cuenta: " + acctCode
															+ " el servicio ExecuteOrderService contesto: "
															+ responseMessage,
													listDeleteAcct.get(a).getId(), jsonObject.toString());

											// Se guarda con status 2 si BPMN presento error
											manager.updateDeleteAccount(DAListenerConstants.STATUS_2,
													listDeleteAcct.get(a).getId());
										} else {

											manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR0,
													"Se realizo la Eliminacion de la cuenta: " + acctCode + " "
															+ responseMessage,
													listDeleteAcct.get(a).getId(), jsonObject.toString());

											// Se guarda con status 1 si BPMN response successful
											manager.updateDeleteAccount(DAListenerConstants.STATUS_1,
													listDeleteAcct.get(a).getId());
										}
									} else {

										String uuidEr = UUID.randomUUID().toString();
										manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR2,
												"Se presento un error en la eliminacion de la cuenta: " + acctCode
														+ " el servicio ExecuteOrderService contesto: "
														+ orderResponse.getMessage(),
												listDeleteAcct.get(a).getId(), jsonObject.toString());

										// Se guarda con status 2 si BPMN presento error
										manager.updateDeleteAccount(DAListenerConstants.STATUS_2,
												listDeleteAcct.get(a).getId());
									}
								} else {
									// Se guarda con status 2 si BPMN presento error
									manager.updateDeleteAccount(DAListenerConstants.STATUS_2,
											listDeleteAcct.get(a).getId());
								}

								long endTime = System.nanoTime();
								long duration = (endTime - startTime);
								NewRelicImpl.addNewRelicMetric("DeleteMPBankThread", duration / 1000000);
							}
						}
					}
				}
			} catch (PersistenceException error) {
				error.printStackTrace();
				NewRelicImpl.addNewRelicError(error.getMessage());

				LOGGER.error("DeleteMPBankThread " + this.getClass().getName() + error.getMessage(), error);
				String uuid = UUID.randomUUID().toString();
				if (manager != null) {
					try {

						manager.updateDeleteAccount(DAListenerConstants.STATUS_2, iD);

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR1,
								"Error en proceso DeleteMPBankThread, se presenta el siguiente error: "
										+ error.getMessage(),
								iD, "");
					} catch (Exception e) {
						LOGGER.error("Error de proceso DeleteMPBankThread: " + e.getMessage());
						e.printStackTrace();
					}
				}
			} finally {

				if (manager != null) {
					try {
						manager.close();
					} catch (Exception e) {
						LOGGER.error("DeleteMPBankThread " + e.getMessage(), e);
					}
				}
				this.sleepThread(Integer.parseInt(configParams.get(DAListenerConstants.SLEEP_THREAD)));
			}
		}
		executorService.shutdown();

	}

	/**
	 * Sleep thread.
	 *
	 * @param milliSecounds the milli secounds
	 */
	private void sleepThread(final int milliSecounds) {
		try {
			Thread.sleep(milliSecounds);
		} catch (InterruptedException e) {
			LOGGER.error("DeleteMPBankThread " + e.getMessage(), e);
		}
	}

	/**
	 * Obtain parameters.
	 *
	 * @param accountCode the account code
	 * @return the list
	 */
	private List<Parameter> obtainParameters(String accountCode) {

		List<Parameter> parameter = new ArrayList<Parameter>();
		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.BILLACCOUNT, accountCode);

		return parameter;
	}

}
