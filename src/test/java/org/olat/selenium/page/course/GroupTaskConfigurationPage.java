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

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the configuration of the course element of type group task.
 * 
 * Initial date: 03.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupTaskConfigurationPage {

	private final WebDriver browser;
	
	public GroupTaskConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GroupTaskConfigurationPage selectWorkflow() {
		return selectTab("o_sel_gta_workflow", By.className("o_sel_course_gta_steps"));
	}
	
	public GroupTaskConfigurationPage optional(boolean optional) {
		By optionalBy;
		if(optional) {
			optionalBy = By.cssSelector("fieldset#o_coobligation input[type='radio'][value='optional']");
		} else {
			optionalBy = By.cssSelector("fieldset#o_coobligation input[type='radio'][value='mandatory']");
		}
		OOGraphene.waitElement(optionalBy, browser);
		OOGraphene.check(browser.findElement(optionalBy), Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage enableAssignment(boolean enable) {
		return enableStep("o_sel_gta_step_assignment", enable);
	}
	
	public GroupTaskConfigurationPage enableSubmission(boolean enable) {
		return enableStep("o_sel_gta_step_submission", enable);
	}
	
	public GroupTaskConfigurationPage enableReview(boolean enable) {
		return enableStep("o_sel_gta_step_feedback", enable);
	}
	
	public GroupTaskConfigurationPage enableRevision(boolean enable) {
		return enableStep("o_sel_gta_step_revision", enable);
	}
	
	public GroupTaskConfigurationPage enableSolution(boolean enable) {
		return enableStep("o_sel_gta_step_solution", enable);
	}
	
	public GroupTaskConfigurationPage enableGrading(boolean enable) {
		return enableStep("o_sel_gta_step_grading", enable);
	}
	
	private GroupTaskConfigurationPage enableStep(String elementCssClass, boolean enable) {
		String toggleStepSelector = "fieldset.o_sel_course_gta_steps div." + elementCssClass + " button." + elementCssClass;
		OOGraphene.toggle(toggleStepSelector, enable, true, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage enableSolutionForAll(boolean forAll) {
		By optionBy;
		if(forAll) {
			optionBy = By.xpath("//fieldset[@id='o_covisibleall']//label/input[@name='visibleall'][@value='all']");
		} else {
			optionBy = By.xpath("//fieldset[@id='o_covisibleall']//label/input[@name='visibleall'][@value='restricted']");
		}
		OOGraphene.waitElement(optionBy, browser).click();
		return this;
	}
	
	public GroupTaskConfigurationPage saveWorkflow() {
		By saveBy = By.cssSelector(".o_sel_course_gta_save_workflow button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage openBusinessGroupChooser() {
		By chooseGroupBy = By.cssSelector("a.o_form_groupchooser");
		browser.findElement(chooseGroupBy).click();
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage createBusinessGroup(String name) {
		By createGroupBy = By.cssSelector("div.modal-content div.o_button_group_right a.o_sel_create_new_group");
		browser.findElement(createGroupBy).click();
		OOGraphene.waitModalDialog(browser, "fieldset.o_sel_group_edit_group_form");
		OOGraphene.waitTinymce(browser);
		
		//fill the form
		By nameBy = By.cssSelector(".o_sel_group_edit_title input[type='text']");
		browser.findElement(nameBy).sendKeys(name);
		OOGraphene.tinymce("-", browser);
		
		//save the group
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		OOGraphene.click(submitBy, browser);
		OOGraphene.waitModalDialogWithFieldsetDisappears(browser, "o_sel_group_edit_group_form");
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage confirmBusinessGroupsSelection() {
		By saveBy = By.cssSelector(".o_sel_group_selection_groups button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		saveButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage selectAssessment() {
		return selectTab("o_sel_gta_assessment", By.className("o_sel_course_ms_form"));
	}
	
	public GroupTaskConfigurationPage selectAssignment() {
		return selectTab("o_sel_gta_assignment", By.className("o_sel_course_gta_tasks"));
	}
	
	public GroupTaskConfigurationPage selectSolution() {
		return selectTab("o_sel_gta_solution", By.className("o_sel_course_gta_solutions"));
	}
	
	public GroupTaskConfigurationPage uploadTask(String title, File file) {
		By addTaskBy = By.className("o_sel_course_gta_add_task");
		browser.findElement(addTaskBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector(".o_sel_course_gta_upload_task_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_task_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		//save
		By saveBy = By.cssSelector(".o_sel_course_gta_upload_task_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage enableAutoAssignment(boolean enable) {
		//task.assignment.type
		String type = enable ? "auto" : "manual";
		By typeBy = By.xpath("//fieldset[contains(@class,'o_sel_course_gta_task_config_form')]//input[@name='task.assignment.type'][@value='" + type + "']");
		OOGraphene.check(browser.findElement(typeBy), Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage saveTasks() {
		By saveBy = By.cssSelector(".o_sel_course_gta_task_config_buttons button.btn-primary");
		OOGraphene.waitElement(saveBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage uploadSolution(String title, File file) {
		By addTaskBy = By.className("o_sel_course_gta_add_solution");
		browser.findElement(addTaskBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By titleBy = By.cssSelector(".o_sel_course_gta_upload_solution_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_solution_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		//save
		By saveBy = By.cssSelector(".o_sel_course_gta_upload_solution_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskConfigurationPage setAssessmentOptions(float minVal, float maxVal, float cutVal) {
		new AssessmentCEConfigurationPage(browser).setScoreAuto(minVal, maxVal, cutVal);
		return this;
	}
	
	public GroupTaskConfigurationPage saveAssessmentOptions() {
		By saveBy = By.cssSelector(".o_sel_course_ms_form button.btn.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	private GroupTaskConfigurationPage selectTab(String tabCssClass, By panelBy) {
		By tabBy = By.cssSelector("ul.o_node_config li." + tabCssClass + ">a");
		OOGraphene.waitElement(tabBy, browser).click();
		OOGraphene.waitElement(panelBy, browser);
		return this;
	}
}
