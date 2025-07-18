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
package org.olat.modules.openbadges.ui;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeVerification;
import org.olat.modules.openbadges.OpenBadgesManager;

/**
 * Initial date: 2023-06-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedBadgeRow {

	private final OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize;
	private Component badgeImage;
	private String name;
	private String version;
	private String issuedOn;
	private String issuer;
	private String downloadUrl;
	private FormLink toolLink;
	private String addToLinkedInUrl;
	private String recipientName;
	private boolean verified;
	private final boolean expired;

	public IssuedBadgeRow(OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize, 
						  OpenBadgesManager openBadgesManager) {
		BadgeClass badgeClass = badgeAssertionWithSize.badgeAssertion().getBadgeClass();
		this.name = badgeClass.getName();
		this.version = badgeClass.getVersion();
		this.badgeAssertionWithSize = badgeAssertionWithSize;
		this.verified = BadgeVerification.signed.equals(badgeClass.getVerificationMethod());
		this.expired = openBadgesManager.isBadgeAssertionExpired(badgeAssertionWithSize.badgeAssertion());
	}

	public BadgeAssertion getBadgeAssertion() {
		return badgeAssertionWithSize.badgeAssertion();
	}

	public Component getBadgeImage() {
		return badgeImage;
	}

	public void setBadgeImage(Component badgeImage) {
		this.badgeImage = badgeImage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIssuedOn() {
		return issuedOn;
	}

	public void setIssuedOn(String issuedOn) {
		this.issuedOn = issuedOn;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public Size fitIn(int width, int height) {
		return badgeAssertionWithSize.fitIn(width, height);
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public void setAddToLinkedInUrl(String addToLinkedInUrl) {
		this.addToLinkedInUrl = addToLinkedInUrl;
	}

	public String getAddToLinkedInUrl() {
		return addToLinkedInUrl;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public boolean isExpired() {
		return expired;
	}
}
