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
package org.olat.modules.curriculum.ui.member;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerController;

/**
 * 
 * Initial date: 25 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberHistoryDetailsController extends AbstractHistoryController {

	private EmptyPanelItem emptyHistoryEl;
	
	private final Identity member;
	
	public MemberHistoryDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CurriculumElement element, Identity member) {
		super(ureq, wControl, "member_details_history", rootForm, element);
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale()));
		this.member = member;
		
		initForm(ureq);
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		emptyHistoryEl = uifactory.addEmptyPanel("empty.history", null, formLayout);
		emptyHistoryEl.setElementCssClass("o_sel_empty_history");
		emptyHistoryEl.setTitle(translate("membership.no.history.title"));
		emptyHistoryEl.setIconCssClass("o_icon o_icon-lg o_icon_user");
		emptyHistoryEl.setVisible(false);

		initTable(formLayout, false, false);
		initFilters();
		initFiltersPresets();
	}
	
	@Override
	protected CurriculumElementMembershipHistorySearchParameters getSearchParameters() {
		CurriculumElementMembershipHistorySearchParameters searchParams = new CurriculumElementMembershipHistorySearchParameters();
		searchParams.setElements(List.of(curriculumElement));
		searchParams.setIdentities(List.of(member));
		return searchParams;
	}
	
	@Override
	protected void loadModel(boolean reset) {
		super.loadModel(reset);
		
		boolean empty = tableModel.getRowCount() <= 0;
		emptyHistoryEl.setVisible(empty);
		tableEl.setVisible(!empty);
	}
}
