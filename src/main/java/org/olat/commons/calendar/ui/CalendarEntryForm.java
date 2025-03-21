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

package org.olat.commons.calendar.ui;

import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.springframework.beans.factory.annotation.Autowired;


public class CalendarEntryForm extends FormBasicController {

	public static final String SUBMIT_SINGLE = "single";

	public static final String RECURRENCE_NONE = "NONE";

	private SingleSelection chooseCalendar;
	private TextElement subjectEl;
	private TextElement descriptionEl;
	private TextElement locationEl;
	private TextElement liveStreamUrlEl;
	private FormLink colorResetLink;
	private MultipleSelectionElement liveStreamUrlTypeEl;
	private SingleSelection liveStreamUrlTemplateEl;
	private FormToggle allDayEvent;
	private DateChooser startEl;
	private DateChooser endEl;
	private SingleSelection classification;
	private SingleSelection chooseRecurrence;
	private DateChooser recurrenceEnd;
	private FormLink deleteEventButton;
	private ColorPickerElement colorPickerEl;
	
	private final KalendarEvent event;
	private final KalendarRenderWrapper chosenWrapper;
	private final List<KalendarRenderWrapper> writeableCalendars;
	private final Map<String, String> calendarIdToCalendarType;
	private String colorCssClass;
	private final boolean readOnly;
	private final boolean isNew;
	private final String caller;
	
	private final String[] calendarKeys;
	private final String[] calendarValues;
	private final String[] keysRecurrence;
	private final String[] valuesRecurrence;
	private final String[] classKeys;
	private final String[] classValues;
	
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private LiveStreamService liveStreamService;

	/**
	 * Display an event for modification or to add a new event.
	 * 
	 * @param event Event to use in entry form.
	 * @param availableCalendars At least one calendar must be editable if this is a new event.
	 * @param isNew	If it is a new event, display a list of calendars to choose from.
	 */
	public CalendarEntryForm(UserRequest ureq, WindowControl wControl, KalendarEvent event, KalendarRenderWrapper chosenWrapper,
			Collection<KalendarRenderWrapper> availableCalendars, boolean isNew, String caller, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "calEntry_edit", mainForm);
		this.caller = caller;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		this.event = event;
		this.chosenWrapper = chosenWrapper;
		readOnly = chosenWrapper != null && chosenWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
		this.isNew = isNew;
		
		writeableCalendars = new ArrayList<>();
		for (KalendarRenderWrapper calendarRenderWrapper : availableCalendars) {
			if (calendarRenderWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) {
				writeableCalendars.add(calendarRenderWrapper);
			}
		}
		
		calendarKeys = new String[writeableCalendars.size()];
		calendarValues = new String[writeableCalendars.size()];
		calendarIdToCalendarType = new HashMap<>();
		for (int i = 0; i < writeableCalendars.size(); i++) {
			KalendarRenderWrapper cw = writeableCalendars.get(i);
			calendarKeys[i] = cw.getKalendar().getCalendarID();
			calendarValues[i] = cw.getDisplayName();
			calendarIdToCalendarType.put(cw.getKalendar().getCalendarID(), cw.getKalendar().getType());
		}

		keysRecurrence = new String[] {
				RECURRENCE_NONE,
				KalendarEvent.DAILY,
				KalendarEvent.WORKDAILY,
				KalendarEvent.WEEKLY,
				KalendarEvent.BIWEEKLY,
				KalendarEvent.MONTHLY,
				KalendarEvent.YEARLY
		};
		valuesRecurrence = new String[] {
				translate("cal.form.recurrence.none"),
				translate("cal.form.recurrence.daily"),
				translate("cal.form.recurrence.workdaily"),
				translate("cal.form.recurrence.weekly"),
				translate("cal.form.recurrence.biweekly"),
				translate("cal.form.recurrence.monthly"),
				translate("cal.form.recurrence.yearly")
		};
		
		// classification
		classKeys = new String[] {"0", "1", "2"};
		classValues = new String[] {
				getTranslator().translate("cal.form.class.private"),
				getTranslator().translate("cal.form.class.freebusy"),
				getTranslator().translate("cal.form.class.public")
		};
	
		initForm(ureq);
	}
	
	protected void setEntry(KalendarEvent kalendarEvent) {
		// subject
		if (readOnly && kalendarEvent.getClassification() == KalendarEvent.CLASS_X_FREEBUSY) {
			subjectEl.setValue(getTranslator().translate("cal.form.subject.hidden"));
		} else {
			subjectEl.setValue(kalendarEvent.getSubject());
		}
		// location
		if (readOnly && kalendarEvent.getClassification() == KalendarEvent.CLASS_X_FREEBUSY) {
			locationEl.setValue(getTranslator().translate("cal.form.location.hidden"));
		} else {
			locationEl.setValue(kalendarEvent.getLocation());
		}
	
		doUpdateColor(kalendarEvent);
		
		startEl.setDate(DateUtils.toDate(kalendarEvent.getBegin()));
		endEl.setDate(DateUtils.toDate(kalendarEvent.getEnd()));
		boolean allDay = kalendarEvent.isAllDayEvent();
		if (allDay) {
			allDayEvent.toggleOn();
		} else {
			allDayEvent.toggleOff();
		}
		endEl.setDateChooserTimeEnabled(!allDay);
		startEl.setDateChooserTimeEnabled(!allDay);

		setClassification(kalendarEvent);

		if(StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			chooseRecurrence.setVisible(false);
		} else {
			String recurrence = CalendarUtils.getRecurrence(kalendarEvent.getRecurrenceRule());
			if(recurrence != null && !recurrence.equals("") && !recurrence.equals(RECURRENCE_NONE)) {
				chooseRecurrence.select(recurrence, true);
				ZonedDateTime recurEnd = calendarManager.getRecurrenceEndDate(kalendarEvent.getRecurrenceRule());
				if(recurEnd != null) {
					recurrenceEnd.setDate(DateUtils.toDate(recurEnd));
				}
			} else {
				chooseRecurrence.select(RECURRENCE_NONE, true);
			}
		}
		
		updateLiveStreamUI(kalendarEvent);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		startEl.clearError();
		if (startEl.getDate() == null) {
			startEl.setErrorKey("cal.form.error.date");
			allOk &= false;
		} else if(!validateFormItem(ureq, startEl)) {
			allOk &= false;
		}
		
		endEl.clearError();
		if (endEl.getDate() == null) {
			endEl.setErrorKey("cal.form.error.date");
			allOk &= false;
		} else if(!validateFormItem(ureq, endEl)) {
			allOk &= false;
		} else if (startEl.getDate() != null && endEl.getDate().before(startEl.getDate())) {
			endEl.setErrorKey("cal.form.error.endbeforebegin");
			allOk &= false;
		}
		
		boolean hasEnd = !chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE);
		recurrenceEnd.clearError();
		if (hasEnd && recurrenceEnd.getDate() == null) {
			recurrenceEnd.setErrorKey("cal.form.error.date");
			allOk &= false;
		}
		
		if (hasEnd && recurrenceEnd.getDate() != null && startEl.getDate() != null
				&& recurrenceEnd.getDate().before(startEl.getDate())) {
			recurrenceEnd.setErrorKey("cal.form.error.endbeforebegin");
			allOk &= false;
		}
		
		return allOk;
	}

	/**
	 * Get event with updated values.
	 */
	public KalendarEvent getUpdatedKalendarEvent() {
		// subject
		event.setSubject(subjectEl.getValue());
		// description
		String description = StringHelper.xssScan(descriptionEl.getValue());
		event.setDescription(description);
		// location
		event.setLocation(locationEl.getValue());
		// color
		event.setColor(CalendarColors.colorFromColorClass(colorCssClass));

		// date / time
		event.setBegin(startEl.getZonedDateTime());
		event.setEnd(endEl.getZonedDateTime());
		event.setLastModified(new Date().getTime());
		if(event.getCreated() == 0) {
			event.setCreated(new Date().getTime());
		}
		
		// allday event?
		event.setAllDayEvent(allDayEvent.isOn());

		// classification
		switch (classification.getSelected()) {
			case 0 -> event.setClassification(KalendarEvent.CLASS_PRIVATE);
			case 1 -> event.setClassification(KalendarEvent.CLASS_X_FREEBUSY);
			case 2 -> event.setClassification(KalendarEvent.CLASS_PUBLIC);
			default ->
					throw new OLATRuntimeException("getSelected() in KalendarEntryForm.classification returned weird value", null);
		}

		// recurrence
		if (chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE)) {
			event.setRecurrenceRule(null);
		} else {
			String rrule = calendarManager.getRecurrenceRule(chooseRecurrence.getSelectedKey(), recurrenceEnd.getDate());
			event.setRecurrenceRule(rrule);
		}
		
		// Live Stream
		event.setLiveStreamUrlTemplateKey(null);
		if (liveStreamUrlTemplateEl.isVisible() && liveStreamUrlTemplateEl.isEnabled() && liveStreamUrlTemplateEl.isOneSelected()) {
			event.setLiveStreamUrlTemplateKey(Long.valueOf(liveStreamUrlTemplateEl.getSelectedKey()));
			event.setLiveStreamUrl(getLiveStreamUrlFromSelection());
		}
		if (liveStreamUrlEl.isVisible() && liveStreamUrlEl.isEnabled()) {
			String liveStreamUrl = StringHelper.containsNonWhitespace(liveStreamUrlEl.getValue())
					? liveStreamUrlEl.getValue()
					: null;
			event.setLiveStreamUrl(liveStreamUrl);
		}

		return event;
	}


	public String getChoosenKalendarID() {
		if (chooseCalendar == null) {
			return chosenWrapper.getKalendar().getCalendarID();
		}
		return chooseCalendar.getSelectedKey();
	}

	public Map<String, String> getCalendarIdToCalendarType() {
		return calendarIdToCalendarType;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("cal.form.title");
		setFormContextHelp("manual_user/personal_menu/Calendar/");
		
		chooseCalendar = uifactory.addDropdownSingleselect("cal.form.chooseCalendar", formLayout, calendarKeys, calendarValues, null);
		if(chosenWrapper != null) {
			chooseCalendar.select(chosenWrapper.getKalendar().getCalendarID(), true);
		} else {
			chooseCalendar.select(calendarKeys[0], true);
		}
		chooseCalendar.addActionListener(FormEvent.ONCHANGE);
		chooseCalendar.setVisible(isNew);
		if(event.getManagedFlags() != null && event.getManagedFlags().length > 0) {
			chooseCalendar.setEnabled(false);
		}

		String calName = chosenWrapper == null ? "" : StringHelper.escapeHtml(chosenWrapper.getDisplayName());
		StaticTextElement calendarName = uifactory.addStaticTextElement("calendarname", "cal.form.calendarname", calName, formLayout);
		calendarName.setVisible(!isNew);
		
		boolean fb = readOnly && event.getClassification() == KalendarEvent.CLASS_X_FREEBUSY;
		String subject = fb ? translate("cal.form.subject.hidden") : event.getSubject();
		if(subject != null && subject.length() > 64) {
			subjectEl = uifactory.addTextAreaElement("subject", "cal.form.subject", -1, 3, 40, true, false, subject, formLayout);
		} else {
			subjectEl = uifactory.addTextElement("subject", "cal.form.subject", 255, subject, formLayout);
		}
		subjectEl.setMandatory(true);
		subjectEl.setNotEmptyCheck("cal.form.error.mandatory");
		subjectEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.subject));
		subjectEl.setElementCssClass("o_sel_cal_subject");

		boolean managedDates = CalendarManagedFlag.isManaged(event, CalendarManagedFlag.dates);
		allDayEvent = uifactory.addToggleButton("allday", null, null, null, formLayout);
		if (event.isAllDayEvent()) {
			allDayEvent.toggleOn();
		}
		allDayEvent.addActionListener(FormEvent.ONCHANGE);
		allDayEvent.setEnabled(!managedDates);
		allDayEvent.setElementCssClass("o_sel_cal_all_day");
		
		startEl = uifactory.addDateChooser("start", "cal.form.begin", null, formLayout);
		startEl.setDisplaySize(21);
		startEl.setDateChooserTimeEnabled(!event.isAllDayEvent());
		startEl.setValidDateCheck("form.error.date");
		startEl.setMandatory(true);
		startEl.setZonedDateTime(event.getBegin());
		startEl.setEnabled(!managedDates);
		startEl.setElementCssClass("o_sel_cal_begin");
		
		endEl = uifactory.addDateChooser("end", "cal.form.end", null, formLayout);
		endEl.setDisplaySize(21);
		endEl.setDateChooserTimeEnabled(!event.isAllDayEvent());
		endEl.setValidDateCheck("form.error.date");
		endEl.setMandatory(true);
		endEl.setZonedDateTime(event.getEnd());
		endEl.setEnabled(!managedDates);
		endEl.setElementCssClass("o_sel_cal_end");
		
		chooseRecurrence = uifactory.addDropdownSingleselect("cal.form.recurrence", formLayout, keysRecurrence, valuesRecurrence, null);
		String currentRecur = CalendarUtils.getRecurrence(event.getRecurrenceRule());
		boolean rk = currentRecur != null && !currentRecur.equals("");
		chooseRecurrence.select(rk ? currentRecur:RECURRENCE_NONE, true);
		chooseRecurrence.addActionListener(FormEvent.ONCHANGE);
		chooseRecurrence.setEnabled(!managedDates);
		chooseRecurrence.setVisible(!StringHelper.containsNonWhitespace(event.getRecurrenceID()));
		
		recurrenceEnd = uifactory.addDateChooser("cal.form.recurrence.end", "cal.form.recurrence.end", null, formLayout);
		recurrenceEnd.setDisplaySize(21);
		recurrenceEnd.setDateChooserTimeEnabled(false);
		recurrenceEnd.setMandatory(true);
		recurrenceEnd.setElementCssClass("o_sel_cal_until");
		ZonedDateTime recurEnd = calendarManager.getRecurrenceEndDate(event.getRecurrenceRule());
		if(recurEnd != null) {
			recurrenceEnd.setZonedDateTime(recurEnd);
		}
		recurrenceEnd.setEnabled(!managedDates);
		recurrenceEnd.setVisible(!chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE));

		String location = fb ? translate("cal.form.location.hidden") : event.getLocation();
		if(location != null && location.length() > 64) {
			locationEl = uifactory.addTextAreaElement("location", "cal.form.location", -1, 3, 40, true, false, location, formLayout);
		} else {
			locationEl = uifactory.addTextElement("location", "cal.form.location", 255, location, formLayout);
		}
		locationEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.location));
		locationEl.setElementCssClass("o_sel_cal_location");

		FormLayoutContainer colorLinks = FormLayoutContainer.createCustomFormLayout("color.links", getTranslator(), velocity_root + "/event_color.html");
		colorLinks.setRootForm(mainForm);
		colorLinks.setLabel("cal.form.event.color", null);
		formLayout.add(colorLinks);

		List<String> colorNames = CalendarColors.getColorsList();
		List<ColorPickerElement.Color> colors = ColorUIFactory.createColors(colorNames, getLocale(), "o_cal_");

		colorPickerEl = uifactory.addColorPickerElement("color", "cal.form.event.color", formLayout,
				colors);
		colorPickerEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.color));
		colorPickerEl.addActionListener(FormEvent.ONCHANGE);

		colorResetLink = uifactory.addFormLink("resetColor", "cal.form.event.color.reset", "", formLayout, Link.BUTTON);
		colorPickerEl.setResetButtonId(colorResetLink.getFormDispatchId());
		doUpdateColor(event);

		String description = event.getDescription();
		descriptionEl = uifactory.addTextAreaElement("description", "cal.form.description", -1, 3, 40, true, false, description, formLayout);
		descriptionEl.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.description));
		descriptionEl.setElementCssClass("o_sel_cal_description");

		classification = uifactory.addDropdownSingleselect("classification", "cal.form.class", formLayout, classKeys, classValues);
		classification.setHelpUrlForManualPage("manual_user/personal_menu/Calendar/#visibility");
		classification.setHelpTextKey("cal.form.class.hover", null);
		classification.setEnabled(!CalendarManagedFlag.isManaged(event, CalendarManagedFlag.classification));
		setClassification(event);

		liveStreamUrlTypeEl = uifactory.addCheckboxesHorizontal("cal.live.stream.url.type", formLayout, emptyStrings(), emptyStrings());
		liveStreamUrlTypeEl.addActionListener(FormEvent.ONCHANGE);

		liveStreamUrlTemplateEl = uifactory.addDropdownSingleselect("cal.live.stream.url.template", formLayout, emptyStrings(), emptyStrings());
		liveStreamUrlTemplateEl.addActionListener(FormEvent.ONCHANGE);

		liveStreamUrlEl = uifactory.addTextElement("cal.live.stream.url", 2000, event.getLiveStreamUrl(), formLayout);
		updateLiveStreamUI(event);

		StringBuilder buf = new StringBuilder();
		if (event.getCreated() != 0) {
			buf.append(StringHelper.formatLocaleDateTime(event.getCreated(), getTranslator().getLocale()));
			if (event.getCreatedBy() != null && !event.getCreatedBy().equals("")) {
				buf.append(" ");
				buf.append(getTranslator().translate("cal.form.created.by"));
				buf.append(" ");
				buf.append(StringHelper.escapeHtml(event.getCreatedBy()));
			} 
		} else {
			buf.append("-");
		}
		uifactory.addStaticTextElement("cal.form.created.label", buf.toString(), formLayout);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton(SUBMIT_SINGLE, "cal.form.submitSingle", buttonLayout);
		
		if (readOnly) {
			flc.setEnabled(false);
		}  else if(!isNew) {
			deleteEventButton = uifactory.addFormLink("delete", "cal.edit.delete", null, buttonLayout, Link.BUTTON);
			deleteEventButton.setElementCssClass("o_sel_cal_delete");
		}
	}

	private void setClassification(KalendarEvent event) {
		switch (event.getClassification()) {
			case KalendarEvent.CLASS_X_FREEBUSY -> classification.select("1", true);
			case KalendarEvent.CLASS_PUBLIC -> classification.select("2", true);
			default -> classification.select("0", true);
		}
	}

	private void updateLiveStreamUI(KalendarEvent kalendarEvent) {
		boolean isLiveStream = CalendarController.CALLER_LIVE_STREAM.equals(caller);
		boolean liveStreamNotManaged = !CalendarManagedFlag.isManaged(kalendarEvent, CalendarManagedFlag.liveStreamUrl);
		
		liveStreamUrlTypeEl.setVisible(isLiveStream && liveStreamNotManaged);
		liveStreamUrlTemplateEl.setVisible(isLiveStream && liveStreamNotManaged);
		
		liveStreamUrlEl.setValue(kalendarEvent.getLiveStreamUrl());

		boolean liveStreamUrlManually = true;
		if (liveStreamUrlTypeEl.isVisible()) {
			List<UrlTemplate> urlTemplates = liveStreamService.getAllUrlTemplates();
			if (urlTemplates.isEmpty()) {
				liveStreamUrlTypeEl.setVisible(false);
				liveStreamUrlTemplateEl.setVisible(false);
				return;
			}
			
			SelectionValues typeKV = new SelectionValues();
			typeKV.add(SelectionValues.entry("key", translate("cal.live.stream.url.type.manually")));
			SelectionValues urlTemplateKV = new SelectionValues();
			liveStreamUrlTypeEl.setKeysAndValues(typeKV.keys(), typeKV.values());
			
			urlTemplates.forEach(urlTemplate -> urlTemplateKV.add(SelectionValues.entry(urlTemplate.getKey().toString(), urlTemplate.getName())));
			urlTemplateKV.sort(SelectionValues.VALUE_ASC);
			liveStreamUrlTemplateEl.setKeysAndValues(urlTemplateKV.keys(), urlTemplateKV.values(), null);
			liveStreamUrlTemplateEl.select(liveStreamUrlTemplateEl.getKey(0), true);
			
			Long urlTemplateKey = kalendarEvent.getLiveStreamUrlTemplateKey();
			if (isValidUrlTemplateKey(urlTemplateKey)) {
				liveStreamUrlTemplateEl.select(urlTemplateKey.toString(), true);
				liveStreamUrlManually = false;
			} else if (!StringHelper.containsNonWhitespace(kalendarEvent.getLiveStreamUrl())) {
				liveStreamUrlTemplateEl.select(liveStreamUrlTemplateEl.getKey(0), true);
				liveStreamUrlManually = false;
			}
			liveStreamUrlTypeEl.select(liveStreamUrlTypeEl.getKey(0), liveStreamUrlManually);
		}
		liveStreamUrlTemplateEl.setVisible(!liveStreamUrlManually);
		liveStreamUrlEl.setVisible(isLiveStream && liveStreamUrlManually);
		liveStreamUrlEl.setEnabled(liveStreamNotManaged);
	}

	private boolean isValidUrlTemplateKey(Long urlTemplateKey) {
		return urlTemplateKey != null 
				&& Arrays.stream(liveStreamUrlTemplateEl.getKeys()).anyMatch(key -> key.equals(urlTemplateKey.toString()));
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent e) {
		if (source == colorResetLink) {
			doSetColor(null);
		} else if (source == chooseCalendar) {
			doSetColor(this.colorCssClass);
		} else if (source == chooseRecurrence) {
			recurrenceEnd.setVisible(!chooseRecurrence.getSelectedKey().equals(RECURRENCE_NONE));
		} else if(allDayEvent == source) {
			boolean allDay = allDayEvent.isOn();
			startEl.setDateChooserTimeEnabled(!allDay);
			endEl.setDateChooserTimeEnabled(!allDay);
		} else if (source == liveStreamUrlTypeEl) {
			boolean manually = liveStreamUrlTypeEl.isAtLeastSelected(1);
			liveStreamUrlTemplateEl.setVisible(!manually);
			liveStreamUrlEl.setVisible(manually);
		} else if (source == liveStreamUrlTemplateEl) {
			doSyncLiveStreamUrl();
		} else if(deleteEventButton == source) {
			fireEvent(ureq, new Event("delete"));
		} else if (source == colorPickerEl) {
			colorCssClass = CalendarColors.colorClassFromColor(colorPickerEl.getColor().id());
			doSetColor(colorCssClass);
		}
	}

	private void doSyncLiveStreamUrl() {
		String liveStreamUrl = getLiveStreamUrlFromSelection();
		liveStreamUrlEl.setValue(liveStreamUrl);
	}

	private String getLiveStreamUrlFromSelection() {
		Long key = Long.valueOf(liveStreamUrlTemplateEl.getSelectedKey());
		UrlTemplate urlTemplate = liveStreamService.getUrlTemplate(key);
		String url = liveStreamService.concatUrls(urlTemplate);
		return StringHelper.containsNonWhitespace(url) ? url : null;
	}
	
	private void doUpdateColor(KalendarEvent kalendarEvent) {
		String colorCssClass = StringHelper.containsNonWhitespace(kalendarEvent.getColor())
				? "o_cal_".concat(kalendarEvent.getColor()) : null;
		doSetColor(colorCssClass);
	}
	
	private void doSetColor(String colorCssClass) {
		this.colorCssClass = colorCssClass;

		boolean canReset = !CalendarManagedFlag.isManaged(event, CalendarManagedFlag.color)
				&& StringHelper.containsNonWhitespace(colorCssClass);
		colorResetLink.setVisible(canReset);
		
		String color = null;
		if (StringHelper.containsNonWhitespace(colorCssClass)) {
			color = CalendarColors.colorFromColorClass(colorCssClass);
		} else if (chosenWrapper != null) {
			color = CalendarColors.colorFromColorClass(chosenWrapper.getCssClass());
		} else {
			String kalendarID = getChoosenKalendarID();
			Optional<KalendarRenderWrapper> selectedWrapper = writeableCalendars.stream()
					.filter(cal -> cal.getKalendar().getCalendarID().equals(kalendarID))
					.findFirst();
			if (selectedWrapper.isPresent()) {
				color = CalendarColors.colorFromColorClass(selectedWrapper.get().getCssClass());
			}
		}
		colorPickerEl.setColor(color);
	}
}
