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
package org.olat.login.oauth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.fullWebApp.MessageWindowController;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.login.LoginModule;
import org.olat.login.oauth.model.OAuthRegistration;
import org.olat.login.oauth.model.OAuthSession;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.oauth.spi.OpenIDVerifier;
import org.olat.login.oauth.spi.OpenIdConnectApi.OpenIdConnectService;
import org.olat.login.oauth.spi.OpenIdConnectFullConfigurableApi.OpenIdConnectFullConfigurableService;
import org.olat.login.oauth.ui.JSRedirectWindowController;
import org.olat.login.oauth.ui.OAuthAuthenticationController;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;

/**
 * Callback for OAuth 2
 * 
 * 
 * Initial date: 03.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(OAuthDispatcher.class);

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	@Autowired
	private OAuthLoginManager oauthLoginManager;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			if(log.isDebugEnabled()){
				log.debug("Bad Request {}", request.getPathInfo());
			}
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		String error = request.getParameter("error"); 
		if(null != error) { 
			error(ureq, translateOauthError(ureq, error));
			return; 
		}
		String problem = request.getParameter("oauth_problem");
		if(problem != null && "token_rejected".equals(problem.trim())) {
			error(ureq, translateOauthError(ureq, error));
			return; 
		}
		
		OAuthSession oauthSession = getOAuthSession(ureq, request);
		if(oauthSession == null) {
			return; 
		}
		
		try {
			OAuthSPI provider = oauthSession.oauthProvider();
			OAuthService service = oauthSession.service();

			Token accessToken;
			if(provider == null) {
				log.info(Tracing.M_AUDIT, "OAuth Login failed, no provider in request");
				DispatcherModule.redirectToDefaultDispatcher(response);
				return;
			} else if(provider.isImplicitWorkflow()) {
				String idToken = ureq.getParameter("id_token");
				if(idToken == null) {
					redirectImplicitWorkflow(ureq);
					return;
				} else if(service instanceof OpenIdConnectFullConfigurableService configurableService) {
					OpenIDVerifier verifier = OpenIDVerifier.create(ureq, oauthSession);
					accessToken = configurableService.getAccessToken(verifier);
				} else if(service instanceof OpenIdConnectService connectService) {
					OpenIDVerifier verifier = OpenIDVerifier.create(ureq, oauthSession);
					accessToken = connectService.getAccessToken(verifier);
				} else {
					return;
				}
			} else if(service instanceof OAuth10aService oauth10aService
					&& oauthSession.requestToken() instanceof OAuth1RequestToken oauth1RequestToken) {
				String requestVerifier = request.getParameter("oauth_verifier"); 
				if(requestVerifier == null) {//OAuth 2.0 as a code
					requestVerifier = request.getParameter("code");
				}
				accessToken = oauth10aService.getAccessToken(oauth1RequestToken, requestVerifier);
			} else if(service instanceof OAuth20Service oauth20Service) {
				String requestVerifier = request.getParameter("oauth_verifier"); 
				if(requestVerifier == null) {//OAuth 2.0 as a code
					requestVerifier = request.getParameter("code");
				}
				accessToken = oauth20Service.getAccessToken(requestVerifier);
			} else {
				return;
			}

			OAuthUser infos = provider.getUser(service, accessToken);
			if(infos == null || !StringHelper.containsNonWhitespace(infos.getId())) {
				error(ureq, translate(ureq, "error.no.id"));
				log.error("OAuth Login failed, no infos extracted from access token: {}", accessToken);
				return;
			}

			OAuthRegistration registration = new OAuthRegistration(provider.getProviderName(), infos);
			login(infos, registration);

			if(provider instanceof OAuthUserCreator userCreator) {
				if(registration.getIdentity() != null) {
					Identity newIdentity = userCreator.updateUser(infos, registration.getIdentity());
					registration.setIdentity(newIdentity);		
				}
			}
			
			if(provider instanceof OAuthUserCreator && registration.getIdentity() == null) {
				disclaimer(request, response, infos, registration, provider);
			} else if(registration.getIdentity() == null) {
				if(oauthLoginModule.isAllowUserCreation()) {
					createUser(infos, registration, provider, ureq, request, response);
				} else {
					error(ureq, translate(ureq, "error.account.creation"));
					log.error("OAuth Login ok but the user has not an account on OpenOLAT: {}", infos);
				}
			} else {
				if(ureq.getUserSession() != null) {
					//re-init the activity logger
					ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
				}
			
				Identity identity = registration.getIdentity();
				if(!oauthLoginModule.isSkipDisclaimerDialog() && registrationManager.needsToConfirmDisclaimer(identity)) {
					disclaimer(request, response, infos, registration, provider);
				} else {
					login(identity, infos, provider, ureq, response);
				}
			}
		} catch (Exception e) {
			log.error("Unexpected error", e);
			error(ureq, translate(ureq, "error.generic"));
		}
	}
	
	private OAuthSession getOAuthSession(UserRequest ureq, HttpServletRequest request) {
		OAuthSession oauthSession = null;
		HttpSession sess = request.getSession();
		String state = ureq.getParameter("state");
		if(StringHelper.containsNonWhitespace(state)) {
			oauthSession = oauthLoginManager.retrieveAuthorizationRequest(state);
		}
		
		if(oauthSession == null) {
			// OAuth 1.0
			try {
				oauthSession = (OAuthSession)sess.getAttribute(OAuthConstants.OAUTH_SESSION);
			} catch (Exception e) {
				log.error("Unexpected error", e);
				error(ureq, translate(ureq, "error.generic"));
			}
		}
		
		return oauthSession;
	}
	
	private void createUser(OAuthUser infos, OAuthRegistration registration, OAuthSPI provider,
			UserRequest ureq, HttpServletRequest request, HttpServletResponse response) {
		if(oauthLoginModule.isSkipRegistrationDialog()
				&& (oauthLoginModule.isSkipDisclaimerDialog() || !registrationModule.isDisclaimerEnabled())
				&& oauthLoginManager.isValid(infos)) {
			Identity authenticatedIdentity = oauthLoginManager.createIdentity(infos, registration.getAuthProvider());
			if(authenticatedIdentity != null) {
				login(authenticatedIdentity, infos, provider, ureq, response);	
			} else {
				error(ureq, "Unexpected error");
			}
		} else if(oauthLoginModule.isSkipRegistrationDialog() && oauthLoginManager.isValid(infos)) {
			disclaimer(request, response, infos, registration, provider);
		} else {
			register(request, response, registration);
		}
	}
	
	private void login(Identity identity, OAuthUser infos, OAuthSPI provider, UserRequest ureq, HttpServletResponse response) {
		int loginStatus = AuthHelper.doLogin(identity, provider.getProviderName(), ureq);
		if (loginStatus != AuthHelper.LOGIN_OK) {
			if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToServiceNotAvailable(response);
			} else if (loginStatus == AuthHelper.LOGIN_INACTIVE
					|| loginStatus == AuthHelper.LOGIN_DENIED) {
				error(ureq, translate(ureq, "login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
				log.error("OAuth Login ok but the user is inactive or denied: {}", identity);
			} else if (loginStatus == AuthHelper.LOGIN_PENDING) {
				error(ureq, translate(ureq, "login.error.pending", WebappHelper.getMailConfig("mailSupport")));
				log.error("OAuth Login ok but the user is pending: {}", identity);
			} else {
				// error, redirect to login screen
				DispatcherModule.redirectToDefaultDispatcher(response);
			}
		} else {
			ureq.getUserSession().setOAuth2Tokens(infos.getOAuth2Tokens());
			//update last login date and register active user
			securityManager.setIdentityLastLogin(identity);
			MediaResource mr = ureq.getDispatchResult().getResultingMediaResource();
			if (mr instanceof RedirectMediaResource rmr) {
				redirectAfterLogin(ureq, rmr.getRedirectURL());
			} else {
				DispatcherModule.redirectToDefaultDispatcher(response); // error, redirect to login screen
			}
		}
	}
	
	
	private void redirectAfterLogin(UserRequest ureq, String redirectUrl) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>\n<html><head><title>Reload</title>");
		sb.append("<script>window.location.replace(\"").append(redirectUrl).append("\");</script></head><body></body></html>");
		ServletUtil.serveStringResource(ureq.getHttpResp(), sb.toString());
	}
	
	private void redirectImplicitWorkflow(UserRequest ureq) {
		ChiefController msgcc = new JSRedirectWindowController(ureq);
		msgcc.getWindow().dispatchRequest(ureq, true);
	}
	
	private void login(OAuthUser infos, OAuthRegistration registration) {
		final String id = infos.getId();
		// Need an identifier at least
		if(!StringHelper.containsNonWhitespace(id)) {
			return;
		}
		
		final List<String> authenticationExternalIds = infos.getAuthenticationExternalIds();
		final List<Authentication> auths = securityManager.findAuthentications(id, authenticationExternalIds, registration.getAuthProvider(), BaseSecurity.DEFAULT_ISSUER);
		if(auths.isEmpty()) {
			final String email = infos.getEmail();
			final String institutionalEmail = infos.getInstitutionalEmail();
			if(StringHelper.containsNonWhitespace(email) || StringHelper.containsNonWhitespace(institutionalEmail)) {
				Identity identity = null;
				if(StringHelper.containsNonWhitespace(institutionalEmail)) {
					identity = userManager.findUniqueIdentityByEmail(institutionalEmail);
				}
				if(identity == null && StringHelper.containsNonWhitespace(email)) {
					identity = userManager.findUniqueIdentityByEmail(email);
				}
				if(identity == null) {
					identity = securityManager.findIdentityByLogin(id);
				}
				if(identity == null) {
					identity = securityManager.findIdentityByNameCaseInsensitive(id);
				}
				if(identity == null) {
					identity = securityManager.findIdentityByNickName(id);
				}
				if(identity != null) {
					securityManager.createAndPersistAuthentication(identity, registration.getAuthProvider(), BaseSecurity.DEFAULT_ISSUER, null, id, null, null);
					registration.setIdentity(identity);
				} else {
					log.error("OAuth Login failed, user with user name {} not found. OAuth user: {}", email, infos);
				}
			}
		} else if(auths.size() == 1) {
			registration.setIdentity(auths.get(0).getIdentity());
		} else {
			log.warn("Several authentications found for id: {} and external ids: {}", id, authenticationExternalIds);
		}
	}
	
	private String translate(UserRequest ureq, String i18nKey) {
		Translator trans = Util.createPackageTranslator(OAuthAuthenticationController.class, ureq.getLocale(),
				Util.createPackageTranslator(LoginModule.class, ureq.getLocale()));
		return trans.translate(i18nKey);
	}
	
	private String translate(UserRequest ureq, String i18nKey, String arg) {
		Translator trans = Util.createPackageTranslator(OAuthAuthenticationController.class, ureq.getLocale(),
				Util.createPackageTranslator(LoginModule.class, ureq.getLocale()));
		return trans.translate(i18nKey, arg);
	}
	
	private String translateOauthError(UserRequest ureq, String error) {
		error = error == null ? null : error.trim();
		String message;
		if("access_denied".equals(error)) {
			message = translate(ureq, "error.access.denied");
		} else if("token_rejected".equals(error)) {
			message = translate(ureq, "error.token.rejected");
		} else if("invalid_grant".equals(error)) {
			message = translate(ureq, "error.invalid.grant");
			
		} else {
			message = translate(ureq, "error.generic");
		}
		return message;
	}
	
	private void error(UserRequest ureq, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("<h4><i class='o_icon o_icon-fw o_icon_error'> </i>");
		sb.append(translate(ureq, "error.title"));
		sb.append("</h4><p>");
		sb.append(message);
		sb.append("</p>");
		ChiefController msgcc = new MessageWindowController(ureq, sb.toString());
		msgcc.getWindow().dispatchRequest(ureq, true);
	}
	
	private void disclaimer(HttpServletRequest request, HttpServletResponse response, OAuthUser user,
			OAuthRegistration registration, OAuthSPI provider) {
		try {
			request.getSession().setAttribute(OAuthConstants.OAUTH_SPI, provider);
			request.getSession().setAttribute(OAuthConstants.OAUTH_USER_ATTR, user);
			request.getSession().setAttribute(OAuthConstants.OAUTH_REGISTRATION_ATTR, registration);
			DispatcherModule.forwardTo(request, response, DispatcherModule.getPathDefault() + OAuthConstants.OAUTH_DISCLAIMER_PATH + "/");
		} catch (Exception e) {
			log.error("Redirect failed: url={}{}", WebappHelper.getServletContextPath(), DispatcherModule.getPathDefault(),e);
		}
	}
	
	private void register(HttpServletRequest request, HttpServletResponse response, OAuthRegistration registration) {
		try {
			request.getSession().setAttribute(OAuthConstants.OAUTH_REGISTRATION_ATTR, registration);
			DispatcherModule.forwardTo(request, response, DispatcherModule.getPathDefault() + OAuthConstants.OAUTH_REGISTER_PATH + "/");
		} catch (Exception e) {
			log.error("Redirect failed: url={}{}", WebappHelper.getServletContextPath(), DispatcherModule.getPathDefault(),e);
		}
	}
}