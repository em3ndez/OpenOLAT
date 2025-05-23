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
package org.olat.user.ui.organisation;

import org.olat.admin.privacy.PrivacyAdminController;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderEmailFilter;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.ui.BillingAddressListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationOverviewController extends BasicController {
	
	private static final FolderControllerConfig LEGAL_FOLDER_CONFIG = FolderControllerConfig.builder()
			.withDisplayWebDAVLinkEnabled(false)
			.withMail(FolderEmailFilter.never)
			.build();
	
	private final Link metadataLink;
	private final Link resourcesLink;
	private final Link userManagementLink;
	private final Link lineManagersLink;
	private final Link educationManagersLink;
	private final Link billingAddressesLink;
	private final Link emailDomainsLink;
	private final Link legalFolderLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private EditOrganisationController metadataCtrl;
	private OrganisationResourceListController resourcesCtrl;
	private OrganisationUserManagementController userMgmtCtrl;
	private OrganisationRoleEditController lineManagerConfigCtrl;
	private OrganisationRoleEditController educationManagerConfigCtrl;
	private BillingAddressListController billingAddressesCtrl;
	private OrganisationEmailDomainAdminController emailDomainsCtrl;
	private FolderController legalFolderCtrl;
	
	private final Organisation organisation;
	
	@Autowired
	private OrganisationModule organisationModul;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private AccessControlModule acModule;

	public OrganisationOverviewController(UserRequest ureq, WindowControl wControl, Organisation organisation) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PrivacyAdminController.class, getLocale(), getTranslator()));

		this.organisation = organisation;

		mainVC = createVelocityContainer("organisation_overview");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		metadataLink = LinkFactory.createLink("organisation.metadata", mainVC, this);
		segmentView.addSegment(metadataLink, true);
		userManagementLink = LinkFactory.createLink("organisation.user.management", mainVC, this);
		segmentView.addSegment(userManagementLink, false);
		resourcesLink = LinkFactory.createLink("organisation.resources", mainVC, this);
		segmentView.addSegment(resourcesLink, false);
		lineManagersLink = LinkFactory.createLink("admin.props.linemanagers", mainVC, this);
		educationManagersLink = LinkFactory.createLink("admin.props.educationmanagers", mainVC, this);
		if (organisation.isDefault() || organisation.getParent() == null) {
			segmentView.addSegment(lineManagersLink, false);
			segmentView.addSegment(educationManagersLink, false);
		}
		billingAddressesLink = LinkFactory.createLink("organisation.billing.addresses", mainVC, this);
		if (acModule.isInvoiceEnabled()) {
			segmentView.addSegment(billingAddressesLink, false);
		}
		emailDomainsLink = LinkFactory.createLink("organisation.email.domains", mainVC, this);
		if (organisationModul.isEmailDomainEnabled()) {
			segmentView.addSegment(emailDomainsLink, false);
		}
		legalFolderLink = LinkFactory.createLink("organisation.legal.folder", mainVC, this);
		if (organisationModul.isLegalFolderEnabled()) {
			segmentView.addSegment(legalFolderLink, false);
		}
		
		putInitialPanel(mainVC);
		doOpenMetadadata(ureq);
	}
	

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(metadataCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			} else if(event == Event.DONE_EVENT) {// done -> data saved
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == metadataLink) {
					doOpenMetadadata(ureq);
				} else if (clickedLink == userManagementLink){
					doOpenUsermanagement(ureq);
				} else if(clickedLink == resourcesLink) {
					doOpenResources(ureq);
				} else if (clickedLink == lineManagersLink) {
					doOpenLineManagers(ureq);
				} else if (clickedLink == educationManagersLink) {
					doOpenEducationManagers(ureq);
				} else if(clickedLink == billingAddressesLink) {
					doOpenBillingAddresses(ureq);
				} else if(clickedLink == emailDomainsLink) {
					doOpenEmailDomains(ureq);
				} else if(clickedLink == legalFolderLink) {
					doOpenLegalFolder(ureq);
				}
			}
		}
	}
	
	private void doOpenMetadadata(UserRequest ureq) {
		if(metadataCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Metadata"), null);
			metadataCtrl = new EditOrganisationController(ureq, bwControl, organisation);
			listenTo(metadataCtrl);
		}

		addToHistory(ureq, metadataCtrl);
		mainVC.put("segmentCmp", metadataCtrl.getInitialComponent());
	}
	
	private void doOpenUsermanagement(UserRequest ureq) {
		if(userMgmtCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Users"), null);
			userMgmtCtrl = new OrganisationUserManagementController(ureq, bwControl, organisation);
			listenTo(userMgmtCtrl);
		}
		
		addToHistory(ureq, userMgmtCtrl);
		mainVC.put("segmentCmp", userMgmtCtrl.getInitialComponent());
	}
	
	private void doOpenResources(UserRequest ureq) {
		if(resourcesCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Resources"), null);
			resourcesCtrl = new OrganisationResourceListController(ureq, bwControl, organisation);
			listenTo(resourcesCtrl);
		}

		addToHistory(ureq, resourcesCtrl);
		mainVC.put("segmentCmp", resourcesCtrl.getInitialComponent());
		
	}

	private void doOpenLineManagers(UserRequest ureq) {
		if (lineManagerConfigCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("LineManagers"), null);
			lineManagerConfigCtrl = new OrganisationRoleEditController(ureq, bwControl, organisation, OrganisationRoles.linemanager);
			listenTo(lineManagerConfigCtrl);
		}

		addToHistory(ureq, lineManagerConfigCtrl);
		mainVC.put("segmentCmp", lineManagerConfigCtrl.getInitialComponent());
	}

	private void doOpenEducationManagers(UserRequest ureq) {
		if (educationManagerConfigCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("EducationManagers"), null);
			educationManagerConfigCtrl = new OrganisationRoleEditController(ureq, bwControl, organisation, OrganisationRoles.educationmanager);
			listenTo(educationManagerConfigCtrl);
		}

		addToHistory(ureq, educationManagerConfigCtrl);
		mainVC.put("segmentCmp", educationManagerConfigCtrl.getInitialComponent());
	}

	private void doOpenBillingAddresses(UserRequest ureq) {
		if (billingAddressesCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("BillingAddresses"), null);
			billingAddressesCtrl = new BillingAddressListController(ureq, bwControl, organisation, null);
			listenTo(billingAddressesCtrl);
		}
		
		addToHistory(ureq, billingAddressesCtrl);
		mainVC.put("segmentCmp", billingAddressesCtrl.getInitialComponent());
	}

	private void doOpenEmailDomains(UserRequest ureq) {
		if (emailDomainsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("EMailDomains"), null);
			emailDomainsCtrl = new OrganisationEmailDomainAdminController(ureq, bwControl, organisation);
			listenTo(emailDomainsCtrl);
		}
		
		addToHistory(ureq, emailDomainsCtrl);
		mainVC.put("segmentCmp", emailDomainsCtrl.getInitialComponent());
	}

	private void doOpenLegalFolder(UserRequest ureq) {
		if (legalFolderCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("LegalFolder"), null);
			VFSContainer organisationContainer = organisationService.getLegalContainer(organisation);
			organisationContainer = new NamedContainerImpl(translate("organisation.legal.folder"), organisationContainer);
			legalFolderCtrl = new FolderController(ureq, bwControl, organisationContainer, LEGAL_FOLDER_CONFIG);
			listenTo(legalFolderCtrl);
		}
		
		addToHistory(ureq, legalFolderCtrl);
		mainVC.put("segmentCmp", legalFolderCtrl.getInitialComponent());
	}
	
}
