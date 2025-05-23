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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.course.run.RunMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EfficiencyStatementController extends FormBasicController {

	private FormToggle efficiencyStatementEl;

	private DialogBoxController enableEfficiencyDC;
	private DialogBoxController disableEfficiencyDC;
	
	private final RepositoryEntry entry;
	private CourseConfig courseConfig;
	private final boolean editable;

	public EfficiencyStatementController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.courseConfig = courseConfig;
		this.editable = editable;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.efficency.title");
		setFormContextHelp("manual_user/learningresources/Course_Settings/#assessment");
		formLayout.setElementCssClass("o_sel_course_efficiency_statements");
		
		boolean managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		efficiencyStatementEl = uifactory.addToggleButton("effIsOn", "efficiency.statement.show", translate("on"), translate("off"), formLayout);
		efficiencyStatementEl.addActionListener(FormEvent.ONCHANGE);
		efficiencyStatementEl.toggle(courseConfig.isEfficiencyStatementEnabled());
		efficiencyStatementEl.setEnabled(editable && !managedEff);
		
		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == disableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doChangeConfig(ureq);
			} else {
				efficiencyStatementEl.toggleOn();
			}
		} else if (source == enableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doChangeConfig(ureq);
			} else {
				efficiencyStatementEl.toggleOff();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doSave(UserRequest ureq) {
		boolean confirmUpdateStatement = courseConfig.isEfficiencyStatementEnabled() != efficiencyStatementEl.isOn();
		if (confirmUpdateStatement) {
			if (courseConfig.isEfficiencyStatementEnabled()) {
				// a change from enabled Efficiency to disabled
				disableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.todisabled"), disableEfficiencyDC);
			} else {
				// a change from disabled Efficiency
				enableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.toenable"), enableEfficiencyDC);
			}
		} else {
			doChangeConfig(ureq);
		}
	}
	
	private void doChangeConfig(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		boolean enableEfficiencyStatement = efficiencyStatementEl.isOn();
		boolean updateStatement = courseConfig.isEfficiencyStatementEnabled() != enableEfficiencyStatement;
		courseConfig.setEfficiencyStatementIsEnabled(enableEfficiencyStatement);

		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if(updateStatement) {
			EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
			CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigType.efficiencyStatement, course.getResourceableId());
			eventBus.fireEventToListenersOf(courseConfigEvent, course);
			
			ILoggingAction loggingAction = enableEfficiencyStatement ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
