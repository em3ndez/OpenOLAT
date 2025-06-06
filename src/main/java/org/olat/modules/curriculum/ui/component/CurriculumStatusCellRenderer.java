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
package org.olat.modules.curriculum.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumStatus;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public CurriculumStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof CurriculumStatus status) {
			getStatus(target, "o_labeled_light", status, translator);
		} else if(cellValue instanceof CurriculumElementStatus status) {
			getStatus(target, "o_labeled_light", status, translator);
		}
	}
	
	public static final void getStatus(StringOutput target, String type, Enum<?> status, Translator trans) {
		target.append("<span class=\"").append(type).append(" o_curriculum_status_")
		      .append(status.name()).append("\">")
		      .append("<i class=\"o_icon o_icon-fw o_icon_curriculum_status_").append(status.name()).append("\"> </i> ")
		      .append(trans.translate("status.".concat(status.name())))
		      .append("</span>");
	}
}
