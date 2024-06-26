/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.ta;

import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseModule;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryManager;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Notification handler for course node task. Subscribers get informed about
 * new uploaded file in the dropbox.
 * <P>
 * Initial Date: 23.11.2005 <br />
 * 
 * @author christian guretzki
 */
@Service
public class DropboxFileUploadNotificationHandler extends AbstractTaskNotificationHandler implements NotificationsHandler {
	private static final Logger log = Tracing.createLoggerFor(DropboxFileUploadNotificationHandler.class);
	
	private static final String CSS_CLASS_DROPBOX_ICON = "o_dropbox_icon";

	public DropboxFileUploadNotificationHandler() {
		//empty block
	}
	
	protected static SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, CourseNode node) {
	  return CourseModule.createSubscriptionContext(courseEnv, node, node.getIdent());
	}

	@Override
	protected String getCssClassIcon() {
		return CSS_CLASS_DROPBOX_ICON;
	}

	@Override
	protected String getNotificationHeaderKey() {
		return "dropbox.notifications.header";
	}

	@Override
	protected String getNotificationEntryKey() {
		return "dropbox.notifications.entry";
	}
	
	protected Logger getLogger() {
		return log;
	}

	@Override
	public String getType() {
		return "DropboxController";
	}

	@Override
	public String getDisplayName(Publisher publisher) {
		return RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(publisher.getResId());
	}

	@Override
	public String getIconCss() {
		return CSSHelper.getIconCssClassFor(getCssClassIcon());
	}

	@Override
	public String getAdditionalDescriptionI18nKey(Locale locale) {
		return null;
	}

}
