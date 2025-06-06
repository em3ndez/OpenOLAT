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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.AssessmentModeStatistics;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.AssessmentModeHelper;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.ChangeAssessmentModeEvent;
import org.olat.course.assessment.ui.mode.ModeStatusCellRenderer;
import org.olat.course.assessment.ui.mode.TimeCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentModeOverviewListTableModel.ModeCols;
import org.olat.course.assessment.ui.tool.component.AssessmentModeProgressionItem;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Small list of the assessment planed today and in the future for the
 * coaches.
 * 
 * Initial date: 15 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentModeOverviewListController extends FormBasicController implements FlexiTableComponentDelegate, GenericEventListener {

	private static final OLATResourceable ASSESSMENT_MODE_ORES = OresHelper.createOLATResourceableType(AssessmentMode.class);
	
	private FlexiTableElement tableEl;
	private AssessmentModeOverviewListTableModel model;

	private CloseableModalController cmc;
	private DialogBoxController startDialogBox;
	private ConfirmStopAssessmentModeController stopCtrl;
	
	private int count = 0;
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;

	@Autowired
	private LectureService lectureService;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public AssessmentModeOverviewListController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "assessment_modes", Util.createPackageTranslator(AssessmentModeListController.class, ureq.getLocale()));
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		initForm(ureq);
		loadModel();
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), ASSESSMENT_MODE_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	public int getNumOfAssessmentModes() {
		return model == null ? 0 : model.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.status, new ModeStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.followupTime, new TimeCellRenderer(getTranslator())));

		model = new AssessmentModeOverviewListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 10, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		VelocityContainer row = createVelocityContainer("assessment_mode_overview_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		AssessmentModeHelper helper = new AssessmentModeHelper(getTranslator());
		row.contextPut("helper", helper);
		tableEl.setRowRenderer(row, this);
		tableEl.setRendererType(FlexiTableRendererType.custom);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>();
		if(rowObject instanceof AssessmentModeOverviewRow mode) {
			if(mode.getActionButton() != null) {
				cmps.add(mode.getActionButton().getComponent());
			}
			List<FormLink> elementLinks = mode.getElementLinks();
			for(FormLink elementLink:elementLinks) {
				cmps.add(elementLink.getComponent());
			}
			if(mode.getWaitBarItem() != null) {
				cmps.add(mode.getWaitBarItem().getComponent());
			}
		}
		return cmps;
	}

	public void loadModel() {
		synchronized(model) { 
			Date today = CalendarUtils.removeTime(new Date());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 5);
			Date until = CalendarUtils.endOfDay(cal.getTime());
			
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setLectureConfiguredRepositoryEntry(true);
			searchParams.setStartDate(today);
			searchParams.setEndDate(until);
			
			List<LectureBlock> lectures = lectureService.getLectureBlocks(courseEntry, getIdentity());
			Set<Long> taughtLectures = lectures.stream().map(LectureBlock::getKey).collect(Collectors.toSet());

			SearchAssessmentModeParams searchAssessmentModeParams = new SearchAssessmentModeParams();
			List<Status> allModeStatus = new ArrayList<>(Arrays.stream(Status.class.getEnumConstants()).toList());
			// exclude modes with status equals end
			allModeStatus.removeIf(s -> s.equals(Status.end));
			List<String> allModeStatusAsString = allModeStatus.stream().map(Enum::name).toList();
			searchAssessmentModeParams.setAllowedModeStatus(allModeStatusAsString);
			searchAssessmentModeParams.setRepositoryEntryKey(courseEntry.getKey());

			List<AssessmentMode> modes = assessmentModeManager.findAssessmentMode(searchAssessmentModeParams);
			List<AssessmentModeOverviewRow> rows = new ArrayList<>();
			for(AssessmentMode mode:modes) {
				rows.add(forgeRow(mode, today, taughtLectures));
			}

			// sort entries by status primarily and then by desired dates (OO-7499)
			// elements are already pre-ordered by mode.beginWithLeadTime (in query)
			rows.sort(new Comparator<>() {
				@Override
				public int compare(AssessmentModeOverviewRow o1, AssessmentModeOverviewRow o2) {
					// Compare by status
					int statusComparison = Integer.compare(getAssignedValue(o1.getAssessmentMode().getStatus()), getAssignedValue(o2.getAssessmentMode().getStatus()));

					// If status are different, return the comparison result based on status
					if (statusComparison != 0) {
						return statusComparison;
					} else {
						// If status are the same, compare by date
						if (o1.getAssessmentMode().getStatus().equals(Status.leadtime)
								|| o1.getAssessmentMode().getStatus().equals(Status.none)) {
							return compareByBeginDateAsc(o1.getAssessmentMode(), o2.getAssessmentMode());
						} else if (o1.getAssessmentMode().getStatus().equals(Status.assessment)) {
							return compareByDateStatusAssessment(o1.getAssessmentMode(), o2.getAssessmentMode());
						} else if (o1.getAssessmentMode().getStatus().equals(Status.followup)) {
							return compareByDateStatusFollowup(o1.getAssessmentMode(), o2.getAssessmentMode());
						} else {
							return compareByDateStatusEnd(o1.getAssessmentMode(), o2.getAssessmentMode());
						}
					}
				}

				private static int getAssignedValue(Status mode) {
					return switch (mode) {
						case leadtime -> 0;
						case assessment -> 1;
						case followup -> 2;
						case none -> 3;
						case end -> 4;
					};
				}

				// Status Preparation time & Scheduled: Sort by start asc
				private static int compareByBeginDateAsc(AssessmentMode mode1, AssessmentMode mode2) {
					return mode1.getBegin().compareTo(mode2.getBegin());
				}

				// Status In progress: Sort by end asc
				private static int compareByDateStatusAssessment(AssessmentMode mode1, AssessmentMode mode2) {
					return mode1.getEnd().compareTo(mode2.getEnd());
				}

				// Status Follow-up: Sort by end + follow-up asc
				private static int compareByDateStatusFollowup(AssessmentMode mode1, AssessmentMode mode2) {
					return mode1.getEndWithFollowupTime().compareTo(mode2.getEndWithFollowupTime());
				}

				// Status End: Sort by end desc
				private static int compareByDateStatusEnd(AssessmentMode mode1, AssessmentMode mode2) {
					return mode2.getEnd().compareTo(mode1.getEnd());
				}
			});

			model.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
	
	private AssessmentModeOverviewRow forgeRow(AssessmentMode mode, Date today, Set<Long> teachedLectures) {
		long startTime = CalendarUtils.removeTime(mode.getBegin()).getTime();
		long endTime = CalendarUtils.endOfDay(mode.getEnd()).getTime();
		long todayTime = today.getTime();
		boolean isToday = startTime <= todayTime && endTime >= todayTime;

		Calendar cal = Calendar.getInstance();
		long now = cal.getTimeInMillis();
		cal.setTime(mode.getEnd());
		long endInMillseconds = cal.getTimeInMillis() - now;
		boolean endSoon = (endInMillseconds < (5L * 60L * 1000L))
				&& (mode.getStatus() == Status.assessment || mode.getStatus() == Status.followup);

		String assessmentModeRendered = getAssessmentModeRendered(mode);
		AssessmentModeOverviewRow row = new AssessmentModeOverviewRow(mode, isToday, endSoon, endInMillseconds, assessmentModeRendered);
		
		LectureBlock block = mode.getLectureBlock();
		boolean allowToStartStop = assessmentCallback.canStartStopAllAssessments()
				|| (block != null && teachedLectures.contains(block.getKey()));

		if(mode.isManualBeginEnd() && allowToStartStop) {
			if(assessmentModeCoordinationService.canStart(mode)) {
				String id = "start_" + (++count);
				FormLink startButton = uifactory.addFormLink(id, "start", "assessment.tool.start", null, flc, Link.BUTTON_SMALL);
				startButton.setDomReplacementWrapperRequired(false);
				startButton.setIconLeftCSS("o_icon o_icon-fw o_icon_status_in_progress");
				startButton.setUserObject(row);
				startButton.setPrimary(true);
				flc.add(id, startButton);
				forgeStatistics(mode, row);
				row.setActionButton(startButton);
			} else if(assessmentModeCoordinationService.canStop(mode)) {
				String id = "end_" + (++count);
				FormLink endButton = uifactory.addFormLink(id, "end", "assessment.tool.stop", null, flc, Link.BUTTON_SMALL);
				endButton.setDomReplacementWrapperRequired(false);
				endButton.setIconLeftCSS("o_icon o_icon-fw o_as_mode_stop");
				if(assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode)) {
					endButton.setIconRightCSS("o_icon o_icon-fw o_icon_disadvantage_compensation");
				}
				endButton.setUserObject(row);
				endButton.setPrimary(true);
				flc.add(id, endButton);
				forgeStatistics(mode, row);
				row.setActionButton(endButton);
			}
		} else if (mode.getStatus() == AssessmentMode.Status.leadtime
				|| mode.getStatus() == AssessmentMode.Status.assessment
				|| mode.getStatus() == AssessmentMode.Status.followup) {
			forgeStatistics(mode, row);
		} else {
			row.setWaitBarItem(null);
		}

		
		String elements = mode.getElementList();
		if(StringHelper.containsNonWhitespace(elements)) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			for(String element:elements.split("[,]")) {
				CourseNode node = course.getRunStructure().getNode(element);
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, node);
				if(assessmentConfig.isAssessable() && !(node instanceof STCourseNode) && !(node instanceof PortfolioCourseNode)) {
					String id = "element_" + (++count);
					FormLink elementButton = uifactory.addFormLink(id, "element", node.getShortTitle(), null, flc, Link.LINK | Link.NONTRANSLATED);
					elementButton.setDomReplacementWrapperRequired(false);
					CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance()
							.getCourseNodeConfigurationEvenForDisabledBB(node.getType());
					elementButton.setIconLeftCSS("o_icon ".concat(cnConfig.getIconCSSClass()));
					elementButton.setUserObject(node);
					flc.add(id, elementButton);
					row.addElementLink(elementButton);
				}
			}
		}

		return row;
	}

	public String getAssessmentModeRendered(AssessmentMode mode) {
		StringOutput status = new StringOutput();
		ModeStatusCellRenderer modeStatusCellRenderer = new ModeStatusCellRenderer(Util.createPackageTranslator(AssessmentModeListController.class, getLocale()));
		modeStatusCellRenderer.renderStatus(mode.getStatus(), mode.getEndStatus(), status);
		return status.toString();
	}
	
	private void forgeStatistics(AssessmentMode mode, AssessmentModeOverviewRow row) {
		AssessmentModeStatistics statistics = assessmentModeCoordinationService.getStatistics(mode);
		if(statistics != null) {
			statistics.setStatus(mode.getStatus());// direct from the database
			
			String id = "wait_" + (++count);
			AssessmentModeProgressionItem waitBarItem = new AssessmentModeProgressionItem(id, mode, getTranslator());
			waitBarItem.setMax(statistics.getNumPlanned());
			waitBarItem.setActual(statistics.getNumInOpenOlat());
			row.setWaitBarItem(waitBarItem);
			flc.add(waitBarItem);
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, ASSESSMENT_MODE_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}

	@Override
	public void event(Event event) {
		if(event instanceof ChangeAssessmentModeEvent came) {
			processChangeAssessmentModeEvents(came);
		} else if(event instanceof AssessmentModeNotificationEvent amne) {
			processChangeAssessmentModeEvents(amne);
		}
	}
	
	private void processChangeAssessmentModeEvents(ChangeAssessmentModeEvent event) {
		try {
			List<AssessmentModeOverviewRow> rows = model.getObjects();
			for(AssessmentModeOverviewRow row:rows) {
				if(event.getAssessmentModeKey().equals(row.getAssessmentMode().getKey())) {
					loadModel();
				}	
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void processChangeAssessmentModeEvents(AssessmentModeNotificationEvent event) {
		try {
			LockRequest request = event.getAssessementMode();
			Long entryKey = request.getRepositoryEntryKey();
			if(courseEntry.getKey().equals(entryKey)
					&& request instanceof TransientAssessmentMode assessmentMode) {
				List<AssessmentModeOverviewRow> rows = model.getObjects();
				for(AssessmentModeOverviewRow row:rows) {
					if(assessmentMode.getModeKey().equals(row.getAssessmentMode().getKey())
							&& (!Objects.equals(assessmentMode.getStatus(), row.getAssessmentMode().getStatus())
									|| !Objects.equals(assessmentMode.getEndStatus(), row.getAssessmentMode().getEndStatus()))) {
						loadModel();
					}	
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(startDialogBox == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doStart((AssessmentModeOverviewRow)startDialogBox.getUserObject());
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
		} else if(stopCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(stopCtrl);
		removeAsListenerAndDispose(cmc);
		stopCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			if("start".equals(link.getCmd())) {
				doConfirmStart(ureq, (AssessmentModeOverviewRow)link.getUserObject());
			} else if("end".equals(link.getCmd())) {
				doConfirmStop(ureq, (AssessmentModeOverviewRow)link.getUserObject());
			} else if("element".equals(link.getCmd())) {
				doJumpTo(ureq, (CourseNode)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmStart(UserRequest ureq, AssessmentModeOverviewRow mode) {
		String title = translate("confirm.start.title");
		String text = translate("confirm.start.text");
		startDialogBox = activateYesNoDialog(ureq, title, text, startDialogBox);
		startDialogBox.setUserObject(mode);
	}

	private void doStart(AssessmentModeOverviewRow row) {
		AssessmentMode mode = assessmentModeManager.getAssessmentModeById(row.getAssessmentMode().getKey());
		if(mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
		} else {
			assessmentModeCoordinationService.startAssessment(mode, getIdentity());
			getLogger().info(Tracing.M_AUDIT, "Start assessment mode : {} ({}) in course: {} ({})",
					mode.getName(), mode.getKey(), courseEntry.getDisplayname(), courseEntry.getKey());
		}
		loadModel();
	}
	
	private void doConfirmStop(UserRequest ureq, AssessmentModeOverviewRow row) {
		if(guardModalController(stopCtrl)) return;

		AssessmentMode mode = assessmentModeManager.getAssessmentModeById(row.getAssessmentMode().getKey());
		if(mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
			loadModel();
		} else {
			stopCtrl = new ConfirmStopAssessmentModeController(ureq, getWindowControl(), mode);
			listenTo(stopCtrl);
			
			String title = translate("confirm.stop.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), stopCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void doJumpTo(UserRequest ureq, CourseNode node) {
		fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, node.getIdent()));
	}
}
