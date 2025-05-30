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
package org.olat.selenium.page.course;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 11 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsPage {

	private final WebDriver browser;
	
	public TeamsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Assert on the wrapper of the meetings lists.
	 * 
	 * @return Itself
	 */
	public TeamsPage assertOnRuntime() {
		By runtimeBy = By.className("o_sel_teams_overview");
		OOGraphene.waitElement(runtimeBy, browser);
		return this;
	}
	
	/**
	 * The list of meetings to configure.
	 * 
	 * @return Itself
	 */
	public TeamsPage selectEditMeetingsList() {
		By editListBy = By.cssSelector("a.o_sel_teams_edit_meetings_segment");
		OOGraphene.waitElement(editListBy, browser).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(By.className("o_sel_teams_edit_meetings_list"), browser);
		return this;
	}	
	
	/**
	 * Add a single meeting.
	 * 
	 * @param name The name of the meeting
	 * @param template The name of the template
	 * @return Itself
	 */
	public TeamsPage addSingleMeeting(String name) {
		openCreateDropDown();
		
		By addSingleMeetingBy = By.cssSelector("a.o_sel_teams_single_meeting_add");
		OOGraphene.waitElement(addSingleMeetingBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		
		By nameBy = By.cssSelector(".o_sel_teams_edit_meeting_subject input[type='text']");
		OOGraphene.waitElement(nameBy, browser).sendKeys(name);
		
		By saveBy = By.cssSelector("fieldset.o_sel_teams_edit_meeting button.btn.btn-primary");
		OOGraphene.moveAndClick(saveBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public TeamsPage addMultipleWeeklyMeetings(String name) {
		openCreateDropDown();
		
		By addWeeklyMeetingBy = By.cssSelector("a.o_sel_teams_weekly_meeting_add");
		OOGraphene.waitElement(addWeeklyMeetingBy, browser).click();
		OOGraphene.waitModalWizard(browser);
		
		By nameBy = By.cssSelector("fieldset.o_sel_teams_recurring_form div.o_sel_teams_recurring_meeting_name input[type='text']");
		OOGraphene.waitElement(nameBy, browser).sendKeys(name);
		
		By startBy = By.cssSelector("div.o_sel_teams_recurring_meeting_start span.input-group-addon i");
		browser.findElement(startBy).click();
		OOGraphene.selectNextMonthInDatePicker(browser);
		OOGraphene.selectDayInDatePicker(3, browser);
		
		By endBy = By.cssSelector("div.o_sel_teams_recurring_meeting_end span.input-group-addon i");
		browser.findElement(endBy).click();
		OOGraphene.selectNextMonthInDatePicker(browser);
		OOGraphene.selectDayInDatePicker(25, browser);
		
		return this;
	}
	
	public TeamsPage nextToDatesList() {
		OOGraphene.nextStep(browser);
		By datesBy = By.cssSelector("div.o_sel_teams_recurring_meeting_dates");
		OOGraphene.waitElement(datesBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @param numOfMeetings Num. of meetings + 1 (for the headers)
	 * @return Itself
	 */
	public TeamsPage assertOnDatesList(int numOfMeetings) {
		By datesBy = By.cssSelector("div.o_sel_teams_recurring_meeting_dates table tr");
		OOGraphene.waitElement(datesBy, browser);
		
		List<WebElement> lines = browser.findElements(datesBy);
		Assert.assertEquals(numOfMeetings, lines.size());
		
		return this;
	}
	
	public TeamsPage finishRecurringMeetings() {
		OOGraphene.finishStep(browser);
		return this;
	}
	
	private TeamsPage openCreateDropDown() {
		By addMenuCaretBy = By.cssSelector("button.o_sel_teams_meeting_add");
		OOGraphene.waitElement(addMenuCaretBy, browser).click();
		
		By addMenuBy = By.cssSelector("ul.o_sel_teams_meeting_add");
		OOGraphene.waitElement(addMenuBy, browser);
		return this;
	}
	
	/**
	 * The list of meetings for the end users.
	 * 
	 * @return Itself
	 */
	public TeamsPage selectMeetingsList() {
		By editListBy = By.cssSelector("a.o_sel_teams_meetings_segment");
		OOGraphene.waitElement(editListBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Check if the meeting with the specified name is in the list (edit or run);
	 * @param meetingName The name of the meeting
	 * @return Itself
	 */
	public TeamsPage assertOnList(String meetingName) {
		By meetingBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//td[contains(@class,'o_dnd_label')][text()[contains(.,'" + meetingName + "')]]");
		OOGraphene.waitElement(meetingBy, browser);
		return this;
	}
	
	public TeamsPage assertOnList(String meetingName, int numOfMeetings) {
		By meetingBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//td[contains(@class,'o_dnd_label')][text()[contains(.,'" + meetingName + "')]]");
		OOGraphene.waitElement(meetingBy, browser);
		List<WebElement> meetings = browser.findElements(meetingBy);
		Assert.assertEquals(numOfMeetings, meetings.size());
		return this;
	}
	
	/**
	 * Select a meeting in the run list.
	 * 
	 * @param meetingName The name of the meeting to select
	 * @return Itself
	 */
	public TeamsPage selectMeeting(String meetingName) {
		By selectBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//tr[td[text()[contains(.,'" + meetingName + "')]]]/td/a[contains(@onclick,'select')]");
		OOGraphene.waitElement(selectBy, browser).click();
		By titleBy = By.xpath("//div[@class='o_sel_teams_meeting']//h3[text()[contains(.,'" + meetingName + "')]]");
		OOGraphene.waitElementPresence(titleBy, 10, browser);
		return this;
	}
	
	/**
	 * Check that the meeting with the specified is on the start page.
	 * 
	 * @param meetingName The meeting name
	 * @return Itself
	 */
	public TeamsPage assertOnMeeting(String meetingName) {
		By titleBy = By.xpath("//div[@class='o_sel_teams_meeting']//h3[text()[contains(.,'" + meetingName + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	/**
	 * Check that the join button is visible.
	 * 
	 * @return Itself
	 */
	public TeamsPage assertOnJoinDisabled() {
		By joinBy = By.cssSelector("div.o_sel_teams_meeting a.btn.disabled.o_sel_teams_join");
		OOGraphene.waitElement(joinBy, browser);
		return this;
	}

}
