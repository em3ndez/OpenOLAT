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
package org.olat.modules.catalog.launcher;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 8, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PopularCoursesHandlerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private PopularCoursesHandler sut;
	
	@Test
	public void shouldLoadStats() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsAuthor(JunitTestHelper.miniRandom());
		RepositoryEntry repositoryEntry = JunitTestHelper.createRandomRepositoryEntry(identity);
		dbInstance.commitAndCloseSession();
		
		// Just a syntax check
		sut.getMostPopularRepositoryEntryKeys(List.of(repositoryEntry.getKey()));
	}


}
