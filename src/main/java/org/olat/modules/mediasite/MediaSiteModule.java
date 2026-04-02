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
package org.olat.modules.mediasite;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Initial date: 07.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class MediaSiteModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String MEDIASITE_ENABLED 				= "mediasite.enabled";
	private static final String MEDIASITE_GLOBAL_LOGIN_ENABLED	= "mediasite.global.login.enabled";
	private static final String MEDIASITE_ENTERPRISE_KEY 		= "mediasite.enterprise.key";
	private static final String MEDIASITE_ENTERPRISE_SECRET 	= "mediasite.enterprise.secret";
	private static final String MEDIASITE_BASE_URL 				= "mediasite.base.url";
	private static final String MEDIASITE_ADMINISTRATION_URL	= "mediasite.administration.url";
	private static final String MEDIASITE_SERVER_NAME			= "mediasite.server.name";
	private static final String MEDIASITE_USERNAME_PROPERTY		= "mediasite.username.property.key";
	private static final String MEDIASITE_SUPRESS_AGREEMENT		= "mediasite.supress.data.transmission.agreement";
	private static final String MEDIASITE_LTI13_CLIENT_ID		= "mediasite.lti13.client.id";
	private static final String MEDIASITE_LTI13_DEPLOYMENT_ID	= "mediasite.lti13.deployment.id";
	private static final String MEDIASITE_LTI13_INITIATE_LOGIN_URL	= "mediasite.lti13.initiate.login.url";
	private static final String MEDIASITE_LTI13_REDIRECT_URL	= "mediasite.lti13.redirect.url";
	private static final String MEDIASITE_LTI13_JWKS_URL		= "mediasite.lti13.jwks.url";
	private static final String MEDIASITE_LTI_VERSION			= "mediasite.lti.version";

	@Value("${mediasite.enabled}")
	private boolean enabled;
	@Value("${mediasite.global.login.enabled}")
	private boolean globalLoginEnabled;
	@Value("${mediasite.enterprise.key}")
	private String enterpriseKey;
	@Value("${mediasite.enterprise.secret}")
	private String enterpriseSecret;
	@Value("${mediasite.base.url}")
	private String baseURL;
	@Value("${mediasite.administration.url}")
	private String administrationURL;
	@Value("${mediasite.server.name}")
	private String serverName;
	@Value("${mediasite.username.property.key}")
	private String usernameProperty;
	@Value("${mediasite.supress.data.transmission.agreement}")
	private boolean supressDataTransmissionAgreement;
	@Value("${mediasite.lti13.client.id:}")
	private String lti13ClientId;
	@Value("${mediasite.lti13.deployment.id:}")
	private String lti13DeploymentId;
	@Value("${mediasite.lti13.initiate.login.url:}")
	private String lti13InitiateLoginUrl;
	@Value("${mediasite.lti13.redirect.url:}")
	private String lti13RedirectUrl;
	@Value("${mediasite.lti13.jwks.url:}")
	private String lti13JwksUrl;
	@Value("${mediasite.lti.version:lti_1_1}")
	private String ltiVersion;

	public MediaSiteModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		enabled = getBooleanPropertyValue(MEDIASITE_ENABLED) || enabled;
		globalLoginEnabled = getBooleanPropertyValue(MEDIASITE_GLOBAL_LOGIN_ENABLED) || globalLoginEnabled;
		enterpriseKey = getStringPropertyValue(MEDIASITE_ENTERPRISE_KEY, enterpriseKey);
		enterpriseSecret = getStringPropertyValue(MEDIASITE_ENTERPRISE_SECRET, enterpriseSecret);
		baseURL = getStringPropertyValue(MEDIASITE_BASE_URL, baseURL);
		administrationURL = getStringPropertyValue(MEDIASITE_ADMINISTRATION_URL, administrationURL);
		serverName = getStringPropertyValue(MEDIASITE_SERVER_NAME, serverName);
		usernameProperty = getStringPropertyValue(MEDIASITE_USERNAME_PROPERTY, usernameProperty);
		supressDataTransmissionAgreement = getBooleanPropertyValue(MEDIASITE_SUPRESS_AGREEMENT) || supressDataTransmissionAgreement;
		lti13ClientId = getStringPropertyValue(MEDIASITE_LTI13_CLIENT_ID, lti13ClientId);
		lti13DeploymentId = getStringPropertyValue(MEDIASITE_LTI13_DEPLOYMENT_ID, lti13DeploymentId);
		lti13InitiateLoginUrl = getStringPropertyValue(MEDIASITE_LTI13_INITIATE_LOGIN_URL, lti13InitiateLoginUrl);
		lti13RedirectUrl = getStringPropertyValue(MEDIASITE_LTI13_REDIRECT_URL, lti13RedirectUrl);
		lti13JwksUrl = getStringPropertyValue(MEDIASITE_LTI13_JWKS_URL, lti13JwksUrl);
		ltiVersion = getStringPropertyValue(MEDIASITE_LTI_VERSION, ltiVersion);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(MEDIASITE_ENABLED, enabled, true);
	}
	
	public void setGlobalLoginEnabled(boolean globalLoginEnabled) {
		this.globalLoginEnabled = globalLoginEnabled;
		setBooleanProperty(MEDIASITE_GLOBAL_LOGIN_ENABLED, globalLoginEnabled, true);
	}
	
	public void setEnterpriseKey(String enterpriseKey) {
		this.enterpriseKey = enterpriseKey;
		setStringProperty(MEDIASITE_ENTERPRISE_KEY, enterpriseKey, true);
	}
	
	public void setEnterpriseSecret(String enterpriseSecret) {
		this.enterpriseSecret = enterpriseSecret;
		setStringProperty(MEDIASITE_ENTERPRISE_SECRET, enterpriseSecret, true);
	}
	
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
		setStringProperty(MEDIASITE_BASE_URL, baseURL, true);
	}
	
	public void setAdministrationURL(String administrationURL) {
		this.administrationURL = administrationURL;
		setStringProperty(MEDIASITE_ADMINISTRATION_URL, administrationURL, true);
	}
	
	public void setServerName(String serverName) {
		this.serverName = serverName;
		setStringProperty(MEDIASITE_SERVER_NAME, serverName, true);
	}
	
	public void setUsernameProperty(String usernameProperty) {
		this.usernameProperty = usernameProperty;
		setStringProperty(MEDIASITE_USERNAME_PROPERTY, usernameProperty, true);
	}
	
	public void setSupressDataTransmissionAgreement(boolean supressDataTransmissionAgreement) {
		this.supressDataTransmissionAgreement = supressDataTransmissionAgreement;
		setBooleanProperty(MEDIASITE_SUPRESS_AGREEMENT, supressDataTransmissionAgreement, true);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isGlobalLoginEnabled() {
		return globalLoginEnabled;
	}
	
	public String getEnterpriseKey() {
		return enterpriseKey;
	}
	
	public String getEnterpriseSecret() {
		return enterpriseSecret;
	}
	
	public String getBaseURL() {
		return baseURL;
	}
	
	public String getAdministrationURL() {
		return administrationURL;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String getUsernameProperty() {
		return usernameProperty;
	}
	
	public boolean isSupressDataTransmissionAgreement() {
		return supressDataTransmissionAgreement;
	}

	public String getLti13ClientId() {
		return lti13ClientId;
	}

	public void setLti13ClientId(String lti13ClientId) {
		this.lti13ClientId = lti13ClientId;
		setStringProperty(MEDIASITE_LTI13_CLIENT_ID, lti13ClientId, true);
	}

	public String getLti13DeploymentId() {
		return lti13DeploymentId;
	}

	public void setLti13DeploymentId(String lti13DeploymentId) {
		this.lti13DeploymentId = lti13DeploymentId;
		setStringProperty(MEDIASITE_LTI13_DEPLOYMENT_ID, lti13DeploymentId, true);
	}

	public String getLti13InitiateLoginUrl() {
		return lti13InitiateLoginUrl;
	}

	public void setLti13InitiateLoginUrl(String lti13InitiateLoginUrl) {
		this.lti13InitiateLoginUrl = lti13InitiateLoginUrl;
		setStringProperty(MEDIASITE_LTI13_INITIATE_LOGIN_URL, lti13InitiateLoginUrl, true);
	}

	public String getLti13RedirectUrl() {
		return lti13RedirectUrl;
	}

	public void setLti13RedirectUrl(String lti13RedirectUrl) {
		this.lti13RedirectUrl = lti13RedirectUrl;
		setStringProperty(MEDIASITE_LTI13_REDIRECT_URL, lti13RedirectUrl, true);
	}

	public String getLti13JwksUrl() {
		return lti13JwksUrl;
	}

	public void setLti13JwksUrl(String lti13JwksUrl) {
		this.lti13JwksUrl = lti13JwksUrl;
		setStringProperty(MEDIASITE_LTI13_JWKS_URL, lti13JwksUrl, true);
	}
	
	public LtiVersion getLtiVersion() {
		return LtiVersion.valueOf(ltiVersion);
	}
	
	public void setLtiVersion(LtiVersion ltiVersion) {
		this.ltiVersion = ltiVersion.name();
		setStringProperty(MEDIASITE_LTI_VERSION, this.ltiVersion, true);
	}
	
	public Set<LtiVersion> availableGlobalServerConfigurations() {
		Set<LtiVersion> result = new HashSet<>();
		if (StringHelper.containsNonWhitespace(getEnterpriseKey()) && 
				StringHelper.containsNonWhitespace(getEnterpriseSecret()) && 
				StringHelper.containsNonWhitespace(getBaseURL()) && 
				StringHelper.containsNonWhitespace(getAdministrationURL()) && 
				StringHelper.containsNonWhitespace(getUsernameProperty())) {
			result.add(LtiVersion.lti_1_1);
		}
		if (StringHelper.containsNonWhitespace(getLti13ClientId()) && 
				StringHelper.containsNonWhitespace(getLti13DeploymentId()) &&
				StringHelper.containsNonWhitespace(getLti13InitiateLoginUrl()) &&
				StringHelper.containsNonWhitespace(getLti13RedirectUrl()) &&
				StringHelper.containsNonWhitespace(getLti13JwksUrl())) {
			result.add(LtiVersion.lti_1_3);
		}
		return result;
	}

}
