/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.ResidentKeyRequirement;
import com.webauthn4j.data.UserVerificationRequirement;

/**
 * Initial Date:  04.08.2004
 *
 * @author Mike Stock
 * @author guido
 */
@Service("loginModule")
public class LoginModule extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(LoginModule.class);
	
	private static final OrganisationRoles[] policyRoles = new OrganisationRoles[] {
		OrganisationRoles.sysadmin, OrganisationRoles.administrator, OrganisationRoles.principal,
		OrganisationRoles.rolesmanager, OrganisationRoles.usermanager,
		OrganisationRoles.learnresourcemanager, OrganisationRoles.groupmanager,
		OrganisationRoles.lecturemanager, OrganisationRoles.projectmanager, OrganisationRoles.qualitymanager,
		OrganisationRoles.poolmanager, OrganisationRoles.linemanager, OrganisationRoles.educationmanager	
	};
	
	public static final String DISABLED = "disabled";
	public static final String FORBIDDEN = "forbidden";
	public static final String AT_LEAST_1 = "atLeast1";
	public static final String AT_LEAST_2 = "atLeast2";
	public static final String AT_LEAST_3 = "atLeast3";
	public static final String VALIDATE_SEPARATELY = "validateSeparately";
	
	private static final String USERNAME_REGEX = "username.regex";
	private static final String CHANGE_ONCE = "password.change.once";
	private static final String MIN_LENGTH = "password.min.length";
	private static final String MAX_LENGTH = "password.max.length";
	private static final String LETTERS = "password.letters";
	private static final String LETTERS_UPPER = "password.letters.upper";
	private static final String LETTERS_LOWER = "password.letters.lower";
	private static final String DIGITS_SPECIALS = "password.digits.specials";
	private static final String DIGITS = "password.digits";
	private static final String SPECIALS = "password.specials";
	private static final String FORBIDDEN_USERNAME = "password.forbidden.username";
	private static final String FORBIDDEN_FIRSTNAME = "password.forbidden.firstname";
	private static final String FORBIDDEN_LASTNAME = "password.forbidden.lastname";
	private static final String MAX_AGE = "password.max.age";
	private static final String MAX_AGE_AUTHOR = "password.max.age.author";
	private static final String MAX_AGE_GROUPMANAGER = "password.max.age.groupmanager";
	private static final String MAX_AGE_USERMANAGER = "password.max.age.usermanager";
	private static final String MAX_AGE_ROLESMANAGER = "password.max.age.rolesmanager";
	private static final String MAX_AGE_LEARNRESOURCEMANAGER = "password.max.age.learnresourcemanager";
	private static final String MAX_AGE_POOLMANAGER = "password.max.age.poolmanager";
	private static final String MAX_AGE_CURRICULUMMANAGER = "password.max.age.curriculummanager";
	private static final String MAX_AGE_LINEMANAGER = "password.max.age.linemanager";
	private static final String MAX_AGE_EDUCATIONMANAGER = "password.max.age.educationmanager";
	private static final String MAX_AGE_PROJECTMANAGER = "password.max.age.projectmanager";
	private static final String MAX_AGE_QUALITYMANAGER = "password.max.age.qualitymanager";
	private static final String MAX_AGE_LECTUREMANAGER = "password.max.age.lecturemanager";
	private static final String MAX_AGE_PRINCIPAL = "password.max.age.principal";
	private static final String MAX_AGE_ADMINISTRATOR = "password.max.age.administrator";
	private static final String MAX_AGE_SYSADMIN = "password.max.age.sysadmin";
	private static final String HISTORY = "password.history";
	
	private static final String OLAT_PROVIDER_BUTTON = "olatprovider.login.button";
	private static final String OLAT_PROVIDER_PASSKEY = "olatprovider.passkey.enable";
	private static final String PASSKEY_USER_VERIFICATION = "olatprovider.passkey.user.verification";
	private static final String PASSKEY_RESIDENT_KEY = "olatprovider.passkey.resident.key";
	private static final String PASSKEY_ATTESTATION_CONVEYANCE = "olatprovider.attestation.conveyance.preference";
	private static final String PASSKEY_LEVEL_PREFIX = "olatprovider.passkey.level.";
	private static final String PASSKEY_UPGRADE = "olatprovider.passkey.upgrade";
	
	private static final String PASSKEY_MAX_SKIP = "olatprovider.passkey.max.skip";
	
	private static final String LOGIN_FAQ_URL = "login.faq.url";
	
	private static final Set<Encoder.Algorithm> CONSIDERED_SECURE_HASH_ALGRITHM = Set.of(Encoder.Algorithm.sha512, Encoder.Algorithm.argon2id, Encoder.Algorithm.pbkdf2);
	
	@Autowired
	private List<AuthenticationProvider> authenticationProviders;
	
	@Value("${login.attackPreventionEnabled:true}")
	private boolean attackPreventionEnabled;
	@Value("${login.AttackPreventionMaxattempts:5}")
	private int attackPreventionMaxAttempts;
	@Value("${login.AttackPreventionTimeoutmin:5}")
	private int attackPreventionTimeout;
	
	@Value("${username.regex}")
	private String usernameRegex;
	private Pattern usernamePattern;

	@Value("${password.change.valid.minutes.gui}")
	private Integer validUntilMinutesGui;
	
	@Value("${password.change.once:false}")
	private boolean passwordChangeOnce;
	
	@Value("${password.min.length}")
	private int passwordMinLength;
	@Value("${password.max.length}")
	private int passwordMaxLength;
	@Value("${password.letters}")
	private String passwordLetters;
	@Value("${password.letters.uppercase}")
	private String passwordLettersUppercase;
	@Value("${password.letters.lowercase}")
	private String passwordLettersLowercase;
	@Value("${password.digits.special.signs}")
	private String passwordDigitsAndSpecialSigns;
	@Value("${password.digits}")
	private String passwordDigits;
	@Value("${password.special.signs}")
	private String passwordSpecialSigns;
	@Value("${password.forbidden.username}")
	private boolean passwordUsernameForbidden;
	@Value("${password.forbidden.firstname}")
	private boolean passwordFirstnameForbidden;
	@Value("${password.forbidden.lastname}")
	private boolean passwordLastnameForbidden;
	
	@Value("${password.max.age}")
	private int passwordMaxAge;
	@Value("${password.max.age.author}")
	private int passwordMaxAgeAuthor;
	@Value("${password.max.age.groupmanager}")
	private int passwordMaxAgeGroupManager;
	@Value("${password.max.age.poolmanager}")
	private int passwordMaxAgePoolManager;
	@Value("${password.max.age.usermanager}")
	private int passwordMaxAgeUserManager;
	@Value("${password.max.age.rolesmanager}")
	private int passwordMaxAgeRolesManager;
	@Value("${password.max.age.learnresourcemanager}")
	private int passwordMaxAgeLearnResourceManager;
	@Value("${password.max.age.curriculummanager}")
	private int passwordMaxAgeCurriculumManager;
	@Value("${password.max.age.linemanager}")
	private int passwordMaxAgeLineManager;
	@Value("${password.max.age.educationmanager}")
	private int passwordMaxAgeEducationManager;
	@Value("${password.max.age.projectmanager}")
	private int passwordMaxAgeProjectManager;
	@Value("${password.max.age.qualitymanager}")
	private int passwordMaxAgeQualityManager;
	@Value("${password.max.age.lecturemanager}")
	private int passwordMaxAgeLectureManager;
	@Value("${password.max.age.principal}")
	private int passwordMaxAgePrincipal;
	@Value("${password.max.age.administrator}")
	private int passwordMaxAgeAdministrator;
	@Value("${password.max.age.sysadmin}")
	private int passwordMaxAgeSysAdmin;
	
	@Value("${password.history:10}")
	private int passwordHistory;
	
	@Value("${password.encoder:argon2}")
	private String passwordEncoder;

	@Value("${invitation.login:enabled}")
	private String invitationEnabled;
	@Value("${guest.login:enabled}")
	private String guestLoginEnabled;
	@Value("${guest.login.links:enabled}")
	private String guestLoginLinksEnabled;
	
	private String defaultProviderName = "OLAT";
	@Value("${login.using.username.or.email.enabled:true}")
	private boolean allowLoginUsingEmail;
	
	@Value("${olatprovider.login.button:false}")
	private boolean olatProviderLoginButton;
	
	@Value("${olatprovider.passkey.enable:true}")
	private boolean olatProviderWithPasskey;
	@Value("${olatprovider.passkey.user.verification}")
	private String passkeyUserVerification;
	@Value("${olatprovider.passkey.resident.key}")
	private String passkeyResidentKey;
	@Value("${olatprovider.attestation.conveyance.preference}")
	private String passkeyAttestationConveyancePreference;
	@Value("${olatprovider.passkey.timeout:120}")
	private int passkeyTimeout;
	@Value("${olatprovider.passkey.upgrade:true}")
	private String passkeyUpgrade;
	
	@Value("${olatprovider.passkey.level.sysadmin:level2}")
	private String passkeyLevelSystemAdministrator;
	@Value("${olatprovider.passkey.level.administrator:level2}")
	private String passkeyLevelAdministrator;
	@Value("${olatprovider.passkey.level.usermanager:level2}")
	private String passkeyLevelUserManager;
	@Value("${olatprovider.passkey.level.rolesmanager:level2}")
	private String passkeyLevelRolesManager;
	@Value("${olatprovider.passkey.level.learnresourcemanager:level2}")
	private String passkeyLevelLearnResourceManager;
	@Value("${olatprovider.passkey.level.lecturemanager:level2}")
	private String passkeyLevelLectureManager;
	@Value("${olatprovider.passkey.level.groupmanager:level2}")
	private String passkeyLevelGroupManager;
	@Value("${olatprovider.passkey.level.poolmanager:level2}")
	private String passkeyLevelPoolManager;
	@Value("${olatprovider.passkey.level.curriculummanager:level2}")
	private String passkeyLevelCurriculumManager;
	@Value("${olatprovider.passkey.level.qualitymanager:level2}")
	private String passkeyLevelQualityManager;
	@Value("${olatprovider.passkey.level.projectmanager:level2}")
	private String passkeyLevelProjectManager;
	@Value("${olatprovider.passkey.level.linemanager:level2}")
	private String passkeyLevelLineManager;
	@Value("${olatprovider.passkey.level.educationmanager:level2}")
	private String passkeyLevelEducationManager;
	@Value("${olatprovider.passkey.level.principal:level2}")
	private String passkeyLevelPrincipal;
	@Value("${olatprovider.passkey.level.author:level2}")
	private String passkeyLevelAuthor;
	@Value("${olatprovider.passkey.level.user:level1}")
	private String passkeyLevelUser;
	
	@Value("${olatprovider.passkey.max.skip:5}")
	private String passkeyMaxSkip;
	
	@Value("${login.faq.url}")
	private String loginFaqUrl;

	private CoordinatorManager coordinatorManager;
	private CacheWrapper<String,Integer> failedLoginCache;

	@Autowired
	private UserModule userModule;
	
	@Autowired
	public LoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		this.coordinatorManager = coordinatorManager;
	}
	
	@Override
	public void init() {
		// configure timed cache default params: refresh 1 minute, timeout according to configuration
		failedLoginCache = coordinatorManager.getCoordinator().getCacher().getCache(LoginModule.class.getSimpleName(), "blockafterfailedattempts");
				
		updateProperties();
		validateProperties();
		
		boolean defaultProviderFound = false;
		for (Iterator<AuthenticationProvider> iterator = authenticationProviders.iterator(); iterator.hasNext();) {
			AuthenticationProvider provider = iterator.next();
			if (provider.isDefault()) {
				defaultProviderFound = true;
				defaultProviderName = provider.getName();
				log.info("Using default authentication provider '{}'.", defaultProviderName);
			}
		}
		
		if (!defaultProviderFound) {
			throw new StartupException("Defined DefaultAuthProvider::" + defaultProviderName + " not existent or not enabled. Please fix.");
		}
	}

	private void validateProperties() {
		validateValidationConfig("password.letters", passwordLetters,
				Arrays.asList(DISABLED, FORBIDDEN, AT_LEAST_1, AT_LEAST_2, AT_LEAST_3, VALIDATE_SEPARATELY));
		validateValidationConfig("password.letters.uppercase", passwordLettersUppercase,
				Arrays.asList(DISABLED, FORBIDDEN, AT_LEAST_1, AT_LEAST_2, AT_LEAST_3));
		validateValidationConfig("password.letters.lowercase", passwordLettersLowercase,
				Arrays.asList(DISABLED, FORBIDDEN, AT_LEAST_1, AT_LEAST_2, AT_LEAST_3));
		validateValidationConfig("password.digits.special.signs", passwordDigitsAndSpecialSigns,
				Arrays.asList(DISABLED, FORBIDDEN, AT_LEAST_1, AT_LEAST_2, AT_LEAST_3, VALIDATE_SEPARATELY));
		validateValidationConfig("password.digits", passwordDigits,
				Arrays.asList(DISABLED, FORBIDDEN, AT_LEAST_1, AT_LEAST_2, AT_LEAST_3));
		validateValidationConfig("password.special.signs", passwordSpecialSigns,
				Arrays.asList(DISABLED, FORBIDDEN, AT_LEAST_1, AT_LEAST_2, AT_LEAST_3));
	}
	
	private void validateValidationConfig(String configName, String configValue, List<String> validValues) {
		if (!validValues.contains(configValue)) {
			logInvalidValidationConfig(configName, configValue, validValues);
		}
	}
	
	private void logInvalidValidationConfig(String configName, String configValue, List<String> validValues) {
		log.error("Invalid configuration for {} (value = {}). Valid values are: {}", 
				configName, configValue, validValues.stream().collect(Collectors.joining(", ")));
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	@Override
	protected void initDefaultProperties() {
		super.initDefaultProperties();
		if (attackPreventionEnabled) {
			log.info("Attack prevention enabled. Max number of attempts: {}, timeout: {} minutes.",
					attackPreventionMaxAttempts, attackPreventionTimeout);
		} else {
			log.info("Attack prevention is disabled.");
		}
		
		//compatibility with older settings
		if("true".equals(guestLoginEnabled)) {
			guestLoginEnabled = "enabled";
		} else if("false".equals(guestLoginEnabled)) {
			guestLoginEnabled = "disabled";
		}
		
		if("true".equals(guestLoginLinksEnabled)) {
			guestLoginLinksEnabled = "enabled";
		} else if("false".equals(guestLoginLinksEnabled)) {
			guestLoginLinksEnabled = "disabled";
		}
		
		if("true".equals(invitationEnabled)) {
			invitationEnabled = "enabled";
		} else if("false".equals(guestLoginLinksEnabled)) {
			invitationEnabled = "disabled";
		}
		
		if (isGuestLoginEnabled()) {
			log.info("Guest login on login page enabled");
		} else {
			log.info("Guest login on login page disabled or not properly configured. ");
		}
		
		if (isInvitationEnabled()) {
			log.info("Invitation login enabled");
		} else {
			log.info("Invitation login disabled");
		}
	}
	
	private void updateProperties() {
		//set properties
		String invitation = getStringPropertyValue("invitation.login", true);
		if(StringHelper.containsNonWhitespace(invitation)) {
			invitationEnabled = invitation;
		}
		String guestLogin = getStringPropertyValue("guest.login", true);
		if(StringHelper.containsNonWhitespace(guestLogin)) {
			guestLoginEnabled = guestLogin;
		}
		String guestLoginLinks = getStringPropertyValue("guest.login.links", true);
		if(StringHelper.containsNonWhitespace(guestLoginLinks)) {
			guestLoginLinksEnabled = guestLoginLinks;
		}
		String usernameOrEmailLogin = getStringPropertyValue("login.using.username.or.email.enabled", true);
		if(StringHelper.containsNonWhitespace(usernameOrEmailLogin)) {
			allowLoginUsingEmail = "true".equals(usernameOrEmailLogin);
		}
		
		String usernameRegexObj = getStringPropertyValue(USERNAME_REGEX, true);
		if(StringHelper.containsNonWhitespace(usernameRegexObj)) {
			usernameRegex = usernameRegexObj;
		}
		
		String changeOnce = getStringPropertyValue(CHANGE_ONCE, true);
		if(StringHelper.containsNonWhitespace(changeOnce)) {
			passwordChangeOnce = "true".equals(changeOnce);
		}
		
		String passwordMinLengthObj = getStringPropertyValue(MIN_LENGTH, true);
		if(StringHelper.containsNonWhitespace(passwordMinLengthObj)) {
			passwordMinLength = Integer.parseInt(passwordMinLengthObj);
		}
		
		String passwordMaxLengthObj = getStringPropertyValue(MAX_LENGTH, true);
		if(StringHelper.containsNonWhitespace(passwordMaxLengthObj)) {
			passwordMaxLength = Integer.parseInt(passwordMaxLengthObj);
		}
		
		String passwordLettersObj = getStringPropertyValue(LETTERS, true);
		if(StringHelper.containsNonWhitespace(passwordLettersObj)) {
			passwordLetters = passwordLettersObj;
		}
		
		String passwordLettersUppercaseObj = getStringPropertyValue(LETTERS_UPPER, true);
		if(StringHelper.containsNonWhitespace(passwordLettersUppercaseObj)) {
			passwordLettersUppercase = passwordLettersUppercaseObj;
		}
		
		String passwordLettersLowercaseObj = getStringPropertyValue(LETTERS_LOWER, true);
		if(StringHelper.containsNonWhitespace(passwordLettersLowercaseObj)) {
			passwordLettersLowercase = passwordLettersLowercaseObj;
		}
		
		String passwordDigitsAndSpecialSignsObj = getStringPropertyValue(DIGITS_SPECIALS, true);
		if(StringHelper.containsNonWhitespace(passwordDigitsAndSpecialSignsObj)) {
			passwordDigitsAndSpecialSigns = passwordDigitsAndSpecialSignsObj;
		}
		
		String passwordDigitsObj = getStringPropertyValue(DIGITS, true);
		if(StringHelper.containsNonWhitespace(passwordDigitsObj)) {
			passwordDigits = passwordDigitsObj;
		}
		
		String passwordSpecialSignsObj = getStringPropertyValue(SPECIALS, true);
		if(StringHelper.containsNonWhitespace(passwordSpecialSignsObj)) {
			passwordSpecialSigns = passwordSpecialSignsObj;
		}
		
		String usernameForbiddenObj = getStringPropertyValue(FORBIDDEN_USERNAME, true);
		if(StringHelper.containsNonWhitespace(usernameForbiddenObj)) {
			passwordUsernameForbidden = "true".equals(usernameForbiddenObj);
		}
		
		String firstnameForbiddenObj = getStringPropertyValue(FORBIDDEN_FIRSTNAME, true);
		if(StringHelper.containsNonWhitespace(firstnameForbiddenObj)) {
			passwordFirstnameForbidden = "true".equals(firstnameForbiddenObj);
		}
		
		String lastnameForbiddenObj = getStringPropertyValue(FORBIDDEN_LASTNAME, true);
		if(StringHelper.containsNonWhitespace(lastnameForbiddenObj)) {
			passwordLastnameForbidden = "true".equals(lastnameForbiddenObj);
		}
		
		int validUntilMinutesGuiInt = getIntPropertyValue("password.change.valid.minutes.gui");
		if (validUntilMinutesGuiInt > 0) {
			validUntilMinutesGui = validUntilMinutesGuiInt;
		}
		
		passwordMaxAge = getAgeValue(MAX_AGE, passwordMaxAge);
		passwordMaxAgeAuthor = getAgeValue(MAX_AGE_AUTHOR, passwordMaxAgeAuthor);
		passwordMaxAgeGroupManager = getAgeValue(MAX_AGE_GROUPMANAGER, passwordMaxAgeGroupManager);
		passwordMaxAgePoolManager = getAgeValue(MAX_AGE_POOLMANAGER, passwordMaxAgePoolManager);
		passwordMaxAgeUserManager = getAgeValue(MAX_AGE_USERMANAGER, passwordMaxAgeUserManager);
		passwordMaxAgeRolesManager = getAgeValue(MAX_AGE_ROLESMANAGER, passwordMaxAgeRolesManager);
		passwordMaxAgeLearnResourceManager = getAgeValue(MAX_AGE_LEARNRESOURCEMANAGER, passwordMaxAgeLearnResourceManager);
		passwordMaxAgeCurriculumManager = getAgeValue(MAX_AGE_CURRICULUMMANAGER, passwordMaxAgeCurriculumManager);
		passwordMaxAgeLectureManager = getAgeValue(MAX_AGE_LECTUREMANAGER, passwordMaxAgeLectureManager);
		passwordMaxAgeProjectManager = getAgeValue(MAX_AGE_PROJECTMANAGER, passwordMaxAgeProjectManager);
		passwordMaxAgeQualityManager = getAgeValue(MAX_AGE_QUALITYMANAGER, passwordMaxAgeQualityManager);
		passwordMaxAgeLineManager = getAgeValue(MAX_AGE_LINEMANAGER, passwordMaxAgeLineManager);
		passwordMaxAgeEducationManager = getAgeValue(MAX_AGE_EDUCATIONMANAGER, passwordMaxAgeEducationManager);
		passwordMaxAgePrincipal = getAgeValue(MAX_AGE_PRINCIPAL, passwordMaxAgePrincipal);
		passwordMaxAgeAdministrator = getAgeValue(MAX_AGE_ADMINISTRATOR, passwordMaxAgeAdministrator);
		passwordMaxAgeSysAdmin = getAgeValue(MAX_AGE_SYSADMIN, passwordMaxAgeSysAdmin);
		
		String history = getStringPropertyValue(HISTORY, true);
		if(StringHelper.containsNonWhitespace(history)) {
			passwordHistory = Integer.parseInt(history);
		}
		
		String olatProviderPasskey = getStringPropertyValue(OLAT_PROVIDER_PASSKEY, true);
		if(StringHelper.containsNonWhitespace(olatProviderPasskey)) {
			olatProviderWithPasskey = "true".equals(olatProviderPasskey);
		}
		
		String olatProviderButtonObj = getStringPropertyValue(OLAT_PROVIDER_BUTTON, true);
		if(StringHelper.containsNonWhitespace(olatProviderButtonObj)) {
			olatProviderLoginButton = "true".equals(olatProviderButtonObj);
		}
		
		passkeyUserVerification = getStringPropertyValue(PASSKEY_USER_VERIFICATION, passkeyUserVerification);
		passkeyResidentKey = getStringPropertyValue(PASSKEY_RESIDENT_KEY, passkeyResidentKey);
		passkeyAttestationConveyancePreference = getStringPropertyValue(PASSKEY_ATTESTATION_CONVEYANCE, passkeyAttestationConveyancePreference);

		passkeyLevelSystemAdministrator = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.sysadmin, passkeyLevelSystemAdministrator);
		passkeyLevelAdministrator = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.administrator, passkeyLevelAdministrator);
		passkeyLevelUserManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.usermanager, passkeyLevelUserManager);
		passkeyLevelRolesManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.rolesmanager, passkeyLevelRolesManager);
		passkeyLevelLearnResourceManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.learnresourcemanager, passkeyLevelLearnResourceManager);
		passkeyLevelLectureManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.lecturemanager, passkeyLevelLectureManager);
		passkeyLevelGroupManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.groupmanager, passkeyLevelGroupManager);
		passkeyLevelPoolManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.poolmanager, passkeyLevelPoolManager);
		passkeyLevelCurriculumManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.curriculummanager, passkeyLevelCurriculumManager);
		passkeyLevelQualityManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.qualitymanager, passkeyLevelQualityManager);
		passkeyLevelProjectManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.projectmanager, passkeyLevelProjectManager);
		passkeyLevelLineManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.linemanager, passkeyLevelLineManager);
		passkeyLevelEducationManager = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.educationmanager, passkeyLevelEducationManager);
		passkeyLevelPrincipal = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.principal, passkeyLevelPrincipal);
		passkeyLevelAuthor = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.author, passkeyLevelAuthor);
		passkeyLevelUser = getStringPropertyValue(PASSKEY_LEVEL_PREFIX + OrganisationRoles.user, passkeyLevelUser);
		
		passkeyUpgrade = getStringPropertyValue(PASSKEY_UPGRADE , passkeyUpgrade);
		
		loginFaqUrl = this.getStringPropertyValue(LOGIN_FAQ_URL, loginFaqUrl);
	}

	private int getAgeValue(String propertyName, int defaultValue) {
		String value = getStringPropertyValue(propertyName, Integer.toString(defaultValue));
		if(StringHelper.containsNonWhitespace(value)) {
			return Integer.parseInt(value);
		}
		return defaultValue;
	}

	/**
	 * @return The configured default login provider.
	 */
	public String getDefaultProviderName() {
		return defaultProviderName;
	}
	
	public Encoder.Algorithm getDefaultHashAlgorithm() {
		Encoder.Algorithm algorithm = Encoder.Algorithm.secureValueOf(passwordEncoder, Encoder.Algorithm.argon2id);
		return CONSIDERED_SECURE_HASH_ALGRITHM.contains(algorithm) ? algorithm : Encoder.Algorithm.argon2id;
	}
	
	/**
	 * @param provider
	 * @return AuthenticationProvider implementation.
	 */
	public AuthenticationProvider getAuthenticationProvider(String provider) {
		AuthenticationProvider authenticationProvider = null;
		for(AuthenticationProvider authProvider:authenticationProviders) {
			if(authProvider.getName().equalsIgnoreCase(provider) || authProvider.accept(provider)) {
				authenticationProvider = authProvider;
			}
		}
		return authenticationProvider;
	}
	
	/**
	 * This method will always return something and will try to find some
	 * matching provider. It will find LDAP'A0 -> LDAP or return the
	 * default provider.
	 * 
	 * @param provider
	 * @return
	 */
	public AuthenticationProvider getAuthenticationProviderHeuristic(String provider) {
		//first exact match
		AuthenticationProvider authenticationProvider = getAuthenticationProvider(provider);
		if(authenticationProvider == null && StringHelper.containsNonWhitespace(provider)) {
			String upperedCaseProvider = provider.toUpperCase();
			for(AuthenticationProvider authProvider:authenticationProviders) {
				if(upperedCaseProvider.contains(authProvider.getName().toUpperCase())) {
					authenticationProvider = authProvider;
					break;
				}
			}
		}
		if(authenticationProvider == null) {
			//return default
			for(AuthenticationProvider authProvider:authenticationProviders) {
				if(authProvider.isDefault()) {
					authenticationProvider = authProvider;
					break;
				}
			}
		}
		return authenticationProvider;
	}
	
	/**
	 * @return Collection of available AuthenticationProviders
	 */
	public Collection<AuthenticationProvider> getAuthenticationProviders() {
		return new ArrayList<>(authenticationProviders);
	}
	
	/**
	 * Must be called upon each login attempt. Returns true
	 * if number of login attempts has reached the set limit.
	 * 
	 * @param login The user name
	 * @return True if further logins will be prevented (i.e. max attempts reached).
	 */
	public final boolean registerFailedLoginAttempt(String login) {
		return registerFailedLoginAttempt(login, 1);
	}
	
	/**
	 * Must be called upon each login attempt. Returns true
	 * if number of login attempts has reached the set limit.
	 * 
	 * @param login The user name
	 * @param attemptsFactor Multiple the max number of attempts (Windows WebDAV client will send 7 requests at once, don't block at the first real attempt)
	 * @return True if further logins will be prevented (i.e. max attempts reached).
	 */
	public final boolean registerFailedLoginAttempt(String login, int attemptsFactor) {
		if (!attackPreventionEnabled) {
			return false;
		}
		
		Integer numAttempts = failedLoginCache.get(login);
		if (numAttempts == null) { // create new entry
			numAttempts = Integer.valueOf(1);
			failedLoginCache.put(login, numAttempts);
		} else { // update entry
			numAttempts = Integer.valueOf(numAttempts.intValue() + 1);
			failedLoginCache.update(login, numAttempts);
		}		
		return (numAttempts.intValue() > (attemptsFactor * attackPreventionMaxAttempts));
	}
	
	/**
	 * Clear all failed login attempts for a given login.
	 * @param login
	 */
	public final void clearFailedLoginAttempts(String login) {
		if (attackPreventionEnabled && StringHelper.containsNonWhitespace(login)) {
			failedLoginCache.remove(login);
		}
	}
	
	/**
	 * Tells whether a login is blocked to prevent brute force attacks or not.
	 * @param login
	 * @return True if login is blocked by attack prevention mechanism
	 */
	public final boolean isLoginBlocked(String login) {
		if (!attackPreventionEnabled) return false;
		Integer numAttempts = failedLoginCache.get(login);
		
		if (numAttempts == null) return false;
		else return (numAttempts.intValue() > attackPreventionMaxAttempts);
	}
	
	/**
	 * @return True if guest login must be shown on login screen, false
	 *         otherwise
	 */
	public boolean isGuestLoginEnabled() {
		return "enabled".equals(guestLoginEnabled);
	}
	
	public void setGuestLoginEnabled(boolean enabled) {
		guestLoginEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("guest.login", guestLoginEnabled, true);
	}
	
	public boolean isGuestLoginLinksEnabled() {
		return "enabled".equalsIgnoreCase(guestLoginLinksEnabled);
	}
	
	public void setGuestLoginLinksEnabled(boolean enabled) {
		guestLoginLinksEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("guest.login.links", guestLoginLinksEnabled, true);
	}
	
	/**
	 * This was used to defined the portfolio invitations.
	 * 
	 * @return
	 */
	public boolean isInvitationEnabled() {
		return "enabled".equals(invitationEnabled);
	}
	
	public void setInvitationEnabled(boolean enabled) {
		invitationEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("invitation.login", invitationEnabled, true);
	}
	
	/**
	 * @return Number of minutes a login gets blocked after too many attempts.
	 */
	public Integer getAttackPreventionTimeoutMin() {
		return Integer.valueOf(attackPreventionTimeout);
	}
	
	/**
	 * @return True if login with email is allowed (set in olat.properties)
	 */
	public boolean isAllowLoginUsingEmail() {
		boolean isAllowLoginUsingEmail = allowLoginUsingEmail;
		if (!userModule.isEmailUnique()) {
			isAllowLoginUsingEmail = false;
		}
		return isAllowLoginUsingEmail;
	}
	
	public void setAllowLoginUsingEmail(boolean allow) {
		allowLoginUsingEmail = allow;
		setStringProperty("login.using.username.or.email.enabled", Boolean.toString(allow), true);
	}
	
	public boolean isOlatProviderLoginButton() {
		return olatProviderLoginButton;
	}

	public void setOlatProviderLoginButton(boolean enable) {
		this.olatProviderLoginButton = enable;
		setStringProperty(OLAT_PROVIDER_BUTTON, Boolean.toString(enable), true);
	}

	public String getUsernameRegex() {
		return usernameRegex;
	}

	public void setUsernameRegex(String usernameRegex) {
		this.usernameRegex = usernameRegex;
		this.usernamePattern = null;
		setStringProperty(USERNAME_REGEX, usernameRegex, true);
	}

	public Pattern getUsernamePattern() {
		if (usernamePattern == null) {
			usernamePattern = Pattern.compile(usernameRegex);
		}
		return usernamePattern;
	}

	public Integer getValidUntilMinutesGui() {
		return validUntilMinutesGui;
	}

	public void setValidUntilMinutesGui(Integer validUntilMinutesGui) {
		this.validUntilMinutesGui = validUntilMinutesGui;
		setIntProperty("password.change.valid.minutes.gui", validUntilMinutesGui, true);
	}

	public boolean isPasswordChangeOnce() {
		return passwordChangeOnce;
	}

	public void setPasswordChangeOnce(boolean passwordChangeOnce) {
		this.passwordChangeOnce = passwordChangeOnce;
		setStringProperty(CHANGE_ONCE, passwordChangeOnce ? "true" : "false", true);
	}
	
	public int getPasswordMinLength() {
		return passwordMinLength;
	}

	public void setPasswordMinLength(int passwordMinLength) {
		this.passwordMinLength = passwordMinLength;
		setStringProperty(MIN_LENGTH, String.valueOf(passwordMinLength), true);
	}

	public int getPasswordMaxLength() {
		return passwordMaxLength;
	}

	public void setPasswordMaxLength(int passwordMaxLength) {
		this.passwordMaxLength = passwordMaxLength;
		setStringProperty(MAX_LENGTH, String.valueOf(passwordMaxLength), true);
	}

	public String getPasswordLetters() {
		return passwordLetters;
	}

	public void setPasswordLetters(String passwordLetters) {
		this.passwordLetters = passwordLetters;
		setStringProperty(LETTERS, String.valueOf(passwordLetters), true);
	}

	public String getPasswordLettersUppercase() {
		return passwordLettersUppercase;
	}

	public void setPasswordLettersUppercase(String passwordLettersUppercase) {
		this.passwordLettersUppercase = passwordLettersUppercase;
		setStringProperty(LETTERS_UPPER, String.valueOf(passwordLettersUppercase), true);
	}

	public String getPasswordLettersLowercase() {
		return passwordLettersLowercase;
	}

	public void setPasswordLettersLowercase(String passwordLettersLowercase) {
		this.passwordLettersLowercase = passwordLettersLowercase;
		setStringProperty(LETTERS_LOWER, String.valueOf(passwordLettersLowercase), true);
	}

	public String getPasswordDigitsAndSpecialSigns() {
		return passwordDigitsAndSpecialSigns;
	}

	public void setPasswordDigitsAndSpecialSigns(String passwordDigitsAndSpecialSigns) {
		this.passwordDigitsAndSpecialSigns = passwordDigitsAndSpecialSigns;
		setStringProperty(DIGITS_SPECIALS, String.valueOf(passwordDigitsAndSpecialSigns), true);
	}

	public String getPasswordDigits() {
		return passwordDigits;
	}

	public void setPasswordDigits(String passwordDigits) {
		this.passwordDigits = passwordDigits;
		setStringProperty(DIGITS, String.valueOf(passwordDigits), true);
	}

	public String getPasswordSpecialSigns() {
		return passwordSpecialSigns;
	}

	public void setPasswordSpecialSigns(String passwordSpecialSigns) {
		this.passwordSpecialSigns = passwordSpecialSigns;
		setStringProperty(SPECIALS, String.valueOf(passwordSpecialSigns), true);
	}

	public boolean isPasswordUsernameForbidden() {
		return passwordUsernameForbidden;
	}

	public void setPasswordUsernameForbidden(boolean passwordUsernameForbidden) {
		this.passwordUsernameForbidden = passwordUsernameForbidden;
		setStringProperty(FORBIDDEN_USERNAME, Boolean.toString(passwordUsernameForbidden), true);
	}

	public boolean isPasswordFirstnameForbidden() {
		return passwordFirstnameForbidden;
	}

	public void setPasswordFirstnameForbidden(boolean passwordFirstnameForbidden) {
		this.passwordFirstnameForbidden = passwordFirstnameForbidden;
		setStringProperty(FORBIDDEN_FIRSTNAME, Boolean.toString(passwordFirstnameForbidden), true);
	}

	public boolean isPasswordLastnameForbidden() {
		return passwordLastnameForbidden;
	}

	public void setPasswordLastnameForbidden(boolean passwordLastnameForbidden) {
		this.passwordLastnameForbidden = passwordLastnameForbidden;
		setStringProperty(FORBIDDEN_LASTNAME, Boolean.toString(passwordLastnameForbidden), true);
	}

	public boolean isPasswordAgePolicyConfigured() {
		return passwordMaxAge > 0 || passwordMaxAgeAuthor > 0
				|| passwordMaxAgeGroupManager > 0 || passwordMaxAgePoolManager > 0
				|| passwordMaxAgeUserManager > 0  || passwordMaxAgeRolesManager > 0 
				|| passwordMaxAgeLearnResourceManager > 0 || passwordMaxAgeLectureManager > 0
				|| passwordMaxAgeProjectManager > 0 || passwordMaxAgeQualityManager > 0 
				|| passwordMaxAgeLineManager > 0 || passwordMaxAgeEducationManager > 0
				|| passwordMaxAgeCurriculumManager > 0 || passwordMaxAgePrincipal > 0
				|| passwordMaxAgeAdministrator > 0 || passwordMaxAgeSysAdmin > 0;
	}
	

	
	/**
	 * 
	 * @param roles The roles
	 * @return A number of hours
	 */
	public int getPasswordAgePolicy(Roles roles) {
		int age = passwordMaxAge;
		for(OrganisationRoles policyRole:policyRoles) {
			if(roles.hasRole(policyRole)) {
				age = getMaxAgeOrDefault(age, getPasswordMaxAgeFor(policyRole));
			}
		}
		return age;
	}
	
	/**
	 * 
	 * @param roleMaxAge The max. age
	 * @return A number of hours
	 */
	private int getMaxAgeOrDefault(int currentAge, int roleMaxAge) {
		if(currentAge <= 0 || (roleMaxAge > 0 && roleMaxAge < currentAge)) {
			return roleMaxAge;
		}
		return currentAge;
	}

	/**
	 * The default max. age for a password in hours.
	 * 
	 * @return A number of hours
	 */
	public int getPasswordMaxAge() {
		return passwordMaxAge;
	}

	/**
	 * The default max. age in hours.
	 * 
	 * @param maxAge The age in hours
	 */
	public void setPasswordMaxAge(int maxAge) {
		this.passwordMaxAge = maxAge;
		setStringProperty(MAX_AGE, Integer.toString(maxAge), true);
	}
	
	public int getPasswordMaxAgeFor(OrganisationRoles role) {
		switch(role) {
			case user: return passwordMaxAge;
			case author: return passwordMaxAgeAuthor;
			case usermanager: return passwordMaxAgeUserManager;
			case rolesmanager: return passwordMaxAgeRolesManager;
			case groupmanager: return passwordMaxAgeGroupManager;
			case learnresourcemanager: return passwordMaxAgeLearnResourceManager;
			case poolmanager: return passwordMaxAgePoolManager;
			case curriculummanager: return passwordMaxAgeCurriculumManager;
			case lecturemanager: return passwordMaxAgeLectureManager;
			case projectmanager: return passwordMaxAgeProjectManager;
			case qualitymanager: return passwordMaxAgeQualityManager;
			case linemanager: return passwordMaxAgeLineManager;
			case educationmanager: return passwordMaxAgeEducationManager;
			case principal: return passwordMaxAgePrincipal;
			case administrator: return passwordMaxAgeAdministrator;
			case sysadmin: return passwordMaxAgeSysAdmin;
			default: return passwordMaxAge;
		}
	}
	
	public void setPasswordMaxAgeFor(OrganisationRoles role, int maxAge) {
		switch(role) {
			case user:
				passwordMaxAge = setPasswordMaxAge(MAX_AGE, maxAge);
				break;
			case author:
				passwordMaxAgeAuthor = setPasswordMaxAge(MAX_AGE_AUTHOR, maxAge);
				break;
			case usermanager:
				passwordMaxAgeUserManager = setPasswordMaxAge(MAX_AGE_USERMANAGER, maxAge);
				break;
			case rolesmanager:
				passwordMaxAgeRolesManager = setPasswordMaxAge(MAX_AGE_ROLESMANAGER, maxAge);
				break;
			case groupmanager:
				passwordMaxAgeGroupManager = setPasswordMaxAge(MAX_AGE_GROUPMANAGER, maxAge);
				break;
			case learnresourcemanager:
				passwordMaxAgeLearnResourceManager = setPasswordMaxAge(MAX_AGE_LEARNRESOURCEMANAGER, maxAge);
				break;
			case poolmanager:
				passwordMaxAgePoolManager = setPasswordMaxAge(MAX_AGE_POOLMANAGER, maxAge);
				break;
			case curriculummanager:
				passwordMaxAgeCurriculumManager = setPasswordMaxAge(MAX_AGE_CURRICULUMMANAGER, maxAge);
				break;
			case lecturemanager:
				passwordMaxAgeLectureManager = setPasswordMaxAge(MAX_AGE_LECTUREMANAGER, maxAge);
				break;
			case projectmanager:
				passwordMaxAgeProjectManager = setPasswordMaxAge(MAX_AGE_PROJECTMANAGER, maxAge);
				break;
			case qualitymanager:
				passwordMaxAgeQualityManager = setPasswordMaxAge(MAX_AGE_QUALITYMANAGER, maxAge);
				break;
			case linemanager:
				passwordMaxAgeLineManager = setPasswordMaxAge(MAX_AGE_LINEMANAGER, maxAge);
				break;
			case educationmanager:
				passwordMaxAgeEducationManager = setPasswordMaxAge(MAX_AGE_EDUCATIONMANAGER, maxAge);
				break;
			case principal:
				passwordMaxAgePrincipal = setPasswordMaxAge(MAX_AGE_PRINCIPAL, maxAge);
				break;
			case administrator:
				passwordMaxAgeAdministrator = setPasswordMaxAge(MAX_AGE_ADMINISTRATOR, maxAge);
				break;
			case sysadmin:
				passwordMaxAgeSysAdmin = setPasswordMaxAge(MAX_AGE_SYSADMIN, maxAge);
				break;
			default: /* Ignore the other roles */
		}
	}
	
	public int setPasswordMaxAge(String propertyName, int maxAge) {
		setStringProperty(propertyName, Integer.toString(maxAge), true);
		return maxAge;
	}

	public int getPasswordHistory() {
		return passwordHistory;
	}

	public void setPasswordHistory(int history) {
		passwordHistory = history;
		setStringProperty(HISTORY, Integer.toString(history), true);
	}

	public boolean isOlatProviderWithPasskey() {
		return olatProviderWithPasskey;
	}

	public void setOlatProviderWithPasskey(boolean enable) {
		this.olatProviderWithPasskey = enable;
		setStringProperty(OLAT_PROVIDER_PASSKEY, enable ? "true" : "false", true);
	}
	
	/**
	 * 
	 * @return The passkey time in seconds
	 */
	public int getPasskeyTimeout() {
		return passkeyTimeout <= 0 ? 120 : passkeyTimeout;
	}

	public UserVerificationRequirement getPasskeyUserVerification() {
		switch(passkeyUserVerification) {
			case "preferred": return UserVerificationRequirement.PREFERRED;
			case "discouraged": return UserVerificationRequirement.DISCOURAGED;
			case "required": return UserVerificationRequirement.REQUIRED;
			default: return UserVerificationRequirement.PREFERRED;
		}
	}

	public void setPasskeyUserVerification(String userVerification) {
		passkeyUserVerification = userVerification;
		setStringProperty(PASSKEY_USER_VERIFICATION, passkeyUserVerification, true);
	}
	
	
	
	public ResidentKeyRequirement getPasskeyResidentKey() {
		switch(passkeyResidentKey) {
			case "preferred": return ResidentKeyRequirement.PREFERRED;
			case "discouraged": return ResidentKeyRequirement.DISCOURAGED;
			case "required": return ResidentKeyRequirement.REQUIRED;
			default: return ResidentKeyRequirement.PREFERRED;
		}
	}

	public void setPasskeyResidentKey(String residentKey) {
		this.passkeyResidentKey = residentKey;
		setStringProperty(PASSKEY_RESIDENT_KEY, passkeyResidentKey, true);
	}

	public AttestationConveyancePreference getPasskeyAttestationConveyancePreference() {
		switch(passkeyAttestationConveyancePreference) {
			case "none": return AttestationConveyancePreference.NONE;
			case "direct": return AttestationConveyancePreference.DIRECT;
			case "indirect": return AttestationConveyancePreference.INDIRECT;
			case "enterprise": return AttestationConveyancePreference.ENTERPRISE;
			default: return AttestationConveyancePreference.NONE;
		}
	}
	
	public void setPasskeyAttestationConveyancePreference(String setting) {
		passkeyAttestationConveyancePreference = setting;
		setStringProperty(PASSKEY_ATTESTATION_CONVEYANCE, passkeyAttestationConveyancePreference, true);
	}
	
	public PasskeyLevels getPasskeyLevel(Roles roles) {
		OrganisationRoles[] orgRoles = OrganisationRoles.values();
		for(OrganisationRoles orgRole:orgRoles) {
			if(roles.hasSomeRoles(orgRole)) {
				return getPasskeyLevel(orgRole);
			}
		}
		return PasskeyLevels.level1;
	}

	public PasskeyLevels getPasskeyLevel(OrganisationRoles role) {
		String level;
		switch(role) {
			case sysadmin: level = passkeyLevelSystemAdministrator; break;
			case administrator: level = passkeyLevelAdministrator; break;
			case usermanager: level = passkeyLevelUserManager; break;
			case rolesmanager: level = passkeyLevelRolesManager; break;
			case learnresourcemanager: level = passkeyLevelLearnResourceManager; break;
			case lecturemanager: level = passkeyLevelLectureManager; break;
			case groupmanager: level = passkeyLevelGroupManager; break;
			case poolmanager: level = passkeyLevelPoolManager; break;
			case curriculummanager: level = passkeyLevelCurriculumManager; break;
			case qualitymanager: level = passkeyLevelQualityManager; break;
			case projectmanager: level = passkeyLevelProjectManager; break;
			case linemanager: level = passkeyLevelLineManager; break;
			case educationmanager: level = passkeyLevelEducationManager; break;
			case principal: level = passkeyLevelPrincipal; break;
			case author: level = passkeyLevelAuthor; break;
			case user, invitee: level = passkeyLevelUser; break;
			default: return PasskeyLevels.level1;
		}
		return PasskeyLevels.valueOf(level);
	}
	
	public void setPasskeyLevel(OrganisationRoles role, PasskeyLevels level) {
		switch(role) {
			case sysadmin: passkeyLevelSystemAdministrator = setPasskeyLevelByRole(role, level); break;
			case administrator: passkeyLevelAdministrator = setPasskeyLevelByRole(role, level); break;
			case usermanager: passkeyLevelUserManager = setPasskeyLevelByRole(role, level); break;
			case rolesmanager: passkeyLevelRolesManager = setPasskeyLevelByRole(role, level); break;
			case learnresourcemanager: passkeyLevelLearnResourceManager = setPasskeyLevelByRole(role, level); break;
			case lecturemanager: passkeyLevelLectureManager = setPasskeyLevelByRole(role, level); break;
			case groupmanager: passkeyLevelGroupManager = setPasskeyLevelByRole(role, level); break;
			case poolmanager: passkeyLevelPoolManager = setPasskeyLevelByRole(role, level); break;
			case curriculummanager: passkeyLevelCurriculumManager = setPasskeyLevelByRole(role, level); break;
			case qualitymanager: passkeyLevelQualityManager = setPasskeyLevelByRole(role, level); break;
			case projectmanager: passkeyLevelProjectManager = setPasskeyLevelByRole(role, level); break;
			case linemanager: passkeyLevelLineManager = setPasskeyLevelByRole(role, level); break;
			case educationmanager: passkeyLevelEducationManager = setPasskeyLevelByRole(role, level); break;
			case principal: passkeyLevelPrincipal = setPasskeyLevelByRole(role, level); break;
			case author: passkeyLevelAuthor = setPasskeyLevelByRole(role, level); break;
			case user: passkeyLevelUser = setPasskeyLevelByRole(role, level); break;
			default: break;
		}
	}
	
	private String setPasskeyLevelByRole(OrganisationRoles role, PasskeyLevels level) {
		setStringProperty(PASSKEY_LEVEL_PREFIX + role, level.name(), true);
		return level.name();
	}
	
	public boolean isPasskeyUpgradeAllowed() {
		return "true".equals(passkeyUpgrade);
	}
	
	public void setPasskeyUpgradeAllowed(boolean upgrade) {
		passkeyUpgrade = upgrade ? "true" : "false";
		setStringProperty(PASSKEY_UPGRADE , passkeyUpgrade, true);
	}
	
	public String getPasskeyUpgrade() {
		return passkeyUpgrade;
	}
	
	public void setPasskeyUpgrade(String upgrade) {
		passkeyUpgrade = upgrade;
		setStringProperty(PASSKEY_UPGRADE , upgrade, true);
	}
	
	public long getPasskeyMaxSkip() {
		if(StringHelper.isLong(passkeyMaxSkip)) {
			return Long.parseLong(passkeyMaxSkip);
		}
		return 0l;
	}

	public void setPasskeyMaxSkip(long count) {
		passkeyMaxSkip = Long.toString(count);
		setStringProperty(PASSKEY_MAX_SKIP, passkeyMaxSkip, true);
	}

	public String getLoginFaqUrl() {
		return loginFaqUrl;
	}

	public void setLoginFaqUrl(String loginFaqUrl) {
		this.loginFaqUrl = loginFaqUrl;
		setStringProperty(LOGIN_FAQ_URL, loginFaqUrl, true);
	}
}
