package hn.com.tigo.da.listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
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
import hn.com.tigo.core.da.dto.DAGroupPaymentDTO;
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
 * ApplyPayBankThread.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:20:48 AM 2022
 */
public class ApplyPayBankThread extends Thread {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(ApplyPayBankThread.class);

	/** The executor service. */
	private ThreadPoolExecutor executorService;

	/** The working queue. */
	private BlockingQueue<Runnable> workingQueue;

	/** The state. */
	private States state;

	/** The config params. */
	private HashMap<String, String> configParams;

	/**
	 * Instantiates a new apply pay bank thread.
	 */
	public ApplyPayBankThread() {
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
					LOGGER.error("ERROR: en obtencion de URL EmailService: " + e1.getMessage());
					e1.printStackTrace();
					String uuidEr = UUID.randomUUID().toString();
					manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR2,
							"ERROR: en obtencion de URL EmailService " + e1.getMessage(), "", "");
				}

				Calendar cycleCalendar = Calendar.getInstance();
				final SimpleDateFormat df = new SimpleDateFormat(
						configParams.get(DAListenerConstants.FORMAT_DATE_YYYYMMDD));

				// Se obtiene listado de archivos en estatus 3
				final List<DABankProcessDTO> listBase64Bank = manager
						.selectDABase64Bank(DAListenerConstants.STATUS_APPLY_PAY);

				if (listBase64Bank.size() > 0) {
					for (int a = 0; a < listBase64Bank.size(); a++) {

						long startTime = System.nanoTime();
						BPiD = listBase64Bank.get(a).getId();

						if (listBase64Bank.get(a).getBase64Bank() != null) {

							// Se decodifica el base64 a string
							byte[] base64 = Base64.getDecoder().decode(listBase64Bank.get(a).getBase64Bank());
							String base64Final = new String(base64, StandardCharsets.UTF_8).replaceAll("\r\n", "\n");

							// Se identifica de que banco procede
							if (configParams.get(DAListenerConstants.BAC_ID)
									.equals(String.valueOf(listBase64Bank.get(a).getBankid()))) {
								bankProcessor = configParams.get(DAListenerConstants.BANK_PROC_BAC);

								String cycle = DAListenerUtils.getCycle(listBase64Bank.get(a).getCycle());
								
								String[] parts = base64Final.split("\n");
								parts = ArrayUtils.remove(parts, parts.length - 1);

								DAListenerUtils.sendEmailInit(configParams.get(DAListenerConstants.BODY_EMAIL),
										configParams.get(DAListenerConstants.APPLYPAY_FILE_INIT), urlEmail,
										configParams.get(DAListenerConstants.BANK_PROC_BAC),
										String.valueOf(listBase64Bank.get(a).getCountClient()), cycle,
										configParams.get(DAListenerConstants.EMAILS),
										configParams.get(DAListenerConstants.FROM_EMAIL),
										configParams.get(DAListenerConstants.APPLY_FILE_SUBJ), manager);

								for (int b = 1; b < parts.length; b++) {

									int size = parts[b].length();
									String acctcFather = removeZero(parts[b].toString().substring(49, 59));

									// Se Obtiene la info de acctCode padre en tabla DA_Group_payment
									final List<DAGroupPaymentDTO> listAccSons = manager.selectDAAcctSons(acctcFather, 0,
											listBase64Bank.get(a).getId());
									if (listAccSons != null) {

										// Se valida si padre posee hijos
										if (listAccSons.size() > 0 && listAccSons.get(0).getAcctSons() != null) {

											// Se crea array de los hijos los cuales estan separados por ;
											String[] acctSons = listAccSons.get(0).getAcctSons().split(";");

											// Para BAC cada linea del cuerpo de archivo tiene maximo 88 lineas, si
											// posee
											// mas es porque posee codigo de error
											if (size > 88) {

												String errorBank = parts[b].toString().substring((size - 2), size);
												updateBankProcDetailPay(manager, acctSons, errorBank);

											} else {
												applyPayByRegister(manager, cycleCalendar, df, listAccSons, acctSons,
														DAListenerConstants.BANK_BAC_CREDOMATIC);
											}
										} else {
											// Para BAC cada linea del cuerpo de archivo tiene maximo 88 lineas, si
											// posee
											// mas es porque posee codigo de error
											if (size > 88) {

												String errorBank = parts[b].toString().substring((size - 2), size);

												// Se guarda el codigo de error y se actualiza status 1
												manager.updateBankProcDetailPay(errorBank,
														DAListenerConstants.STATUS_NEG1, "", acctcFather,
														DAListenerConstants.STATUS_0, DAListenerConstants.STATUS_0,
														DAListenerConstants.STATUS_0);

											} else {
												// Se llama al metodo appyPay para aplicar pago
												applyPay(manager, cycleCalendar, df, acctcFather, listAccSons,
														DAListenerConstants.CODE_SUCCESS, DAListenerConstants.STATUS_0,
														DAListenerConstants.BANK_BAC_CREDOMATIC);
											}
										}
									}
								}

								List<DABankProcessDetailPayDTO> listApplyPayError = manager.selectApplyPayError(
										listBase64Bank.get(a).getId(), DAListenerConstants.STATUS_NEG1,
										DAListenerConstants.CODE_SUCCESS);

								retriesPayError(manager, DAListenerConstants.BANK_BAC_CREDOMATIC, cycleCalendar, df, listBase64Bank, a,
										listApplyPayError);

								List<DAFieldStringDTO> sizeVal = manager.countValidate(listBase64Bank.get(a).getId(),
										1);
								List<DAFieldStringDTO> applyVal = manager.amountValidate(listBase64Bank.get(a).getId(),
										1);

								double amountApply = 0;

								if (applyVal.get(0).getFieldString() != null) {
									amountApply = Double.valueOf(applyVal.get(0).getFieldString());
									BigDecimal bd = new BigDecimal(amountApply).setScale(2, RoundingMode.HALF_UP);
									amountApply = bd.doubleValue();
								}

								String uuidEr = UUID.randomUUID().toString();
								manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR0,
										"Aplicacion de pagos: Se Procesaron "
												+ String.valueOf(listBase64Bank.get(a).getCountClient())
												+ " Registros, de las cuales "
												+ (sizeVal.get(0).getFieldString() != null
														? sizeVal.get(0).getFieldString()
														: "0")
												+ " fueron líneas válidas para el ciclo " + cycle
												+ " Con un monto total de: " + String.valueOf(amountApply),
										listBase64Bank.get(a).getId(), "");

								DAListenerUtils.sendEmailEnd(configParams.get(DAListenerConstants.BODY_EMAIL),
										configParams.get(DAListenerConstants.APPLYPAY_FILE_END), urlEmail,
										configParams.get(DAListenerConstants.BANK_PROC_BAC),
										listBase64Bank.get(a).getFileNameDA(), listBase64Bank.get(a).getAmount(),
										String.valueOf(amountApply),
										String.valueOf(listBase64Bank.get(a).getCountClient()),
										(sizeVal.get(0).getFieldString() != null ? sizeVal.get(0).getFieldString()
												: "0"),
										cycle, configParams.get(DAListenerConstants.EMAILS),
										configParams.get(DAListenerConstants.FROM_EMAIL),
										configParams.get(DAListenerConstants.APPLY_FILE_SUBJ), manager);

							} else if (configParams.get(DAListenerConstants.FICOHSA_ID)
									.equals(String.valueOf(listBase64Bank.get(a).getBankid()))) {

								bankProcessor = configParams.get(DAListenerConstants.BANK_PROC_FICOHSA);
								String cycle = DAListenerUtils.getCycle(listBase64Bank.get(a).getCycle());

								String[] parts = base64Final.split("\n");

								DAListenerUtils.sendEmailInit(configParams.get(DAListenerConstants.BODY_EMAIL),
										configParams.get(DAListenerConstants.APPLYPAY_FILE_INIT), urlEmail,
										configParams.get(DAListenerConstants.BANK_PROC_FICOHSA),
										String.valueOf(listBase64Bank.get(a).getCountClient()), cycle,
										configParams.get(DAListenerConstants.EMAILS),
										configParams.get(DAListenerConstants.FROM_EMAIL),
										configParams.get(DAListenerConstants.APPLY_FILE_SUBJ), manager);

								for (int b = 0; b < parts.length; b++) {
									String[] rowPart = parts[b].toString().split(";");

									String acctcFather = removeZero(rowPart[1].toString());
									if (rowPart.length > 11) {

										// Obtengo la info de acctCode padre en tabla DA_Group_payment
										final List<DAGroupPaymentDTO> listAccSons = manager
												.selectDAAcctSons(acctcFather, 0, listBase64Bank.get(a).getId());

										if (listAccSons != null) {

											// Se valida si padre posee hijos
											if (listAccSons.size() > 0 && listAccSons.get(0).getAcctSons() != null) {

												// Se crea array de los hijos los cuales estan separados por ;
												String[] acctSons = listAccSons.get(0).getAcctSons().split(";");

												// Para el archivo Ficohsa se valida el penultimo bloque de linea,
												// codigo 00
												// indica que fue satisfactorio
												if (!rowPart[11].toString().equals(DAListenerConstants.CODE_SUCCESS)) {

													String errorBank = rowPart[11].toString();
													updateBankProcDetailPay(manager, acctSons, errorBank);

												} else {
													applyPayByRegister(manager, cycleCalendar, df, listAccSons,
															acctSons, DAListenerConstants.BANK_PROC_FICOHSA);
												}
											} else {
												if (!rowPart[11].toString().equals(DAListenerConstants.CODE_SUCCESS)) {

													String errorBank = rowPart[11].toString();

													// Se guarda el codigo de error y se actualiza status 1
													manager.updateBankProcDetailPay(errorBank,
															DAListenerConstants.STATUS_NEG1, "", acctcFather,
															DAListenerConstants.STATUS_0, DAListenerConstants.STATUS_0,
															DAListenerConstants.STATUS_0);

												} else {

													// Se llama al metodo appyPay para aplicar pago
													applyPay(manager, cycleCalendar, df, acctcFather, listAccSons,
															DAListenerConstants.CODE_SUCCESS,
															DAListenerConstants.STATUS_0,
															DAListenerConstants.BANK_PROC_FICOHSA);
												}
											}
										}
									} else {
										String uuidEr = UUID.randomUUID().toString();
										manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR1,
												"ERROR en aplicacion de pago: Error en Archivo de entrada no esta procesado por banco, linea no cuenta con codigo de procesamiento: "
														+ acctcFather,
												listBase64Bank.get(a).getId(), "");
									}
								}

								List<DABankProcessDetailPayDTO> listApplyPayError = manager.selectApplyPayError(
										listBase64Bank.get(a).getId(), DAListenerConstants.STATUS_NEG1,
										DAListenerConstants.CODE_SUCCESS);

								retriesPayError(manager, DAListenerConstants.BANK_PROC_FICOHSA, cycleCalendar, df, listBase64Bank, a,
										listApplyPayError);

								List<DAFieldStringDTO> sizeVal = manager.countValidate(listBase64Bank.get(a).getId(),
										1);
								List<DAFieldStringDTO> applyVal = manager.amountValidate(listBase64Bank.get(a).getId(),
										1);

								double amountApply = 0;

								if (applyVal.get(0).getFieldString() != null) {
									amountApply = Double.valueOf(applyVal.get(0).getFieldString());
									BigDecimal bd = new BigDecimal(amountApply).setScale(2, RoundingMode.HALF_UP);
									amountApply = bd.doubleValue();
								}

								String uuidEr = UUID.randomUUID().toString();
								manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR0,
										"Aplicacion de pagos: Se Procesaron "
												+ String.valueOf(listBase64Bank.get(a).getCountClient())
												+ " Registros, de las cuales "
												+ (sizeVal.get(0).getFieldString() != null
														? sizeVal.get(0).getFieldString()
														: "0")
												+ " fueron líneas válidas para el ciclo " + cycle
												+ " Con un monto total de: " + String.valueOf(amountApply),
										listBase64Bank.get(a).getId(), "");

								DAListenerUtils.sendEmailEnd(configParams.get(DAListenerConstants.BODY_EMAIL),
										configParams.get(DAListenerConstants.APPLYPAY_FILE_END), urlEmail,
										configParams.get(DAListenerConstants.BANK_PROC_FICOHSA),
										listBase64Bank.get(a).getFileNameDA(), listBase64Bank.get(a).getAmount(),
										String.valueOf(amountApply),
										String.valueOf(listBase64Bank.get(a).getCountClient()),
										(sizeVal.get(0).getFieldString() != null ? sizeVal.get(0).getFieldString()
												: "0"),
										cycle, configParams.get(DAListenerConstants.EMAILS),
										configParams.get(DAListenerConstants.FROM_EMAIL),
										configParams.get(DAListenerConstants.APPLY_FILE_SUBJ), manager);
							}

						}

						// Pasa a status 4 en bank process
						manager.updateStatusBankProcess(DAListenerConstants.STATUS_PAY,
								DAListenerConstants.STATUS_APPLY_PAY, listBase64Bank.get(a).getId());

						long endTime = System.nanoTime();
						long duration = (endTime - startTime);
						NewRelicImpl.addNewRelicMetric("ApplyPayBankThread", duration / 1000000);
					}
				}

			} catch (PersistenceException error) {
				error.printStackTrace();
				NewRelicImpl.addNewRelicError(error.getMessage());

				LOGGER.error("ApplyPayBankThread " + this.getClass().getName() + error.getMessage(), error);
				String uuid = UUID.randomUUID().toString();
				if (manager != null) {
					try {
						manager.updateStatusBankProcess(-4, 3, BPiD);

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR1,
								"Error en proceso ApplyPayBankThread, se presenta el siguiente error: "
										+ error.getMessage(),
								BPiD, "");

						if (!BPiD.equals("")) {
							DAListenerUtils.sendEmailError(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.APPLYPAY_FILE_ERROR), urlEmail, bankProcessor,
									error.getMessage(), configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.APPLY_FILE_SUBJ), manager);

						}
					} catch (Exception e) {
						LOGGER.error("Error de proceso ApplyPayBankThread: " + e.getMessage());
						e.printStackTrace();
					}
				}
			} finally {

				if (manager != null) {
					try {
						manager.close();
					} catch (Exception e) {
						LOGGER.error("ApplyPayBankThread " + e.getMessage(), e);
					}
				}
				this.sleepThread(Integer.parseInt(configParams.get(DAListenerConstants.SLEEP_THREAD)));
			}
		}
		executorService.shutdown();

	}

	/**
	 * Retries pay error.
	 *
	 * @param manager the manager
	 * @param bankProcessor the bank processor
	 * @param cycleCalendar the cycle calendar
	 * @param df the df
	 * @param listBase64Bank the list base 64 bank
	 * @param a the a
	 * @param listApplyPayError the list apply pay error
	 * @throws PersistenceException the persistence exception
	 */
	private void retriesPayError(DAManager manager, String bankProcessor, Calendar cycleCalendar,
			final SimpleDateFormat df, final List<DABankProcessDTO> listBase64Bank, int a,
			List<DABankProcessDetailPayDTO> listApplyPayError) throws PersistenceException {

		if (listApplyPayError != null) {
			int g = 0;
			while (g < Long.parseLong(configParams.get(DAListenerConstants.RETRIES))) {

				for (int f = 0; f < listApplyPayError.size(); f++) {

					if (listApplyPayError.get(f) != null) {

						if (listApplyPayError.get(f).getRetries() < Long
								.parseLong(configParams.get(DAListenerConstants.RETRIES))) {

							if (listApplyPayError.get(f).getStatus() == DAListenerConstants.STATUS_NEG1) {

								// servicio order
								URL url = null;
								Order order = null;

								try {
									url = new URL(configParams.get(DAListenerConstants.WSDL_EXOR_COMPLEX));
									order = new ExecuteOrderService(url).getExecuteOrderPort();
								} catch (Exception e1) {
									LOGGER.error(
											"ERROR en aplicacion de pago: Error en creacion de consumo ExecuteOrderService: "
													+ e1.getMessage());
									e1.printStackTrace();
									String uuidEr = UUID.randomUUID().toString();
									manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR1,
											"ERROR en aplicacion de pago: Error en creacion de consumo ExecuteOrderService: "
													+ e1.getMessage(),
											listBase64Bank.get(a).getId(), "");
								}

								// Se obtiene monto por cada registro
								double amount = Double.valueOf(listApplyPayError.get(f).getAmount());

								List<Parameter> parameter = obtainParameters(listApplyPayError.get(f).getAcctCode(),
										listApplyPayError.get(f).getSubscriberId(),
										listApplyPayError.get(f).getInvoiceId(), df.format(cycleCalendar.getTime()),
										String.valueOf(amount), configParams.get(bankProcessor));

								AdditionalParameters additionalParameters = DAListenerUtils
										.getAdditionalParameters(parameter);
								SimpleOrderRequest request = null;
								OrderResponse orderResponse = null;
								String uuid = UUID.randomUUID().toString();
								JSONObject jsonObject = null;

								try {
									// Se hace llamado de servicio ExecuteOrderService

									request = DAListenerUtils.getRequestOrder(listApplyPayError.get(f).getAcctCode(),
											additionalParameters,
											configParams.get(DAListenerConstants.COMMENT_ACTIVATE),
											DAListenerConstants.CHANNEL_ID_98,
											Long.valueOf(configParams.get(DAListenerConstants.PRODUCT_ID)));
									jsonObject = new JSONObject(request);
									orderResponse = order.activateProduct(request);

								} catch (Exception e) {

									LOGGER.error(
											"ERROR en aplicacion de pago: Error en consumo de servicio ExecuteOrderService: "
													+ e.getLocalizedMessage());
									manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
											"ERROR en aplicacion de pago: Error en consumo de servicio ExecuteOrderService: "
													+ listApplyPayError.get(f).getAcctCode() + " ==> " + e.getMessage(),
											listBase64Bank.get(a).getId(), jsonObject.toString());
								}

								listApplyPayError.get(f).setRetries(listApplyPayError.get(f).getRetries() + 1);

								if (orderResponse != null) {

									String responseCode = orderResponse.getOrderResponseDetail().get(0).getParameters()
											.getParameter().get(0).getValue();

									String responseMessage = orderResponse.getOrderResponseDetail().get(0)
											.getParameters().getParameter().get(1).getValue();

									String responsePayId = orderResponse.getOrderResponseDetail().get(0).getParameters()
											.getParameter().get(2).getValue();

									String responseTXId = orderResponse.getTransactionID();

									listApplyPayError.get(f).setPaymentId(responseTXId);
									listApplyPayError.get(f).setPaymentSeq(responsePayId);

									// Se valida la respuesta de BPMN
									if (!responseCode.equals("0") || responsePayId == "") {

										manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
												"Se presento un error en la aplicacion de pago, para la cuenta: "
														+ listApplyPayError.get(f).getAcctCode()
														+ " el servicio ExecuteOrderService contesto: "
														+ responseMessage,
												listBase64Bank.get(a).getId(), jsonObject.toString());

									} else {

										manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR0,
												"Se realizo la aplicacion de pago, para la cuenta: "
														+ listApplyPayError.get(f).getAcctCode() + " "
														+ responseMessage,
												listBase64Bank.get(a).getId(), jsonObject.toString());

										listApplyPayError.get(f).setStatus(DAListenerConstants.STATUS_1);
									}
								}
							}
						}
					}
				}
				g++;
			}

			for (int f = 0; f < listApplyPayError.size(); f++) {

				if (listApplyPayError.get(f) != null) {

					manager.updateBankProcDetailPayByOne(listApplyPayError.get(f).getErrorBank(),
							listApplyPayError.get(f).getStatus(), listApplyPayError.get(f).getPaymentId(),
							listApplyPayError.get(f).getAcctCode(), DAListenerConstants.STATUS_NEG1,
							listApplyPayError.get(f).getGroupPayment(), listApplyPayError.get(f).getInvoiceId(),
							listApplyPayError.get(f).getPaymentSeq(), listApplyPayError.get(f).getRetries());
				}
			}
		}
	}

	/**
	 * Apply pay by register.
	 *
	 * @param manager the manager
	 * @param cycleCalendar the cycle calendar
	 * @param df the df
	 * @param listAccSons the list acc sons
	 * @param acctSons the acct sons
	 * @param bankProccesor the bank proccesor
	 * @throws PersistenceException the persistence exception
	 */
	private void applyPayByRegister(DAManager manager, Calendar cycleCalendar, final SimpleDateFormat df,
			final List<DAGroupPaymentDTO> listAccSons, String[] acctSons, String bankProccesor)
			throws PersistenceException {
		for (int c = 0; c < acctSons.length; c++) {

			// Se llama al metodo appyPay para aplicar pago
			applyPay(manager, cycleCalendar, df, removeZero(acctSons[c].toString()), listAccSons,
					DAListenerConstants.CODE_SUCCESS, DAListenerConstants.STATUS_1, bankProccesor);
		}
	}

	/**
	 * Update bank proc detail pay.
	 *
	 * @param manager the manager
	 * @param acctSons the acct sons
	 * @param errorBank the error bank
	 * @throws PersistenceException the persistence exception
	 */
	private void updateBankProcDetailPay(DAManager manager, String[] acctSons, String errorBank)
			throws PersistenceException {
		for (int c = 0; c < acctSons.length; c++) {

			// Se guarda el codigo de error y se actualiza status 1
			manager.updateBankProcDetailPay(errorBank, DAListenerConstants.STATUS_NEG1, "", acctSons[c].toString(),
					DAListenerConstants.STATUS_0, DAListenerConstants.STATUS_1, DAListenerConstants.STATUS_0);
		}
	}

	/**
	 * Apply pay.
	 *
	 * @param manager the manager
	 * @param cycleCalendar the cycle calendar
	 * @param df the df
	 * @param acctCode the acct code
	 * @param listAccSons the list acc sons
	 * @param errorBank the error bank
	 * @param groupPayment the group payment
	 * @param bankProcessor the bank processor
	 * @throws PersistenceException the persistence exception
	 */
	private void applyPay(DAManager manager, Calendar cycleCalendar, final SimpleDateFormat df, String acctCode,
			final List<DAGroupPaymentDTO> listAccSons, String errorBank, long groupPayment, String bankProcessor)
			throws PersistenceException {

		// Lista de los acctcode en tabla bank process detail
		List<DABankProcessDetailPayDTO> listacctBPD = manager.selectBankProcessDetailPay(acctCode, 0,
				listAccSons.get(0).getIdBankProcess());

		// servicio order
		URL url = null;
		Order order = null;

		try {
			url = new URL(configParams.get(DAListenerConstants.WSDL_EXOR_COMPLEX));
			order = new ExecuteOrderService(url).getExecuteOrderPort();
		} catch (Exception e1) {
			LOGGER.error("ERROR en aplicacion de pago: Error en creacion de consumo ExecuteOrderService: "
					+ e1.getMessage());
			e1.printStackTrace();
			String uuidEr = UUID.randomUUID().toString();
			manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR1,
					"ERROR en aplicacion de pago: Error en creacion de consumo ExecuteOrderService: " + e1.getMessage(),
					listAccSons.get(0).getIdBankProcess(), "");
		}

		if (listacctBPD != null) {
			for (int d = 0; d < listacctBPD.size(); d++) {
				String accountCode = listacctBPD.get(d).getAcctCode();
				String invoiceId = listacctBPD.get(d).getInvoiceId();

				// Se obtiene monto por cada registro
				double amount = Double.valueOf(listacctBPD.get(d).getAmount());

				List<Parameter> parameter = obtainParameters(accountCode, listacctBPD.get(d).getSubscriberId(),
						invoiceId, df.format(cycleCalendar.getTime()), String.valueOf(amount),
						configParams.get(bankProcessor));

				AdditionalParameters additionalParameters = DAListenerUtils.getAdditionalParameters(parameter);
				SimpleOrderRequest request = null;
				OrderResponse orderResponse = null;
				String uuid = UUID.randomUUID().toString();
				JSONObject jsonObject = null;

				try {
					// Se hace llamado de servicio ExecuteOrderService

					request = DAListenerUtils.getRequestOrder(accountCode, additionalParameters,
							configParams.get(DAListenerConstants.COMMENT_ACTIVATE), DAListenerConstants.CHANNEL_ID_98,
							Long.valueOf(configParams.get(DAListenerConstants.PRODUCT_ID)));
					jsonObject = new JSONObject(request);
					orderResponse = order.activateProduct(request);

				} catch (Exception e) {

					LOGGER.error("ERROR en aplicacion de pago: Error en consumo de servicio ExecuteOrderService: "
							+ e.getLocalizedMessage());
					manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
							"ERROR en aplicacion de pago: Error en consumo de servicio ExecuteOrderService: "
									+ accountCode + " ==> " + e.getMessage(),
							listAccSons.get(0).getIdBankProcess(), jsonObject.toString());
				}
				if (orderResponse != null) {

					String responseCode = orderResponse.getOrderResponseDetail().get(0).getParameters().getParameter()
							.get(0).getValue();

					String responseMessage = orderResponse.getOrderResponseDetail().get(0).getParameters()
							.getParameter().get(1).getValue();

					String responsePayId = orderResponse.getOrderResponseDetail().get(0).getParameters().getParameter()
							.get(2).getValue();

					String responseTXId = orderResponse.getTransactionID();

					// Se valida la respuesta de BPMN
					if (!responseCode.equals("0") || responsePayId == "") {

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
								"Se presento un error en la aplicacion de pago, para la cuenta: " + accountCode
										+ " el servicio ExecuteOrderService contesto: " + responseMessage,
								listAccSons.get(0).getIdBankProcess(), jsonObject.toString());

						// Se guarda con status -1 si BPMN presento error
						manager.updateBankProcDetailPayByOne(errorBank, DAListenerConstants.STATUS_NEG1, responseTXId,
								acctCode, DAListenerConstants.STATUS_0, groupPayment, invoiceId, responsePayId,
								DAListenerConstants.STATUS_0);
					} else {

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR0,
								"Se realizo la aplicacion de pago, para la cuenta: " + accountCode + " "
										+ responseMessage,
								listAccSons.get(0).getIdBankProcess(), jsonObject.toString());

						// Se guarda con status 1 si BPMN response successful
						manager.updateBankProcDetailPayByOne(errorBank, DAListenerConstants.STATUS_1, responseTXId,
								acctCode, DAListenerConstants.STATUS_0, groupPayment, invoiceId, responsePayId,
								DAListenerConstants.STATUS_0);
					}
				} else {
					// Se guarda con status -1 si BPMN presento error
					manager.updateBankProcDetailPayByOne(errorBank, DAListenerConstants.STATUS_NEG1, "", acctCode,
							DAListenerConstants.STATUS_0, groupPayment, invoiceId, "", DAListenerConstants.STATUS_0);
				}
			}
		}
	}

	/**
	 * Obtain parameters.
	 *
	 * @param accountCode the account code
	 * @param subscriberId the subscriber id
	 * @param invoiceId the invoice id
	 * @param date the date
	 * @param amount the amount
	 * @param bankProcessor the bank processor
	 * @return the list
	 */
	private List<Parameter> obtainParameters(String accountCode, String subscriberId, String invoiceId, String date,
			String amount, String bankProcessor) {
		List<Parameter> parameter = new ArrayList<Parameter>();

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.ID_TYPE,
				DAListenerConstants.ACCTCODE);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.ID, accountCode);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.SUBSCRIBERID, subscriberId);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.PAY_TYPE_EOC,
				configParams.get(DAListenerConstants.PAY_TYPE_EOC_MSG));

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.DOCUMENT_NUMBER, invoiceId);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.PAY_DATE, date);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.AMOUNT, amount);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.CURRENCY,
				DAListenerConstants.CURRENCY_TYPE);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.PAY_CHANNEL, DAListenerConstants.DA);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.BANK, bankProcessor);

		parameter = DAListenerUtils.obtainParameter(parameter, DAListenerConstants.RESPONSE,
				DAListenerConstants.SUCCESS);

		return parameter;
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
			LOGGER.error("ApplyPayBankThread " + e.getMessage(), e);
		}
	}

	/**
	 * Removes the zero.
	 *
	 * @param acctCode the acct code
	 * @return the string
	 */
	private String removeZero(String acctCode) {

		for (int i = 0; i < acctCode.length();) {
			String a = String.valueOf(acctCode.charAt(i));
			if (a.equals("0")) {
				acctCode = acctCode.substring(i + 1, acctCode.length());
			} else {
				return acctCode;
			}
		}
		return acctCode;
	}

}
