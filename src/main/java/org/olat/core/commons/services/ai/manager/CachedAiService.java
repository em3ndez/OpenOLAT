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

import org.olat.core.commons.services.ai.AiSPI;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Immutable holder for a cached LangChain4j AiServices instance, keyed by
 * (spiId, modelName). Used as a volatile field to allow safe, lock-free
 * cache invalidation when the AI configuration changes.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, https://www.frentix.com
 *
 */
record CachedAiService<T>(String spiId, String modelName, T service) {

	boolean matches(String spiId, String modelName) {
		return this.spiId.equals(spiId) && this.modelName.equals(modelName);
	}

	static <T> CachedAiService<T> getOrCreate(Class<T> serviceType, CachedAiService<T> cached,
			AiSPI spi, String spiId, String modelName, int maxTokens) {
		if (cached != null && cached.matches(spiId, modelName)) {
			return cached;
		}
		ChatModel chatModel = spi.buildChatModel(modelName, maxTokens);
		T service = AiServices.builder(serviceType).chatModel(chatModel).build();
		return new CachedAiService<>(spiId, modelName, service);
	}
}
