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

package org.olat.resource.accesscontrol.provider.token;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.DefaultACSecurityCallback;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.resource.accesscontrol.model.OrderAdditionalInfos;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.token.ui.TokenAccessConfigurationController;
import org.olat.resource.accesscontrol.provider.token.ui.TokenSubmitController;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TokenAccessHandler implements AccessMethodHandler {
	
	public static final String METHOD_TYPE = "token.method";
	public static final String METHOD_CSS_CLASS = "o_ac_token";
	
	@Override
	public boolean isPaymentMethod() {
		return false;
	}

	@Override
	public boolean isOverlapAllowed(AccessMethodHandler handler) {
		return true;
	}

	@Override
	public String getType() {
		return METHOD_TYPE;
	}

	@Override
	public String getMethodName(Locale locale) {
		Translator translator = Util.createPackageTranslator(TokenSubmitController.class, locale);
		return translator.translate("token.method");
	}

	@Override
	public String getDescription(Locale locale) {
		Translator translator = Util.createPackageTranslator(TokenSubmitController.class, locale);
		return translator.translate("token.method.description");
	}
	
	@Override
	public AccessMethodSecurityCallback getSecurityCallback(OLATResource resource, Identity identity, Roles roles) {
		return new DefaultACSecurityCallback(roles);
	}

	@Override
	public Controller createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link, Identity bookedIdentity) {
		return new TokenSubmitController(ureq, wControl, link, bookedIdentity);
	}

	@Override
	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			CatalogInfo catalogInfo) {
		return new TokenAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogInfo, true);
	}

	@Override
	public TokenAccessConfigurationController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			CatalogInfo catalogInfo) {
		return new TokenAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogInfo, false);
	}
	
	@Override
	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderPart part, AccessMethod method, Form form) {
		return null;
	}

	@Override
	public boolean checkArgument(OfferAccess link, Object argument) {
		if(argument instanceof String token && StringHelper.containsNonWhitespace(token)) {
			Offer offer = link.getOffer();
			if(offer instanceof OfferImpl impl && token.equals(impl.getToken())) {
				return true;
			}
		} else if(argument instanceof OrderAdditionalInfos infos && infos.hasCode()) {
			return true;
		}
		return false;
	}

	@Override
	public List<PSPTransaction> getPSPTransactions(List<Order> orders) {
		return Collections.emptyList();
	}
}