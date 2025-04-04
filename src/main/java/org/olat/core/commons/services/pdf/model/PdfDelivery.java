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
package org.olat.core.commons.services.pdf.model;

import java.io.Serializable;

import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 6 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfDelivery implements Serializable {

	private static final long serialVersionUID = -8032606199374880702L;
	private final String key;
	private final PdfOutputOptions options;
	
	private String directory;
	private Identity identity;
	
	private Window window;
	private WindowControl windowControl;
	private PopupBrowserWindow browserWindow;
	private ControllerCreator controllerCreator;
	
	public PdfDelivery(String key, PdfOutputOptions options) {
		this.key = key;
		this.options = options;
	}

	public String getKey() {
		return key;
	}
	
	public PdfOutputOptions getOptions() {
		return options;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}

	public WindowControl getWindowControl() {
		return windowControl;
	}

	public void setWindowControl(WindowControl windowControl) {
		this.windowControl = windowControl;
	}

	public ControllerCreator getControllerCreator() {
		return controllerCreator;
	}

	public void setControllerCreator(ControllerCreator controllerCreator) {
		this.controllerCreator = controllerCreator;
	}

	public PopupBrowserWindow getBrowserWindow() {
		return browserWindow;
	}

	public void setBrowserWindow(PopupBrowserWindow browserWindow) {
		this.browserWindow = browserWindow;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PdfDelivery) {
			PdfDelivery delivery = (PdfDelivery)obj;
			return key != null && key.equals(delivery.key);
		}
		return false;
	}
}
