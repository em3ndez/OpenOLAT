/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.ms;

import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.formEvaluation;
import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.gradeSystem;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.timeline.TimelineBuilder;
import org.olat.core.gui.components.timeline.TimelineController;
import org.olat.core.gui.components.timeline.TimelineModel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.AssessmentDocumentsSupplier;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.PanelInfo;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.properties.LogEntry;
import org.olat.properties.LogFormatter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jun 16, 2004
 * @author gnaegi
 */
public class MSCourseNodeRunController extends BasicController implements Activateable2, AssessmentDocumentsSupplier {

	private final VelocityContainer myContent;
	private final AssessmentParticipantViewController assessmentParticipantViewCtrl;
	private TimelineController timelineCtrl;
	
	private final boolean showLog;
	private final UserCourseEnvironment userCourseEnv;
	private final CourseNode courseNode;
	private final AssessmentConfig assessmentConfig;
	private final AssessmentEvaluation assessmentEval;
	private final EvaluationFormProvider evaluationFormProvider;
	private final PanelInfo panelInfo;
	
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * Constructor for a manual scoring course run controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param userCourseEnv The user course environment
	 * @param msCourseNode An course node
	 * @param displayNodeInfo If true, the node title and learning objectives will be displayed
	 * @param showLog If true, the change log will be displayed
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode, EvaluationFormProvider evaluationFormProvider, boolean displayNodeInfo, boolean showLog) {
		this(ureq, wControl, userCourseEnv, courseNode, evaluationFormProvider, displayNodeInfo, showLog, false);
	}
	
	/**
	 * Constructor for a manual scoring course run controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param userCourseEnv The user course environment
	 * @param courseNode An course element
	 * @param displayNodeInfo If true, the node title and learning objectives will be displayed
	 * @param showLog If true, the change log will be displayed
	 * @param overrideUserResultsVisiblity If the controller can override the user visiblity of the score evaluation
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode, EvaluationFormProvider evaluationFormProvider,
			boolean displayNodeInfo, boolean showLog, boolean overrideUserResultsVisiblity) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		
		this.showLog = showLog;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(userCourseEnv), courseNode);
		this.evaluationFormProvider = evaluationFormProvider;
		this.panelInfo = new PanelInfo(MSCourseNodeRunController.class,
				"::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent());
		
		if (overrideUserResultsVisiblity) {
			assessmentEval = new AssessmentEvaluation(courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv), Boolean.TRUE);
		} else {
			assessmentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		}
		myContent = createVelocityContainer("run");

		assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, wControl, assessmentEval,
				assessmentConfig, this, gradeSystem(userCourseEnv, courseNode),
				formEvaluation(userCourseEnv, courseNode, assessmentConfig), panelInfo, false, false);
		listenTo(assessmentParticipantViewCtrl);
		myContent.put("assessment", assessmentParticipantViewCtrl.getInitialComponent());
		
		if (Mode.none != assessmentConfig.getScoreMode()) {
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, courseNode);
			listenTo(highScoreCtr);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				myContent.put("highScore", highScoreComponent);
			}
		}
		
		//admin setting whether to show change log or not
		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());

		exposeUserDataToVC(ureq);
		putInitialPanel(myContent);
	}

	@Override
	public List<VFSLeaf> getIndividualAssessmentDocuments() {
		return courseAssessmentService.getIndividualAssessmentVFSDocuments(courseNode, userCourseEnv);
	}

	@Override
	public boolean isDownloadEnabled() {
		return true;
	}
	
	/**
	 * @return true if the assessed user has a score
	 */
	public boolean hasScore() {
		return assessmentEval.getScore() != null;
	}
	
	/**
	 * @return true if the assessed user has passed or failed.
	 */
	public boolean hasPassed() {
		return assessmentEval.getPassed() != null;
	}
	
	/**
	 * @return true if the assessed user has a comment
	 */
	public boolean hasComment() {
		return assessmentConfig.hasComment() && StringHelper.containsNonWhitespace(assessmentEval.getComment());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(type.startsWith("path")) {
			if(assessmentParticipantViewCtrl != null) {
				assessmentParticipantViewCtrl.activate(ureq, entries, state);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (timelineCtrl == source && "downloadTimeline".equals(event.getCommand())) {
			doDownloadLog(ureq);
		}
	}

	private void doDownloadLog(UserRequest ureq) {
		String nodeLog = courseAssessmentService.getAuditLog(courseNode, userCourseEnv);
		String fileTitle = courseNode.getShortTitle() + "-" + userCourseEnv.getIdentityEnvironment().getIdentity().getKey();

		StringMediaResource resource = createLogMediaResource(nodeLog, fileTitle);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	public static StringMediaResource createLogMediaResource(String nodeLog, String nodeTitle) {
		StringMediaResource resource = new StringMediaResource();

		resource.setData(nodeLog);
		resource.setDownloadable(true, createLogFilename(nodeTitle));

		resource.setContentType("text/plain");
		resource.setEncoding("UTF-8");

		return resource;
	}

	private static String createLogFilename(String nodeTitle) {
		String sanitizedTitle = nodeTitle.replaceAll("[^a-zA-Z0-9\\-_]", "_");
		return sanitizedTitle + "_log.txt";
	}
	
	private void exposeUserDataToVC(UserRequest ureq) {
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		if(resultsVisible) {
			if (evaluationFormProvider != null && courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED)) {
				MSResultDetailsController detailsCtrl = new MSResultDetailsController(ureq, getWindowControl(), userCourseEnv, courseNode, evaluationFormProvider);
				listenTo(detailsCtrl);
				myContent.put("details", detailsCtrl.getInitialComponent());
			}
			
			if (showLog) {
				UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
				String userLog = am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity());
				if (StringHelper.containsNonWhitespace(userLog)) {
					LogFormatter logFormatter = new LogFormatter();
					List<LogEntry> logEntries = logFormatter.parseLog(userLog).validEntries();
					if(StringHelper.containsNonWhitespace(userLog)) {
						doShowLogs(ureq, logEntries);
					}
				}
			}
		}
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		String infoTextUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		if(StringHelper.containsNonWhitespace(infoTextUser)) {
				myContent.contextPut(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, infoTextUser);
				myContent.contextPut("indisclaimer", isPanelOpen(ureq, "disclaimer", true));
		}
	}

	private void doShowLogs(UserRequest ureq, List<LogEntry> logEntries) {
		List<TimelineModel.TimelineYear> logTimeline = TimelineBuilder.buildLogEntriesTimeline(logEntries, getLocale());

		timelineCtrl = new TimelineController(
				ureq, getWindowControl(), getTranslator(), logTimeline, logTimeline, false, true);
		listenTo(timelineCtrl);
		myContent.put("log", timelineCtrl.getInitialComponent());
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(panelInfo.attributedClass(), getOpenPanelId(panelId));
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(panelInfo.attributedClass(), getOpenPanelId(panelId), Boolean.valueOf(newValue));
		}
		myContent.contextPut("in-" + panelId, Boolean.valueOf(newValue));
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + panelInfo.idSuffix();
	}
	
}
