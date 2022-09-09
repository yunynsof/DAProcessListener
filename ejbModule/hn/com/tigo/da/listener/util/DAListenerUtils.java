package hn.com.tigo.da.listener.util;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tigo.josm.gateway.services.order.additionalparameterdto.v1.AdditionalParameters;
import com.tigo.josm.gateway.services.order.additionalparameterdto.v1.Parameter;
import com.tigo.josm.gateway.services.order.simpleorderrequest.v1.SimpleOrderRequest;

import hn.com.tigo.core.da.manager.DAManager;
import hn.com.tigo.josm.persistence.exception.PersistenceException;
import hn.tigo.resources.emailservice.EmailService;
import hn.tigo.resources.emailservice.EmailService_Service;
import hn.tigo.resources.emailservice.SentDTO;
import hn.tigo.resources.emailservice.ToDto;

/**
 * DAListenerUtils.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:22:04 AM 2022
 */
public class DAListenerUtils {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(DAListenerUtils.class);

	/**
	 * Gets the request order.
	 *
	 * @param accountCode the account code
	 * @param additionalParameters the additional parameters
	 * @param comments the comments
	 * @param channelId the channel id
	 * @param productId the product id
	 * @return the request order
	 */
	public static SimpleOrderRequest getRequestOrder(final String accountCode,
			AdditionalParameters additionalParameters, String comments, int channelId, long productId) {
		SimpleOrderRequest simpleOrderRequest = new SimpleOrderRequest();
		simpleOrderRequest.setChannelId(channelId);
		simpleOrderRequest.setSubscriberId(accountCode);
		simpleOrderRequest.setProductId(productId);
		simpleOrderRequest.setQuantity(DAListenerConstants.QUANTITY);
		simpleOrderRequest.setComment(comments);

		simpleOrderRequest.setAdditionalParameters(additionalParameters);

		return simpleOrderRequest;
	}

	/**
	 * Gets the additional parameters.
	 *
	 * @param parameter the parameter
	 * @return the additional parameters
	 */
	public static AdditionalParameters getAdditionalParameters(List<Parameter> parameter) {
		AdditionalParameters additionalParameters = new AdditionalParameters();
		for (int i = 0; i < parameter.size(); i++) {
			additionalParameters.getParameter().add(i, parameter.get(i));
		}
		return additionalParameters;
	}

	/**
	 * Obtain parameter.
	 *
	 * @param parameter the parameter
	 * @param key the key
	 * @param value the value
	 * @return the list
	 */
	public static List<Parameter> obtainParameter(List<Parameter> parameter, String key, String value) {
		Parameter parameter1 = new Parameter();
		if (value == null || value.equals(""))
			value = "0";

		parameter1.setKey(key);
		parameter1.setValue(value);

		int i = (parameter.size());
		parameter.add(i, parameter1);
		return parameter;
	}

	/**
	 * Send message.
	 *
	 * @param emailFrom the email from
	 * @param body the body
	 * @param emails the emails
	 * @param urlEmail the url email
	 * @param subject the subject
	 * @param manager the manager
	 * @throws PersistenceException the persistence exception
	 */
	public static void SendMessage(String emailFrom, String body, String emails, URL urlEmail, String subject,
			DAManager manager) throws PersistenceException {
		try {
			SentDTO sendTo = new SentDTO();
			String[] parts = emails.split(";");
			for (String ml : parts) {
				ToDto email = new ToDto();
				email.setTo(ml);
				sendTo.getSend().add(email);
			}
			EmailService emailService = new EmailService_Service(urlEmail).getEmailServicePort();
			emailService.sendMessage(emailFrom, sendTo, "", subject, body, null);

		} catch (Exception e) {

			LOGGER.error("ERROR: en consumo de servicio Email: " + e.getLocalizedMessage());
			String uuid = UUID.randomUUID().toString();

			manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR2,
					"ERROR en Consumo Servicio Email: " + e.getLocalizedMessage(), "",
					"URL: " + urlEmail + " From: " + emailFrom + " sendTo: " + emails);
		}
	}

	/**
	 * Gets the cycle.
	 *
	 * @param cycle the cycle
	 * @return the cycle
	 */
	public static String getCycle(long cycle) {

		String lastTwoDigits = String.valueOf(cycle).substring(6);
		return lastTwoDigits;

	}

	/**
	 * Send email init.
	 *
	 * @param body the body
	 * @param typeMsg the type msg
	 * @param urlEmail the url email
	 * @param bankProcessor the bank processor
	 * @param size the size
	 * @param cycle the cycle
	 * @param emails the emails
	 * @param fromEmail the from email
	 * @param fileSubj the file subj
	 * @param manager the manager
	 * @throws PersistenceException the persistence exception
	 */
	public static void sendEmailInit(String body, String typeMsg, URL urlEmail, String bankProcessor, String size,
			String cycle, String emails, String fromEmail, String fileSubj, DAManager manager)
			throws PersistenceException {

		body = body.replace("<MSG>", typeMsg);
		body = body.replace("<BANK>", bankProcessor);
		body = body.replace("<COUNT>", size);
		body = body.replace("<CYCLE>", cycle);

		DAListenerUtils.SendMessage(fromEmail, body, emails, urlEmail, fileSubj, manager);
	}

	/**
	 * Send email error.
	 *
	 * @param body the body
	 * @param typeMsg the type msg
	 * @param urlEmail the url email
	 * @param bankProcessor the bank processor
	 * @param error the error
	 * @param emails the emails
	 * @param fromEmail the from email
	 * @param fileSubj the file subj
	 * @param manager the manager
	 * @throws PersistenceException the persistence exception
	 */
	public static void sendEmailError(String body, String typeMsg, URL urlEmail, String bankProcessor, String error,
			String emails, String fromEmail, String fileSubj, DAManager manager) throws PersistenceException {

		body = body.replace("<MSG>", typeMsg);
		body = body.replace("<BANK>", bankProcessor);
		body = body.replace("<ERROR>", error);

		DAListenerUtils.SendMessage(fromEmail, body, emails, urlEmail, fileSubj, manager);
	}

	/**
	 * Send email end.
	 *
	 * @param body the body
	 * @param typeMsg the type msg
	 * @param urlEmail the url email
	 * @param bankProcessor the bank processor
	 * @param nameFile the name file
	 * @param amountTotal the amount total
	 * @param amountApply the amount apply
	 * @param size the size
	 * @param sizeVal the size val
	 * @param cycle the cycle
	 * @param emails the emails
	 * @param fromEmail the from email
	 * @param fileSubj the file subj
	 * @param manager the manager
	 * @throws PersistenceException the persistence exception
	 */
	public static void sendEmailEnd(String body, String typeMsg, URL urlEmail, String bankProcessor, String nameFile,
			String amountTotal, String amountApply, String size, String sizeVal, String cycle, String emails,
			String fromEmail, String fileSubj, DAManager manager) throws PersistenceException {

		body = body.replace("<MSG>", typeMsg);
		body = body.replace("<BANK>", bankProcessor);
		body = body.replace("<NAME_FILE>", nameFile);
		body = body.replace("<AMOUNT_TOT>", amountTotal);
		body = body.replace("<AMOUNT_APPLY>", amountApply);
		body = body.replace("<COUNT>", size);
		body = body.replace("<COUNT_VAL>", sizeVal);
		body = body.replace("<CYCLE>", cycle);

		DAListenerUtils.SendMessage(fromEmail, body, emails, urlEmail, fileSubj, manager);
	}
}
