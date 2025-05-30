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
package org.olat.modules.curriculum.ui.component;

import java.util.Comparator;

import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 3 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumRolesComparator implements Comparator<CurriculumRoles> {

	@Override
	public int compare(CurriculumRoles o1, CurriculumRoles o2) {
		int c;
		if(o1 == null && o2 == null) {
			c = 0;
		} else if(o1 == null) {
			c = -1;
		} else if(o2 == null) {
			c = 1;
		} else {
			c = -o1.compareTo(o2);
		}
		return c;
	}
}
