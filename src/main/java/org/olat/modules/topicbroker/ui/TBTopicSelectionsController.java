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
package org.olat.modules.topicbroker.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBGroupRestrictionCandidates;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSecurityCallback;

/**
 * 
 * Initial date: 11 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicSelectionsController extends TBTopicListController {

	public TBTopicSelectionsController(UserRequest ureq, WindowControl wControl, TBBroker broker,
			TBSecurityCallback secCallback, TBParticipantCandidates participantCandidates,
			TBGroupRestrictionCandidates groupRestrictionCandidates) {
		super(ureq, wControl, broker, secCallback, participantCandidates, groupRestrictionCandidates);
	}

	@Override
	protected String getContextHelpUrl() {
		return null;
	}

	@Override
	protected String getFormInfo() {
		return null;
	}

	@Override
	protected boolean isShowStatus() {
		return true;
	}

	@Override
	protected boolean isShowSelections() {
		return true;
	}

}
