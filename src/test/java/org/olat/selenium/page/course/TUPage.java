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
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 6 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TUPage {
	
	private final WebDriver browser;
	
	public TUPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public TUPage checkPage(String cssSelector) {
		By frameBy = By.cssSelector("div.o_iframedisplay.o_module_tu_wrapper iframe");
		WebElement frameEl = OOGraphene.waitElement(frameBy, browser);
		WebDriver frame = browser.switchTo().frame(frameEl);
		OOGraphene.waitElement(By.cssSelector(cssSelector), 15, frame);
		browser.switchTo().defaultContent();
		return this;
	}
}
