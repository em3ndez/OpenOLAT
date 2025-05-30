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
package org.olat.modules.reminder;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class RuleEditorFragment {
	
	protected ReminderRule rule;
	protected final FormUIFactory uifactory = FormUIFactory.getInstance();
	
	public RuleEditorFragment(ReminderRule rule) {
		this.rule = rule;
	}
	
	public abstract FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq);

	/**
	 * @param ureq The user request
	 * @param source The element which send the event
	 * @param event  The event
	 */
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		//
	}
	
	public abstract boolean validateFormLogic(UserRequest ureq);
	
	public abstract ReminderRule getConfiguration();

}
