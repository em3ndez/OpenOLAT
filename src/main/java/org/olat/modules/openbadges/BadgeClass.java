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
package org.olat.modules.openbadges;

import java.beans.Transient;
import java.util.Date;

import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2023-05-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface BadgeClass {

	BadgeOrganization getBadgeOrganization();

	void setBadgeOrganization(BadgeOrganization badgeOrganization);

	enum BadgeClassStatus {
		preparation, active, deleted, revoked
	}

	enum BadgeClassTimeUnit {
		day, week, month, year
	}

	enum BadgeClassVersionType {
		current, old
	}
	
	Long getKey();

	Date getCreationDate();

	Date getLastModified();

	void setLastModified(Date lastModified);

	String getUuid();

	String getRootId();

	void setRootId(String rootId);

	BadgeClassStatus getStatus();

	void setStatus(BadgeClassStatus status);

	String getVersion();

	String getVersionWithScan();

	String getVersionDisplayString();
	
	void setVersion(String version);

	BadgeVerification getVerificationMethod();

	void setVerificationMethod(BadgeVerification verificationMethod);

	String getPrivateKey();

	void setPrivateKey(String privateKey);

	String getPublicKey();

	void setPublicKey(String publicKey);

	String getLanguage();

	void setLanguage(String language);

	String getImage();

	void setImage(String image);

	String getName();

	String getNameWithScan();

	void setName(String name);

	void setNameWithScan(String name);

	String getDescription();

	String getDescriptionWithScan();

	void setDescription(String description);

	void setDescriptionWithScan(String description);

	String getCriteria();

	void setCriteria(String criteria);

	String getSalt();

	void setSalt(String salt);

	String getIssuer();

	String getIssuerDisplayString();

	void setIssuer(String issuer);

	boolean isValidityEnabled();

	void setValidityEnabled(boolean validityEnabled);

	int getValidityTimelapse();

	void setValidityTimelapse(int validityTimelapse);

	BadgeClassTimeUnit getValidityTimelapseUnit();

	void setValidityTimelapseUnit(BadgeClassTimeUnit validityTimelapseUnit);

	BadgeClassVersionType getVersionType();

	void setVersionType(BadgeClassVersionType versionType);

	RepositoryEntry getEntry();

	void setEntry(RepositoryEntry entry);

	BadgeClass getPreviousVersion();

	void setPreviousVersion(BadgeClass previousVersion);

	boolean hasPreviousVersion();
	
	BadgeClass getNextVersion();

	void setNextVersion(BadgeClass nextVersion);

	@Transient
	void prepareForEntryReset(RepositoryEntry entry);

	@Transient
	int upgradeBadgeDependencyConditions();
}
