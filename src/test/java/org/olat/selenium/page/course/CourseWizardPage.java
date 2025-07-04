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
 * Drive the wizard to create a course.
 * 
 * Initial date: 04.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseWizardPage {
	
	private final WebDriver browser;
	
	public CourseWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static CourseWizardPage getWizard(WebDriver browser) {
		return new CourseWizardPage(browser);
	}
	
	/**
	 * Click next on the step which selects the course elements.
	 * 
	 * @return Itself
	 */
	public CourseWizardPage nextNodes() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_repo_access_configuration"), browser);
		return this;
	}
	
	/**
	 * Finish the wizard
	 * @return this
	 */
	public CourseWizardPage finish() {
		OOGraphene.finishStep(browser);
		return this;
	}
	
	public CourseWizardPage selectAllCourseElements() {
		By elementslBy = By.cssSelector("dialog.modal div.o_sel_course_elements");
		OOGraphene.waitElement(elementslBy, browser);
		
		By checkAllBy = By.cssSelector("dialog.modal div.o_sel_course_elements input[type='checkbox']");
		List<WebElement> checkAll = browser.findElements(checkAllBy);
		Assert.assertFalse(checkAll.isEmpty());
		int numOfCheckbox = checkAll.size();
		for(int i=0;i<numOfCheckbox; i++) {
			By checkbox = By.xpath("//dialog[contains(@class,'modal')]//div[contains(@class,'o_sel_course_elements')]//td/div/div[" + (i+1) + "]/div/label/input[@type='checkbox']");
			OOGraphene.waitElement(checkbox, browser).click();
			OOGraphene.waitBusy(browser);
			By checkedBox = By.xpath("//dialog[contains(@class,'modal')]//div[contains(@class,'o_sel_course_elements')]//td/div/div[" + (i+1) + "]/div/label/input[@type='checkbox'][@checked='checked']");
			OOGraphene.waitElement(checkedBox, browser);
		}
		return this;
	}
}