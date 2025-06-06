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
package org.olat.modules.taxonomy.ui;

import static org.olat.modules.taxonomy.ui.TaxonomyUIFactory.BUNDLE_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem.TabIndentation;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTaxonomyLevelController extends FormBasicController {
	
	private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	
	private TabbedPaneItem tabbedPane;
	
	private TextElement sortOrderEl;
	private TextElement identifierEl;
	private SingleSelection pathEl;
	private SingleSelection taxonomyLevelTypeEl;
	private FormLayoutContainer teaserImageCont;
	private FileElement teaserImageEl;
	private FormLayoutContainer backgroundImageCont;
	private FileElement backgroundImageEl;
	
	private final String i18nSuffix;
	private final boolean canEdit;
	private TaxonomyLevel level;
	private TaxonomyLevel parentLevel;
	private Taxonomy taxonomy;
	
	// Translations
	private String displayNameKey;
	private boolean displayNameEnabled;
	private String descriptionKey;
	private boolean descriptionEnabled;
	private Locale defaultLocale;
	private int defaultLocaleTabIndex = -1;
	private TextElement defaultLocaleDisplayNameEl;
	private List<TranslationItem> translationItems;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CatalogV2Module catalogV2Module;

	public EditTaxonomyLevelController(UserRequest ureq, WindowControl wControl, TaxonomySecurityCallback secCallback, TaxonomyLevel level) {
		super(ureq, wControl, "admin_metadata");
		this.level = level;
		this.canEdit = secCallback.canEditMetadata(level);
		this.i18nSuffix = level.getI18nSuffix();
		this.parentLevel = level.getParent();
		this.taxonomy = level.getTaxonomy();
		initTranslation();
		initForm(ureq);
	}
	
	public EditTaxonomyLevelController(UserRequest ureq, WindowControl wControl, TaxonomyLevel parentLevel, Taxonomy rootTaxonomy) {
		super(ureq, wControl, "admin_metadata");
		this.level = null;
		this.canEdit = true;
		this.i18nSuffix = taxonomyService.createI18nSuffix();
		this.parentLevel = parentLevel;
		this.taxonomy = rootTaxonomy;
		initTranslation();
		initForm(ureq);
	}
	
	private void initTranslation() {
		this.displayNameKey = TaxonomyUIFactory.PREFIX_DISPLAY_NAME + i18nSuffix;
		this.displayNameEnabled = canEdit && !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.displayName);
		this.descriptionKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + i18nSuffix;
		this.descriptionEnabled = canEdit && !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.description);
		this.defaultLocale = I18nModule.getDefaultLocale();
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return level;
	}
	
	public TaxonomyLevel getParentLevel() {
		return parentLevel;
	}
	
	private List<TaxonomyLevelType> getTypes() {
		List<TaxonomyLevelType> types = new ArrayList<>();
		if(level != null) {
			List<TaxonomyLevel> parentLine = taxonomyService.getTaxonomyLevelParentLine(level, taxonomy);
			for(int i=parentLine.size() - 1; i-->0; ) {
				TaxonomyLevel parent = parentLine.get(i);
				TaxonomyLevelType parentType = parent.getType();
				if(parentType != null) {
					Set<TaxonomyLevelTypeToType> typeToTypes = parentType.getAllowedTaxonomyLevelSubTypes();
					for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
						if(typeToType != null) {
							types.add(typeToType.getAllowedSubTaxonomyLevelType());
						}
					}
					break;
				}
			}
		}
		if(types.isEmpty()) {
			types.addAll(taxonomyService.getTaxonomyLevelTypes(taxonomy));
		} else if(level != null && level.getType() != null) {
			if(!types.contains(level.getType())) {
				types.add(level.getType());
			}
		}
		return types;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_taxonomy_level_form");
		
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("taxonomy.metadata"));
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		if(level == null || level.getKey() == null) {
			String[] pathKeys;
			String[] pathValues;
			if(parentLevel == null) {
				List<String> pathKeyList = new ArrayList<>();
				List<String> pathValueList = new ArrayList<>();
				buildPathKeysAndValues(pathKeyList, pathValueList);
				pathKeys = pathKeyList.toArray(new String[pathKeyList.size()]);
				pathValues = pathValueList.toArray(new String[pathValueList.size()]);
			} else {
				pathKeys = new String[] { parentLevel.getKey().toString() };
				pathValues = new String[] { parentLevel.getMaterializedPathIdentifiers() };
			}
			pathEl = uifactory.addDropdownSingleselect("level.path", "taxonomy.level.path", generalCont, pathKeys, pathValues, null);
			pathEl.setEnabled(canEdit && parentLevel == null);
		}

		String identifier = level == null ? "" : level.getIdentifier();
		identifierEl = uifactory.addTextElement("level.identifier", "level.ext.ref", 255, identifier, generalCont);
		identifierEl.setEnabled(canEdit && !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.identifier));
		identifierEl.setElementCssClass("o_sel_taxonomy_level_identifier");
		identifierEl.setMandatory(true);

		List<TaxonomyLevelType> types = getTypes();
		String[] typeKeys = new String[types.size() + 1];
		String[] typeValues = new String[types.size() + 1];
		typeKeys[0] = "";
		typeValues[0] = "-";
		for(int i=types.size(); i-->0; ) {
			typeKeys[i+1] = types.get(i).getKey().toString();
			typeValues[i+1] = types.get(i).getDisplayName();
		}
		taxonomyLevelTypeEl = uifactory.addDropdownSingleselect("level.type", "level.type", generalCont, typeKeys, typeValues, null);
		taxonomyLevelTypeEl.setEnabled(canEdit && !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.type));
		boolean typeFound = false;
		if(level != null && level.getType() != null) {
			String selectedTypeKey = level.getType().getKey().toString();
			for(String typeKey:typeKeys) {
				if(typeKey.equals(selectedTypeKey)) {
					taxonomyLevelTypeEl.select(selectedTypeKey, true);
					typeFound = true;
					break;
				}
			}
		}
		if(!typeFound) {
			taxonomyLevelTypeEl.select(typeKeys[0], true);
		}
		
		String sortOrder = level == null || level.getSortOrder() == null ? "" : level.getSortOrder().toString();
		sortOrderEl = uifactory.addTextElement("level.sort.order", "level.sort.order", 255, sortOrder, generalCont);
		sortOrderEl.setEnabled(canEdit && !TaxonomyLevelManagedFlag.isManaged(level, TaxonomyLevelManagedFlag.displayName));
		
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabIndentation.defaultFormLayout);

		List<Locale> locales = i18nModule.getEnabledLanguageKeys().stream()
				.map(key -> i18nManager.getLocaleOrNull(key))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Map<Locale, Locale> allOverlays = i18nModule.getOverlayLocales();
		translationItems = new ArrayList<>(locales.size());
		for (int i = 0; i < locales.size(); i++) {
			Locale locale = locales.get(i);
			initTranslationController(formLayout, locale, allOverlays.get(locale));
			if (defaultLocale.equals(locale)) {
				defaultLocaleTabIndex = i;
			}
		}
		tabbedPane.setSelectedPane(ureq, defaultLocaleTabIndex);
		
		FormLayoutContainer imagelCont = FormLayoutContainer.createDefaultFormLayout("images", getTranslator());
		imagelCont.setFormTitle(translate("level.images"));
		imagelCont.setRootForm(mainForm);
		formLayout.add(imagelCont);
		
		teaserImageCont = FormLayoutContainer.createCustomFormLayout("teaserPreview", getTranslator(), velocity_root + "/teaser_preview.html");
		teaserImageCont.setLabel("level.image.teaser", null);
		teaserImageCont.setRootForm(mainForm);
		imagelCont.add(teaserImageCont);
		teaserImageCont.contextPut("square", catalogV2Module.isEnabled() && CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogV2Module.getLauncherTaxonomyLevelStyle()));
		
		teaserImageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "level.image.teaser", imagelCont);
		teaserImageEl.setMaxUploadSizeKB(2048, null, null);
		teaserImageEl.setExampleKey("level.image.teaser.example", null);
		teaserImageEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		teaserImageEl.setReplaceButton(true);
		teaserImageEl.setDeleteEnabled(true);
		teaserImageEl.setEnabled(canEdit);
		teaserImageEl.addActionListener(FormEvent.ONCHANGE);
		VFSLeaf teaserImage = taxonomyService.getTeaserImage(level);
		if (teaserImage instanceof LocalFileImpl teaserLeaf) {
			teaserImageEl.setInitialFile(teaserLeaf.getBasefile());
		}
		updateTeaserImagePreview(ureq);
		
		backgroundImageCont = FormLayoutContainer.createCustomFormLayout("backgroundPreview", getTranslator(), velocity_root + "/background_preview.html");
		backgroundImageCont.setLabel("level.image.background", null);
		backgroundImageCont.setRootForm(mainForm);
		imagelCont.add(backgroundImageCont);
		
		backgroundImageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "level.image.background", imagelCont);
		backgroundImageEl.setMaxUploadSizeKB(5024, null, null);
		backgroundImageEl.setExampleKey("level.image.background.example", null);
		backgroundImageEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		backgroundImageEl.setReplaceButton(true);
		backgroundImageEl.setDeleteEnabled(true);
		backgroundImageEl.setEnabled(canEdit);
		backgroundImageEl.addActionListener(FormEvent.ONCHANGE);
		VFSLeaf backgroundImage = taxonomyService.getBackgroundImage(level);
		if (backgroundImage instanceof LocalFileImpl backgroundLeaf) {
			backgroundImageEl.setInitialFile(backgroundLeaf.getBasefile());
		}
		updateBackgroundImagePreview(ureq);
		
		if (canEdit) {
			FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
			buttonsWrapperCont.setRootForm(mainForm);
			formLayout.add(buttonsWrapperCont);
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttonsCont", getTranslator());
			buttonsWrapperCont.add(buttonsCont);
			uifactory.addFormSubmitButton("save", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		}
	}
	
	private void buildPathKeysAndValues(List<String> pathKeyList, List<String> pathValueList) {
		pathKeyList.add("-");
		pathValueList.add("");

		List<TreeNode> nodeList = new TaxonomyAllTreesBuilder(getLocale()).getFlattedModel(taxonomy, false, getLocale());
		for(TreeNode node:nodeList) {
			TaxonomyLevel taxonomyLevel = (TaxonomyLevel)node.getUserObject();
			pathKeyList.add(taxonomyLevel.getKey().toString());
			pathValueList.add(taxonomyLevel.getMaterializedPathIdentifiers());
		}
	}
	
	private void initTranslationController(FormItemContainer formLayout, Locale locale, Locale overlayLocale) {
		String displayName = i18nManager.getLocalizedString(BUNDLE_NAME, displayNameKey, null, locale, true, false);
		String description = i18nManager.getLocalizedString(BUNDLE_NAME, descriptionKey, null, locale, true, false);
		
		String elementSuffix = locale.toString();
		FormLayoutContainer cont = FormLayoutContainer.createDefaultFormLayout("trans_" + elementSuffix, getTranslator());
		cont.setRootForm(mainForm);
		formLayout.add(cont);
		tabbedPane.addTab(locale.getDisplayLanguage(getLocale()), cont);
		
		TextElement displayNameEl = uifactory.addTextElement("level.displayname." + elementSuffix, "level.title", 255, displayName, cont);
		displayNameEl.setElementCssClass("o_sel_taxonomy_level_name");
		displayNameEl.setEnabled(displayNameEnabled);
		if (defaultLocale.equals(locale)) {
			displayNameEl.setMandatory(true);
			defaultLocaleDisplayNameEl = displayNameEl;
		}

		RichTextElement descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("level.description." + elementSuffix,
				 "level.description", description, 10, 60, cont, getWindowControl());
		descriptionEl.setEnabled(descriptionEnabled);
		
		TranslationItem translationItem = new TranslationItem(locale, overlayLocale, displayNameEl, descriptionEl);
		translationItems.add(translationItem);
		displayNameEl.setUserObject(translationItem);
	}
	
	private void updateTeaserImagePreview(UserRequest ureq) {
		File teaserImage = teaserImageEl.getUploadFile();
		if (teaserImage == null) {
			teaserImage = teaserImageEl.getInitialFile();
		}
		
		if (teaserImage != null) {
			String teaserImageUrl = registerMapper(ureq, new VFSMediaMapper(teaserImage));
			teaserImageCont.contextPut("imageUrl", teaserImageUrl);
			String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), level, () -> translate("level.image.teaser.sample"));
			teaserImageCont.contextPut("displayName", StringHelper.escapeHtml(displayName));
			teaserImageCont.setVisible(true);
			teaserImageEl.setLabel(null, null);
		} else {
			teaserImageCont.setVisible(false);
			teaserImageEl.setLabel("level.image.teaser", null);
		}
	}
	
	private void updateBackgroundImagePreview(UserRequest ureq) {
		File backgroundImage = backgroundImageEl.getUploadFile();
		if (backgroundImage == null) {
			backgroundImage = backgroundImageEl.getInitialFile();
		}
		
		if (backgroundImage != null) {
			String backgroundImageUrl = registerMapper(ureq, new VFSMediaMapper(backgroundImage));
			backgroundImageCont.contextPut("bgImageUrl", backgroundImageUrl);
			String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), level, () -> translate("level.image.teaser.sample"));
			backgroundImageCont.contextPut("displayName", StringHelper.escapeHtml(displayName));
			backgroundImageCont.setVisible(true);
			backgroundImageEl.setLabel(null, null);
		} else {
			backgroundImageCont.setVisible(false);
			backgroundImageEl.setLabel("level.image.background", null);
		}
		
		if (level != null && level.getType() != null) {
			backgroundImageCont.contextPut("typeDisplayName", level.getType().getDisplayName());
		} else {
			backgroundImageCont.contextRemove("typeDisplayName");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == teaserImageEl) {
			if(DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
				teaserImageEl.setInitialFile(null);
				if (teaserImageEl.getUploadFile() != null) {
					teaserImageEl.reset();
				}
				teaserImageEl.clearError();
				updateTeaserImagePreview(ureq);
				markDirty();
			} else if (teaserImageEl.isUploadSuccess()) {
				teaserImageEl.clearError();
				updateTeaserImagePreview(ureq);
				markDirty();
			}
		} else if (source == backgroundImageEl) {
			if(DeleteFileElementEvent.DELETE.equals(event.getCommand())) {
				backgroundImageEl.setInitialFile(null);
				if (backgroundImageEl.getUploadFile() != null) {
					backgroundImageEl.reset();
				}
				backgroundImageEl.clearError();
				updateBackgroundImagePreview(ureq);
				markDirty();
			} else if (backgroundImageEl.isUploadSuccess()) {
				backgroundImageEl.clearError();
				updateBackgroundImagePreview(ureq);
				markDirty();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateTextfield(identifierEl, 64);
		allOk &= validateTextfield(defaultLocaleDisplayNameEl, 255);
		
		sortOrderEl.clearError();
		if(StringHelper.containsNonWhitespace(sortOrderEl.getValue())) {
			try {
				Integer.parseInt(sortOrderEl.getValue());
			} catch (NumberFormatException e) {
				sortOrderEl.setErrorKey("error.sort.order.integer");
				allOk &= false;
			}
		}
		
		teaserImageEl.validate();

		if(!StringHelper.containsNonWhitespace(defaultLocaleDisplayNameEl.getValue())) {
			defaultLocaleDisplayNameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
			tabbedPane.setSelectedPane(ureq, defaultLocaleTabIndex);
		}
		
		
		return allOk;
	}
	
	private boolean validateTextfield(TextElement textEl, int maxSize) {
		boolean allOk = true;
		
		textEl.clearError();
		if(!StringHelper.containsNonWhitespace(textEl.getValue())) {
			textEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(textEl.getValue().length() >= maxSize) {
			textEl.setErrorKey("form.error.toolong", Integer.toString(maxSize));
			allOk &= false;
		}
		// TODO To be discussed
		// TODO import/export will have problems, if the identifier contains invalid (File)names
		// TODO OO-6763
		/*else if (!FileUtils.validateFilename(textEl.getValue())) {
			textEl.setErrorKey("form.error.invalid", textEl.getValue());
			allOk &= false;
		}*/
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(level == null) {
			TaxonomyLevel selectedParentLevel = null;
			if(parentLevel == null) {
				if(pathEl != null && pathEl.isEnabled() && pathEl.isOneSelected() && !"-".equals(pathEl.getSelectedKey())) {
					TaxonomyLevelRef ref = new TaxonomyLevelRefImpl(Long.valueOf(pathEl.getSelectedKey()));
					selectedParentLevel = taxonomyService.getTaxonomyLevel(ref);
				}
			} else {
				selectedParentLevel = parentLevel;
			}
			level = taxonomyService.createTaxonomyLevel(identifierEl.getValue(), i18nSuffix, null,
					null, selectedParentLevel, taxonomy);
		} else {
			level = taxonomyService.getTaxonomyLevel(level);
			level.setIdentifier(identifierEl.getValue());
		}
		
		String selectedTypeKey = taxonomyLevelTypeEl.getSelectedKey();
		if(StringHelper.containsNonWhitespace(selectedTypeKey)) {
			TaxonomyLevelTypeRef typeRef = new TaxonomyLevelTypeRefImpl(Long.valueOf(selectedTypeKey));
			TaxonomyLevelType type = taxonomyService.getTaxonomyLevelType(typeRef);
			level.setType(type);
		} else {
			level.setType(null);
		}
		if(StringHelper.isLong(sortOrderEl.getValue())) {
			level.setSortOrder(Integer.valueOf(sortOrderEl.getValue()));
		} else {
			level.setSortOrder(null);
		}
		
		level = taxonomyService.updateTaxonomyLevel(level);
		
		for (TranslationItem translationItem : translationItems) {
			I18nItem displayNameItem = i18nManager.getI18nItem(BUNDLE_NAME, displayNameKey, translationItem.getOverlayLocale());
			i18nManager.saveOrUpdateI18nItem(displayNameItem, translationItem.getDisplayNameEl().getValue());
			
			I18nItem descriptionItem = i18nManager.getI18nItem(BUNDLE_NAME, descriptionKey, translationItem.getOverlayLocale());
			i18nManager.saveOrUpdateI18nItem(descriptionItem, translationItem.getDescriptionEl().getValue());
		}
		
		if (teaserImageEl.getUploadFile() != null) {
			taxonomyService.storeTeaserImage(level, getIdentity(), teaserImageEl.getUploadFile(), teaserImageEl.getUploadFileName());
		} else if (teaserImageEl.getInitialFile() == null) {
			taxonomyService.deleteTeaserImage(level);
		}
		
		if (backgroundImageEl.getUploadFile() != null) {
			taxonomyService.storeBackgroundImage(level, getIdentity(), backgroundImageEl.getUploadFile(), backgroundImageEl.getUploadFileName());
		} else if (backgroundImageEl.getInitialFile() == null) {
			taxonomyService.deleteBackgroundImage(level);
		}
		updateTeaserImagePreview(ureq);
		updateBackgroundImagePreview(ureq);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class TranslationItem {
		
		private final Locale locale;
		private final Locale overlayLocale;
		private final TextElement displayNameEl;
		private final RichTextElement descriptionEl;
		
		public TranslationItem(Locale locale, Locale overlayLocale, TextElement displayNameEl, RichTextElement descriptionEl) {
			this.locale = locale;
			this.overlayLocale = overlayLocale;
			this.displayNameEl = displayNameEl;
			this.descriptionEl = descriptionEl;
		}

		public Locale getLocale() {
			return locale;
		}
		
		public Locale getOverlayLocale() {
			return overlayLocale;
		}

		public TextElement getDisplayNameEl() {
			return displayNameEl;
		}

		public RichTextElement getDescriptionEl() {
			return descriptionEl;
		}
		
	}
	
}
