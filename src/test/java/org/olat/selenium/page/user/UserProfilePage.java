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
package org.olat.selenium.page.user;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.dumbster.smtp.SmtpMessage;

/**
 * Drive the profile of a user.
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserProfilePage {
	
	private static final Logger log = Tracing.createLoggerFor(UserProfilePage.class);
	
	private WebDriver browser;
	
	public UserProfilePage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check if the profile panel is visible (but doesn't check if it is active)
	 * @return
	 */
	public UserProfilePage assertOnProfile() {
		By profileSegmentBy = By.cssSelector("div.o_segments a.btn.o_sel_usersettings_profile");
		try {
			OOGraphene.waitElement(profileSegmentBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Assertonprofile", browser);
			throw e;
		}
		WebElement profileSegmentEl = browser.findElement(profileSegmentBy);
		Assert.assertTrue(profileSegmentEl.isDisplayed());
		return this;
	}
	
	/**
	 * Check if the user name in the profile segment is visible.
	 * 
	 * @param username
	 * @return
	 */
	public UserProfilePage assertOnUsername(String username) {
		By usernameBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//input[@value='" + username + "']");
		WebElement usernameEl = OOGraphene.waitElement(usernameBy, browser);
		Assert.assertTrue(usernameEl.isDisplayed());
		return this;
	}
	
	public UserProfilePage changeEmail(String newEmail) {
		By changeEmailBy = By.cssSelector(".o_user_profile_form a.o_sel_user_change_mail");
		OOGraphene.waitElement(changeEmailBy, browser);
		OOGraphene.click(changeEmailBy, browser);
		
		// Modal to change the E-mail
		OOGraphene.waitModalDialog(browser);
		
		By emailBy = By.cssSelector("dialog .o_sel_email_form input.o_sel_registration_email[type='text']");
		OOGraphene.waitElement(emailBy, browser).sendKeys(newEmail);
		return this;
	}

	public UserProfilePage validateEmail() {
		By validateBy = By.cssSelector(".o_sel_email_form a.btn.btn-primary");
        browser.findElement(validateBy).click();
        
        By otpBy = By.cssSelector(".o_sel_registration_otp");
        OOGraphene.waitElement(otpBy, browser);
		return this;
	}
	
	public UserProfilePage confirmEmail(String otp) {
		By otpBy = By.cssSelector(".o_sel_email_form .o_sel_registration_otp input[type='text']");
		OOGraphene.waitElement(otpBy, browser);
		browser.findElement(otpBy).sendKeys(otp);
		OOGraphene.waitBusy(browser);
		
		By confirmBy = By.xpath("//dialog//fieldset[contains(@class,'o_form')]//button[contains(@class,'btn-primary')][not(contains(@class,'o_disabled'))]");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		 
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public String extractOtp(SmtpMessage message) {
		String body = message.getBody();
		int index = body.indexOf("'otp'>");
		if(index >= 0) {
			body = body.substring(index + 6);
		}
		int nextIndex = body.indexOf("</span");
		if(nextIndex >= 0) {
			body = body.substring(0, nextIndex);
		}
		return body;
	}
	
	public UserProfilePage assertOnEmail(String email) {
		By emailBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//div[contains(@class,'o_sel_user_mail')]//input[@type='text'][@value='" + email + "']");
		OOGraphene.waitElement(emailBy, browser);
		return this;
	}
	
	public  UserProfilePage assertOnChangedEmail(String newEmail) {
		By emailBy = By.xpath("//div[contains(@class,'o_user_profile_form')]//div[contains(@class,'o_sel_user_mail')]//input[@type='text'][@disabled='disabled'][contains(@value,'" + newEmail + "')]");
		OOGraphene.waitElement(emailBy, browser);
		return this;
	}
	
	public UserProfilePage saveProfilAndConfirmEmail() {
		try {
			By saveBy = By.cssSelector(".o_user_profile_form button.btn-primary");
			OOGraphene.click(saveBy, browser);
			OOGraphene.waitingALittleLonger();
			OOGraphene.waitModalDialog(browser);
			By yesBy = By.xpath("//div[contains(@class,'modal-dialog')]//a[contains(@onclick,'link_0')]");
			browser.findElement(yesBy).click();
			OOGraphene.waitModalDialogDisappears(browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Confirm Email", browser);
			throw e;
		}
		return this;
	}
	
	public String extractConfirmationLink(SmtpMessage message) {
		String body = message.getBody();
		byte[] bytes = body.getBytes();
		try(InputStream in = new ByteArrayInputStream(bytes)) {
			body = IOUtils.toString(MimeUtility.decode(in, "quoted-printable"), StandardCharsets.UTF_8);
		} catch(Exception e) {
			log.error("", e);
		}
		
		int index = body.indexOf("text/html");
		index = body.indexOf("http", index);
		if(index >= 0) {
			String subBody = body.substring(index);
			int lastIndex = subBody.indexOf('\r');
			if(lastIndex > 128 || lastIndex == -1) {
				lastIndex = subBody.indexOf("\n");
			}
			if(lastIndex > 128 || lastIndex == -1) {
				lastIndex = subBody.indexOf("<br");
			}
			
			String link = subBody.substring(0, lastIndex);
			// hack
			link = link.replace("&amp;", "&")
					.replace("em=change", "emchange")
					.replace("emchan=ge", "emchange")
					.trim();
			return link;
		}
		return null;
	}
	
	public UserProfilePage loadConfirmationLink(String link) {
		browser.navigate().to(link);
		return this;
	}

}
