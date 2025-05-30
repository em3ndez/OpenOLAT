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
package org.olat.modules.quality.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormDispatcher;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityExecutorParticipationStatus;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.model.QualityParticipationStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityParticipationDAO {
	
	@Autowired
	private DB dbInstance;
	
	List<QualityParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			Collection<String> identifierTypesOnly, Collection<String> identifierTypesExcluded) {
		if (dataCollection == null) {
			return List.of();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityParticipationImpl(");
		sb.append("       participation.key");
		sb.append("     , (case when participation.firstName is null then user.firstName else participation.firstName end) as firstname");
		sb.append("     , (case when participation.lastName is null then user.lastName else participation.lastName end) as lastname");
		sb.append("     , (case when participation.email is null then user.email else participation.email end) as email");
		sb.append("     , context.key");
		sb.append("     , context.role as role");
		sb.append("     , audienceRepositoryEntry.displayname as repositoryEntryName");
		sb.append("     , audienceCurriculumElement.displayName as curriculumElementName");
		sb.append("       )");
		sb.append("  from evaluationformparticipation as participation");
		sb.append(" inner join participation.survey as survey");
		sb.append("  left join participation.executor as executor");
		sb.append("  left join executor.user as user");
		sb.append("  left join qualitycontext as context on participation.key = context.evaluationFormParticipation.key");
		sb.append("  left join context.audienceRepositoryEntry as audienceRepositoryEntry");
		sb.append("  left join context.audienceCurriculumElement as audienceCurriculumElement");
		sb.append(" where survey.resName=:resName");
		sb.append("   and survey.resId=:resId");
		if (identifierTypesOnly != null && !identifierTypesOnly.isEmpty()) {
			sb.append(" and participation.identifier.type in :identifierTypesOnly");
		}
		if (identifierTypesExcluded != null && !identifierTypesExcluded.isEmpty()) {
			sb.append(" and participation.identifier.type not in :identifierTypesExcluded");
		}
		
		TypedQuery<QualityParticipation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityParticipation.class)
				.setParameter("resName", dataCollection.getResourceableTypeName())
				.setParameter("resId", dataCollection.getResourceableId());
		if (identifierTypesOnly != null && !identifierTypesOnly.isEmpty()) {
			query.setParameter("identifierTypesOnly", identifierTypesOnly);
		}
		if (identifierTypesExcluded != null && !identifierTypesExcluded.isEmpty()) {
			query.setParameter("identifierTypesExcluded", identifierTypesExcluded);
		}
		
		return query.getResultList();
	}

	QualityParticipationStats getExecutorParticipationStats(QualityExecutorParticipationSearchParams searchParam) {
		if (searchParam == null) return QualityParticipationStats.ZEROS;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityParticipationStats(");
		sb.append("       sum(case when participation.executor is not null then 1 else 0 end)");
		sb.append("     , sum(case when participation.identifier.type = '").append(EvaluationFormDispatcher.EMAIL_PARTICIPATION_TYPE).append("' then 1 else 0 end)");
		sb.append("     , sum(case when participation.identifier.type = '").append(EvaluationFormDispatcher.PUBLIC_PARTICIPATION_TYPE).append("'");
		sb.append("                 and participation.status = '").append(EvaluationFormParticipationStatus.done).append("'");
		sb.append("                then 1 else 0 end)");
		sb.append("     )");
		appendFrom(sb);
		appendWhereClause(sb, searchParam);
		
		TypedQuery<QualityParticipationStats> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityParticipationStats.class);
		appendWhereParameters(query, searchParam);
		List<QualityParticipationStats> stats = query.getResultList();
		return stats != null && !stats.isEmpty()? stats.get(0): QualityParticipationStats.ZEROS;
	}

	public List<QualityExecutorParticipation> loadExecutorParticipations(Translator translator,
			QualityExecutorParticipationSearchParams searchParam, int firstResult, int maxResults, SortKey... orderBy) {
		if (searchParam == null) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.quality.model.QualityExcecutorParticipationImpl(");
		sb.append("       participation.key as participationKey");
		sb.append("     , participation.identifier as identifier");
		sb.append("     , case");
		sb.append("           when participation.status = '").append(EvaluationFormParticipationStatus.done).append("'");
		sb.append("           then ").append(QualityExecutorParticipationStatus.PARTICIPATED.getOrder());
		sb.append("           when session is not null and collection.status = '").append(QualityDataCollectionStatus.RUNNING).append("'");
		sb.append("           then ").append(QualityExecutorParticipationStatus.PARTICIPATING.getOrder());
		sb.append("           when collection.status = '").append(QualityDataCollectionStatus.FINISHED).append("'");
		sb.append("           then ").append(QualityExecutorParticipationStatus.OVER.getOrder());
		sb.append("           when collection.status = '").append(QualityDataCollectionStatus.RUNNING).append("'");
		sb.append("           then ").append(QualityExecutorParticipationStatus.READY.getOrder());
		sb.append("           else ").append(QualityExecutorParticipationStatus.FUTURE.getOrder());
		sb.append("       end as executionStatus");
		sb.append("     , collection.start as start");
		sb.append("     , collection.deadline as deadline");
		sb.append("     , collection.title as title");
		sb.append("     , collection.topicType as topicType");
		sb.append("     , case");
		sb.append("       when curriculumElementType is not null");
		sb.append("       then curriculumElementType.displayName");
		for (QualityDataCollectionTopicType topicType: QualityDataCollectionTopicType.values()) {
			sb.append("       when collection.topicType = '").append(topicType.toString()).append("'");
			sb.append("       then '").append(translator.translate(topicType.getI18nKey())).append("'");
		}
		sb.append("       end as typeName");
		sb.append("     , case collection.topicType");
		sb.append("            when '").append(QualityDataCollectionTopicType.CUSTOM).append("'");
		sb.append("            then collection.topicCustom");
		sb.append("            when '").append(QualityDataCollectionTopicType.IDENTIY).append("'");
		sb.append("              then case when collection.topicIdentity.status < ").append(Identity.STATUS_DELETED);
		sb.append("                then concat(user.lastName, ' ', user.firstName) end");
		sb.append("            when '").append(QualityDataCollectionTopicType.ORGANISATION).append("'");
		sb.append("            then organisation.displayName");
		sb.append("            when '").append(QualityDataCollectionTopicType.CURRICULUM).append("'");
		sb.append("            then curriculum.displayName");
		sb.append("            when '").append(QualityDataCollectionTopicType.CURRICULUM_ELEMENT).append("'");
		sb.append("            then curriculumElement.displayName");
		sb.append("            when '").append(QualityDataCollectionTopicType.REPOSITORY).append("'");
		sb.append("            then repository.displayname");
		sb.append("       end as topic");
		sb.append("     , previousCollection.title as previousTitle");
		sb.append("       )");
		appendFrom(sb);
		appendWhereClause(sb, searchParam);
		
		appendExecutorParticipationOrderBy(sb, orderBy);

		TypedQuery<QualityExecutorParticipation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityExecutorParticipation.class);
		appendWhereParameters(query, searchParam);
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}

	public void appendFrom(QueryBuilder sb) {
		sb.append("  from evaluationformparticipation as participation");
		sb.append("       inner join participation.survey as survey");
		sb.append("       left join participation.executor as executor");
		sb.append("       left join evaluationformsession as session on session.participation.key = participation.key");
		sb.append("       inner join qualitydatacollection as collection on collection.key = survey.resId");
		sb.append("       left join collection.topicIdentity.user as user");
		sb.append("       left join collection.topicOrganisation as organisation");
		sb.append("       left join collection.topicCurriculum as curriculum");
		sb.append("       left join collection.topicCurriculumElement as curriculumElement");
		sb.append("       left join curriculumElement.type as curriculumElementType");
		sb.append("       left join collection.topicRepositoryEntry as repository");
		sb.append("       left join survey.seriesPrevious as previousSurvey");
		sb.append("       left join qualitydatacollection as previousCollection on previousSurvey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		sb.append("                                                            and previousSurvey.resId = previousCollection.key");
	}

	private void appendWhereClause(QueryBuilder sb, QualityExecutorParticipationSearchParams searchParam) {
		sb.and().append("survey.resName = '").append(QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME).append("'");
		if (searchParam.getExecutorRef() != null && searchParam.getExecutorRef().getKey() != null) {
			sb.and().append("executor.key = :executorKey");
		}
		if (searchParam.getDataCollectionKeys() != null && !searchParam.getDataCollectionKeys().isEmpty()) {
			sb.and().append("collection.key in (:dataCollectionKeys)");
		}
		if (searchParam.getParticipationRef() != null && searchParam.getParticipationRef().getKey() != null) {
			sb.and().append("participation.key = :participationKey");
		}
		if (searchParam.getParticipationStatus() != null) {
			sb.and().append("participation.status = :participationStatus");
		}
		if (searchParam.getDataCollectionStatus() != null && !searchParam.getDataCollectionStatus().isEmpty()) {
			sb.and().append("collection.status in :collectionStatus");
		}
	}

	private void appendWhereParameters(TypedQuery<?> query, QualityExecutorParticipationSearchParams searchParam) {
		if (searchParam.getExecutorRef() != null && searchParam.getExecutorRef().getKey() != null) {
			query.setParameter("executorKey", searchParam.getExecutorRef().getKey());
		}
		if (searchParam.getDataCollectionKeys() != null && !searchParam.getDataCollectionKeys().isEmpty()) {
			query.setParameter("dataCollectionKeys", searchParam.getDataCollectionKeys());
		}
		if (searchParam.getParticipationRef() != null && searchParam.getParticipationRef().getKey() != null) {
			query.setParameter("participationKey", searchParam.getParticipationRef().getKey());
		}
		if (searchParam.getParticipationStatus() != null) {
			query.setParameter("participationStatus", searchParam.getParticipationStatus());
		}
		if (searchParam.getDataCollectionStatus() != null && !searchParam.getDataCollectionStatus().isEmpty()) {
			query.setParameter("collectionStatus", searchParam.getDataCollectionStatus());
		}
	}

	private void appendExecutorParticipationOrderBy(QueryBuilder sb, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			sb.append(sortKey);
			sb.appendAsc(asc);
		} else {
			sb.append(" order by executionStatus asc, start desc ");
		}
	}

}

