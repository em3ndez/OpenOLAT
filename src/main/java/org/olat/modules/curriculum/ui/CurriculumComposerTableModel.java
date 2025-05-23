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
package org.olat.modules.curriculum.ui;

import static org.olat.modules.curriculum.ui.CurriculumComposerController.CMD_SELECT_CURRICULUM;
import static org.olat.modules.curriculum.ui.CurriculumListManagerController.SUB_PATH_OVERVIEW;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.ui.component.MinMaxParticipants;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerTableModel extends DefaultFlexiTreeTableDataModel<CurriculumElementRow>
implements FlexiBusinessPathModel, SortableFlexiTableDataModel<CurriculumElementRow> {

	private static final ElementCols[] COLS = ElementCols.values();
	
	private final boolean flat;
	private final Locale locale;
	
	public CurriculumComposerTableModel(FlexiTableColumnModel columnModel, boolean flat, Locale locale) {
		super(columnModel);
		this.flat = flat;
		this.locale = locale;
		setObjects(new ArrayList<>());
	}
	
	@Override
	public int getTotalNodesCount() {
		if(flat) {
			return super.getRowCount();
		}
		return super.getTotalNodesCount();
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumElementRow> views = new CurriculumComposerTableSortDelegate(orderBy, this, locale).sort();
			setFilteredObjects(views);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty() && filters.get(0) != null)) {	
			List<Long> typesKeys = List.of();
			List<Long> curriculumsKeys = List.of();
			List<String> occupancyValues = List.of();
			List<CurriculumElementStatus> status = List.of();
			boolean withOffersOnly = false;
			Long searchLong = StringHelper.isLong(searchString) ? Long.valueOf(searchString) : null;
			searchString = StringHelper.containsNonWhitespace(searchString) ? searchString.toLowerCase() : null;
			Date begin = null;
			Date end = null;
			
			FlexiTableFilter curriculumFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_CURRICULUM);
			if (curriculumFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					curriculumsKeys = filterValues.stream()
							.map(Long::valueOf).toList();
				}
			}
			
			FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_STATUS);
			if (statusFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					status = filterValues.stream()
							.map(CurriculumElementStatus::valueOf).toList();
				}
			}
			
			FlexiTableFilter typeFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_TYPE);
			if (typeFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					typesKeys = filterValues.stream()
							.map(Long::valueOf).toList();
				}
			}
			
			FlexiTableFilter offerFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_OFFER);
			if (offerFilter instanceof FlexiTableOneClickSelectionFilter extendedFilter) {
				withOffersOnly = extendedFilter.isSelected();
			}
			
			FlexiTableFilter pFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_PERIOD);
			if (pFilter instanceof FlexiTableDateRangeFilter dateRangeFilter) {
				FlexiTableDateRangeFilter.DateRange dateRange = dateRangeFilter.getDateRange();
				if(dateRange != null) {
					begin = dateRange.getStart();
					end = dateRange.getEnd();
				}
			}
			
			FlexiTableFilter occupancyFilter = FlexiTableFilter.getFilter(filters, CurriculumComposerController.FILTER_OCCUPANCY_STATUS);
			if (occupancyFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				occupancyValues = extendedFilter.getValues();
			}
			
			List<CurriculumElementRow> filteredRows = new ArrayList<>(backupRows.size());
			for(CurriculumElementRow row:backupRows) {
				boolean accept = (accept(row, searchLong) || accept(row, searchString))
						&& acceptCurriculum(row, curriculumsKeys)
						&& acceptStatus(row, status)
						&& acceptTypes(row, typesKeys)
						&& acceptWithOffers(row, withOffersOnly)
						&& acceptOccupancyStatus(row, occupancyValues)
						&& acceptDateRange(row, begin, end);
				row.setAcceptedByFilter(accept);
				if(accept) {
					filteredRows.add(row);
				}
			}
			
			if(filteredRows.size() < backupRows.size()) {
				reconstructParentLine(filteredRows);
			}
			setFilteredObjects(filteredRows);
		} else {
			for(CurriculumElementRow row:backupRows) {
				row.setAcceptedByFilter(true);
			}
			setUnfilteredObjects();
		}
	}

	private void reconstructParentLine(List<CurriculumElementRow> rows) {
		Set<CurriculumElementRow> rowSet = new HashSet<>(rows);
		for(int i=0; i<rows.size(); i++) {
			CurriculumElementRow row = rows.get(i);
			for(CurriculumElementRow parent=row.getParent(); parent != null && !rowSet.contains(parent); parent=parent.getParent()) {
				rows.add(i, parent);
				rowSet.add(parent);	
			}
		}
	}
	
	private boolean accept(CurriculumElementRow row, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) {
			return true;
		}
		return ((row.getDisplayName() != null && row.getDisplayName().toLowerCase().contains(searchString))
				|| (row.getIdentifier() != null && row.getIdentifier().toLowerCase().contains(searchString))
				|| (row.getExternalId() != null && row.getExternalId().toLowerCase().contains(searchString)));
	}

	private boolean accept(CurriculumElementRow row, Long searchLong) {
		return searchLong != null && searchLong.equals(row.getKey());
	}
	
	private boolean acceptCurriculum(CurriculumElementRow row, List<Long> curriculumsKeys) {
		return curriculumsKeys.isEmpty()
				|| (row.getCurriculumKey() != null && curriculumsKeys.contains(row.getCurriculumKey()));
	}
	
	private boolean acceptStatus(CurriculumElementRow row, List<CurriculumElementStatus> statusList) {
		return statusList.isEmpty()
				|| (row.getStatus() != null && statusList.contains(row.getStatus()));
	}
	
	private boolean acceptTypes(CurriculumElementRow row, List<Long> elementTypesList) {
		return elementTypesList.isEmpty()
				|| (row.getCurriculumElementTypeKey() != null && elementTypesList.contains(row.getCurriculumElementTypeKey()));
	}
	
	private boolean acceptWithOffers(CurriculumElementRow row, boolean withOffersOnly) {
		if (withOffersOnly) {
			return row.getAccessPriceMethods() != null && !row.getAccessPriceMethods().isEmpty();
		}
		return true;
	}
	
	private boolean acceptOccupancyStatus(CurriculumElementRow row, List<String> statusList) {
		if(statusList == null || statusList.isEmpty()) return true;
		
		for(String status:statusList) {
			if(acceptOccupancyStatus(row, status)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean acceptDateRange(CurriculumElementRow row, Date begin, Date end) {
		if(begin == null && end == null) return true;
		
		if(begin != null && end != null) {
			return row.getBeginDate() != null && begin.compareTo(row.getBeginDate()) <= 0
					&& row.getEndDate() != null && end.compareTo(row.getEndDate()) >= 0;
		}
		if(begin != null) {
			return row.getBeginDate() != null && begin.compareTo(row.getBeginDate()) <= 0;
		}
		if( end != null) {
			return row.getEndDate() != null && end.compareTo(row.getEndDate()) >= 0;
		}
		return false;
	}
	
	private boolean acceptOccupancyStatus(CurriculumElementRow row, String status) {
		MinMaxParticipants minMax = row.getMinMaxParticipants();
		long seats = row.getNumOfPending() + row.getNumOfParticipants();
		if(CurriculumComposerController.FILTER_OCCUPANCY_STATUS_NOT_SPECIFIED.equalsIgnoreCase(status)) {
			return minMax.min() == null && minMax.max() == null;
		}
		if(CurriculumComposerController.FILTER_OCCUPANCY_STATUS_NOT_REACHED.equalsIgnoreCase(status)) {
			return minMax.min() != null && seats < minMax.min().longValue();
		}
		if(CurriculumComposerController.FILTER_OCCUPANCY_STATUS_MIN_REACHED.equalsIgnoreCase(status)) {
			return minMax.min() != null && seats >= minMax.min().longValue()
					&& (minMax.max() == null || seats < minMax.max().longValue());
		}
		if(CurriculumComposerController.FILTER_OCCUPANCY_STATUS_FREE_SEATS.equalsIgnoreCase(status)) {
			return minMax.max() != null && seats < minMax.max().longValue();
		}
		if(CurriculumComposerController.FILTER_OCCUPANCY_STATUS_FULLY_BOOKED.equalsIgnoreCase(status)) {
			return minMax.max() != null && seats == minMax.max().longValue();
		}
		if(CurriculumComposerController.FILTER_OCCUPANCY_STATUS_OVERBOOKED.equalsIgnoreCase(status)) {
			return minMax.max() != null && seats > minMax.max().longValue();
		}
		return false;
	}
	
	public CurriculumElementRow getCurriculumElementRowByKey(Long elementKey) {
		List<CurriculumElementRow> rows = new ArrayList<>(backupRows);
		for(CurriculumElementRow row:rows) {
			if(elementKey.equals(row.getKey())) {
				return row;
			}
		}
		return null;
	}

	@Override
	public String getUrl(Component source, Object object, String action) {
		if(object instanceof CurriculumElementRow row && row.getBaseUrl() != null) {
			String baseUrl = row.getBaseUrl();
			return switch(action) {
				case "select" -> baseUrl.concat(SUB_PATH_OVERVIEW);
				case "owners" -> baseUrl.concat("/Members/0/Owners/0");
				case "coaches" -> baseUrl.concat("/Members/0/Coaches/0");
				case "participants" -> baseUrl.concat("/Members/0/Participants/0");
				case CMD_SELECT_CURRICULUM -> row.getCurriculumUrl();
				default -> baseUrl;
			};
		}
		return null;
	}
	
	@Override
	public boolean hasOpenCloseAll() {
		return !flat;
	}

	@Override
	public boolean hasChildren(int row) {
		CurriculumElementRow element = getObject(row);
		return element.hasChildren();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementRow element = getObject(row);
		return getValueAt(element, col);
	}
	
	@Override
	public Object getValueAt(CurriculumElementRow element, int col) {
		return switch(COLS[col]) {
			case key -> element.getKey();
			case numbering -> element.getNumber();
			case displayName -> element.getDisplayName();
			case externalRef -> element.getIdentifier();
			case externalId -> element.getExternalId();
			case curriculum -> getCurriculum(element);
			case beginDate -> element.getBeginDate();
			case endDate -> element.getEndDate();
			case type -> element.getCurriculumElementTypeDisplayName();
			case resources -> element.getResources();
			case structure -> element.getStructureLink();
			case status -> element.getStatus();
			case tools -> element.getTools();
			case numOfMembers -> element.getNumOfMembers();
			case numOfParticipants -> element.getNumOfParticipants();
			case numOfCoaches -> element.getNumOfCoaches();
			case numOfOwners -> element.getNumOfOwners();
			case numOfCurriculumElementOwners -> element.getNumOfCurriculumElementOwners();
			case numOfMasterCoaches -> element.getNumOfMasterCoaches();
			case numOfPending -> element.getNumOfPending();
			case offers -> element;
			case calendars -> element.getCalendarsLink();
			case lectures -> element.getLecturesLink();
			case qualityPreview -> element.getQualityPreviewLink();
			case learningProgress -> element.getLearningProgressLink();
			case minMaxParticipants -> element.getMinMaxParticipants();
			case availability -> element.getParticipantsAvailabilityNum();
			default -> "ERROR";
		};
	}
	
	private String getCurriculum(CurriculumElementRow element) {
		if(StringHelper.containsNonWhitespace(element.getCurriculumExternalRef())) {
			return element.getCurriculumExternalRef();
		}
		return element.getCurriculumDisplayName();
	}
	
	public enum ElementCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		numbering("table.header.numbering"),
		displayName("table.header.title"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id"),
		curriculum("table.header.curriculum"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		type("table.header.type"),
		resources("table.header.resources"),
		numOfMembers("table.header.num.of.members"),
		numOfParticipants("table.header.num.of.participants"),
		numOfCoaches("table.header.num.of.coaches"),
		numOfOwners("table.header.num.of.owners"),
		numOfCurriculumElementOwners("table.header.num.of.curriculumelementowners"),
		numOfMasterCoaches("table.header.num.of.mastercoaches"),
		numOfPending("table.header.num.pending"),
		calendars("table.header.calendars"),
		lectures("table.header.lectures"),
		structure("table.header.structure"),
		qualityPreview("table.header.quality.preview"),
		learningProgress("table.header.learning.progress"),
		status("table.header.status"),
		offers("table.header.offers"),
		tools("table.header.tools"),
		minMaxParticipants("table.header.minmax.participants"),
		availability("table.header.availability");
		
		private final String i18nKey;
		
		private ElementCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != offers && this != tools && this != structure;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
