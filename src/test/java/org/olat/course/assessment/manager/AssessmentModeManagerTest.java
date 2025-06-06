/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.assessment.ui.mode.AssessmentModeHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentModeManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaMgr;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-course-infos-unit-test", "Org-course-infos-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createAssessmentMode() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		
		mode.setName("Assessment in sight");
		mode.setDescription("Assessment description");
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, 2);
		Date begin = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 2);
		Date end = cal.getTime();
		mode.setBegin(begin);
		mode.setEnd(end);
		mode.setLeadTime(15);
		
		mode.setTargetAudience(Target.course);
		
		mode.setRestrictAccessElements(true);
		mode.setElementList("173819739,239472389");
		
		mode.setRestrictAccessIps(true);
		mode.setIpList("192.168.1.123");
		
		mode.setSafeExamBrowser(true);
		mode.setSafeExamBrowserKey("785rhqg47368ahfahl");
		mode.setSafeExamBrowserHint("Use the SafeExamBrowser");
		
		mode.setApplySettingsForCoach(true);
		
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		Assert.assertNotNull(savedMode.getKey());
		Assert.assertNotNull(savedMode.getCreationDate());
		Assert.assertNotNull(savedMode.getLastModified());

		//reload and check
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(savedMode.getKey());
		Assert.assertNotNull(reloadedMode);
		Assert.assertEquals(savedMode.getKey(), reloadedMode.getKey());
		Assert.assertNotNull(reloadedMode.getCreationDate());
		Assert.assertNotNull(reloadedMode.getLastModified());
		Assert.assertEquals(savedMode, reloadedMode);
		
		Assert.assertEquals("Assessment in sight", reloadedMode.getName());
		Assert.assertEquals("Assessment description", reloadedMode.getDescription());
		
		Assert.assertEquals(begin, reloadedMode.getBegin());
		Assert.assertEquals(end, reloadedMode.getEnd());
		Assert.assertEquals(15, reloadedMode.getLeadTime());
		
		Assert.assertEquals(Target.course, reloadedMode.getTargetAudience());
		
		Assert.assertTrue(reloadedMode.isRestrictAccessElements());
		Assert.assertEquals("173819739,239472389", reloadedMode.getElementList());
		
		Assert.assertTrue(reloadedMode.isRestrictAccessIps());
		Assert.assertEquals("192.168.1.123", reloadedMode.getIpList());
		
		Assert.assertTrue(reloadedMode.isApplySettingsForCoach());
		
		Assert.assertTrue(reloadedMode.isSafeExamBrowser());
		Assert.assertEquals("785rhqg47368ahfahl", reloadedMode.getSafeExamBrowserKey());
		Assert.assertEquals("Use the SafeExamBrowser", reloadedMode.getSafeExamBrowserHint());
	}
	
	@Test
	public void loadAssessmentModes() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		List<AssessmentMode> assessmentModes = assessmentModeMgr.getAssessmentModeFor(entry);
		Assert.assertNotNull(assessmentModes);
		Assert.assertEquals(1, assessmentModes.size());
		Assert.assertEquals(savedMode, assessmentModes.get(0));
	}
	
	@Test
	public void createAssessmentModeToGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, null);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		AssessmentMode savedMode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		Assert.assertEquals(mode, reloadedMode);
		Assert.assertEquals(savedMode, reloadedMode);
		Assert.assertNotNull(reloadedMode.getGroups());
		Assert.assertEquals(1, reloadedMode.getGroups().size());
		Assert.assertEquals(modeToGroup, reloadedMode.getGroups().iterator().next());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAssessmentModeToArea() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, null);
		BGArea area = areaMgr.createAndPersistBGArea("little area", "My little secret area", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		AssessmentMode savedMode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode reloadedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		Assert.assertEquals(mode, reloadedMode);
		Assert.assertEquals(savedMode, reloadedMode);
		Assert.assertNotNull(reloadedMode.getAreas());
		Assert.assertEquals(1, reloadedMode.getAreas().size());
		Assert.assertEquals(modeToArea, reloadedMode.getAreas().iterator().next());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAssessmentMode_lectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(lectureBlock, 5, 10, "192.168.1.203");
		mode.setSafeExamBrowser(true);
		mode.setSafeExamBrowserKey("very-complicated-key");
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		AssessmentMode lecturedMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
		Assert.assertNotNull(lecturedMode);
		Assert.assertEquals(mode, lecturedMode);
		Assert.assertEquals(lectureBlock, lecturedMode.getLectureBlock());
		Assert.assertEquals(5, lecturedMode.getLeadTime());
		Assert.assertEquals(10, lecturedMode.getFollowupTime());
		Assert.assertEquals("192.168.1.203", lecturedMode.getIpList());
		Assert.assertEquals("very-complicated-key", lecturedMode.getSafeExamBrowserKey());
	}
	
	@Test
	public void deleteAssessmentMode() {
		//prepare the setup
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as_mode_1", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, null);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		AssessmentMode savedMode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		
		BusinessGroup businessGroupForArea = businessGroupService.createBusinessGroup(author, "as_mode_1", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, null);
		BGArea area = areaMgr.createAndPersistBGArea("little area", "My little secret area", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroupForArea, area);
		dbInstance.commitAndCloseSession();
		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(savedMode, area);
		savedMode.getAreas().add(modeToArea);
		savedMode = assessmentModeMgr.merge(savedMode, true, author);
		dbInstance.commitAndCloseSession();
		
		//delete
		assessmentModeMgr.delete(savedMode);
		dbInstance.commit();
		//check
		AssessmentMode deletedMode = assessmentModeMgr.getAssessmentModeById(mode.getKey());
		Assert.assertNull(deletedMode);
	}
	
	@Test
	public void loadAssessmentMode_repositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(entry);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void loadAssessmentMode_lectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		((AssessmentModeImpl)mode).setLectureBlock(lectureBlock);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		AssessmentMode lecturedMode = assessmentModeMgr.getAssessmentMode(lectureBlock);
		Assert.assertNotNull(lecturedMode);
		Assert.assertEquals(mode, lecturedMode);
		Assert.assertEquals(lectureBlock, lecturedMode.getLectureBlock());
	}
	
	@Test
	public void loadCurrentAssessmentModes() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModes(now);
		Assert.assertNotNull(currentModes);
		Assert.assertFalse(currentModes.isEmpty());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	/**
	 * Manual without lead time -> not in the current list
	 */
	@Test
	public void loadCurrentAssessmentModes_manualNow() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModes(now);
		Assert.assertNotNull(currentModes);
		Assert.assertFalse(currentModes.contains(mode));
	}
	
	/**
	 * Manual with lead time -> in the current list
	 */
	@Test
	public void loadCurrentAssessmentModes_manualNowLeadingTime() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode.setLeadTime(120);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModes(now);
		Assert.assertNotNull(currentModes);
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void getCurrentAssessmentMode() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getCurrentAssessmentMode(entry, now);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	/**
	 * Manual without lead time -> not in the current list
	 */
	@Test
	public void getCurrentAssessmentMode_manualNow() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getCurrentAssessmentMode(entry, now);
		Assert.assertNotNull(currentModes);
		Assert.assertTrue(currentModes.isEmpty());
	}
	
	/**
	 * Manual with lead time -> in the current list
	 */
	@Test
	public void getCurrentAssessmentMode_manualNowLeadingTime() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//manual now
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode.setLeadTime(120);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		Date now = new Date();
		List<AssessmentMode> currentModes = assessmentModeMgr.getCurrentAssessmentMode(entry, now);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
	}
	
	@Test
	public void isNodeInUse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		CourseNode node = new IQTESTCourseNode();
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setElementList(node.getIdent());
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		boolean inUse = assessmentModeMgr.isNodeInUse(entry, node);
		Assert.assertTrue(inUse);
		
		// other node
		CourseNode otherNode = new IQTESTCourseNode();
		boolean notInUse = assessmentModeMgr.isNodeInUse(entry, otherNode);
		Assert.assertFalse(notInUse);
	}
	
	@Test
	public void isInAssessmentMode() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryReference = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("assessed-1");
		repositoryEntryRelationDao.addRole(assessedIdentity, entry, GroupRoles.participant.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setStatus(Status.assessment);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check
		boolean entryNow = assessmentModeMgr.isInAssessmentMode(entry, null, assessedIdentity);
		Assert.assertTrue(entryNow);
		
		//no assessment for this course
		boolean entryReferenceNow = assessmentModeMgr.isInAssessmentMode(entryReference, null, assessedIdentity);
		Assert.assertFalse(entryReferenceNow);

		// status changed
		mode.setStatus(Status.followup);
		mode = dbInstance.getCurrentEntityManager().merge(mode);
		dbInstance.commitAndCloseSession();
		boolean entryFollowUp = assessmentModeMgr.isInAssessmentMode(entry, null, assessedIdentity);
		Assert.assertFalse(entryFollowUp);
		
		mode.setStatus(Status.end);
		mode.setEndStatus(EndStatus.all);
		mode = dbInstance.getCurrentEntityManager().merge(mode);
		dbInstance.commitAndCloseSession();
		boolean entryEnded = assessmentModeMgr.isInAssessmentMode(entry, null, assessedIdentity);
		Assert.assertFalse(entryEnded);
	}
	
	/**
	 * Manual without leading time -> not in assessment mode
	 */
	@Test
	public void isInAssessmentMode_manual() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("assessed-2");
		repositoryEntryRelationDao.addRole(assessedIdentity, entry, GroupRoles.participant.name());
		
		// leading time
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setManualBeginEnd(true);
		mode.setStatus(Status.leadtime);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		// check
		boolean entryNow = assessmentModeMgr.isInAssessmentMode(entry, null, assessedIdentity);
		Assert.assertFalse(entryNow);
		
		// changed status
		mode.setStatus(Status.assessment);
		mode = dbInstance.getCurrentEntityManager().merge(mode);
		dbInstance.commitAndCloseSession();
		boolean entryAssessment = assessmentModeMgr.isInAssessmentMode(entry, null, assessedIdentity);
		Assert.assertTrue(entryAssessment);
		
		// changed status
		mode.setStatus(Status.end);
		mode.setEndStatus(EndStatus.all);
		mode = dbInstance.getCurrentEntityManager().merge(mode);
		dbInstance.commitAndCloseSession();
		boolean entryEnded = assessmentModeMgr.isInAssessmentMode(entry, null, assessedIdentity);
		Assert.assertFalse(entryEnded);
	}
	
	@Test
	public void isInAssessmentModeBusinessGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assessed-auth-1", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assessed-grp-1", defaultUnitTestOrganisation, null);
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assessed-grp-2", defaultUnitTestOrganisation, null);
		
		BusinessGroup businessGroup1 = businessGroupService.createBusinessGroup(null, "as-mode-10", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, courseEntry);
		BusinessGroup businessGroup2 = businessGroupService.createBusinessGroup(null, "as-mode-11", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, courseEntry);
		
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup2, GroupRoles.participant.name());
		
		AssessmentMode mode1pos1 = createPersistManualAssessmentMode(courseEntry, Target.groups, "position-1");
		AssessmentMode mode2pos1 = createPersistManualAssessmentMode(courseEntry, Target.groups, "position-1");
		AssessmentMode mode1pos2 = createPersistManualAssessmentMode(courseEntry, Target.groups, "position-2");
		AssessmentMode mode2pos2 = createPersistManualAssessmentMode(courseEntry, Target.groups, "position-2");
		
		AssessmentModeHelper.updateBusinessGroupRelations(List.of(businessGroup1.getKey()),
				mode1pos1, AssessmentMode.Target.groups, assessmentModeMgr, businessGroupService);
		AssessmentModeHelper.updateBusinessGroupRelations(List.of(businessGroup1.getKey()),
				mode1pos2, AssessmentMode.Target.groups, assessmentModeMgr, businessGroupService);
		AssessmentModeHelper.updateBusinessGroupRelations(List.of(businessGroup2.getKey()),
				mode2pos1, AssessmentMode.Target.groups, assessmentModeMgr, businessGroupService);
		AssessmentModeHelper.updateBusinessGroupRelations(List.of(businessGroup2.getKey()),
				mode2pos2, AssessmentMode.Target.groups, assessmentModeMgr, businessGroupService);
		dbInstance.commitAndCloseSession();
		
		boolean inAssessment11 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-1", participant1);
		Assert.assertFalse(inAssessment11);
		boolean inAssessment21 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-1", participant2);
		Assert.assertFalse(inAssessment21);
		boolean inAssessment12 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-2", participant1);
		Assert.assertFalse(inAssessment12);
		boolean inAssessment22 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-2", participant2);
		Assert.assertFalse(inAssessment22);
		
		mode1pos1 = assessmentModeCoordinationService.startAssessment(mode1pos1, null);
		mode2pos1 = assessmentModeCoordinationService.startAssessment(mode2pos1, null);
		dbInstance.commitAndCloseSession();
		sleep(1000);
		
		boolean inAssessment1pos1run1 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-1", participant1);
		Assert.assertTrue(inAssessment1pos1run1);
		boolean inAssessment2pos1run1 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-1", participant2);
		Assert.assertTrue(inAssessment2pos1run1);
		boolean inAssessment1pos2run1 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-2", participant1);
		Assert.assertFalse(inAssessment1pos2run1);
		boolean inAssessment2pos2run1 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-2", participant2);
		Assert.assertFalse(inAssessment2pos2run1);
		
		// Without sub-identifier is allowed
		boolean inAssessment2run1 = assessmentModeMgr.isInAssessmentMode(courseEntry, null, participant2);
		Assert.assertTrue(inAssessment2run1);
		
		mode1pos1 = assessmentModeCoordinationService.stopAssessment(mode1pos1, false, true, author);
		mode2pos1 = assessmentModeCoordinationService.stopAssessment(mode2pos1, false, true, author);
		mode1pos2 = assessmentModeCoordinationService.startAssessment(mode1pos2, author);
		mode2pos2 = assessmentModeCoordinationService.startAssessment(mode2pos2, author);
		dbInstance.commitAndCloseSession();
		sleep(1000);
		
		boolean inAssessment1pos1run2 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-1", participant1);
		Assert.assertFalse(inAssessment1pos1run2);
		boolean inAssessment2pos1run2 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-1", participant2);
		Assert.assertFalse(inAssessment2pos1run2);
		boolean inAssessment1pos2run2 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-2", participant1);
		Assert.assertTrue(inAssessment1pos2run2);
		boolean inAssessment2pos2run2 = assessmentModeMgr.isInAssessmentMode(courseEntry, "position-2", participant2);
		Assert.assertTrue(inAssessment2pos2run2);
	}
	
	/**
	 * Check an assessment linked to a group with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInBusinessGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-2", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-3", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-3", defaultUnitTestOrganisation, null);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as-mode-2", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	
	/**
	 * Check an assessment linked to a group with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInBusinessGroup_coach() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6", defaultUnitTestOrganisation, null);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-3", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCourse() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6");
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCourse_coach() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-7");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-8");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-9");
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	/**
	 * Check an assessment linked to an area with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInArea() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-12", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-13", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-14", defaultUnitTestOrganisation, null);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(author, "as-mode-3", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		BGArea area = areaMgr.createAndPersistBGArea("area for people", "", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	/**
	 * Check an assessment linked to an area with one participant
	 * 
	 */
	@Test
	public void loadAssessmentMode_identityInArea_coach() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-12", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-13", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-14", defaultUnitTestOrganisation, null);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-3", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach, businessGroup, GroupRoles.coach.name());
		
		BGArea area = areaMgr.createAndPersistBGArea("area for people", "", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCurriculum() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-30", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-31", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-32", defaultUnitTestOrganisation, null);
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-1", "Curriculum for assessment", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for assessment", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, author);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, author);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertTrue(currentCoachModes.isEmpty());
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void loadAssessmentMode_identityInCurriculumCoach() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-35", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-36", defaultUnitTestOrganisation, null);
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-37", defaultUnitTestOrganisation, null);
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-2", "Curriculum for assessment", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for assessment", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, author);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, author);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.curriculumEls);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//check coach
		List<AssessmentMode> currentCoachModes = assessmentModeMgr.getAssessmentModeFor(coach);
		Assert.assertNotNull(currentCoachModes);
		Assert.assertEquals(1, currentCoachModes.size());
		Assert.assertTrue(currentCoachModes.contains(mode));
		
		//check author
		List<AssessmentMode> currentAuthorModes = assessmentModeMgr.getAssessmentModeFor(author);
		Assert.assertNotNull(currentAuthorModes);
		Assert.assertTrue(currentAuthorModes.isEmpty());
	}
	
	@Test
	public void getAssessedIdentities_course_groups() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-15", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-16", defaultUnitTestOrganisation, null);
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-17", defaultUnitTestOrganisation, null);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-4", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach1, businessGroup, GroupRoles.coach.name());
		
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-18");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-19");
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);

		AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup);
		mode.getGroups().add(modeToGroup);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(4, assessedIdentityKeys.size());
		Assert.assertFalse(assessedIdentityKeys.contains(author.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach2.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant2.getKey()));
	}
	
	@Test
	public void getPlannedAssessmentMode() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
	
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setBegin(DateUtils.addDays(mode.getBegin(), -4));
		mode.setEnd(DateUtils.addDays(mode.getEnd(), -4));
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		Date from = DateUtils.addDays(new Date(), -5);
		Date to = DateUtils.addDays(new Date(), -2);
		List<AssessmentMode> plannedModes = assessmentModeMgr.getPlannedAssessmentMode(entry, from, to);
		assertThat(plannedModes)
			.isNotNull()
			.containsAnyOf(mode);
	}
	
	@Test
	public void findAssessmentModeSearchParams() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
	
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setBegin(DateUtils.addDays(mode.getBegin(), -25));
		((AssessmentModeImpl)mode).setBeginWithLeadTime(mode.getBegin());
		mode.setEnd(DateUtils.addDays(mode.getEnd(), -23));
		((AssessmentModeImpl)mode).setEndWithFollowupTime(mode.getEnd());
		mode.setName("Mode");
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		SearchAssessmentModeParams params = new SearchAssessmentModeParams();
		params.setDateFrom(DateUtils.addDays(new Date(), -26));
		params.setDateTo(DateUtils.addDays(new Date(), -20));
		params.setIdAndRefs(entry.getKey().toString());
		params.setName("Mode");
		params.setAllowedModeStatus(List.of(Status.none.name()));

		List<AssessmentMode> plannedModes = assessmentModeMgr.findAssessmentMode(params);
		assertThat(plannedModes)
			.isNotNull()
			.containsAnyOf(mode);

		params.setAllowedModeStatus(List.of(Status.end.name()));
		List<AssessmentMode> excludeEndMode = assessmentModeMgr.findAssessmentMode(params);
		assertThat(excludeEndMode).hasSize(plannedModes.size() - 1);
	}
	
	/**
	 * Manual without lead time -> not in the current list
	 */
	@Test
	public void findRunningAssessmentModeSearchParams() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setBegin(DateUtils.addDays(mode.getBegin(), -25));
		((AssessmentModeImpl)mode).setBeginWithLeadTime(mode.getBegin());
		mode.setEnd(DateUtils.addDays(mode.getEnd(), -23));
		((AssessmentModeImpl)mode).setEndWithFollowupTime(mode.getEnd());
		mode.setName("Running mode");
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(true);
		mode.setStatus(Status.assessment);
		mode.setEndStatus(null);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		
		//check
		SearchAssessmentModeParams params = new SearchAssessmentModeParams();
		params.setRunning(Boolean.TRUE);
	
		List<AssessmentMode> runningModes = assessmentModeMgr.findAssessmentMode(params);
		assertThat(runningModes)
			.isNotNull()
			.containsAnyOf(mode);
		
		// update 
		mode.setStatus(Status.end);
		mode.setEndStatus(EndStatus.all);
		mode = assessmentModeMgr.merge(mode, false, null);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentMode> rModes = assessmentModeMgr.findAssessmentMode(params);
		assertThat(rModes)
			.isNotNull()
			.doesNotContainSequence(mode);
	}
	
	@Test
	public void getAssessedIdentities_course_areas() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-20", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-21", defaultUnitTestOrganisation, null);
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-22", defaultUnitTestOrganisation, null);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "as-mode-5", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(coach1, businessGroup, GroupRoles.coach.name());
		
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-23");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-24");
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);
		
		BGArea area = areaMgr.createAndPersistBGArea("area for people", "", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroup, area);

		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		mode = assessmentModeMgr.merge(mode, true, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(4, assessedIdentityKeys.size());
		Assert.assertFalse(assessedIdentityKeys.contains(author.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach2.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant2.getKey()));
	}
	
	@Test
	public void getAssessedIdentities_course_curriculum() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-37", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-38", defaultUnitTestOrganisation, null);
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-39", defaultUnitTestOrganisation, null);
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-3", "Curriculum for assessment", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for assessment", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant1, CurriculumRoles.participant, author);
		curriculumService.addMember(element, coach1, CurriculumRoles.coach, author);
		dbInstance.commitAndCloseSession();
		
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-23");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-24");
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.courseAndGroups);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);

		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(4, assessedIdentityKeys.size());
		Assert.assertFalse(assessedIdentityKeys.contains(author.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(coach2.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant2.getKey()));
	}
	
	@Test
	public void getAssessedIdentities_course_curriculumElements() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-39", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-40", defaultUnitTestOrganisation, null);
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-41", defaultUnitTestOrganisation, null);
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-42", defaultUnitTestOrganisation, null);
		
		Curriculum curriculum = curriculumService.createCurriculum("cur-as-mode-4", "Curriculum for assessment", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel-1", "Element for assessment",  CurriculumElementStatus.active,
				null, null, null, null, CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-rel-2", "Element for assessment",  CurriculumElementStatus.active,
				null, null, null, null, CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element1, entry, false);
		curriculumService.addRepositoryEntry(element2, entry, false);
		curriculumService.addMember(element1, participant1, CurriculumRoles.participant, author);
		curriculumService.addMember(element2, participant2, CurriculumRoles.participant, author);
		curriculumService.addMember(element1, coach1, CurriculumRoles.coach, author);
		curriculumService.addMember(element2, coach1, CurriculumRoles.coach, author);
		dbInstance.commitAndCloseSession();
		
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-23");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-24");
		repositoryEntryRelationDao.addRole(participant3, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(coach2, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(author, entry, GroupRoles.owner.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.curriculumEls);
		mode.setApplySettingsForCoach(true);
		mode = assessmentModeMgr.persist(mode);

		AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(mode, element1);
		mode.getCurriculumElements().add(modeToElement);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		Set<Long> assessedIdentityKeys = assessmentModeMgr.getAssessedIdentityKeys(mode);
		Assert.assertNotNull(assessedIdentityKeys);
		Assert.assertEquals(2, assessedIdentityKeys.size());
		Assert.assertTrue(assessedIdentityKeys.contains(coach1.getKey()));
		Assert.assertTrue(assessedIdentityKeys.contains(participant1.getKey()));
	}
	
	@Test
	public void removeBusinessGroupFromRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-4", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-5", defaultUnitTestOrganisation, null);
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-6", defaultUnitTestOrganisation, null);
		BusinessGroup businessGroup1 = businessGroupService.createBusinessGroup(author, "as-mode-7", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		BusinessGroup businessGroup2 = businessGroupService.createBusinessGroup(author, "as-mode-8", "desc", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup2, GroupRoles.participant.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.groups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup1 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup1);
		AssessmentModeToGroup modeToGroup2 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup2);
		mode.getGroups().add(modeToGroup1);
		mode.getGroups().add(modeToGroup2);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant 1
		List<AssessmentMode> currentModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(currentModes1);
		Assert.assertEquals(1, currentModes1.size());
		Assert.assertTrue(currentModes1.contains(mode));
		//check participant 2
		List<AssessmentMode> currentModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(currentModes2);
		Assert.assertEquals(1, currentModes2.size());
		Assert.assertTrue(currentModes2.contains(mode));
		
		//remove business group 1
		businessGroupRelationDao.deleteRelation(businessGroup1, entry);
		dbInstance.commitAndCloseSession();
		
		//check participant 1
		List<AssessmentMode> afterDeleteModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(afterDeleteModes1);
		Assert.assertEquals(0, afterDeleteModes1.size());
		//check participant 2
		List<AssessmentMode> afterDeleteModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(afterDeleteModes2);
		Assert.assertEquals(1, afterDeleteModes2.size());
		Assert.assertTrue(afterDeleteModes2.contains(mode));	
	}
	
	@Test
	public void deleteBusinessGroupFromRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-9", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-10", defaultUnitTestOrganisation, null);
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-11", defaultUnitTestOrganisation, null);
		BusinessGroup businessGroup1 = businessGroupService.createBusinessGroup(author, "as-mode-12", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		BusinessGroup businessGroup2 = businessGroupService.createBusinessGroup(author, "as-mode-13", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		businessGroupRelationDao.addRole(participant1, businessGroup1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(participant2, businessGroup2, GroupRoles.participant.name());
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.groups);
		mode.setApplySettingsForCoach(false);
		mode = assessmentModeMgr.persist(mode);
		
		AssessmentModeToGroup modeToGroup1 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup1);
		AssessmentModeToGroup modeToGroup2 = assessmentModeMgr.createAssessmentModeToGroup(mode, businessGroup2);
		mode.getGroups().add(modeToGroup1);
		mode.getGroups().add(modeToGroup2);
		mode = assessmentModeMgr.merge(mode, true, author);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);
		
		//check participant 1
		List<AssessmentMode> currentModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(currentModes1);
		Assert.assertEquals(1, currentModes1.size());
		Assert.assertTrue(currentModes1.contains(mode));
		//check participant 2
		List<AssessmentMode> currentModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(currentModes2);
		Assert.assertEquals(1, currentModes2.size());
		Assert.assertTrue(currentModes2.contains(mode));
		
		//remove business group 1
		businessGroupLifecycleManager.deleteBusinessGroup(businessGroup2, author, false);
		dbInstance.commitAndCloseSession();

		//check participant 1
		List<AssessmentMode> afterDeleteModes1 = assessmentModeMgr.getAssessmentModeFor(participant1);
		Assert.assertNotNull(afterDeleteModes1);
		Assert.assertEquals(1, afterDeleteModes1.size());
		Assert.assertTrue(afterDeleteModes1.contains(mode));
		//check participant 2
		List<AssessmentMode> afterDeleteModes2 = assessmentModeMgr.getAssessmentModeFor(participant2);
		Assert.assertNotNull(afterDeleteModes2);
		Assert.assertEquals(0, afterDeleteModes2.size());
	}
	
	@Test
	public void deleteAreaFromRepositoryEntry() {
		//prepare the setup
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-14", defaultUnitTestOrganisation, null);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("as-mode-15", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		
		AssessmentMode mode = createMinimalAssessmentmode(entry);
		mode.setTargetAudience(AssessmentMode.Target.groups);
		mode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mode);

		BusinessGroup businessGroupForArea = businessGroupService.createBusinessGroup(author, "as_mode_1", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, null);
		businessGroupRelationDao.addRole(participant, businessGroupForArea, GroupRoles.participant.name());
		BGArea area = areaMgr.createAndPersistBGArea("little area", "My little secret area", entry.getOlatResource());
		areaMgr.addBGToBGArea(businessGroupForArea, area);
		dbInstance.commitAndCloseSession();
		AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(mode, area);
		mode.getAreas().add(modeToArea);
		mode = assessmentModeMgr.merge(mode, true, null);
		dbInstance.commitAndCloseSession();
		
		//check the participant modes
		List<AssessmentMode> currentModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(currentModes);
		Assert.assertEquals(1, currentModes.size());
		Assert.assertTrue(currentModes.contains(mode));
		
		//delete
		areaMgr.deleteBGArea(area);
		dbInstance.commitAndCloseSession();

		//check the participant modes after deleting the area
		List<AssessmentMode> afterDeleteModes = assessmentModeMgr.getAssessmentModeFor(participant);
		Assert.assertNotNull(afterDeleteModes);
		Assert.assertEquals(0, afterDeleteModes.size());
	}

	/**
	 * Create a minimal assessment mode which start one hour before now
	 * and stop two hours after now.
	 * 
	 * @param entry
	 * @return
	 */
	private AssessmentMode createMinimalAssessmentmode(RepositoryEntry entry) {
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(Target.course);
		mode.setManualBeginEnd(false);
		return mode;
	}
	
	private AssessmentMode createPersistManualAssessmentMode(RepositoryEntry entry, Target target, String elementId) {
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, -1);
		mode.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 2);
		mode.setEnd(cal.getTime());
		mode.setTargetAudience(target);
		mode.setRestrictAccessElements(true);
		mode.setElementList(elementId);
		mode.setManualBeginEnd(true);
		return assessmentModeMgr.persist(mode);
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
}
