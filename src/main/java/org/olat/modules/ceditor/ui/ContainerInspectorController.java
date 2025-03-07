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
package org.olat.modules.ceditor.ui;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.GeneralStyleSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerInspectorController extends FormBasicController implements PageElementInspectorController {

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	private ContainerElement container;
	private final PageElementStore<ContainerElement> store;
	private List<FormLink> layoutLinks;
	private MediaUIHelper.GeneralStyleComponents generalStyleComponents;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;

	public ContainerInspectorController(UserRequest ureq, WindowControl wControl, ContainerElement container,
			PageElementStore<ContainerElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.container = container;
		this.store = store;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.layout");
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
		if (needToChangeAlertBoxSettings()) {
			doChangeAlertBoxSettings(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof ContainerEditorController && event instanceof ChangePartEvent cpe) {
			if (cpe.isElement(container)) {
				container = (ContainerElement) cpe.getElement();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addLayoutTab(formLayout);
		addStyleTab(formLayout);
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		layoutLinks = MediaUIHelper.addContainerLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory,
				container.getContainerSettings(), velocity_root);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		FormLayoutContainer styleCont = FormLayoutContainer.createVerticalFormLayout("style", getTranslator());
		formLayout.add(styleCont);
		tabbedPane.addTab(getTranslator().translate("tab.style"), styleCont);

		generalStyleComponents = MediaUIHelper.addGeneralStyleSettings(styleCont, getTranslator(), uifactory,
				getGeneralStyleSettings(container.getContainerSettings()), colorService, getLocale());

		alertBoxComponents = MediaUIHelper.addAlertBoxSettings(styleCont, getTranslator(), uifactory,
				getAlertBoxSettings(container.getContainerSettings()), colorService, getLocale());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink formLink && formLink.getUserObject() instanceof ContainerLayout containerLayout) {
			doSetLayout(ureq, containerLayout);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		} else if (generalStyleComponents.matches(source)) {
			doChangeGeneralStyleSettings(ureq);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSetLayout(UserRequest ureq, ContainerLayout newLayout) {
		ContainerSettings settings = container.getContainerSettings();
		settings.updateType(newLayout);
		container.setLayoutOptions(ContentEditorXStream.toXml(settings));
		container = store.savePageElement(container);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(container));

		for (FormLink layoutLink : layoutLinks) {
			boolean active = layoutLink.getUserObject() == newLayout;
			layoutLink.setElementCssClass(active ? "active" : "");
		}
	}

	private boolean needToChangeAlertBoxSettings() {
		ContainerSettings settings = container.getContainerSettings();
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(settings);
		if (alertBoxComponents.titleEl().getValue() != null) {
			return !alertBoxComponents.titleEl().getValue().equals(alertBoxSettings.getTitle());
		}
		return false;
	}
	
	private void doChangeAlertBoxSettings(UserRequest ureq) {
		ContainerSettings settings = container.getContainerSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(settings);
		alertBoxComponents.sync(alertBoxSettings);
		settings.setAlertBoxSettings(alertBoxSettings);

		container.setLayoutOptions(ContentEditorXStream.toXml(settings));
		container = store.savePageElement(container);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(container));
	}

	private void doChangeGeneralStyleSettings(UserRequest ureq) {
		ContainerSettings settings = container.getContainerSettings();

		GeneralStyleSettings generalStyleSettings = getGeneralStyleSettings(settings);
		generalStyleComponents.sync(generalStyleSettings);
		settings.setGeneralStyleSettings(generalStyleSettings);

		container.setLayoutOptions(ContentEditorXStream.toXml(settings));
		container = store.savePageElement(container);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(container));
	}

	private AlertBoxSettings getAlertBoxSettings(ContainerSettings containerSettings) {
		if (containerSettings.getAlertBoxSettings() != null) {
			return containerSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private GeneralStyleSettings getGeneralStyleSettings(ContainerSettings containerSettings) {
		if (containerSettings.getGeneralStyleSettings() != null) {
			return containerSettings.getGeneralStyleSettings();
		}
		return GeneralStyleSettings.getPredefined();
	}
}
