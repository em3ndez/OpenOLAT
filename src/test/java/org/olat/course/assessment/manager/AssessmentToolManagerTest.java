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
package org.olat.course.assessment.manager;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentConfigMock;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CoachingAssessmentEntry;
import org.olat.course.assessment.CoachingAssessmentSearchParams;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.course.assessment.model.AssessedCurriculumElement;
import org.olat.course.assessment.model.AssessmentScoreStatistic;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.core.CourseElement;
import org.olat.course.core.manager.CourseElementDAO;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 23.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolManagerTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(AssessmentToolManagerTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private CourseElementDAO courseElementDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	private static Organisation defaultUnitTestOrganisation;
	private static Identity defaultUnitTestAdministrator;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper
					.createAndPersistRndAdmin("Cur-Elem-Web", defaultUnitTestOrganisation)
					.getIdentity();
		}
	}
	
	
	@Test
	public void assessmentTool_coach() {
		//course
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-author-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-1", defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-2", defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-3", defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-4", defaultUnitTestOrganisation, null);
		Identity assessedIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-5", defaultUnitTestOrganisation, null);
		Identity assessedIdentity6 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-6", defaultUnitTestOrganisation, null);
		Identity assessedIdentity7 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-7", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-1", defaultUnitTestOrganisation, null);

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-2", "assessment-tool-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity5, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), random(), false, null);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curriculumElement1, entry, false);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, false);
		
		curriculumService.addMember(curriculumElement1, assessedIdentity6, CurriculumRoles.participant, author);
		curriculumService.addMember(curriculumElement2, assessedIdentity7, CurriculumRoles.participant, author);
		curriculumService.addMember(curriculumElement1, coach, CurriculumRoles.coach, author);
		
		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		ae1.setScore(BigDecimal.valueOf(3.0));
		ae1.setPassed(Boolean.FALSE);
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		ae2.setScore(BigDecimal.valueOf(5.0));
		ae2.setPassed(Boolean.TRUE);
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, refEntry);
		ae3.setScore(BigDecimal.valueOf(8.0));
		ae3.setPassed(Boolean.TRUE);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, refEntry);
		ae4.setScore(BigDecimal.valueOf(9.0));
		ae4.setPassed(Boolean.TRUE);
		ae4 = assessmentEntryDao.updateAssessmentEntry(ae4);
		AssessmentEntry ae5 = assessmentEntryDao.createAssessmentEntry(assessedIdentity5, null, entry, subIdent, null, refEntry);
		ae5.setScore(BigDecimal.valueOf(9.0));
		ae5.setPassed(Boolean.TRUE);
		ae5.setObligation(ObligationOverridable.of(AssessmentObligation.excluded));
		ae5 = assessmentEntryDao.updateAssessmentEntry(ae5);
		AssessmentEntry ae6 = assessmentEntryDao.createAssessmentEntry(assessedIdentity6, null, entry, subIdent, null, refEntry);
		ae6.setScore(BigDecimal.valueOf(4.0));
		ae6.setPassed(Boolean.TRUE);
		ae6 = assessmentEntryDao.updateAssessmentEntry(ae6);
		AssessmentEntry ae7 = assessmentEntryDao.createAssessmentEntry(assessedIdentity7, null, entry, subIdent, null, refEntry);
		ae7.setScore(BigDecimal.valueOf(9.0));
		ae7.setPassed(Boolean.TRUE);
		ae7 = assessmentEntryDao.updateAssessmentEntry(ae7);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, null, refEntry);
		// Coach has done the assessment as a fake participant
		AssessmentEntry aeCoach = assessmentEntryDao.createAssessmentEntry(coach, null, entry, subIdent, null, refEntry);
		aeCoach.setScore(BigDecimal.valueOf(10.0));
		aeCoach.setPassed(Boolean.TRUE);
		aeCoach = assessmentEntryDao.updateAssessmentEntry(aeCoach);
		dbInstance.commitAndCloseSession();
		
		// coach of group 1 with id 1 and id2
		List<BusinessGroup> coachedGroups = Collections.singletonList(group1);
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(false, false, false, false,
				true, false, coachedGroups, singleton(coach));
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		params.setParticipantTypes(Collections.singleton(ParticipantType.member));

		// Test with the coached members
		AssessmentStatistics statistics = assessmentToolManager.getStatistics(coach, params);
		Assert.assertEquals(4.0d, statistics.getAverageScore().doubleValue(), 0.0001);
		Assert.assertEquals(1, statistics.getCountFailed());
		Assert.assertEquals(2, statistics.getCountPassed());
		
		AssessmentMembersStatistics participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(4, participantStatistics.getNumOfMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());

		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(coach, params);
			assertThat(assessedIdentities).containsExactlyInAnyOrder(
					assessedIdentity1,
					assessedIdentity2,
					assessedIdentity5,
					assessedIdentity6
				);

		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, AssessmentEntryStatus.notStarted);
		Assert.assertEquals(0, assessmentEntries.size());
		
		// Not, let's get infos for fake participants as well
		params.setParticipantTypes(null);
		
		statistics = assessmentToolManager.getStatistics(coach, params);
		Assert.assertEquals(5.5d, statistics.getAverageScore().doubleValue(), 0.0001);
		Assert.assertEquals(1, statistics.getCountFailed());
		Assert.assertEquals(3, statistics.getCountPassed());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(4, participantStatistics.getNumOfMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(1, participantStatistics.getNumOfFakeParticipants());
		
		assessedIdentities = assessmentToolManager.getAssessedIdentities(coach, params);
		assertThat(assessedIdentities).containsExactlyInAnyOrder(
					assessedIdentity1,
					assessedIdentity2,
					assessedIdentity5,
					assessedIdentity6,
					coach
				);
		
		
		// separate check with more options in the search parameters
		// add by group key 
		params.setBusinessGroupKeys(Collections.singletonList(group1.getKey()));
		params.setCurriculumElementKeys(Collections.singletonList(curriculumElement1.getKey()));
		
		//check assessed identities list
		List<Identity> assessedIdentitiesAlt = assessmentToolManager.getAssessedIdentities(coach, params);
		Assert.assertNotNull(assessedIdentitiesAlt);
		Assert.assertEquals(4, assessedIdentitiesAlt.size());
		
		// assessed groups
		List<AssessedBusinessGroup> assessedGroups = assessmentToolManager.getBusinessGroupStatistics(coach, params);
		Assert.assertNotNull(assessedGroups);
		Assert.assertEquals(1, assessedGroups.size());
		Assert.assertEquals(2, assessedGroups.get(0).getNumOfParticipants());
		Assert.assertEquals(1, assessedGroups.get(0).getNumOfPassed());
		
		// assessed curriculum elements
		List<AssessedCurriculumElement> curriculumElementStatistics = assessmentToolManager.getCurriculumElementStatistics(coach, params);
		Assert.assertNotNull(curriculumElementStatistics);
		Assert.assertEquals(1, curriculumElementStatistics.size());
		Assert.assertEquals(1, curriculumElementStatistics.get(0).getNumOfParticipants());
		Assert.assertEquals(1, assessedGroups.get(0).getNumOfPassed());
		
		List<AssessmentEntry> assessmentEntriesAlt = assessmentToolManager.getAssessmentEntries(coach, params, AssessmentEntryStatus.notStarted);
		Assert.assertNotNull(assessmentEntriesAlt);
		Assert.assertEquals(0, assessmentEntriesAlt.size());
	}
	
	@Test
	public void assessmentTool_admin() {
		//course
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-1", defaultUnitTestOrganisation).getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-5", defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-6", defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-7", defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-8", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-9", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity6 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-10", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity7 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-11", defaultUnitTestOrganisation, null);
		Identity assessedIdentity8 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-12", defaultUnitTestOrganisation, null);
		Identity assessedIdentity9 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-13", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-9", defaultUnitTestOrganisation, null);

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-2", "assessment-tool-bg-2-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		ae1.setScore(BigDecimal.valueOf(3.0));
		ae1.setPassed(Boolean.FALSE);
		ae1.setAssessmentStatus(AssessmentEntryStatus.done);
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		ae2.setScore(BigDecimal.valueOf(5.0));
		ae2.setPassed(Boolean.TRUE);
		ae2.setAssessmentStatus(AssessmentEntryStatus.inReview);
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, refEntry);
		ae3.setScore(BigDecimal.valueOf(8.0));
		ae3.setPassed(Boolean.TRUE);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, refEntry);
		ae4.setScore(BigDecimal.valueOf(9.0));
		ae4.setPassed(Boolean.TRUE);
		ae4 = assessmentEntryDao.updateAssessmentEntry(ae4);
		AssessmentEntry ae5 = assessmentEntryDao.createAssessmentEntry(assessedExtIdentity5, null, entry, subIdent, null, refEntry);
		ae5.setScore(BigDecimal.valueOf(3.0));
		ae5.setGrade("Grade A");
		ae5.setPassed(Boolean.TRUE);
		ae5 = assessmentEntryDao.updateAssessmentEntry(ae5);
		AssessmentEntry ae6 = assessmentEntryDao.createAssessmentEntry(assessedExtIdentity6, null, entry, subIdent, null, refEntry);
		ae6.setScore(BigDecimal.valueOf(4.0));
		ae6.setGrade("Grade A");
		ae6.setPassed(Boolean.TRUE);
		ae6 = assessmentEntryDao.updateAssessmentEntry(ae6);
		AssessmentEntry ae7 = assessmentEntryDao.createAssessmentEntry(assessedExtIdentity7, null, entry, subIdent, null, refEntry);
		ae7.setScore(BigDecimal.valueOf(5.0));
		ae7.setPassed(Boolean.TRUE);
		ae7 = assessmentEntryDao.updateAssessmentEntry(ae7);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity8, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity9, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, null, refEntry);
		// Fake participants
		AssessmentEntry aeAdmin = assessmentEntryDao.createAssessmentEntry(admin, null, entry, subIdent, null, refEntry);
		aeAdmin.setScore(BigDecimal.valueOf(10.0));
		aeAdmin.setPassed(Boolean.TRUE);
		aeAdmin = assessmentEntryDao.updateAssessmentEntry(aeAdmin);
		AssessmentEntry aeCoach = assessmentEntryDao.createAssessmentEntry(coach, null, entry, subIdent, null, refEntry);
		aeCoach.setScore(BigDecimal.valueOf(10.0));
		aeCoach.setPassed(Boolean.TRUE);
		aeCoach = assessmentEntryDao.updateAssessmentEntry(aeCoach);
		dbInstance.commitAndCloseSession();
		
		// administrator with full access
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, Set.of(admin, coach));
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);
		params.setParticipantTypes(Set.of(ParticipantType.member, ParticipantType.nonMember));
		
		AssessmentStatistics statistics = assessmentToolManager.getStatistics(coach, params);
		Assert.assertEquals(5.28571d, statistics.getAverageScore().doubleValue(), 0.0001);
		Assert.assertEquals(9d, statistics.getMaxScore().doubleValue(), 0.0001);
		Assert.assertEquals(9, statistics.getCountTotal());
		Assert.assertEquals(1, statistics.getCountFailed());
		Assert.assertEquals(6, statistics.getCountPassed());
		Assert.assertEquals(2, statistics.getCountUndefined());
		Assert.assertEquals(1, statistics.getCountDone());
		Assert.assertEquals(8, statistics.getCountNotDone());
		Assert.assertEquals(7, statistics.getCountScore());
		
		AssessmentMembersStatistics participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(3, participantStatistics.getNumOfMembers());
		Assert.assertEquals(6, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());

		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(coach, params);
			assertThat(assessedIdentities).containsExactlyInAnyOrder(
					assessedIdentity1,
					assessedIdentity2,
					assessedIdentity3,
					assessedIdentity4,
					assessedExtIdentity5,
					assessedExtIdentity6,
					assessedExtIdentity7,
					assessedIdentity8,
					assessedIdentity9
				);
		
		// Check the participant filter: No Filter
		params.setParticipantTypes(null);
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(11, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(3, participantStatistics.getNumOfMembers());
		Assert.assertEquals(6, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(2, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: Members
		params.setParticipantTypes(Set.of(ParticipantType.member));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(3, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(3, participantStatistics.getNumOfMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: Not member
		params.setParticipantTypes(Set.of(ParticipantType.nonMember));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(6, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(0, participantStatistics.getNumOfMembers());
		Assert.assertEquals(6, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: Member or not member
		params.setParticipantTypes(Set.of(ParticipantType.member, ParticipantType.nonMember));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(9, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(3, participantStatistics.getNumOfMembers());
		Assert.assertEquals(6, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: fake participants
		params.setParticipantTypes(Set.of(ParticipantType.fakeParticipant));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(2, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(0, participantStatistics.getNumOfMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(2, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: Not member and group member
		params.setParticipantTypes(Set.of(ParticipantType.nonMember));
		params.setBusinessGroupKeys(List.of(group1.getKey()));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(0, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(0, participantStatistics.getNumOfMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: (Member of not member) and group member
		params.setParticipantTypes(Set.of(ParticipantType.member, ParticipantType.nonMember));
		params.setBusinessGroupKeys(List.of(group1.getKey()));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(2, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(2, participantStatistics.getNumOfMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());
		
		// Check the participant filter: Non member, no fake participants
		assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, Collections.emptySet());
		params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);
		params.setParticipantTypes(Set.of(ParticipantType.nonMember));
		assessedIdentities = assessmentToolManager.getAssessedIdentities(admin, params);
		Assert.assertEquals(6, assessedIdentities.size());
		
		participantStatistics = assessmentToolManager.getNumberOfParticipants(coach, params, true);
		Assert.assertEquals(0, participantStatistics.getNumOfMembers());
		Assert.assertEquals(6, participantStatistics.getNumOfNonMembers());
		Assert.assertEquals(0, participantStatistics.getNumOfFakeParticipants());
	}
	
	@Test
	public void assessmentTool_owner() {
		//course
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-author-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("asto-1", defaultUnitTestOrganisation, null);

		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();

		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		ae1.setScore(BigDecimal.valueOf(3.0));
		ae1.setPassed(Boolean.FALSE);
		// no fake users
		dbInstance.commitAndCloseSession();
		
		// coach of group 1 with id 1 and id2
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(false, false, false, false,
				true, false, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);

		// Test with the coached members
		AssessmentStatistics statistics = assessmentToolManager.getStatistics(author, params);
		Assert.assertNotNull(statistics);
	}
	
	@Test
	public void getNumberOfParticipants() {
		//course
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-1", defaultUnitTestOrganisation).getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator, defaultUnitTestOrganisation);
		
		//members as participant and coach
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-5", defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-6", defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-7", defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-8", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-9", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity6 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-10", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity7 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-11", defaultUnitTestOrganisation, null);
		Identity assessedExtIdentity8 = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-ext-12", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-9", defaultUnitTestOrganisation, null);
		Identity coachWithAE = JunitTestHelper.createAndPersistIdentityAsRndUser("ast-coach-9-ae", defaultUnitTestOrganisation, null);

		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-tool-bg-1", "assessment-tool-bg-1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coachWithAE, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry, GroupRoles.participant.name());
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedExtIdentity5, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedExtIdentity6, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedExtIdentity7, null, entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(null, UUID.randomUUID().toString(), entry, subIdent, null, refEntry);
		assessmentEntryDao.createAssessmentEntry(coachWithAE, null, entry, subIdent, null, refEntry);
		dbInstance.commitAndCloseSession();
		
		// the course infos need to calculate the number of participants
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity1);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity2);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity3);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedIdentity4);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity5);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity6);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity7);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), assessedExtIdentity8);
		userCourseInformationsManager.updateUserCourseInformations(entry.getOlatResource(), coachWithAE);
		dbInstance.commitAndCloseSession();
		
		// statistics as admin
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, Collections.emptySet());
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, refEntry, assessmentCallback);

		AssessmentMembersStatistics statisticsAsAdmin = assessmentToolManager.getNumberOfParticipants(admin, params, true);
		Assert.assertNotNull(statisticsAsAdmin);
		Assert.assertEquals(3, statisticsAsAdmin.getNumOfNonMembers());
		Assert.assertEquals(4, statisticsAsAdmin.getNumOfMembers());
		Assert.assertEquals(3, statisticsAsAdmin.getNumOfNonMembersLoggedIn());
		Assert.assertEquals(4, statisticsAsAdmin.getNumOfMembersLoggedIn());
	}
	
	@Test
	public void getStatistics_filterByObligation() {
		// Course and users
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-2").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Assessment data
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, null);
		ae1.setScore(BigDecimal.valueOf(1.0));
		ae1.setPassed(Boolean.TRUE);
		ae1.setObligation(null); // should be treated like mandatory
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, null);
		ae2.setScore(BigDecimal.valueOf(4.0));
		ae2.setPassed(Boolean.TRUE);
		ae2.setObligation(ObligationOverridable.of(AssessmentObligation.mandatory));
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, null);
		ae3.setScore(BigDecimal.valueOf(1.0));
		ae3.setPassed(Boolean.TRUE);
		ae3.setObligation(ObligationOverridable.of(AssessmentObligation.optional));
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, null);
		ae4.setScore(BigDecimal.valueOf(0.0));
		ae4.setPassed(Boolean.FALSE);
		ae4.setObligation(ObligationOverridable.of(AssessmentObligation.excluded));
		ae4 = assessmentEntryDao.updateAssessmentEntry(ae4);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		params.setAssessmentObligations(List.of(AssessmentObligation.mandatory, AssessmentObligation.optional));
		AssessmentStatistics statistics = assessmentToolManager.getStatistics(admin, params);
		
		Assert.assertEquals(2d, statistics.getAverageScore().doubleValue(), 0.0001);
		Assert.assertEquals(0, statistics.getCountFailed());
		Assert.assertEquals(3, statistics.getCountPassed());
	}
	
	@Test
	public void getScoreStatistic() {
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-3").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Assessment data
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, null);
		ae1.setScore(BigDecimal.valueOf(4.1));
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, null);
		ae2.setScore(BigDecimal.valueOf(3.9));
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, null);
		ae3.setScore(BigDecimal.valueOf(1));
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, null);
		ae4 = assessmentEntryDao.updateAssessmentEntry(ae4);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		List<AssessmentScoreStatistic> scoreStatistics = assessmentToolManager.getScoreStatistics(admin, params);
		assertThat(scoreStatistics).hasSize(2);
		
		Map<Integer, Long> scoreToCount = scoreStatistics.stream()
				.collect(Collectors.toMap(AssessmentScoreStatistic::getScore, AssessmentScoreStatistic::getCount));
		assertThat(scoreToCount.get(Integer.valueOf(4))).isEqualTo(2);
		assertThat(scoreToCount.get(Integer.valueOf(1))).isEqualTo(1);
	}
	
	@Test
	public void getAssessmentEntries_filter_passedOverridden() {
		// Course and users
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Assessment data
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, null);
		ae1.setPassedOverridable(Overridable.of(Boolean.TRUE));
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, null);
		ae2.setPassedOverridable(Overridable.of(Boolean.FALSE));
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, null);
		Overridable<Boolean> passed3 = Overridable.of(Boolean.TRUE);
		passed3.override(Boolean.FALSE, admin, new Date());
		ae3.setPassedOverridable(passed3);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, null);
		Overridable<Boolean> passed4 = Overridable.of(Boolean.FALSE);
		passed4.override(Boolean.TRUE, admin, new Date());
		ae4.setPassedOverridable(passed4);
		ae4 = assessmentEntryDao.updateAssessmentEntry(ae4);
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2, ae3, ae4);
		
		params.setPassedOverridden(Boolean.FALSE);
		assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2);
		
		params.setPassedOverridden(Boolean.TRUE);
		assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae3, ae4);
	}
	
	@Test
	public void getAssessmentEntries_filter_userVisibility() {
		// Course and users
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-4").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Assessment data
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, null);
		ae1.setObligation(null);
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, null);
		ae2.setUserVisibility(Boolean.TRUE);
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, null);
		ae3.setUserVisibility(Boolean.FALSE);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2, ae3);
		
		params.setUserVisibility(Boolean.TRUE);
		assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2);
		
		params.setUserVisibility(Boolean.FALSE);
		assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae3);
	}
	
	@Test
	public void getAssessmentEntries_filter_scoreNull() {
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-5").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		createCourseElement(entry, subIdent);
		
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.coach.name());
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity1, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae1.setScore(new BigDecimal("1"));
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity2, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae2.setScore(new BigDecimal("2"));
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity3, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae3.setScore(null);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		params.setScoreNull(Boolean.FALSE);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2).doesNotContain(ae3);
		
		params.setScoreNull(Boolean.TRUE);
		assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae3).doesNotContain(ae1, ae2);
	}
	
	@Test
	public void getAssessmentEntries_filter_gradeNull() {
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-6").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		createCourseElement(entry, subIdent);
		
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.coach.name());
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity1, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae1.setGrade(random());
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity2, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae2.setGrade(random());
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity3, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae3.setScore(null);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		params.setGradeNull(Boolean.FALSE);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2).doesNotContain(ae3);
		
		params.setGradeNull(Boolean.TRUE);
		assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae3).doesNotContain(ae1, ae2);
	}
	
	@Test
	public void getAssessmentEntries_filter_obligation() {
		// Course and users
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-7").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Assessment data
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, null);
		ae1.setObligation(null);
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, null);
		ae2.setObligation(ObligationOverridable.of(AssessmentObligation.mandatory));
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, null);
		ae3.setObligation(ObligationOverridable.of(AssessmentObligation.optional));
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		AssessmentEntry ae4 = assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, null, null);
		ae4.setObligation(ObligationOverridable.of(AssessmentObligation.excluded));
		ae4 = assessmentEntryDao.updateAssessmentEntry(ae4);
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2, ae3, ae4);
		
		params.setAssessmentObligations(List.of(AssessmentObligation.mandatory, AssessmentObligation.optional));
		assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2, ae3);
	}
	
	@Test
	public void getAssessmentEntries_filter_subIdents() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin);
		String subIden1 = random();
		createCourseElement(entry, subIden1);
		String subIden2 = random();
		createCourseElement(entry, subIden2);
		String subIden3 = random();
		createCourseElement(entry, subIden3);
		
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.coach.name());
		
		AssessmentEntry ae11 = createAssessmentEntry(assessedIdentity1, entry, subIden1, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae12 = createAssessmentEntry(assessedIdentity1, entry, subIden2, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae13 = createAssessmentEntry(assessedIdentity1, entry, subIden3, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae21 = createAssessmentEntry(assessedIdentity2, entry, subIden1, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae22 = createAssessmentEntry(assessedIdentity2, entry, subIden2, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae31 = createAssessmentEntry(assessedIdentity3, entry, subIden1, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, null, null, assessmentCallback);
		params.setSubIdents(List.of(subIden2, subIden3));
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(coach, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae12, ae13, ae22).doesNotContain(ae11, ae21, ae31);
	}
	
	@Test
	public void getAssessmentEntries_filter_identityKeys() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// Assessment data
		AssessmentEntry ae1 = assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, null, null);
		AssessmentEntry ae2 = assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, null, null);
		dbInstance.commitAndCloseSession();
		
		AssessmentToolSecurityCallback assessmentCallback = new AssessmentToolSecurityCallback(true, false, true, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(entry, subIdent, null, assessmentCallback);
		params.setIdentityKeys(Set.of(assessedIdentity1.getKey(), assessedIdentity2.getKey()));
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(admin, params, null);
		assertThat(assessmentEntries).containsExactlyInAnyOrder(ae1, ae2);
	}
	
	@Test
	public void getCoachingEntries() {
		//course
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-8").getIdentity();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		RepositoryEntry entry3 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		RepositoryEntry entry4 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		RepositoryEntry entryTrashed = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		repositoryService.deleteSoftly(entryTrashed, admin, false, false);
		dbInstance.commitAndCloseSession();
		
		String subIdent = random();
		createCourseElement(entry1, subIdent);
		createCourseElement(entry2, subIdent);
		createCourseElement(entry3, subIdent);
		createCourseElement(entry4, subIdent);
		createCourseElement(entryTrashed, subIdent);
		dbInstance.commitAndCloseSession();
		
		// Group participant
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Group participant
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Group participant, group not linked to the course
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant and group participant
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant
		Identity assessedIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, no assessment entry
		Identity assessedIdentity8 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, obligation mandatory
		Identity assessedIdentity9 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, obligation mandatory
		Identity assessedIdentity10 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, obligation mandatory
		Identity assessedIdentity11 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, obligation none
		Identity assessedIdentity12 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant by owner
		Identity assessedIdentity13 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, only group coach
		Identity assessedIdentity14 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, wrong status
		Identity assessedIdentity15 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, wrong user visibility
		Identity assessedIdentity16 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Course participant, no user visibility (= true)
		Identity assessedIdentity17 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Not member but assessment entry (coach)
		Identity assessedIdentity18 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Not member but assessment entry (owner)
		Identity assessedIdentity19 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		// Member, but course in trash
		Identity assessedIdentity21 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		//Member but identity is deleted
		Identity assessedIdentity22 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		assessedIdentity22 = securityManager.saveIdentityStatus(assessedIdentity22, Identity.STATUS_DELETED, admin);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		dbInstance.commitAndCloseSession();

		BusinessGroup group1 = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry1);
		
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coach, entry2, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach, entry3, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coach, entry3, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach, entryTrashed, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(assessedIdentity4, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity5, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity8, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity9, entry2, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity10, entry2, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity11, entry2, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity12, entry2, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity13, entry2, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity14, entry3, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity15, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity16, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity17, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity21, entryTrashed, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity22, entryTrashed, GroupRoles.participant.name());
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity4, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group1, GroupRoles.coach.name());
		
		// Group not linked to the course
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, random(), random(), BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRole(assessedIdentity3, group2, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, group2, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity1, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity2, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity3, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae4 = createAssessmentEntry(assessedIdentity4, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae5 = createAssessmentEntry(assessedIdentity5, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae8 = createAssessmentEntry(null, entry1, random(), AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae9 = createAssessmentEntry(assessedIdentity9, entry2, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae10 = createAssessmentEntry(assessedIdentity10, entry2, subIdent, AssessmentObligation.optional, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae11 = createAssessmentEntry(assessedIdentity11, entry2, subIdent, AssessmentObligation.excluded, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae12 = createAssessmentEntry(assessedIdentity12, entry2, subIdent, null, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae13 = createAssessmentEntry(assessedIdentity13, entry2, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae14 = createAssessmentEntry(assessedIdentity14, entry3, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae15 = createAssessmentEntry(assessedIdentity15, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.notStarted, Boolean.FALSE);
		AssessmentEntry ae16 = createAssessmentEntry(assessedIdentity16, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.TRUE);
		AssessmentEntry ae17 = createAssessmentEntry(assessedIdentity17, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, null);
		AssessmentEntry ae18 = createAssessmentEntry(assessedIdentity18, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae19 = createAssessmentEntry(assessedIdentity19, entry2, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae21 = createAssessmentEntry(assessedIdentity21, entryTrashed, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae22 = createAssessmentEntry(assessedIdentity22, entryTrashed, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setStatus(AssessmentEntryStatus.inReview);
		params.setUserVisibility(Boolean.FALSE);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("assert that is available")
				.contains(
					ae1.getKey(),
					ae2.getKey(),
					ae4.getKey(),
					ae5.getKey(),
					ae9.getKey(),
					ae10.getKey(),
					ae12.getKey(),
					ae13.getKey(),
					ae14.getKey(),
					ae19.getKey()
				).doesNotContain(
					ae3.getKey(),
					ae8.getKey(),
					ae11.getKey(),
					ae15.getKey(),
					ae16.getKey(),
					ae17.getKey(),
					ae18.getKey(),
					ae21.getKey(),
					ae22.getKey()
				);
		
		assertThat(coachingEntries).filteredOn(CoachingAssessmentEntry::isCoach).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("assert that is coach")
				.contains(
					ae1.getKey(),
					ae2.getKey(),
					ae4.getKey(),
					ae5.getKey(),
					ae14.getKey()
				).doesNotContain(
					ae9.getKey(),
					ae10.getKey(),
					ae12.getKey(),
					ae13.getKey(),
					ae18.getKey(),
					ae19.getKey()
				);
		
		assertThat(coachingEntries).filteredOn(CoachingAssessmentEntry::isOwner).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("assert that is owner")
				.contains(
					ae9.getKey(),
					ae10.getKey(),
					ae12.getKey(),
					ae13.getKey(),
					ae14.getKey(),
					ae19.getKey()
				).doesNotContain(
					ae1.getKey(),
					ae2.getKey(),
					ae4.getKey(),
					ae5.getKey(),
					ae18.getKey()
				);
	}
	
	@Test
	public void getCoachingEntries_filter_scoreNull() {
		//course
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		waitMessageAreConsumed();// Wait sync course element ready
		String subIdent = random();
		createCourseElement(entry, subIdent);
		
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);

		repositoryEntryRelationDao.addRole(assessedIdentity1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity1, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae1.setScore(new BigDecimal(1));
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity2, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae2.setScore(new BigDecimal(2));
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity3, entry, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae3.setScore(null);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		dbInstance.commitAndCloseSession();
		waitMessageAreConsumed();
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setScoreNull(Boolean.TRUE);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae3.getKey());
		
		params.setScoreNull(Boolean.FALSE);
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getCoachingEntries_filter_gradeNull() {
		//course
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		createCourseElement(entry1, subIdent);
		
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);

		repositoryEntryRelationDao.addRole(assessedIdentity1, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity2, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(assessedIdentity3, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity1, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae1.setGrade(random());
		ae1 = assessmentEntryDao.updateAssessmentEntry(ae1);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity2, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae2.setGrade(random());
		ae2 = assessmentEntryDao.updateAssessmentEntry(ae2);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity3, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		ae3.setGrade(null);
		ae3 = assessmentEntryDao.updateAssessmentEntry(ae3);
		dbInstance.commitAndCloseSession();
		
		log.info("ae1: {}-{} ae2: {}-{} ae3: {}-{}", ae1.getKey(), ae1.getGrade(), ae2.getKey(), ae2.getGrade(), ae3.getKey(), ae3.getGrade());
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setGradeNull(Boolean.TRUE);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae3.getKey());
		
		params.setGradeNull(Boolean.FALSE);
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getCoachingEntries_filter_configScoreModes() {
		//course
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity, entry1, GroupRoles.participant.name());
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		
		AssessmentConfigMock assessmentConfig = createAssessmentConfigMock();
		assessmentConfig.setScoreMode(Mode.setByNode);
		String subIdent1 = random();
		createCourseElement(entry1, subIdent1, assessmentConfig);
		assessmentConfig.setScoreMode(Mode.evaluated);
		String subIdent2 = random();
		createCourseElement(entry1, subIdent2, assessmentConfig);
		assessmentConfig.setScoreMode(Mode.none);
		String subIdent3 = random();
		createCourseElement(entry1, subIdent3, assessmentConfig);
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity, entry1, subIdent1, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity, entry1, subIdent2, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity, entry1, subIdent3, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setConfigScoreModes(List.of(Mode.none));
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae3.getKey());

		params.setConfigScoreModes(List.of(Mode.setByNode, Mode.evaluated));
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getCoachingEntries_filter_configGrade() {
		//course
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity, entry1, GroupRoles.participant.name());
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		
		AssessmentConfigMock assessmentConfig = createAssessmentConfigMock();
		assessmentConfig.setGrade(true);
		String subIdent1 = random();
		createCourseElement(entry1, subIdent1, assessmentConfig);
		String subIdent2 = random();
		createCourseElement(entry1, subIdent2, assessmentConfig);
		assessmentConfig.setGrade(false);
		String subIdent3 = random();
		createCourseElement(entry1, subIdent3, assessmentConfig);
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity, entry1, subIdent1, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity, entry1, subIdent2, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity, entry1, subIdent3, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setConfigHasGrade(Boolean.FALSE);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae3.getKey());
		
		params.setConfigHasGrade(Boolean.TRUE);
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getCoachingEntries_filter_configAutoGrade() {
		//course
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(assessedIdentity, entry1, GroupRoles.participant.name());
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		
		AssessmentConfigMock assessmentConfig = createAssessmentConfigMock();
		assessmentConfig.setAutoGrade(true);
		String subIdent1 = random();
		createCourseElement(entry1, subIdent1, assessmentConfig);
		String subIdent2 = random();
		createCourseElement(entry1, subIdent2, assessmentConfig);
		assessmentConfig.setAutoGrade(false);
		String subIdent3 = random();
		createCourseElement(entry1, subIdent3, assessmentConfig);
		
		// some datas
		AssessmentEntry ae1 = createAssessmentEntry(assessedIdentity, entry1, subIdent1, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae2 = createAssessmentEntry(assessedIdentity, entry1, subIdent2, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae3 = createAssessmentEntry(assessedIdentity, entry1, subIdent3, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setConfigIsAutoGrade(Boolean.FALSE);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae3.getKey());
		
		params.setConfigIsAutoGrade(Boolean.TRUE);
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey).containsExactlyInAnyOrder(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getCoachingEntries_filter_userVisibilitySetable() {
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		setRootCourseElementEntry(entry1, STCourseNode.CONFIG_COACH_USER_VISIBILITY, true);
		
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		setRootCourseElementEntry(entry2, STCourseNode.CONFIG_COACH_USER_VISIBILITY, false);
		
		String subIdent = random();
		createCourseElement(entry1, subIdent);
		createCourseElement(entry2, subIdent);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(owner, entry1, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(owner, entry2, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach, entry2, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, entry2, GroupRoles.participant.name());
		
		AssessmentEntry ae1 = createAssessmentEntry(participant, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae2 = createAssessmentEntry(participant, entry2, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		log.info("ae1: {} ae2: {}", ae1.getKey(), ae2.getKey());
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setUserVisibilitySettable(true);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("coach can set user visibility")
				.contains(ae1.getKey())
				.doesNotContain(ae2.getKey());
		
		params.setCoach(owner);
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("owner can set user visibility")
				.contains(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getCoachingEntries_filter_gradeApplicable() {
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		setRootCourseElementEntry(entry1, STCourseNode.CONFIG_COACH_GRADE_APPLY, true);
		
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		setRootCourseElementEntry(entry2, STCourseNode.CONFIG_COACH_GRADE_APPLY, false);
		
		String subIdent = random();
		createCourseElement(entry1, subIdent);
		createCourseElement(entry2, subIdent);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		repositoryEntryRelationDao.addRole(owner, entry1, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach, entry1, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, entry1, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(owner, entry2, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach, entry2, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant, entry2, GroupRoles.participant.name());
		
		AssessmentEntry ae1 = createAssessmentEntry(participant, entry1, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		AssessmentEntry ae2 = createAssessmentEntry(participant, entry2, subIdent, AssessmentObligation.mandatory, AssessmentEntryStatus.inReview, Boolean.FALSE);
		dbInstance.commitAndCloseSession();
		
		CoachingAssessmentSearchParams params = new CoachingAssessmentSearchParams();
		params.setCoach(coach);
		params.setGradeApplicable(true);
		List<CoachingAssessmentEntry> coachingEntries = assessmentToolManager.getCoachingEntries(params);
		
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("coach can apply grade")
				.contains(ae1.getKey())
				.doesNotContain(ae2.getKey());
		
		params.setCoach(owner);
		coachingEntries = assessmentToolManager.getCoachingEntries(params);
		assertThat(coachingEntries).extracting(CoachingAssessmentEntry::getAssessmentEntryKey)
				.as("owner can aply grade")
				.contains(ae1.getKey(), ae2.getKey());
	}
	
	@Test
	public void getFakeParticipants() {
		Identity admin = JunitTestHelper.createAndPersistRndAdmin("ast-admin-9").getIdentity();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(admin, defaultUnitTestOrganisation);
		String subIdent = random();
		
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity formerParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coachWithoutAE = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity coachAndParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity ownerInOtherRE = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity ownerAndParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		Identity masterCoachAndParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser(random(), defaultUnitTestOrganisation, null);
		
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coachWithoutAE, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coachAndParticipant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coachAndParticipant, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(owner, entry, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(ownerInOtherRE, entryOther, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(ownerAndParticipant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(ownerAndParticipant, entry, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(masterCoach, entry, CurriculumRoles.mastercoach.name());
		repositoryEntryRelationDao.addRole(masterCoachAndParticipant, entry, CurriculumRoles.participant.name());
		repositoryEntryRelationDao.addRole(masterCoachAndParticipant, entry, CurriculumRoles.mastercoach.name());
		
		assessmentEntryDao.createAssessmentEntry(participant, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(formerParticipant, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(coach, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(coachAndParticipant, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(owner, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(ownerInOtherRE, null, entryOther, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(ownerAndParticipant, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(masterCoach, null, entry, subIdent, null, null);
		assessmentEntryDao.createAssessmentEntry(masterCoachAndParticipant, null, entry, subIdent, null, null);
		dbInstance.commitAndCloseSession();
		
		// Admin
		Set<IdentityRef> fakeParticipants = assessmentToolManager.getFakeParticipants(entry, admin, true, true);
		assertThat(fakeParticipants).extracting(IdentityRef::getKey)
				.containsExactlyInAnyOrder(
						coach.getKey(),
						owner.getKey(),
						masterCoach.getKey())
				.doesNotContain(
						participant.getKey(),
						coachWithoutAE.getKey(),
						coachAndParticipant.getKey(),
						ownerInOtherRE.getKey(),
						ownerAndParticipant.getKey(),
						masterCoachAndParticipant.getKey()
						);
		
		// Coach with assessment entry
		fakeParticipants = assessmentToolManager.getFakeParticipants(entry, coach, false, true);
		assertThat(fakeParticipants).extracting(IdentityRef::getKey)
				.containsExactlyInAnyOrder(coach.getKey());
		
		// Coach without assessment entry
		fakeParticipants = assessmentToolManager.getFakeParticipants(entry, coachWithoutAE, false, true);
		assertThat(fakeParticipants).isEmpty();
	}

	private AssessmentEntry createAssessmentEntry(Identity identity, RepositoryEntry entry, String subIdent,
			AssessmentObligation obligation, AssessmentEntryStatus status, Boolean userVisibility) {
		AssessmentEntry ae = assessmentEntryDao.createAssessmentEntry(identity, null, entry, subIdent, null, null);
		if (obligation != null) {
			ae.setObligation(ObligationOverridable.of(obligation));
		}
		ae.setAssessmentStatus(status);
		ae.setUserVisibility(userVisibility);
		return assessmentEntryDao.updateAssessmentEntry(ae);
	}

	private CourseElement createCourseElement(RepositoryEntry entry, String subIdent) {
		return createCourseElement(entry, subIdent, createAssessmentConfigMock());
	}
	
	private void setRootCourseElementEntry(RepositoryEntry entry, String configKey, Boolean value) {
		ICourse course2 = CourseFactory.openCourseEditSession(entry.getOlatResource().getResourceableId());
		course2.getRunStructure().getRootNode().getModuleConfiguration().setBooleanEntry(configKey, value);
		CourseFactory.saveCourse(course2.getResourceableId());
		CourseFactory.closeCourseEditSession(course2.getResourceableId(), true);
	}
	
	private CourseElement createCourseElement(RepositoryEntry entry, String subIdent, AssessmentConfig assessmentConfig) {
		CourseNode courseNode = new SPCourseNode();
		courseNode.setIdent(subIdent);
		courseNode.setShortTitle(miniRandom());
		courseNode.setLongTitle(random());
		return courseElementDao.create(entry, courseNode, assessmentConfig);
	}
	
	private AssessmentConfigMock createAssessmentConfigMock() {
		AssessmentConfigMock assessmentConfig = new AssessmentConfigMock();
		assessmentConfig.setAssessable(true);
		assessmentConfig.setScoreMode(Mode.setByNode);
		assessmentConfig.setGrade(false);
		assessmentConfig.setAutoGrade(false);
		assessmentConfig.setPassedMode(Mode.setByNode);
		assessmentConfig.setCutValue(Float.valueOf(2.4f));
		return assessmentConfig;
	}

}
