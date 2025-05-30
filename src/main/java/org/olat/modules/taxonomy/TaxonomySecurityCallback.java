/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.taxonomy;

import org.olat.modules.taxonomy.model.FullTaxonomySecurityCallback;

/**
 * 
 * Initial date: May 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface TaxonomySecurityCallback {
	
	static final TaxonomySecurityCallback FULL = new FullTaxonomySecurityCallback();

	void refresh();
	
	boolean canEditTaxonomyMetadata();
	
	boolean canImportExport();
	
	boolean canFilterRelevant();
	
	boolean isRelevant(TaxonomyLevel level);
	
	boolean canCreateChild(TaxonomyLevel level);
	
	boolean canDelete(TaxonomyLevel level);

	boolean canMove(TaxonomyLevel level);
	
	boolean canEditMetadata(TaxonomyLevel level);
	
	boolean canViewLevelTypes();
	
	boolean canViewManagement(TaxonomyLevel level);
	
	boolean canViewCompetences();

	boolean canViewLostFound();

}
