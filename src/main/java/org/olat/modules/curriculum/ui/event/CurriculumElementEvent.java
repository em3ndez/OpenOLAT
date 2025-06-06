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
package org.olat.modules.curriculum.ui.event;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.ContextEntry;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 23 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementEvent extends Event {

	private static final long serialVersionUID = -4775987956799651344L;

	public static final String SELECT_ELEMENT = "select-curriculum-element";
	
	private final CurriculumElementRef curriculumElement;
	private final List<ContextEntry> entries;
	
	public CurriculumElementEvent(CurriculumElementRef curriculumElement, List<ContextEntry> entries) {
		super(SELECT_ELEMENT);
		this.entries = entries;
		this.curriculumElement = curriculumElement;
	}
	
	public List<ContextEntry> getContext() {
		return entries;
	}
	
	public CurriculumElementRef getCurriculumElement() {
		return curriculumElement;
	}
}
