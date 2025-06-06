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
package org.olat.course.nodes.gta.ui.peerreview;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.user.UserPropertiesInfoController;
import org.olat.user.UserPropertiesInfoController.LabelValues;

/**
 * 
 * Initial date: 29 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskPortraitController extends BasicController {

	public TaskPortraitController(UserRequest ureq, WindowControl wControl, Identity identity, Task task) {
		super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		
		VelocityContainer mainVC = createVelocityContainer("task_portrait");
		
		String taskName = task == null || task.getTaskName() == null ? "" : task.getTaskName();
		LabelValues additionalLV = LabelValues.builder().add(translate("task"), taskName).build();
		
		UserPropertiesInfoController userInfoCtrl = new UserPropertiesInfoController(ureq, wControl, identity, null, additionalLV);
		listenTo(userInfoCtrl);
		mainVC.put("userInfo", userInfoCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
