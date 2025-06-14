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
package org.olat.course.nodes.ms;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeOverviewController;
import org.olat.course.assessment.ui.tool.AssessmentEventToState;
import org.olat.course.nodes.CourseNodeSegmentPrefs;
import org.olat.course.nodes.CourseNodeSegmentPrefs.CourseNodeSegment;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.CourseNodeBadgesController;
import org.olat.modules.reminder.ReminderModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSCoachRunController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_OVERVIEW = "Overview";
	private static final String ORES_TYPE_PARTICIPANTS = "Participants";
	private static final String ORES_TYPE_REMINDERS = "Reminders";
	private static final String ORES_TYPE_BADGES = "Badges";

	private Link overviewLink;
	private Link participantsLink;
	private Link remindersLink;
	private Link badgesLink;

	private final VelocityContainer mainVC;
	private final CourseNodeSegmentPrefs segmentPrefs;
	private final SegmentViewComponent segmentView;

	private final AssessmentCourseNodeOverviewController overviewCtrl;
	private final AssessmentEventToState assessmentEventToState;
	private final TooledStackedPanel participantsPanel;
	private final AssessmentCourseNodeController participantsCtrl;
	private CourseNodeReminderRunController remindersCtrl;
	private CourseNodeBadgesController badgesCtrl;

	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	public MSCoachRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			MSCourseNode courseNode) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		
		segmentPrefs = new CourseNodeSegmentPrefs(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_OVERVIEW), null);
		overviewCtrl = courseAssessmentService.getCourseNodeOverviewController(ureq, swControl, courseNode, userCourseEnv, false, false, false);
		listenTo(overviewCtrl);
		assessmentEventToState = new AssessmentEventToState(overviewCtrl);
		
		overviewLink = LinkFactory.createLink("segment.overview", mainVC, this);
		segmentView.addSegment(overviewLink, true);
		
		participantsPanel = new TooledStackedPanel("participantsPanel", getTranslator(), this);
		participantsPanel.setToolbarAutoEnabled(false);
		participantsPanel.setToolbarEnabled(false);
		participantsPanel.setShowCloseLink(true, false);
		participantsPanel.setCssClass("o_segment_toolbar o_block_top");
		
		swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_PARTICIPANTS), null);
		participantsCtrl = courseAssessmentService.getCourseNodeRunController(ureq, swControl, participantsPanel, 
				courseNode, userCourseEnv);
		listenTo(participantsCtrl);
		participantsCtrl.activate(ureq, null, null);
		participantsPanel.pushController(translate("segment.participants"), participantsCtrl);
		
		participantsLink = LinkFactory.createLink("segment.participants", mainVC, this);
		segmentView.addSegment(participantsLink, false);

		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		// Reminders
		if (reminderModule.isEnabled() && userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_REMINDERS), null);
			remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, courseEntry, courseNode.getReminderProvider(courseEntry, false));
			listenTo(remindersCtrl);
			if (remindersCtrl.hasDataOrActions()) {
				remindersLink = LinkFactory.createLink("segment.reminders", mainVC, this);
				segmentView.addSegment(remindersLink, false);
			}
		}

		// Badges
		if (openBadgesManager.showBadgesRunSegment(courseEntry, courseNode, userCourseEnv)) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_BADGES), null);
			badgesCtrl = new CourseNodeBadgesController(ureq, swControl, courseEntry, courseNode);
			listenTo(badgesCtrl);

			badgesLink = LinkFactory.createLink("segment.badges", mainVC, this);
			segmentView.addSegment(badgesLink, false);
		}

		doOpenPreferredSegment(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		participantsCtrl.activate(ureq, entries, state);
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_OVERVIEW.equalsIgnoreCase(type)) {
			doOpenOverview(ureq, true);
		} else if(ORES_TYPE_PARTICIPANTS.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenParticipants(ureq, true).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if(ORES_TYPE_REMINDERS.equalsIgnoreCase(type)) {
			doOpenReminders(ureq, true);
		} else if(ORES_TYPE_BADGES.equalsIgnoreCase(type) && badgesLink != null) {
			doOpenBadges(ureq, true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == overviewLink) {
					doOpenOverview(ureq, true);
				} else if (clickedLink == participantsLink) {
					doOpenParticipants(ureq, true);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq, true);
				} else if (clickedLink == badgesLink) {
					doOpenBadges(ureq, true);
				}
			}
		} 
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (assessmentEventToState.handlesEvent(source, event)) {
			doOpenParticipants(ureq, true).activate(ureq, null, assessmentEventToState.getState(event));
		}
		super.event(ureq, source, event);
	}
	
	private void doOpenPreferredSegment(UserRequest ureq) {
		CourseNodeSegment segment = segmentPrefs.getSegment(ureq);
		if (CourseNodeSegment.overview == segment && overviewLink != null) {
			doOpenOverview(ureq, false);
		} else if (CourseNodeSegment.participants == segment && participantsLink != null) {
			doOpenParticipants(ureq, false);
		} else if (CourseNodeSegment.reminders == segment && remindersLink != null) {
			doOpenReminders(ureq, false);
		} else if (CourseNodeSegment.badges == segment && badgesLink != null) {
			doOpenBadges(ureq, false);
		} else {
			doOpenOverview(ureq, false);
		}
	}

	private void doOpenOverview(UserRequest ureq, boolean saveSegmentPref) {
		overviewCtrl.reload();
		mainVC.put("segmentCmp", overviewCtrl.getInitialComponent());
		segmentView.select(overviewLink);
		segmentPrefs.setSegment(ureq, CourseNodeSegment.overview, segmentView, saveSegmentPref);
	}
	
	private Activateable2 doOpenParticipants(UserRequest ureq, boolean saveSegmentPref) {
		participantsCtrl.reload(ureq);
		addToHistory(ureq, participantsCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", participantsPanel);
			segmentView.select(participantsLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.participants, segmentView, saveSegmentPref);
		}
		return participantsCtrl;
	}
	
	private void doOpenReminders(UserRequest ureq, boolean saveSegmentPref) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
			segmentView.select(remindersLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.reminders, segmentView, saveSegmentPref);
		}
	}

	private void doOpenBadges(UserRequest ureq, boolean saveSegmentPref) {
		if (badgesLink != null) {
			mainVC.put("segmentCmp", badgesCtrl.getInitialComponent());
			segmentView.select(badgesLink);
			segmentPrefs.setSegment(ureq, CourseNodeSegment.badges, segmentView, saveSegmentPref);
		}
	}
}
