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
package org.olat.modules.coach.ui.component;

import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 14 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchStateEntry implements StateEntry {
	
	private static final long serialVersionUID = -5016094853538563387L;
	
	private String searchString;
	
	public SearchStateEntry() {
		//
	}
	
	public SearchStateEntry(String searchString) {
		this.searchString = searchString;
	}
	
	public String getSearchString() {
		return searchString;
	}

	@Override
	public SearchStateEntry clone() {
		return new SearchStateEntry(searchString);
	}
}
