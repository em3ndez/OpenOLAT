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
package org.olat.core.commons.services.help.spi;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.help.OpenOlatDocsHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 02.07.2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
@Service("ooTeachLinkHelp")
public class OOTeachLinkSPI implements HelpLinkSPI  {

	private static final String PLUGIN_NAME = "ooTeach";
	
	@Autowired
	private HelpModule helpModule;
	
	@Autowired
	private OpenOlatDocsHelper openOlatDocsHelper;
	
	@Override
	public UserTool getHelpUserTool(WindowControl wControl) {
		return new OOTeachLinkUserTool();
	}
	
	public class OOTeachLinkUserTool implements UserTool {
		
		
		public OOTeachLinkUserTool() {
		}

		@Override
		public Component getMenuComponent(UserRequest ureq, VelocityContainer container, boolean iconOnly) {
			ExternalLink helpLink = new ExternalLink("help.ooTeach");
			helpLink.setIconLeftCSS("o_icon o_icon-fw " + helpModule.getOOTeachIcon());
			helpLink.setName(container.getTranslator().translate("help.ooTeach"));
			helpLink.setTooltip(container.getTranslator().translate("help.ooTeach"));
			helpLink.setTarget("ooTeach");
			helpLink.setUrl(helpModule.getOOTeachLink());
			container.put("help.ooTeach", helpLink);
			return helpLink;
		}

		@Override
		public void dispose() {
			//
		}
	}
	
	@Override
	public String getURL(Locale locale, String page) {
		// Fallback to OpenOlat-docs context help
		return openOlatDocsHelper.getURL(locale, page);
	}

	@Override
	public Component getHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS,
			String page) {
		// Fallback to OpenOlat-docs context help
		return openOlatDocsHelper.createHelpPageLink(ureq, title, tooltip, iconCSS, elementCSS, page);
	}
	
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
}
