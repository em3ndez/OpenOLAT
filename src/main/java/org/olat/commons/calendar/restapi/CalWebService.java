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
package org.olat.commons.calendar.restapi;

import static org.olat.commons.calendar.restapi.CalendarWSHelper.hasReadAccess;
import static org.olat.commons.calendar.restapi.CalendarWSHelper.hasWriteAccess;
import static org.olat.commons.calendar.restapi.CalendarWSHelper.processEvents;
import static org.olat.commons.calendar.restapi.CalendarWSHelper.transfer;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 23.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag (name = "Calendar")
public class CalWebService {
	
	private final KalendarRenderWrapper calendar;
	
	public CalWebService(KalendarRenderWrapper calendar) {
		this.calendar = calendar;
	}
	
	@GET
	@Path("events")
	@Operation(summary = "List events from a calendar.", description = "Returns list of events from a specific calendar.")
	@ApiResponse(responseCode = "200", description = "Request was successful",
		content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EventVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = EventVO.class)))
		} 
	)
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "Not found")	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEventsByCalendar(@QueryParam("start")@Parameter(description = "Set the date for the earliest event") @DefaultValue("0") Integer start,
			@QueryParam("limit")  @Parameter(description = "Limit the amount of events to be returned") @DefaultValue("25") Integer limit,
			@QueryParam("onlyFuture") @DefaultValue("false") Boolean onlyFuture,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		if(calendar == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!hasReadAccess(calendar)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<EventVO> events = new ArrayList<>();
		Collection<KalendarEvent> kalEvents = calendar.getKalendar().getEvents();
		for(KalendarEvent kalEvent:kalEvents) {
			EventVO eventVo = new EventVO(kalEvent);
			events.add(eventVo);
		}

		return processEvents(events, onlyFuture, start, limit, httpRequest, request);
	}
	
	@DELETE
	@Path("events/{eventId}")
	@Operation(summary = "Delete specific event.", description = "Deletes a specific event in a calendar.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteEventByCalendar(@PathParam("eventId") String eventId,
			@Context HttpServletRequest httpRequest) {
		
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		if(calendar == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!hasWriteAccess(calendar)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		if(eventId == null) {
			return Response.ok().status(Status.NOT_FOUND).build();
		} else {
			KalendarEvent kalEvent = calendar.getKalendar().getEvent(eventId, null);
			if(kalEvent == null) {
				return Response.ok().status(Status.NOT_FOUND).build();
			} else {
				calendarManager.removeEventFrom(calendar.getKalendar(), kalEvent);
			}
		}

		return Response.ok().build();
	}
	
	@PUT
	@Path("event")
	@Operation(summary = "Put a specific event.", description = "Puts a specific event in a specific calendar.")
	@ApiResponse(responseCode = "200",description = "Ok")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putEventByCalendar(EventVO event, @Context HttpServletRequest httpRequest) {
		List<EventVO> events = Collections.singletonList(event);
		return addEventsByCalendar(events, httpRequest);
	}
	
	@PUT
	@Path("events")
	@Operation(summary = "Put specific events.", description = "Puts specific events in a specific calendar.")
	@ApiResponse(responseCode = "200", description = "Ok")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "Not found.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putEventsByCalendar(EventVO[] eventArray, @Context HttpServletRequest httpRequest) {
		List<EventVO> events = new ArrayList<>();
		for(EventVO event:eventArray) {
			events.add(event);
		}
		return addEventsByCalendar(events, httpRequest);
	}
	
	@POST
	@Path("event")
	@Operation(summary = "Post a specific event.", description = "Posts a specific event in a specific calendar.")
	@ApiResponse(responseCode = "200", description = "Ok")
	@ApiResponse(responseCode = "401", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postEventByCalendar(EventVO event, @Context HttpServletRequest httpRequest) {
		List<EventVO> events = Collections.singletonList(event);
		return addEventsByCalendar(events, httpRequest);
	}
	
	@POST
	@Path("events")
	@Operation(summary = "Post specific events.", description = "Posts specific events in a specific calendar.")
	@ApiResponse(responseCode = "200", description = "Ok.")
	@ApiResponse(responseCode = "401", description = "Not authorized.")
	@ApiResponse(responseCode = "404", description = "Not found.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postEventsByCalendar(EventVO[] eventArray, @Context HttpServletRequest httpRequest) {
		List<EventVO> events = new ArrayList<>();
		for(EventVO event:eventArray) {
			events.add(event);
		}
		return addEventsByCalendar(events, httpRequest);
	}
	
	private Response addEventsByCalendar(List<EventVO> events, HttpServletRequest httpRequest) {
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		if(calendar == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!hasWriteAccess(calendar)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<KalendarEvent> kalEventToAdd = new ArrayList<>();
		List<KalendarEvent> kalEventToUpdate = new ArrayList<>();
		CalendarModule calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		
		
		
		for(EventVO event:events) {
			KalendarEvent kalEvent;
			if(!StringHelper.containsNonWhitespace(event.getId())) {
				String id = UUID.randomUUID().toString();
				ZonedDateTime zBegin = DateUtils.toZonedDateTime(event.getBegin(), calendarModule.getDefaultZoneId());
				ZonedDateTime zEnd = DateUtils.toZonedDateTime(event.getEnd(), calendarModule.getDefaultZoneId());
				kalEvent = new KalendarEvent(id, event.getRecurrenceId(), event.getSubject(), zBegin, zEnd);
				transfer(event, kalEvent);
				kalEventToAdd.add(kalEvent);
			} else {
				kalEvent = calendar.getKalendar().getEvent(event.getId(), event.getRecurrenceId());
				if(kalEvent == null) {
					ZonedDateTime zBegin = DateUtils.toZonedDateTime(event.getBegin(), calendarModule.getDefaultZoneId());
					ZonedDateTime zEnd = DateUtils.toZonedDateTime(event.getEnd(), calendarModule.getDefaultZoneId());
					kalEvent = new KalendarEvent(event.getId(), event.getRecurrenceId(), event.getSubject(), zBegin, zEnd);
					transfer(event, kalEvent);
					kalEventToAdd.add(kalEvent);
				} else {
					kalEvent.setBegin(DateUtils.toZonedDateTime(event.getBegin()));
					kalEvent.setEnd(DateUtils.toZonedDateTime(event.getEnd()));
					kalEvent.setSubject(event.getSubject());
					transfer(event, kalEvent);
					kalEventToUpdate.add(kalEvent);
				}
			}

			// create default link to course/group if it does not exist already
			boolean hasLinkToResource = kalEvent.getKalendarEventLinks()
					.stream()
					.anyMatch(ke -> ke.getProvider().equals(RepositoryEntry.class.getSimpleName()) || ke.getProvider().equals(BusinessGroup.class.getSimpleName()));

			if (!hasLinkToResource) {
				if (calendar.getKalendar().getType().equals("course")) {
					Long resourceId = Long.valueOf(calendar.getKalendar().getCalendarID());
					RepositoryEntry re = repositoryManager.lookupRepositoryEntry(OresHelper.createOLATResourceableInstance("CourseModule", resourceId), false);
					if(re != null) {
						String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + re.getKey() + "]");
						KalendarEventLink courseCalEventLink = new KalendarEventLink(RepositoryEntry.class.getSimpleName(), url, calendar.getDisplayName(), url, "o_CourseModule_icon");
						kalEvent.getKalendarEventLinks().add(courseCalEventLink);
					}
				} else if (calendar.getKalendar().getType().equals("group")) {
					String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[BusinessGroup:" + calendar.getKalendar().getCalendarID() + "]");
					KalendarEventLink groupCalEventLink = new KalendarEventLink(BusinessGroup.class.getSimpleName(), url, calendar.getDisplayName(), url, "o_icon_group");
					kalEvent.getKalendarEventLinks().add(groupCalEventLink);
				}
			}
		}

		if(!kalEventToAdd.isEmpty()) {
			calendarManager.addEventTo(calendar.getKalendar(), kalEventToAdd);
		}
		if(!kalEventToUpdate.isEmpty()) {
			calendarManager.updateEventsFrom(calendar.getKalendar(), kalEventToUpdate);
		}
		return Response.ok().build();
	}
}
