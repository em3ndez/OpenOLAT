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
package org.olat.modules.curriculum.model;

import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 29 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record CurriculumElementInfos(CurriculumElement curriculumElement, Curriculum curriculum,
		long numOfResources, long numOfTemplates, long numOfLectureBlocks, long numOfLectureBlocksWithEntry,
		long numOfParticipants, long numOfCoaches, long numOfOwners,
		long numOfCurriculumElementOwners, long numOfMasterChoaches, long numOfPending)
implements CurriculumElementRef {
	
	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}
}
