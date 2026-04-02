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
import org.olat.core.commons.services.ai.AiImageDescriptionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.service.ImageDescriptionAiService;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.message.UserMessage;

/**
 * Spring service implementation for image description generation via AI.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, https://www.frentix.com
 *
 */
@Service
public class AiImageDescriptionServiceImpl implements AiImageDescriptionService {
	private static final Logger log = Tracing.createLoggerFor(AiImageDescriptionServiceImpl.class);
	private static final int MAX_TOKENS = 2000;

	@Autowired
	private AiModule aiModule;

	private volatile CachedAiService<ImageDescriptionAiService> cachedAiService;

	@Override
	public boolean isEnabled() {
		return aiModule.isImageDescriptionGeneratorEnabled();
	}

	@Override
	public AiImageDescriptionResponse generateImageDescription(String imageBase64, String mimeType, Locale locale) {
		return generateImageDescription(imageBase64, mimeType, locale, aiModule.getImgDescSpiId(), aiModule.getImgDescModel());
	}

	@Override
	public AiImageDescriptionResponse generateImageDescription(String imageBase64, String mimeType, Locale locale, String spiId, String modelName) {
		AiImageDescriptionResponse response = new AiImageDescriptionResponse();
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			response.setError("AI provider is not configured or not available.");
			return response;
		}
		try {
			cachedAiService = CachedAiService.getOrCreate(ImageDescriptionAiService.class, cachedAiService, spi, spiId, modelName, MAX_TOKENS);
			UserMessage userMessage = ImageDescriptionAiService.buildUserMessage(locale, imageBase64, mimeType);
			response.setDescription(cachedAiService.service().describeImage(userMessage));

		} catch (Exception e) {
			log.warn("Error while creating image description via AI service [{}]", spiId, e);
			response.setError(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
		}
		return response;
	}

}
