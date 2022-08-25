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

public class DAListenerUtils {

	private static final Logger LOGGER = LogManager.getLogger(DAListenerUtils.class);

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

	public static AdditionalParameters getAdditionalParameters(List<Parameter> parameter) {
		AdditionalParameters additionalParameters = new AdditionalParameters();
		for (int i = 0; i < parameter.size(); i++) {
			additionalParameters.getParameter().add(i, parameter.get(i));
		}
		return additionalParameters;
	}

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

	public static String getCycle(long cycle) {

		String lastTwoDigits = String.valueOf(cycle).substring(6);
		return lastTwoDigits;

	}

	public static void sendEmailInit(String body, String typeMsg, URL urlEmail, String bankProcessor, String size,
			String cycle, String emails, String fromEmail, String fileSubj, DAManager manager)
			throws PersistenceException {

		body = body.replace("<MSG>", typeMsg);
		body = body.replace("<BANK>", bankProcessor);
		body = body.replace("<COUNT>", size);
		body = body.replace("<CYCLE>", cycle);

		DAListenerUtils.SendMessage(fromEmail, body, emails, urlEmail, fileSubj, manager);
	}

	public static void sendEmailError(String body, String typeMsg, URL urlEmail, String bankProcessor, String error,
			String emails, String fromEmail, String fileSubj, DAManager manager) throws PersistenceException {

		body = body.replace("<MSG>", typeMsg);
		body = body.replace("<BANK>", bankProcessor);
		body = body.replace("<ERROR>", error);

		DAListenerUtils.SendMessage(fromEmail, body, emails, urlEmail, fileSubj, manager);
	}

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
