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
package org.olat.login.oauth;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.olat.core.configuration.ConfigOnOff;
import org.olat.login.oauth.model.OAuthUser;

import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OAuthSPI extends ConfigOnOff {
	
	public OAuthService getScribeProvider();
	
	public String getName();
	
	public boolean isRootEnabled();
	
	/**
	 * @return true: iditable in the UI; false: not editable in the UI, only via
	 *         olat.local.properties
	 */
	public boolean isEditable();
	
	/**
	 * Limit of 8 characters
	 * @return
	 */
	public String getProviderName();
	
	public String getIconCSS();
	
	public boolean isImplicitWorkflow();
	
	public String getIssuerIdentifier();
	
	public OAuthUser getUser(OAuthService service, Token accessToken)
			throws IOException, InterruptedException, ExecutionException;


}
