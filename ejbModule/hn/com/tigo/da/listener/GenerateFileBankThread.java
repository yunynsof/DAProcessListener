package hn.com.tigo.da.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
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
import com.jayway.jsonpath.JsonPath;
import com.tigo.enterprise.resources.parameters.simple.v1.schema.ParameterArray;
import com.tigo.enterprise.resources.parameters.simple.v1.schema.ParameterType;

import hn.com.tigo.core.da.dto.DABankFileIncluExcluDTO;
import hn.com.tigo.core.da.dto.DABankIncluExcluDTO;
import hn.com.tigo.core.da.dto.DABankProcessDTO;
import hn.com.tigo.core.da.dto.DACardInfoDTO;
import hn.com.tigo.core.da.dto.DAConfigCyclesDTO;
import hn.com.tigo.core.da.dto.DADetailReprocessDTO;
import hn.com.tigo.core.da.dto.DAFieldStringDTO;
import hn.com.tigo.core.da.dto.DAGroupNoCardDTO;
import hn.com.tigo.core.da.dto.DAGroupPaymentDTO;
import hn.com.tigo.core.da.dto.DANoCardGroupPayDTO;
import hn.com.tigo.core.da.manager.DAManager;
import hn.com.tigo.core.invoice.utils.States;
import hn.com.tigo.da.listener.model.AcctAccessCode;
import hn.com.tigo.da.listener.model.AdditionalProperty;
import hn.com.tigo.da.listener.model.BacMovilModel;
import hn.com.tigo.da.listener.model.FicMovilModel;
import hn.com.tigo.da.listener.model.InvoiceHeaderFilter;
import hn.com.tigo.da.listener.model.InvoiceInfo;
import hn.com.tigo.da.listener.model.JsonRequestCBSInvoice;
import hn.com.tigo.da.listener.model.JsonResponseCBSInvoice;
import hn.com.tigo.da.listener.model.ListAdditionalProperty;
import hn.com.tigo.da.listener.model.QueryObj;
import hn.com.tigo.da.listener.util.DAListenerConstants;
import hn.com.tigo.da.listener.util.DAListenerUtils;
import hn.com.tigo.josm.adapter.requesttype.v1.TaskRequestType;
import hn.com.tigo.josm.adapter.requesttype.v1.TaskResponseType;
import hn.com.tigo.josm.orchestrator.adapter.cbs2.task.CBSQueryCustomerInfoTask;
import hn.com.tigo.josm.orchestrator.adapter.cbs2.task.CBSQueryCustomerInfoTaskService;
import hn.com.tigo.josm.orchestrator.adapter.cbs2.task.CBSQueryInvoiceEnhancedTask;
import hn.com.tigo.josm.orchestrator.adapter.cbs2.task.CBSQueryInvoiceEnhancedTaskService;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJB;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJBLocal;
import hn.com.tigo.josm.persistence.exception.PersistenceException;

/**
 * GenerateFileBankThread.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:21:26 AM 2022
 */
public class GenerateFileBankThread extends Thread {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(GenerateFileBankThread.class);

	/** The executor service. */
	private ThreadPoolExecutor executorService;

	/** The working queue. */
	private BlockingQueue<Runnable> workingQueue;

	/** The state. */
	private States state;

	/** The config params. */
	private HashMap<String, String> configParams;

	/**
	 * Instantiates a new generate file bank thread.
	 */
	public GenerateFileBankThread() {
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

				Calendar cycleCalendar = Calendar.getInstance();
				final SimpleDateFormat df = new SimpleDateFormat(
						configParams.get(DAListenerConstants.FORMAT_DATE_YYMM));
				final SimpleDateFormat dft = new SimpleDateFormat(
						configParams.get(DAListenerConstants.FORMAT_DATE_DDMMYYYY));
				String fileNamePay = null;
				String fileInPut = "";

				List<BacMovilModel> listBacMovil = new ArrayList<BacMovilModel>();
				List<FicMovilModel> listFicMovil = new ArrayList<FicMovilModel>();

				int ListCISize = 0;

				try {
					urlEmail = new URL(configParams.get(DAListenerConstants.EMAIL_SERVICE_PROV));
				} catch (MalformedURLException e1) {
					LOGGER.error("ERROR: en obtencion de URL EmailService: " + e1.getMessage());
					e1.printStackTrace();
					String uuidEr = UUID.randomUUID().toString();
					manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR2,
							"ERROR: en obtencion de URL EmailService " + e1.getMessage(), "", "");
				}

				// Se carga lista de bankProcess en estatus=0
				final List<DABankProcessDTO> listBP = manager.selectDABankProcess(DAListenerConstants.STATUS_BPDP);
				if (listBP.size() > 0) {
					// cambia status a 1
					for (int b = 0; b < listBP.size(); b++) {
						manager.updateStatusBankProcess(1, 0, listBP.get(b).getId());
					}

					for (int i = 0; i < listBP.size(); i++) {

						long startTime = System.nanoTime();
						BPiD = listBP.get(i).getId();

						// Se procesa el bank process, yendo a card_info y leer el ciclo proveniente de
						// bank process
						if (configParams.get(DAListenerConstants.BAC_ID)
								.equals(String.valueOf(listBP.get(i).getBankid()))) {

							bankProcessor = configParams.get(DAListenerConstants.BANK_PROC_BAC);
							ListCISize = InsertBankProcessDetail(manager, listBP, i, DAListenerConstants.BANK_PROC_BAC,
									urlEmail);
						} else if (configParams.get(DAListenerConstants.FICOHSA_ID)
								.equals(String.valueOf(listBP.get(i).getBankid()))) {

							bankProcessor = configParams.get(DAListenerConstants.BANK_PROC_FICOHSA);
							ListCISize = InsertBankProcessDetail(manager, listBP, i,
									DAListenerConstants.BANK_PROC_FICOHSA, urlEmail);
						}

						if (listBP.get(i).getId() != null) {
							// se levanta una lista de los acctcode agrupados por bank process
							final List<DAFieldStringDTO> listGroupAcctC = manager
									.selectDAGroupAcc(listBP.get(i).getId());

							// se inserta el agrupado consolidado (suma de amount y taxAmount) por acctcode
							// en Group_acctcode
							for (int f = 0; f < listGroupAcctC.size(); f++) {

								manager.insertGroupAcct(listGroupAcctC.get(f).getFieldString(), listBP.get(i).getId());
								manager.updateGroupAcct(listGroupAcctC.get(f).getFieldString(), listBP.get(i).getId());
							}

							// se levanta una lista de agrupado de tabla group_acctcode, de numero de
							// tarjetas mediante group_payment =1 y bank process
							final List<DANoCardGroupPayDTO> listNoCard = manager.selectDANoCard(listBP.get(i).getId(),
									"1");

							for (int e = 0; e < listNoCard.size(); e++) {
								// se inserta en group payment el registro mas antiguo de acctcode, por
								// group_payment=1, numero de tarjeta y bank process
								manager.insertGroupPayment1(listBP.get(i).getId(), 1, listNoCard.get(e).getNoCard());

								// Se levanta lista de group_acctcode para obtener ACCTCODE,AMOUNT,TAX_AMOUNT
								// por group_payment=1, numero de tarjeta y bank process
								final List<DAGroupNoCardDTO> listGroupNoCard = manager
										.selectGroupNoCard(listBP.get(i).getId(), 1, listNoCard.get(e).getNoCard());

								String listAcctCode = "";
								double amountTot = 0;
								long taxAmountTot = 0;

								for (int f = 0; f < listGroupNoCard.size(); f++) {
									listAcctCode = listAcctCode + listGroupNoCard.get(f).getAcctCode() + ";";
									amountTot = amountTot + Double.valueOf(listGroupNoCard.get(f).getAmount());
									taxAmountTot = taxAmountTot + Long.valueOf(listGroupNoCard.get(f).getTaxAmount());
								}

								// se agregan los consolidados por group_payment=1, numero de tarjeta y bank
								// process a cada acctcode padre

								BigDecimal bd = new BigDecimal(amountTot).setScale(2, RoundingMode.HALF_UP);
								amountTot = bd.doubleValue();

								manager.updateGroupPayAcc(listAcctCode, String.valueOf(amountTot),
										String.valueOf(taxAmountTot), 1, listNoCard.get(e).getNoCard(),
										listBP.get(i).getId());

							}
							// se inserta en tabla group_payment los registro de group_acctcode que tengan
							// group_payment=0 y bank process
							manager.insertGroupPayment0(listBP.get(i).getId(), 0);

							// Generacion de archivos
							if (configParams.get(DAListenerConstants.BAC_ID)
									.equals(String.valueOf(listBP.get(i).getBankid()))) {

								final List<DAGroupPaymentDTO> listGroupPayment = manager
										.selectDAGroupPayment(listBP.get(i).getId(), 0);

								for (int g = 0; g < listGroupPayment.size(); g++) {

									DAFieldStringDTO name = manager.selectNameCardInfo(
											listGroupPayment.get(g).getNoCard(),
											listGroupPayment.get(g).getAcctFather(), 1,
											listGroupPayment.get(g).getGroupPayment(),
											configParams.get(DAListenerConstants.BANK_PROC_BAC));

									double amountFather = Double.valueOf(listGroupPayment.get(g).getAmountFather());
									double taxAmountFather = Double
											.valueOf(listGroupPayment.get(g).getTaxAmountFather()) / 1000000;

									BigDecimal bd2 = new BigDecimal(taxAmountFather).setScale(2, RoundingMode.HALF_UP);
									double taxFinal = bd2.doubleValue();

									BacMovilModel bacmodel = new BacMovilModel();

									bacmodel.setType(configParams.get(DAListenerConstants.CONSTANTS_BAC));
									bacmodel.setNameClient(name.getFieldString());
									bacmodel.setCreateDate(dft.format(cycleCalendar.getTime()));
									bacmodel.setNumRef(listGroupPayment.get(g).getAcctFather());

									String amountWithZero = addZeroDecimal(String.valueOf(amountFather));
									bacmodel.setAmount(amountWithZero.replace(DAListenerConstants.DOT_CHAR, ""));
									bacmodel.setSequence(String.valueOf(g + 1));

									String taxWithZero = addZeroDecimal(String.valueOf(taxFinal));
									bacmodel.setTax(taxWithZero.replace(DAListenerConstants.DOT_CHAR, ""));

									listBacMovil.add(bacmodel);

								}

								String cycle = DAListenerUtils.getCycle(listBP.get(i).getCycle());

								// Nombre de archivo
								fileNamePay = configParams.get(DAListenerConstants.IDENTIFIER_BAC)
										+ df.format(cycleCalendar.getTime())
										+ getCycles(cycle, DAListenerConstants.AS400, manager)
										+ configParams.get(DAListenerConstants.FILE_EXTENSION);

								final List<DAFieldStringDTO> sumTotalGPByBP = manager
										.sumTotalGPByBP(listBP.get(i).getId());

								String amountFinal = sumTotalGPByBP.get(0).getFieldString();
								amountFinal = (amountFinal==null)?"0.0":amountFinal;								

								// Cuerpo de archivo
								fileInPut = configParams.get(DAListenerConstants.CREDOMATIC_CONST)
										+ spaceZero(getCycles(cycle, DAListenerConstants.BAC, manager), 3)
										+ spaceZero(String.valueOf(listGroupPayment.size()), 6)
										+ spaceZero(amountFinal.replace(DAListenerConstants.DOT_CHAR, ""), 12) + "\r\n";

								for (int d = 0; d < listBacMovil.size(); d++) {
									fileInPut = fileInPut + listBacMovil.get(d).getType()
											+ spaceEmpty(listBacMovil.get(d).getNameClient(), 30)
											+ dft.format(cycleCalendar.getTime()) + spaceEmpty("", 10)
											+ spaceZero(listBacMovil.get(d).getNumRef(), 10)
											+ spaceZero(listBacMovil.get(d).getAmount(), 10)
											+ spaceZero(listBacMovil.get(d).getSequence(), 5) + " "
											+ spaceZero(listBacMovil.get(d).getTax(), 13) + "\r\n";
								}

								// Se genera archivo Inclusiones y exclusiones
								// Se levanta lista con estatus = 0 y ciclo de bank process

								List<DABankIncluExcluDTO> listbankIE = manager
										.selectDABankIncluExclu(DAListenerConstants.STATUS_0, cycle);

								if (listbankIE.size() > 0) {

									String fileNamePayIE = configParams.get(DAListenerConstants.CONSTANT_INCLU_EXCLU)
											+ df.format(cycleCalendar.getTime())
											+ getCycles(cycle, DAListenerConstants.AS400, manager)
											+ configParams.get(DAListenerConstants.FILE_EXTENSION);
									String fileInPutIE = "";

									// Se pasan a estatus = 1
									manager.updateBankIncluExclu(DAListenerConstants.STATUS_1, fileNamePayIE,
											DAListenerConstants.STATUS_0, cycle);

									for (int h = 0; h < listbankIE.size(); h++) {

										fileInPutIE = fileInPutIE
												+ spaceZero(getCycles(cycle, DAListenerConstants.BAC, manager), 3)
												+ listbankIE.get(h).getTypeTran() + spaceEmpty("", 10)
												+ spaceZero(listbankIE.get(h).getAnexo(), 10)
												+ spaceEmpty(methodGet(listbankIE.get(h).getNoCard(),
														DAListenerConstants.CRED_TAR_DECRYPT, manager,
														listbankIE.get(h).getAnexo() + "Generated ExluInclu",
														listBP.get(i).getId()), 16)
												+ dft.format(cycleCalendar.getTime()) + listbankIE.get(h).getExpDate()
												+ "\r\n";

									}

									int flag = 0;
									if (listBP.get(i).getUuidInlcluExclu() != null) {

										List<DABankFileIncluExcluDTO> fileIncluExclu = manager
												.selectDAFileIncluExclu(listBP.get(i).getId());

										if (fileIncluExclu.size() > 0) {

											byte[] base64 = Base64.getDecoder()
													.decode(fileIncluExclu.get(0).getFileBase64());
											String fileString = new String(base64, StandardCharsets.UTF_8);
											fileInPutIE = fileString + fileInPutIE;

											flag = 1;
											manager.updateFileIncluExclu(
													Base64.getEncoder().encodeToString(
															fileInPutIE.getBytes(StandardCharsets.UTF_8)),
													listBP.get(i).getId());
										}
									}

									if (flag == 0) {
										// Se inserta en tabla da_bank_file_inclu_exclu

										manager.insertBankFileIncluExclu(listBP.get(i).getId(), fileNamePayIE,
												Base64.getEncoder()
														.encodeToString(fileInPutIE.getBytes(StandardCharsets.UTF_8)));
									}

									String uuidEr = UUID.randomUUID().toString();
									manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR0,
											"Se culmino la generacion de archivo Inclusiones y Exclusiones con id: "
													+ listBP.get(i).getId(),
											listBP.get(i).getId(), "");

								}

								List<DAFieldStringDTO> sizeVal = manager.countValidate(listBP.get(i).getId(), 0);

								// Se inserta base64 final en DA_BANK_PROCESS
								manager.updateBankProcess(constructBanProcess(listBP.get(i).getId(),
										String.valueOf(ListCISize), amountFinal,
										Integer.valueOf((sizeVal.get(0).getFieldString() != null
												? sizeVal.get(0).getFieldString()
												: "0")),
										DAListenerConstants.STATUS_GENERATED, cycleCalendar.getTime(),
										cycleCalendar.getTime(),
										Base64.getEncoder().encodeToString(fileInPut.getBytes(StandardCharsets.UTF_8)),
										fileNamePay, "", listBP.get(i).getId()));

								String uuid2 = UUID.randomUUID().toString();
								manager.insertLogs(uuid2, DAListenerConstants.TYPE_ERROR0,
										"Generacion de archivo de salida: Se Procesaron " + String.valueOf(ListCISize)
												+ " Registros, de las cuales " + String.valueOf(listBacMovil.size())
												+ " fueron líneas válidas para el ciclo " + cycle + " Banco: "
												+ configParams.get(DAListenerConstants.BANK_PROC_BAC),
										listBP.get(i).getId(), "");

								DAListenerUtils.sendEmailEnd(configParams.get(DAListenerConstants.BODY_EMAIL),
										configParams.get(DAListenerConstants.GENERATED_FILE_END), urlEmail,
										configParams.get(DAListenerConstants.BANK_PROC_BAC), "", "", "",
										String.valueOf(ListCISize), String.valueOf(listBacMovil.size()), cycle,
										configParams.get(DAListenerConstants.EMAILS),
										configParams.get(DAListenerConstants.FROM_EMAIL),
										configParams.get(DAListenerConstants.GENERATED_FILE_SUBJ), manager);

								// Generacion de archivo Ficohsa
							} else if (configParams.get(DAListenerConstants.FICOHSA_ID)
									.equals(String.valueOf(listBP.get(i).getBankid()))) {

								final List<DAGroupPaymentDTO> listGroupPayment = manager
										.selectDAGroupPayment(listBP.get(i).getId(), 0);

								for (int g = 0; g < listGroupPayment.size(); g++) {

									URL url = null;
									CBSQueryCustomerInfoTask order = null;

									try {
										
										url = new URL(configParams.get(DAListenerConstants.WSDL_CBS_INVOICE_DA));
										order = new CBSQueryCustomerInfoTaskService(url).getCBSQueryCustomerInfoTaskPort();
										
									} catch (Exception e1) {
										LOGGER.error(
												"ERROR: creacion de consumo QueryCustomerService: " + e1.getMessage());
										e1.printStackTrace();
										String uuidEr = UUID.randomUUID().toString();
										manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR1,
												"ERROR: creacion de consumo QueryCustomerService "
														+ listGroupPayment.get(g).getAcctFather() + " ==> "
														+ e1.getMessage(),
												listBP.get(i).getId(), "");
									}

									String accountCode = listGroupPayment.get(g).getAcctFather();
				
									TaskResponseType orderResponse = null;
									TaskRequestType request = null;
									String uuid = UUID.randomUUID().toString();

									try {
										
										request = getRequestCBSQueryCustomer(accountCode);
										JSONObject jsonObject = new JSONObject(request);

										orderResponse = order.executeTask(request);

										manager.insertLogs(uuid,
												(orderResponse.getResponseCode() != 0 ? DAListenerConstants.TYPE_ERROR2
														: DAListenerConstants.TYPE_ERROR0),
												"Consumo de servicio QueryCustomerService; Consulta informacion cliente "
														+ orderResponse.getResponseDescription(),
												listBP.get(i).getId(), jsonObject.toString());
										
										String idNumber = "";

										if (orderResponse != null) {
											String response = orderResponse.getParameters().getParameter().get(0).getValue();

											JSONObject responseQC = new JSONObject(response);
											JSONObject resultMsg = responseQC.getJSONObject("QueryCustomerInfoResultMsg");
											JSONObject infoResult = resultMsg.getJSONObject("QueryCustomerInfoResult");
											JSONObject resultAccount = infoResult.getJSONObject("Account");
											JSONObject resultAcctInfo = resultAccount.getJSONObject("AcctInfo");
											JSONObject resultUsCust = resultAcctInfo.getJSONObject("UserCustomer");
											JSONObject resultIndInf = resultUsCust.getJSONObject("IndividualInfo");
											idNumber = resultIndInf.get("IDNumber").toString();
										}

										if (!idNumber.equals("")) {
											
											double amountFather = Double.valueOf(listGroupPayment.get(g).getAmountFather());
											double taxAmountFather = Double
													.valueOf(listGroupPayment.get(g).getTaxAmountFather()) / 1000000;

											BigDecimal bd2 = new BigDecimal(taxAmountFather).setScale(2,
													RoundingMode.HALF_UP);
											double taxFinal = bd2.doubleValue();

											FicMovilModel ficModel = new FicMovilModel();

											ficModel.setTelef3(listGroupPayment.get(g).getSubscriberId());
											ficModel.setAnoxofi(listGroupPayment.get(g).getAcctFather());
											ficModel.setConpos(DAListenerConstants.PAYMENT_TYPE);
											ficModel.setEdenti(idNumber);

											ficModel.setTajetaFic(methodGet(listGroupPayment.get(g).getNoCard(),
													DAListenerConstants.CRED_TAR_DECRYPT, manager,
													listGroupPayment.get(g).getAcctFather(),
													listGroupPayment.get(g).getIdBankProcess()));

											String amountWithZero = addZeroDecimal(String.valueOf(amountFather));
											ficModel.setValorFi(amountWithZero);

											String taxWithZero = addZeroDecimal(String.valueOf(taxFinal));
											ficModel.setImpuesto1(taxWithZero);
											ficModel.setImpuesto2(configParams.get(DAListenerConstants.IMPUESTO2));
											ficModel.setMonedaFi(configParams.get(DAListenerConstants.LPS));
											ficModel.setTasa2Fi(configParams.get(DAListenerConstants.TASA2FI));
											ficModel.setLeyenda(configParams.get(DAListenerConstants.LEYENDA)
													+ listGroupPayment.get(g).getInvoiceId());

											listFicMovil.add(ficModel);

										}

									} catch (Exception e) {

										LOGGER.error(
												"ERROR: en consumo de servicio QueryCustomerService: Consulta informacion cliente " + e.getMessage());
										e.printStackTrace();
										JSONObject jsonObject = new JSONObject(request);

										manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
												"Error de Consumo Servicio QueryCustomerService Consulta informacion cliente "
														+ listGroupPayment.get(g).getAcctFather() + " ==> "
														+ e.getMessage(),
												listBP.get(i).getId(), jsonObject.toString());
									}
								
								}

								String cycle = DAListenerUtils.getCycle(listBP.get(i).getCycle());

								// Nombre de archivo
								String fileNamePayFic = configParams.get(DAListenerConstants.IDENTIFIER_FICOHSA)
										+ df.format(cycleCalendar.getTime())
										+ getCycles(cycle, DAListenerConstants.AS400, manager)
										+ configParams.get(DAListenerConstants.FILE_EXTENSION);

								final List<DAFieldStringDTO> sumTotalGPByBP = manager
										.sumTotalGPByBP(listBP.get(i).getId());

								String amountFinal = sumTotalGPByBP.get(0).getFieldString();

								// Cuerpo de archivo
								String fileInPutFic = "";
								for (int d = 0; d < listFicMovil.size(); d++) {
									fileInPutFic = fileInPutFic + spaceZero(listFicMovil.get(d).getTelef3(), 10) + ";"
											+ spaceZero(listFicMovil.get(d).getAnoxofi(), 10) + ";"
											+ listFicMovil.get(d).getConpos() + ";" + listFicMovil.get(d).getEdenti()
											+ ";" + listFicMovil.get(d).getTajetaFic() + ";"
											+ spaceZero(listFicMovil.get(d).getValorFi(), 14) + ";"
											+ spaceZero(listFicMovil.get(d).getImpuesto1(), 13) + ";"
											+ listFicMovil.get(d).getImpuesto2() + ";"
											+ listFicMovil.get(d).getMonedaFi() + ";" + listFicMovil.get(d).getTasa2Fi()
											+ ";" + listFicMovil.get(d).getLeyenda() + "\r\n";
								}

								List<DAFieldStringDTO> sizeVal = manager.countValidate(listBP.get(i).getId(), 0);
								fileInPutFic = (fileInPutFic.equals("") ? " " : fileInPutFic);

								// Se inserta base64 final en DA_BANK_PROCESS
								manager.updateBankProcess(constructBanProcess(listBP.get(i).getId(),
										String.valueOf(ListCISize), amountFinal,
										Integer.valueOf((sizeVal.get(0).getFieldString() != null
												? sizeVal.get(0).getFieldString()
												: "0")),
										DAListenerConstants.STATUS_GENERATED, cycleCalendar.getTime(),
										cycleCalendar.getTime(),
										Base64.getEncoder()
												.encodeToString(fileInPutFic.getBytes(StandardCharsets.UTF_8)),
										fileNamePayFic, "", ""));

								String uuid = UUID.randomUUID().toString();
								manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR0,
										"Generacion de archivo de salida: Se Procesaron " + String.valueOf(ListCISize)
												+ " Registros, de las cuales " + String.valueOf(listFicMovil.size())
												+ " fueron líneas válidas para el ciclo " + cycle + " Banco: "
												+ configParams.get(DAListenerConstants.BANK_PROC_FICOHSA),
										listBP.get(i).getId(), "");

								DAListenerUtils.sendEmailEnd(configParams.get(DAListenerConstants.BODY_EMAIL),
										configParams.get(DAListenerConstants.GENERATED_FILE_END), urlEmail,
										configParams.get(DAListenerConstants.BANK_PROC_FICOHSA), "", "", "",
										String.valueOf(ListCISize), String.valueOf(listFicMovil.size()), cycle,
										configParams.get(DAListenerConstants.EMAILS),
										configParams.get(DAListenerConstants.FROM_EMAIL),
										configParams.get(DAListenerConstants.GENERATED_FILE_SUBJ), manager);

							}
						}

						long endTime = System.nanoTime();
						long duration = (endTime - startTime);
						NewRelicImpl.addNewRelicMetric("GenerateFileBankThread", duration / 1000000);
					}
				}
			} catch (PersistenceException error) {
				error.printStackTrace();
				NewRelicImpl.addNewRelicError(error.getMessage());

				LOGGER.error("GenerateFileBankThread " + this.getClass().getName() + error.getMessage(), error);
				String uuid = UUID.randomUUID().toString();
				if (manager != null) {
					try {
						manager.updateStatusBankProcess(-1, 1, BPiD);

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR1,
								"Error en proceso GenerateFileBankThread, se presenta el siguiente error: "
										+ error.getMessage(),
								BPiD, "");

						if (!BPiD.equals("")) {
							DAListenerUtils.sendEmailError(configParams.get(DAListenerConstants.BODY_EMAIL),
									configParams.get(DAListenerConstants.GENERATED_FILE_ERROR), urlEmail, bankProcessor,
									error.getMessage(), configParams.get(DAListenerConstants.EMAILS),
									configParams.get(DAListenerConstants.FROM_EMAIL),
									configParams.get(DAListenerConstants.GENERATED_FILE_SUBJ), manager);
						}

					} catch (Exception e) {
						LOGGER.error("Error de proceso GenerateFileBankThread: " + e.getMessage());
						e.printStackTrace();
					}
				}
			} finally {

				if (manager != null) {
					try {
						manager.close();
					} catch (Exception e) {
						LOGGER.error("GenerateFileBankThread " + e.getMessage(), e);
					}
				}
				this.sleepThread(20000/* Integer.parseInt(configParams.get(DAListenerConstants.SLEEP_THREAD)) */);
			}
		}
		executorService.shutdown();

	}


	/**
	 * Insert bank process detail.
	 *
	 * @param manager the manager
	 * @param listBP the list BP
	 * @param i the i
	 * @param bankProcessor the bank processor
	 * @param urlEmail the url email
	 * @return the int
	 * @throws PersistenceException the persistence exception
	 */
	private int InsertBankProcessDetail(DAManager manager, final List<DABankProcessDTO> listBP, int i,
			String bankProcessor, URL urlEmail) throws PersistenceException {

		// Lista de cardInfo mediante ciclo y banco procesador
		LOGGER.info(DAListenerUtils.getCycle(listBP.get(i).getCycle()));
		String cycle = DAListenerUtils.getCycle(listBP.get(i).getCycle());
		List<DACardInfoDTO> listCI = new ArrayList<DACardInfoDTO>();

		if (listBP.get(i).getReprocess().equals("1")) {

			List<DADetailReprocessDTO> listDReprocess = manager.selectDADReprocess(listBP.get(i).getId(),
					String.valueOf(listBP.get(i).getCycle()));

			for (int k = 0; k < listDReprocess.size(); k++) {

				List<DACardInfoDTO> listCIRep = manager.selectDACIReprocess(DAListenerConstants.STATUS_ACTIVE,
						DAListenerUtils.getCycle(Long.parseLong(listDReprocess.get(k).getCycle())),
						configParams.get(bankProcessor), listDReprocess.get(k).getAccount(),
						listDReprocess.get(k).getSubscriber());

				for (int l = 0; l < listCIRep.size(); l++) {

					int size = listCI.size();
					listCI.add(size, listCIRep.get(l));
				}
			}

		} else {
			listCI = manager.selectDACardInfo(DAListenerConstants.STATUS_ACTIVE, cycle,
					configParams.get(bankProcessor));
		}
		if (listCI != null) {

			DAListenerUtils.sendEmailInit(configParams.get(DAListenerConstants.BODY_EMAIL),
					configParams.get(DAListenerConstants.GENERATED_FILE_INIT), urlEmail,
					configParams.get(bankProcessor), String.valueOf(listCI.size()), cycle,
					configParams.get(DAListenerConstants.EMAILS), configParams.get(DAListenerConstants.FROM_EMAIL),
					configParams.get(DAListenerConstants.GENERATED_FILE_SUBJ), manager);

			for (int a = 0; a < listCI.size(); a++) {
				URL url = null;
				CBSQueryInvoiceEnhancedTask cBSQueryInvoiceEnhanced = null;

				try {
					url = new URL(configParams.get(DAListenerConstants.WSDL_CBS_INVOICE));
					cBSQueryInvoiceEnhanced = new CBSQueryInvoiceEnhancedTaskService(url)
							.getCBSQueryInvoiceEnhancedTaskPort();
				} catch (Exception e1) {
					LOGGER.error("ERROR: creacion de consumo de servicio InvoiceCBS: " + e1.getMessage());
					e1.printStackTrace();
					String uuid = UUID.randomUUID().toString();
					manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR1,
							"ERROR: creacion de consumo de servicio InvoiceCBS " + e1.getMessage(),
							listBP.get(i).getId(), "");
				}

				TaskResponseType taskResponseType = null;
				TaskRequestType request = null;
				try {
					request = getRequestCBSInvoice(listCI.get(a).getAcctcode());
					taskResponseType = cBSQueryInvoiceEnhanced.executeTask(request);

				} catch (Exception e) {
					LOGGER.error("ERROR: en consumo de servicio InvoiceCBS: " + e.getMessage());
					e.printStackTrace();
					String uuid = UUID.randomUUID().toString();

					manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
							"Error de Consumo Servicio Invoice CBS " + listCI.get(a).getAcctcode() + " ==> "
									+ e.getMessage(),
							listBP.get(i).getId(), request.getParameters().getParameter().get(0).getValue());
				}

				if (taskResponseType != null) {

					LOGGER.info(listCI.get(a).getSubscriberId());
					String response = taskResponseType.getParameters().getParameter().get(0).getValue();

					LOGGER.info(response);

					// Se eliminan prefijos ars: cb: arc: de jsonResponse
					response = deleteWord(response, DAListenerConstants.ARS, DAListenerConstants.CBS,
							DAListenerConstants.ARC, DAListenerConstants.XMLNS);
					JsonResponseCBSInvoice jsonResponse = new JsonResponseCBSInvoice();

					String invoiceInfo = ObtainResponseInvoice(response, DAListenerConstants.ATTR_INVO_INFO, manager,
							request.getParameters().getParameter().get(0).getValue(), listCI.get(a).getAcctcode(),
							listBP.get(i).getId());

					if (invoiceInfo != null) {
						Gson gson = new Gson();
						jsonResponse = gson.fromJson(invoiceInfo, JsonResponseCBSInvoice.class);

						// Se obtienen las facturas por cuenta de facturacion
						List<InvoiceInfo> listInvoice = jsonResponse.getInvoiceInfo();

						for (int c = 0; c < listInvoice.size(); c++) {

							if (listBP.get(i).getReprocess().equals("1") && listInvoice.get(c).getPrimaryIdentity()
									.equals(listCI.get(a).getSubscriberId())) {

								insertBPDetail(manager, listBP, i, listCI, a, request, invoiceInfo, gson, listInvoice,
										c);

							} else {

								insertBPDetail(manager, listBP, i, listCI, a, request, invoiceInfo, gson, listInvoice,
										c);
							}

						}
					}
				}
			}
		}
		return listCI.size();
	}

	/**
	 * Insert BP detail.
	 *
	 * @param manager the manager
	 * @param listBP the list BP
	 * @param i the i
	 * @param listCI the list CI
	 * @param a the a
	 * @param request the request
	 * @param invoiceInfo the invoice info
	 * @param gson the gson
	 * @param listInvoice the list invoice
	 * @param c the c
	 * @throws PersistenceException the persistence exception
	 */
	private void insertBPDetail(DAManager manager, final List<DABankProcessDTO> listBP, int i,
			List<DACardInfoDTO> listCI, int a, TaskRequestType request, String invoiceInfo, Gson gson,
			List<InvoiceInfo> listInvoice, int c) throws PersistenceException {
		String uuid = UUID.randomUUID().toString();
		long amount = Long.valueOf(listInvoice.get(c).getOpenAmount());
		long amountTax = Long.valueOf(listInvoice.get(c).getTAXAmount());
		int flagTasaCamb = 0;

		if (listInvoice.get(c).getCurrencyID().equals(configParams.get(DAListenerConstants.CODIGO_TASA_DOLAR))) {

			double tasaCambio = 0;
			String additionalProp = ObtainModelResponse(invoiceInfo, "$.InvoiceInfo[" + c + "].AdditionalProperty",
					DAListenerConstants.ATTR_ADD_PROP, manager,
					request.getParameters().getParameter().get(0).getValue(), listCI.get(a).getAcctcode(),
					listBP.get(i).getId());

			if (additionalProp != null) {
				ListAdditionalProperty additionalProperty = gson.fromJson(additionalProp, ListAdditionalProperty.class);
				List<AdditionalProperty> listAddProp = additionalProperty.getAdditionalProperty();

				for (int j = 0; j < listAddProp.size(); j++) {
					if (listAddProp.get(j).getCode().equals(DAListenerConstants.NOMB_TASA_DOLAR)) {
						tasaCambio = Double.valueOf(listAddProp.get(j).getValue());
					}
				}
				amount = (long) (amount * tasaCambio);
			} else {

				manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
						"NOT_INSERTED No se almaceno el siguiente registro, ya que no contaba con la tasa de cambio; AcctCode: "
								+ listCI.get(a).getAcctcode() + ", InvoiceId:  " + listInvoice.get(c).getInvoiceID(),
						listBP.get(i).getId(), "");
				flagTasaCamb = 1;
			}

		}

		if (flagTasaCamb == 0) {

			double amountFinal = Double.valueOf(amount) / 1000000;
			BigDecimal bd = new BigDecimal(amountFinal).setScale(2, RoundingMode.HALF_UP);
			amountFinal = bd.doubleValue();

			String invoiceID = listInvoice.get(c).getInvoiceID();
			String acctCode = listCI.get(a).getAcctcode();

			String monthFinal = (String.valueOf(listCI.get(a).getMonth()).length() < 2)
					? ("0" + String.valueOf(listCI.get(a).getMonth()))
					: String.valueOf(listCI.get(a).getMonth());
					
			String subscriber;
			if (listInvoice.get(c).getPrimaryIdentity() != null
					&& !listInvoice.get(c).getPrimaryIdentity().equals("")) {
				subscriber = listInvoice.get(c).getPrimaryIdentity();
			} else {
				subscriber = listCI.get(a).getSubscriberId();
			}

			manager.insertBankProcessDetailPay(uuid, listBP.get(i).getId(), DAListenerConstants.STATUS_BPDP, acctCode,
					subscriber, listCI.get(a).getNoCard(), String.valueOf(amountFinal),
					configParams.get(DAListenerConstants.COMMENTS_PAY) + acctCode, invoiceID,
					listCI.get(a).getGroupPayment(), String.valueOf(amountTax), listCI.get(a).getYear() + monthFinal);
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
			LOGGER.error("GenerateFileBankThread " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the request CBS invoice.
	 *
	 * @param accountCode the account code
	 * @return the request CBS invoice
	 */
	private TaskRequestType getRequestCBSInvoice(final String accountCode) {

		TaskRequestType taskRequestType = new TaskRequestType();
		ParameterArray parameterArray = new ParameterArray();
		ParameterType parameterType = new ParameterType();
		AcctAccessCode acctAccessCode = new AcctAccessCode();
		QueryObj queryObj = new QueryObj();
		InvoiceHeaderFilter invoiceHeaderFilter = new InvoiceHeaderFilter();
		JsonRequestCBSInvoice request = new JsonRequestCBSInvoice();

		acctAccessCode.setAccountCode(accountCode);
		acctAccessCode.setPayType(DAListenerConstants.PAY_TYPE);
		queryObj.setAcctAccessCode(acctAccessCode);

		invoiceHeaderFilter.setStatus(DAListenerConstants.STATUS);

		request.setQueryObj(queryObj);
		request.setInvoiceHeaderFilter(invoiceHeaderFilter);
		request.setRetrieveDetail(DAListenerConstants.RETRIEVEDETAIL);
		request.setTotalRowNum(DAListenerConstants.TOTAL_ROWNUM);
		request.setBeginRowNum(DAListenerConstants.ROWNUM);
		request.setFetchRowNum(DAListenerConstants.ROWNUM);

		JSONObject jsonObject = new JSONObject(request);

		parameterType.setName(DAListenerConstants.JSON);
		parameterType.setValue(jsonObject.toString());

		parameterArray.getParameter().add(parameterType);
		taskRequestType.setParameters(parameterArray);

		return taskRequestType;
	}
	
	private TaskRequestType getRequestCBSQueryCustomer(final String accountCode) {

		TaskRequestType taskRequestType = new TaskRequestType();
		ParameterArray parameterArray = new ParameterArray();
		ParameterType parameterType = new ParameterType();
	
		parameterType.setName(DAListenerConstants.JSON);
		parameterType.setValue("{\r\n" + 
				"	\"queryObj\": {\r\n" + 
				"		\"acctAccessCode\": {\r\n" + 
				"			\"accountCode\": \""+accountCode+"\",\r\n" + 
				"			\"payType\":\"2\"\r\n" + 
				"		}\r\n" + 
				"	}\r\n" + 
				"}");

		parameterArray.getParameter().add(parameterType);
		taskRequestType.setParameters(parameterArray);

		return taskRequestType;
	}

	/**
	 * Construct ban process.
	 *
	 * @param id the id
	 * @param sequence the sequence
	 * @param amount the amount
	 * @param countClient the count client
	 * @param status the status
	 * @param dateInit the date init
	 * @param dateEnd the date end
	 * @param base64 the base 64
	 * @param fileNameDA the file name DA
	 * @param fileNameBank the file name bank
	 * @param uudInluExclu the uud inlu exclu
	 * @return the DA bank process DTO
	 */
	private DABankProcessDTO constructBanProcess(final String id, final String sequence, final String amount,
			final long countClient, final long status, final Date dateInit, final Date dateEnd, final String base64,
			final String fileNameDA, final String fileNameBank, final String uudInluExclu) {
		DABankProcessDTO dto = new DABankProcessDTO();
		dto.setId(id);
		dto.setSequence(sequence);
		dto.setAmount(amount);
		dto.setCountClient(countClient);
		dto.setStatus(status);
		dto.setDateInit(dateInit);
		dto.setDateEnd(dateEnd);
		dto.setBase64(base64);
		dto.setFileNameDA(fileNameDA);
		dto.setFileNameBank(fileNameBank);
		dto.setUuidInlcluExclu(uudInluExclu);
		return dto;
	}

	/**
	 * Gets the cycles.
	 *
	 * @param cycleCBS the cycle CBS
	 * @param nameCycle the name cycle
	 * @param manager the manager
	 * @return the cycles
	 * @throws PersistenceException the persistence exception
	 */
	private String getCycles(final String cycleCBS, final String nameCycle, DAManager manager)
			throws PersistenceException {

		List<DAConfigCyclesDTO> listCycles = manager.selectDAConfCycles(cycleCBS);

		if (nameCycle.equals(DAListenerConstants.AS400)) {
			return listCycles.get(0).getCycleAs();
		}
		if (nameCycle.equals(DAListenerConstants.BAC)) {
			return listCycles.get(0).getCycleBac();
		}
		return null;
	}

	/**
	 * Delete word.
	 *
	 * @param response the response
	 * @param word the word
	 * @param word2 the word 2
	 * @param word3 the word 3
	 * @param word4 the word 4
	 * @return the string
	 */
	private String deleteWord(String response, String word, String word2, String word3, String word4) {

		if (response.contains(word)) {
			response = response.replaceAll(word, "");
		}
		if (response.contains(word2)) {
			response = response.replaceAll(word2, "");
		}
		if (response.contains(word3)) {
			response = response.replaceAll(word3, "");
		}
		if (response.contains(word4)) {
			response = response.replaceAll(word4, "");
		}
		return response;
	}

	/**
	 * Space zero.
	 *
	 * @param value the value
	 * @param cantDigit the cant digit
	 * @return the string
	 */
	private String spaceZero(String value, final int cantDigit) {

		if (value != null) {
			if (value.length() > cantDigit) {
				value = value.substring(0, cantDigit - 1);
			}
			if (value.length() < cantDigit) {
				for (int i = value.length(); i < cantDigit; i++) {
					value = "0" + value;
				}
			}
		} else {
			value = "";
			for (int i = 0; i < cantDigit; i++) {
				value = "0" + value;
			}
		}
		return value;
	}

	/**
	 * Space empty.
	 *
	 * @param value the value
	 * @param cantDigit the cant digit
	 * @return the string
	 */
	private String spaceEmpty(String value, final int cantDigit) {

		if (value.length() > cantDigit) {
			value = value.substring(0, cantDigit - 1);
		}
		if (value.length() < cantDigit) {
			for (int i = value.length(); i < cantDigit; i++) {
				value = value + " ";
			}
		}
		return value;
	}

	/**
	 * Method get.
	 *
	 * @param noCard the no card
	 * @param type the type
	 * @param manager the manager
	 * @param acctCode the acct code
	 * @param bankProcessId the bank process id
	 * @return the string
	 * @throws PersistenceException the persistence exception
	 */
	private String methodGet(String noCard, String type, DAManager manager, String acctCode, String bankProcessId)
			throws PersistenceException {
		StringBuffer content = null;
		try {
			String noCardB64 = Base64.getEncoder().encodeToString(noCard.getBytes(StandardCharsets.UTF_8));

			String urlFinal = configParams.get(DAListenerConstants.ENDPOINT_CRED_TAR) + type + "/" + noCardB64;
			LOGGER.info(urlFinal);

			URL url = new URL(urlFinal);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			LOGGER.info(content.toString());
		} catch (IOException e) {
			LOGGER.error("ERROR: en desencriptacion de No tarjeta: " + e.getMessage());
			e.printStackTrace();
			String uuidEr = UUID.randomUUID().toString();
			manager.insertLogs(uuidEr, DAListenerConstants.TYPE_ERROR2,
					"ERROR: en desencriptacion de No tarjeta " + acctCode + " ==> " + e.getMessage(), bankProcessId,
					"");
			return "0000000000000000";
		}
		return content.toString();
	}

	/**
	 * Obtain model response.
	 *
	 * @param json the json
	 * @param jsonPath the json path
	 * @param attribute the attribute
	 * @param manager the manager
	 * @param request the request
	 * @param acctCode the acct code
	 * @param bankProcessId the bank process id
	 * @return the string
	 * @throws PersistenceException the persistence exception
	 */
	private String ObtainModelResponse(String json, String jsonPath, String attribute, DAManager manager,
			String request, String acctCode, String bankProcessId) throws PersistenceException {
		String jsonTemporary = obtainPathValueFromJson(json, jsonPath, manager, request, acctCode, bankProcessId);

		if (jsonTemporary != null) {
			String firstChar = "" + jsonTemporary.charAt(0);
			String lastChar = "" + jsonTemporary.charAt(jsonTemporary.length() - 1);

			if (firstChar.equals("{") && lastChar.equals("}")) {
				jsonTemporary = "{\"" + attribute + "\":[" + jsonTemporary + "]}";
			} else {
				jsonTemporary = "{\"" + attribute + "\":" + jsonTemporary + "}";
			}
			return jsonTemporary;
		}
		return null;
	}

	/**
	 * Obtain response invoice.
	 *
	 * @param json the json
	 * @param attribute the attribute
	 * @param manager the manager
	 * @param request the request
	 * @param acctCode the acct code
	 * @param bankProcessId the bank process id
	 * @return the string
	 * @throws PersistenceException the persistence exception
	 */
	private String ObtainResponseInvoice(String json, String attribute, DAManager manager, String request,
			String acctCode, String bankProcessId) throws PersistenceException {
		String jsonTemporary = obtainPathValueFromInvoice(json, manager, request, acctCode, bankProcessId);

		if (jsonTemporary != null) {
			String firstChar = "" + jsonTemporary.charAt(0);
			String lastChar = "" + jsonTemporary.charAt(jsonTemporary.length() - 1);

			if (firstChar.equals("{") && lastChar.equals("}")) {
				jsonTemporary = "{\"" + attribute + "\":[" + jsonTemporary + "]}";
			} else {
				jsonTemporary = "{\"" + attribute + "\":" + jsonTemporary + "}";
			}
			return jsonTemporary;
		}
		return null;
	}

	/**
	 * Obtain path value from json.
	 *
	 * @param json the json
	 * @param pathJson the path json
	 * @param manager the manager
	 * @param request the request
	 * @param acctCode the acct code
	 * @param bankProcessId the bank process id
	 * @return the string
	 * @throws PersistenceException the persistence exception
	 */
	public static String obtainPathValueFromJson(String json, String pathJson, DAManager manager, String request,
			String acctCode, String bankProcessId) throws PersistenceException {
		String value = null;
		try {
			Object valueList = JsonPath.parse(json).read(pathJson);
			value = valueList.toString();

		} catch (Exception ex) {
			LOGGER.error(
					"NOT_INSERTED ERROR: No se pudo transformar el json de QueryInvoiceEnhanced " + ex.getMessage());
			ex.printStackTrace();
			String uuid = UUID.randomUUID().toString();
			manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
					"NOT_INSERTED ERROR: No se pudo transformar el json de QueryInvoiceEnhanced para la cuenta: "
							+ acctCode + " ==> " + ex.getMessage(),
					bankProcessId, request);
		}
		return value;
	}

	/**
	 * Obtain path value from invoice.
	 *
	 * @param json the json
	 * @param manager the manager
	 * @param request the request
	 * @param acctCode the acct code
	 * @param bankProcessId the bank process id
	 * @return the string
	 * @throws PersistenceException the persistence exception
	 */
	public static String obtainPathValueFromInvoice(String json, DAManager manager, String request, String acctCode,
			String bankProcessId) throws PersistenceException {
		String value = null;
		try {

			JSONObject jsonObject = new JSONObject(json);

			JSONObject queryIEResutl = jsonObject.getJSONObject("QueryInvoiceEnhancedResultMsg")
					.getJSONObject("QueryInvoiceEnhancedResult");
			String invoiceInfo = queryIEResutl.get("InvoiceInfo").toString();
			return invoiceInfo;

		} catch (Exception ex) {
			LOGGER.error(
					"NOT_INSERTED ERROR: No se pudo transformar el json de QueryInvoiceEnhanced " + ex.getMessage());
			ex.printStackTrace();
			String uuid = UUID.randomUUID().toString();
			manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
					"NOT_INSERTED ERROR: No se pudo transformar el json de QueryInvoiceEnhanced para la cuenta: "
							+ acctCode + " ==> " + ex.getMessage(),
					bankProcessId, request);
		}
		return value;
	}

	/**
	 * Adds the zero decimal.
	 *
	 * @param number the number
	 * @return the string
	 */
	private String addZeroDecimal(String number) {

		String[] parts = number.split("\\.");
		if (parts.length == 2) {
			if (parts[1].length() < 2) {
				number = number + "0";
			}
		} else {
			number = number + ".00";
		}

		return number;
	}

}
