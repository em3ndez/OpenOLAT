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
* <p>
*/ 

package org.olat.core.gui.control.winmgr;

import org.json.JSONObject;
import org.olat.core.gui.control.winmgr.CommandFactory.InvokeIdentifier;

/**
 * Initial Date:  22.03.2006 <br>
 *
 * @author Felix Jost
 */
public class Command {
	private InvokeIdentifier command;
	private JSONObject subJSON;
	
	protected Command(InvokeIdentifier command) {
		this.command = command;
	}
	
	/**
	 * @return Returns the command.
	 */
	public final int getCommand() {
		return command.number();
	}

	/**
	 * @return Returns the subJSON.
	 */
	public final JSONObject getSubJSON() {
		return subJSON;
	}

	/**
	 * @param subJSON The subJSON to set.
	 */
	public final void setSubJSON(JSONObject subJSON) {
		this.subJSON = subJSON;
	}
	
}
