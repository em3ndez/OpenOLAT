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
package org.olat.selenium;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.core.AdministrationMessagesPage;
import org.olat.selenium.page.core.LoginPasswordForgottenPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.user.PasswordAndAuthenticationAdminPage;
import org.olat.selenium.page.user.RegistrationPage;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class LoginTest extends Deployments {

	private static final Logger log = Tracing.createLoggerFor(LoginTest.class);
	
	private WebDriver browser = getWebDriver(0);
	@ArquillianResource
	private URL deploymentUrl;

	/**
	 * Test if the dmz can be loaded.
	 */
	@Test
	@RunAsClient
	public void loadIndex() {
		//check that the login page, or dmz is loaded
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage();
	}
	
	/**
	 * Test login as administrator.
	 */
	@Test
	@RunAsClient
	public void loginAsAdministrator() 
	throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//load dmz
		LoginPage loginPage = LoginPage
				.load(browser, deploymentUrl)
				.assertOnLoginPage();
		//login as administrator
		loginPage
			.loginAs(administrator)
			.resume();
	}
	
	/**
	 * 
	 * Create a new user and try to login with its credentials.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginAsNewUser()
	throws IOException, URISyntaxException {
		//create a random user
		UserRestClient userClient = new UserRestClient(deploymentUrl);
		UserVO user = userClient.createRandomUser();

		//load dmz
		LoginPage loginPage = LoginPage
				.load(browser, deploymentUrl)
				.assertOnLoginPage();
		//login
		loginPage.loginAs(user.getLogin(), user.getPassword());
	}
	
	/**
	 * 
	 * Login as LDAP user and the OpenOlat user is automatically created.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginAsNewLDAPUser()
	throws IOException, URISyntaxException {
		//load dmz
		LoginPage loginPage = LoginPage
				.load(browser, deploymentUrl)
				.assertOnLoginPage();
		//login
		loginPage
			.loginAs("mrohrer", "olat")
			.resume()
			.assertLoggedInByLastName("Rohrer");
	}
	
	
	/**
	 * An administrator enables the OpenOlat login with a start button.
	 * A user logs in. The administrator disables the feature.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginWithStartButton()
	throws IOException, URISyntaxException {
		WebDriver userBrowser = getWebDriver(1);
		
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		
		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		PasswordAndAuthenticationAdminPage passkeyAdminPage = NavigationPage.load(browser)
				.openAdministration()
				.openPasswordAndAuthentication()
				.enableStartButton(true);
		
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.assertOnLoginPage()
			.startLogin()
			.loginAs(user.getLogin(), user.getPassword())
			.assertLoggedInByLastName(user.getLastName());

		passkeyAdminPage.enableStartButton(false);
	}
	
	
	/**
	 * Create a new user and she logs in with the REST API and after
	 * uses the REST security token to jump directly in "My courses".
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginWithRestToken()
	throws IOException, URISyntaxException {
		//create a random user
		UserRestClient adminClient = new UserRestClient(deploymentUrl);
		UserVO user = adminClient.createRandomUser();

		// user log in via REST
		UserRestClient userClient = new UserRestClient(adminClient.getRestURI(), user.getLogin(), user.getPassword());
		String token = userClient.callMeForSecurityToken();

		// user uses its x-token to enter OpenOlat
		String restRequest = deploymentUrl.toString() + "url/MyCoursesSite/0/My/0"
				+ "?" + RestSecurityHelper.SEC_TOKEN + "=" + token;
		URL restUrl = new URL(restRequest);
		browser.navigate().to(restUrl);
		
		LoginPage loginPage = new LoginPage(browser);
		loginPage.assertLoggedIn(user);
		
		By myEntriesBy = By.cssSelector("#o_main.o_sel_my_repository_entries");
		OOGraphene.waitElement(myEntriesBy, browser);
	}
	
	/**
	 * An administrator set a maintenance message. A first user
	 * logs in before and wait until the message appears. A second
	 * user load the login page, check that the message is visible,
	 * logs in and check that the message is visible too.
	 * 
	 * 
	 * @param loginPage
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void maintenanceMessage()
	throws IOException, URISyntaxException {
		WebDriver reiBrowser = getWebDriver(1);
		WebDriver kanuBrowser = getWebDriver(2);
		
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		//a first user log in
		LoginPage kanuLogin = LoginPage.load(kanuBrowser, deploymentUrl)
			.loginAs(kanu);
		
		// administrator come in, and set a maintenance message
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		String message = "Hello - " + UUID.randomUUID();
		AdministrationMessagesPage messagesPage = NavigationPage.load(browser)
			.openAdministration()
			.selectInfoMessages()
			.newMaintenanceMessage(message);
		
		//A new user see the login page 	
		LoginPage.load(reiBrowser, deploymentUrl)
			.waitOnMaintenanceMessage(message)
			.loginAs(rei)
			.assertOnMaintenanceMessage(message);
		
		kanuLogin
			.waitOnMaintenanceMessage(message);
		
		//administrator remove the message
		messagesPage
			.clearMaintenanceMessage();
		
		//we wait it disappears
		kanuLogin
			.waitOnMaintenanceMessageCleared();
	}
	
	/**
	 * Someone wants to register.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void registration() {
		String email = UUID.randomUUID() + "@openolat.com";
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage();
		RegistrationPage registration = RegistrationPage.getPage(browser)
			.signIn()
			.nextToDisclaimer()
			.acknowledgeDisclaimer()
			.validate(email);
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());

		String registrationLink = registration.extractRegistrationLink(messages.get(0));
		String otp = RegistrationPage.extractOtp(messages.get(0));
		Assert.assertNotNull(registrationLink);
		log.info("Registration link: {}, TOP: {}", registrationLink, otp);
		
		String login = "md_" + CodeHelper.getForeverUniqueID();
		String password = "VerySecret#01";
		registration
			.validateOtp(otp)
			.finalizeRegistration("Arisu", "Iwakura", login, password);

		loginPage
			.assertLoggedInByLastName("Iwakura");
	}
	
	/**
	 * Someone wants to register using the registration URL .
	 * @throws MalformedURLException 
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void registrationLink() throws MalformedURLException {
		String email = UUID.randomUUID() + "@openolat.com";
		
		// The default browser is probably still logged in
		WebDriver anOtherBrowser = getWebDriver(1);
		
		// Use the registration URL
		URL registrationUrl = new URL(deploymentUrl + "url/registration/0");
		anOtherBrowser.navigate()
			.to(registrationUrl);
		OOGraphene.waitModalDialog(anOtherBrowser);
		
		RegistrationPage registration = RegistrationPage.getPage(anOtherBrowser)
			.nextToDisclaimer()
			.acknowledgeDisclaimer()
			.validate(email);
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());

		String registrationLink = registration.extractRegistrationLink(messages.get(0));
		String otp = RegistrationPage.extractOtp(messages.get(0));
		Assert.assertNotNull(registrationLink);
		log.info("Registration link: {}, TOP: {}", registrationLink, otp);
		
		String login = "md_" + CodeHelper.getForeverUniqueID();
		String password = "MyVerySecure#01";
		registration
			.validateOtp(otp)
			.finalizeRegistration("Momo", "Ayase", login, password);

		LoginPage loginPage = new LoginPage(anOtherBrowser);
		loginPage
			.assertLoggedInByLastName("Ayase");
	}
	
	
	/**
	 * Someone wants to register using the classic registration URL
	 * /dmz/registration/
	 * 
	 * @throws MalformedURLException 
	 */
	@Test
	public void registrationDMZLink() throws MalformedURLException {
		String email = UUID.randomUUID() + "@openolat.com";
		
		// The default browser is probably still logged in
		WebDriver anOtherBrowser = getWebDriver(1);
		
		// Use the registration URL
		URL registrationUrl = new URL(deploymentUrl + "dmz/registration/");
		anOtherBrowser.navigate()
			.to(registrationUrl);
		OOGraphene.waitModalDialog(anOtherBrowser);
		
		RegistrationPage registration = RegistrationPage.getPage(anOtherBrowser)
			.nextToDisclaimer()
			.acknowledgeDisclaimer()
			.validate(email);
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());

		String registrationLink = registration.extractRegistrationLink(messages.get(0));
		String otp = RegistrationPage.extractOtp(messages.get(0));
		Assert.assertNotNull(registrationLink);
		log.info("Registration link: {}, TOP: {}", registrationLink, otp);
		
		String login = "md_" + CodeHelper.getForeverUniqueID();
		String password = "MyVery#02Secure";
		registration
			.validateOtp(otp)
			.finalizeRegistration("Ken", "Takakura", login, password);

		LoginPage loginPage = new LoginPage(anOtherBrowser);
		loginPage
			.assertLoggedInByLastName("Takakura");
	}
	
	
	/**
	 * Create a new user, the user uses the change password
	 * workflow to set a new password and log in.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void passwordForgotten()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		LoginPasswordForgottenPage forgottenPage = loginPage
				.passwordForgotten()
				.userIdentification(user.getLogin());
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
		String otp = RegistrationPage.extractOtp(messages.get(0));
		log.info("Registration OTP: {}", otp);
		
		String newPassword = "Sel#12ChngeMeQuickly";
		forgottenPage
			.confirmOtp(otp)
			.newPassword(newPassword);
		
		loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), newPassword)
			.assertLoggedInByLastName(user.getLastName());
	}
}
