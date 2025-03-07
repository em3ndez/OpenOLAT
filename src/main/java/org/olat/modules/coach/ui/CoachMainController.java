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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.grading.model.GradingSecurity;

/**
 * 
 * Initial date: 6 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoachMainController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel toolbarPanel;
	
	private final CoachMainRootController rootCtrl;
	
	public CoachMainController(UserRequest ureq, WindowControl wControl,
			CoachingSecurity coachingSec, GradingSecurity gradingSec) {
		super(ureq, wControl);
		
		toolbarPanel = new TooledStackedPanel("categoriesStackPanel", getTranslator(), this);
		toolbarPanel.setShowCloseLink(false, false);
		toolbarPanel.setInvisibleCrumb(-1);
		toolbarPanel.setToolbarAutoEnabled(true);
		putInitialPanel(toolbarPanel);
		
		rootCtrl = new CoachMainRootController(ureq, getWindowControl(), toolbarPanel,
				coachingSec, gradingSec);
		listenTo(rootCtrl);
		toolbarPanel.pushController(translate("coaching.overview"), rootCtrl);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		rootCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
