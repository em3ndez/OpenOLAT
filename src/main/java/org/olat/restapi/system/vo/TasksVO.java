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
package org.olat.restapi.system.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author srosse, stephane.rosse@cyberiacafe.ch, http://www.cyberiacafe.ch
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "tasksVO")
public class TasksVO {
	
	@XmlElementWrapper(name="longRunningTasks")
	@XmlElement(name="longRunningTask",nillable=true)
	private String[] longRunningTasks;

	public String[] getLongRunningTasks() {
		return longRunningTasks;
	}

	public void setLongRunningTasks(String[] longRunningTasks) {
		this.longRunningTasks = longRunningTasks;
	}
	
	

}
