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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.login.LoginModule;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.ui.RepositoryEntryACColumnDescriptor;
import org.olat.repository.ui.RepositoryFlexiTableModel;
import org.olat.repository.ui.RepositoryFlexiTableModel.RepoCols;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.GuestAccessRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementResourceListController extends FormBasicController implements FlexiTableCssDelegate {

	private FormLink addResourcesButton;
	private FormLink removeResourcesButton;
	private FlexiTableElement tableEl;
	private RepositoryFlexiTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmRemoveCtrl;
	private ReferencableEntriesSearchController repoSearchCtr;
	
	private final boolean resourcesManaged;
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	private final CurriculumElementType curriculumElementType;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementResourceListController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_resources");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		this.curriculumElementType = curriculumElement.getType();
		resourcesManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.resources);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.ac, new RepositoryEntryACColumnDescriptor()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalId));// visible if managed
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.lifecycleLabel));// visible if lifecycle
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleSoftKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.displayname, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleStart, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RepoCols.lifecycleEnd, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.access, new AccessRenderer(getLocale())));
		if(loginModule.isGuestLoginEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.guests, new GuestAccessRenderer(getLocale())));
		}

		tableModel = new RepositoryFlexiTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setCssDelegate(this);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-element-resource-list");
				
		// special rights for managers
		if(!resourcesManaged && secCallback.canManagerCurriculumElementResources(curriculumElement)
				&& (curriculumElementType == null || curriculumElementType.getMaxRepositoryEntryRelations() != 0)) {
			// 1) add
			addResourcesButton = uifactory.addFormLink("add.resources", formLayout, Link.BUTTON);
			addResourcesButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");		
			// 2) remove
			removeResourcesButton = uifactory.addFormLink("remove.resources", formLayout, Link.BUTTON);
			tableEl.addBatchButton(removeResourcesButton);
			// empty behavior
			tableEl.setEmptyTableSettings("table.resources.empty", "table.resources.empty.hint", "o_CourseModule_icon", "add.resources", "o_icon_add", true);
		} else {			
			// default empty message with out create hint
			tableEl.setEmptyTableSettings("table.resources.empty", null, "o_CourseModule_icon");
		}
		
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		RepositoryEntry row = tableModel.getObject(pos);
		if(row == null || row.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| row.getEntryStatus() == RepositoryEntryStatusEnum.deleted) {
			return "o_entry_deleted";
		}
		if(row.getEntryStatus() == RepositoryEntryStatusEnum.closed) {
			return "o_entry_closed";
		}
		return null;
	}
	
	public void loadModel() {
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
		tableModel.setObjects(entries);
		tableEl.reset(true, true, true);
		
		int maxRelations = curriculumElementType == null ? -1 : curriculumElementType.getMaxRepositoryEntryRelations();
		if(addResourcesButton != null) {
			boolean canAddResource = maxRelations < 0 || entries.size() < maxRelations ;
			addResourcesButton.setEnabled(canAddResource);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<RepositoryEntry> rows = (List<RepositoryEntry>)confirmRemoveCtrl.getUserObject();
				doRemove(rows);
			}
		} else if(repoSearchCtr == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doAddRepositoryEntry(repoSearchCtr.getSelectedEntry());
				loadModel();
			} else if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				doAddRepositoryEntry(repoSearchCtr.getSelectedEntries());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(cmc);
		repoSearchCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addResourcesButton == source) {
			doChooseResources(ureq);
		} else if(removeResourcesButton == source) {
			doConfirmRemoveResources(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("select".equals(se.getCommand())) {
					doSelectRepositoryEntry(ureq, tableModel.getObject(se.getIndex()));
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doChooseResources(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doChooseResources(UserRequest ureq) {
		if(guardModalController(repoSearchCtr)) return;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean adminSearch = roles.hasRole(OrganisationRoles.administrator)
				|| roles.hasRole(OrganisationRoles.learnresourcemanager)
				|| roles.hasRole(OrganisationRoles.curriculummanager);
		boolean orgSearch = secCallback.canEditCurriculumElement(curriculumElement) && !adminSearch;
		repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[]{ CourseModule.getCourseTypeName() }, null, null, translate("add.resources"),
				false, false, true, orgSearch, adminSearch, false, Can.referenceable);
		listenTo(repoSearchCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent(), true, translate("add.resources"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddRepositoryEntry(RepositoryEntry entry) {
		doAddRepositoryEntry(Collections.singletonList(entry));
	}
	
	private void doAddRepositoryEntry(List<RepositoryEntry> entries) {
		boolean moveLectureBlocks = false;
		if(entries.size() == 1 && tableModel.getRowCount() == 0) {
			moveLectureBlocks = true;
		}
		
		for(RepositoryEntry entry:entries) {
			curriculumService.addRepositoryEntry(curriculumElement, entry, moveLectureBlocks);
		}
	}
	
	private void doConfirmRemoveResources(UserRequest ureq) {
		Set<Integer> selectedRows = tableEl.getMultiSelectedIndex();
		if(selectedRows.isEmpty()) {
			showWarning("warning.atleastone.resource");
		} else {
			List<RepositoryEntry> rows = new ArrayList<>(selectedRows.size());
			for(Integer selectedRow:selectedRows) {
				rows.add(tableModel.getObject(selectedRow.intValue()));
			}
			String title = translate("confirm.remove.resource.title");
			confirmRemoveCtrl = activateYesNoDialog(ureq, title, translate("confirm.remove.resource.text", ""), confirmRemoveCtrl);
			confirmRemoveCtrl.setUserObject(rows);
		}
	}
	
	private void doRemove(List<RepositoryEntry> resourcesToRemove) {
		for(RepositoryEntry resourceToRemove:resourcesToRemove) {
			curriculumService.removeRepositoryEntry(curriculumElement, resourceToRemove);
		}
		loadModel();
	}
	
	private void doSelectRepositoryEntry(UserRequest ureq, RepositoryEntry entry) {
		if(entry == null) return;
		
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
