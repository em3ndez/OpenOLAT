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

package org.olat.commons.calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarUtils;

import net.fortuna.ical4j.transform.recurrence.Frequency;

public class KalendarEvent implements Cloneable, Comparable<KalendarEvent> {

	public static final int CLASS_PRIVATE = 0;
	public static final int CLASS_X_FREEBUSY = 1;
	public static final int CLASS_PUBLIC = 2;
	
	public static final String DAILY = Frequency.DAILY.name();
	public static final String WEEKLY = Frequency.WEEKLY.name();
	public static final String MONTHLY = Frequency.MONTHLY.name();
	public static final String YEARLY = Frequency.YEARLY.name();
	public static final String WORKDAILY = "WORKDAILY";
	public static final String BIWEEKLY = "BIWEEKLY";
	
	public static final String UNTIL = "UNTIL";
	public static final String COUNT = "COUNT";
	
	private String id;
	
	private Kalendar kalendar;
	
	private String subject;
	private String description;
	private ZonedDateTime begin;
	private ZonedDateTime end;
	private ZonedDateTime immutableBegin;
	private ZonedDateTime immutableEnd;
	private boolean isAllDayEvent;
	private String location;
	private String color;
	private List<KalendarEventLink> kalendarEventLinks;
	private long created;
	private long lastModified;
	private String createdBy;
	private int classification;
	
	private String comment;
	private Integer numParticipants;
	private String[] participants;
	private String sourceNodeId;

	private ZonedDateTime occurenceDate;
	private String recurrenceId;
	private String recurrenceRule;
	private String recurrenceExc;
	
	private Long liveStreamUrlTemplateKey;
	private String liveStreamUrl;
	
	private String externalId;
	private String externalSource;
	private CalendarManagedFlag[] managedFlags;

	public KalendarEvent() {
		// save no-args constructor for XStream
	}
	
	/**
	 * Create a new calendar event with the given subject and
	 * given start and end times as UNIX timestamps.
	 * @param subject
	 * @param begin
	 * @param end
	 */
	public KalendarEvent(String id, String recurrenceId, String subject, ZonedDateTime begin, ZonedDateTime end) {
		this.id = id;
		this.recurrenceId = recurrenceId;
		this.subject = subject;
		this.begin = begin;
		immutableBegin = begin;
		this.end = end;
		immutableEnd = end;
		isAllDayEvent = false;
		kalendarEventLinks = new ArrayList<>();
	}
	
	/**
	 * Create a new calendar entry with the given subject, starting at
	 * <begin> and with a duration of <duration> milliseconds.
	 * @param subject
	 * @param begin
	 * @param duration
	 */
	public KalendarEvent(String id, String subject, ZonedDateTime begin, int duration) {
		this.id = id;
		this.subject = subject;
		this.begin = begin;
		immutableBegin = begin;
		end = begin.plus(duration, ChronoUnit.MILLIS);
		immutableEnd = end;
		isAllDayEvent = false;
		kalendarEventLinks = new ArrayList<>();
	}
	
	public void setKalendar(Kalendar kalendar) {
		this.kalendar = kalendar;
	}
	
	public String getID() {
		return id;
	}
	
	public String getRecurrenceID() {
		return recurrenceId;
	}
	
	public void setRecurrenceID(String recurrenceId) {
		this.recurrenceId = recurrenceId;
	}
	
	/**
	 * The occurence date is the date calculated by the algorithm
	 * from ical4j which list of the events of recurring event.
	 * 
	 * @return
	 */
	public ZonedDateTime getOccurenceDate() {
		return occurenceDate;
	}
	
	public void setOccurenceDate(ZonedDateTime occurenceDate) {
		this.occurenceDate = occurenceDate;
	}
	
	public ZonedDateTime getBegin() {
		return begin;
	}
	
	public void setBegin(ZonedDateTime begin) {
		this.begin = begin;
	}
	
	/**
	 * This value is the original read value from the calendar
	 * before any changes by the UI. It serves to compare changes
	 * made on the event.
	 * 
	 * @return The original begin date of the event.
	 */
	public ZonedDateTime getImmutableBegin() {
		return immutableBegin;
	}
	
	/**
	 * This value is the original read value from the calendar
	 * before any changes by the UI. It serves to compare changes
	 * made on the event.
	 * 
	 * @return The original begin date of the event.
	 */
	public ZonedDateTime getImmutableEnd() {
		return immutableEnd;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ZonedDateTime getEnd() {
		return end;
	}
	
	public void setEnd(ZonedDateTime end) {
		this.end = end;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public boolean isManaged() {
		return managedFlags != null && managedFlags.length > 0;
	}

	public CalendarManagedFlag[] getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(CalendarManagedFlag[] managedFlags) {
		this.managedFlags = managedFlags;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalSource() {
		return externalSource;
	}

	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}

	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Kalendar getCalendar() {
		return kalendar;
	}

	public boolean isAllDayEvent() {
		return isAllDayEvent;
	}

	public void setAllDayEvent(boolean isAllDayEvent) {
		this.isAllDayEvent = isAllDayEvent;
	}
	
	public boolean isToday() {
		LocalDate now = LocalDate.now();
		boolean today = begin.toLocalDate().equals(now);
		if(today && end != null) {
			today &= end.toLocalDate().equals(now);
		}
		return today;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isWithinOneDay() {
		boolean oneDay = false;
		if(end == null) {
			//an event without end date finish the same day (3.6.1. Event Component, https://tools.ietf.org/html/rfc5545#section-3.6.1)
			oneDay = true; //if a duration, the constructor make it an end date
		} else {
			oneDay = (end.getDayOfYear() - begin.getDayOfYear() == 0)
					&& begin.getYear() == end.getYear();
		}
		return oneDay;
	}

	/**
	 * @return Returns the uRI.
	 */
	public List<KalendarEventLink> getKalendarEventLinks() {
		if(kalendarEventLinks == null) {
			kalendarEventLinks = new ArrayList<>(2);
		}
		return kalendarEventLinks;
	}

	/**
	 * @param uri The uRI to set.
	 */
	public void setKalendarEventLinks(List<KalendarEventLink> kalendarEventLinks) {
		this.kalendarEventLinks = kalendarEventLinks;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getNumParticipants() {
		return numParticipants;
	}

	public void setNumParticipants(Integer numParticipants) {
		this.numParticipants = numParticipants;
	}

	public String[] getParticipants() {
		return participants;
	}

	public void setParticipants(String[] participants) {
		this.participants = participants;
	}

	public String getSourceNodeId() {
		return sourceNodeId;
	}

	public void setSourceNodeId(String sourceNodeId) {
		this.sourceNodeId = sourceNodeId;
	}

	public String getRecurrenceRule() {
		return recurrenceRule;
	}
	
	public void setRecurrenceRule(String recurrenceRule) {
		this.recurrenceRule = recurrenceRule;
	}

	public String getRecurrenceExc() {
		return recurrenceExc;
	}

	public void setRecurrenceExc(String recurrenceExc) {
		this.recurrenceExc = recurrenceExc;
	}
	
	public Long getLiveStreamUrlTemplateKey() {
		return liveStreamUrlTemplateKey;
	}

	public void setLiveStreamUrlTemplateKey(Long liveStreamUrlTemplateKey) {
		this.liveStreamUrlTemplateKey = liveStreamUrlTemplateKey;
	}

	public String getLiveStreamUrl() {
		return liveStreamUrl;
	}

	public void setLiveStreamUrl(String liveStreamUrl) {
		this.liveStreamUrl = liveStreamUrl;
	}

	public void addRecurrenceExc(ZonedDateTime excDate) {
		List<ZonedDateTime> excDates = CalendarUtils.getRecurrenceExcludeDates(recurrenceExc);
		excDates.add(excDate);
		String excRule = CalendarUtils.getRecurrenceExcludeRule(excDates);
		setRecurrenceExc(excRule);
	}
	
	/**
	 * Set the immutable dates equals to the begin and end dates.
	 */
	public void resetImmutableDates() {
		immutableBegin = begin;
		immutableEnd = end;
	}
	
	@Override
	public KalendarEvent clone() {
		Object c = null;
		try {
			c = super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		return (KalendarEvent)c;
	}

	@Override
	public int compareTo(KalendarEvent event1) {
		if(event1 == null) {
			return -1;
		}
		return getBegin().compareTo(event1.getBegin());
	}
}
