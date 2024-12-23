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
package org.olat.course.nodes.survey;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;

/**
 * 
 * Initial date: 02.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SurveyRunSecurityCallback {
	
	private final boolean admin;
	private final boolean courseReadOnly;
	private final boolean guestOnly;
	private boolean executor;
	private boolean reportViewer;
	private boolean reportViewerManger;
	
	public SurveyRunSecurityCallback(ModuleConfiguration moduleConfiguration, UserCourseEnvironment userCourseEnv) {
		this.courseReadOnly = userCourseEnv.isCourseReadOnly();
		this.guestOnly = userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly();
		NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
		SurveyModule surveyModule = CoreSpringFactory.getImpl(SurveyModule.class);
		this.executor = nodeRightService.isGranted(moduleConfiguration, userCourseEnv, surveyModule.getExecutionNodeRightType());
		this.reportViewer = nodeRightService.isGranted(moduleConfiguration, userCourseEnv, surveyModule.getReportNodeRightType());
		this.admin = userCourseEnv.isAdmin();
		if (admin) {
			if (executor) {
				// Only course owners may execute but not managers.
				// The NodeRightService returns managers as executers as well, so we exclude them now.
				executor = isCurrentlyOwner(userCourseEnv);
			}
			if (!executor) {
				// Managers may always see the report (if selected in role switcher)
				// even without prior fill in the survey (e.g. to reset the survey)
				RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
				reportViewerManger = repositoryService.hasRoleExpanded(
						userCourseEnv.getIdentityEnvironment().getIdentity(),
						userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
						OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(), OrganisationRoles.learnresourcemanager.name());
			}
		}
	}
	
	private boolean isCurrentlyOwner(UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv instanceof UserCourseEnvironmentImpl uceImpl) {
			if (uceImpl.getCurrentRole() != null && uceImpl.getCurrentRole() == Role.owner) {
				return true;
			}
		}
		return false;
	}

	public boolean isGuestOnly() {
		return guestOnly;
	}

	public boolean isExecutor() {
		return executor;
	}

	public boolean isReportViewer() {
		return reportViewer;
	}

	public boolean canParticipate() {
		return isExecutor() && !courseReadOnly;
	}
	
	public boolean hasParticipated(EvaluationFormParticipation participation) {
		return participation != null && EvaluationFormParticipationStatus.done.equals(participation.getStatus());
	}

	public boolean canExecute(EvaluationFormParticipation participation) {
		return participation != null && isExecutor() && !hasParticipated(participation);
	}
	
	public boolean canViewReporting(EvaluationFormParticipation participation) {
		if (reportViewerManger) {
			return true;
		}
		
		if (isReportViewer()) {
			if (!isExecutor()) {
				return true;
			}
			if (hasParticipated(participation)) {
				return true;
			}
			if (isReadOnly()) {
				return true;
			}
		}
		return false;
	}

	public boolean isReadOnly() {
		return courseReadOnly && isExecutor();
	}

	public boolean canResetAll() {
		return !courseReadOnly && admin;
	}

}
