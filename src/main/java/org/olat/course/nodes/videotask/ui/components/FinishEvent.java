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
package org.olat.course.nodes.videotask.ui.components;

import org.olat.core.gui.control.Event;
import org.olat.modules.video.VideoTaskSession;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FinishEvent extends Event {
	
	private static final long serialVersionUID = -1407375921170297386L;

	public static final String FINISH_TASK = "finish-task";
	
	private boolean startNextAttempt;
	private VideoTaskSession taskSession;
	
	public FinishEvent(boolean startNextAttempt) {
		this(null, startNextAttempt);
	}
	
	public FinishEvent(VideoTaskSession taskSession, boolean startNextAttempt) {
		super(FINISH_TASK);
		this.taskSession = taskSession;
		this.startNextAttempt = startNextAttempt;
	}
	
	public boolean isStartNextAttempt() {
		return startNextAttempt;
	}
	
	/**
	 * return The assessment session by only in test mode
	 */
	public VideoTaskSession getTaskSession() {
		return taskSession;
	}

}
