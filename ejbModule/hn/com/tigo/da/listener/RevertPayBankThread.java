package hn.com.tigo.da.listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
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

import com.tigo.josm.gateway.services.order.additionalparameterdto.v1.AdditionalParameters;
import com.tigo.josm.gateway.services.order.additionalparameterdto.v1.Parameter;
import com.tigo.josm.gateway.services.order.orderresponse.v1.OrderResponse;
import com.tigo.josm.gateway.services.order.simpleorderrequest.v1.SimpleOrderRequest;

import hn.com.tigo.core.da.dto.DABankProcessDTO;
import hn.com.tigo.core.da.dto.DABankProcessDetailPayDTO;
import hn.com.tigo.core.da.dto.DAFieldStringDTO;
import hn.com.tigo.core.da.manager.DAManager;
import hn.com.tigo.core.invoice.utils.States;
import hn.com.tigo.da.listener.util.DAListenerConstants;
import hn.com.tigo.da.listener.util.DAListenerUtils;
import hn.com.tigo.josm.gateway.services.gateway.ExecuteOrderService;
import hn.com.tigo.josm.gateway.services.gateway.Order;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJB;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJBLocal;
import hn.com.tigo.josm.persistence.exception.PersistenceException;

/**
 * RevertPayBankThread.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:21:35 AM 2022
 */
public class RevertPayBankThread extends Thread {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(RevertPayBankThread.class);

	/** The executor service. */
	private ThreadPoolExecutor executorService;

	/** The working queue. */
	private BlockingQueue<Runnable> workingQueue;

	/** The state. */
	private States state;

	/** The config params. */
	private HashMap<String, String> configParams;

	/**
	 * Instantiates a new revert pay bank thread.
	 */
	public RevertPayBankThread() {
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
			String BPiD = "";
			URL urlEmail = null;
			String bankProcessor = "";

			try {
				ServiceSessionEJBLocal<DAManager> serviceSession = ServiceSessionEJB.getInstance();
				manager = (DAManager) serviceSession.getSessionDataSource(DAManager.class, DAListenerConstants.JNDI);
				configParams = manager.listAllParam();
				try {
					urlEmail = new URL(configParams.get(DAListenerConstants.EMAIL_SERVICE_PROV));
				} catch (MalformedURLException e1) {
					LOGGER.error(
							"ERROR en reversion de pago: Error en obtencion de URL EmailService: " + e1.getMessage());
					e1.printStackTrace();
					String uuidEr = UUID.randomUUID().toString();
					manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR2,
							"ERROR en reversion de pago: Error en obtencion de URL EmailService " + e1.getMessage(), "",
							"");
				}

				Calendar cycleCalendar = Calendar.getInstance();
				final SimpleDateFormat df = new SimpleDateFormat(
						configParams.get(DAListenerConstants.FORMAT_DATE_YYYYMMDD));

				// Se obtiene listado de archivos en estatus 5
				final List<DABankProcessDTO> listBankProcess = manager
						.selectDABase64Bank(DAListenerConstants.STATUS_REVERT_PAY);
				if (listBankProcess.size() > 0) {
					for (int a = 0; a < listBankProcess.size(); a++) {

						long startTime = System.nanoTime();
						BPiD = listBankProcess.get(a).getId();
						String cycle = DAListenerUtils.getCycle(listBankProcess.get(a).getCycle());

						if (configParams.get(DAListenerConstants.BAC_ID)
								.equals(String.valueOf(listBankProcess.get(a).getBankid()))) {

							bankProcessor = configParams.get(DAListenerConstants.BANK_PROC_BAC);

							DAListenerUtils.sendEmailInit(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.REVERTPAY_FILE_INIT), urlEmail,
									configParams.get(DAListenerConstants.BANK_PROC_BAC),
									String.valueOf(listBankProcess.get(a).getCountClient()), cycle,
									configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.REVERT_FILE_SUBJ), manager);

							revertPay(manager, cycleCalendar, df, listBankProcess, a);
							List<DAFieldStringDTO> applyVal = manager.amountValidate(listBankProcess.get(a).getId(), 2);

							double amountApply = 0;

							if (applyVal.get(0).getFieldString() != null) {
								amountApply = Double.valueOf(applyVal.get(0).getFieldString());
								BigDecimal bd = new BigDecimal(amountApply).setScale(2, RoundingMode.HALF_UP);
								amountApply = bd.doubleValue();
							}

							String uuidEr = UUID.randomUUID().toString();
							manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR0,
									"Reversion de pagos: Se Procesaron "
											+ String.valueOf(listBankProcess.get(a).getCountClient())
											+ " Registros para el ciclo " + cycle + " Con un monto total de: "
											+ String.valueOf(amountApply),
									listBankProcess.get(a).getId(), "");

							DAListenerUtils.sendEmailEnd(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.REVERTPAY_FILE_END), urlEmail,
									configParams.get(DAListenerConstants.BANK_PROC_BAC),
									listBankProcess.get(a).getFileNameDA(), listBankProcess.get(a).getAmount(),
									String.valueOf(amountApply),
									String.valueOf(listBankProcess.get(a).getCountClient()), "", cycle,
									configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.REVERT_FILE_SUBJ), manager);

						} else if (configParams.get(DAListenerConstants.FICOHSA_ID)
								.equals(String.valueOf(listBankProcess.get(a).getBankid()))) {

							bankProcessor = configParams.get(DAListenerConstants.BANK_PROC_FICOHSA);

							DAListenerUtils.sendEmailInit(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.REVERTPAY_FILE_INIT), urlEmail,
									configParams.get(DAListenerConstants.BANK_PROC_FICOHSA),
									String.valueOf(listBankProcess.get(a).getCountClient()), cycle,
									configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.REVERT_FILE_SUBJ), manager);

							revertPay(manager, cycleCalendar, df, listBankProcess, a);
							List<DAFieldStringDTO> applyVal = manager.amountValidate(listBankProcess.get(a).getId(), 2);

							double amountApply = 0;

							if (applyVal.get(0).getFieldString() != null) {
								amountApply = Double.valueOf(applyVal.get(0).getFieldString());
								BigDecimal bd = new BigDecimal(amountApply).setScale(2, RoundingMode.HALF_UP);
								amountApply = bd.doubleValue();
							}

							String uuidEr = UUID.randomUUID().toString();
							manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR0,
									"Reversion de pagos: Se Procesaron "
											+ String.valueOf(listBankProcess.get(a).getCountClient())
											+ " Registros para el ciclo " + cycle + " Con un monto total de: "
											+ String.valueOf(amountApply),
									listBankProcess.get(a).getId(), "");

							DAListenerUtils.sendEmailEnd(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.REVERTPAY_FILE_END), urlEmail,
									configParams.get(DAListenerConstants.BANK_PROC_FICOHSA),
									listBankProcess.get(a).getFileNameDA(), listBankProcess.get(a).getAmount(),
									String.valueOf(amountApply),
									String.valueOf(listBankProcess.get(a).getCountClient()), "", cycle,
									configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.REVERT_FILE_SUBJ), manager);
						}

						// Pasa a status 6 en bank process
						manager.updateStatusBankProcess(DAListenerConstants.STATUS_REVERT,
								DAListenerConstants.STATUS_REVERT_PAY, listBankProcess.get(a).getId());

						long endTime = System.nanoTime();
						long duration = (endTime - startTime);
						NewRelicImpl.addNewRelicMetric("RevertPayBankThread", duration / 1000000);
					}
				}

			} catch (PersistenceException error) {
				error.printStackTrace();
				NewRelicImpl.addNewRelicError(error.getMessage());

				LOGGER.error("RevertPayBankThread " + this.getClass().getName() + error.getMessage(), error);
				String uuid = UUID.randomUUID().toString();
				if (manager != null) {
					try {
						manager.updateStatusBankProcess(-6, 5, BPiD);

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR1,
								"Error en proceso RevertPayBankThread, se presenta el siguiente error: "
										+ error.getMessage(),
								BPiD, "");

						if (!BPiD.equals("")) {
							DAListenerUtils.sendEmailError(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.REVERTPAY_FILE_ERROR), urlEmail, bankProcessor,
									error.getMessage(), configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.REVERT_FILE_SUBJ), manager);
						}

					} catch (Exception e) {
						LOGGER.error("Error de proceso RevertPayBankThread: " + e.getMessage());
						e.printStackTrace();
					}
				}
			} finally {

				if (manager != null) {
					try {
						manager.close();
					} catch (Exception e) {
						LOGGER.error("RevertPayBankThread " + e.getMessage(), e);
					}
				}
				this.sleepThread(Integer.parseInt(configParams.get(DAListenerConstants.SLEEP_THREAD)));
			}
		}
		executorService.shutdown();

	}

	/**
	 * Revert pay.
	 *
	 * @param manager the manager
	 * @param cycleCalendar the cycle calendar
	 * @param df the df
	 * @param listBankProcess the list bank process
	 * @param a the a
	 * @throws PersistenceException the persistence exception
	 */
	private void revertPay(DAManager manager, Calendar cycleCalendar, final SimpleDateFormat df,
			final List<DABankProcessDTO> listBankProcess, int a) throws PersistenceException {

		final List<DABankProcessDetailPayDTO> listBPDetail = manager
				.selectBankProcessRevert(listBankProcess.get(a).getId(), DAListenerConstants.STATUS_1);
		for (int b = 0; b < listBPDetail.size(); b++) {

			String acctCode = listBPDetail.get(b).getAcctCode();

			// servicio order
			URL url = null;
			Order order = null;

			try {
				url = new URL(configParams.get(DAListenerConstants.WSDL_EXOR_COMPLEX));
				order = new ExecuteOrderService(url).getExecuteOrderPort();
			} catch (Exception e1) {
				LOGGER.error("ERROR en reversion de pago: Error en creacion de consumo ExecuteOrderService: "
						+ e1.getMessage());
				e1.printStackTrace();
				String uuidEr = UUID.randomUUID().toString();
				manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR1,
						"ERROR en reversion de pago: Error en creacion de consumo ExecuteOrderService: "
								+ e1.getMessage(),
						listBPDetail.get(b).getIdBankProcess(), "");
			}

			List<Parameter> parameter = obtainParameters(acctCode, listBPDetail.get(b).getPaymentSeq(),
					df.format(cycleCalendar.getTime()));

			AdditionalParameters additionalParameters = DAListenerUtils.getAdditionalParameters(parameter);
			SimpleOrderRequest request = null;
			OrderResponse orderResponse = null;
			String uuid = UUID.randomUUID().toString();
			JSONObject jsonObject = null;

			try {

				request = DAListenerUtils.getRequestOrder(acctCode, additionalParameters,
						configParams.get(DAListenerConstants.COMMENTS_REVERT), DAListenerConstants.CHANNEL_ID_98,
						Long.valueOf(configParams.get(DAListenerConstants.PRODUCT_ID)));
				jsonObject = new JSONObject(request);

				// Se hace llamado de servicio ExecuteOrderService
				orderResponse = order.deactivateProduct(request);

			} catch (Exception e) {

				LOGGER.error("ERROR en reversion de pago: Error en consumo de servicio ExecuteOrderService: "
						+ e.getLocalizedMessage());
				manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
						"ERROR en reversion de pago: Error en Consumo Servicio ExecuteOrderService: " + acctCode
								+ " ==> " + e.getMessage(),
						listBPDetail.get(b).getIdBankProcess(), jsonObject.toString());
			}

			if (orderResponse != null) {

				String responseCode = orderResponse.getOrderResponseDetail().get(0).getParameters().getParameter()
						.get(0).getValue();
				LOGGER.info(responseCode);
				String responseMessage = orderResponse.getOrderResponseDetail().get(0).getParameters().getParameter()
						.get(1).getValue();
				LOGGER.info(responseMessage);

				String responseTXId = orderResponse.getTransactionID();
				LOGGER.info(responseTXId);

				// Se valida la respuesta de BPMN
				if (!responseCode.equals("0")) {

					manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
							"Se presento un error en la reversion de pago, para la cuenta: " + acctCode
									+ " el servicio ExecuteOrderService contesto: " + responseMessage,
							listBPDetail.get(b).getIdBankProcess(), jsonObject.toString());

					// Se guarda con status -2 si BPMN presento error
					manager.updateBPDPByOne(DAListenerConstants.STATUS_NEG2, responseTXId, acctCode,
							DAListenerConstants.STATUS_1, listBPDetail.get(b).getGroupPayment(),
							listBPDetail.get(b).getInvoiceId());
				} else {

					manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR0,
							"Se realizo la Reversion de pago, para la cuenta: " + acctCode + " " + responseMessage,
							listBPDetail.get(b).getIdBankProcess(), jsonObject.toString());

					// Se guarda con status 2 si BPMN response successful
					manager.updateBPDPByOne(DAListenerConstants.STATUS_2, responseTXId, acctCode,
							DAListenerConstants.STATUS_1, listBPDetail.get(b).getGroupPayment(),
							listBPDetail.get(b).getInvoiceId());
				}
			} else {

				// Se guarda con status -2 si BPMN presento error
				manager.updateBPDPByOne(DAListenerConstants.STATUS_NEG2, "", acctCode, DAListenerConstants.STATUS_1,
						listBPDetail.get(b).getGroupPayment(), listBPDetail.get(b).getInvoiceId());
			}
		}
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
			LOGGER.error("RevertPayBankThread " + e.getMessage(), e);
		}
	}

	/**
	 * Obtain parameters.
	 *
	 * @param accountCode the account code
	 * @param paymentSeq the payment seq
	 * @param date the date
	 * @return the list
	 */
	private List<Parameter> obtainParameters(String accountCode, String paymentSeq, String date) {

		List<Parameter> parameter = new ArrayList<Parameter>();

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.ID_TYPE,
				DAListenerConstants.ACCTCODE);
		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.ID, accountCode);
		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.PAYMENT_ID, paymentSeq);
		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.PAY_DATE, date);

		return parameter;
	}

}
