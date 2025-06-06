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
package org.olat.selenium.page.core;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

/**
 * Fragment which contains the menu tree. The WebElement to create
 * this fragment must be a parent of the div.o_tree
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MenuTreePageFragment {
	
	public static final By treeBy = By.className("o_tree");
	
	private final  WebDriver browser;

	public MenuTreePageFragment(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Click the root link in the tree.
	 * 
	 * @return The menu page fragment
	 */
	public MenuTreePageFragment selectRoot() {
		By rootNodeBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')][contains(@class,'o_tree_l0')]/a");
		try {
			OOGraphene.waitElement(rootNodeBy, browser).click();
		} catch (StaleElementReferenceException e) {
			// the tree can be updated asynchronously, give a second chance
			OOGraphene.waitingALittleBit();
			browser.findElement(rootNodeBy).click();
		}
		By rootNodeActiveBy = By.xpath("//div[contains(@class,'o_tree')]//span[contains(@class,'o_tree_link')][contains(@class,'o_tree_l0')][contains(@class,'active')]/a[not(contains(@class,'o_click'))]");
		OOGraphene.waitElement(rootNodeActiveBy, browser);
		return this;
	}
	
	public MenuTreePageFragment selectWithTitle(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li/div/span[contains(@class,'o_tree_link')]/a[span[contains(text(),'" + title + "')]]");
		try {
			OOGraphene.waitElement(linkBy, browser).click();
		} catch (StaleElementReferenceException e) {
			// the tree can be updated asynchronously, give a second chance
			OOGraphene.waitingALittleBit();
			browser.findElement(linkBy).click();
		}
		OOGraphene.waitBusyAndScrollTop(browser);
		By activeLinkBy = By.xpath("//div[contains(@class,'o_tree')]//li[contains(@class,'active')]/div/span[contains(@class,'o_tree_link')][contains(@class,'active')]/a[not(contains(@class,'o_click'))][span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(activeLinkBy, browser);
		return this;
	}

	public MenuTreePageFragment assertWithTitle(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li/div/span[contains(@class,'o_tree_link')]/a[span[text()[contains(.,'" + title + "')]]]");
		OOGraphene.waitElement(linkBy, browser);
		return this;
	}
	
	public MenuTreePageFragment assertWithTitleSelected(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li[contains(@class,'active')]/div/span[contains(@class,'o_tree_link')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElement(linkBy, browser);
		return this;
	}

	public MenuTreePageFragment assertTitleNotExists(String title) {
		By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li/div/span[contains(@class,'o_tree_link')]/a[span[contains(text(),'" + title + "')]]");
		OOGraphene.waitElementDisappears(linkBy, 5, browser);
		return this;
	}
}
