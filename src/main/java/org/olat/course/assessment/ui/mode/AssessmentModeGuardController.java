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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LockGuardController;
import org.olat.core.commons.fullWebApp.LockRequestEvent;
import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.countdown.CountDownComponent;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.manager.IpListValidator;
import org.olat.course.assessment.manager.SafeExamBrowserValidator;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeGuardController extends BasicController implements LockGuardController, GenericEventListener {
	
	private final Link mainContinueButton;
	private final ExternalLink mainSEBQuitButton;
	private final VelocityContainer mainVC;
	
	private final String address;
	private final String mapperUri;
	
	private boolean pushUpdate = false;
	private List<TransientAssessmentMode> modes;
	
	private final ResourceGuards guards = new ResourceGuards();
	
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	/**
	 *
	 * @param ureq
	 * @param wControl
	 * @param modes List of assessments
	 * @param forcePush Async popup need forcePush=true
	 */
	public AssessmentModeGuardController(UserRequest ureq, WindowControl wControl, List<TransientAssessmentMode> modes,
			String address, boolean forcePush) {
		super(ureq, wControl);
		this.address = address;
		mapperUri = registerCacheableMapper(ureq, "seb-settings", new SettingsMapper(guards));

		this.modes = modes;
		this.pushUpdate = forcePush;
		
		mainVC = createVelocityContainer("choose_mode");
		mainVC.contextPut("guards", guards);
		mainVC.contextPut("checked", "not-checked");
		
		mainContinueButton = LinkFactory.createCustomLink("continue-main", "continue-main", "continue-main", "current.mode.continue", Link.BUTTON, mainVC, this);
		mainContinueButton.setElementCssClass("o_sel_assessment_continue");
		mainContinueButton.setCustomEnabledLinkCSS("btn btn-primary");
		mainContinueButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		mainContinueButton.setVisible(false);
		mainVC.put("continue-main", mainContinueButton);
		
		mainSEBQuitButton = LinkFactory.createExternalLink("quit-seb-main", translate("current.mode.seb.quit"), "");
		mainSEBQuitButton.setElementCssClass("btn btn-default btn-primary o_sel_assessment_quit");
		mainSEBQuitButton.setName(translate("current.mode.seb.quit"));
		mainSEBQuitButton.setTarget("_self");
		mainSEBQuitButton.setVisible(false);
		mainVC.put("quit-main", mainSEBQuitButton);
		
		syncAssessmentModes(ureq, null);
		
		putInitialPanel(mainVC);
		
		//register for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	

	
	@Override
	public String getModalTitle() {
		return translate("current.mode");
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
        super.doDispose();
	}
	
	@Override
	public boolean updateLockRequests(UserRequest ureq) {
		boolean f;
		if(pushUpdate) {
			syncAssessmentModes(ureq, null);
			f = true;
			pushUpdate = false;
		} else {
			f = false;
		}
		return f;
	}
	
	private void syncAssessmentModes(UserRequest ureq, Boolean useHeaders) {
		List<ResourceGuard> modeWrappers = new ArrayList<>();
		
		String quitUrl = null;
		for(TransientAssessmentMode mode:modes) {
			if(mode != null) {
				ResourceGuard wrapper = syncAssessmentMode(ureq, mode, useHeaders);
				if(wrapper != null) {
					modeWrappers.add(wrapper);
				}
				if(mode.hasLinkToQuitSEB()) {
					quitUrl = mode.getLinkToQuitSEB();
				}
			}
		}
		
		guards.setList(modeWrappers);
		mainContinueButton.setVisible(modeWrappers.isEmpty());
		// See if we can use a quit URL for SEB
		if(modeWrappers.isEmpty()) {
			if(quitUrl == null) {
				quitUrl = getSEBQuitURLFromLastUnlockedResource();
			}
			if(StringHelper.containsNonWhitespace(quitUrl)) {
				mainSEBQuitButton.setUrl(quitUrl);
				mainSEBQuitButton.setVisible(true);
				// prefer the quit URL in SEB
				mainContinueButton.setVisible(false);
			} else {
				mainSEBQuitButton.setVisible(false);
			}
		} else {
			mainSEBQuitButton.setVisible(false);
		}
		mainVC.setDirty(true);
	}
	
	private String getSEBQuitURLFromLastUnlockedResource() {
		LockResourceInfos infos = getWindowControl().getWindowBackOffice().getChiefController().getLastUnlockedResourceInfos();
		if(infos != null && infos.getLockMode() != null && infos.getLockMode().hasLinkToQuitSEB()) {
			return infos.getLockMode().getLinkToQuitSEB();
		}
		return null;
	}
	
	private ResourceGuard syncAssessmentMode(UserRequest ureq, TransientAssessmentMode mode, Boolean useHeaders) {
		Date now = new Date();
		Date beginWithLeadTime = mode.getBeginWithLeadTime();
		Date endWithFollowupTime = mode.getEndWithFollowupTime();
		// Check if the mode must not be guarded anymore
		if(mode.isManual() && ((Status.end.equals(mode.getStatus()) && EndStatus.all.equals(mode.getEndStatus())) || Status.none.equals(mode.getStatus()))) {
			return null;
		}
		// Check if automatic is out of bounds, disadvantage by manual is controlled by status
		Integer extraTime = null;
		if(!mode.isManual()) {
			extraTime = assessmentModeCoordinationService.getDisadvantageCompensationExtensionTime(mode, getIdentity());
			endWithFollowupTime = addExtraTimeToDate(endWithFollowupTime, extraTime);
			if(beginWithLeadTime.after(now) || now.after(endWithFollowupTime)) {
				return null;
			}
		} 
		
		ResourceGuard guard = guards.getGuardFor(mode);
		if(guard == null) {
			guard = createGuard(mode);
		}

		StringBuilder sb = new StringBuilder();
		boolean allowed = true;
		boolean safeExamCheck = false;
		if(mode.getIpList() != null) {
			boolean ipInRange = IpListValidator.isIpAllowed(mode.getIpList(), address);
			if(!ipInRange) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>");
				sb.append(translate("error.ip.range"));
				sb.append("</h4>");
				sb.append(translate("error.ip.range.desc", address));
			}
			allowed &= ipInRange;
		}
		if(StringHelper.containsNonWhitespace(mode.getSafeExamBrowserKey())) {
			safeExamCheck = guard.isSafeExamCheck() || isSafelyAllowed(ureq, mode.getSafeExamBrowserKey(), null, useHeaders);
			if(!safeExamCheck) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>");
				sb.append(translate("error.safe.exam"));
				sb.append("</h4>");
				sb.append(translate("error.safe.exam.desc", assessmentModule.getSafeExamBrowserDownloadUrl()));
			}
			allowed &= safeExamCheck;
		} else if(StringHelper.containsNonWhitespace(mode.getSafeExamBrowserConfigPList())) {
			safeExamCheck = guard.isSafeExamCheck() || isSafelyAllowed(ureq, null, mode.getSafeExamBrowserConfigPListKey(), useHeaders);
			if(!safeExamCheck) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>");
				sb.append(translate("error.safe.exam"));
				sb.append("</h4>");
				sb.append(translate("error.safe.exam.desc", assessmentModule.getSafeExamBrowserDownloadUrl()));
				
				guard.getDownloadSEBButton().setVisible(true);
				guard.getDownloadSEBConfigurationButton().setVisible(mode.isSafeExamBrowserConfigDownload());
			}
			allowed &= safeExamCheck;
		}
		
		guard.getCountDown().setDate(mode.getBegin());

		String state;
		if(allowed) {
			guard.setSafeExamCheck(safeExamCheck);
			Link go = guard.getGo();
			Link cont = guard.getContinue();
			ExternalLink quit = guard.getQuitSEB();
			state = updateButtons(mode, now, extraTime, go, cont, quit);
			if(go.isVisible()) {
				assessmentModeCoordinationService.waitFor(getIdentity(), mode);
			}
		} else {
			state = "error";
		}
		
		guard.sync(state, sb.toString(), mode, getLocale());
		return guard;
	}
	
	private boolean isSafelyAllowed(UserRequest ureq, String safeExamBrowserKeys, String configurationKey, Boolean useHeaders) {
		String safeExamHash = ureq.getParameter("configKey");
		String url = ureq.getParameter("urlForKeyHash");
		String browserExamKey = ureq.getParameter("browserExamKey");
		getLogger().debug("SEB requests parameters - configkey: {}, url: {}, browser exam key: {}", safeExamHash, url, browserExamKey);
		return (useHeaders != null && useHeaders.booleanValue() && SafeExamBrowserValidator.isSafelyAllowed(ureq.getHttpReq(), safeExamBrowserKeys, configurationKey))
				|| (useHeaders != null && !useHeaders.booleanValue() && SafeExamBrowserValidator.isSafelyAllowedJs(safeExamHash, browserExamKey, url, safeExamBrowserKeys, configurationKey));
	}
	
	private static final Date addExtraTimeToDate(Date date, Integer extraTime) {
		if(extraTime == null || extraTime.intValue() <= 0) {
			return date;
		}
		return DateUtils.addSeconds(date, extraTime.intValue());
	}
	
	private String updateButtons(TransientAssessmentMode mode, Date now, Integer extraTime, Link go, Link cont, ExternalLink quit) {
		String state;
		if(mode.isManual()) {
			state = updateButtonsManual(mode, go, cont, quit);
		} else {
			state = updateButtonsAuto(mode, now, extraTime, go, cont, quit);
		}
		return state;
	}
	
	private String updateButtonsManual(TransientAssessmentMode mode, Link go, Link cont, ExternalLink quitSEB) {
		String state;
		if(Status.leadtime == mode.getStatus()) {
			state = Status.leadtime.name();
			go.setEnabled(false);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(Status.assessment == mode.getStatus() || isDisadvantageCompensationExtension(mode)) {
			state = Status.assessment.name();
			go.setEnabled(true);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(Status.followup == mode.getStatus()) {
			state = Status.followup.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(Status.end == mode.getStatus()) {
			state = Status.end.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(true);
			cont.setVisible(true);
			quitSEB.setEnabled(mode.hasLinkToQuitSEB());
			quitSEB.setVisible(mode.hasLinkToQuitSEB());
		} else {
			state = "error";
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		}
		return state;
	}
	
	private boolean isDisadvantageCompensationExtension(TransientAssessmentMode mode) {
		if(mode.getEndStatus() == EndStatus.withoutDisadvantage
				&& (mode.getStatus() == Status.followup || mode.getStatus() == Status.end)) {
			return disadvantageCompensationService.isActiveDisadvantageCompensation(getIdentity(),
					new RepositoryEntryRefImpl(mode.getRepositoryEntryKey()), mode.getElementList());
		}
		return false;
	}
	
	private String updateButtonsAuto(TransientAssessmentMode mode, Date now, Integer extraTime, Link go, Link cont, ExternalLink quitSEB) {
		Date begin = mode.getBegin();
		Date beginWithLeadTime = mode.getBeginWithLeadTime();
		Date end = addExtraTimeToDate(mode.getEnd(), extraTime);
		Date endWithLeadTime = addExtraTimeToDate(mode.getEndWithFollowupTime(), extraTime);
		
		String state;
		if(beginWithLeadTime.compareTo(now) <= 0 && begin.compareTo(now) > 0) {
			state = Status.leadtime.name();
			go.setEnabled(false);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(begin.compareTo(now) <= 0 && end.compareTo(now) > 0) {
			state = Status.assessment.name();
			go.setEnabled(true);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(end.compareTo(now) <= 0 && endWithLeadTime.compareTo(now) > 0) {
			state = Status.followup.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(endWithLeadTime.compareTo(now) <= 0 || Status.end == mode.getStatus()) {
			state = Status.end.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(true);
			cont.setVisible(true);
			quitSEB.setEnabled(mode.hasLinkToQuitSEB());
			quitSEB.setVisible(mode.hasLinkToQuitSEB());
		} else {
			state = "error";
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		}
		return state;
	}
	
	private ResourceGuard createGuard(TransientAssessmentMode mode) {
		String id = Long.toString(CodeHelper.getRAMUniqueID());

		String goId = "go-".concat(id);
		Link goButton = LinkFactory.createCustomLink(goId, goId, "go", "current.mode.start", Link.BUTTON, mainVC, this);
		goButton.setElementCssClass("o_sel_assessment_start");
		goButton.setCustomEnabledLinkCSS("btn btn-primary");
		goButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		
		String continueId = "continue-".concat(id);
		Link continueButton = LinkFactory.createCustomLink(continueId, continueId, "continue", "current.mode.continue", Link.BUTTON, mainVC, this);
		continueButton.setCustomEnabledLinkCSS("btn btn-primary");
		continueButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		
		String quitUrl = mode.getLinkToQuitSEB();
		ExternalLink quitSEBLink = LinkFactory.createExternalLink("download.seb-" + id, translate("current.mode.seb.quit"), quitUrl);
		quitSEBLink.setName(translate("current.mode.seb.quit"));
		quitSEBLink.setTooltip(translate("current.mode.seb.quit"));
		quitSEBLink.setElementCssClass("btn btn-default");
		quitSEBLink.setTarget("_self");

		String setUrl = Settings.createServerURI() + mapperUri + "/" + CodeHelper.getRAMUniqueID() + "/" + mode.getModeKey() + "/" + SafeExamBrowserConfigurationMediaResource.SEB_SETTINGS_FILENAME;
		ExternalLink downloadSEBConfigurationButton = LinkFactory.createExternalLink("download-seb-config-" + id, "download.seb.config", setUrl);
		downloadSEBConfigurationButton.setElementCssClass("btn btn-default");
		downloadSEBConfigurationButton.setName(translate("download.seb.config"));
		downloadSEBConfigurationButton.setTarget("_self");
		downloadSEBConfigurationButton.setVisible(false);
		
		String sebUrl = assessmentModule.getSafeExamBrowserDownloadUrl();
		ExternalLink downloadSEBLink = LinkFactory.createExternalLink("download.seb-" + id, translate("download.seb"), sebUrl);
		downloadSEBLink.setName(translate("download.seb"));
		downloadSEBLink.setTooltip(translate("download.seb"));
		downloadSEBLink.setElementCssClass("btn btn-default");
		
		CountDownComponent countDown = new CountDownComponent("count-" + id, mode.getBegin(), getTranslator());
		countDown.setI18nKey("current.mode.in");
		
		ResourceGuard guard = new ResourceGuard(mode.getModeKey(), goButton, continueButton, quitSEBLink, downloadSEBLink, downloadSEBConfigurationButton, countDown);
		mainVC.put(goButton.getDispatchID(), goButton);
		mainVC.put(continueButton.getDispatchID(), continueButton);
		mainVC.put(countDown.getDispatchID(), countDown);
		mainVC.put(downloadSEBConfigurationButton.getDispatchID(), downloadSEBConfigurationButton);
		mainVC.put(downloadSEBLink.getDispatchID(), downloadSEBLink);
		
		goButton.setUserObject(guard);
		continueButton.setUserObject(guard);
		return guard;
	}

	@Override
	public void event(Event event) {
		 if (event instanceof AssessmentModeNotificationEvent notificationEvent) {
			try {
				processAssessmentModeNotificationEvent(notificationEvent);
			} catch (Exception e) {
				logError("", e);
			}
		}
	}
	
	private void processAssessmentModeNotificationEvent(AssessmentModeNotificationEvent event) {
		if(getIdentity() != null && event.getAssessedIdentityKeys() != null
				&& event.getAssessementMode() instanceof TransientAssessmentMode mode
				&& event.getAssessedIdentityKeys().contains(getIdentity().getKey())) {

			boolean update = false;
			List<TransientAssessmentMode> updatedModes = new ArrayList<>();
			for(TransientAssessmentMode currentMode:modes) {
				if(currentMode.getRequestKey().equals(mode.getRequestKey())) {
					updatedModes.add(mode);
					update |= (currentMode.getStatus() != mode.getStatus());
					
				} else {
					updatedModes.add(currentMode);
					update |= true;
				}
			}
			modes = updatedModes;
			pushUpdate |= update;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link) {
			String cmd = link.getCommand();
			if("go".equals(cmd)) {
				ResourceGuard guard = (ResourceGuard)link.getUserObject();
				launchAssessmentMode(ureq, guard.getReference());
			} else if("continue".equals(cmd) || "continue-main".equals(cmd)) {
				ResourceGuard guard = (ResourceGuard)link.getUserObject();
				continueAfterAssessmentMode(ureq, guard);
			}
		} else if(source == mainVC) {
			if("checkSEBKeys".equals(event.getCommand())) {
				syncAssessmentModes(ureq, Boolean.FALSE);
				mainVC.contextPut("checked", "checked");
			} else if("checkSEBHeaders".equals(event.getCommand())) {
				syncAssessmentModes(ureq, Boolean.TRUE);
				mainVC.contextPut("checked", "checked");
			}
		}
	}
	
	private void continueAfterAssessmentMode(UserRequest ureq, ResourceGuard selectedGuard) {
		List<ResourceGuard> lastGuards = new ArrayList<>();
		for(ResourceGuard currentGuard:guards.getList()) {
			if(currentGuard != selectedGuard) {
				lastGuards.add(currentGuard);
			}
		}
		guards.setList(lastGuards);
		
		boolean canContinue = guards.getSize() == 0;
		if(canContinue) {
			//make sure to see the navigation bar
			ChiefController cc = Windows.getWindows(ureq).getChiefController(ureq);
			cc.getScreenMode().setMode(Mode.standard, null);

			Long modeKey = selectedGuard == null ? null : selectedGuard.getModeKey();
			fireEvent(ureq, new ContinueEvent(modeKey));
			String businessPath = "[MyCoursesSite:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else {
			mainVC.setDirty(true);
		}
	}
	
	/**
	 * Remove the list of assessment modes and lock the chief controller.
	 * 
	 * 
	 * @param ureq
	 * @param mode
	 */
	private void launchAssessmentMode(UserRequest ureq, TransientAssessmentMode mode) {
		ureq.getUserSession().setLockRequests(null);
		OLATResourceable resource = mode.getResource();
		ureq.getUserSession().setLockResource(resource, mode);
		getWindowControl().getWindowBackOffice().getChiefController().lockResource(resource);
		fireEvent(ureq, new LockRequestEvent(LockRequestEvent.CHOOSE_ASSESSMENT_MODE, mode));
		
		String businessPath = "[RepositoryEntry:" + mode.getRepositoryEntryKey() + "]";
		if(StringHelper.containsNonWhitespace(mode.getStartElementKey())) {
			businessPath += "[CourseNode:" + mode.getStartElementKey() + "]";
		}
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());

		assessmentModeCoordinationService.start(getIdentity(), mode);
	}
	
	public static final class ResourceGuard {

		private String status;
		private String errors;
		private final Link goButton;
		private final Link continueButton;
		private final ExternalLink quitSEBButton;
		private final ExternalLink downloadSEBButton;
		private final ExternalLink downloadSEBConfigurationButton;
		
		private final Long modeKey;
		private String name;
		private String displayName;
		private String description;
		private String safeExamBrowserHint;
		private String safeExamBrowserConfigPList;
		
		private String begin;
		private String end;
		private String leadTime;
		private String followupTime;
		
		private boolean safeExamCheck = false;
		
		private TransientAssessmentMode reference;
		
		private CountDownComponent countDown;
		
		public ResourceGuard(Long modeKey, Link goButton, Link continueButton, ExternalLink quitSebButton,
				ExternalLink downloadSEBButton, ExternalLink downloadSEBConfigurationButton, CountDownComponent countDown) {
			this.modeKey = modeKey;
			this.goButton = goButton;
			this.countDown = countDown;
			this.quitSEBButton = quitSebButton;
			this.continueButton = continueButton;
			this.downloadSEBButton = downloadSEBButton;
			this.downloadSEBConfigurationButton = downloadSEBConfigurationButton;
		}
		
		public void sync(String newStatus, String newErrors, TransientAssessmentMode mode, Locale locale) {
			errors = newErrors;
			status = newStatus;
			
			reference = mode;
			name = mode.getName();
			displayName = mode.getDisplayName();
			description = mode.getDescription();
			safeExamBrowserHint = mode.getSafeExamBrowserHint();
			safeExamBrowserConfigPList = mode.getSafeExamBrowserConfigPList();
			
			Formatter f = Formatter.getInstance(locale);
			begin = f.formatDateAndTime(mode.getBegin());
			end = f.formatDateAndTime(mode.getEnd());
			
			if(mode.getFollowupTime() > 0) {
				followupTime = Integer.toString(mode.getFollowupTime());
			} else {
				followupTime = null;
			}
			
			if(mode.getLeadTime() > 0) {
				leadTime = Integer.toString(mode.getLeadTime());
			} else {
				leadTime = null;
			}
		}
		
		public Long getModeKey() {
			return modeKey;
		}
		
		public TransientAssessmentMode getReference() {
			return reference;
		}

		public String getName() {
			return name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getSafeExamBrowserHint() {
			return safeExamBrowserHint;
		}
		
		public String getSafeExamBrowserConfigPList() {
			return safeExamBrowserConfigPList;
		}

		public String getDisplayName() {
			return displayName;
		}
		
		public String getBegin() {
			return begin;
		}
		
		public String getEnd() {
			return end;
		}
		
		public String getLeadTime() {
			return leadTime;
		}
		
		public String getFollowupTime() {
			return followupTime;
		}
		
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public boolean isSafeExamCheck() {
			return safeExamCheck;
		}

		public void setSafeExamCheck(boolean safeExamCheck) {
			this.safeExamCheck = safeExamCheck;
		}

		public String getErrors() {
			return errors;
		}

		public void setErrors(String errors) {
			this.errors = errors;
		}

		public Link getGo() {
			return goButton;
		}
		
		public Link getContinue() {
			return continueButton;
		}
		
		public ExternalLink getQuitSEB() {
			return quitSEBButton;
		}
		
		public ExternalLink getDownloadSEBButton() {
			return downloadSEBButton;
		}

		public ExternalLink getDownloadSEBConfigurationButton() {
			return downloadSEBConfigurationButton;
		}

		public CountDownComponent getCountDown() {
			return countDown;
		}
	}
	
	public static class SettingsMapper implements Mapper {
		
		private final ResourceGuards guards;
		
		public SettingsMapper(ResourceGuards guards) {
			this.guards = guards;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath.endsWith(SafeExamBrowserConfigurationMediaResource.SEB_SETTINGS_FILENAME)) {
				int index = relPath.indexOf(SafeExamBrowserConfigurationMediaResource.SEB_SETTINGS_FILENAME) - 1;
				if(index > 0) {
					String guardKey = relPath.substring(0, index);
					int lastIndex = guardKey.lastIndexOf('/');
					if(lastIndex >= 0 && guardKey.length() > lastIndex + 1) {
						guardKey = guardKey.substring(lastIndex + 1);
						if(StringHelper.isLong(guardKey)) {
							for(ResourceGuard guard:guards.getList()) {
								if(guardKey.equals(guard.getModeKey().toString())) {
									return mediaResource(guard);
								}							
							}
						}
					}
				}
			}
			
			if(!guards.getList().isEmpty()) {
				return mediaResource(guards.getList().get(0));
			}
			return new NotFoundMediaResource();
		}
		
		private MediaResource mediaResource(ResourceGuard guard) {
			AssessmentMode assessmentMode = CoreSpringFactory.getImpl(AssessmentModeManager.class).getAssessmentModeById(guard.getModeKey());
			if(assessmentMode == null) {
				return new NotFoundMediaResource();
			}
			return new SafeExamBrowserConfigurationMediaResource(assessmentMode.getSafeExamBrowserConfigPList());
		}
	}
	
	public static class ResourceGuards {
		
		private List<ResourceGuard> guards = new ArrayList<>();
		
		public int getSize() {
			return guards.size();
		}
		
		public ResourceGuard getGuardFor(TransientAssessmentMode mode) {
			ResourceGuard guard = null;
			for(ResourceGuard g:getList()) {
				if(g.getModeKey().equals(mode.getModeKey())) {
					guard = g;
				}
			}
			return guard;
		}

		public List<ResourceGuard> getList() {
			return guards;
		}

		public void setList(List<ResourceGuard> guards) {
			this.guards = guards;
		}
	}
}