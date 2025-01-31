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
package org.olat.repository.ui.list;

import java.math.BigDecimal;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDetailsHeaderController extends FormBasicController {
	
	public static final Event START_EVENT = new Event("start");
	private final static String CMD_START = "start";
	private final static String CMD_AUTO_BOOKING = "autoBooking";
	
	protected FormLink startLink;
	protected FormLink leaveLink;
	private ExternalLinkItem offersLink;

	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected ACService acService;

	public AbstractDetailsHeaderController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_header.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
	}
	
	protected abstract String getIconCssClass();
	protected abstract String getExternalRef();
	protected abstract String getTitle();
	protected abstract String getAuthors();
	protected abstract String getTeaser();
	protected abstract VFSLeaf getTeaserImage();
	protected abstract VFSLeaf getTeaserMovie();
	protected abstract RepositoryEntryEducationalType getEducationalType();
	
	protected abstract void initAccess(UserRequest ureq, FormLayoutContainer layoutCont);
	protected abstract String getStartLinkText();
	protected abstract String getStartLinkWarning();
	protected abstract String getStartLinkError();
	protected abstract boolean tryAutoBooking(UserRequest ureq);
	protected abstract Long getResourceKey();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			layoutCont.contextPut("iconCssClass", getIconCssClass());
			layoutCont.contextPut("externalRef", getExternalRef());
			layoutCont.contextPut("title", getTitle());
			layoutCont.contextPut("authors", getAuthors());
			layoutCont.contextPut("teaser", getTeaser());
			
			VFSLeaf image = getTeaserImage();
			VFSLeaf movie = getTeaserMovie();
			if (image != null || movie != null) {
				ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
				if (movie != null) {
					ic.setMedia(movie);
					ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
					if (image != null) {
						ic.setPoster(image);
					}
				} else {
					ic.setMedia(image);
					ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
				}
				layoutCont.put("thumbnail", ic);
			}
			
			if (getEducationalType() != null) {
				String educationalType = translate(RepositoyUIFactory.getI18nKey(getEducationalType()));
				layoutCont.contextPut("educationalType", educationalType);
			}
			
			initAccess(ureq, layoutCont);
			
			layoutCont.contextPut("startLinkWarning", getStartLinkWarning());
			layoutCont.contextPut("startLinkError", getStartLinkError());
			if (StringHelper.containsNonWhitespace(getStartLinkError())) {
				if (startLink != null) {
					startLink.setEnabled(false);
				}
				if (offersLink != null) {
					offersLink.setUrl("#");
					offersLink.setCssClass("btn btn-default btn-primary o_disabled disabled");
				}
			}
		}
	}

	protected void formatPrice(AccessResult acResult) {
		BigDecimal lowestPriceAmount = null;
		String lowestPrice = null;
		int numOfPrices = 0;
		for (OfferAccess access:acResult.getAvailableMethods()) {
			Price p = access.getOffer().getPrice();
			String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
			if (p != null && StringHelper.containsNonWhitespace(price)) {
				numOfPrices++;
				if (lowestPriceAmount == null || lowestPriceAmount.compareTo(p.getAmount()) > 0) {
					lowestPriceAmount = p.getAmount();
					lowestPrice = price;
				}
			}
		}
		if (lowestPriceAmount != null) {
			if (numOfPrices > 1) {
				lowestPrice = translate("book.price.from", lowestPrice);
			}
			flc.contextPut("price", lowestPrice);
		}
	}

	protected void createGoToOffersLink(FormLayoutContainer layoutCont, boolean guestOnly) {
		offersLink = uifactory.addExternalLink("start", "#offers", "_self", layoutCont);
		offersLink.setCssClass("btn btn-default btn-primary ");
		offersLink.setElementCssClass("o_book o_button_call_to_action");
		offersLink.setName(translate("book.now"));
		offersLink.setVisible(!guestOnly);
	}
	

	protected FormLink createStartLink(FormLayoutContainer layoutCont) {
		return createStartLink(layoutCont, false);
	}
	
	protected FormLink createStartLink(FormLayoutContainer layoutCont, boolean autoBooking) {
		String linkText = getStartLinkText();
		String cmd = autoBooking? CMD_AUTO_BOOKING : CMD_START;
		FormLink link = uifactory.addFormLink("start", cmd, linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setEscapeMode(EscapeMode.html);
		link.setIconRightCSS("o_icon o_icon_start o_icon-lg");
		link.setPrimary(true);
		link.setFocus(true);
		link.setElementCssClass("o_start o_button_call_to_action");
		return link;
	}
	
	protected void showAccessDenied(Controller ctrl) {	
		listenTo(ctrl);
		flc.put("access.refused", ctrl.getInitialComponent());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_START.equals(cmd)) {
				fireEvent(ureq, START_EVENT);
			} else if (CMD_AUTO_BOOKING.equals(cmd)) {
				doAutoBooking(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAutoBooking(UserRequest ureq) {
		if (getIdentity() == null) {
			fireEvent(ureq, new BookEvent(getResourceKey()));
		} else {
			boolean success = tryAutoBooking(ureq);
			if (success) {
				fireEvent(ureq, START_EVENT);
			}
		}
	}

}
