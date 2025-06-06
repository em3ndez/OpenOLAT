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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.SafeExamBrowserEnabled;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.ui.ConfigurationHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a simplified version of the assessment mode editor.
 * 
 * Initial date: 7 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeForLectureEditController extends FormBasicController {

	private static final OLATResourceable ASSESSMENT_MODE_ORES = OresHelper.createOLATResourceableType(AssessmentMode.class);

	private FormLink chooseElementsButton;
	private TextElement nameEl;
	private RichTextElement descriptionEl;
	private StaticTextElement chooseElementsCont;
	private FormToggle useSafeExamBrowserEl;
	private TextAreaElement safeExamBrowserKeyEl;
	private FormLink safeExamBrowserConfigurationEl;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	private ChooseElementsController chooseElementsCtrl;
	private SafeExamBrowserConfigurationController safeExamBrowserConfigurationCtrl;
	
	private List<String> elementKeys;
	
	private final RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private final LectureBlock lectureBlock;
	private final OLATResourceable courseOres;
	private final RepositoryEntryLectureConfiguration lectureConfig;

	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeForLectureEditController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, AssessmentMode assessmentMode) {
		super(ureq, wControl);
		this.entry = entry;
		lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		courseOres = OresHelper.clone(entry.getOlatResource());
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		lectureBlock = this.assessmentMode.getLectureBlock();
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_mode_edit_form");
		setFormContextHelp("manual_user/learningresources/Assessment_mode/");

		if(StringHelper.containsNonWhitespace(assessmentMode.getName())) {
			setFormTitle("form.mode.title", new String[]{ assessmentMode.getName() });
		} else {
			setFormTitle("form.mode.title.add");
		}
		setFormDescription("form.mode.description");
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		if(StringHelper.containsNonWhitespace(assessmentMode.getStartElement())) {
			CourseNode startElement = course.getRunStructure().getNode(assessmentMode.getStartElement());
			if(startElement == null) {
				setFormWarning("warning.missing.start.element");
			}
		}
		
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			String elements = assessmentMode.getElementList();
			for(String element:elements.split(",")) {
				CourseNode node = course.getRunStructure().getNode(element);
				if(node == null) {
					setFormWarning("warning.missing.element");
				}
			}
		}
		
		Status status = assessmentMode.getStatus();
		String name = assessmentMode.getName();
		nameEl = uifactory.addTextElement("mode.name", "mode.name", 255, name, formLayout);
		nameEl.setElementCssClass("o_sel_assessment_mode_name");
		nameEl.setMandatory(true);
		nameEl.setEnabled(status != Status.followup && status != Status.end);
		
		String desc = assessmentMode.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("mode.description", "mode.description",
				desc, 6, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.setEnabled(status != Status.followup && status != Status.end);
		
		String dateAndTime = getDateAndTime();
		uifactory.addStaticTextElement("date.and.time", dateAndTime, formLayout);
		
		String members = getMembers();
		uifactory.addStaticTextElement("mode.target", members, formLayout);
		
		elementKeys = new ArrayList<>();
		StringBuilder elementSb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			CourseEditorTreeModel treeModel = course.getEditorTreeModel();
			for(String element:assessmentMode.getElementList().split(",")) {
				String courseNodeName = getCourseNodeName(element, treeModel);
				if(StringHelper.containsNonWhitespace(courseNodeName)) {
					elementKeys.add(element);
					if(elementSb.length() > 0) elementSb.append(", ");
					elementSb.append(courseNodeName);
				}
			}
		}
		chooseElementsCont = uifactory.addStaticTextElement("chooseElements", "choose.start.element", elementSb.toString(), formLayout);
		chooseElementsCont.setMandatory(true);

		chooseElementsButton = uifactory.addFormLink("choose.elements", formLayout, Link.BUTTON);
		chooseElementsButton.setEnabled(status != Status.end);
		
		boolean useSEB = assessmentMode.isSafeExamBrowser();
		useSafeExamBrowserEl = uifactory.addToggleButton("mode.safeexambrowser", "mode.safeexambrowser", translate("on"), translate("off"), formLayout);
		useSafeExamBrowserEl.addActionListener(FormEvent.ONCHANGE);
		useSafeExamBrowserEl.toggle(useSEB);
		
		safeExamBrowserConfigurationEl = uifactory.addFormLink("show.safeexambrowser.configuration", "show.safeexambrowser.configuration",
				"show.safeexambrowser.configuration.label", formLayout, Link.BUTTON);
		safeExamBrowserConfigurationEl.getComponent().setSuppressDirtyFormWarning(true);
		safeExamBrowserConfigurationEl.setGhost(true);
		boolean showConfiguration = (assessmentMode.getKey() == null && lectureModule.isAssessmentModeSebDefault())
				|| (assessmentMode.getKey() != null && assessmentMode.isSafeExamBrowser() && assessmentMode.getSafeExamBrowserConfiguration() != null);
		safeExamBrowserConfigurationEl.setVisible(showConfiguration);
		
		String key = assessmentMode.getKey() == null
				? ConfigurationHelper.getSebKeys(lectureConfig, lectureModule)
				: assessmentMode.getSafeExamBrowserKey();
		safeExamBrowserKeyEl = uifactory.addTextAreaElement("safeexamkey", "mode.safeexambrowser.key", 4096, 6, 60, false, false, key, formLayout);
		safeExamBrowserKeyEl.setVisible(useSEB && StringHelper.containsNonWhitespace(key));
		safeExamBrowserKeyEl.setEnabled(false);
		
		//ips
		String ipList = assessmentMode.getIpList();
		TextAreaElement ipListEl = uifactory.addTextAreaElement("mode.ips.list", "mode.ips.list", 4096, 4, 60, false, false, ipList, formLayout);
		ipListEl.setVisible(assessmentMode.isRestrictAccessIps());
		ipListEl.setEnabled(false);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		if(status != Status.end) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}
	
	private String getDateAndTime() {
		return new AssessmentModeHelper(getTranslator()).getBeginEndDate(assessmentMode);
	}
	
	private String getMembers() {
		StringBuilder sb = new StringBuilder();
		RepositoryEntry blockEntry = lectureBlock.getEntry();
		List<Group> selectedGroups = lectureService.getLectureBlockToGroups(lectureBlock);
		
		// course
		Group entryBaseGroup = repositoryService.getDefaultGroup(blockEntry);
		if(selectedGroups.contains(entryBaseGroup)) {
			sb.append(translate("mode.target.course", StringHelper.escapeHtml(blockEntry.getDisplayname())));
		}
		
		// business groups
		int numOfBusinessGroups = 0;
		StringBuilder gpString = new StringBuilder();
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> businessGroups = businessGroupService.findBusinessGroups(params, blockEntry, 0, -1, BusinessGroupOrder.nameAsc);
		for(BusinessGroup businessGroup:businessGroups) {
			if(selectedGroups.contains(businessGroup.getBaseGroup())) {
				if(gpString.length() > 0) gpString.append(", ");
				gpString.append(StringHelper.escapeHtml(businessGroup.getName()));
				numOfBusinessGroups++;
			}
		}
		
		if(numOfBusinessGroups > 0) {
			if(sb.length() > 0) sb.append(" ");
			String i18n = numOfBusinessGroups == 1 ? "mode.target.business.group" : "mode.target.business.groups";
			sb.append(translate(i18n, gpString.toString()));
		}
		
		// curriculum elements
		int numOfCurriculums = 0;
		StringBuilder curString = new StringBuilder();
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(blockEntry);
		for(CurriculumElement element:elements) {
			if(selectedGroups.contains(element.getGroup())) {
				if(curString.length() > 0) curString.append(", ");
				curString.append(StringHelper.escapeHtml(element.getDisplayName()));
				numOfCurriculums++;
			}
		}
		
		if(numOfCurriculums > 0) {
			if(sb.length() > 0) sb.append(" ");
			String i18n = numOfCurriculums == 1 ? "mode.target.curriculum.element" : "mode.target.curriculum.elements";
			sb.append(translate(i18n, curString.toString()));
		}
		return sb.toString();
	}
	
	private String getCourseNodeName(String ident, CourseEditorTreeModel treeModel) {
		String name = null;
		CourseNode courseNode = treeModel.getCourseNode(ident);
		if(courseNode != null) {
			name = courseNode.getShortTitle();
		}
		return name;
	}
	
	private void updateUI() {
		boolean useSEB = useSafeExamBrowserEl.isOn();
		boolean useKeys = (assessmentMode.getKey() == null && !lectureModule.isAssessmentModeSebDefault())
				|| (assessmentMode.getKey() != null && StringHelper.containsNonWhitespace(assessmentMode.getSafeExamBrowserKey()));
		safeExamBrowserKeyEl.setVisible(useSEB && useKeys);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(chooseElementsCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				doSetElements(chooseElementsCtrl.getSelectedKeys());
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				save(ureq, true);
			}
		} else if(safeExamBrowserConfigurationCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CLOSE_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(safeExamBrowserConfigurationCtrl);
		removeAsListenerAndDispose(chooseElementsCtrl);
		removeAsListenerAndDispose(cmc);
		safeExamBrowserConfigurationCtrl = null;
		chooseElementsCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
			//too long
		} else {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		chooseElementsCont.clearError();
		if(elementKeys.isEmpty()) {
			chooseElementsCont.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date begin = assessmentMode.getBegin();
		Date end = assessmentMode.getEnd();
		int followupTime = assessmentMode.getFollowupTime();
		int leadTime = assessmentMode.getLeadTime();

		Status currentStatus = assessmentMode.getStatus();

		Status nextStatus = modeCoordinationService.evaluateStatus(begin, leadTime, end, followupTime);
		if(currentStatus == nextStatus) {
			save(ureq, true);
		} else {
			String title = translate("confirm.status.change.title");

			String text = switch (nextStatus) {
				case none -> translate("confirm.status.change.none");
				case leadtime -> translate("confirm.status.change.leadtime");
				case assessment -> translate("confirm.status.change.assessment");
				case followup -> translate("confirm.status.change.followup");
				case end -> translate("confirm.status.change.end");
				default -> "ERROR";
			};
			confirmCtrl = activateOkCancelDialog(ureq, title, text, confirmCtrl);
		}
	}
	
	private void save(UserRequest ureq, boolean forceStatus) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		} else {
			AssessmentMode concurrentAssessmentMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
			if(concurrentAssessmentMode != null) {
				assessmentMode = concurrentAssessmentMode;
			}
		}

		assessmentMode.setName(nameEl.getValue());
		assessmentMode.setDescription(descriptionEl.getValue());
		
		String targetKey = AssessmentMode.Target.courseAndGroups.name();// all in once
		assessmentMode.setTargetAudience(AssessmentMode.Target.valueOf(targetKey));

		boolean elementRestrictions = !elementKeys.isEmpty();
		assessmentMode.setRestrictAccessElements(elementRestrictions);
		if(elementRestrictions) {
			StringBuilder sb = new StringBuilder();
			for(String elementKey:elementKeys) {
				if(sb.length() > 0) sb.append(",");
				sb.append(elementKey);
			}
			assessmentMode.setElementList(sb.toString());
		} else {
			assessmentMode.setElementList(null);
		}
		
		boolean useSEB = useSafeExamBrowserEl.isOn();
		assessmentMode.setSafeExamBrowser(useSEB);
		if(useSEB) {
			if(lectureModule.isAssessmentModeSebDefault()) {
				SafeExamBrowserConfiguration configuration = assessmentModule.getSafeExamBrowserConfigurationDefaultConfiguration();
				assessmentMode.setSafeExamBrowserConfiguration(configuration);
				boolean safeExamBrowserConfigDownload = lectureModule.isAssessmentModeSebDownload();
				assessmentMode.setSafeExamBrowserConfigDownload(safeExamBrowserConfigDownload);
				String safeExamBrowserHint = lectureModule.getAssessmentModeSebHint();
				assessmentMode.setSafeExamBrowserHint(safeExamBrowserHint);
				assessmentMode.setSafeExamBrowserKey(null);
			} else {
				String sebKey = ConfigurationHelper.getSebKeys(lectureConfig, lectureModule);
				assessmentMode.setSafeExamBrowserKey(sebKey);
			}
		} else {
			assessmentMode.setSafeExamBrowserKey(null);
		}

		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}
		
		assessmentModeMgr.syncAssessmentModeToLectureBlock(assessmentMode);
		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus, getIdentity());
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		ChangeAssessmentModeEvent changedEvent = new ChangeAssessmentModeEvent(assessmentMode, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(changedEvent, ASSESSMENT_MODE_ORES);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if(chooseElementsButton == source) {
			doChooseElements(ureq);
		} else if(safeExamBrowserConfigurationEl == source) {
			doShowSafeExamBrowserConfiguration(ureq);
		} else if(useSafeExamBrowserEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(fiSrc != safeExamBrowserConfigurationEl) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doChooseElements(UserRequest ureq) {
		if(guardModalController(chooseElementsCtrl)) return;

		chooseElementsCtrl = new ChooseElementsController(ureq, getWindowControl(), elementKeys, courseOres);
		listenTo(chooseElementsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseElementsCtrl.getInitialComponent(),
				true, translate("popup.chooseelements"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSetElements(List<String> elements) {
		elementKeys = elements;
		
		StringBuilder elementSb = new StringBuilder();
		if(!elementKeys.isEmpty()) {
			ICourse course = CourseFactory.loadCourse(courseOres);
			CourseEditorTreeModel treeModel = course.getEditorTreeModel();
			for(String element:elementKeys) {
				String courseNodeName = getCourseNodeName(element, treeModel);
				if(StringHelper.containsNonWhitespace(courseNodeName)) {
					if(elementSb.length() > 0) elementSb.append(", ");
					elementSb.append(courseNodeName);
				}
			}
		}
		chooseElementsCont.setValue(elementSb.toString());
	}
	
	private void doShowSafeExamBrowserConfiguration(UserRequest ureq) {
		SafeExamBrowserEnabled configuration;
		if(assessmentMode.getKey() == null) {
			configuration = new DefaultSafeExamBrowserConfiguration();
		} else {
			configuration = assessmentMode;
		}
		safeExamBrowserConfigurationCtrl = new SafeExamBrowserConfigurationController(ureq, getWindowControl(), configuration);
		listenTo(safeExamBrowserConfigurationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), safeExamBrowserConfigurationCtrl.getInitialComponent(),
				true, translate("show.safeexambrowser.configuration.label"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private class DefaultSafeExamBrowserConfiguration implements SafeExamBrowserEnabled {

		@Override
		public boolean isSafeExamBrowser() {
			return true;
		}

		@Override
		public void setSafeExamBrowser(boolean safeExamBrowser) {
			//
		}

		@Override
		public String getSafeExamBrowserKey() {
			return null;
		}

		@Override
		public void setSafeExamBrowserKey(String safeExamBrowserKey) {
			//
		}

		@Override
		public SafeExamBrowserConfiguration getSafeExamBrowserConfiguration() {
			return assessmentModule.getSafeExamBrowserConfigurationDefaultConfiguration();
		}

		@Override
		public void setSafeExamBrowserConfiguration(SafeExamBrowserConfiguration configuration) {
			//
		}

		@Override
		public String getSafeExamBrowserConfigPList() {
			return null;
		}

		@Override
		public String getSafeExamBrowserConfigPListKey() {
			return null;
		}

		@Override
		public boolean isSafeExamBrowserConfigDownload() {
			return lectureModule.isAssessmentModeSebDownload();
		}

		@Override
		public void setSafeExamBrowserConfigDownload(boolean safeExamBrowserConfigDownload) {
			// 
		}

		@Override
		public String getSafeExamBrowserHint() {
			return lectureModule.getAssessmentModeSebHint();
		}

		@Override
		public void setSafeExamBrowserHint(String safeExamBrowserHint) {
			//
		}
	}
	
	private static class SafeExamBrowserConfigurationController extends AbstractEditSafeExamBrowserController {
		
		private FormLink closeButton;
		
		public SafeExamBrowserConfigurationController(UserRequest ureq, WindowControl wControl, SafeExamBrowserEnabled configuration) {
			super(ureq, wControl, configuration);
			
			initForm(ureq);
			updateUI();
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			super.initForm(formLayout, listener, ureq);
			
			FormLayoutContainer buttonsWrapperCont = uifactory.addDefaultFormLayout("buttonsWrapper", null, formLayout);
			FormLayoutContainer buttonCont = uifactory.addButtonsFormLayout("buttons", null, buttonsWrapperCont);
			closeButton = uifactory.addFormLink("close", buttonCont, Link.BUTTON);
		}

		@Override
		protected void updateUI() {
			super.updateUI();
			
			safeExamBrowserEl.setVisible(false);
			typeOfUseEl.setVisible(false);
			downloadConfigEl.setEnabled(isEditable());
			safeExamBrowserHintEl.setEnabled(isEditable());
		}

		@Override
		protected boolean isEditable() {
			return false;
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(closeButton == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}