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
*/

package org.olat.course.assessment.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.PersistenceException;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.model.AssessmentNodesLastModified;
import org.olat.course.assessment.model.UserEfficiencyStatementForCoaching;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.assessment.model.UserEfficiencyStatementStandalone;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.UserManager;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * Description:<br>
 * Methods to update a users efficiency statement and to retrieve such statements
 * from the database.
 * 
 * <P>
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
@Service
public class EfficiencyStatementManager implements UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(EfficiencyStatementManager.class);

	public static final String KEY_ASSESSMENT_NODES = "assessmentNodes";
	public static final String KEY_COURSE_TITLE = "courseTitle";
	public static final String PROPERTY_CATEGORY = "efficiencyStatement";

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();

	static {
		Class<?>[] types = new Class[] {
				EfficiencyStatement.class
			};
		xstream.addPermission(new ExplicitTypePermission(types));
	}
	
	public static EfficiencyStatement fromXML(String xml) {
		return (EfficiencyStatement)xstream.fromXML(xml);
	}
	
	public static String toXML(EfficiencyStatement statement) {
		return xstream.toXML(statement);
	}

	/**
	 * Updates the users efficiency statement for this course environment. The update
	 * will be done synchronized (doInSync).
	 * 
	 * @param userCourseEnv The user course environment
	 */
	public void updateUserEfficiencyStatement(UserCourseEnvironment userCourseEnv) {
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		AssessmentNodesLastModified lastModifications = new AssessmentNodesLastModified();
		RepositoryEntry repoEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.getAssessmentNodeDataList(userCourseEnv, lastModifications, true, true, true);
		doInSyncUpdateUserEfficiencyStatement(identity, userCourseEnv.getCourseEnvironment(), assessmentNodeList, lastModifications,  repoEntry);
	}

	public UserEfficiencyStatement createUserEfficiencyStatement(Date creationDate, Float score, Float weightedScore, String grade,
			String gradeSystemIdent, String performanceClassIdent, Boolean passed, Identity identity, OLATResource resource) {
		UserEfficiencyStatementImpl efficiencyProperty = new UserEfficiencyStatementImpl();
		efficiencyProperty.setVersion(0);
		if(creationDate == null) {
			efficiencyProperty.setCreationDate(new Date());
			efficiencyProperty.setLastModified(efficiencyProperty.getCreationDate());
		} else {
			efficiencyProperty.setCreationDate(creationDate);
			efficiencyProperty.setLastModified(new Date());
		}
		efficiencyProperty.setScore(score);
		efficiencyProperty.setWeightedScore(weightedScore);
		efficiencyProperty.setGrade(grade);
		efficiencyProperty.setGradeSystemIdent(gradeSystemIdent);
		efficiencyProperty.setPerformanceClassIdent(performanceClassIdent);
		efficiencyProperty.setPassed(passed);

		efficiencyProperty.setTotalNodes(0);
		efficiencyProperty.setAttemptedNodes(0);
		efficiencyProperty.setPassedNodes(0);

		efficiencyProperty.setIdentity(identity);
		efficiencyProperty.setResource(resource);
		
		efficiencyProperty.setLastStatement(true);

		ICourse course = CourseFactory.loadCourse(resource.getResourceableId());
		efficiencyProperty.setTitle(course.getCourseEnvironment().getCourseTitle());
		efficiencyProperty.setShortTitle(course.getCourseEnvironment().getRunStructure().getRootNode().getShortTitle());
		efficiencyProperty.setCourseRepoKey(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey());

		dbInstance.getCurrentEntityManager().persist(efficiencyProperty);

		return efficiencyProperty;
	}
	
	public UserEfficiencyStatement createStandAloneUserEfficiencyStatement(Date creationDate, Float score, String grade,
			String gradeSystemIdent, String performanceClassIdent, Boolean passed, Integer totalNodes,
			Integer attemptedNodes, Integer passedNodes, String statementXml, Identity identity, Long resourceKey,
			String courseTitle) {
		UserEfficiencyStatementStandalone efficiencyProperty = new UserEfficiencyStatementStandalone();
		if(creationDate != null) {
			efficiencyProperty.setCreationDate(creationDate);
			efficiencyProperty.setLastModified(new Date());
		} else {
			efficiencyProperty.setCreationDate(new Date());
			efficiencyProperty.setLastModified(efficiencyProperty.getCreationDate());
		}

		efficiencyProperty.setScore(score);
		efficiencyProperty.setGrade(grade);
		efficiencyProperty.setGradeSystemIdent(gradeSystemIdent);
		efficiencyProperty.setPerformanceClassIdent(performanceClassIdent);
		efficiencyProperty.setPassed(passed);

		efficiencyProperty.setTotalNodes(totalNodes == null ? Integer.valueOf(0) : totalNodes);
		efficiencyProperty.setAttemptedNodes(attemptedNodes == null ? Integer.valueOf(0) : attemptedNodes);
		efficiencyProperty.setPassedNodes(passedNodes == null ? Integer.valueOf(0) : passedNodes);
		
		efficiencyProperty.setLastStatement(true);
		efficiencyProperty.setStatementXml(statementXml);

		efficiencyProperty.setIdentity(identity);
		efficiencyProperty.setResourceKey(resourceKey);

		efficiencyProperty.setTitle(courseTitle);
		efficiencyProperty.setShortTitle(courseTitle);
		efficiencyProperty.setCourseRepoKey(null);

		dbInstance.getCurrentEntityManager().persist(efficiencyProperty);

		return efficiencyProperty;
	}
	
	
	
	/**
	 * The method will call the synchronized update of the efficiency statement (doInSync).
	 * 
	 * @param assessedIdentity The identity
	 * @param courseEnv The course environment
	 * @param assessmentNodeList The list of course elements
	 * @param lastModifications Last modifications
	 * @param repoEntry The repository entry of the course
	 */
	public void updateUserEfficiencyStatement(final Identity assessedIdentity, final CourseEnvironment courseEnv,
			final List<AssessmentNodeData> assessmentNodeList, final AssessmentNodesLastModified lastModifications, final RepositoryEntry repoEntry) {
		doInSyncUpdateUserEfficiencyStatement(assessedIdentity, courseEnv, assessmentNodeList, lastModifications, repoEntry);
	}
	
	private void doInSyncUpdateUserEfficiencyStatement(final Identity assessedIdentity, final CourseEnvironment courseEnv,
			final List<AssessmentNodeData> assessmentNodeList, final AssessmentNodesLastModified lastModifications, final RepositoryEntry repoEntry) {
		AssessmentManager am = courseEnv.getAssessmentManager();
		OLATResourceable efficiencyStatementResourceable = am.createOLATResourceableForLocking(assessedIdentity);
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(efficiencyStatementResourceable, () -> {					
			internalUpdateUserEfficiencyStatement(assessedIdentity, courseEnv, assessmentNodeList, lastModifications, repoEntry);
		});
	}

	private void internalUpdateUserEfficiencyStatement(Identity assessedIdentity, final CourseEnvironment courseEnv,
			List<AssessmentNodeData> assessmentNodeList, AssessmentNodesLastModified lastModifications, final RepositoryEntry repoEntry) {
		
		EfficiencyStatement efficiencyStatement = createEfficiencyStatement(assessedIdentity, courseEnv, assessmentNodeList, lastModifications, repoEntry);
		UserEfficiencyStatementImpl efficiencyProperty = getUserEfficiencyStatementFull(repoEntry, assessedIdentity);
		if (efficiencyProperty == null) {
			// create new
			efficiencyProperty = new UserEfficiencyStatementImpl();
			efficiencyProperty.setVersion(0);
			efficiencyProperty.setCreationDate(new Date());
			efficiencyProperty.setIdentity(assessedIdentity);
			efficiencyProperty.setCourseRepoKey(repoEntry.getKey());
			efficiencyProperty.setResource(repoEntry.getOlatResource());
			efficiencyProperty.setCourseRepoKey(repoEntry.getKey());
			efficiencyProperty.setShortTitle(courseEnv.getRunStructure().getRootNode().getShortTitle());
			efficiencyProperty.setTitle(courseEnv.getRunStructure().getRootNode().getLongTitle());
			efficiencyProperty.setLastStatement(true);
			fillEfficiencyStatement(efficiencyStatement, lastModifications, efficiencyProperty);
			efficiencyProperty = persistOrLoad(efficiencyProperty, repoEntry, assessedIdentity);
			log.debug("creating new efficiency statement property::{} for id::{} repoEntry:: {}",
					(efficiencyProperty == null ? "null" : efficiencyProperty.getKey()), assessedIdentity.getKey() , repoEntry.getKey());
		} else {
			// update existing
			log.debug("updating efficiency statement property::{} for id::{} repoEntry::{}",
					efficiencyProperty.getKey() , assessedIdentity.getKey() , repoEntry.getKey());
			efficiencyProperty.setShortTitle(courseEnv.getRunStructure().getRootNode().getShortTitle());
			efficiencyProperty.setTitle(courseEnv.getRunStructure().getRootNode().getLongTitle());
			fillEfficiencyStatement(efficiencyStatement, lastModifications, efficiencyProperty);
			efficiencyProperty = updateUserEfficiencyStatement(efficiencyProperty);
		}
		
		// send modified event to everybody
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_EFFICIENCY_STATEMENT_CHANGED,
				assessedIdentity, repoEntry, null, null);
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseEnv.getCourseResourceableId());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, courseOres);
	}
	
	/**
	 * Merge the changes. Not synchronized!
	 * 
	 * @param statement The efficiency statement
	 * @return The merged efficiency statement
	 */
	public UserEfficiencyStatementImpl updateUserEfficiencyStatement(UserEfficiencyStatementImpl statement) {
		return dbInstance.getCurrentEntityManager().merge(statement);
	}
	
	protected UserEfficiencyStatementImpl persistOrLoad(UserEfficiencyStatementImpl efficiencyProperty, RepositoryEntry entry, Identity identity) {
		try {
			dbInstance.commit();
			dbInstance.getCurrentEntityManager().persist(efficiencyProperty);
			dbInstance.commit();
		} catch(PersistenceException | DBRuntimeException e) {
			if(PersistenceHelper.isConstraintViolationException(e)) {
				log.warn("", e);
				dbInstance.rollback();
				efficiencyProperty = getUserEfficiencyStatementFull(entry, identity);
			} else {
				log.error("", e);
			}
		}
		return efficiencyProperty;
	}
	
	public EfficiencyStatement createEfficiencyStatement(Identity assessedIdentity, final CourseEnvironment courseEnv,
			List<AssessmentNodeData> assessmentNodeList, AssessmentNodesLastModified lastModifications, final RepositoryEntry repoEntry) {
		List<Map<String,Object>> assessmentNodes = AssessmentHelper.assessmentNodeDataListToMap(assessmentNodeList);
		
		EfficiencyStatement efficiencyStatement = new EfficiencyStatement();
		efficiencyStatement.setAssessmentNodes(assessmentNodes);
		efficiencyStatement.setCourseTitle(courseEnv.getCourseTitle());
		efficiencyStatement.setCourseRepoEntryKey(repoEntry.getKey());
		String userInfos = userManager.getUserDisplayName(assessedIdentity);
		efficiencyStatement.setDisplayableUserInfo(userInfos);
		efficiencyStatement.setLastUpdated(System.currentTimeMillis());
		if(lastModifications != null) {
			if(lastModifications.getLastUserModified() != null) {
				efficiencyStatement.setLastUserModified(lastModifications.getLastUserModified().getTime());
			}
			if(lastModifications.getLastCoachModified() != null) {
				efficiencyStatement.setLastCoachModified(lastModifications.getLastCoachModified().getTime());
			}
		}
		return efficiencyStatement;
	}
	
	public void fillEfficiencyStatement(EfficiencyStatement efficiencyStatement, AssessmentNodesLastModified lastModifications, UserEfficiencyStatementImpl efficiencyProperty) {
		if(lastModifications != null) {
			if(lastModifications.getLastUserModified() != null
					&& (efficiencyProperty.getLastUserModified() == null || efficiencyProperty.getLastUserModified().before(lastModifications.getLastUserModified()))) {
				efficiencyProperty.setLastUserModified(lastModifications.getLastUserModified());
			}
			if(lastModifications.getLastCoachModified() != null
					&& (efficiencyProperty.getLastCoachModified() == null || efficiencyProperty.getLastCoachModified().before(lastModifications.getLastCoachModified()))) {
				efficiencyProperty.setLastCoachModified(lastModifications.getLastCoachModified());
			}
		}
		
		List<Map<String,Object>> nodeData = efficiencyStatement.getAssessmentNodes();
		if(!nodeData.isEmpty()) {
			Map<String,Object> rootNode = nodeData.get(0);
			Object passed = rootNode.get(AssessmentHelper.KEY_PASSED);
			if(passed instanceof Boolean pass) {
				efficiencyProperty.setPassed(pass);
			} else {
				efficiencyProperty.setPassed(null);
			}
			
			Object score = rootNode.get(AssessmentHelper.KEY_SCORE_F);
			if(score instanceof Float fScore) {
				efficiencyProperty.setScore(fScore);	
			} else if(score instanceof Number nScore) {
				efficiencyProperty.setScore(nScore.floatValue());
			} else {
				efficiencyProperty.setScore(null);
			}
			
			Object weightedScore = rootNode.get(AssessmentHelper.KEY_WEIGHTED_SCORE_F);
			if(weightedScore instanceof Float fWeightedScore) {
				efficiencyProperty.setWeightedScore(fWeightedScore);	
			} else if(weightedScore instanceof Number nWeightedScore) {
				efficiencyProperty.setWeightedScore(nWeightedScore.floatValue());
			} else {
				efficiencyProperty.setWeightedScore(null);
			}
			
			if(rootNode.get(AssessmentHelper.KEY_COMPLETION) instanceof Double completion) {
				efficiencyProperty.setCompletion(completion);
			}
	
			if(rootNode.get(AssessmentHelper.KEY_GRADE) instanceof String grade) {
				efficiencyProperty.setGrade(grade);
			}
			
			if(rootNode.get(AssessmentHelper.KEY_GRADE_SYSTEM_IDENT) instanceof String gradeSystemIdent) {
				efficiencyProperty.setGradeSystemIdent(gradeSystemIdent);
			}
			
			if(rootNode.get(AssessmentHelper.KEY_PERFORMANCE_CLASS_IDENT) instanceof String performanceClassIdent) {
				efficiencyProperty.setPerformanceClassIdent(performanceClassIdent);
			}
			
			if(rootNode.get(AssessmentHelper.KEY_TITLE_SHORT) instanceof String shortTitle) {
				efficiencyProperty.setShortTitle(shortTitle);
			}
			
			if(rootNode.get(AssessmentHelper.KEY_TITLE_LONG) instanceof String longTitle) {
				efficiencyProperty.setTitle(longTitle);
			}
			
			int totalNodes = getTotalNodes(nodeData);
			efficiencyProperty.setTotalNodes(totalNodes);
			
			int attemptedNodes = getAttemptedNodes(nodeData);
			efficiencyProperty.setAttemptedNodes(attemptedNodes);
			
			int passedNodes = getPassedNodes(nodeData);
			efficiencyProperty.setPassedNodes(passedNodes);
			
			for(Map<String,Object> node:nodeData) {
				Date lastUserModified = (Date)node.get(AssessmentHelper.KEY_LAST_USER_MODIFIED);
				if(lastUserModified != null) {
					if(efficiencyProperty.getLastUserModified() == null || efficiencyProperty.getLastUserModified().before(lastUserModified)) {
						efficiencyProperty.setLastUserModified(lastUserModified);
					}
				}
				Date lastCoachModified = (Date)node.get(AssessmentHelper.KEY_LAST_COACH_MODIFIED);
				if(lastCoachModified != null) {
					if(efficiencyProperty.getLastCoachModified() == null || efficiencyProperty.getLastCoachModified().before(lastCoachModified)) {
						efficiencyProperty.setLastCoachModified(lastCoachModified);
					}
				}
			}
		}

		efficiencyProperty.setLastModified(new Date());
		efficiencyProperty.setStatementXml(toXML(efficiencyStatement));
	}
	
	public boolean invalidateEfficiencyStatement(RepositoryEntryRef courseRepoEntry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("update effstatement statement set statement.lastStatement=false")
		  .append(" where statement.identity.key=:identityKey and statement.courseRepoKey=:repoKey")
		  .append(" and statement.lastStatement=true");

		int rowUpdated = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", courseRepoEntry.getKey())
				.executeUpdate();
		return rowUpdated > 0;
	}

	/**
	 * Get the user efficiency statement list for this course
	 * @param courseRepoEntryKey
	 * @param identity
	 * @return The efficiency statement
	 */
	public EfficiencyStatement getUserEfficiencyStatementByCourseRepositoryEntry(RepositoryEntry courseRepoEntry, IdentityRef identity){
		UserEfficiencyStatementImpl s = getUserEfficiencyStatementFull(courseRepoEntry, identity);
		if(s == null || s.getStatementXml() == null) {
			return null;
		}
		return fromXML(s.getStatementXml());
	}
	
	/**
	 * Returns the efficiency statement marked as last statement.
	 * 
	 * @param resourceKey The resource key
	 * @param identity The identity
	 * @return The efficiency statement
	 */
	public EfficiencyStatement getUserEfficiencyStatementByResourceKey(Long resourceKey, Identity identity){
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementstandalone as statement")
		  .append(" where statement.identity.key=:identityKey and statement.resourceKey=:resourceKey")
		  .append(" and statement.lastStatement=true");

		List<UserEfficiencyStatementStandalone> statement = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementStandalone.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceKey", resourceKey)
				.getResultList();
		if(statement.isEmpty() || statement.get(0).getStatementXml() == null) {
			return null;
		}
		return fromXML(statement.get(0).getStatementXml());
	}
	
	/**
	 * Returns the specified efficiency statement.
	 * 
	 * @param statementKey The user efficiency statement key
	 * @return The efficiency statement
	 */
	public EfficiencyStatement getEfficiencyStatementByKey(Long statementKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatement as statement ")
		  .append(" left join fetch statement.resource as resource")
		  .append(" where statement.key=:statementKey");

		List<UserEfficiencyStatementImpl> statement = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementImpl.class)
				.setParameter("statementKey", statementKey)
				.getResultList();
		if(statement.isEmpty() || statement.get(0).getStatementXml() == null) {
			return null;
		}
		return fromXML(statement.get(0).getStatementXml());
	}
	
	public EfficiencyStatement getEfficiencyStatement(UserEfficiencyStatement userEfficiencyStatement) {
		if(userEfficiencyStatement instanceof UserEfficiencyStatementImpl uefs) {
			return fromXML(uefs.getStatementXml());
		}
		if(userEfficiencyStatement instanceof UserEfficiencyStatementStandalone uefs) {
			return fromXML(uefs.getStatementXml());
		}
		return getEfficiencyStatementByKey(userEfficiencyStatement.getKey());
	}
	
	/**
	 * Returns the efficiency statement marked as last statement.
	 * 
	 * @param courseRepoEntry The repository entry key
	 * @param identity The identity
	 * @return The user efficiency statement with the XML statement loaded
	 */
	public UserEfficiencyStatementImpl getUserEfficiencyStatementFull(RepositoryEntryRef courseRepoEntry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatement as statement ")
		  .append(" left join fetch statement.resource as resource")
		  .append(" where statement.identity.key=:identityKey and statement.courseRepoKey=:repoKey")
		  .append(" and statement.lastStatement=true");

		List<UserEfficiencyStatementImpl> statement = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementImpl.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", courseRepoEntry.getKey())
				.getResultList();
		if(statement.isEmpty()) {
			return null;
		}
		return statement.get(0);
	}
	
	/**
	 * Returns the efficiency statements of the specified user, but only the statements
	 * marked as last statement of a course.
	 * 
	 * @param identity The identity
	 * @return A list of efficiency statements
	 */
	public List<UserEfficiencyStatementImpl> getUserEfficiencyStatementFull(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatement as statement")
		  .append(" left join fetch statement.resource as resource")
		  .append(" where statement.identity.key=:identityKey and statement.lastStatement=true");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementImpl.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public boolean hasUserEfficiencyStatement(Long courseRepoEntryKey, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement.key from effstatementlight as statement")
		  .append(" where statement.identity.key=:identityKey and statement.courseRepoKey=:repoKey");

		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", courseRepoEntryKey)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0) != null;
	}
	
	/**
	 * Returns the user efficiency statement marked as last statement.
	 * 
	 * @param courseRepo The repository entry
	 * @param identity The identity
	 * @return The user efficiency statement
	 */
	public UserEfficiencyStatement getUserEfficiencyStatementLightByRepositoryEntry(RepositoryEntryRef courseRepo, IdentityRef identity) {
		if (identity == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.identity.key=:identityKey and statement.courseRepoKey=:repoKey")
		  .append(" and statement.lastStatement=true");

		List<UserEfficiencyStatement> statement = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", courseRepo.getKey())
				.getResultList();
		if(statement.isEmpty()) {
			return null;
		}
		return statement.get(0);
	}

	/**
	 * Returns all the user efficiency statements of the specified course and identity.
	 * 
	 * @param courseRepo The repository entry key
	 * @param identity The identity
	 * @return A list of user efficiency statements
	 */
	public List<UserEfficiencyStatement> getHistoryOfUserEfficiencyStatementsLightByRepositoryEntry(RepositoryEntryRef courseRepo, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.identity.key=:identityKey and statement.courseRepoKey=:repoKey")
		  .append(" order by statement.creationDate desc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoKey", courseRepo.getKey())
				.getResultList();
	} 
	
	public List<UserEfficiencyStatement> getUserEfficiencyStatementLight(IdentityRef student, List<RepositoryEntry> courses) {
		if(student == null || courses == null || courses.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.identity.key=:studentKey and statement.resourceKey in (:courseResourcesKey)")
		  .append(" and statement.lastStatement=true");
		
		List<Long> coursesKey = new ArrayList<>();
		for(RepositoryEntry course:courses) {
			coursesKey.add(course.getOlatResource().getKey());
		}

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("courseResourcesKey",coursesKey)
				.setParameter("studentKey", student.getKey())
				.getResultList();
	}
	
	public List<UserEfficiencyStatement> getUserEfficiencyStatementLight(IdentityRef student) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.identity.key=:studentKey")
		  .append(" and statement.lastStatement=true");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("studentKey", student.getKey())
				.getResultList();
	}
	
	public List<UserEfficiencyStatementForCoaching> getUserEfficiencyStatementForCoaching(RepositoryEntry course) {
		if(course == null) return Collections.emptyList();
		
		Long resourceKey = course.getOlatResource().getKey();

		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("efficiencyStatemtForCoachingByResourceKeys", UserEfficiencyStatementForCoaching.class)
				.setParameter("courseResourceKey", resourceKey)
				.getResultList();
	}
	
	public List<UserEfficiencyStatementForCoaching> getUserEfficiencyStatementForCoaching(BusinessGroup group) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select distinct statement from effstatementcoaching as statement")
		  .append(" inner join repositoryentry as v on (v.olatResource.key=statement.resourceKey)")
		  .append(" inner join v.groups as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" where baseGroup.key=:bGroupKey and statement.identityKey=membership.identity.key")
		  .append(" and statement.lastStatement=true");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementForCoaching.class)
				.setParameter("bGroupKey", group.getBaseGroup().getKey())
				.getResultList();
	}
	
	public UserEfficiencyStatement getUserEfficiencyStatementLightByResource(Long resourceKey, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementstandalone as statement")
		  .append(" where statement.identity.key=:identityKey and statement.resourceKey=:resourceKey")
		  .append(" and statement.lastStatement=true");

		List<UserEfficiencyStatement> statement = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatement.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceKey", resourceKey)
				.getResultList();
		if(statement.isEmpty()) {
			return null;
		}
		return statement.get(0);
	}
	
	public UserEfficiencyStatement getUserEfficiencyStatementByKey(Long key) {
		String query = "select statement from effstatement as statement where statement.key=:key";

		List<UserEfficiencyStatementImpl> statement = dbInstance.getCurrentEntityManager()
				.createQuery(query, UserEfficiencyStatementImpl.class)
				.setParameter("key", key)
				.getResultList();
		if(statement.isEmpty() || !StringHelper.containsNonWhitespace(statement.get(0).getStatementXml())) {
			return null;
		}
		return statement.get(0);
	}
	
	public UserEfficiencyStatementLight getUserEfficiencyStatementLightByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.key=:key");

		List<UserEfficiencyStatementLight> statement = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementLight.class)
				.setParameter("key", key)
				.getResultList();
		if(statement.isEmpty()) {
			return null;
		}
		return statement.get(0);
	}

	/**
	 * Get the passed value of a course node of a specific efficiency statement
	 * @param nodeIdent
	 * @param efficiencyStatement
	 * @return true if passed, false if not, null if node not found
	 */
	public Boolean getPassed(String nodeIdent, EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		if (assessmentNodes != null) {
			Iterator<Map<String,Object>> iter = assessmentNodes.iterator();
			while (iter.hasNext()) {
				Map<String,Object> nodeData = iter.next();
				if (nodeData.get(AssessmentHelper.KEY_IDENTIFYER).equals(nodeIdent)) {
					return (Boolean) nodeData.get(AssessmentHelper.KEY_PASSED);
				}
			}
		}
		return null;
	}
	
	public int getTotalNodes(List<Map<String,Object>> assessmentNodes) {
		int count = 0;
		for (Iterator<Map<String,Object>> iter = assessmentNodes.iterator(); iter.hasNext(); ) {
			Map<String,Object> nodeData = iter.next();
			Boolean selectable = (Boolean)nodeData.get(AssessmentHelper.KEY_SELECTABLE);
			if(selectable != null && selectable.booleanValue()) {
				count++;
			}
		}
		return count;
	}
	
	public int getAttemptedNodes(List<Map<String,Object>> assessmentNodes) {
		int count = 0;
		for (Iterator<Map<String,Object>> iter = assessmentNodes.iterator(); iter.hasNext(); ) {
			Map<String,Object> nodeData = iter.next();
			Boolean selectable = (Boolean)nodeData.get(AssessmentHelper.KEY_SELECTABLE);
			if(selectable != null && selectable.booleanValue()) {
				if(nodeData.containsKey(AssessmentHelper.KEY_SCORE) || nodeData.containsKey(AssessmentHelper.KEY_PASSED)) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getPassedNodes(List<Map<String,Object>> assessmentNodes) {
		int count = 0;
		for (Iterator<Map<String,Object>> iter = assessmentNodes.iterator(); iter.hasNext(); ) {
			Map<String,Object> nodeData = iter.next();
			Boolean passed = (Boolean)nodeData.get(AssessmentHelper.KEY_PASSED);
			Boolean selectable = (Boolean)nodeData.get(AssessmentHelper.KEY_SELECTABLE);
			if(passed != null && passed.booleanValue() && selectable != null && selectable.booleanValue()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Get the score value of a course node of a specific efficiency statement
	 * @param nodeIdent
	 * @param efficiencyStatement
	 * @return the score, null if node not found
	 */
	public Double getScore(String nodeIdent, EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		if (assessmentNodes != null) {
			Iterator<Map<String,Object>> iter = assessmentNodes.iterator();
			while (iter.hasNext()) {
				Map<String,Object> nodeData = iter.next();
				if (nodeData.get(AssessmentHelper.KEY_IDENTIFYER).equals(nodeIdent)) {
					String scoreString = (String) nodeData.get(AssessmentHelper.KEY_SCORE);
					return Double.valueOf(scoreString);
				}
			}
		}
		return null;
	}
	
	/**
	 * Find all efficiency statements for a specific user
	 * @param identity
	 * @return List of efficiency statements
	 */
	protected List<EfficiencyStatement> findEfficiencyStatements(IdentityRef identity) {
		List<EfficiencyStatement> efficiencyStatements = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from effstatement as statement")
		  .append(" where statement.identity.key=:identityKey")
		  .append(" and statement.lastStatement=true");

		List<UserEfficiencyStatementImpl> statements = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementImpl.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		for(UserEfficiencyStatementImpl statement:statements) {
			if(StringHelper.containsNonWhitespace(statement.getStatementXml())) {
				EfficiencyStatement s = fromXML(statement.getStatementXml());
				efficiencyStatements.add(s);
			}
		}
		return efficiencyStatements;
	}
	
	/**
	 * Find all efficiency statements for a specific user.
	 * This method return only efficiency statements for courses with enabled
	 * efficiency statement config.
	 * 
	 * @param identity
	 * @return List of efficiency statements
	 */
	public List<UserEfficiencyStatementLight> findEfficiencyStatementsLight(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.identity.key=:identityKey")
		  .append(" and statement.lastStatement=true");
		
		List<UserEfficiencyStatementLight> allEfficiencyStatements = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementLight.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		
		List<Long> courseEntryKeys = allEfficiencyStatements.stream()
				.map(UserEfficiencyStatementLight::getCourseRepoKey)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Map<Long, RepositoryEntry> keyToRepositoryEntry = repositoryManager.lookupRepositoryEntries(courseEntryKeys).stream()
			.collect(Collectors.toMap(RepositoryEntry::getKey, Function.identity()));
		
		List<UserEfficiencyStatementLight> efficiencyStatements = new ArrayList<>(allEfficiencyStatements.size());
		for (UserEfficiencyStatementLight efficiencyStatement : allEfficiencyStatements) {
			if (efficiencyStatement.getCourseRepoKey() != null) {
				RepositoryEntry repositoryEntry = keyToRepositoryEntry.get(efficiencyStatement.getCourseRepoKey());
				if (repositoryEntry != null) {
					try {
						ICourse course = CourseFactory.loadCourse(repositoryEntry);
						if (course != null && course.getCourseConfig().isEfficiencyStatementEnabled()) {
							efficiencyStatements.add(efficiencyStatement);
						}
					} catch (Exception e) {
						//
					}
				} else {
					// Probably deleted course.
					// If course has efficiency statement disabled, the efficiency statement is
					// deleted when the course is deleted. So efficiency statement was enabled.
					efficiencyStatements.add(efficiencyStatement);
				}
			} else {
				// Probably standalone statement
				efficiencyStatements.add(efficiencyStatement);
			}
		}
		
		return efficiencyStatements;
	}
	
	public List<UserEfficiencyStatementLight> findEfficiencyStatementsLight(List<Long> keys) {
		if(keys == null || keys.isEmpty()) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select statement from effstatementlight as statement")
		  .append(" where statement.key in (:keys)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementLight.class)
				.setParameter("keys", keys)
				.getResultList();
	}
	
	public List<Identity> findIdentitiesWithEfficiencyStatements(RepositoryEntryRef repoEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(statement.identity) from effstatement as statement ")
		  .append(" where statement.courseRepoKey=:repoKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", repoEntry.getKey())
				.getResultList();
	}
	
	/**
	 * Delete all efficiency statements from the given course for all users
	 * @param courseRepoEntryKey
	 * @return int number of deleted efficiency statements
	 */
	public void deleteEfficiencyStatementsFromCourse(Long courseRepoEntryKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from effstatement as statement ")
		  .append(" where statement.courseRepoKey=:repoKey");

		List<UserEfficiencyStatementImpl> statements = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UserEfficiencyStatementImpl.class)
				.setParameter("repoKey", courseRepoEntryKey)
				.getResultList();
		for(UserEfficiencyStatementImpl statement:statements) {
			dbInstance.deleteObject(statement);
		}
	}
	
	/**
	 * Delete the given efficiency statement for this person
	 * @param efficiencyStatement
	 */
	public void deleteEfficiencyStatement(UserEfficiencyStatementLight efficiencyStatement) {
		dbInstance.getCurrentEntityManager().remove(efficiencyStatement);
	}

	/**
	 * Create or update all efficiency statement lists for the given list of identities and this course
	 * This is called from only one thread, since the course is locked at editing (either CourseEdit or CourseDetails edit).
	 * 
	 * @param ores The resource to load the course
	 * @param identities List of identities
	 * false: always create new one (be careful with this one!)
	 */	
	public void updateEfficiencyStatements(final RepositoryEntry courseEntry, List<Identity> identities) {
		if (!identities.isEmpty()) {
			final ICourse course = CourseFactory.loadCourse(courseEntry);
			final CourseEnvironment courseEnv = course.getCourseEnvironment();
			log.info(Tracing.M_AUDIT, "Updating efficiency statements for course::{}, this might produce temporary heavy load on the CPU", course.getResourceableId());

			int count = 0;
			for (Identity identity : identities) {			
				UserCourseEnvironment uce = AssessmentHelper.createInitAndUpdateUserCourseEnvironment(identity, course);
				AssessmentNodesLastModified lastModifications = new AssessmentNodesLastModified();
				List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.getAssessmentNodeDataList(uce, lastModifications, true, true, true);
				doInSyncUpdateUserEfficiencyStatement(identity, courseEnv, assessmentNodeList, lastModifications,  courseEntry);
				if (Thread.interrupted()) {
					break;
				}
				
				if(++count % 10 == 0) {
					DBFactory.getInstance().commitAndCloseSession();
				}
			}
		}
	}

	@Override
	public String getExporterID() {
		return "efficiency.statements";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDir, Locale locale) {
		List<EfficiencyStatement> efficiencyStatements = findEfficiencyStatements(identity);
		File archiveFile = new EfficiencyStatementArchiver(locale)
				.archive(efficiencyStatements, identity, archiveDir);
		manifest.appendFile(archiveFile.getName());
	}

	/**
	 * Archive efficiency statement and than delete them for the specified identity.
	 * 
	 * @param identity  Delete data for this identity.
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		deleteEfficientyStatement(identity);
	}
	
	public void deleteEfficientyStatement(Identity identity) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from effstatement as statement ")
			  .append(" where statement.identity.key=:identityKey");

			int numOfDeletedStatements = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString())
					.setParameter("identityKey", identity.getKey())
					.executeUpdate();
			
			log.debug("{} efficiency statements deleted for identity={}", numOfDeletedStatements, identity);
		} catch (Exception e) {
			log.error("deleteUserData(EfficiencyStatements): {}", identity, e);
		}
	}
}
