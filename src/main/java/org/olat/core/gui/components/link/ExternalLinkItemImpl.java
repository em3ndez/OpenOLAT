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
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 1 déc. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExternalLinkItemImpl extends FormItemImpl implements ExternalLinkItem {
	
	private final ExternalLink externalLink;
	
	public ExternalLinkItemImpl(String name) {
		super(name);
		externalLink = new ExternalLink(name, this);
	}
	
	public ExternalLinkItemImpl(String id, String name) {
		super(id, name, false);
		externalLink = new ExternalLink(id, name, this);
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		super.setElementCssClass(elementCssClass);
		externalLink.setElementCssClass(elementCssClass);
	}

	@Override
	public void setName(String name) {
		externalLink.setName(name);
	}

	@Override
	public String getUrl() {
		return externalLink.getUrl();
	}

	@Override
	public void setUrl(String url) {
		externalLink.setUrl(url);
	}

	@Override
	public String getIconLeftCSS() {
		return externalLink.getIconLeftCSS();
	}

	@Override
	public void setIconLeftCSS(String iconLeftCSS) {
		externalLink.setIconLeftCSS(iconLeftCSS);
	}

	@Override
	public String getIconRightCSS() {
		return externalLink.getIconRightCSS();
	}

	@Override
	public void setIconRightCSS(String iconRightCSS) {
		externalLink.setIconRightCSS(iconRightCSS);
	}

	@Override
	public String getCssClass() {
		return externalLink.getCssClass();
	}

	@Override
	public void setCssClass(String cssClass) {
		externalLink.setCssClass(cssClass);
	}

	@Override
	public String getTarget() {
		return externalLink.getTarget();
	}

	@Override
	public void setTarget(String target) {
		externalLink.setTarget(target);
	}
	
	@Override
	public String getTooltip() {
		return externalLink.getTooltip();
	}

	@Override
	public void setTooltip(String tip) {
		externalLink.setTooltip(tip);
	}
	
	@Override
	public EscapeMode getEscapeMode() {
		return externalLink.getEscapeMode();
	}

	@Override
	public void setEscapeMode(EscapeMode escapeMode) {
		externalLink.setEscapeMode(escapeMode);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}

	@Override
	protected ExternalLink getFormItemComponent() {
		return externalLink;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}
}
