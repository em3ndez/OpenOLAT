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
package org.olat.modules.portfolio.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.ui.model.BinderRow;

/**
 * 
 * Initial date: 7 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmMoveBinderToTrashController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	
	private MultipleSelectionElement acknowledgeEl;
	
	private BinderRow userObject;
	private final List<BinderStatistics> stats;
	
	public ConfirmMoveBinderToTrashController(UserRequest ureq, WindowControl wControl, BinderStatistics stats) {
		this(ureq, wControl, Collections.singletonList(stats));
	}
	
	public ConfirmMoveBinderToTrashController(UserRequest ureq, WindowControl wControl, List<BinderStatistics> stats) {
		super(ureq, wControl, "confirm_delete_binder");
		this.stats = stats;
		initForm(ureq);
	}
	
	public BinderRow getUserObject() {
		return userObject;
	}

	public void setUserObject(BinderRow userObject) {
		this.userObject = userObject;
	}

	public List<BinderStatistics> getBinderStatistics() {
		return stats;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder names =  new StringBuilder();
		int numOfPages = 0;
		int numOfSections = 0;
		int numOfComments = 0;
		for(BinderStatistics stat:stats) {
			if(names.length() > 0) names.append(", ");
			names.append(StringHelper.escapeHtml(stat.getTitle()));
			numOfPages += stat.getNumOfPages();
			numOfSections += stat.getNumOfSections();
			numOfComments += stat.getNumOfComments();
		}

		String[] args = new String[] {
				names.toString(),
				Integer.toString(numOfSections),
				Integer.toString(numOfPages),
				Integer.toString(numOfComments)
		};
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("msg", translate("delete.binder.warning", args));
			layoutCont.contextPut("dangerCssClass", "o_warning_with_icon");
		}
		
		String[] onValues = new String[]{ translate("trash.binder.acknowledge") };
		acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", "delete.binder.acknowledge", formLayout, onKeys, onValues);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("delete", "delete.binder", formLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
