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
package org.olat.core.commons.services.csp;

import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The default policy allows the classic video services: YouTube, Vimeo, Nanoo.tv.
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CSPModule extends AbstractSpringModule {
	
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_DEFAULT_SRC = "'self'";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_SCRIPT_SRC = "'unsafe-inline' 'self' https://player.vimeo.com https://www.youtube.com https://s.ytimg.com";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_STYLE_SRC = "'unsafe-inline' 'self'";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_IMG_SRC = "'self' data:";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_FONT_SRC = "'self' data:";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_CONNECT_SRC = "'self' https://youtu.be https://www.youtube.com";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_FRAME_SRC = "'self' https://player.vimeo.com https://youtu.be https://www.youtube.com https://s.ytimg.com https://applications.zoom.us https://www.nanoo.tv";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_FRAME_ANCESTORS_SRC = "'self'";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_WORKER_SRC = "'self' blob:";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_MEDIA_SRC = "'self' blob:";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_OBJECT_SRC = "'self'";
	public static final String DEFAULT_CONTENT_SECURITY_POLICY_FORM_ACTION = "'self'";

	private static final String CSRF = "csrf";
	private static final String COOKIE_SAME_SITE = "cookieSameSite";
	private static final String FORCE_TOP_FRAME = "forceTopFrame";
	private static final String X_FRAME_OPTIONS_SAMEORIGIN = "xFrameOptionsSameOrigin";
	private static final String STRICT_TRANSPORT_SECURITY = "strictTransportSecurity";
	private static final String X_CONTENT_TYPES_OPTIONS = "xContentTypeOptions";
	private static final String CONTENT_SECURITY_POLICY = "contentSecurityPolicy";
	private static final String CONTENT_SECURITY_POLICY_REPORT_ONLY = "contentSecurityPolicyReportOnly";
	private static final String CONTENT_SECURITY_POLICY_DEFAULT_SRC = "base.security.contentSecurityPolicy.defaultSrc";
	private static final String CONTENT_SECURITY_POLICY_SCRIPT_SRC = "base.security.contentSecurityPolicy.scriptSrc";
	private static final String CONTENT_SECURITY_POLICY_STYLE_SRC = "base.security.contentSecurityPolicy.styleSrc";
	private static final String CONTENT_SECURITY_POLICY_IMG_SRC = "base.security.contentSecurityPolicy.imgSrc";
	private static final String CONTENT_SECURITY_POLICY_FONT_SRC = "base.security.contentSecurityPolicy.fontSrc";
	private static final String CONTENT_SECURITY_POLICY_CONNECT_SRC = "base.security.contentSecurityPolicy.connectSrc";
	private static final String CONTENT_SECURITY_POLICY_FRAME_SRC = "base.security.contentSecurityPolicy.frameSrc";
	private static final String CONTENT_SECURITY_POLICY_FRAME_ANCESTORS = "base.security.contentSecurityPolicy.frameAncestors";
	private static final String CONTENT_SECURITY_POLICY_WORKER_SRC = "base.security.contentSecurityPolicy.workerSrc";
	private static final String CONTENT_SECURITY_POLICY_MEDIA_SRC = "base.security.contentSecurityPolicy.mediaSrc";
	private static final String CONTENT_SECURITY_POLICY_OBJECT_SRC = "base.security.contentSecurityPolicy.objectSrc";
	private static final String CONTENT_SECURITY_POLICY_FORM_ACTION = "base.security.contentSecurityPolicy.formAction";

	@Value("${base.security.csrf:enabled}")
	private String csrf;
	@Value("${base.security.cookie.samesite:Lax}")
	private String cookieSameSite;
	
	@Value("${base.security.frameOptionsSameOrigine:enabled}")
	private String xFrameOptionsSameorigin;
	@Value("${base.security.strictTransportSecurity:enabled}")
	private String strictTransportSecurity;
	@Value("${base.security.xContentTypeOptions:enabled}")
	private String xContentTypeOptions;
	
	@Value("${base.security.contentSecurityPolicy:enabled}")
	private String contentSecurityPolicy;
	@Value("${base.security.contentSecurityPolicy.reportOnly:enabled}")
	private String contentSecurityPolicyReportOnly;
	
	@Value("${base.security.unsafe.eval:true}")
	private String allowUnsafeEval;
	
	@Value("${base.security.contentSecurityPolicy.defaultSrc:}")
	private String contentSecurityPolicyDefaultSrc;
	@Value("${base.security.contentSecurityPolicy.scriptSrc:}")
	private String contentSecurityPolicyScriptSrc;
	@Value("${base.security.contentSecurityPolicy.styleSrc:}")
	private String contentSecurityPolicyStyleSrc;
	@Value("${base.security.contentSecurityPolicy.imgSrc:}")
	private String contentSecurityPolicyImgSrc;
	@Value("${base.security.contentSecurityPolicy.fontSrc:}")
	private String contentSecurityPolicyFontSrc;
	@Value("${base.security.contentSecurityPolicy.connectSrc:}")
	private String contentSecurityPolicyConnectSrc;
	@Value("${base.security.contentSecurityPolicy.frameSrc:}")
	private String contentSecurityPolicyFrameSrc;
	@Value("${base.security.contentSecurityPolicy.frameAncestors:}")
	private String contentSecurityPolicyFrameAncestors;
	@Value("${base.security.contentSecurityPolicy.workerSrc:}")
	private String contentSecurityPolicyWorkerSrc;
	@Value("${base.security.contentSecurityPolicy.mediaSrc:}")
	private String contentSecurityPolicyMediaSrc;
	@Value("${base.security.contentSecurityPolicy.objectSrc:}")
	private String contentSecurityPolicyObjectSrc;
	@Value("${base.security.contentSecurityPolicy.formAction:}")
	private String contentSecurityPolicyFormAction;
	@Autowired
	private List<CSPDirectiveProvider> directiveProviders;
	
	@Autowired
	public CSPModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}


	@Override
	public void init() {
		updateProperties();
	}
	
	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabled = getStringPropertyValue(X_FRAME_OPTIONS_SAMEORIGIN, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			xFrameOptionsSameorigin = enabled;
		}
		enabled = getStringPropertyValue(CSRF, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			csrf = enabled;
		}
		enabled = getStringPropertyValue(STRICT_TRANSPORT_SECURITY, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			strictTransportSecurity = enabled;
		}
		enabled = getStringPropertyValue(X_CONTENT_TYPES_OPTIONS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			xContentTypeOptions = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicy = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_REPORT_ONLY, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyReportOnly = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_DEFAULT_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyDefaultSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_SCRIPT_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyScriptSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_STYLE_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyStyleSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_IMG_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyImgSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_FONT_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyFontSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_CONNECT_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyConnectSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_FRAME_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyFrameSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_FRAME_ANCESTORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyFrameAncestors = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_WORKER_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyWorkerSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_MEDIA_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyMediaSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_OBJECT_SRC, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyObjectSrc = enabled;
		}
		enabled = getStringPropertyValue(CONTENT_SECURITY_POLICY_FORM_ACTION, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			contentSecurityPolicyFormAction = enabled;
		}
		
		cookieSameSite = getStringPropertyValue(COOKIE_SAME_SITE, cookieSameSite);
	}

	public boolean isForceTopFrame() {
		return true;
	}

	public void setForceTopFrame(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		setStringProperty(FORCE_TOP_FRAME, enabled, true);
	}
	
	public boolean isCsrfEnabled() {
		return "enabled".equals(csrf);
	}
	
	public void setCsrfEnabled(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		csrf = enabled;
		setStringProperty(CSRF, enabled, true);
	}
	
	public SameSiteEnum getCookieSameSite() {
		return SameSiteEnum.secureValueOf(cookieSameSite, SameSiteEnum.STRICT);
	}
	
	public void setCookieSameSite(SameSiteEnum sameSiteValue) {
		this.cookieSameSite = sameSiteValue != null ? sameSiteValue.name() : SameSiteEnum.STRICT.name();
		setStringProperty(COOKIE_SAME_SITE, this.cookieSameSite, true);
		
	}

	public boolean isXFrameOptionsSameoriginEnabled() {
		return "enabled".equals(xFrameOptionsSameorigin);
	}

	public void setXFrameOptionsSameoriginEnabled(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		xFrameOptionsSameorigin = enabled;
		setStringProperty(X_FRAME_OPTIONS_SAMEORIGIN, enabled, true);
	}

	public boolean isStrictTransportSecurityEnabled() {
		return "enabled".equals(strictTransportSecurity);
	}

	public void setStrictTransportSecurity(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		strictTransportSecurity = enabled;
		setStringProperty(STRICT_TRANSPORT_SECURITY, enabled, true);
	}

	public boolean isXContentTypeOptionsEnabled() {
		return "enabled".equals(xContentTypeOptions);
	}

	public void setxContentTypeOptions(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		xContentTypeOptions = enabled;
		setStringProperty(X_CONTENT_TYPES_OPTIONS, enabled, true);
	}

	public boolean isContentSecurityPolicyEnabled() {
		return "enabled".equals(contentSecurityPolicy);
	}

	public void setContentSecurityPolicy(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		contentSecurityPolicy = enabled;
		setStringProperty(CONTENT_SECURITY_POLICY, enabled, true);
	}
	
	public boolean isContentSecurityPolicyReportOnlyEnabled() {
		return "enabled".equals(contentSecurityPolicyReportOnly);
	}

	public void setContentSecurityPolicyReportOnly(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		contentSecurityPolicyReportOnly = enabled;
		setStringProperty(CONTENT_SECURITY_POLICY_REPORT_ONLY, enabled, true);
	}
	
	public boolean isAllowUnsafeEval() {
		return "true".equals(allowUnsafeEval);
	}

	public String getContentSecurityPolicyDefaultSrc() {
		return contentSecurityPolicyDefaultSrc;
	}

	public void setContentSecurityPolicyDefaultSrc(String policy) {
		contentSecurityPolicyDefaultSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_DEFAULT_SRC, policy, true);
	}

	public String getContentSecurityPolicyScriptSrc() {
		return contentSecurityPolicyScriptSrc;
	}

	public void setContentSecurityPolicyScriptSrc(String policy) {
		contentSecurityPolicyScriptSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_SCRIPT_SRC, policy, true);
	}

	public String getContentSecurityPolicyStyleSrc() {
		return contentSecurityPolicyStyleSrc;
	}

	public void setContentSecurityPolicyStyleSrc(String policy) {
		contentSecurityPolicyStyleSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_STYLE_SRC, policy, true);
	}

	public String getContentSecurityPolicyImgSrc() {
		return contentSecurityPolicyImgSrc;
	}

	public void setContentSecurityPolicyImgSrc(String policy) {
		contentSecurityPolicyImgSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_IMG_SRC, policy, true);
	}

	public String getContentSecurityPolicyFontSrc() {
		return contentSecurityPolicyFontSrc;
	}

	public void setContentSecurityPolicyFontSrc(String policy) {
		contentSecurityPolicyFontSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_FONT_SRC, policy, true);
	}

	public String getContentSecurityPolicyConnectSrc() {
		return contentSecurityPolicyConnectSrc;
	}

	public void setContentSecurityPolicyConnectSrc(String policy) {
		this.contentSecurityPolicyConnectSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_CONNECT_SRC, policy, true);
	}

	public String getContentSecurityPolicyFrameSrc() {
		return contentSecurityPolicyFrameSrc;
	}

	public void setContentSecurityPolicyFrameSrc(String policy) {
		this.contentSecurityPolicyFrameSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_FRAME_SRC, policy, true);
	}
	
	public String getContentSecurityPolicyFrameAncestors() {
		return contentSecurityPolicyFrameAncestors;
	}

	public void setContentSecurityPolicyFrameAncestors(String policy) {
		this.contentSecurityPolicyFrameAncestors = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_FRAME_ANCESTORS, policy, true);
	}
	
	public String getContentSecurityPolicyWorkerSrc() {
		return contentSecurityPolicyWorkerSrc;
	}

	public void setContentSecurityPolicyWorkerSrc(String policy) {
		this.contentSecurityPolicyWorkerSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_WORKER_SRC, policy, true);
	}

	public String getContentSecurityPolicyMediaSrc() {
		return contentSecurityPolicyMediaSrc;
	}

	public void setContentSecurityPolicyMediaSrc(String policy) {
		this.contentSecurityPolicyMediaSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_MEDIA_SRC, policy, true);
	}

	public String getContentSecurityPolicyObjectSrc() {
		return contentSecurityPolicyObjectSrc;
	}

	public void setContentSecurityPolicyObjectSrc(String policy) {
		this.contentSecurityPolicyObjectSrc = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_OBJECT_SRC, policy, true);
	}
	
	public String getContentSecurityPolicyFormAction() {
		return contentSecurityPolicyFormAction;
	}

	public void setContentSecurityPolicyFormAction(String policy) {
		this.contentSecurityPolicyFormAction = policy;
		setStringProperty(CONTENT_SECURITY_POLICY_FORM_ACTION, policy, true);
	}

	public List<CSPDirectiveProvider> getDirectiveProviders() {
		return directiveProviders == null ? List.of() : List.copyOf(directiveProviders);
	}
}
