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
package org.olat.selenium.page.library;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 25 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryAdminPage {
	
	private WebDriver browser;
	
	public LibraryAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LibraryAdminPage needApproval(boolean approval) {
		By approvalBy = By.cssSelector("fieldset.o_sel_library_configuration .o_sel_library_approval");
		OOGraphene.waitElement(approvalBy, browser);
		OOGraphene.toggle("fieldset.o_sel_library_configuration .o_sel_library_approval", approval, false, browser);
		return this;
	}
	
	public LibraryAdminPage save() {
		By saveBy = By.cssSelector("fieldset.o_sel_library_configuration button.btn.btn-primary");
		OOGraphene.waitElement(saveBy, browser).click();
		return this;
	}
	
	public LibraryAdminPage addSharedFolder(String title) {
		By chooseBy = By.cssSelector("fieldset a.o_sel_add_shared_folder");
		OOGraphene.waitElement(chooseBy, browser).click();
		
		OOGraphene.waitModalDialog(browser);
		
		By referenceableEntriesBy = By.xpath("//div[contains(@class,'o_sel_search_referenceable_entries')]//div[contains(@class,'o_segments_content')]");
		OOGraphene.waitElement(referenceableEntriesBy, browser);
		By myReferenceableEntriesBy = By.xpath("//div[contains(@class,'o_sel_search_referenceable_entries')]//div[contains(@class,'o_segments_content')]/a[contains(@class,'o_sel_repo_popup_my_resources')][contains(@class,'btn-primary')]");
		if(!browser.findElements(myReferenceableEntriesBy).isEmpty()) {
			browser.findElement(By.cssSelector("a.o_sel_repo_popup_my_resources")).click();
			OOGraphene.waitElement(myReferenceableEntriesBy, browser);
		}

		//find the row
		By rowBy = By.xpath("//div[contains(@class,'o_sel_search_referenceable_entries')]//div[contains(@class,'o_segments_content')]//table[contains(@class,'o_table')]//tr/td/a[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(rowBy, browser).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		//double check that the resource is selected (search the preview link)
		By referenceLink = By.xpath("//div[contains(@class,'o_sel_selected_shared_folder')]//p[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(referenceLink, browser);
		
		By saveBy = By.cssSelector("fieldset.o_sel_library_configuration button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);

		return this;
	}

}
