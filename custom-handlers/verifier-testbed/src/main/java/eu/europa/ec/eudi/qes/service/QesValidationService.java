package eu.europa.ec.eudi.qes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gitb.core.AnyContent;
import com.gitb.core.ValueEmbeddingEnumeration;
import com.gitb.tr.TAR;
import com.gitb.tr.TestResultType;
import com.gitb.vs.*;
import com.gitb.vs.Void;
import eu.europa.ec.eudi.gitb.Utils;
import eu.europa.ec.eudi.qes.dto.QesDocumentRetrievalLogsTO;
import eu.europa.ec.eudi.verifier.utils.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QesValidationService implements ValidationService {
	@Autowired private Utils utils;
	@Autowired private Json json;

	private final Logger log = LoggerFactory.getLogger(QesValidationService.class);

	@Override
	public GetModuleDefinitionResponse getModuleDefinition(Void parameters) {
		return new GetModuleDefinitionResponse();
	}

	@Override
	public ValidationResponse validate(ValidateRequest validateRequest) {
		String providedText = utils.getRequiredString(validateRequest.getInput(), "text");

		String expectedText = null;
		try {
			expectedText = utils.getRequiredString(validateRequest.getInput(), "expected");
			log.info("Retrieved 'expected' text.");
		} catch (Exception e) {
			log.warn("None 'expected' text was received. Exception Message: {}", e.getMessage());
		}

		QesDocumentRetrievalLogsTO providedLogs;
		try {
			providedLogs = json.getReader().readValue(providedText, QesDocumentRetrievalLogsTO.class);
			log.info("Loaded relying party's logs into QesDocumentRetrievalLogsTO Object.");
		} catch (JsonProcessingException e) {
			log.error(
				  "Failed to load relying party's logs into  QesDocumentRetrievalLogsTO Object. Exception Message: {}",
				  e.getMessage());
			throw new RuntimeException(e);
		}

		TAR report;
		if(providedLogs.isSuccess()){
			report = utils.createReport(TestResultType.SUCCESS);
		}
		else {
			report = utils.createReport(TestResultType.FAILURE);
		}

		try {
			log.info("Created JSON Array from list of relying party's logs.");
			AnyContent content = new AnyContent();
			content.setName("Relying Party's Logs");
			content.setType("application/json");
			content.setEncoding("UTF-8");
			content.getItem().add(utils.createAnyContentSimple("JSON Data",
				  json.getWriter().writeValueAsString(providedLogs.getLogs()), ValueEmbeddingEnumeration.STRING));
			report.getContext().getItem().add(content);
			log.info("Added relying party's logs to Report.");
		} catch (JsonProcessingException e) {
			log.error("Failed to add relying party's log to Report. Exception Message: {}", e.getMessage());
			throw new RuntimeException(e);
		}

		ValidationResponse result = new ValidationResponse();
		result.setReport(report);
		return result;
	}
}
