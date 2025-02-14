/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 12 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BadgesPage {
	
	private final WebDriver browser;
	
	public BadgesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BadgesPage assertOnBadge(String badgeName) {
		By badgeBy = By.xpath("//div[@class='o_badge_tool_row']//legend[text()[contains(.,'" + badgeName + "')]]");
		OOGraphene.waitElement(badgeBy, browser);
		return this;
	}
	
	public BadgesPage assertNotOnBadge(String badgeName) {
		By badgeBy = By.xpath("//div[@class='o_badge_tool_row']//legend[text()[contains(.,'" + badgeName + "')]]");
		OOGraphene.waitElementDisappears(badgeBy, 5, browser);
		return this;
	}
	
	public CoursePageFragment clickToolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		return new CoursePageFragment(browser);
	}

}
