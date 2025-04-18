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
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.modules.curriculum.ui.member.DualNumber;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.member.ModificationStatusSummary;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UsersOverviewTableModel extends DefaultFlexiTableDataModel<UserRow>
implements SortableFlexiTableDataModel<UserRow> {
	
	private static final UserOverviewCols[] COLS = UserOverviewCols.values();
	
	private final Locale locale;
	
	public UsersOverviewTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<UserRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		UserRow memberRow = getObject(row);
		return getValueAt(memberRow, col);
	}
	
	@Override
	public Object getValueAt(UserRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case modifications -> row.getModificationSummary();
				case role -> row.getRoles();
				case organisation -> row.getOrganisations();
				case billingAddress -> row;
				case numOfModifications -> getNumOfModifications(row);
				case tools -> row.getToolsLink();
				default -> "ERROR";
			};
		}
		
		int propPos = col - AbstractMembersController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private DualNumber getNumOfModifications(UserRow row) {
		List<MembershipModification> modifications = row.getModifications();	
		int numOfModifications = modifications == null ? 0 : modifications.size();
		ModificationStatusSummary modificationSummary = row.getModificationSummary();
		int numOfRelevantModifications = modificationSummary == null ? 0 : modificationSummary.numOfModifications();
		return new DualNumber(numOfRelevantModifications, numOfModifications, false);
	}

	public enum UserOverviewCols implements FlexiSortableColumnDef {
		modifications("table.header.activity"),
		role("table.header.current.roles"),
		organisation("table.header.organisations"),
		billingAddress("table.header.billing.address"),
		numOfModifications("table.header.add.access"),
		tools("action.more");
		
		private final String i18nKey;
		
		private UserOverviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
