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

import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 12.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolPage {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentToolPage.class);

	private final WebDriver browser;
	
	public AssessmentToolPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public AssessmentToolPage users() {
		By usersBy = By.cssSelector("a.o_sel_assessment_tool_assessed_users");
		browser.findElement(usersBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentToolPage assertOnUsers(UserVO user) {
		By usersCellsBy = By.cssSelector("div.o_table_flexi table tr td.text-left");
		List<WebElement> usersCellsList = browser.findElements(usersCellsBy);
		Assert.assertFalse(usersCellsList.isEmpty());
		
		boolean found = false;
		for(WebElement usersCell:usersCellsList) {
			found |= usersCell.getText().contains(user.getFirstName());
		}
		Assert.assertTrue(found);
		return this;
	}
	
	/**
	 * Select a user in "Users".
	 * 
	 * @param user
	 * @return
	 */
	public AssessmentToolPage selectUser(UserVO user) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//tr//td//a[text()[contains(.,'" + user.getFirstName() + "')]]");
		browser.findElement(userLinksBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * To see the list of course elements
	 * @return Itself
	 */
	public AssessmentToolPage courseElements() {
		By elementsBy = By.cssSelector("a.o_sel_assessment_tool_assessable_course_nodes");
		browser.findElement(elementsBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the course node in "Users" > "Course nodes".
	 * 
	 * @param nodeTitle The title of the course node
	 * @return Itself
	 */
	public AssessmentToolPage selectUsersCourseNode(String nodeTitle) {
		By rowsBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/span[contains(text(),'" + nodeTitle + "')]]/td/a[contains(@onclick,'cmd.select.node')]");
		OOGraphene.waitElement(rowsBy, browser);
		List<WebElement> rowEls = browser.findElements(rowsBy);
		Assert.assertEquals(1, rowEls.size());
		OOGraphene.scrollBottom(rowsBy, browser);
		browser.findElement(rowsBy).click();
		OOGraphene.waitElement(By.cssSelector("div.o_assessment_panel"), browser);
		return this;
	}
	
	/**
	 * Select the course node in the tree > "Course nodes".
	 * 
	 * @param nodeTitle The title of the course node
	 * @return Itself
	 */
	public AssessmentToolPage selectElementsCourseNode(String nodeTitle) {
		By elementBy = By.xpath("//div[contains(@class,'o_tree')]//ul//li[div/span/a/span[@class='o_tree_item'][contains(text(),'" + nodeTitle + "')]]/div/span/a[contains(@onclick,'nidle')]");
		OOGraphene.waitElement(elementBy, browser).click();
		By statsBy = By.cssSelector("div.panel.o_assessment_stats");
		OOGraphene.waitElement(statsBy, browser);
		return this;
	}
	
	/**
	 * Select the list of identities to assess.
	 * 
	 * @return Itself
	 */
	public AssessmentToolPage selectIdentitiesList() {
		By identitiesListSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_assessment_tool_node_participants");
		OOGraphene.waitElement(identitiesListSegmentBy, browser).click();
		By identitiesListBy = By.cssSelector("div.o_table_flexi.o_sel_assessment_tool_table");
		OOGraphene.waitElement(identitiesListBy, browser);
		return this;
	}
	
	/**
	 * Check in "Users" > "Course nodes" if a specific course node
	 * is passed.
	 * 
	 * @param nodeTitle
	 * @return
	 */
	public AssessmentToolPage assertUserPassedCourseNode(String nodeTitle) {
		By rowsBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/span[contains(text(),'" + nodeTitle + "')]]");
		List<WebElement> rowEls = browser.findElements(rowsBy);
		Assert.assertEquals(1, rowEls.size());
		By passedBy = By.cssSelector("td div.o_state.o_passed");
		WebElement passedEl = rowEls.get(0).findElement(passedBy);
		Assert.assertTrue(passedEl.isDisplayed());
		return this;
	}
	

	public AssessmentToolPage assertUserSwissGradeCourseNode(String nodeTitle, String grade) {
		By gradeBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/span[contains(text(),'" + nodeTitle + "')]]/td/span/span[@class='o_grs_oo_grades_ch'][text()[contains(.,'" + grade + "')]]");
		OOGraphene.waitElement(gradeBy, browser);
		return this;
	}
	
	/**
	 * Set the score in the assessment form
	 * @param score
	 * @return
	 */
	public AssessmentToolPage setAssessmentScore(float score) {
		By scoreBy = By.xpath("//input[contains(@class,'o_sel_assessment_form_score')][@type='text']");
		browser.findElement(scoreBy).clear();
		browser.findElement(scoreBy).sendKeys(Float.toString(score));
		return this;
	}
	
	public AssessmentToolPage updateAssessmentScore(float score) {
		By scoreBy = By.xpath("//input[contains(@class,'o_sel_assessment_form_score')][@type='text']");
		browser.findElement(scoreBy).clear();
		OOGraphene.waitBusy(browser);
		browser.findElement(scoreBy).sendKeys(Float.toString(score));
		return this;
	}
	
	public AssessmentToolPage setAssessmentPassed(Boolean passed) {
		String val = passed == null ? "undefined" : passed.toString();
		By passedBy = By.cssSelector(".o_sel_assessment_form_passed input[type='radio'][value='" + val + "']");
		browser.findElement(passedBy).click();
		return this;
	}
	
	public AssessmentToolPage closeAndPublishAssessment() {
		By saveBy = By.cssSelector("button.btn.o_sel_assessment_form_save_and_done");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public AssessmentToolPage reopenAssessment() {
		By saveBy = By.cssSelector("a.o_sel_assessment_form_reopen");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		By assessmentPanelBy = By.cssSelector("div.o_personal.o_assessment_panel");
		OOGraphene.waitElement(assessmentPanelBy, browser);
		return this;
	}
	
	public AssessmentToolPage assertPassed(UserVO user) {
		By userInfosBy = By.xpath("//div[@class='o_user_info']//div[@class='o_user_info_profile']//div[@class='o_user_info_profile_name'][text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(userInfosBy, browser);
		
		By passedBy = By.cssSelector("div.o_table_wrapper table tr td.text-left div.o_state.o_passed");
		OOGraphene.waitElement(passedBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @param user The user to overview
	 * @param progress The progress in percent
	 * @return Itself
	 */
	public AssessmentToolPage assertProgress(UserVO user, int progress) {
		By progressBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div/div[@class='progress'][div[@title='" + progress + "%']]");
		By tableBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]");
		try {
			OOGraphene.waitElementWithScrollTableRight(tableBy, progressBy, Duration.ofSeconds(15), Duration.ofSeconds(1), browser);
		} catch (Exception e) {
			log.error("Assert progress", e);
			OOGraphene.takeScreenshot("Assert progress", browser);
			
			By progressBarBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div/div[@class='progress']/div[@class='progress-bar']");
			List<WebElement> progressBarEls = browser.findElements(progressBarBy);
			for(WebElement progressBarEl:progressBarEls) {
				log.error("Progress title _{}_", progressBarEl.getDomAttribute("title"));
			}
			OOGraphene.waitElementWithScrollTableRight(tableBy, progressBy, Duration.ofSeconds(5), Duration.ofMillis(200), browser);
		}
		return this;
	}
	
	/**
	 * Wait slowly that the user has passed the test.
	 * 
	 * @param user The assessed user
	 * @return Itself
	 */
	public AssessmentToolPage assertTablePassed(UserVO user) {
		By doneBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div/i[contains(@class,'o_icon_passed') and not(contains(@class,'o_icon_passed_undefined'))]");
		try {// This is an ugly hack to force chrome to update its window on Linux
			OOGraphene.waitElementPresenceSlowly(doneBy, 10, browser);
		} catch(Exception e) {
			log.warn("Try a second time, takes a screenshot for chrome on linux.");
			// On Linux, the screenshot update the window of Chrome
			OOGraphene.takeScreenshotInMemory(browser);
			OOGraphene.waitElementSlowly(doneBy, 10, browser);
		}
		return this;
	}
	
	/**
	 * The status done is off screen. It is important to wait first the passed status and
	 * after call this assert. The assert will move the window right to see and wait the done
	 * status.
	 * 
	 * @param user The assessed user
	 * @return Itself
	 */
	public AssessmentToolPage assertTableStatusDone(UserVO user) {
		try {
			By doneBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td[div/i[contains(@class,'o_icon_status_done')]]");
			OOGraphene.waitElementPresenceSlowly(doneBy, 10, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Status done", browser);
			throw e;
		}
		return this;
	}
	
	public AssessmentToolPage assertProgressEnded(UserVO user) {
		By progressBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div/div[@class='o_sel_ended']");
		OOGraphene.waitElement(progressBy, 10, browser);
		return this;
	}
	
	public AssessmentToolPage generateCertificate() {
		By userLinksBy = By.className("o_sel_certificate_generate");
		OOGraphene.waitElement(userLinksBy, browser).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);

		By certificateBy = By.xpath("//div[@class='o_achievement_card'][div[@class='o_achievement_type']/span[@class='o_sel_certificate_icon']]");
		OOGraphene.waitElementSlowly(certificateBy, 15, browser);
		return this;
	}
	
	public AssessmentToolPage awardBadge() {
		By awardBadgeBy = By.cssSelector("a.o_sel_award_badge");
		OOGraphene.waitElement(awardBadgeBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		
		By awardButtonBy = By.cssSelector(".o_sel_award_badge_form button.btn.btn-primary");
		browser.findElement(awardButtonBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public AssessmentToolPage assertOnBadge(String badgeName) {
		By badgeBy = By.xpath("//div[@class='o_achievement_card']/div[@class='o_achievement_meta']/a/span[text()[contains(.,'" + badgeName + "')]]");
		OOGraphene.waitElement(badgeBy, browser);
		return this;
	}
	
	public BulkAssessmentPage bulk() {
		By bulkBy = By.cssSelector("li.o_tool a.o_sel_assessment_tool_bulk");
		browser.findElement(bulkBy).click();
		OOGraphene.waitBusy(browser);
		
		By newBy = By.cssSelector("a.o_sel_assessment_tool_new_bulk_assessment");
		browser.findElement(newBy).click();
		OOGraphene.waitModalDialog(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_bulk_assessment_data"), browser);
		return new BulkAssessmentPage(browser);
	}
	
	public AssessmentToolPage makeAllVisible() {
		OOGraphene.flexiTableSelectAll(browser);
		
		By bulkBy = By.cssSelector("a.btn.o_sel_assessment_bulk_visible");
		OOGraphene.waitElement(bulkBy, browser).click();
		OOGraphene.waitBusy(browser);
		
		By visibleBy = By.xpath("//table//span[i[contains(@class,'o_icon_results_visible')]]");
		OOGraphene.waitElement(visibleBy, browser);
		return this;
	}
	
	/**
	 * Click back to the course
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarRootCrumb() {
		try {
			OOGraphene.scrollTop(browser);
			By firstCrumbBy = By.cssSelector("ol.breadcrumb>li:not(.o_display_none).o_breadcrumb_root>a");
			OOGraphene.waitElement(firstCrumbBy, browser).click();
			
			By lastCrumbBy = By.cssSelector("ol.breadcrumb>li:not(.o_display_none).o_last_crumb>a");
			OOGraphene.waitElementDisappears(lastCrumbBy, 5, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Assessment root crumb", browser);
			throw e;
		}
		return new CoursePageFragment(browser);
	}
	
	/**
	 * 
	 * @return
	 */
	public AssessmentToolPage assertOnInspectionsMenuItem() {
		By menuItemBy = By.cssSelector("ul.o_tree_l0 li a>i.o_icon_inspection");
		OOGraphene.waitElement(menuItemBy, browser);
		return this;
	}
	
	public AssessmentInspectionConfigurationPage selectAssessmentInspections() {
		By menuItemBy = By.xpath("//ul[contains(@class,'o_tree_l0')]/li//a[i[contains(@class,'o_icon_inspection')]]");
		OOGraphene.waitElement(menuItemBy, browser);
		browser.findElement(menuItemBy).click();

		By overviewBy = By.cssSelector("fieldset.o_sel_assessment_inspection_overview");
		OOGraphene.waitElement(overviewBy, browser);
		return new AssessmentInspectionConfigurationPage(browser);
	}
}