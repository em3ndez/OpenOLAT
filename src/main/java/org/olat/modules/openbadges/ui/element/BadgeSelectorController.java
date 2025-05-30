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
package org.olat.modules.openbadges.ui.element;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.OpenBadgesUIFactory;
import org.olat.repository.RepositoryEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-09-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeSelectorController extends FormBasicController {
	private static final String PARAMETER_KEY_BADGE_SELECTION = "bsel_";

	private static final int MAX_RESULTS = 50;

	private FormLink browserButton;
	private FormLink applyButton;
	private TextElement quickSearchEl;
	private FormLink resetQuickSearchButton;

	private StaticTextElement resultsMoreEl;

	private Set<String> selectedRootIds;
	private final List<Row> rows;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public record Row(String rootId, String image, Size size, String statusString, String title, String version) {}

	public BadgeSelectorController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
								   Set<String> availableRootIds, Set<String> selectedRootIds) {
		super(ureq, wControl, "badge_selector",
				Util.createPackageTranslator(OpenBadgesUIFactory.class, ureq.getLocale()));
		this.selectedRootIds = selectedRootIds;

		rows = openBadgesManager.getBadgeClassesWithSizes(entry).stream()
				.filter(bce -> availableRootIds.contains(bce.badgeClass().getRootId()))
				.map(this::row).toList();

		String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());
		flc.contextPut("mediaUrl", mediaUrl);

		List<Row> selectedRows = rows.stream().filter(row -> selectedRootIds.contains(row.rootId)).toList();
		flc.contextPut("selectedRows", selectedRows);

		flc.contextPut("altImageText", translate("badge.image"));
		initForm(ureq);
		doResetQuickSearch(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initSearchLine(formLayout);

		StaticTextElement selectionNoneEl = uifactory.addStaticTextElement("badge.selector.selection.none",
				"badge.selector.selection", translate("badge.selector.selection.none"), formLayout);
		selectionNoneEl.setVisible(selectedRootIds.isEmpty());

		StaticTextElement selectionNumEl = uifactory.addStaticTextElement("badge.selector.selection.num",
				"badge.selector.selection.num", "", formLayout);
		selectionNumEl.setLabel("badge.selector.selection.num", new String[] { String.valueOf(selectedRootIds.size()) });
		selectionNumEl.setVisible(!selectedRootIds.isEmpty());
		
		resultsMoreEl = uifactory.addStaticTextElement("badge.selector.results.more", null,
				translate("badge.selector.results.more", String.valueOf(MAX_RESULTS)), formLayout);
		resultsMoreEl.setVisible(false);

		browserButton = uifactory.addFormLink("badge.selector.browser", formLayout, Link.BUTTON_SMALL);
		browserButton.setVisible(false);
		applyButton = uifactory.addFormLink("apply", formLayout, Link.BUTTON_SMALL);
		applyButton.setPrimary(true);
	}

	private void initSearchLine(FormItemContainer formLayout) {
		FormLink quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setElementCssClass("o_indicate_search");
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setEnabled(false);
		quickSearchButton.setDomReplacementWrapperRequired(false);
		quickSearchButton.setTitle(translate("search"));

		quickSearchEl = uifactory.addTextElement("quickSearch", null, 32, "", formLayout);
		quickSearchEl.setPlaceholderKey("enter.search.term", null);
		quickSearchEl.setElementCssClass("o_quick_search");
		quickSearchEl.setDomReplacementWrapperRequired(false);
		quickSearchEl.addActionListener(FormEvent.ONKEYUP);
		quickSearchEl.setFocus(true);
		quickSearchEl.setAriaLabel(translate("enter.search.term"));

		resetQuickSearchButton = uifactory.addFormLink("resetQuickSearch", "", null, formLayout,
				Link.BUTTON | Link.NONTRANSLATED);
		resetQuickSearchButton.setElementCssClass("o_reset_search");
		resetQuickSearchButton.setIconLeftCSS("o_icon o_icon_remove_filters");
		resetQuickSearchButton.setDomReplacementWrapperRequired(false);
		resetQuickSearchButton.setTitle(translate("reset"));
	}

	private Row row(OpenBadgesManager.BadgeClassWithSize badgeClassWithSize) {
		BadgeClass badgeClass = badgeClassWithSize.badgeClass();
		Size size = badgeClassWithSize.fitIn(40, 40);
		String statusString = getTranslator().translate("class.status." + badgeClass.getStatus().name());
		return new Row(badgeClass.getRootId(), badgeClass.getImage(), size, statusString, badgeClass.getName(),
				badgeClass.getVersion());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (browserButton == source) {
			fireEvent(ureq, BROWSE_EVENT);
		} else if (applyButton == source) {
			doApply(ureq);
		} else if (quickSearchEl == source) {
			doQuickSearch(ureq);
		} else if (resetQuickSearchButton == source) {
			doResetQuickSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == browserButton) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doApply(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doApply(UserRequest ureq) {
		HashSet<String> selectedRootIds = new HashSet<>();
		for (String name : ureq.getParameterSet()) {
			if (name.startsWith(PARAMETER_KEY_BADGE_SELECTION)) {
				String badgeRootId = name.substring(PARAMETER_KEY_BADGE_SELECTION.length());
				selectedRootIds.add(badgeRootId);
			}
		}
		this.selectedRootIds = selectedRootIds;
		fireEvent(ureq, new BadgesSelectedEvent(selectedRootIds));
	}

	private void doQuickSearch(UserRequest ureq) {
		updateUI();

		fireEvent(ureq, RESIZED_EVENT);
	}

	private void doResetQuickSearch(UserRequest ureq) {
		quickSearchEl.setValue("");
		updateUI();

		fireEvent(ureq, RESIZED_EVENT);
	}

	private void updateUI() {
		resultsMoreEl.setVisible(false);
		
		String searchFieldValue = quickSearchEl.getValue().toLowerCase();
		quickSearchEl.getComponent().setDirty(false);

		if (StringHelper.containsNonWhitespace(searchFieldValue)) {
			List<Row> unselectedRows = rows.stream()
					.filter(row -> !selectedRootIds.contains(row.rootId))
					.filter(row -> row.title.toLowerCase().contains(searchFieldValue))
					.toList();

			if (unselectedRows.size() > MAX_RESULTS) {
				unselectedRows = new ArrayList<>(unselectedRows.subList(0, MAX_RESULTS));
				resultsMoreEl.setVisible(true);
			}

			flc.contextPut("unselectedRows", unselectedRows);
		} else {
			flc.contextPut("unselectedRows", Collections.emptyList());
		}

		resultsMoreEl.getComponent().setDirty(true);
	}

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}

	public static final Event BROWSE_EVENT = new Event("badge-selector-browse");

	public static class BadgesSelectedEvent extends Event {

		@Serial
		private static final long serialVersionUID = -7523245830075971768L;

		private final Set<String> rootIds;

		public BadgesSelectedEvent(Set<String> rootIds) {
			super("badges-selected");
			this.rootIds = rootIds;
		}

		public Set<String> getRootIds() {
			return rootIds;
		}
	}

	public static final Event RESIZED_EVENT = new Event("badge-selector-resized");
}
