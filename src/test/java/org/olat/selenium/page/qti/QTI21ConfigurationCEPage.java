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
package org.olat.selenium.page.qti;

import java.util.Calendar;
import java.util.Date;

import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 2 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ConfigurationCEPage {
	
	public static final By chooseTestButton = By.className("o_sel_re_reference_select");
	
	private final WebDriver browser;
	
	public QTI21ConfigurationCEPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public QTI21ConfigurationCEPage selectConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_repo_entry>a");
		OOGraphene.waitElement(tabBy, browser).click();
		OOGraphene.waitElement(By.className("o_qti_21_configuration"), browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage showScoreOnHomepage(boolean showResults) {
		By scoreBy = By.cssSelector(".o_sel_results_on_homepage select#o_fioqti_showresult_SELBOX");
		OOGraphene.waitElementPresence(scoreBy, 5, browser);
		OOGraphene.scrollBottom(scoreBy, browser);
		WebElement scoreEl = OOGraphene.waitElement(scoreBy, browser);
		String val = showResults ? "false" : "no";
		new Select(scoreEl).selectByValue(val);
		OOGraphene.waitBusy(browser);
		
		By levelBy = By.cssSelector("fieldset.o_sel_qti_show_results_options input[type='checkbox'][value='metadata']");
		if(showResults) {
			OOGraphene.waitElement(levelBy, browser);
		} else {
			OOGraphene.waitElementDisappears(levelBy, 5, browser);
		}
		return this;
	}
	
	public QTI21ConfigurationCEPage assertShowResultsOptions() {
		By optionsBy = By.cssSelector("fieldset.o_sel_qti_show_results_options label>input[type='checkbox']");
		OOGraphene.waitElement(optionsBy, browser);
		return this;
	}
	
	public QTI21ConfigurationCEPage showResultsOnHomepage(Boolean show, QTI21AssessmentResultsOptions options) {
		By showResultsBy = By.cssSelector("div.o_sel_qti_show_results input[type='checkbox']");
		WebElement showResultsEl = browser.findElement(showResultsBy);
		OOGraphene.check(showResultsEl, show);
		OOGraphene.waitBusy(browser);
		By resultsLevelCheckedBy = By.xpath("//div[contains(@class,'o_sel_qti_show_results')]//input[@type='checkbox'][@checked='checked']");
		OOGraphene.waitElement(resultsLevelCheckedBy, browser);

		if(options.isMetadata()) {
			By metadataBy = By.xpath("//fieldset[contains(@class,'o_sel_qti_show_results_options')]//input[@type='checkbox'][@value='metadata']");
			OOGraphene.waitElementRefreshed(metadataBy, browser).click();
			OOGraphene.waitBusy(browser);
		}
		if(options.isSectionSummary()) {
			By sectionSummaryBy = By.cssSelector("fieldset.o_sel_qti_show_results_options input[type='checkbox'][value='sectionsSummary']");
			OOGraphene.waitElementRefreshed(sectionSummaryBy, browser).click();
			OOGraphene.waitBusy(browser);
		}
		if(options.isQuestionSummary()) {
			By questionSummaryBy = By.cssSelector("fieldset.o_sel_qti_show_results_options input[type='checkbox'][value='questionSummary']");
			OOGraphene.waitElementRefreshed(questionSummaryBy, browser).click();
			OOGraphene.waitBusy(browser);
		}
		if(options.isUserSolutions()) {
			By userSolutionsBy = By.cssSelector("fieldset.o_sel_qti_show_results_options input[type='checkbox'][value='userSolutions']");
			OOGraphene.waitElementRefreshed(userSolutionsBy, browser).click();
			OOGraphene.waitBusy(browser);
		}
		if(options.isCorrectSolutions()) {
			By correctSolutionsBy = By.cssSelector("fieldset.o_sel_qti_show_results_options input[type='checkbox'][value='correctSolutions']");
			OOGraphene.waitElementRefreshed(correctSolutionsBy, browser).click();
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	/**
	 * Set the correction mode.
	 * 
	 * @param mode The mode defined by IQEditController.CORRECTION_AUTO,
	 * 		IQEditController.CORRECTION_MANUAL or IQEditController.CORRECTION_GRADING
	 * @return Itself
	 */
	public QTI21ConfigurationCEPage setCorrectionMode(String mode) {
		By correctionBy = By.xpath("//fieldset[contains(@class,'o_qti_21_correction')]//fieldset[@id='o_cocorrection_mode']//input[@value='" + mode + "'][@name='correction.mode'][@type='radio']");
		OOGraphene.waitElement(correctionBy, browser).click();
		return this;
	}
	
	public QTI21ConfigurationCEPage setTime(Date start, Date end) {
		OOGraphene.toggle("div.o_qti_21_datetest button.o_qti_21_datetest", true, false, browser);
		OOGraphene.waitModalDialog(browser);
		// confirm
		By confirmBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(confirmBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitingALittleLonger();
		OOGraphene.scrollBottom(By.cssSelector("div.o_qti_21_datetest_end"), browser);
		
		// set dates
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		setTime("o_qti_21_datetest_start", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
		cal.setTime(end);
		setTime("o_qti_21_datetest_end", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
		return this;
	}
	
	private QTI21ConfigurationCEPage setTime(String fieldClass, int hour, int minutes, boolean waitBusy) {
		try {
			By untilAltBy = By.cssSelector("div." + fieldClass + " div.o_date_picker span.input-group-addon i");
			OOGraphene.waitElement(untilAltBy, browser).click();
			OOGraphene.waitingALittleLonger();//SEL wait animation
			
			By todayBy = By.cssSelector("div." + fieldClass + " div.datepicker-dropdown.active span.datepicker-cell.day.focused");
			OOGraphene.waitElementRefreshed(todayBy, browser).click();
			
			By activePickerBy = By.cssSelector("div." + fieldClass + " div.datepicker-dropdown.active");
			OOGraphene.waitElementDisappears(activePickerBy, 5, browser);
			OOGraphene.waitingLong();//SEL wait animation
		
			if(waitBusy) {
				OOGraphene.waitBusy(browser);
			}
			
			By hourBy = By.xpath("//div[contains(@class,'" + fieldClass + "')]//div[contains(@class,'o_first_ms')]/input[contains(@id,'o_dch_o_')]");
			WebElement hourEl = browser.findElement(hourBy);
			hourEl.clear();
			hourEl.sendKeys(Integer.toString(hour));

			By minuteBy = By.xpath("//div[contains(@class,'" + fieldClass + "')]//div[contains(@class,'o_first_ms')]/input[contains(@id,'o_dcm_o_')]");
			WebElement minuteEl = browser.findElement(minuteBy);
			minuteEl.clear();
			minuteEl.sendKeys(Integer.toString(minutes));
		} catch (Exception | Error e) {
			OOGraphene.takeScreenshot("Datetest", browser);
			throw e;
		}
		return this;
	}
	
	public QTI21ConfigurationCEPage saveConfiguration() {
		By saveBy = By.cssSelector(".o_qti_21_configuration button");
		browser.findElement(saveBy).click();
		By dirtySaveBy = By.xpath("//fieldset[contains(@class,'o_qti_21_configuration')]//button[contains(@class,'btn-primary') and not(contains(@class,'o_button_dirty'))]");
		OOGraphene.waitElement(dirtySaveBy, browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	public QTI21LayoutConfigurationCEPage selectLayoutConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_qti_layout>a");
		OOGraphene.waitElement(tabBy, browser).click();
		OOGraphene.waitElement(By.className("o_qti_21_layout_configuration"), browser);
		return new QTI21LayoutConfigurationCEPage(browser);
	}
	
	public QTI21ConfigurationCEPage selectLearnContent() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_repo_entry>a");
		OOGraphene.waitElement(tabBy, browser).click();
		OOGraphene.waitElement(chooseTestButton, browser);
		return this;
	}

	public QTI21ConfigurationCEPage chooseTest(String resourceTitle) {
		return chooseTest(resourceTitle, true);
	}
	
	public QTI21ConfigurationCEPage chooseSelfTest(String resourceTitle) {
		return chooseTest(resourceTitle, false);
	}
	
	private QTI21ConfigurationCEPage chooseTest(String resourceTitle, boolean closeInfoBox) {
		CourseEditorPageFragment fragment = new CourseEditorPageFragment(browser);
		fragment.chooseResourceModern(chooseTestButton, resourceTitle);
		if(closeInfoBox) {
			OOGraphene.waitAndCloseBlueMessageWindow(browser);
		}
		return this;
	}
}
