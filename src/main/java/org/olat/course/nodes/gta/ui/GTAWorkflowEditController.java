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
package org.olat.course.nodes.gta.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.handler.AssessmentConfig.CoachAssignmentMode;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.model.NoDueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormItem;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAWorkflowEditController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] executionKeys = new String[]{ GTAType.group.name(), GTAType.individual.name() };
	
	private static final String[] optionalKeys = new String[] { AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name() };
	private static final String[] solutionVisibleToAllKeys = new String[] { "all", "restricted" };
	
	private static final String ASSIGNMENT_COACHES = "coaches";
	private static final String ASSIGNMENT_COACHES_AND_OWNERS = "coaches-owners";
	
	private static final String REVIEW_AND_CORRECTION_KEY = "review-correction";
	private static final String PEER_REVIEW_KEY = "peer-review";
	
	private CloseableModalController cmc;
	private DialogBoxController confirmChangesCtrl;
	private AreaSelectionController areaSelectionCtrl;
	private GroupSelectionController groupSelectionCtrl;
	
	private SingleSelection typeEl;
	private SingleSelection optionalEl;
	private FormLink chooseGroupButton;
	private FormLink chooseAreaButton;
	private StaticTextElement groupListEl;
	private StaticTextElement areaListEl;
	private DueDateConfigFormItem assignmentDeadlineEl;
	private DueDateConfigFormItem submissionDeadlineEl;
	private DueDateConfigFormItem solutionVisibleAfterEl;
	private DueDateConfigFormItem lateSubmissionDeadlineEl;
	private MultipleSelectionElement relativeDatesEl;
	private FormToggle taskAssignmentEl;
	private FormToggle feedbackEl;
	private SingleSelection feedbackTypeEl;
	private DueDateConfigFormItem peerReviewPeriodEl;
	private TextElement peerReviewPeriodLengthEl;
	private FormToggle revisionEl;
	private FormToggle sampleEl;
	private FormToggle gradingEl;
	private FormToggle submissionEl;
	private MultipleSelectionElement lateSubmissionEl;
	private FormLayoutContainer stepsCont;
	private SingleSelection solutionVisibleToAllEl;
	private FormLayoutContainer documentsCont;
	private MultipleSelectionElement coachAllowedUploadEl;
	private MultipleSelectionElement coachAssignmentEnabledEl;
	private SingleSelection coachAssignmentModeEl;
	private SingleSelection coachOrCoachAndOwnerModeEl;
	private MultipleSelectionElement assignmentNotificationCoachEl;
	private MultipleSelectionElement assignmentNotificationParticipantEl;
	
	private final GTACourseNode gtaNode;
	private final ModuleConfiguration config;
	private boolean optional;
	private final CourseEditorEnv courseEditorEnv;
	private List<Long> areaKeys;
	private List<Long> groupKeys;
	private final RepositoryEntry courseRe;
	private final boolean isLearningPath;
	private final boolean individualTask;
	
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private DueDateService dueDateService;
	
	public GTAWorkflowEditController(UserRequest ureq, WindowControl wControl, GTACourseNode gtaNode, CourseEditorEnv courseEditorEnv) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(getTranslator(), DueDateConfigFormItem.class, getLocale()));
		this.gtaNode = gtaNode;
		config = gtaNode.getModuleConfiguration();
		individualTask = gtaNode.getType().equals(GTACourseNode.TYPE_INDIVIDUAL);
		this.courseEditorEnv = courseEditorEnv;
		
		//reload to make sure we have the last changes
		courseRe = repositoryService
				.loadByKey(courseEditorEnv.getCourseGroupManager().getCourseEntry().getKey());
		ICourse course = CourseFactory.loadCourse(courseRe);
		isLearningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
		
		optional = config.getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTypeForm(formLayout);
		initStepForm(formLayout);
		initDocumentsForm(formLayout);
		initCoaching(formLayout);
		initButtonsForm(formLayout, ureq);
	}
	
	private void initTypeForm(FormItemContainer formLayout) {
		String type = config.getStringValue(GTACourseNode.GTASK_TYPE);
		
		FormLayoutContainer typeCont = FormLayoutContainer.createDefaultFormLayout("type", getTranslator());
		typeCont.setFormTitle(translate("task.type.title"));
		typeCont.setFormDescription(translate("task.type.description"));
		typeCont.setElementCssClass("o_sel_course_gta_groups_areas");
		typeCont.setRootForm(mainForm);
		formLayout.add(typeCont);
		
		String[] executionValues = new String[]{
				translate("task.execution.group"),
				translate("task.execution.individual")
		};

		typeEl = uifactory.addDropdownSingleselect("execution", "task.execution", typeCont, executionKeys, executionValues, null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		typeEl.setEnabled(false);
		if(StringHelper.containsNonWhitespace(type)) {
			for(String executionKey:executionKeys) {
				if(executionKey.equals(type)) {
					typeEl.select(executionKey, true);
				}
			}
		}
		if(!typeEl.isOneSelected()) {
			typeEl.select(executionKeys[0], true);
		}

		chooseGroupButton = uifactory.addFormLink("choose.groups", "choose.groups", "choosed.groups", typeCont, Link.BUTTON_XSMALL);
		chooseGroupButton.setCustomEnabledLinkCSS("btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupButton.setElementCssClass("o_omit_margin");
		chooseGroupButton.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		if(!courseEditorEnv.getCourseGroupManager().hasBusinessGroups()){
			chooseGroupButton.setI18nKey("create.groups");
		}
		
		groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
		String groupList = getGroupNames(groupKeys);
		groupListEl = uifactory.addStaticTextElement("group.list", null, groupList, typeCont);		
		groupListEl.setElementCssClass("text-muted");
		groupListEl.setLabel(null, null);
		
		chooseAreaButton = uifactory.addFormLink("choose.areas", "choose.areas", "choosed.areas", typeCont, Link.BUTTON_XSMALL);
		chooseAreaButton.setCustomEnabledLinkCSS("btn btn-default o_xsmall o_form_areachooser");
		chooseAreaButton.setElementCssClass("o_omit_margin");
		chooseAreaButton.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");
		if(!courseEditorEnv.getCourseGroupManager().hasAreas()){
			chooseAreaButton.setI18nKey("create.areas");
		}
		
		areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
		String areaList = getAreaNames(areaKeys);
		areaListEl = uifactory.addStaticTextElement("areas.list", null, areaList, typeCont);
		areaListEl.setElementCssClass("text-muted");
		areaListEl.setLabel(null, null);
		
		boolean mismatch = ((GTAType.group.name().equals(type) && !gtaNode.getType().equals(GTACourseNode.TYPE_GROUP))
				|| (GTAType.individual.name().equals(type) && !gtaNode.getType().equals(GTACourseNode.TYPE_INDIVIDUAL)));
		
		if(GTAType.group.name().equals(type)) {
			typeEl.setVisible(mismatch);
		} else if(GTAType.individual.name().equals(type)) {
			if(mismatch) {
				typeEl.setVisible(true);
				chooseGroupButton.setVisible(false);
				groupListEl.setVisible(false);
				chooseAreaButton.setVisible(false);
				areaListEl.setVisible(false);
			} else {
				typeCont.setVisible(false);
			}
		}
	}
	
	private void initStepForm(FormItemContainer formLayout) {
		//Steps
		stepsCont = FormLayoutContainer.createDefaultFormLayout("steps", getTranslator());
		stepsCont.setFormTitle(translate("task.steps.title"));
		stepsCont.setFormDescription(translate("task.steps.description"));
		stepsCont.setElementCssClass("o_sel_course_gta_steps");
		stepsCont.setRootForm(mainForm);
		stepsCont.setFormContextHelp("manual_user/learningresources/Course_Element_Task/#configurations");
		formLayout.add(stepsCont);

		String[] optionalValues = new String[] {
				translate("task.mandatory"), translate("task.optional"),
		};
		optionalEl = uifactory.addRadiosHorizontal("obligation", "task.obligation", stepsCont, optionalKeys, optionalValues);
		optionalEl.addActionListener(FormEvent.ONCHANGE);	
		if(optional) {
			optionalEl.select(optionalKeys[1], true);
		} else {
			optionalEl.select(optionalKeys[0], true);
		}
		optionalEl.setVisible(!isLearningPath);

		relativeDatesEl = uifactory.addCheckboxesHorizontal("relative.dates", "relative.dates", stepsCont, onKeys, new String[]{ "" });
		relativeDatesEl.addActionListener(FormEvent.ONCHANGE);
		boolean useRelativeDates = config.getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES);
		relativeDatesEl.select(onKeys[0], useRelativeDates);
		
		uifactory.addSpacerElement("s1", stepsCont, false);
		
		//assignment
		taskAssignmentEl = uifactory.addToggleButton("task.assignment", "task.assignment", translate("on"), translate("off"), stepsCont);
		taskAssignmentEl.setElementCssClass("o_sel_gta_step_assignment");
		taskAssignmentEl.setText(translate("task.assignment.enabled"));
		taskAssignmentEl.addActionListener(FormEvent.ONCHANGE);
		boolean assignement = config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT);
		taskAssignmentEl.toggle(assignement);
		
		assignmentDeadlineEl = DueDateConfigFormItem.create("assignment.deadline", getRelativeToDates(true),
				useRelativeDates, gtaNode.getDueDateConfig(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE));
		assignmentDeadlineEl.setLabel("assignment.deadline", null);
		assignmentDeadlineEl.setVisible(assignement);
		stepsCont.add(assignmentDeadlineEl);
		
		uifactory.addSpacerElement("s2", stepsCont, false);

		//turning in
		submissionEl = uifactory.addToggleButton("submission", "submission", translate("on"), translate("off"), stepsCont);
		submissionEl.setElementCssClass("o_sel_gta_step_submission");
		submissionEl.setText(translate("submission.enabled"));
		submissionEl.addActionListener(FormEvent.ONCHANGE);
		boolean submit = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		submissionEl.toggle(submit);
		
		submissionDeadlineEl = DueDateConfigFormItem.create("submit.deadline", getRelativeToDates(false), useRelativeDates,
				gtaNode.getDueDateConfig(GTACourseNode.GTASK_SUBMIT_DEADLINE));
		submissionDeadlineEl.setLabel("submit.deadline", null);
		submissionDeadlineEl.setVisible(submit);
		stepsCont.add(submissionDeadlineEl);
		
		String[] lateSubmissionValues = new String[] { translate("late.submission.enabled") };
		lateSubmissionEl = uifactory.addCheckboxesHorizontal("late.submission", "late.submission", stepsCont, onKeys, lateSubmissionValues);
		lateSubmissionEl.addActionListener(FormEvent.ONCHANGE);
		boolean lateSubmit = config.getBooleanSafe(GTACourseNode.GTASK_LATE_SUBMIT);
		lateSubmissionEl.select(onKeys[0], lateSubmit);
		
		lateSubmissionDeadlineEl = DueDateConfigFormItem.create("late.submit.deadline", getRelativeToDates(false), useRelativeDates,
				gtaNode.getDueDateConfig(GTACourseNode.GTASK_LATE_SUBMIT_DEADLINE));
		lateSubmissionDeadlineEl.setLabel("late.submit.deadline", null);
		lateSubmissionDeadlineEl.setVisible(lateSubmit);
		lateSubmissionDeadlineEl.setMandatory(true);
		stepsCont.add(lateSubmissionDeadlineEl);
		
		uifactory.addSpacerElement("s3", stepsCont, false);

		// feedback: review and correction with coach or peer review
		feedbackEl = uifactory.addToggleButton("feedback", "feedback", translate("on"), translate("off"), stepsCont);
		feedbackEl.setElementCssClass("o_sel_gta_step_feedback");
		feedbackEl.setText(translate("feedback.text"));
		feedbackEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean correctionEnabled = config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION);
		boolean peerReviewEnabled = config.getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW);
		feedbackEl.toggle(correctionEnabled || peerReviewEnabled);
		
		SelectionValues feedbackPK = new SelectionValues();
		feedbackPK.add(SelectionValues.entry(REVIEW_AND_CORRECTION_KEY, translate("feedback.review.and.correction"), translate("feedback.review.and.correction.descr"), null, null, true));
		feedbackPK.add(SelectionValues.entry(PEER_REVIEW_KEY, translate("feedback.peer.review"), translate("feedback.peer.review.descr"), null, null, true));
		feedbackTypeEl = uifactory.addCardSingleSelectHorizontal("feedback.type", "feedback.type", stepsCont, feedbackPK);
		feedbackTypeEl.addActionListener(FormEvent.ONCHANGE);
		feedbackTypeEl.setVisible(feedbackEl.isOn() && individualTask);
		if(correctionEnabled) {
			feedbackTypeEl.select(REVIEW_AND_CORRECTION_KEY, true);
		} else if(peerReviewEnabled) {
			feedbackTypeEl.select(PEER_REVIEW_KEY, true);
		}

		DueDateConfig peerReviewPeriodConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE);
		peerReviewPeriodEl = DueDateConfigFormItem.create("peer.review.period.visible", getRelativeToDates(false),
				useRelativeDates, peerReviewPeriodConfig);
		peerReviewPeriodEl.setPeriod(true);
		peerReviewPeriodEl.setLabel("peer.review.period.visible", null);
		peerReviewPeriodEl.setVisible(feedbackEl.isOn() && peerReviewEnabled);
		stepsCont.add(peerReviewPeriodEl);
		
		String peerReviewLength = config.getStringValue(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE_LENGTH, "");
		peerReviewPeriodLengthEl = uifactory.addTextElement("peer.review.period.length", 4, peerReviewLength, stepsCont);
		peerReviewPeriodLengthEl.setVisible(peerReviewEnabled && useRelativeDates);
		peerReviewPeriodLengthEl.setMaxLength(4);
		peerReviewPeriodLengthEl.setDisplaySize(4);
		peerReviewPeriodLengthEl.setElementCssClass("form-inline");
		peerReviewPeriodLengthEl.setTextAddOn("days");
		peerReviewPeriodLengthEl.setMandatory(true);
		
		//revision
		revisionEl = uifactory.addToggleButton("revision", "revision.period", translate("on"), translate("off"), stepsCont);
		revisionEl.setElementCssClass("o_sel_gta_step_revision");
		revisionEl.setText(translate("revision.enabled"));
		revisionEl.addActionListener(FormEvent.ONCHANGE);
		boolean revision = config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD);
		revisionEl.toggle(revision);
		revisionEl.setVisible(feedbackEl.isOn() && correctionEnabled);		
		uifactory.addSpacerElement("s4", stepsCont, false);

		//sample solution
		sampleEl = uifactory.addToggleButton("sample", "sample.solution", translate("on"), translate("off"), stepsCont);
		sampleEl.setElementCssClass("o_sel_gta_step_solution");
		sampleEl.setText(translate("sample.solution.enabled"));
		sampleEl.addActionListener(FormEvent.ONCHANGE);
		boolean sample = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION);
		sampleEl.toggle(sample);
		
		DueDateConfig solutionVisibleAfterConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		solutionVisibleAfterEl = DueDateConfigFormItem.create("sample.solution.visible.after", getRelativeToDates(false),
				useRelativeDates, solutionVisibleAfterConfig);
		solutionVisibleAfterEl.setLabel("sample.solution.visible.after", null);
		solutionVisibleAfterEl.setVisible(sample);
		stepsCont.add(solutionVisibleAfterEl);
		
		boolean solutionVisibleRelToAll = config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false);
		String[] solutionVisibleToAllValues = getSolutionVisibleToAllValues();
		solutionVisibleToAllEl = uifactory.addRadiosVertical("visibleall", "sample.solution.visible.for", stepsCont, solutionVisibleToAllKeys, solutionVisibleToAllValues);
		solutionVisibleToAllEl.setVisible(sample && (DueDateConfig.isAbsolute(solutionVisibleAfterConfig) || optional));
		if(solutionVisibleRelToAll) {
			solutionVisibleToAllEl.select(solutionVisibleToAllKeys[0], true);
		} else {
			solutionVisibleToAllEl.select(solutionVisibleToAllKeys[1], true);
		}
		uifactory.addSpacerElement("s5", stepsCont, false);

		//grading
		gradingEl = uifactory.addToggleButton("grading", "grading", translate("on"), translate("off"), stepsCont);
		gradingEl.setElementCssClass("o_sel_gta_step_grading");
		gradingEl.setText(translate("grading.enabled"));
		gradingEl.addActionListener(FormEvent.ONCHANGE);
		boolean grading = config.getBooleanSafe(GTACourseNode.GTASK_GRADING);
		gradingEl.toggle(grading);
	}

	private String[] getSolutionVisibleToAllValues() {
		return new String[] {
			optional ? translate("sample.solution.visible.all.optional") : translate("sample.solution.visible.all"),
			translate("sample.solution.visible.upload")
		};
	}
	
	private void initDocumentsForm(FormItemContainer formLayout) {
		documentsCont = FormLayoutContainer.createDefaultFormLayout("documents", getTranslator());
		documentsCont.setFormTitle(translate("task.documents.title"));
		documentsCont.setRootForm(mainForm);
		formLayout.add(documentsCont);
		
		//coach allowed to upload documents
		String[] onValues = new String[]{ translate("task.manage.documents.coach") };
		coachAllowedUploadEl = uifactory.addCheckboxesVertical("coachTasks", "task.manage.documents", documentsCont, onKeys, onValues, 1);
		
		updateDocuments();
	}
	
	private void initCoaching(FormItemContainer formLayout) {
		FormLayoutContainer coachingLayout = uifactory.addDefaultFormLayout("coaching", null, formLayout);
		coachingLayout.setFormTitle(translate("coach.assignment.title"));
		coachingLayout.setVisible(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE)));
		
		String[] onValues = new String[]{ translate("coach.assignment.enabled") };
		coachAssignmentEnabledEl = uifactory.addCheckboxesVertical("coach.assignment", coachingLayout, onKeys, onValues, 1);
		coachAssignmentEnabledEl.setVisible(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE)));
		coachAssignmentEnabledEl.addActionListener(FormEvent.ONCHANGE);
		boolean coachAssignment = config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT, false);
		if(coachAssignment) {
			coachAssignmentEnabledEl.select(onKeys[0], true);
		}

		SelectionValues assignmentCoachesAndOwnersKV = new SelectionValues();
		assignmentCoachesAndOwnersKV.add(SelectionValues.entry(ASSIGNMENT_COACHES, translate("coach.assignment.mode.coaches.only")));
		assignmentCoachesAndOwnersKV.add(SelectionValues.entry(ASSIGNMENT_COACHES_AND_OWNERS, translate("coach.assignment.mode.coaches.and.owners")));
		coachOrCoachAndOwnerModeEl = uifactory.addRadiosVertical("coach.assignment.mode.owners", "coach.assignment.mode.owners", coachingLayout,
				assignmentCoachesAndOwnersKV.keys(), assignmentCoachesAndOwnersKV.values());
		coachOrCoachAndOwnerModeEl.setVisible(coachAssignment);
		boolean withOwners = config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT_OWNERS, false);
		if(withOwners) {
			coachOrCoachAndOwnerModeEl.select(ASSIGNMENT_COACHES_AND_OWNERS, true);
		} else {
			coachOrCoachAndOwnerModeEl.select(ASSIGNMENT_COACHES, true);
		}

		SelectionValues assignmentModesKV = new SelectionValues();
		assignmentModesKV.add(SelectionValues.entry(CoachAssignmentMode.manual.name(), translate("coach.assignment.mode.manual")));
		assignmentModesKV.add(SelectionValues.entry(CoachAssignmentMode.automatic.name(), translate("coach.assignment.mode.auto")));
		coachAssignmentModeEl = uifactory.addRadiosHorizontal("coach.assignment.mode", "coach.assignment.mode", coachingLayout,
				assignmentModesKV.keys(), assignmentModesKV.values());
		coachAssignmentModeEl.setVisible(coachAssignment);
		String mode = config.getStringValue(GTACourseNode.GTASK_COACH_ASSIGNMENT_MODE, GTACourseNode.GTASK_COACH_ASSIGNMENT_MODE_DEFAULT);
		if(StringHelper.containsNonWhitespace(mode) && assignmentModesKV.containsKey(mode)) {
			coachAssignmentModeEl.select(mode, true);
		} else {
			coachAssignmentModeEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_MODE_DEFAULT, true);
		}
		
		SelectionValues notificationCoachKV = new SelectionValues();
		notificationCoachKV.add(SelectionValues.entry(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT, translate("coach.assignment.notification.coach.assignment")));
		notificationCoachKV.add(SelectionValues.entry(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT, translate("coach.assignment.notification.coach.unassignment")));
		notificationCoachKV.add(SelectionValues.entry(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER, translate("coach.assignment.notification.coach.neworder")));
		assignmentNotificationCoachEl = uifactory.addCheckboxesVertical("coach.assignment.notification.coach", coachingLayout,
				notificationCoachKV.keys(), notificationCoachKV.values(), 1);
		assignmentNotificationCoachEl.setVisible(coachAssignment);
		assignmentNotificationCoachEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT,
				config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT, true));
		assignmentNotificationCoachEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT,
				config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT, true));
		assignmentNotificationCoachEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER,
				config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER, true));
		
		SelectionValues notificationParticipantKV = new SelectionValues();
		notificationParticipantKV.add(SelectionValues.entry(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT, translate("coach.assignment.notification.participant.assignment")));
		assignmentNotificationParticipantEl = uifactory.addCheckboxesVertical("coach.assignment.notification.participant", coachingLayout,
				notificationParticipantKV.keys(), notificationParticipantKV.values(), 1);
		assignmentNotificationParticipantEl.setVisible(coachAssignment);
		assignmentNotificationParticipantEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT,
				config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT, true));
	}
	
	private void initButtonsForm(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonswrapper", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonsWrapperCont);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		buttonCont.setElementCssClass("o_sel_course_gta_save_workflow");
		buttonsWrapperCont.add(buttonCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		assignmentDeadlineEl.clearError();
		if (!assignmentDeadlineEl.validate()) {
			allOk &= false;
		}
		
		submissionDeadlineEl.clearError();
		if (!submissionDeadlineEl.validate()) {
			allOk &= false;
		}
		
		allOk &= validateLateDeadline(lateSubmissionDeadlineEl, submissionDeadlineEl);
		
		solutionVisibleAfterEl.clearError();
		if (!solutionVisibleAfterEl.validate()) {
			allOk &= false;
		}
		
		solutionVisibleToAllEl.clearError();
		if(solutionVisibleToAllEl.isVisible() && !solutionVisibleToAllEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		peerReviewPeriodLengthEl.clearError();
		if(peerReviewPeriodLengthEl.isVisible()) {
			if(!StringHelper.containsNonWhitespace(peerReviewPeriodLengthEl.getValue())) {
				peerReviewPeriodLengthEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else if(!StringHelper.isLong(peerReviewPeriodLengthEl.getValue())) {
				peerReviewPeriodLengthEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		taskAssignmentEl.clearError();
		if(!taskAssignmentEl.isOn() && !submissionEl.isOn()
				&& !feedbackEl.isOn() && !revisionEl.isOn()
				&& !sampleEl.isOn() && !gradingEl.isOn()) {

			taskAssignmentEl.setErrorKey("error.select.atleastonestep");
			allOk &= false;
		}
		
		coachAssignmentModeEl.clearError();
		if(!coachAssignmentModeEl.isOneSelected()) {
			coachAssignmentModeEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateLateDeadline(DueDateConfigFormItem lateDeadlineEl, DueDateConfigFormItem deadlineEl) {
		boolean allOk = true;
		
		lateDeadlineEl.clearError();
		if(lateDeadlineEl.isVisible()) {
			if(!lateDeadlineEl.validate()) {
				allOk &= false;
			} else if(lateDeadlineEl.getDueDateConfig() == NoDueDateConfig.NO_DUE_DATE_CONFIG) {
				lateDeadlineEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else if(deadlineEl.getDueDateConfig() == NoDueDateConfig.NO_DUE_DATE_CONFIG ) {
				lateDeadlineEl.setErrorKey("error.late.submission.mandatory");
				allOk &= false;
			} else {
				DueDateConfig submissionConfig = deadlineEl.getDueDateConfig();
				DueDateConfig lateSubmissionConfig = lateDeadlineEl.getDueDateConfig();
				if(submissionConfig.getAbsoluteDate() != null && lateSubmissionConfig.getAbsoluteDate() != null &&
						submissionConfig.getAbsoluteDate().compareTo(lateSubmissionConfig.getAbsoluteDate()) >= 0) {
					lateDeadlineEl.setErrorKey("error.late.submission.after.submission");
					allOk &= false;
				} else if(submissionConfig.getRelativeToType() != null && lateSubmissionConfig.getRelativeToType() != null) {
					if(lateSubmissionConfig.getNumOfDays() < 0) {
						lateDeadlineEl.setErrorKey("form.mandatory.hover");
						allOk &= false;
					} else if(submissionConfig.getNumOfDays() < 0) {
						lateDeadlineEl.setErrorKey("error.late.submission.mandatory");
						allOk &= false;
					}else if(submissionConfig.getRelativeToType().equals(lateSubmissionConfig.getRelativeToType())
							&& submissionConfig.getNumOfDays() >= lateSubmissionConfig.getNumOfDays()) {
						lateDeadlineEl.setErrorKey("error.late.submission.after.submission");
						allOk &= false;
					}
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		RepositoryEntry entry = courseEditorEnv.getCourseGroupManager().getCourseEntry();
		if(gtaManager.isTasksInProcess(entry, gtaNode)) {
			doConfirmChanges(ureq);
		} else {
			commitChanges();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private void doConfirmChanges(UserRequest ureq) {
		String title = translate("warning.tasks.in.process.title");
		String url = helpModule.getManualProvider().getURL(getLocale(), "manual_user/learningresources/Course_Element_Task/");
		String text = translate("warning.tasks.in.process.text", url);
		confirmChangesCtrl = activateOkCancelDialog(ureq, title, text, confirmChangesCtrl);
	}
	
	private void commitChanges() {
		if(typeEl.isSelected(0)) {
			config.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.group.name());
			config.setList(GTACourseNode.GTASK_AREAS, areaKeys);
			config.setList(GTACourseNode.GTASK_GROUPS, groupKeys);
		} else {
			config.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
			config.setList(GTACourseNode.GTASK_AREAS, new ArrayList<>(0));
			config.setList(GTACourseNode.GTASK_GROUPS, new ArrayList<>(0));
		}
		
		if (optionalEl.isVisible()) {
			config.setStringValue(GTACourseNode.GTASK_OBLIGATION, AssessmentObligation.valueOf(optionalEl.getSelectedKey()).name());
		}
		
		boolean relativeDates = relativeDatesEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_RELATIVE_DATES, relativeDates);
		
		// Assignment
		boolean assignment = taskAssignmentEl.isOn();
		config.setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT, assignment);
		DueDateConfig assignmentDueDateConfig = assignment? assignmentDeadlineEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		config.setIntValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, assignmentDueDateConfig.getNumOfDays());
		config.setStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO, assignmentDueDateConfig.getRelativeToType());
		config.setDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE, assignmentDueDateConfig.getAbsoluteDate());
		
		// Submission step
		boolean turningIn = submissionEl.isOn();
		config.setBooleanEntry(GTACourseNode.GTASK_SUBMIT, turningIn);
		DueDateConfig submissionDueDateConfig = turningIn? submissionDeadlineEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		config.setIntValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE, submissionDueDateConfig.getNumOfDays());
		config.setStringValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO, submissionDueDateConfig.getRelativeToType());
		config.setDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE, submissionDueDateConfig.getAbsoluteDate());
		
		// Late submission
		boolean turningLateIn = turningIn && lateSubmissionEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_LATE_SUBMIT, turningLateIn);
		DueDateConfig lateSubmissionDueDateConfig = turningLateIn? lateSubmissionDeadlineEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		config.setIntValue(GTACourseNode.GTASK_LATE_SUBMIT_DEADLINE_RELATIVE, lateSubmissionDueDateConfig.getNumOfDays());
		config.setStringValue(GTACourseNode.GTASK_LATE_SUBMIT_DEADLINE_RELATIVE_TO, lateSubmissionDueDateConfig.getRelativeToType());
		config.setDateValue(GTACourseNode.GTASK_LATE_SUBMIT_DEADLINE, lateSubmissionDueDateConfig.getAbsoluteDate());
		
		// Review and peer review
		String feedback = "false";
		if(feedbackEl.isOn()) {
			if(individualTask) {
				feedback = feedbackTypeEl.getSelectedKey();
			} else {
				feedback = REVIEW_AND_CORRECTION_KEY;
			}
		}
				
		boolean correction = REVIEW_AND_CORRECTION_KEY.equals(feedback);
		boolean peerReview = PEER_REVIEW_KEY.equals(feedback);
		config.setBooleanEntry(GTACourseNode.GTASK_REVIEW_AND_CORRECTION, correction);
		config.setBooleanEntry(GTACourseNode.GTASK_PEER_REVIEW, peerReview);
		config.setBooleanEntry(GTACourseNode.GTASK_REVISION_PERIOD, correction && revisionEl.isOn());

		DueDateConfig peerReviewDueDateConfig = peerReview ? peerReviewPeriodEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		config.setIntValue(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE_RELATIVE, peerReviewDueDateConfig.getNumOfDays());
		config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE_RELATIVE_TO, peerReviewDueDateConfig.getRelativeToType());
		config.setDateValue(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE_START, peerReviewDueDateConfig.getAbsoluteStartDate());
		config.setDateValue(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE, peerReviewDueDateConfig.getAbsoluteDate());
		String peerReviewDeadlineLength = peerReviewPeriodLengthEl.getValue();
		if(StringHelper.isLong(peerReviewDeadlineLength)) {
			config.setStringValue(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE_LENGTH, peerReviewDeadlineLength);
		} else {
			config.remove(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE_LENGTH);
		}
		
		boolean sample = sampleEl.isOn();
		config.setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION, sample);
		DueDateConfig sampleDueDateConfig = sample ? solutionVisibleAfterEl.getDueDateConfig(): DueDateConfig.noDueDateConfig();
		config.setIntValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE, sampleDueDateConfig.getNumOfDays());
		config.setStringValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER_RELATIVE_TO, sampleDueDateConfig.getRelativeToType());
		config.setDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER, sampleDueDateConfig.getAbsoluteDate());
		config.setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, solutionVisibleToAllEl.isSelected(0));

		config.setBooleanEntry(GTACourseNode.GTASK_GRADING, gradingEl.isOn());
		
		if (documentsCont.isVisible()) {
			boolean coachUploadAllowed = coachAllowedUploadEl.isAtLeastSelected(1);
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, coachUploadAllowed);
		}
		
		boolean coachAssignment = coachAssignmentEnabledEl.isVisible() && coachAssignmentEnabledEl.isAtLeastSelected(1);
		config.setBooleanEntry(GTACourseNode.GTASK_COACH_ASSIGNMENT, coachAssignment);
		
		if(coachAssignment) {
			String assignmentMode = coachAssignmentModeEl.getSelectedKey();
			config.setStringValue(GTACourseNode.GTASK_COACH_ASSIGNMENT_MODE, assignmentMode);
			
			String coachAndOwnersKey = coachOrCoachAndOwnerModeEl.getSelectedKey();
			boolean coachAndOwners = ASSIGNMENT_COACHES_AND_OWNERS.equals(coachAndOwnersKey);
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ASSIGNMENT_OWNERS, coachAndOwners);
			
			Collection<String> coachNotifications = assignmentNotificationCoachEl.getSelectedKeys();
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT,
					coachNotifications.contains(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT));
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT,
					coachNotifications.contains(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT));
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER,
					coachNotifications.contains(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER));
			config.setBooleanEntry(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT,
					assignmentNotificationParticipantEl.getSelectedKeys().contains(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT));
		} else {
			config.remove(GTACourseNode.GTASK_COACH_ASSIGNMENT_MODE);
			config.remove(GTACourseNode.GTASK_COACH_ASSIGNMENT_OWNERS);
			config.remove(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT);
			config.remove(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT);
			config.remove(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER);
			config.remove(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(submissionEl == source || lateSubmissionEl == source) {
			updateSubmissionDeadline();
		} else if(taskAssignmentEl == source) {
			updateAssignmentDeadline();
			updateDocuments();
		} else if(relativeDatesEl == source) {
			updateAssignmentDeadline();
			updateSubmissionDeadline();
			updateSolutionDeadline();
			updateRevisions();
		} else if(sampleEl == source || solutionVisibleAfterEl == source || optionalEl == source) {
			updateSolutionDeadline();
			updateDocuments();
		} else if (feedbackEl == source || feedbackTypeEl == source) {
			updateRevisions();
		} else if(coachAssignmentEnabledEl == source) {
			updateCoaching();
		} else if(chooseGroupButton == source) {
			doChooseGroup(ureq);
		} else if(chooseAreaButton == source) {
			doChooseArea(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	public void onNodeConfigChanged() {
		boolean newOptional = config.getStringValue(GTACourseNode.GTASK_OBLIGATION).equals(AssessmentObligation.optional.name());
		if (newOptional != optional) {
			optional = newOptional;
			updateSolutionDeadline();
		}
	}
	
	private void updateAssignmentDeadline() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean assignment = taskAssignmentEl.isOn();
		
		assignmentDeadlineEl.setRelativeToDates(getRelativeToDates(true));
		assignmentDeadlineEl.setVisible(assignment);
		assignmentDeadlineEl.setRelative(useRelativeDate);
	}
	
	private void updateSubmissionDeadline() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean submit = submissionEl.isOn();
		
		submissionDeadlineEl.setRelativeToDates(getRelativeToDates(false));
		submissionDeadlineEl.setVisible(submit);
		submissionDeadlineEl.setRelative(useRelativeDate);
		
		lateSubmissionEl.setVisible(submit);
		
		boolean lateSubmit = submit && lateSubmissionEl.isAtLeastSelected(1);
		lateSubmissionDeadlineEl.setRelativeToDates(getRelativeToDates(false));
		lateSubmissionDeadlineEl.setVisible(lateSubmit);
		lateSubmissionDeadlineEl.setRelative(useRelativeDate);
	}
	
	private void updateSolutionDeadline() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean solution = sampleEl.isOn();
		
		solutionVisibleAfterEl.setRelativeToDates(getRelativeToDates(false));
		solutionVisibleAfterEl.setVisible(solution);
		solutionVisibleAfterEl.setRelative(useRelativeDate);
		
		if (optionalEl.isVisible()) {
			optional = optionalEl.isSelected(1);
		}
		solutionVisibleToAllEl.setVisible(solution &&
				(DueDateConfig.isAbsolute(solutionVisibleAfterEl.getDueDateConfig()) || optional));
		solutionVisibleToAllEl.setKeysAndValues(solutionVisibleToAllKeys, getSolutionVisibleToAllValues(), null);
		if(!solutionVisibleToAllEl.isOneSelected()) {
			solutionVisibleToAllEl.select(solutionVisibleToAllKeys[1], true);
		}
	}
	
	private SelectionValues getRelativeToDates(boolean excludeAssignment) {
		SelectionValues relativeToDates = new SelectionValues();
		List<String> courseRelativeToDateTypes = dueDateService.getCourseRelativeToDateTypes(courseRe);
		DueDateConfigFormatter.create(getLocale()).addCourseRelativeToDateTypes(relativeToDates, courseRelativeToDateTypes);
		
		if(!excludeAssignment) {
			boolean assignment = taskAssignmentEl.isOn();
			if (assignment) {
				relativeToDates.add(SelectionValues.entry(GTACourseNode.TYPE_RELATIVE_TO_ASSIGNMENT,
						getTranslator().translate("relative.to.assignment")));
			}
		}
		
		return relativeToDates;
	}
	
	private void updateRevisions() {
		boolean feedback = feedbackEl.isOn();
		feedbackTypeEl.setVisible(feedback && individualTask);
		
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		boolean correctionEnabled = feedbackTypeEl.isVisible() && feedbackTypeEl.isKeySelected(REVIEW_AND_CORRECTION_KEY);
		boolean peerReviewEnabled = feedbackTypeEl.isVisible() && feedbackTypeEl.isKeySelected(PEER_REVIEW_KEY);
		
		peerReviewPeriodEl.setRelativeToDates(getRelativeToDates(false));
		peerReviewPeriodEl.setVisible(feedback  && peerReviewEnabled);
		peerReviewPeriodEl.setRelative(useRelativeDate);
		peerReviewPeriodLengthEl.setVisible(feedback && peerReviewEnabled && useRelativeDate);
		
		revisionEl.setVisible(feedback && (!individualTask || correctionEnabled));
		revisionEl.toggle(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD));
	}
	
	private void updateDocuments() {
		boolean visible = taskAssignmentEl.isOn() || sampleEl.isOn();
		documentsCont.setVisible(visible);

		boolean coachUpload = config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false);
		if(coachUpload && visible) {
			coachAllowedUploadEl.select(onKeys[0], true);
		}
	}
	
	private void updateCoaching() {
		boolean visible = coachAssignmentEnabledEl.isAtLeastSelected(1);
		coachAssignmentModeEl.setVisible(visible);
		coachOrCoachAndOwnerModeEl.setVisible(visible);
		
		boolean currentVisible = assignmentNotificationCoachEl.isVisible();
		assignmentNotificationCoachEl.setVisible(visible);
		assignmentNotificationParticipantEl.setVisible(visible);
		if(!currentVisible && visible) {
			assignmentNotificationCoachEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_ASSIGNMENT, true);
			assignmentNotificationCoachEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_UNASSIGNMENT, true);
			assignmentNotificationCoachEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_COACH_NOTIFICATION_NEW_ORDER, true);
			assignmentNotificationParticipantEl.select(GTACourseNode.GTASK_COACH_ASSIGNMENT_PARTICIPANT_NOTIFICATION_ASSIGNMENT, true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				groupKeys = groupSelectionCtrl.getSelectedKeys();
				groupListEl.setValue(getGroupNames(groupKeys));
				if(courseEditorEnv.getCourseGroupManager().hasBusinessGroups()) {
					chooseGroupButton.setI18nKey("choose.groups");
				} else {
					chooseGroupButton.setI18nKey("create.groups");
				}
				cmc.deactivate();
				cleanUp();
				groupListEl.getRootForm().submit(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(areaSelectionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				areaKeys = areaSelectionCtrl.getSelectedKeys();
				areaListEl.setValue(getAreaNames(areaKeys));
				if(courseEditorEnv.getCourseGroupManager().hasAreas()) {
					chooseAreaButton.setI18nKey("choose.areas");
				} else {
					chooseAreaButton.setI18nKey("create.areas");
				}
				cmc.deactivate();
				cleanUp();
				areaListEl.getRootForm().submit(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(confirmChangesCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				commitChanges();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(groupSelectionCtrl);
		removeAsListenerAndDispose(areaSelectionCtrl);
		removeAsListenerAndDispose(cmc);
		groupSelectionCtrl = null;
		areaSelectionCtrl = null;
		cmc = null;
	}
	
	private void doChooseGroup(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupSelectionCtrl);

		groupSelectionCtrl = new GroupSelectionController(ureq, getWindowControl(), true,
				courseEditorEnv.getCourseGroupManager(), groupKeys);
		listenTo(groupSelectionCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), groupSelectionCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseArea(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaSelectionCtrl);
		
		areaSelectionCtrl = new AreaSelectionController (ureq, getWindowControl(), true,
				courseEditorEnv.getCourseGroupManager(), areaKeys);
		listenTo(areaSelectionCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), areaSelectionCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private String getGroupNames(List<Long> groupKeyList) {
		StringBuilder sb = new StringBuilder(64);
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(groupKeyList);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_group'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(group.getName()));
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> areaKeyList) {
		StringBuilder sb = new StringBuilder(64);
		List<BGArea> areas = areaManager.loadAreas(areaKeyList);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_courseareas'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(area.getName()));
		}
		return sb.toString();
	}
	
}