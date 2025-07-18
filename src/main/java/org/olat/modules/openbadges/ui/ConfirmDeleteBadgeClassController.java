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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.openbadges.BadgeClass;

/**
 * Initial date: 2025-06-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ConfirmDeleteBadgeClassController extends FormBasicController {

	public enum Mode {
		unused, revoke, remove
	}

	private final BadgeClass badgeClass;
	private final long totalUseCount;
	private MultipleSelectionElement confirmationEl;
	private Mode mode;
	private SingleSelection modeEl;
	
	public ConfirmDeleteBadgeClassController(UserRequest ureq, WindowControl wControl, BadgeClass badgeClass, long totalUseCount) {
		super(ureq, wControl);
		this.badgeClass = badgeClass;
		this.totalUseCount = totalUseCount;
		mode = totalUseCount == 0 ? Mode.unused : null;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues confirmationKV = new SelectionValues();
		String name = badgeClass.getNameWithScan();
		confirmationKV.add(SelectionValues.entry("confirm", translate("confirm.delete.permanently", name)));
		confirmationEl = uifactory.addCheckboxesHorizontal("confirmation", formLayout, confirmationKV.keys(), confirmationKV.values());
		confirmationEl.setMandatory(true);
		
		if (totalUseCount > 0) {
			uifactory.addStaticTextElement("use", null, 
					translate("confirm.delete.decision", Long.toString(totalUseCount)), formLayout);
		}

		if (mode == null) {
			SelectionValues modeKV = new SelectionValues();
			modeKV.add(SelectionValues.entry(Mode.revoke.name(), translate("confirm.revoke.awarded.badges.title"),
					translate("confirm.revoke.awarded.badges.text"), "o_icon o_icon_history", null, true));
			modeKV.add(SelectionValues.entry(Mode.remove.name(), translate("confirm.remove.awarded.badges.title"),
					translate("confirm.remove.awarded.badges.text"), "o_icon o_icon_trash", null, true));

			modeEl = uifactory.addCardSingleSelectHorizontal("confirm.delete.mode", formLayout, modeKV.keys(),
					modeKV.values(), modeKV.descriptions(), modeKV.icons());
			modeEl.setMandatory(true);
		}

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);

		FormSubmit deleteButton = uifactory.addFormSubmitButton("delete", buttonLayout);
		deleteButton.setElementCssClass("btn btn-default btn-danger");
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		confirmationEl.clearError();
		if (!confirmationEl.isAtLeastSelected(1)) {
			confirmationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		if (modeEl != null) {
			modeEl.clearError();
			if (!modeEl.isOneSelected()) {
				modeEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk; 
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (modeEl != null) {
			mode = Mode.valueOf(modeEl.getSelectedKey());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public Mode getMode() {
		return mode;
	}

	public BadgeClass getBadgeClass() {
		return badgeClass;
	}
}
