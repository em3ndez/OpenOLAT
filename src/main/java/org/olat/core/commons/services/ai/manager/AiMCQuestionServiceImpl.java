/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.manager;

import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiMCQuestionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.service.MCQuestionAiService;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Spring service implementation for multiple choice question generation via AI.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, https://www.frentix.com
 *
 */
@Service
public class AiMCQuestionServiceImpl implements AiMCQuestionService {
	
	private static final Logger log = Tracing.createLoggerFor(AiMCQuestionServiceImpl.class);
	private static final int MAX_TOKENS = 4000;

	@Autowired
	private AiModule aiModule;
	@Autowired
	private TextService textService;

	private volatile CachedAiService<MCQuestionAiService> cachedAiService;

	@Override
	public boolean isEnabled() {
		return aiModule.isMCQuestionGeneratorEnabled();
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number) {
		return generateMCQuestionsResponse(input, number, aiModule.getMCGeneratorSpiId(), aiModule.getMCGeneratorModel());
	}

	@Override
	public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number, String spiId, String modelName) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			response.setError("AI provider is not configured or not available.");
			return response;
		}
		try {
			Locale locale = textService.detectLocale(input);
			if (locale == null) {
				response.setError("Could not detect language of the input text.");
				return response;
			}

			cachedAiService = CachedAiService.getOrCreate(MCQuestionAiService.class, cachedAiService, spi, spiId, modelName, MAX_TOKENS);
			String language = locale.getDisplayLanguage(Locale.ENGLISH);
			cachedAiService.service().generateQuestions(number, 2, 3, language, input)
					.forEach(response::addQuestion);

		} catch (Exception e) {
			log.warn("Error while creating MC questions via AI service [{}]", spiId, e);
			response.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
		}
		return response;
	}

}
