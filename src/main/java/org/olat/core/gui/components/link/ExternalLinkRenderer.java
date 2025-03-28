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
package org.olat.core.gui.components.link;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExternalLinkRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ExternalLink link = (ExternalLink)source;
		
		
		String linkText = link.getName();
		String title = link.getTooltip();
		// don't use title if the same as link text to remove a11y redundancy
		if (StringHelper.containsNonWhitespace(linkText) && linkText.equals(title)) {
			title = "";
		}
		boolean isIconLink = (!StringHelper.containsNonWhitespace(linkText) && StringHelper.containsNonWhitespace(link.getIconLeftCSS()));
		
		//class
		sb.append("<a class=\"");
		if (!link.isEnabled()) {
			sb.append("o_disabled");
		}
		if(StringHelper.containsNonWhitespace(link.getElementCssClass())) {
			sb.append(" ").append(link.getElementCssClass());
		}
		if (StringHelper.containsNonWhitespace(link.getCssClass())) {
			sb.append(" ").append(link.getCssClass());
		}
		sb.append("\" ");
		if(link.isEnabled())  {
			sb.append(" href=\"").append(link.getUrl()).append("\"");
		}
		if(StringHelper.containsNonWhitespace(link.getTarget())) {
			sb.append(" target=\"").append(link.getTarget()).append("\"");
		}
		if(!isIconLink && StringHelper.containsNonWhitespace(title)) {
			sb.append(" title=\"").appendHtmlAttributeEscaped(title).append("\"");
		}
		sb.append(" rel=\"noopener noreferrer\"");
		sb.append(" id='").append(link.getComponentName()).append("'>");
		
		if(StringHelper.containsNonWhitespace(link.getIconLeftCSS())) {
			sb.append("<i class=\"").append(link.getIconLeftCSS()).append("\" aria-hidden='true'");
			if(isIconLink && StringHelper.containsNonWhitespace(title)) {
				sb.append(" title=\"").appendHtmlAttributeEscaped(title).append("\"");
			}
			sb.append("> </i> ");
		}
		if(StringHelper.containsNonWhitespace(linkText)) {
			sb.append("<span>").append(linkText, link.getEscapeMode()).append("</span>");
		}
		if(isIconLink && StringHelper.containsNonWhitespace(title)) {
			sb.append("<span class='sr-only'>").appendHtmlEscaped(link.getTooltip()).append("</span>");
		}
		if(StringHelper.containsNonWhitespace(link.getIconRightCSS())) {
			sb.append(" <i class=\"").append(link.getIconRightCSS()).append("\" aria-hidden='true'");
			if(isIconLink && StringHelper.containsNonWhitespace(title)) {
				sb.append(" title=\"").appendHtmlAttributeEscaped(title).append("\"");
			}
			sb.append("> </i>");
		}
		sb.append("</a>");
	}
}
