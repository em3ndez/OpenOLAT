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
package org.olat.user;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * 
 * Initial date: 7 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserPortraitFactory {
	
	public static UserPortraitComponent createUserPortrait(String name, VelocityContainer vc, Locale locale) {
		UserPortraitComponent usersPortraitsComponent = new UserPortraitComponent(name, locale);
		if (vc != null) {
			vc.put(usersPortraitsComponent.getComponentName(), usersPortraitsComponent);
		}
		return usersPortraitsComponent;
	}
	
	public static UsersPortraitsComponent createUsersPortraits(UserRequest ureq, String name, VelocityContainer vc) {
		UsersPortraitsComponent usersPortraitsComponent = new UsersPortraitsComponent(ureq, name);
		if (vc != null) {
			vc.put(usersPortraitsComponent.getComponentName(), usersPortraitsComponent);
		}
		return usersPortraitsComponent;
	}
	
}
