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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;

/**
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExternalLink extends AbstractComponent implements FormBaseComponent {
	
	private static final ExternalLinkRenderer RENDERER = new ExternalLinkRenderer();

	private String url;
	private String name;
	private String target;
	private String tooltip;
	private String iconLeftCSS;
	private String iconRightCSS;
	private String cssClass;
	private EscapeMode escapeMode = EscapeMode.html;
	
	private final ExternalLinkItem formItem;
	
	public ExternalLink(String id, String name) {
		this(id, name, null);
	}
	
	public ExternalLink(String id, String name, ExternalLinkItem formItem) {
		super(id, name);
		this.formItem = formItem;
		setDomReplacementWrapperRequired(false);
	}
	
	public ExternalLink(String name) {
		this(name, (ExternalLinkItem)null);
	}
	
	public ExternalLink(String name, ExternalLinkItem formItem) {
		super(name);
		this.formItem = formItem;
		setDomReplacementWrapperRequired(false);
	}

	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(super.getDispatchID());
	}

	@Override
	public ExternalLinkItem getFormItem() {
		return formItem;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIconLeftCSS() {
		return iconLeftCSS;
	}

	public void setIconLeftCSS(String iconLeftCSS) {
		this.iconLeftCSS = iconLeftCSS;
	}

	public String getIconRightCSS() {
		return iconRightCSS;
	}

	public void setIconRightCSS(String iconRightCSS) {
		this.iconRightCSS = iconRightCSS;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public EscapeMode getEscapeMode() {
		return escapeMode;
	}

	public void setEscapeMode(EscapeMode escapeMode) {
		this.escapeMode = escapeMode;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}