/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.user;

import java.util.List;

import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.modules.co.ContactFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserInfoProfileController extends BasicController {

	private Link visitingCardLink;
	private Link emailLink;
	private Link chatLink;

	private CloseableModalController cmc;
	private HomePageDisplayController infoCtrl;
	private ContactFormController contactCtrl;
	
	private final PortraitUser portraitUser;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private HomePageConfigManager homePageConfigManager;
	@Autowired
	private InstantMessagingService imService;

	public UserInfoProfileController(UserRequest ureq, WindowControl wControl, UserInfoProfileConfig profileConfig, PortraitUser portraitUser) {
		super(ureq, wControl);
		this.portraitUser = portraitUser;
		
		VelocityContainer mainVC = createVelocityContainer("user_info_profile");
		putInitialPanel(mainVC);
		
		mainVC.contextPut("name", StringHelper.escapeHtml(portraitUser.getDisplayName()));
		
		UserPortraitComponent userPortraitMComp = UserPortraitFactory.createUserPortrait("user.portrait.medium", mainVC, getLocale());
		userPortraitMComp.setPortraitUser(portraitUser);
		userPortraitMComp.setDisplayPresence(profileConfig.isChatEnabled() && !portraitUser.getIdentityKey().equals(getIdentity().getKey()));
		
		UserPortraitComponent userPortraitLComp = UserPortraitFactory.createUserPortrait("user.portrait.large", mainVC, getLocale());
		userPortraitLComp.setSize(PortraitSize.large);
		userPortraitLComp.setPortraitUser(portraitUser);
		userPortraitLComp.setDisplayPresence(profileConfig.isChatEnabled() && !portraitUser.getIdentityKey().equals(getIdentity().getKey()));
		
		if (profileConfig.isShowIdentityStatus()) {
			String statusCss = getStatusCss(portraitUser);
			if (StringHelper.containsNonWhitespace(statusCss)) {
				mainVC.contextPut("statusCss", statusCss);
				
				Translator statusTranslator = Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale());
				String statusLabel = "<span class=\"o_user_status_badge " + statusCss + "\">"
						+ statusTranslator.translate(getStatusI18nKey(portraitUser))
						+ "</span>";
				mainVC.contextPut("statusLabel", statusLabel);
			}
		}
		
		if (portraitUser.getIdentityKey() < 0) {
			return;
		}
		
		visitingCardLink = LinkFactory.createLink("user.info.visiting.card", mainVC, this);
		visitingCardLink.setIconLeftCSS("o_icon o_icon-fw o_icon_visiting_card");
		visitingCardLink.setAriaRole("button");
		
		emailLink = LinkFactory.createLink("user.info.email", mainVC, this);
		emailLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		emailLink.setAriaRole("button");

		if (profileConfig.isChatEnabled() && !portraitUser.getIdentityKey().equals(getIdentity().getKey())) {
			chatLink = LinkFactory.createLink("user.info.chat", mainVC, this);
			chatLink.setIconLeftCSS("o_icon o_icon-fw o_icon_chat");
			chatLink.setAriaRole("button");
		}

		if (profileConfig.isUserManagementLinkEnabled()) {
			List<Organisation> manageableOrganisations = organisationService.getOrganisations(
					getIdentity(), ureq.getUserSession().getRoles(),
					OrganisationRoles.administrator, OrganisationRoles.principal,
					OrganisationRoles.usermanager, OrganisationRoles.rolesmanager);
			if (!manageableOrganisations.isEmpty()) {
				List<Organisation> portraitUserOrganisations = organisationService.getOrganisations(
						portraitUser::getIdentityKey, OrganisationRoles.valuesWithoutGuestAndInvitee());
				if (portraitUserOrganisations.stream().anyMatch(manageableOrganisations::contains)) {
					String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(
							"[UserAdminSite:0][usearch:0][table:0][Identity:" + portraitUser.getIdentityKey() + "]");
					ExternalLink userManagementLink = LinkFactory.createExternalLink("user.info.user.management", "user.info.user.management", url);
					userManagementLink.setCssClass("o_open_org");
					userManagementLink.setName(translate("user.info.user.management"));
					userManagementLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
					mainVC.put("user.info.user.management", userManagementLink);
				}
			}
		}
	}
	
	private String getStatusCss(PortraitUser portraitUser) {
		if (portraitUser != null && portraitUser.getIdentityStatus() != null) {
			if (portraitUser.getIdentityStatus() == Identity.STATUS_INACTIVE) {
				return "o_user_status_inactive";
			} else if (portraitUser.getIdentityStatus() == Identity.STATUS_LOGIN_DENIED) {
				return "o_user_status_login_denied";
			} else if (portraitUser.getIdentityStatus() == Identity.STATUS_PENDING) {
				return "o_user_status_pending";
			} else if (portraitUser.getIdentityStatus() == Identity.STATUS_DELETED) {
				return "o_user_status_deleted";
			} else {
				return "o_user_status_active";
			}
		}
		return null;
	}
	
	private String getStatusI18nKey(PortraitUser portraitUser) {
		if (portraitUser != null && portraitUser.getIdentityStatus() != null) {
			if (portraitUser.getIdentityStatus() == Identity.STATUS_INACTIVE) {
				return "rightsForm.status.inactive";
			} else if (portraitUser.getIdentityStatus() == Identity.STATUS_LOGIN_DENIED) {
				return "rightsForm.status.login_denied";
			} else if (portraitUser.getIdentityStatus() == Identity.STATUS_PENDING) {
				return "rightsForm.status.pending";
			} else if (portraitUser.getIdentityStatus() == Identity.STATUS_DELETED) {
				return "rightsForm.status.deleted";
			} else {
				return "rightsForm.status.activ";
			}
		}
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == contactCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(infoCtrl);
		removeAsListenerAndDispose(cmc);
		contactCtrl = null;
		infoCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == visitingCardLink) {
			doVisitingCard(ureq);
		} else if (source == emailLink) {
			doEmail(ureq);
		} else if (source == chatLink) {
			doChat(ureq);
		}
	}

	private void doVisitingCard(UserRequest ureq) {
		if (guardModalController(infoCtrl)) return;
		
		Identity identity = securityManager.loadIdentityByKey(portraitUser.getIdentityKey());
		if (identity == null) {
			showWarning("error.visiting.card.not.possible");
			return;
		}
		
		HomePageConfig homePageConfig = homePageConfigManager.loadConfigFor(identity);
		infoCtrl = new HomePageDisplayController(ureq, getWindowControl(), identity, homePageConfig);
		listenTo(infoCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), infoCtrl.getInitialComponent(),
				true, translate("user.info.visiting.card"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doEmail(UserRequest ureq) {
		if (guardModalController(contactCtrl)) return;
		
		Identity recipeint = securityManager.loadIdentityByKey(portraitUser.getIdentityKey());
		if (recipeint == null) {
			showWarning("error.email.not.possible");
			return;
		}
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		ContactList emailList = new ContactList(portraitUser.getDisplayName());
		emailList.add(recipeint);
		cmsg.addEmailTo(emailList);
		
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent(),
				true, translate("user.info.email"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doChat(UserRequest ureq) {
		Buddy buddy = imService.getBuddyById(portraitUser.getIdentityKey());
		if (buddy == null) {
			showWarning("error.chat.not.possible");
			return;
		}
		
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}

}
