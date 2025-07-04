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
package org.olat.modules.openbadges.v2;

import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeVerification;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.model.BadgeSigningOrganization;

import org.json.JSONObject;

/**
 * Initial date: 2023-05-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class Profile {

	private static final String PUBLIC_KEY = "publicKey";
	private static final String REVOCATION_LIST_KEY = "revocationList";

	private String id;
	private String name;
	private String url;
	private String email;
	private String publicKey;
	private String revocationList;
	
	public Profile(JSONObject jsonObject) {
		for (String key : jsonObject.keySet()) {
			if (Constants.ID_KEY.equals(key)) {
				if (jsonObject.get(Constants.ID_KEY) instanceof String idString) {
					setId(idString);
				} else {
					throw new IllegalArgumentException("Invalid profile ID.");
				}
			} else if (Constants.TYPE_KEY.equals(key)) {
				if (!Constants.TYPE_VALUE_ISSUER.equals(jsonObject.get(Constants.TYPE_KEY)) && !Constants.TYPE_VALUE_PROFILE.equals(jsonObject.get(Constants.TYPE_KEY))) {
					throw new IllegalArgumentException("Only type 'Profile' or 'Issuer' supported.");
				}
			} else if (Constants.NAME_KEY.equals(key)) {
				if (jsonObject.get(Constants.NAME_KEY) instanceof String nameString) {
					setName(nameString);
				} else {
					throw new IllegalArgumentException("Invalid profile name.");
				}
			} else if (Constants.URL_KEY.equals(key)) {
				if (jsonObject.get(Constants.URL_KEY) instanceof String urlString) {
					setUrl(urlString);
				} else {
					throw new IllegalArgumentException("Invalid profile URL.");
				}
			} else if (Constants.EMAIL_KEY.equals(key)) {
				if (jsonObject.get(Constants.EMAIL_KEY) instanceof String emailString) {
					setEmail(emailString);
				} else {
					throw new IllegalArgumentException("Invalid email.");
				}
			} else if (PUBLIC_KEY.equals(key)) {
				if (jsonObject.get(PUBLIC_KEY) instanceof String publicKeyString) {
					setPublicKey(publicKeyString);
				} else {
					throw new IllegalArgumentException("Invalid publicKey.");
				}	
			} else if (REVOCATION_LIST_KEY.equals(key)) {
				if (jsonObject.get(REVOCATION_LIST_KEY) instanceof String revocationListString) {
					setRevocationList(revocationListString);
				} else {
					throw new IllegalArgumentException("Invalid revocationList.");
				}
			}
		}
	}

	public Profile(BadgeClass badgeClass) {
		this(new JSONObject(badgeClass.getIssuer()));
		setId(OpenBadgesFactory.createIssuerUrl(badgeClass.getUuid()));
		if (BadgeVerification.signed.equals(badgeClass.getVerificationMethod())) {
			setPublicKey(OpenBadgesFactory.createPublicKeyUrl(badgeClass.getUuid()));
			setRevocationList(OpenBadgesFactory.createRevocationListUrl(badgeClass.getUuid()));
		}
	}
	
	public Profile(BadgeSigningOrganization badgeSigningOrganization) {
		setId(badgeSigningOrganization.organizationUrl());
		setPublicKey(badgeSigningOrganization.publicKeyUrl());
		setRevocationList(badgeSigningOrganization.revocationListUrl());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getNameWithScan() {
		return StringHelper.xssScan(getName());
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNameWithScan(String name) {
		setName(StringHelper.unescapeHtml(FilterFactory.getHtmlTagsFilter().filter(StringHelper.xssScan(name))));
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getRevocationList() {
		return revocationList;
	}

	public void setRevocationList(String revocationList) {
		this.revocationList = revocationList;
	}

	public JSONObject asJsonObject(String type) {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.TYPE_KEY, type);
		jsonObject.put(Constants.ID_KEY, getId());
		jsonObject.put(Constants.CONTEXT_KEY, Constants.CONTEXT_VALUE);
		String name = getNameWithScan();
		if (StringHelper.containsNonWhitespace(name)) {
			jsonObject.put(Constants.NAME_KEY, name);
		}
		if (StringHelper.containsNonWhitespace(getUrl())) {
			jsonObject.put(Constants.URL_KEY, getUrl());
		}
		if (StringHelper.containsNonWhitespace(getEmail())) {
			jsonObject.put(Constants.EMAIL_KEY, getEmail());
		}
		if (StringHelper.containsNonWhitespace(getPublicKey())) {
			jsonObject.put(PUBLIC_KEY, getPublicKey());
		}
		if (StringHelper.containsNonWhitespace(getRevocationList())) {
			jsonObject.put(REVOCATION_LIST_KEY, getRevocationList());
		}
		return jsonObject;
	}
}
