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
package org.olat.modules.library.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.LibraryModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LibraryAdminController extends FormBasicController {
	
	private FormToggle enableEl;
	private FormToggle uploadEnableEl;
	private FormToggle uploadApproveEnableEl;
	private TextElement mailAfterUploadEl;
	private TextElement mailAfterFreeingEl;
	private FormLink addSharedFolderButton;
	private FormLink removeSharedFolderButton;
	private StaticTextElement sharedFolderNameEl;
	private FormLayoutContainer sharedFolderCont;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController chooseFolderCtr;
	
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public LibraryAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateUI();
		initSharedFolder();
	}
	
	private void initSharedFolder() {
		String entryKey = libraryModule.getLibraryEntryKey();
		if(StringHelper.isLong(entryKey)) {
			RepositoryEntry libraryEntry = repositoryService.loadByKey(Long.valueOf(entryKey));
			if(libraryEntry != null) {
				doSelectSharedFolder(libraryEntry);
			} else {
				doRemoveSharedFolder();
			}
		} else {
			doRemoveSharedFolder();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("library.configuration.title");
		formLayout.setElementCssClass("o_sel_library_configuration");
		
		boolean enabled = libraryModule.isEnabled();
		enableEl = uifactory.addToggleButton("library.enable", "library.enable", translate("on"), translate("off"), formLayout);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.toggle(enabled);

		sharedFolderNameEl = uifactory.addStaticTextElement("library.shared.folder", "library.shared.folder",
				translate("library.no.sharedfolder"), formLayout);
		sharedFolderNameEl.setElementCssClass("o_sel_selected_shared_folder");
		
		sharedFolderCont = FormLayoutContainer.createButtonLayout("sharedButtons", getTranslator());
		formLayout.add(sharedFolderCont);
		removeSharedFolderButton = uifactory.addFormLink("remove.shared.folder", sharedFolderCont, Link.BUTTON);
		addSharedFolderButton = uifactory.addFormLink("add.shared.folder", sharedFolderCont, Link.BUTTON);
		addSharedFolderButton.setElementCssClass("o_sel_add_shared_folder");
		
		boolean uploadEnabled = libraryModule.isUploadEnabled();
		uploadEnableEl = uifactory.addToggleButton("library.upload.enabled", "library.upload.enabled", translate("on"), translate("off"), formLayout);
		uploadEnableEl.addActionListener(FormEvent.ONCHANGE);
		uploadEnableEl.toggle(uploadEnabled);

		boolean uploadApprovalEnabled = libraryModule.isApprovalEnabled();
		uploadApproveEnableEl = uifactory.addToggleButton("library.upload.approval.enabled", "library.upload.approval.enabled", translate("on"), translate("off"), formLayout);
		uploadApproveEnableEl.setElementCssClass("o_sel_library_approval");
		uploadApproveEnableEl.toggle(uploadApprovalEnabled);
		
		String mailAfterUpload = libraryModule.getEmailContactsToNotifyAfterUpload();
		mailAfterUploadEl = uifactory.addTextElement("library.configuration.mail.after.upload", 256, mailAfterUpload, formLayout);
		
		String mailAfterFreeing = libraryModule.getEmailContactsToNotifyAfterFreeing();
		mailAfterFreeingEl = uifactory.addTextElement("library.configuration.mail.after.freeing", 256, mailAfterFreeing, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		boolean uploadEnable = uploadEnableEl.isOn();
		uploadEnableEl.setVisible(enabled);
		uploadApproveEnableEl.setVisible(uploadEnable);
		mailAfterUploadEl.setVisible(enabled && uploadEnable);
		mailAfterFreeingEl.setVisible(enabled && uploadEnable);
		sharedFolderNameEl.setVisible(enabled);
		sharedFolderCont.setVisible(enabled);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(chooseFolderCtr == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = chooseFolderCtr.getSelectedEntry();
				doSelectSharedFolder(repoEntry);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(chooseFolderCtr);
		removeAsListenerAndDispose(cmc);
		chooseFolderCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source || uploadEnableEl == source) {
			updateUI();
		} else if(removeSharedFolderButton == source) {
			doRemoveSharedFolder();
		} else if(addSharedFolderButton == source) {
			doDisplaySearchController(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateEmail(mailAfterUploadEl);
		allOk &= validateEmail(mailAfterFreeingEl);
		return allOk;
	}
	
	private boolean validateEmail(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue()) && !MailHelper.isValidEmailAddress(el.getValue())) {
			el.setErrorKey("error.mail.not.valid");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isOn();
		boolean uploadEnabled = uploadEnableEl.isOn();
		boolean uploadApprovalEnabled = uploadApproveEnableEl.isOn();
		libraryModule.setEnabled(enabled);
		libraryModule.setUploadEnabled(uploadEnabled);
		libraryModule.setApprovalEnabled(uploadApprovalEnabled);
		if(enabled) {
			libraryModule.setEmailContactsToNotifyAfterUpload(mailAfterUploadEl.getValue());
			libraryModule.setEmailContactsToNotifyAfterFreeing(mailAfterFreeingEl.getValue());
			
			RepositoryEntry sharedFolder = (RepositoryEntry)sharedFolderNameEl.getUserObject();
			if(sharedFolder == null) {
				libraryModule.setLibraryEntryKey(null);
				libraryManager.removeExistingLockFile();
				libraryManager.setCatalogRepoEntry(null);
			} else if(!sharedFolder.getKey().toString().equals(libraryModule.getLibraryEntryKey())) {
				libraryManager.removeExistingLockFile();
				libraryModule.setLibraryEntryKey(sharedFolder.getKey().toString());
				libraryManager.setCatalogRepoEntry(sharedFolder);
				libraryManager.lockFolderAndPreventDoubleIndexing();
			}	
		} else {
			libraryManager.setCatalogRepoEntry(null);
			libraryManager.removeExistingLockFile();
		}
	}
	
	/**
	 * Displays the shared folder search controller
	 * 
	 * @param ureq
	 */
	private void doDisplaySearchController(UserRequest ureq) {
		if(guardModalController(chooseFolderCtr)) return;
		
		String choose = translate("library.catalog.choose.folder.link");
		chooseFolderCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, SharedFolderFileResource.TYPE_NAME, choose);
		listenTo(chooseFolderCtr);
		
		String title = translate("add.shared.folder");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseFolderCtr.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	/**
	 * Updates configuration with selected shared folder.
	 * 
	 * @param repoEntry The shared folder entry
	 * @param ureq The user request
	 */
	private void doSelectSharedFolder(RepositoryEntry repoEntry) {
		sharedFolderNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
		sharedFolderNameEl.setUserObject(repoEntry);
		removeSharedFolderButton.setVisible(true);
	}

	/**
	 * Removes the current shared folder from the configuration
	 * 
	 * @param ureq The user request
	 */
	private void doRemoveSharedFolder() {			
		sharedFolderNameEl.setValue(translate("library.no.sharedfolder"));
		sharedFolderNameEl.setUserObject(null);
		removeSharedFolderButton.setVisible(false);
	}
}
