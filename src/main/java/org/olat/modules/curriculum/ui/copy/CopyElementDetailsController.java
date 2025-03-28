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
package org.olat.modules.curriculum.ui.copy;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumComposerController;

/**
 * 
 * Initial date: 18 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyElementDetailsController extends FormBasicController {

	private final boolean hasTemplate;
	
	private final CopyElementDetailsResourcesController detailsResourcesCtrl;
	private final CopyElementDetailsTemplatesController detailsTemplatesCtrl;
	private final CopyElementDetailsLectureBlocksController lecturesBlocksCtrl;
	
	public CopyElementDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CurriculumElement curriculumElement, CopyElementContext context) {
		super(ureq, wControl, LAYOUT_CUSTOM, "element_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale()));

		detailsTemplatesCtrl = new CopyElementDetailsTemplatesController(ureq, wControl, rootForm,
				curriculumElement, context);
		listenTo(detailsTemplatesCtrl);
		hasTemplate = detailsTemplatesCtrl.getNumOfTemplates() > 0;
		
		detailsResourcesCtrl = new CopyElementDetailsResourcesController(ureq, wControl, rootForm,
				curriculumElement, context, hasTemplate);
		listenTo(detailsResourcesCtrl);
		
		lecturesBlocksCtrl = new CopyElementDetailsLectureBlocksController(ureq, wControl, rootForm,
				curriculumElement, context);
		listenTo(lecturesBlocksCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("resources", detailsResourcesCtrl.getInitialFormItem());
		if(hasTemplate) {
			formLayout.add("templates", detailsTemplatesCtrl.getInitialFormItem());
		}
		formLayout.add("lecturesBlocks", lecturesBlocksCtrl.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
