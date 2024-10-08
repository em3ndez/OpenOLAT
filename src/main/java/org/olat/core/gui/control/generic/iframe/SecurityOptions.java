/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.generic.iframe;

/**
 * 
 * Initial date: 13 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityOptions {

	private String contentSecurityPolicy;
	private boolean strictSanitize = false;
	
	public static SecurityOptions sanitize() {
		SecurityOptions securityOptions = new SecurityOptions();
		securityOptions.setStrictSanitize(true);
		return securityOptions;
	}
	
	public String getContentSecurityPolicy() {
		return contentSecurityPolicy;
	}
	
	public void setContentSecurityPolicy(String contentSecurityPolicy) {
		this.contentSecurityPolicy = contentSecurityPolicy;
	}

	public boolean isStrictSanitize() {
		return strictSanitize;
	}

	public void setStrictSanitize(boolean strictSanitize) {
		this.strictSanitize = strictSanitize;
	}
}
