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
package org.olat.modules.library.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * A controller for a small box in the toolbox to collect a string to make a search.<br>
 * Events fired:
 * <ul>
 *   <li>DONE_EVENT</li>
 * </ul>
 * <P>
 * Initial Date:  5 oct. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchQueryController extends FormBasicController {

	private FormLink searchButton;
	private TextElement searchInput;
	
	public SearchQueryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "searchbox");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchInput = uifactory.addTextElement("search_input", null, 255, "", formLayout);
		searchInput.setPlaceholderKey("search", null);
		searchInput.setAriaLabel("search");

		searchButton = uifactory.addFormLink("rightAddOn", "", "", formLayout, Link.NONTRANSLATED);
		searchButton.setIconLeftCSS("o_icon o_icon-fw o_icon_search o_icon-lg");
		String searchLabel = getTranslator().translate("search");
		searchButton.setLinkTitle(searchLabel);
		searchButton.setCustomEnabledLinkCSS("o_search");
		searchButton.setEnabled(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == searchButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public String getSearchQuery() {
		return searchInput.getValue();
	}
}
