/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;

/**
 * 
 * Initial date: 20 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TranslationBundle {
	
	private final boolean textArea;
	private final String i18nKey;
	private final String labelI18nKey;
	private final StaticTextElement viewEl;
	private final FormLink translationLink;
	
	public TranslationBundle(String i18nKey, String labelI18nKey, StaticTextElement viewEl, FormLink translationLink, boolean textArea) {
		this.textArea = textArea;
		this.i18nKey = i18nKey;
		this.viewEl = viewEl;
		this.labelI18nKey = labelI18nKey;
		this.translationLink = translationLink;
	}

	public StaticTextElement getViewEl() {
		return viewEl;
	}
	
	public boolean isTextArea() {
		return textArea;
	}

	public String getI18nKey() {
		return i18nKey;
	}
	
	public String getLabelI18nKey() {
		return labelI18nKey;
	}
	
	public void setVisible(boolean visible) {
		viewEl.setVisible(visible);
		translationLink.setVisible(visible);
	}
}