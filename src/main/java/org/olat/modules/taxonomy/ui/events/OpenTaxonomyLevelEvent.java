/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui.events;

import org.olat.core.gui.control.Event;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: May 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OpenTaxonomyLevelEvent extends Event {

	private static final long serialVersionUID = 6392149973532210626L;

	private final TaxonomyLevel taxonomyLevel;
	
	public OpenTaxonomyLevelEvent(TaxonomyLevel taxonomyLevel) {
		super("open-tax-level");
		this.taxonomyLevel = taxonomyLevel;
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

}
