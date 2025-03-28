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
package org.olat.resource.accesscontrol.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.Price;

public class OLATResourceAccess {
	
	private OLATResource resource;
	private List<PriceMethodBundle> methods = new ArrayList<>(3);
	
	public OLATResourceAccess() {
		//
	}
	
	public OLATResourceAccess(OLATResource resource, Price price, AccessMethod method, boolean autoBooking) {
		this.resource = resource;
		
		if(method != null) {
			this.methods.add(new PriceMethodBundle(price, method, autoBooking));
		}
	}

	public OLATResource getResource() {
		return resource;
	}
	
	public void setResource(OLATResource resource) {
		this.resource = resource;
	}
	
	public List<PriceMethodBundle> getMethods() {
		return methods;
	}
	
	public void addBundle(Price price, AccessMethod method, boolean autoBooking) {
		if(method != null) {
			this.methods.add(new PriceMethodBundle(price, method, autoBooking));
		}
	}
}
