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
package org.olat.modules.topicbroker.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBEnrollmentStats;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicRef;
import org.olat.modules.topicbroker.ui.TBUIFactory;


/**
 * 
 * Initial date: 17 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentStatsCalculation implements TBEnrollmentStats {

	private final int numIdentities;
	private Map<Integer, Integer> selectionSortOrderToNum = new HashMap<>();
	private Map<Long, Integer> topicKeyToNum = new HashMap<>();
	private int numRequiredEnrollments = 0;
	private int numEnrollments = 0;
	private final int numTopicsTotal;
	private int numWaitingList = 0;
	private int numMissing = 0;
	
	public TBEnrollmentStatsCalculation(TBBroker broker, List<Identity> identities,
			List<TBParticipant> participants, List<TBTopic> topics, List<TBSelection> selections) {
		numIdentities = identities.size();
		numTopicsTotal = topics.size();
		
		Map<Long, TBParticipant> identityKeyToParticipant = participants.stream().collect(Collectors.toMap(participant -> participant.getIdentity().getKey(), Function.identity()));
		Map<Long, List<TBSelection>> identityKeyToSelections = selections.stream()
				.collect(Collectors.groupingBy(selection -> selection.getParticipant().getIdentity().getKey()));
		
		for (Identity identity : identities) {
			TBParticipant participant = identityKeyToParticipant.get(identity.getKey());
			int participantNaxEnrollments = TBUIFactory.getRequiredEnrollments(broker, participant);
			
			int participantNumEnrollments = 0;
			int participantNumWaitingList = 0;
			
			List<TBSelection> identitySelections = identityKeyToSelections.getOrDefault(identity.getKey(), List.of());
			for (int i = 0; i < broker.getMaxSelections() && i < identitySelections.size(); i++) {
				TBSelection selection = identitySelections.get(i);
				if (selection.isEnrolled()) {
					Integer sortOrder = Integer.valueOf(selection.getSortOrder());
					Integer currentCount = selectionSortOrderToNum.getOrDefault(sortOrder, Integer.valueOf(0));
					selectionSortOrderToNum.put(sortOrder, currentCount + 1);
					participantNumEnrollments++;
					
					Long topicKey = selection.getTopic().getKey();
					Integer currentTopicCount = topicKeyToNum.getOrDefault(topicKey, Integer.valueOf(0));
					topicKeyToNum.put(topicKey, currentTopicCount + 1);
				} else {
					participantNumWaitingList++;
				}
			}
			
			numRequiredEnrollments += participantNaxEnrollments;
			numEnrollments += participantNumEnrollments;
			
			if (participantNumEnrollments < participantNaxEnrollments) {
				int enrollmentsGap = participantNaxEnrollments - participantNumEnrollments;
				if (enrollmentsGap <= participantNumWaitingList) {
					numWaitingList += enrollmentsGap;
				} else {
					numWaitingList += participantNumWaitingList;
					numMissing += (enrollmentsGap - participantNumWaitingList);
				}
			}
		}
	}

	@Override
	public int getNumIdentities() {
		return numIdentities;
	}

	@Override
	public int getNumRequiredEnrollments() {
		return numRequiredEnrollments;
	}

	@Override
	public int getNumEnrollments() {
		return numEnrollments;
	}

	@Override
	public int getNumEnrollments(int selectionSortOrder) {
		return selectionSortOrderToNum.getOrDefault(Integer.valueOf(selectionSortOrder), Integer.valueOf(0)).intValue();
	}

	@Override
	public int getNumEnrollments(TBTopicRef topic) {
		return topicKeyToNum.getOrDefault(topic.getKey(), Integer.valueOf(0)).intValue();
	}

	@Override
	public int getNumTopicsTotal() {
		return numTopicsTotal;
	}

	@Override
	public int getNumTopicsMinReached() {
		// If a topic did not reached the minimum number of participants,
		// all enrollments were removed by the enrollment process.
		return topicKeyToNum.size();
	}

	@Override
	public int getNumWaitingList() {
		return numWaitingList;
	}

	@Override
	public int getNumMissing() {
		return numMissing;
	}

}
