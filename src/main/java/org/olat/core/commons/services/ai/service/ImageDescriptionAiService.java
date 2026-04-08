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
package org.olat.core.commons.services.ai.service;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.model.ImageDescriptionData;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.service.SystemMessage;

/**
 * LangChain4j AiServices interface for image description generation.
 * Instantiated per provider via AiServices.builder(...).chatModel(model).build().
 *
 * Initial date: 27.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface ImageDescriptionAiService {

	@SystemMessage("You are an assistant that analyzes images and generates structured metadata for them.")
	ImageDescriptionData describeImage(List<Content> contents);

	static List<Content> buildContents(Locale locale, String imageBase64, String mimeType) {
		String langName = (locale != null) ? locale.getDisplayLanguage(Locale.ENGLISH) : "English";
		String prompt = """
				Analyze this image and generate structured metadata.
				Respond in %s.
				""".formatted(langName);

		return List.of(
				ImageContent.from(imageBase64, mimeType),
				TextContent.from(prompt)
		);
	}
}
