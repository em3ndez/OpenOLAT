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
package org.olat.ldap.ui;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationController;
import org.olat.login.auth.AuthenticationForm;
import org.olat.login.auth.AuthenticationStatus;
import org.olat.login.auth.OLATAuthManager;
import org.olat.registration.DisclaimerFormController;
import org.olat.registration.RegistrationManager;
import org.springframework.beans.factory.annotation.Autowired;

public class LDAPAuthenticationController extends AuthenticationController implements Activateable2 {
	
	private static final Logger log = Tracing.createLoggerFor(LDAPAuthenticationController.class);
	
	public static final String PROVIDER_LDAP = "LDAP";
	
	private VelocityContainer loginComp;
	private Controller subController;
	private AuthenticationForm loginForm; 
	private DisclaimerFormController disclaimerCtr;
	private Identity authenticatedIdentity;
	private String provider = null;

	private CloseableModalController cmc;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPLoginManager ldapLoginManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	@Autowired
	private RegistrationManager registrationManager;

	public LDAPAuthenticationController(UserRequest ureq, WindowControl control) {
		// use fallback translator to login and registration package
		super(ureq, control, Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), Util.createPackageTranslator(RegistrationManager.class, ureq.getLocale())));

		loginComp = createVelocityContainer("ldaplogin");

		// Use the standard OLAT login form but with our LDAP translator
		loginForm = new AuthenticationForm(ureq, control, "ldap_login", getTranslator());
		listenTo(loginForm);
		
		loginComp.put("ldapForm", loginForm.getInitialComponent());
		
		putInitialPanel(loginComp);
	}

	@Override
	public void changeLocale(Locale newLocale) {
		setLocale(newLocale, true);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// 
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		LDAPError ldapError = new LDAPError();
		if (source == loginForm && event == Event.DONE_EVENT) {

			String login = loginForm.getLogin();
			String pass = loginForm.getPass();

			if (loginModule.isLoginBlocked(login)) {
				// do not proceed when already blocked
				showError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
				getLogger().info(Tracing.M_AUDIT, "Login attempt on already blocked login for {}. IP::{}", login, ureq.getHttpReq().getRemoteAddr());
				return;
			}
			authenticatedIdentity = ldapLoginManager.authenticate(login, pass, ldapError);

			if(!ldapError.isEmpty()) {
				final String errStr = ldapError.get();
				if ("login.notauthenticated".equals(errStr)) {
					// user exists in LDAP, authentication was ok, but user
					// has not got the OLAT service or has not been created by now
					getWindowControl().setError(translate("login.notauthenticated"));
					return;                                
				} else {
					// tell about the error again
					ldapError.insert(errStr);
				}
			}

			if (authenticatedIdentity != null) {
				provider = LDAPAuthenticationController.PROVIDER_LDAP;
				
				try {
					//prevents database timeout
					dbInstance.commitAndCloseSession();
				} catch (Exception e) {
					log.error("", e);
				}
			} else {
				// try fallback to OLAT provider if configured
				if (ldapLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin() || ldapLoginModule.isTryFallbackToOLATPwdOnLogin()) {
					AuthenticationStatus status = new AuthenticationStatus();
					authenticatedIdentity = olatAuthenticationSpi.authenticate(null, login, pass, status);
					if(status.getStatus() == AuthHelper.LOGIN_INACTIVE
							|| status.getStatus() == AuthHelper.LOGIN_DENIED) {
						showError("login.error.inactive", WebappHelper.getMailConfig("mailSupport"));
						log.error("LDAP Login ok but the user is inactive or denied: {}", authenticatedIdentity);
						return;
					} else if(status.getStatus() == AuthHelper.LOGIN_PENDING) {
						showError("login.error.pending", WebappHelper.getMailConfig("mailSupport"));
						log.error("LDAP Login ok but the user is pending: {}", authenticatedIdentity);
						return;
					}
				}
				if (authenticatedIdentity != null) {
					provider = BaseSecurityModule.getDefaultAuthProviderIdentifier();
				}
			}
			// Still not found? register for hacking attempts
			if (authenticatedIdentity == null) {
				if (loginModule.registerFailedLoginAttempt(login)) {
					logAudit("Too many failed login attempts for " + login + ". Login blocked. IP::" + ureq.getHttpReq().getRemoteAddr());
					showError("login.blocked", loginModule.getAttackPreventionTimeoutMin().toString());
				} else {
					showError("login.error", ldapError.get());
				}
				return;
			} else if(Identity.STATUS_INACTIVE.equals(authenticatedIdentity.getStatus())) {
				showError("login.error.inactive", WebappHelper.getMailConfig("mailSupport"));
				return;
			} else {
				try {
					String language = authenticatedIdentity.getUser().getPreferences().getLanguage();
					UserSession usess = ureq.getUserSession();
					if(StringHelper.containsNonWhitespace(language)) {
						usess.setLocale(I18nManager.getInstance().getLocaleOrDefault(language));
					}
				} catch (Exception e) {
					logError("Cannot set the user language", e);
				}
			}

			loginModule.clearFailedLoginAttempts(login);

			// Check if disclaimer has been accepted
			if (registrationManager.needsToConfirmDisclaimer(authenticatedIdentity)) {
				// accept disclaimer first
				
				removeAsListenerAndDispose(disclaimerCtr);
				disclaimerCtr = new DisclaimerFormController(ureq, getWindowControl(), authenticatedIdentity, false);
				listenTo(disclaimerCtr);
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerCtr.getInitialComponent());
				listenTo(cmc);

				cmc.activate();	

			} else {
				// disclaimer acceptance not required
				doLoginAndRegister(authenticatedIdentity, ureq, provider);
			}
		}
		
		if (source == subController) {
			if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
				cmc.deactivate();
			}
		} else if (source == disclaimerCtr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				// User accepted disclaimer, do login now
				registrationManager.setHasConfirmedDislaimer(authenticatedIdentity);
				doLoginAndRegister(authenticatedIdentity, ureq, provider);
			} else if (event == Event.CANCELLED_EVENT) {
				// User did not accept, workflow ends here
				showWarning("disclaimer.form.cancelled");
			}
		} else if (source == cmc) {
			// User did close disclaimer window, workflow ends here
			showWarning("disclaimer.form.cancelled");			
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	/**
	 * Internal helper to perform the real login code and do all necessary steps to
	 * register the user session
	 * 
	 * @param authIdentity The authenticated identity
	 * @param ureq
	 * @param myProvider The provider that identified the user
	 */
	private void doLoginAndRegister(Identity authIdentity, UserRequest ureq, String myProvider) {
		if (provider.equals(PROVIDER_LDAP)) {
			// prepare redirects to home etc, set status
			int loginStatus = AuthHelper.doLogin(authIdentity, myProvider, ureq);
			if (loginStatus == AuthHelper.LOGIN_OK) {
				//update last login date and register active user
				securityManager.setIdentityLastLogin(authIdentity);
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE){
				DispatcherModule.redirectToServiceNotAvailable( ureq.getHttpResp() );
			} else if (loginStatus == AuthHelper.LOGIN_INACTIVE) {
				getWindowControl().setError(translate("login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
			} else {
				getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
			}
		} else if (provider.equals(BaseSecurityModule.getDefaultAuthProviderIdentifier())) {
			// delegate login process to OLAT authentication controller
			authenticated(ureq, authIdentity, BaseSecurityModule.getDefaultAuthProviderIdentifier());		
		} else {
			throw new OLATRuntimeException("Unknown login provider::" + myProvider, null);
		}
	}
}
