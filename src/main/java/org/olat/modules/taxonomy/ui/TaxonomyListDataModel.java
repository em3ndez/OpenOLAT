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
package org.olat.modules.taxonomy.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyListDataModel extends DefaultFlexiTableDataModel<TaxonomyRow> implements SortableFlexiTableDataModel<TaxonomyRow> {
	
	private static final TaxonomyCols[] COLS = TaxonomyCols.values();
	private final Locale locale;
	
	public TaxonomyListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override	
	public void sort(SortKey orderBy) {
		List<TaxonomyRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TaxonomyRow category = getObject(row);
		return getValueAt(category, col);
	}

	@Override
	public Object getValueAt(TaxonomyRow taxonomyRow, int col) {
		switch(COLS[col]) {
			case key: return taxonomyRow.getKey();
			case displayName: return taxonomyRow.getDisplayName();
			case identifier: return taxonomyRow.getIdentifier();
			case externalId: return taxonomyRow.getExternalId();
			case creationDate: return taxonomyRow.getCreationDate();
			case numOfLevels: return taxonomyRow.getNumberOfLevels();
		}
		return null;
	}
	
	
	public enum TaxonomyCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.taxonomy.title"),
		identifier("table.header.taxonomy.external.ref"),
		externalId("table.header.taxonomy.external.id"),
		creationDate("created.date"),
		numOfLevels("table.header.taxonomy.level.sublevels");
		
		private final String i18nKey;
		
		private TaxonomyCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
