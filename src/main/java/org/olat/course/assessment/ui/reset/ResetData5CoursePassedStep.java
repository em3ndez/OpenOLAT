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
package org.olat.course.assessment.ui.reset;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.assessment.ui.reset.ResetWizardContext.ResetDataStep;

/**
 * 
 * Initial date: 2 Jun 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ResetData5CoursePassedStep extends BasicStep {
	
	private final ResetWizardContext wizardContext;

	public ResetData5CoursePassedStep(UserRequest ureq, ResetWizardContext wizardContext) {
		super(ureq);
		this.wizardContext = wizardContext;

		setI18nTitleAndDescr("wizard.course.passed.reset", "wizard.course.passed.reset");
		setNextStep(wizardContext.createNextStep(ureq, ResetDataStep.coursePassed));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		wizardContext.setCurrent(ResetDataStep.coursePassed);
		return nextStep() == Step.NOSTEP
				? new PrevNextFinishConfig(true, false ,true)
				: new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext context, Form form) {
		return new ResetCoursePassedController(ureq, wControl, form, context, wizardContext.getDataContext(),
				wizardContext.getCoachCourseEnv(), wizardContext.getSecCallback());
	}

}
