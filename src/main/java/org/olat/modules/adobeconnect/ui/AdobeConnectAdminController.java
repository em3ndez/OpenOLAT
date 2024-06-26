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
package org.olat.modules.adobeconnect.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectAdminController extends BasicController implements Activateable2 {
	
	private final Link meetingsLink;
	private final Link configurationLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private AdobeConnectConfigurationController configController;
	private AdobeConnectAdminMeetingsController meetingsController;
	
	public AdobeConnectAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("adobeconnect_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("adobeconnect.account.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		
		meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
		segmentView.addSegment(meetingsLink, false);
		
		doOpenConfiguration(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Configuration".equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if("Meetings".equalsIgnoreCase(type)) {
			doOpenMeetings(ureq);
			segmentView.select(meetingsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == meetingsLink){
					doOpenMeetings(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configController == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Configuration", 0l), null);
			configController = new AdobeConnectConfigurationController(ureq, bwControl);
			listenTo(configController);
		} else {
			addToHistory(ureq, configController);
		}
		mainVC.put("segmentCmp", configController.getInitialComponent());
	}
	
	private void doOpenMeetings(UserRequest ureq) {
		if(meetingsController == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meetings", 0l), null);
			meetingsController = new AdobeConnectAdminMeetingsController(ureq, bwControl);
			listenTo(meetingsController);
		} else {
			addToHistory(ureq, meetingsController);
		}
		mainVC.put("segmentCmp", meetingsController.getInitialComponent());
	}
}
