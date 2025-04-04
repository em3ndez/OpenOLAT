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
package org.olat.modules.forms.handler;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.ui.CountTableController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.CountDataSource;
import org.olat.modules.forms.ui.model.EvaluationFormControllerReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.MultipleChoiceDataSource;

/**
 * 
 * Initial date: 21.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceTableHandler implements EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "mctablehandler";
	}

	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl, Form rootForm,
			PageElement element, SessionFilter filter, ReportHelper reportHelper) {
		if (element instanceof MultipleChoice multipleChoice) {
			if (multipleChoice.getChoices().size() > 0) {
				CountDataSource dataSource = new MultipleChoiceDataSource(multipleChoice, filter);
				Controller ctrl = new CountTableController(ureq, windowControl, dataSource);
				return new EvaluationFormControllerReportElement(ctrl);
			}
		}
		return null;
	}

}
