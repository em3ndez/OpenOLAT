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
package org.olat.selenium.page.lecture;

import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * 
 * The tool in course for teachers.
 * 
 * Initial date: 7 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesRepositoryPage {
	
	private final WebDriver browser;
	
	public LecturesRepositoryPage(WebDriver browser) {
		this.browser = browser;
	}

	/**
	 * The main list of lecture blocks used everywhere.
	 * 
	 * @return Itself
	 */
	public LecturesRepositoryPage assertOnLectureBlocksList() {
		By listBy = By.cssSelector("div.o_sel_repo_lectures_list table.table");
		OOGraphene.waitElement(listBy, browser);
		return this;
	}
	
	public TeacherRollCallPage openRollCall(String lectureBlockTitle) {
		if(browser instanceof FirefoxDriver) {
			OOGraphene.scrollTableRight(By.cssSelector(".o_table_wrapper .o_scrollable_wrapper .o_scrollable"), browser);
		}

		By selectBy = By.xpath("//div[contains(@class,'o_sel_repo_lectures_list')]//table//tr[td/a[contains(text(),'" + lectureBlockTitle + "')]]/td/a[i[contains(@class,'o_icon_lecture')]]");
		OOGraphene.waitElement(selectBy, browser).click();
		return new TeacherRollCallPage(browser)
				.assertOnRollCall();
	}
	
	/**
	 * Click back to the course
	 * 
	 * @return Itself
	 */
	public CoursePageFragment clickToolbarRootCrumb() {
		By toolbarBackBy = By.xpath("//ol[@class='breadcrumb']/li[contains(@class,'o_first_crumb')]/a");
		OOGraphene.waitElement(toolbarBackBy, browser);
		browser.findElement(toolbarBackBy).click();
		OOGraphene.waitBusy(browser);
		return new CoursePageFragment(browser);
	}
}
