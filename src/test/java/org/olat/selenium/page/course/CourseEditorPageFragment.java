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
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseEditorPageFragment {
	
	public static final By editorBy = By.className("o_course_editor");
	public static final By createNodeButton = By.className("o_sel_course_editor_create_node");
	public static final By createNodeModalBy = By.id("o_course_editor_choose_nodetype");
	
	public static final By publishButtonBy = By.className("o_sel_course_editor_publish");

	public static final By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");
	
	public static final By chooseCpButton = By.className("o_sel_cp_choose_repofile");
	public static final By chooseWikiButton = By.className("o_sel_wiki_choose_repofile");
	public static final By chooseTestButton = By.className("o_sel_re_reference_select");
	public static final By chooseScormButton = By.className("o_sel_scorm_choose_repofile");
	public static final By choosePortfolioButton = By.className("o_sel_map_choose_repofile");
	public static final By chooseSurveyButton = By.className("o_sel_survey_choose_repofile");
	public static final By chooseFormButton = By.className("o_sel_form_choose_repofile");
	
	public static final By tabNavTabsBy = By.cssSelector("ul.nav.nav-tabs");
	
	private WebDriver browser;
	
	public CourseEditorPageFragment(WebDriver browser) {
		this.browser = browser;
	}
	
	public static CourseEditorPageFragment getEditor(WebDriver browser) {
		OOGraphene.waitElement(editorBy, browser);
		return new CourseEditorPageFragment(browser);
	}
	
	public CourseEditorPageFragment assertOnEditor() {
		OOGraphene.waitElement(editorBy, browser);
		List<WebElement> editorEls = browser.findElements(editorBy);
		Assert.assertFalse(editorEls.isEmpty());
		Assert.assertTrue(editorEls.get(0).isDisplayed());
		return this;
	}
	
	public CourseEditorPageFragment assertOnWarning() {
		try {
			OOGraphene.assertAndCloseWarningBox(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("CourseEditorWarning", browser);
			throw e;
		}
		return this;
	}
	
	public CourseEditorPageFragment assertOnResource(String resourceTitle) {
		By landingBy = By.xpath("//div[@class='o_course_editor']//a/span[text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElement(landingBy, browser);
		return this;
	}
	
	public CourseEditorPageFragment assertOnPanelResourceTitle(String resourceTitle) {
		By landingBy = By.xpath("//div[@class='o_course_editor']//div[@class='o_icon_panel_header']//h4[text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElement(landingBy, browser);
		return this;
	}
	
	/**
	 * Select the root course element.
	 */
	public CourseEditorPageFragment selectRoot() {
		By rootNodeBy = By.cssSelector(".o_editor_menu span.o_tree_link.o_tree_l0>a");
		browser.findElement(rootNodeBy).click();
		OOGraphene.waitBusyAndScrollTop(browser);
		By rootNodeActiveBy = By.cssSelector(".o_editor_menu span.o_tree_link.o_tree_l0.active");
		OOGraphene.waitElement(rootNodeActiveBy, browser);
		return this;
	}
	
	public EasyConditionConfigPage selectTabVisibility() {
		By passwordTabBy = By.cssSelector("fieldset.o_sel_course_visibility_condition_form");
		selectTab(passwordTabBy);
		return new EasyConditionConfigPage(browser);
	}
	
	/**
	 * Select the tab where the password setting are
	 * @return
	 */
	public CourseEditorPageFragment selectTabPassword() {
		By passwordTabBy = By.cssSelector("fieldset.o_sel_course_node_password_config");
		return selectTab(passwordTabBy);
	}
	
	public CourseEditorPageFragment setPassword(String password) {
		OOGraphene.scrollBottom(By.cssSelector("fieldset.o_sel_course_node_password_config"), browser);
		
		By switchBy = By.cssSelector(".o_sel_course_password_condition_switch input[type='checkbox']");
		OOGraphene.waitElement(switchBy, browser).click();

		By passwordBy = By.cssSelector(".o_sel_course_password_condition_value input[type='text']");
		OOGraphene.waitElement(passwordBy, browser).sendKeys(password);
		
		By saveBy = By.cssSelector("fieldset.o_sel_course_node_password_config button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	/**
	 * Select the tab score in a structure node.
	 * 
	 */
	public CourseEditorPageFragment selectTabScore() {
		By tabScoreBy = By.cssSelector("ul.o_node_config li.o_sel_st_score>a");
		OOGraphene.waitElement(tabScoreBy, browser).click();
		By scoreBy = By.cssSelector("fieldset.o_sel_structure_score");
		OOGraphene.waitElement(scoreBy, browser);
		return this;
	}
	
	public LearnPathCourseElementEditorPage selectTabLearnPath() {
		By learnPathTabBy = By.cssSelector("fieldset.o_lp_config_edit");
		selectTab(learnPathTabBy);
		return new LearnPathCourseElementEditorPage(browser);
	}
	
	private CourseEditorPageFragment selectTab(By tabBy) {
		//make sure the tab bar is loaded
		OOGraphene.selectTab("o_node_config", tabBy, browser);
		return this;
	}
	
	/**
	 * Enable passed and points by nodes
	 * @return
	 */
	public CourseEditorPageFragment enableRootScoreByNodes() {
		// Enable points
		By enablePointBy = By.cssSelector("fieldset.o_sel_structure_score .o_sel_score_settings input[type='checkbox'][value='score']");
		browser.findElement(enablePointBy).click();
		By pointPanelBy = By.cssSelector("fieldset.o_sel_score_config");
		OOGraphene.waitElement(pointPanelBy, browser);
		
		// Enable passed
		By enablePassedBy = By.cssSelector("fieldset.o_sel_structure_score .o_sel_score_settings input[type='checkbox'][value='passed']");
		browser.findElement(enablePassedBy).click();
		By passedPanelBy = By.cssSelector("fieldset.o_sel_passed_config");
		OOGraphene.waitElement(passedPanelBy, browser);
		
		By enablePointNodesBy = By.cssSelector("fieldset.o_sel_score_config input[type='checkbox'][name='scform.scoreNodeIndents']");
		List<WebElement> pointNodeEls = browser.findElements(enablePointNodesBy);
		for(WebElement pointNodeEl:pointNodeEls) {
			pointNodeEl.click();
		}
		
		By passedInheritBy = By.cssSelector("fieldset.o_sel_passed_config input[type='radio'][name='scform.passedtype'][value='inherit']");
		browser.findElement(passedInheritBy).click();
		
		By enablePassedNodesBy = By.cssSelector("fieldset.o_sel_passed_config input[type='checkbox'][name='scform.passedNodeIndents']");
		OOGraphene.waitElement(enablePassedNodesBy, browser);

		List<WebElement> enablePassedNodeEls = browser.findElements(enablePassedNodesBy);
		for(WebElement enablePassedNodeEl:enablePassedNodeEls) {
			enablePassedNodeEl.click();
		}
		
		//save
		By submitBy = By.cssSelector("fieldset.o_sel_score_buttons button.btn.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Create a new course element
	 * @param nodeAlias The type of the course element
	 * @return
	 */
	public CourseEditorPageFragment createNode(String nodeAlias) {
		OOGraphene.waitElement(createNodeButton, browser);
		browser.findElement(createNodeButton).click();
		OOGraphene.waitModalDialog(browser);
		
		By nodeBy = By.cssSelector("dialog div#o_course_editor_choose_nodetype a.o_sel_course_editor_node-" + nodeAlias);
		OOGraphene.click(nodeBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return assertOnNodeTitle();
	}
	
	/**
	 * Wait until the short title and description field are
	 * there.
	 * 
	 * @return Itself
	 */
	public CourseEditorPageFragment assertOnNodeTitle() {
		By shortTitleBy = By.className("o_sel_node_editor_shorttitle");
		OOGraphene.waitElement(shortTitleBy, browser);
		return this;
	}
	
	/**
	 * Set the course element title and short title, save
	 * and wait that the dialog disappears.
	 * 
	 * @param title The title of the course element
	 * @return Itself
	 */
	public CourseEditorPageFragment nodeTitle(String title) {
		assertOnNodeTitle();

		By shortTitleBy = By.cssSelector("div.o_sel_node_editor_shorttitle input[type='text']");
		WebElement shortTitleEl = browser.findElement(shortTitleBy);
		shortTitleEl.clear();
		shortTitleEl.sendKeys(title);
		
		By longtitle = By.cssSelector("div.o_sel_node_editor_title input");
		WebElement titleEl = browser.findElement(longtitle);
		titleEl.clear();
		titleEl.sendKeys(title);
		
		By saveButton = By.cssSelector("button.o_sel_node_editor_submit");
		OOGraphene.scrollBottom(saveButton, browser);
		browser.findElement(saveButton).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	public String getRestUrl() {
		By openerBy = By.cssSelector("a.o_opener");
		browser.findElement(openerBy).click();

		By urlBy = By.cssSelector("div.o_copy_code input");
		OOGraphene.waitElement(urlBy, browser);
		
		String url = null;
		List<WebElement> urlEls = browser.findElements(urlBy);
		for(WebElement urlEl:urlEls) {
			String text = urlEl.getDomAttribute("value");
			if(text.contains("http")) {
				url = text.trim();
				break;
			}
		}
		Assert.assertNotNull(url);
		return url;
	}
	
	public CourseEditorPageFragment moveUnder(String targetNodeTitle) {
		openChangeNodeToolsMenu();
		
		By changeNodeLinkBy = By.cssSelector("a.o_sel_course_editor_move_node");
		browser.findElement(changeNodeLinkBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By targetNodeBy = By.xpath("//div[contains(@class,'o_tree_insert_tool')]//a[contains(@title,'" + targetNodeTitle + "')]");
		browser.findElement(targetNodeBy).click();
		OOGraphene.waitBusy(browser);
		
		By underBy = By.xpath("//div[contains(@class,'o_tree_insert_tool')]//a[i[contains(@class,'o_icon_node_under')]]");
		browser.findElement(underBy).click();
		OOGraphene.waitBusy(browser);
		
		By saveBy = By.cssSelector("div.modal-content div.o_button_group a.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public CourseEditorPageFragment deleteElement() {
		By deleteNodeLinkBy = By.xpath("//div[@class='o_title_cmds']//a[contains(@onclick,'command.deletenode')]");
		browser.findElement(deleteNodeLinkBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By confirmBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
		browser.findElement(confirmBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		By undeleteButtonBy = By.xpath("//a[contains(@onclick,'undeletenode.button')]");
		OOGraphene.waitElement(undeleteButtonBy, browser);
		return this;
	}
	
	public CourseEditorPageFragment selectNode(String nodeTitle) {
		By targetNodeBy = By.xpath("//div[contains(@class,'o_editor_menu')]//a[contains(@title,'" + nodeTitle + "')]");
		browser.findElement(targetNodeBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CourseEditorPageFragment assertSelectedNode(String nodeTitle) {
		By targetNodeBy = By.xpath("//div[contains(@class,'o_editor_menu')]//span[contains(@class,'active')][contains(@class,'o_tree_level_label_leaf')]/a[contains(@title,'" + nodeTitle + "')]");
		OOGraphene.waitElement(targetNodeBy, browser);
		return this;
	}
	
	/**
	 * Open the tools drop-down
	 * @return Itself
	 */
	public CourseEditorPageFragment openChangeNodeToolsMenu() {
		OOGraphene.scrollTop(browser);
		By changeNodeToolsMenuCaret = By.cssSelector("div.o_title_cmds button.o_sel_course_editor_change_node");
		OOGraphene.waitElement(changeNodeToolsMenuCaret, browser).click();
		By changeNodeToolsMenu = By.cssSelector("ul.o_sel_course_editor_change_node");
		OOGraphene.waitElement(changeNodeToolsMenu, browser);
		return this;
	}
	
	public CourseEditorPageFragment selectTabTestContent() {
		return selectTabContent(chooseTestButton);
	}
	
	public CourseEditorPageFragment selectTabSurveyContent() {
		return selectTabContent(By.cssSelector("fieldset.o_sel_survey_config_form"));
	}
	
	public CourseEditorPageFragment selectTabFormContent() {
		return selectTabContent(By.cssSelector("fieldset.o_sel_form_config_form"));
	}
	
	public CourseEditorPageFragment selectTabCPContent() {
		return selectTabContent(chooseCpButton);
	}
	
	public CourseEditorPageFragment selectTabScormContent() {
		return selectTabContent(chooseScormButton);
	}
	
	public CourseEditorPageFragment selectTabFeedContent() {
		return selectTabContent(By.className("o_sel_feed"));
	}
	
	public CourseEditorPageFragment selectTabPortfolioContent() {
		return selectTabContent(choosePortfolioButton);
	}
	
	public CourseEditorPageFragment selectTabWikiContent() {
		return selectTabContent(chooseWikiButton);
	}
	
	private CourseEditorPageFragment selectTabContent(By targetBy) {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_repo_entry>a");
		OOGraphene.waitElement(tabBy, browser).click();
		OOGraphene.waitElement(targetBy, browser);
		return this;
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the CP
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseCP(String resourceTitle) {
		return chooseResource(chooseCpButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the wiki
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseWiki(String resourceTitle) {
		return chooseResource(chooseWikiButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the survey
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseSurvey(String resourceTitle) {
		return chooseResource(chooseSurveyButton, resourceTitle);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the survey
	 * @return Itself
	 */
	public FormConfigurationPage chooseForm(String resourceTitle) {
		chooseResource(chooseFormButton, resourceTitle);
		return new FormConfigurationPage(browser);
	}
	
	/**
	 * @see chooseResource
	 * @param resourceTitle The title of the SCORM
	 * @return Itself
	 */
	public CourseEditorPageFragment chooseScorm(String resourceTitle) {
		return chooseResource(chooseScormButton, resourceTitle);
	}
	
	/**
	 * Choose a portfolio, v1.0 or v2.0
	 * 
	 * @param resourceTitle The name of the binder / portfolio
	 * @return
	 */
	public CourseEditorPageFragment choosePortfolio(String resourceTitle) {
		return chooseResource(choosePortfolioButton, resourceTitle);
	}
	
	/**
	 * Click the choose button, which open the resource chooser. Select
	 * the "My entries" segment, search the rows for the resource title,
	 * and select it.
	 * 
	 * 
	 * @param chooseButton The By of the choose button in the course node editor
	 * @param resourceTitle The resource title to find
	 * @return
	 */
	public CourseEditorPageFragment chooseResource(By chooseButton, String resourceTitle) {
		By landingBy = By.xpath("//div[@class='o_course_editor']//a/span[text()[contains(.,'" + resourceTitle + "')]]");
		return chooseResource(chooseButton, resourceTitle, landingBy);
	}
	
	public CourseEditorPageFragment chooseResourceModern(By chooseButton, String resourceTitle) {
		By landingBy = By.xpath("//div[@class='o_re_reference']//div[@class='o_icon_panel_header']/h4[text()[contains(.,'" + resourceTitle + "')]]");
		return chooseResource(chooseButton, resourceTitle, landingBy);
	}
	
	private CourseEditorPageFragment chooseResource(By chooseButton, String resourceTitle, By landingBy) {
		browser.findElement(chooseButton).click();
		OOGraphene.waitModalDialog(browser);
	
		By referenceableEntriesBy = By.xpath("//div[contains(@class,'o_sel_search_referenceable_entries')]//div[contains(@class,'o_segments_content')]");
		OOGraphene.waitElement(referenceableEntriesBy, browser);
		By myReferenceableEntriesBy = By.xpath("//div[contains(@class,'o_sel_search_referenceable_entries')]//div[contains(@class,'o_segments_content')]/a[contains(@class,'o_sel_repo_popup_my_resources')][contains(@class,'btn-primary')]");
		if(!browser.findElements(myReferenceableEntriesBy).isEmpty()) {
			browser.findElement(By.cssSelector("a.o_sel_repo_popup_my_resources")).click();
			OOGraphene.waitElement(myReferenceableEntriesBy, browser);
		}

		//find the row
		By rowBy = By.xpath("//div[contains(@class,'o_sel_search_referenceable_entries')]//div[contains(@class,'o_segments_content')]//table[contains(@class,'o_table')]//tr/td/a[text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElement(rowBy, browser).click();
		OOGraphene.waitModalDialogDisappears(browser);
		
		//double check that the resource is selected (search the preview link)
		OOGraphene.waitElement(landingBy, browser);
		return this;
	}
	
	/**
	 * Create a wiki from the chooser popup
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createWiki(String resourceTitle) {
		return createResource(chooseWikiButton, resourceTitle, null);
	}
	
	/**
	 * Create a QTI 1.2 test from the chooser popup
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createQTITest(String  resourceTitle) {
		return createResource(chooseTestButton, resourceTitle, "FileResource.TEST");
	}
	
	/**
	 * Create a podcast or a blog
	 * @param resourceTitle
	 * @return
	 */
	public CourseEditorPageFragment createFeed(String resourceTitle) {
		By createFeedButtonBy = By.cssSelector("fieldset.o_sel_feed  a.btn.o_sel_re_reference_select");
		return createResource(createFeedButtonBy, resourceTitle, null);
	}
	
	/**
	 * Add a podcast or a blog to a course element with an URL.
	 * 
	 * @param resourceTitle The new of the ressource
	 * @param url The URL of the blog or podcast
	 * @return Itself
	 */
	public CourseEditorPageFragment importExternalUrl(String resourceTitle, String url, String type) {
		By importByUrlBy = By.cssSelector("button.o_sel_repo_import_url");
		browser.findElement(importByUrlBy).click();
		
		By importButton = By.cssSelector("fieldset.o_sel_feed .o_re_reference a.o_sel_re_reference_import_url");
		OOGraphene.waitElement(importButton, browser).click();
		OOGraphene.waitModalDialog(browser);
		
		By urlBy = By.cssSelector("fieldset.o_sel_re_import_url_form div.o_sel_import_url input[type='text']");
		browser.findElement(urlBy).sendKeys(url);
		By displayNameBy = By.cssSelector("fieldset.o_sel_re_import_url_form div.o_sel_author_imported_name input[type='text']");
		browser.findElement(displayNameBy).sendKeys("");
		
		if(type != null) {
			By typeBy = By.xpath("//fieldset[contains(@class,'o_sel_re_import_url_form')]//div[contains(@class,'o_sel_import_type')]//select/option[@value='" + type + "']"); 
			OOGraphene.waitElement(typeBy, browser);
			By imporTypeBy = By.cssSelector("fieldset.o_sel_re_import_url_form .o_sel_import_type select");
			new Select(browser.findElement(imporTypeBy)).selectByValue(type);
		}
		
		browser.findElement(displayNameBy).clear();
		browser.findElement(displayNameBy).sendKeys(resourceTitle);

		By submitBy = By.cssSelector("fieldset.o_sel_re_import_url_form .o_sel_repo_save_details button.btn.btn-primary");
		browser.findElement(submitBy).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		
		By resourceTitleBy = By.xpath("//div[contains(@class,'o_re_reference')]//h4[text()[contains(.,'" + resourceTitle + "')]]");
		OOGraphene.waitElementSlowly(resourceTitleBy, 10, browser);
		return this;
	}
	
	private CourseEditorPageFragment createResource(By chooseButton, String resourceTitle, String resourceType) {
		browser.findElement(chooseButton).click();
		OOGraphene.waitModalDialog(browser);
		
		//popup
		By myResourcesBy = By.cssSelector(".modal-body .o_sel_search_referenceable_entries a.o_sel_repo_popup_my_resources");
		OOGraphene.waitElement(myResourcesBy, browser)
			.click();
		OOGraphene.waitBusy(browser);
		By mySelectedResourcesBy = By.cssSelector(".modal-body .o_sel_search_referenceable_entries a.btn-primary.o_sel_repo_popup_my_resources");
		OOGraphene.waitElement(mySelectedResourcesBy, browser);
		
		//click create
		By createResourceBy = By.cssSelector(".o_sel_search_referenceable_entries .o_sel_repo_popup_create_resource");
		List<WebElement> createEls = browser.findElements(createResourceBy);
		if(createEls.isEmpty()) {
			//open drop down
			By createResourcesBy = By.cssSelector("button.o_sel_repo_popup_create_resources");
			browser.findElement(createResourcesBy).click();
			//choose the right type
			By selectType = By.xpath("//ul[contains(@class,'o_sel_repo_popup_create_resources')]//a[contains(@onclick,'" + resourceType + "')]");
			OOGraphene.waitElement(selectType, browser)
				.click();
		} else {
			browser.findElement(createResourceBy).click();	
		}

		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("dialog.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		OOGraphene.waitElement(inputBy, browser)
			.sendKeys(resourceTitle);
		By submitBy = By.cssSelector("dialog.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(submitBy).click();
		OOGraphene.waitModalDialogWithDivDisappears(browser, "o_sel_repo_save_details");
		OOGraphene.waitModalDialogWithDivDisappears(browser, "o_sel_search_referenceable_entries");

		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	/**
	 * Don't forget to set access.<br>
	 * Click the bread crumb and publish the course.
	 * 
	 * @return Itself
	 */
	public CoursePageFragment autoPublish() {
		//back
		By breadcrumpBackBy = By.cssSelector("#o_main_toolbar li.o_breadcrumb_back a");
		browser.findElement(breadcrumpBackBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//auto publish
		By autoPublishBy = By.cssSelector("dialog.modal a.o_sel_course_quickpublish_auto");
		browser.findElement(autoPublishBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return new CoursePageFragment(browser);
	}

	/**
	 * Open the publish process
	 * @return
	 */
	public PublisherPageFragment publish() {
		OOGraphene.waitElement(publishButtonBy, browser);
		OOGraphene.waitingALittleLonger();
		browser.findElement(publishButtonBy).click();
		OOGraphene.waitModalWizard(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_publish_nodes"), browser);
		return new PublisherPageFragment(browser);
	}
	
	/**
	 * Click the back button
	 * 
	 * @return Itself
	 */
	public CoursePageFragment clickToolbarBack() {
		try {
			browser.findElement(toolbarBackBy).click();
			OOGraphene.waitBusy(browser);
			
			By mainId = By.id("o_main");
			OOGraphene.waitElement(mainId, browser);
			return new CoursePageFragment(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Clicktoolbarback", browser);
			throw e;
		}
	}
	
	/**
	 * Same as clickToolbarBack but click the first element of the path and
	 * wait until the last element disappears.
	 * 
	 * @return Itself
	 */
	public CoursePageFragment clickToolbarFirstCrumb() {
		try {
			By crumbBy = By.xpath("//div[contains(@class,'o_edit_mode')]//ol[@class='breadcrumb']/li[contains(@class,'o_breadcrumb_crumb') and contains(@class,'o_first_crumb')]/a");
			OOGraphene.waitElementPresence(crumbBy, 5, browser);
			OOGraphene.waitElement(crumbBy, browser)
				.click();
			OOGraphene.waitBusy(browser);
			
			By lastCrumbBy = By.cssSelector("ol.breadcrumb li.o_breadcrumb_crumb.o_last_crumb.o_nowrap");
			OOGraphene.waitElementDisappears(lastCrumbBy, 5, browser);
			By mainId = By.id("o_main");
			OOGraphene.waitElement(mainId, browser);
			return new CoursePageFragment(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Clicktoolbarfirstcrumb", browser);
			throw e;
		}
	}
	
	public CoursePageFragment clickToolbarRootCrumb() {
		try {
			By crumbBy = By.xpath("//div[contains(@class,'o_edit_mode')]//ol[@class='breadcrumb']/li[@class='o_breadcrumb_root']/a");
			OOGraphene.waitElementPresence(crumbBy, 10, browser);
			OOGraphene.waitElement(crumbBy, browser)
				.click();
			OOGraphene.waitBusy(browser);
			
			By lastCrumbBy = By.cssSelector("ol.breadcrumb li.o_breadcrumb_crumb.o_last_crumb.o_nowrap");
			OOGraphene.waitElementDisappears(lastCrumbBy, 10, browser);
			By mainId = By.id("o_main");
			OOGraphene.waitElement(mainId, browser);
			return new CoursePageFragment(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Clicktoolbarrootcrumb", browser);
			throw e;
		}
	}
}
