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
package org.olat.modules.curriculum.ui.widgets;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 22 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockStartDateCellRenderer implements FlexiCellRenderer {
	
	private final Formatter formatter;
	
	public LectureBlockStartDateCellRenderer(Locale locale) {
		formatter = Formatter.getInstance(locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		Object rowValue = source.getFormItem().getTableDataModel().getObject(row);
		if(rowValue instanceof LectureBlockWidgetRow blockRow && blockRow.getLectureBlock().getStartDate() != null) {
			LectureBlock lectureBlock = blockRow.getLectureBlock();
			target.append("<span class='o_lecture_date'>")
			      .append("<i class='o_icon o_icon-fw o_icon_calendar'> </i>")
			      .append("<span>");
			// Day
			target.append("<span class='o_lecture_day'>")
			      .append(formatter.formatDateWithDay(lectureBlock.getStartDate()))
			      .append("</span>");
			// Hour
			target.append("<span class='o_lecture_time'>")
		          .append(formatter.formatTimeShort(lectureBlock.getStartDate()))
		          .append("</span>");
			// Duration
			if(lectureBlock.getEndDate() != null) {
				String duration = Formatter.formatDurationCompact(lectureBlock.getEndDate().getTime() - lectureBlock.getStartDate().getTime());
				target.append(", <span class='o_lecture_duration'>").append(duration).append("</span>");
			}
			target.append("</span></span>");
		}
	}
}
