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
package org.olat.modules.forms.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormEmailExecutor;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormParticipationImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormParticipationDAOTest extends OlatTestCase {
	
	private static final String IDENTIFIER_TYPE = "type";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormTestsHelper evaTestHelper;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	
	@Autowired
	private EvaluationFormParticipationDAO sut;
	
	@Before
	public void cleanUp() {
		evaTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateParticipation() {
		boolean anonymous = true;
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		dbInstance.commit();
		
		EvaluationFormParticipation participation = sut.createParticipation(survey, identifier, anonymous, 1, executor);
		
		assertThat(participation).isNotNull();
		assertThat(participation.getCreationDate()).isNotNull();
		assertThat(participation.getLastModified()).isNotNull();
		assertThat(participation.getIdentifier().getType()).isEqualTo(IDENTIFIER_TYPE);
		assertThat(participation.getIdentifier().getKey()).isEqualTo(identifierKey);
		assertThat(participation.isAnonymous()).isEqualTo(anonymous);
		assertThat(participation.getExecutor()).isEqualTo(executor);
		assertThat(participation.getStatus()).isEqualTo(EvaluationFormParticipationStatus.prepared);
	}
	
	@Test
	public void shouldUpdateStatus() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormParticipation participation = sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, 1, null);
		dbInstance.commit();
		
		EvaluationFormParticipationStatus newStatus = EvaluationFormParticipationStatus.done;
		sut.changeStatus(participation, newStatus);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(identifier);
		
		assertThat(loadedParticipation.getStatus()).isEqualTo(newStatus);
	}
	
	@Test
	public void shouldUpdateParticipation() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormParticipation participation = sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, 1, null);
		dbInstance.commit();
		
		participation.setAnonymous(true);
		
		EvaluationFormParticipation updateedParticipation = sut.updateParticipation(participation);
		
		assertThat(updateedParticipation.isAnonymous()).isTrue();
	}
	
	@Test
	public void shouldLoadByKey() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipationRef participation = sut.createParticipation(survey, identifier, false, 1, executor);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByKey(participation);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadBySurveyAndStatus() {
		EvaluationFormParticipationStatus status = EvaluationFormParticipationStatus.done;
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormSurvey survey2 = evaTestHelper.createSurvey();
		EvaluationFormParticipation participation = evaTestHelper.createParticipation(survey, true);
		participation = sut.changeStatus(participation, status);
		EvaluationFormParticipation otherStatus = evaTestHelper.createParticipation(survey, true);
		otherStatus = sut.changeStatus(otherStatus, EvaluationFormParticipationStatus.prepared);
		EvaluationFormParticipation otherSurvey = evaTestHelper.createParticipation(survey2, false);
		otherSurvey = sut.changeStatus(otherSurvey, status);
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormParticipation> participations = sut.loadBySurvey(survey, status, false, true);

		assertThat(participations).contains(participation)
				.doesNotContain(otherStatus, otherSurvey);
	}
	
	@Test
	public void shouldLoadBySurveyAndEmailOnly() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		evaTestHelper.createParticipation(survey, true);
		evaTestHelper.createParticipation(survey, false);
		EvaluationFormParticipation participationEmail1 = evaluationFormManager.createParticipation(survey,
				new EvaluationFormEmailExecutor(random(), null, null));
		EvaluationFormParticipation participationEmail2 = evaluationFormManager.createParticipation(survey,
				new EvaluationFormEmailExecutor(random(), random(), random()));
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormParticipation> participations = sut.loadBySurvey(survey, null, true, true);
		
		assertThat(participations).containsExactlyInAnyOrder(participationEmail1, participationEmail2);
	}
	
	@Test
	public void shouldLoadBySurveyAndExecutor() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipationRef participation = sut.createParticipation(survey, identifier, false, 1, executor);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByExecutor(survey, executor, null).get(0);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadBySurveyAndExecutor_filter_lastRun() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipation participation1 = sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(), false, 1, executor);
		((EvaluationFormParticipationImpl)participation1).setLastRun(false);
		sut.updateParticipation(participation1);
		EvaluationFormParticipation participation2 = sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(), false, 2, executor);
		((EvaluationFormParticipationImpl)participation2).setLastRun(false);
		sut.updateParticipation(participation2);
		EvaluationFormParticipation participation3 = sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(), false, 3, executor);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadByExecutor(survey, executor, null)).containsExactlyInAnyOrder(participation1, participation2, participation3);
		assertThat(sut.loadByExecutor(survey, executor, Boolean.TRUE)).containsExactlyInAnyOrder(participation3);
		assertThat(sut.loadByExecutor(survey, executor, Boolean.FALSE)).containsExactlyInAnyOrder(participation1, participation2);
	}
	
	@Test
	public void shouldLoadBySurveyAndEmail() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		
		EvaluationFormParticipation participation1 = sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE, random()), random(), null, null);
		EvaluationFormParticipation participation2 = sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE, random()), random(), null, null);
		sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE, random()), random(), null, null);
		sut.createParticipation(survey, new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE, random()), false, 1, executor);
		dbInstance.commitAndCloseSession();
		
		List<EvaluationFormParticipation> participations = sut.loadByEmails(survey, List.of(participation1.getEmail(), participation2.getEmail()));
		
		assertThat(participations)
				.hasSize(2)
				.extracting(EvaluationFormParticipation::getKey)
				.containsExactlyInAnyOrder(participation1.getKey(), participation2.getKey());
	}
	
	@Test
	public void shouldLoadBySurveyAndIdentifier() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipationRef participation = sut.createParticipation(survey, identifier, false, 1, null);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(survey, identifier);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadByIdentifier() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		EvaluationFormParticipationRef participation = sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, 1, null);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(identifier);
		
		assertThat(loadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadByIdentifierAndReturnNuttIfNotUnique() {
		String identifierKey = UUID.randomUUID().toString();
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier(IDENTIFIER_TYPE,
				identifierKey);
		sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, 1, null);
		sut.createParticipation(evaTestHelper.createSurvey(), identifier, false, 1, null);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation = sut.loadByIdentifier(identifier);
		
		assertThat(loadedParticipation).isNull();
	}

	@Test
	public void shouldShouldDeleteParticipations() {
		EvaluationFormParticipation participation1 = evaTestHelper.createParticipation();
		EvaluationFormParticipation participation2 = evaTestHelper.createParticipation();
		EvaluationFormParticipation otherParticipation = evaTestHelper.createParticipation();
		dbInstance.commit();

		List<? extends EvaluationFormParticipationRef> participations = Arrays.asList(participation1, participation2);
		sut.deleteParticipations(participations);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation1 = sut.loadByIdentifier(participation1.getIdentifier());
		assertThat(loadedParticipation1).isNull();
		EvaluationFormParticipation loadedParticipation2 = sut.loadByIdentifier(participation2.getIdentifier());
		assertThat(loadedParticipation2).isNull();
		EvaluationFormParticipation loadedOtherParticipation = sut.loadByIdentifier(otherParticipation.getIdentifier());
		assertThat(loadedOtherParticipation).isEqualTo(otherParticipation);
	}

	@Test
	public void shouldShouldDeleteParticipationsOfSurvey() {
		EvaluationFormSurvey survey = evaTestHelper.createSurvey();
		EvaluationFormParticipation participation1 = evaTestHelper.createParticipation(survey, false);
		EvaluationFormParticipation participation2 = evaTestHelper.createParticipation(survey, false);
		EvaluationFormParticipation otherParticipation = evaTestHelper.createParticipation();
		dbInstance.commit();

		sut.deleteParticipations(survey);
		dbInstance.commit();
		
		EvaluationFormParticipation loadedParticipation1 = sut.loadByIdentifier(participation1.getIdentifier());
		assertThat(loadedParticipation1).isNull();
		EvaluationFormParticipation loadedParticipation2 = sut.loadByIdentifier(participation2.getIdentifier());
		assertThat(loadedParticipation2).isNull();
		EvaluationFormParticipation loadedOtherParticipation = sut.loadByIdentifier(otherParticipation.getIdentifier());
		assertThat(loadedOtherParticipation).isEqualTo(otherParticipation);
	}

}
