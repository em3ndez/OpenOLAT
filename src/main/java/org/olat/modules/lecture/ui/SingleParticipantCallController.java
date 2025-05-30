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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.immunityproof.ImmunityProofService;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.component.ImmunityProofLevelCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusItem;
import org.olat.user.UserPropertiesInfoController;
import org.olat.user.UserPropertiesInfoController.Builder;
import org.olat.user.UserPropertiesInfoController.LabelValues;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleParticipantCallController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	
	private FormLink closeButton;
	private FormLink selectAllLink;
	private TextElement commentEl;
	private TextElement absenceReasonEl;
	private SingleSelection absenceCategoriesEl;
	private LectureBlockRollCallStatusItem statusEl;
	private MultipleSelectionElement authorizedAbsencedEl;
	private final List<MultipleSelectionElement> checks = new ArrayList<>();
	
	private UserPropertiesInfoController userInfoCtrl;
	
	private AbsenceNotice absenceNotice;
	private final Identity calledIdentity;
	private LectureBlockRollCall rollCall;
	private final LectureBlock lectureBlock;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	private final List<AbsenceCategory> absenceCategories;
	private final SingleParticipantRollCallRow row;
	
	private ImmunityProofLevel proofLevel;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private ImmunityProofService immunityProofService;
	
	public SingleParticipantCallController(UserRequest ureq, WindowControl wControl, LectureBlock lectureBlock,
			Identity calledIdentity) {
		super(ureq, wControl, "call_wizard");
		this.calledIdentity = calledIdentity;
		this.lectureBlock = lectureBlock;
		absenceCategories = lectureService.getAbsencesCategories(null);

		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		rollCall = lectureService.getOrCreateRollCall(calledIdentity, lectureBlock, null, null, null);
		absenceNotice = lectureService.getAbsenceNotice(calledIdentity, lectureBlock);
		row = new SingleParticipantRollCallRow(lectureBlock, absenceNotice, lectureBlock.getCalculatedLecturesNumber(), List.of());
		row.setRollCall(rollCall);
		
		if (immunityProofModule.isEnabled()) {
			ImmunityProof proof = immunityProofService.getImmunityProof(calledIdentity);
			proofLevel = immunityProofService.getImmunityProofLevel(proof);
		}

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int numOfChecks = lectureBlock.getPlannedLecturesNumber();
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			List<Integer> absences = rollCall.getLecturesAbsentList();
			for(int i=0; i<numOfChecks; i++) {
				String checkId = "check_" + i ;
				MultipleSelectionElement check = uifactory.addCheckboxesHorizontal(checkId, null, layoutCont, onKeys, onValues);
				check.setDomReplacementWrapperRequired(false);
				check.addActionListener(FormEvent.ONCHANGE);
				if(absences.contains(i)) {
					check.select(onKeys[0], true);
				}
				checks.add(check);
			}
			
			layoutCont.contextPut("checks", checks);
			
			row.setChecks(checks.toArray(new MultipleSelectionElement[checks.size()]));
			statusEl = new LectureBlockRollCallStatusItem("status",
					row, authorizedAbsenceEnabled, absenceDefaultAuthorized, getTranslator());
			statusEl.setWithExplanation(true);
			
			updateAbsences(ureq);
		}
		
		if(authorizedAbsenceEnabled) {
			authorizedAbsencedEl = uifactory.addCheckboxesHorizontal("authorized.absence", "authorized.absence", formLayout, onKeys, onValues);
			authorizedAbsencedEl.setDomReplacementWrapperRequired(false);
			authorizedAbsencedEl.addActionListener(FormEvent.ONCHANGE);
			if(rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue()) {
				authorizedAbsencedEl.select(onKeys[0], true);
			}
			row.setAuthorizedAbsence(authorizedAbsencedEl);

			AbsenceCategory currentCategory = rollCall.getAbsenceCategory();
			SelectionValues absenceKeyValues = new SelectionValues();
			absenceKeyValues.add(SelectionValues.entry("", ""));
			for(AbsenceCategory absenceCategory: absenceCategories) {
				if(absenceCategory.isEnabled() || absenceCategory.equals(currentCategory)) {
					absenceKeyValues.add(SelectionValues.entry(absenceCategory.getKey().toString(), absenceCategory.getTitle()));
				}
			}
			absenceCategoriesEl = uifactory.addDropdownSingleselect("absence.category", "absence.category", formLayout, absenceKeyValues.keys(), absenceKeyValues.values());
			absenceCategoriesEl.setDomReplacementWrapperRequired(false);
			absenceCategoriesEl.setVisible(absenceKeyValues.size() > 1 && authorizedAbsencedEl.isAtLeastSelected(1));
			absenceCategoriesEl.setMandatory(true);
			if(currentCategory != null) {
				for(AbsenceCategory absenceCategory: absenceCategories) {
					if(absenceCategory.equals(currentCategory)) {
						absenceCategoriesEl.select(absenceCategory.getKey().toString(), true);
					}
				}
			}
			
			String reason = rollCall.getAbsenceReason();
			absenceReasonEl = uifactory.addTextAreaElement("absence.reason", "authorized.absence.reason", 2000, 4, 36, false, false, reason, formLayout);
			absenceReasonEl.setDomReplacementWrapperRequired(false);
			absenceReasonEl.setPlaceholderKey("authorized.absence.reason", null);
			absenceReasonEl.setVisible(authorizedAbsencedEl.isAtLeastSelected(1));
			absenceReasonEl.setMandatory(!absenceDefaultAuthorized);
		}
		
		String comment = rollCall.getComment();
		commentEl = uifactory.addTextAreaElement("comment", "rollcall.comment", 2000, 4, 36, false, false, comment, formLayout);
		commentEl.setPlaceholderKey("rollcall.comment", null);
		
		selectAllLink = uifactory.addFormLink("all", formLayout);
		uifactory.addFormSubmitButton("save", "save.next", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		
		String i18nCloseKey = numOfChecks == 1 ? "close.lecture.block" : "close.lecture.blocks";
		closeButton = uifactory.addFormLink("close", i18nCloseKey, null, formLayout, Link.BUTTON);
	}
	
	private LabelValues getAdditionalLabelValues(UserRequest ureq) {
		Builder lvBuilder = LabelValues.builder();
		if (immunityProofModule.isEnabled() && lectureBlock.isRunningAt(ureq.getRequestTimestamp())) {
			lvBuilder.addBefore(translate("immuno.status"), ImmunityProofLevelCellRenderer.renderImmunityProofLevel(proofLevel, getTranslator()));
		}
		
		StringOutput out = new StringOutput();
		statusEl.getComponent().getHTMLRendererSingleton()
			.render(null, out, statusEl.getComponent(), null, getTranslator(), null, onKeys);
		String status = out.toString();
		lvBuilder.add(translate("rollcall.status.short"), status);
		
		List<Integer> absences = getAbsenceList();
		lvBuilder.add(translate("absences.title"), Integer.toString(absences.size()));
		
		
		return lvBuilder.build();
	}
	
	public AbsenceCategory getAbsenceCategory() {
		if(!absenceCategoriesEl.isVisible() || !absenceCategoriesEl.isOneSelected()) return null;
		
		String selectedKey = absenceCategoriesEl.getSelectedKey();
		if(StringHelper.isLong(selectedKey)) {
			Long categoryKey = Long.valueOf(selectedKey);
			for(AbsenceCategory absenceCategory:absenceCategories) {
				if(absenceCategory.getKey().equals(categoryKey)) {
					return absenceCategory;
				}
			}
		}
		return null;
	}
	
	public List<Integer> getAbsenceList() {
		List<Integer> absenceList = new ArrayList<>();
		for(int i=0; i<checks.size(); i++) {
			MultipleSelectionElement check = checks.get(i);
			if(check.isAtLeastSelected(1)) {
				absenceList.add(i);
			}
		}
		return absenceList;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(absenceReasonEl != null) {
			absenceReasonEl.clearError();
		}
		if(!absenceDefaultAuthorized && authorizedAbsencedEl != null
				&& authorizedAbsencedEl.isAtLeastSelected(1) && absenceReasonEl != null) {
			if(!StringHelper.containsNonWhitespace(absenceReasonEl.getValue())) {
				absenceReasonEl.setErrorKey("error.reason.mandatory");
				allOk &= false;
			}
		}
		
		if(absenceCategoriesEl != null) {
			absenceCategoriesEl.clearError();
			if(absenceCategoriesEl.isVisible()) {
				List<Integer> absenceList = getAbsenceList();
				if((!absenceCategoriesEl.isOneSelected() || absenceCategoriesEl.isSelected(0)) && !absenceList.isEmpty()) {
					absenceCategoriesEl.setErrorKey("error.reason.mandatory");
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectAllLink == source) {
			doSelectAll();
			doCheckAuthorized();
			updateAbsences(ureq);
		} else if(checks.contains(source)) {
			doCheckAuthorized();
			updateAbsences(ureq);
		} else if(authorizedAbsencedEl == source) {
			updateUI();
		} else if(closeButton == source) {
			if(validateFormLogic(ureq)) {
				doSave();
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		
		boolean authorized = authorizedAbsencedEl.isAtLeastSelected(1);
		if(absenceCategoriesEl != null) {
			String[] categoriesKeys = absenceCategoriesEl.getKeys();
			absenceCategoriesEl.setVisible(categoriesKeys != null && categoriesKeys.length > 0 && authorized);
		}
		if(absenceReasonEl != null) {
			absenceReasonEl.setVisible(authorized);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSave() {
		List<Integer> absenceList = getAbsenceList();

		String comment = commentEl.getValue();
		String before = lectureService.toAuditXml(rollCall);
		rollCall = lectureService.addRollCall(calledIdentity, lectureBlock, rollCall, comment, absenceList);
		if(authorizedAbsencedEl != null) {
			rollCall.setAbsenceAuthorized(authorizedAbsencedEl.isAtLeastSelected(1));
			rollCall.setAbsenceReason(absenceReasonEl.getValue());
			rollCall.setAbsenceCategory(getAbsenceCategory());
			rollCall = lectureService.updateRollCall(rollCall);
		}
		lectureService.auditLog(LectureBlockAuditLog.Action.updateRollCall, before, lectureService.toAuditXml(rollCall),
				Integer.toString(rollCall.getLecturesAttendedNumber()),
				lectureBlock, rollCall, lectureBlock.getEntry(), null, calledIdentity, getIdentity());
	}
	
	
	private void doSelectAll() {
		for(MultipleSelectionElement check:checks) {
			check.select(onKeys[0], true);
		}	
	}
	
	private void doCheckAuthorized() {
		if(authorizedAbsenceEnabled) {
			int absences = 0;
			for(MultipleSelectionElement check:checks) {
				if(check.isAtLeastSelected(1)) {
					++absences;
				}
			}
			
			if(absences == 0) {
				authorizedAbsencedEl.uncheckAll();
			} else  if(absenceDefaultAuthorized && (rollCall == null || rollCall.getAbsenceAuthorized() == null)) {
				authorizedAbsencedEl.select(onKeys[0], true);	
			}
		}
	}
	
	private void updateAbsences(UserRequest ureq) {
		removeAsListenerAndDispose(userInfoCtrl);
		
		userInfoCtrl = new UserPropertiesInfoController(ureq, getWindowControl(), mainForm, calledIdentity, null,
				getAdditionalLabelValues(ureq), null);
		listenTo(userInfoCtrl);
		flc.add("userInfo", userInfoCtrl.getInitialFormItem());
	}
}