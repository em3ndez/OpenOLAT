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
package org.olat.selenium.page;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.coaching.CoachingPage;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.course.MyCoursesPage;
import org.olat.selenium.page.curriculum.CoursePlannerPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupsPage;
import org.olat.selenium.page.library.LibraryPage;
import org.olat.selenium.page.project.ProjectsPage;
import org.olat.selenium.page.qpool.QuestionPoolPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.CatalogAdminPage;
import org.olat.selenium.page.repository.CatalogPage;
import org.olat.selenium.page.user.PortalPage;
import org.olat.selenium.page.user.UserAdminPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page fragment which control the navigation bar with the static sites.
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NavigationPage {
	
	private static final Logger log = Tracing.createLoggerFor(NavigationPage.class);
	
	private static final By navigationSitesBy = By.cssSelector("ul.o_navbar_sites");
	private static final By authoringEnvTabBy = By.cssSelector("li.o_site_author_env > a");
	private static final By projectsTabBy = By.cssSelector("li.o_site_project > a");
	private static final By coursePlannerTabBy = By.cssSelector("li.o_site_curriculum_admin > a");
	private static final By questionPoolTabBy = By.cssSelector("li.o_site_qpool > a");
	private static final By coachingTabBy = By.cssSelector("li.o_site_coaching > a");
	private static final By portalBy = By.cssSelector("li.o_site_portal > a");
	private static final By myCoursesBy = By.cssSelector("li.o_site_repository > a");
	private static final By userManagementBy = By.cssSelector("li.o_site_useradmin > a");
	private static final By administrationBy = By.cssSelector("li.o_site_admin > a");
	private static final By catalogBy = By.cssSelector("li.o_site_catalog > a");
	private static final By catalogAdministrationBy = By.cssSelector("li.o_site_catalog_admin > a");
	private	static final By groupsBy = By.cssSelector("li.o_site_groups > a");
	private	static final By libraryBy = By.cssSelector("li.f_site_library > a");
	
	public static final By myCoursesAssertBy = By.xpath("//div[contains(@class,'o_sel_my_repository_entries')]//div[@class='o_sel_my_courses']");
	public static final By portalAssertBy = By.className("o_portal");
	public static final By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
	
	private final WebDriver browser;
	
	private NavigationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static final NavigationPage load(WebDriver browser) {
		OOGraphene.waitElement(navigationSitesBy, browser);
		return new NavigationPage(browser);
	}
	
	public NavigationPage assertOnNavigationPage() {
		WebElement navigationSites = browser.findElement(navigationSitesBy);
		Assert.assertTrue(navigationSites.isDisplayed());
		return this;
	}
	
	public AuthoringEnvPage openAuthoringEnvironment() {
		try {
			navigate(authoringEnvTabBy);
			OOGraphene.waitElement(By.className("o_sel_author_env"), browser);
			return new AuthoringEnvPage(browser);
		} catch (Error | Exception e) {
			OOGraphene.takeScreenshot("Open authoring environment", browser);
			throw e;
		}
	}
	
	public CoursePlannerPage openCoursePlanner() {
		navigate(coursePlannerTabBy);
		OOGraphene.waitElement(By.cssSelector("#o_main_container h2>i.o_icon_courseplanner"), browser);
		return new CoursePlannerPage(browser);
	}
	
	public ProjectsPage openProjects() {
		navigate(projectsTabBy);
		OOGraphene.waitElement(By.className("o_proj_projects"), browser);
		return new ProjectsPage(browser);
	}
	
	public QuestionPoolPage openQuestionPool() {
		navigate(questionPoolTabBy);
		return new QuestionPoolPage(browser)
				.assertOnQuestionPool();
	}
	
	public CoachingPage openCoaching() {
		navigate(coachingTabBy);
		return new CoachingPage(browser);
	}
	
	public PortalPage openPortal() {
		navigate(portalBy);
		return new PortalPage(browser);
	}
	
	public MyCoursesPage openMyCourses() {
		navigate(myCoursesBy);
		OOGraphene.waitElement(myCoursesAssertBy, browser);
		return new MyCoursesPage(browser);
	}
	
	public UserAdminPage openUserManagement() {
		OOGraphene.waitElement(By.xpath("//body[not(contains(@class,'o_dmz'))]//div[@id='o_main_container']"), browser);
		navigate(userManagementBy);
		return UserAdminPage.getUserAdminPage(browser);
	}
	
	public AdministrationPage openAdministration() {
		navigate(administrationBy);
		return new AdministrationPage(browser);
	}
	
	public CatalogAdminPage openCatalogAdministration() {
		navigate(catalogAdministrationBy);
		return new CatalogAdminPage(browser);
	}
	
	public CatalogPage openCatalog() {
		navigate(catalogBy);
		return new CatalogPage(browser);
	}
	
	public GroupsPage openGroups(WebDriver currentBrowser) {
		navigate(groupsBy);
		return GroupsPage.getPage(currentBrowser);
	}
	
	public LibraryPage openLibrary(WebDriver currentBrowser) {
		navigate(libraryBy);
		return LibraryPage.getPage(currentBrowser);
	}

	private void navigate(By linkBy) {
		try {
			OOGraphene.waitElementPresence(linkBy, 5, browser);
			List<WebElement> links = browser.findElements(linkBy);
			if(links.isEmpty() || !links.get(0).isDisplayed()) {
				//try to open the more menu
				openMoreMenu();
			}

			OOGraphene.waitElement(linkBy, browser).click();
			OOGraphene.waitBusy(browser);
			OOGraphene.waitingTransition(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Navigate", browser);
			throw e;
		}
	}
	
	public void openCourse(String title) {
		By courseTab = By.xpath("//div[@id='o_navbar_container']//li/a[contains(@title,'" + title + "')][not(i[contains(@class,'o_icon_close_tab')])]");
		OOGraphene.waitElementPresence(courseTab, 5, browser);
		List<WebElement> courseLinks = browser.findElements(courseTab);
		if(courseLinks.isEmpty() || !courseLinks.get(0).isDisplayed()) {
			try {
				openMoreMenu();
			} catch (Exception e) {
				// For Firefox importUsers issue
				log.error("", e);
				OOGraphene.waitingALittleBit();
				openMoreMenu();
			}
			courseLinks = browser.findElements(courseTab);
		}
		Assert.assertFalse(courseLinks.isEmpty());
		
		courseLinks.get(0).click();
		OOGraphene.waitBusy(browser);
	}
	
	private void openMoreMenu() {
		By openMoreBy = By.cssSelector("#o_navbar_more>li>a.dropdown-toggle");
		OOGraphene.waitElement(openMoreBy, browser).click();
		//wait the small transition
		By openedMoreMenuby = By.cssSelector("#o_navbar_more>li.open ul.dropdown-menu.dropdown-menu-right");
		OOGraphene.waitElement(openedMoreMenuby, browser);
	}
	
	public NavigationPage closeTab() {
		By closeBy = By.xpath("//li//a[contains(@class,'o_navbar_tab_close')]");
		OOGraphene.waitElementPresence(closeBy, 5, browser);
		WebElement closeEl = browser.findElement(closeBy);
		if(closeEl.isDisplayed()) {
			closeEl.click();
		} else {
			openMoreMenu();
			browser.findElement(closeBy).click();
		}
		OOGraphene.waitBusy(browser);
		return this;
	}
}
