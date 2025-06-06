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
package org.olat.modules.forms.ui.model;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;

/**
 * Wrapper around the identity of the executor. This class helps to fill out
 * surveys with unauthenticated users.
 * 
 * Initial date: 21 Nov 2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutionIdentity {
	
	private final Long identityKey;
	private final User user;
	
	private ExecutionIdentity(Long identityKey, User user) {
		this.identityKey = identityKey;
		this.user = user;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public User getUser() {
		return user;
	}
	
	public static ExecutionIdentity ofIdentity(Identity identity) {
		return new ExecutionIdentity(identity.getKey(), identity.getUser());
	}

	public static ExecutionIdentity ofEmail(String email) {
		TransientIdentity user = new TransientIdentity();
		user.setProperty(UserConstants.EMAIL, email);
		return new ExecutionIdentity(null, user);
	}
	
	public static ExecutionIdentity ofNone() {
		return new ExecutionIdentity(null, new TransientIdentity());
	}

}
