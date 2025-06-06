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
package org.olat.user.ui.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 23 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserRoleHistoryTableModel extends DefaultFlexiTableDataModel<UserRoleHistoryRow>
implements SortableFlexiTableDataModel<UserRoleHistoryRow>, FilterableFlexiTableModel {
	
	private static final UserRoleHistoryCols[] COLS = UserRoleHistoryCols.values();

	private final Locale locale;
	private List<UserRoleHistoryRow> backups;
	
	public UserRoleHistoryTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<UserRoleHistoryRow> sort = new SortableFlexiTableModelDelegate<>(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty() && filters.get(0) != null)) {
			DateRange range = getFrom(filters);
			List<OrganisationRoles> roles = getRoles(filters);
			List<Long> organisations = getOrganisations(filters);
			searchString = searchString.toLowerCase();
			
			List<UserRoleHistoryRow> filteredRows = new ArrayList<>(backups.size());
			for(UserRoleHistoryRow row:backups) {
				boolean accept = accept(row, searchString)
						&& acceptDateRange(row, range)
						&& acceptRoles(row, roles)
						&& acceptOrganisations(row, organisations);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean accept(UserRoleHistoryRow row, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		return row.getUserDisplayName() != null && row.getUserDisplayName().toLowerCase().contains(searchString);
	}
	
	private boolean acceptDateRange(UserRoleHistoryRow row, DateRange range) {
		if(range == null) {
			return true;
		}
		Date date = row.getDate();
		return (range.getStart() == null || range.getStart().compareTo(date) <= 0)
				&& (range.getEnd() == null || range.getEnd().compareTo(date) >= 0);
	}
	
	private DateRange getFrom(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, UserRoleHistoryController.FILTER_DATE);
		if(filter instanceof FlexiTableDateRangeFilter extendedFilter) {
			DateRange range = extendedFilter.getDateRange();
			if(range != null && (range.getStart() != null || range.getEnd() != null)) {
				return range;
			}
		}
		return null;
	}
	
	private boolean acceptRoles(UserRoleHistoryRow row, List<OrganisationRoles> roles) {
		if(roles == null || roles.isEmpty()) {
			return true;
		}
		OrganisationRoles role = row.getRole();
		return role != null && roles.contains(role);
	}
	
	private List<OrganisationRoles> getRoles(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, UserRoleHistoryController.FILTER_ROLE);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> values = extendedFilter.getValues();
			if(values != null && !values.isEmpty()) {
				return values.stream()
						.filter(OrganisationRoles::isValue)
						.map(OrganisationRoles::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	private boolean acceptOrganisations(UserRoleHistoryRow row, List<Long> organisations) {
		if(organisations == null || organisations.isEmpty()) {
			return true;
		}
		Long key = row.getOrganisationKey();
		return key != null && organisations.contains(key);
	}
	
	private List<Long> getOrganisations(List<FlexiTableFilter> filters) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, UserRoleHistoryController.FILTER_ORGANISATION);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> values = extendedFilter.getValues();
			if(values != null && !values.isEmpty()) {
				return values.stream()
						.filter(StringHelper::isLong)
						.map(Long::valueOf)
						.toList();
			}
		}
		return List.of();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		UserRoleHistoryRow detailsRow = getObject(row);
		return getValueAt(detailsRow, col);
	}

	@Override
	public Object getValueAt(UserRoleHistoryRow historyRow, int col) {
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case key -> historyRow.getHistoryKey();
				case creationDate -> historyRow.getDate();
				case member -> historyRow.getUserDisplayName();
				case organisation -> historyRow.getOrganisationName();
				case organisationPath -> historyRow.getOrganisationPath();
				case role -> historyRow.getRole();
				case roleInheritance -> historyRow.isInherited() ? GroupMembershipInheritance.inherited : GroupMembershipInheritance.root;
				case activity -> historyRow.getActivity();
				case previousStatus -> historyRow.getPreviousStatus();
				case status -> historyRow.getStatus();
				case note -> historyRow.getNoteLink();
				case actor -> historyRow.getActorDisplayName();
				default -> "ERROR";
			};
		}
		
		return "ERROR";
	}
	
	@Override
	public void setObjects(List<UserRoleHistoryRow> objects) {
		backups = new ArrayList<>(objects);
		super.setObjects(objects);
	}
	
	public enum UserRoleHistoryCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		creationDate("table.header.date"),
		member("table.header.member"),
		role("table.header.role"),
		roleInheritance("table.header.inheritance"),
		organisation("table.header.organisation"),
		organisationPath("table.header.organisation.path"),
		activity("table.header.activity"),
		previousStatus("table.header.original.value"),
		status("table.header.new.value"),
		note("table.header.note"),
		actor("table.header.actor");
		
		private final String i18nKey;
		
		private UserRoleHistoryCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this == key || this == creationDate;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
