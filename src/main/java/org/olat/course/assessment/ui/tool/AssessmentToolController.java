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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.bulk.BulkAssessmentOverviewController;
import org.olat.course.assessment.ui.reset.ResetData1OptionsStep;
import org.olat.course.assessment.ui.reset.ResetDataContext;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.reset.ResetDataFinishStepCallback;
import org.olat.course.assessment.ui.reset.ResetWizardContext;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeIdentityEvent;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.07.2015<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolController extends MainLayoutBasicController implements Activateable2 {

	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment coachUserEnv;
	private final AssessmentToolSecurityCallback assessmentCallback;

	private Link bulkAssessmentLink;
	private Link resetDataLink;
	private final TooledStackedPanel stackPanel;
	private final AssessmentToolContainer toolContainer;

	private CloseableModalController cmc;
	private AssessmentCourseTreeController courseTreeCtrl;
	private AssessmentEventToState assessmentEventToState;
	private BulkAssessmentOverviewController bulkAssessmentOverviewCtrl;
	private StepsMainRunController resetDataCtrl;

	public AssessmentToolController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, UserCourseEnvironment coachUserEnv,
			AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.courseEntry = courseEntry;
		this.stackPanel = stackPanel;
		this.coachUserEnv = coachUserEnv;
		this.assessmentCallback = assessmentCallback;

		toolContainer = new AssessmentToolContainer();

		stackPanel.addListener(this);

		putInitialPanel(new Panel("empty"));
	}

	public void initToolbar() {
		if(assessmentCallback.canResetData()) {
			resetDataLink = LinkFactory.createToolLink("reset.data", translate("reset.data"), this,
					"o_icon_reset_data");
			stackPanel.addTool(resetDataLink, Align.right);
		}
		
		if(!assessmentCallback.isOnlyPrincipal()) {
			bulkAssessmentLink = LinkFactory.createToolLink("bulkAssessment", translate("menu.bulkfocus"), this,
					"o_icon_group");
			bulkAssessmentLink.setElementCssClass("o_sel_assessment_tool_bulk");
			stackPanel.addTool(bulkAssessmentLink, Align.right);
		}
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if ("Identity".equalsIgnoreCase(resName) || "Node".equalsIgnoreCase(resName)
				|| "CourseNode".equalsIgnoreCase(resName) || "Overview".equalsIgnoreCase(resName)
				|| "Inspection".equalsIgnoreCase(resName)) {
			doTreeView(ureq).activate(ureq, entries, null);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (bulkAssessmentLink == source) {
			cleanUp();
			doBulkAssessmentView(ureq);
		} else if(resetDataLink == source) {
			cleanUp();
			doResetData(ureq);
		} else if (stackPanel == source) {
			if (event instanceof PopEvent pe) {
				if (pe.isClose()) {
					stackPanel.popUpToRootController(ureq);
				} else if (pe.getController() != null && (pe.getController() == courseTreeCtrl || pe.getController() == bulkAssessmentOverviewCtrl)) {
					cleanUp();
					doTreeView(ureq).activate(ureq, null, null);
					initToolbar();
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (assessmentEventToState != null && assessmentEventToState.handlesEvent(source, event)) {
			doTreeView(ureq).activate(ureq, createRootNodeContextEntry(), assessmentEventToState.getState(event));
		} else if (courseTreeCtrl == source) {
			if (event instanceof CourseNodeIdentityEvent cnie) {
				if (StringHelper.isLong(cnie.getCourseNodeIdent())) {
					if (cnie.getAssessedIdentity() == null) {
						OLATResourceable resource = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(cnie.getCourseNodeIdent()));
						List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceable(resource, cnie.getFilter().get());
						doTreeView(ureq).activate(ureq, entries, cnie.getFilter().get());
					} else {
						OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(cnie.getCourseNodeIdent()));
						OLATResourceable idRes = OresHelper.createOLATResourceableInstance("Identity", cnie.getAssessedIdentity().getKey());
						List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(nodeRes, idRes);
						doTreeView(ureq).activate(ureq, entries, cnie.getFilter().get());
					}
				}
			} else if (event instanceof CourseNodeEvent cne) {
				if (cne.getIdent() != null) {
					OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(cne.getIdent()));
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(nodeRes);
					doTreeView(ureq).activate(ureq, entries, null);
				}
			} else if (event instanceof AssessmentModeStatusEvent) {
				fireEvent(ureq, event);
			}
		} else if(resetDataCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					if (courseTreeCtrl != null) {
						courseTreeCtrl.reload(ureq);
					}
				}
				cleanUp();
			}
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(bulkAssessmentOverviewCtrl);
		removeAsListenerAndDispose(resetDataCtrl);
		removeAsListenerAndDispose(cmc);
		bulkAssessmentOverviewCtrl = null;
		resetDataCtrl = null;
		cmc = null;
	}

	public void reloadAssessmentModes() {
		courseTreeCtrl.reloadAssessmentModes();
	}

	private void doBulkAssessmentView(UserRequest ureq) {
		stackPanel.popUpToController(this);
		boolean canChangeUserVisibility = coachUserEnv.isAdmin()
				|| coachUserEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration()
						.getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		bulkAssessmentOverviewCtrl = new BulkAssessmentOverviewController(ureq, getWindowControl(), courseEntry,
				canChangeUserVisibility);
		listenTo(bulkAssessmentOverviewCtrl);
		stackPanel.pushController(translate("menu.bulkfocus"), bulkAssessmentOverviewCtrl);
	}
	
	private void doResetData(UserRequest ureq) {
		ResetDataContext dataContext = new ResetDataContext(courseEntry);
		dataContext.setResetCourse(ResetCourse.all);
		dataContext.setResetParticipants(ResetParticipants.all);
		ResetWizardContext wizardContext = new ResetWizardContext(getIdentity(), dataContext, coachUserEnv, assessmentCallback, true, true, true, true);
		ResetData1OptionsStep step = new ResetData1OptionsStep(ureq, wizardContext);
		
		String title = translate("wizard.reset.data.title");
		ResetDataFinishStepCallback finishCallback = new ResetDataFinishStepCallback(dataContext, assessmentCallback);
		resetDataCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finishCallback, null, title, "");
		listenTo(resetDataCtrl);
		getWindowControl().pushAsModalDialog(resetDataCtrl.getInitialComponent());
	}

	private AssessmentCourseTreeController doTreeView(UserRequest ureq) {
		if (courseTreeCtrl == null || courseTreeCtrl.isDisposed()) {
			stackPanel.popUpToController(this);
			courseTreeCtrl = new AssessmentCourseTreeController(ureq, getWindowControl(), stackPanel, courseEntry,
					coachUserEnv, toolContainer, assessmentCallback);
			listenTo(courseTreeCtrl);
			stackPanel.pushController("node", courseTreeCtrl);
			assessmentEventToState = new AssessmentEventToState(courseTreeCtrl);
		}
		return courseTreeCtrl;
	}

	private List<ContextEntry> createRootNodeContextEntry() {
		OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", Long.valueOf(courseTreeCtrl.getRootNodeId()));
		return BusinessControlFactory.getInstance().createCEListFromString(nodeRes);
	}
}
