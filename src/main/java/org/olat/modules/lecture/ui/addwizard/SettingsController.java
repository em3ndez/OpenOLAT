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
package org.olat.modules.lecture.ui.addwizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.lecture.ui.EditLectureBlockController;

/**
 * 
 * Initial date: 7 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SettingsController extends StepFormBasicController {

	private final EditLectureBlockController editLectureCtrl;
	
	public SettingsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, AddLectureContext addLecture) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

		editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), rootForm, addLecture);
		listenTo(editLectureCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("edit", editLectureCtrl.getInitialFormItem());
	}
	
	@Override
	public void back() {
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(this);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return editLectureCtrl.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		editLectureCtrl.formOK(ureq);
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
