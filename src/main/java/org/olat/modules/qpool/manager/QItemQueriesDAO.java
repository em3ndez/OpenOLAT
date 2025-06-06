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
package org.olat.modules.qpool.manager;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionItemView.OrderBy;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.ItemWrapper;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service do only the main queries for the question pool and mapped them
 * to the standardized QuestionItemView
 * 
 * 
 * Initial date: 12.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qItemQueriesDAO")
public class QItemQueriesDAO {
	
	@Autowired
	private DB dbInstance;
	
	
	public int countItems(SearchQuestionItemParams params) {
		QueryBuilder sb = new QueryBuilder(4096);
		sb.append("select count(item.key)")
		  .append(" from questionitem item")
		  .append(" left join item.type itemType")
		  .append(" left join item.taxonomyLevel taxonomyLevel")
		  .append(" left join item.educationalContext educationalContext");
		appendIn(sb, params);
		appendWhere(sb, params);
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class);
		appendParameters(params, query);
		if(params.isFavoritOnly()) {
			query.setParameter("identityKey", params.getIdentity().getKey());	
		}
		return query.getSingleResult().intValue();
	}

	public List<QuestionItemView> getItems(SearchQuestionItemParams params,
			int firstResult, int maxResults, SortKey... orderBy) {
		QueryBuilder sb = new QueryBuilder(4096);
		sb.append("select item, ")
		  .append(" (select count(sgmi.key) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
		  .append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
		  .append(" ) as owners,")
		  .append(" (select count(teacherCompetence.key) from ctaxonomycompetence teacherCompetence")
		  .append("   where taxonomyLevel.materializedPathKeys like concat(teacherCompetence.taxonomyLevel.materializedPathKeys, '%')")
		  .append("     and teacherCompetence.identity.key=:identityKey")
		  .append("     and teacherCompetence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		  .append(" ) as teacher,")
		  .append(" (select count(managerCompetence.key) from ctaxonomycompetence managerCompetence")
		  .append("   where taxonomyLevel.materializedPathKeys like concat(managerCompetence.taxonomyLevel.materializedPathKeys, '%')")
		  .append("     and managerCompetence.identity.key=:identityKey")
		  .append("     and managerCompetence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		  .append(" ) as manager,")
		  .append(" (select count(p2item.key) from qpool2item as p2item")
		  .append("    where p2item.item.key=item.key")
		  .append("      and p2item.editable=true")
		  .append(" ) as pools,")
		  .append(" (select count(sItem.key) from qshareitem as sItem")
		  .append("    where sItem.item.key=item.key")
		  .append("      and sItem.editable=true")
		  .append(" ) as groups,");
		if(params.isFavoritOnly()) {
			sb.append(" 1 as marks,");
		} else {
			sb.append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
			  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
			  .append(" ) as marks,");
		}
		sb.append(" (select count(userRating.key) from userrating as userRating")
		  .append("   where userRating.resId=item.key and userRating.resName='QuestionItem'")
		  .append("     and userRating.creator.key=:identityKey")
		  .append(" ) as numberOfRatingsIdentity,")
		  .append(" (select avg(avgRating.rating) from userrating as avgRating")
		  .append("   where avgRating.resId=item.key and avgRating.resName='QuestionItem'")
		  .append(" ) as rating,")
		  .append(" (select count(totalRating.key) from userrating as totalRating")
		  .append("   where totalRating.resId=item.key and totalRating.resName='QuestionItem'")
		  .append(" ) as numberOfRatingsTotal")
		  .append(" from questionitem as item")
		  .append(" inner join fetch item.ownerGroup as ownerGroup")
		  .append(" left join fetch item.type as itemType")
		  .append(" left join fetch item.taxonomyLevel as taxonomyLevel")
		  .append(" left join fetch item.educationalContext as educationalContext");
		
		appendIn(sb, params);
		appendWhere(sb, params);

		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null && !OrderBy.marks.name().equals(orderBy[0].getKey())) {
			appendOrderBy(sb, "item", "taxonomyLevel", orderBy);
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		appendParameters(params, query);
		query.setParameter("identityKey", params.getIdentity().getKey());
		if(params.getCollection() != null ) {
			query.setParameter("collectionKey", params.getCollection().getKey());
		}
		if(firstResult >= 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Object[]> results = query.getResultList();
		List<QuestionItemView> views = new ArrayList<>();
		for(Object[] result:results) {
			ItemWrapper itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Number)result[4])
					.setEditableInShare((Number)result[5])
					.setMarked((Number)result[6])
					.setRater((Number)result[7])
					.setRating((Double)result[8])
					.setNumberOfRatings((Number)result[9])
					.create();
			views.add(itemWrapper);
		}
		return views;
	}
		
	public QuestionItemView getItem(Long itemKey, Identity identity, Long restrictToPoolKey, Long restrictToGroupKey) {
		if (itemKey == null || identity == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select item, ")
			.append(" (select count(sgmi.key) from ") .append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			.append("   where sgmi.identity.key=:identityKey and sgmi.securityGroup=ownerGroup")
			.append(" ) as owners,")
		    .append(" (select count(teacherCompetence.key) from ctaxonomycompetence teacherCompetence")
			.append("   where taxonomyLevel.materializedPathKeys like concat(teacherCompetence.taxonomyLevel.materializedPathKeys, '%')")
		    .append("     and teacherCompetence.identity.key=:identityKey")
		    .append("     and teacherCompetence.type='").append(TaxonomyCompetenceTypes.teach).append("'")
		    .append(" ) as teacher,")
		    .append(" (select count(managerCompetence.key) from ctaxonomycompetence managerCompetence")
			.append("   where taxonomyLevel.materializedPathKeys like concat(managerCompetence.taxonomyLevel.materializedPathKeys, '%')")
		    .append("     and managerCompetence.identity.key=:identityKey")
		    .append("     and managerCompetence.type='").append(TaxonomyCompetenceTypes.manage).append("'")
		    .append(" ) as manager,")
			.append(" (select count(p2item.key) from qpool2item p2item")
			.append("    where p2item.item.key=item.key")
			.append("      and p2item.editable=true");
		if (restrictToPoolKey != null) {
			sb.append(" and p2item.pool.key=:restrictToPoolKey");
		}
		sb.append(" ) as pools,")
			.append(" (select count(sItem.key) from qshareitem sItem")
			.append("    where sItem.item.key=item.key")
			.append("      and sItem.editable=true");
		if (restrictToGroupKey != null) {
			sb.append(" and sItem.resource=:restrictToGroupKey");
		}
		sb.append(" ) as groups,")
			.append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
			.append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
			.append(" ) as marks,")
			.append(" (select count(userRating.key) from userrating as userRating")
			.append("   where userRating.resId=item.key and userRating.resName='QuestionItem'")
			.append("     and userRating.creator.key=:identityKey")
			.append(" ) as numberOfRatingsIdentity,")
			.append(" (select avg(avgRating.rating) from userrating as avgRating")
			.append("   where avgRating.resId=item.key and avgRating.resName='QuestionItem'")
			  .append(" ) as rating,")
			  .append(" (select count(totalRating.key) from userrating as totalRating")
			  .append("   where totalRating.resId=item.key and totalRating.resName='QuestionItem'")
			  .append(" ) as numberOfRatingsTotal")
			.append(" from questionitem item")
			.append(" left join fetch item.ownerGroup ownerGroup")
			.append(" left join fetch item.taxonomyLevel taxonomyLevel")
			.append(" left join fetch item.type itemType")
			.append(" left join fetch item.educationalContext educationalContext")
			.append(" where item.key=:itemKey");

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("itemKey", itemKey)
				.setParameter("identityKey", identity.getKey());
		if (restrictToPoolKey != null) {
			query.setParameter("restrictToPoolKey", restrictToPoolKey);
		}
		if (restrictToGroupKey != null) {
			query.setParameter("restrictToGroupKey", restrictToPoolKey);
		}

		ItemWrapper itemWrapper = null;
		List<Object[]> results = query.getResultList();
		if (!results.isEmpty()) {
			Object[] result = results.get(0);
			itemWrapper = ItemWrapper.builder((QuestionItemImpl)result[0])
					.setAuthor((Number)result[1])
					.setTeacher((Number)result[2])
					.setManager((Number)result[3])
					.setEditableInPool((Number)result[4])
					.setEditableInShare((Number)result[5])
					.setMarked((Number)result[6])
					.setRater((Number)result[7])
					.setRating((Double)result[8])
					.setNumberOfRatings((Number)result[9])
					.create();
		}
		return itemWrapper;
	}
	
	private void appendIn(QueryBuilder sb, SearchQuestionItemParams params) {
		if(params.getCollection() != null) {
			sb.append(" inner join qcollection2item coll2item on (coll2item.item.key=item.key)");
		}
		if(params.getPoolKey() != null) {
			sb.append(" inner join qpool2item pool2item on (pool2item.item.key=item.key)");
		}
		if(params.getResource() != null) {
			sb.append(" inner join qshareitem shareditem on (shareditem.item.key=item.key)");
		}
	}
	
	private void appendWhere(QueryBuilder sb, SearchQuestionItemParams params) {
		if(params.getCollection() != null) {
			sb.and().append(" coll2item.collection.key=:collectionKey");
		}
		if(params.getPoolKey() != null) {
			sb.and().append(" pool2item.pool.key=:poolKey");
		}
		if(params.getResource() != null) {
			sb.and().append(" shareditem.resource.key=:resourceKey");
		}
		
		if(StringHelper.containsNonWhitespace(params.getSearchString())) {
			sb.and()
			  .append("(");
			PersistenceHelper.appendFuzzyLike(sb, "item.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "item.keywords", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "item.topic", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "item.coverage", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "item.additionalInformations", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}

		if(params.getItemTypes() != null && !params.getItemTypes().isEmpty()) {
			sb.and().append(" itemType.key in (:itemTypeKey)");
		} else if(params.getExcludedItemTypes() != null && !params.getExcludedItemTypes().isEmpty()) {
			sb.and().append(" itemType.key not in (:excludedItemTypeKeys)");
		}
		
		if(StringHelper.containsNonWhitespace(params.getTitle())) {
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "item.title", "title", dbInstance.getDbVendor());
		}
		if(StringHelper.containsNonWhitespace(params.getTopic())) {
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "item.topic", "topic", dbInstance.getDbVendor());
		}
		if(StringHelper.containsNonWhitespace(params.getOwner())) {
			sb.and()
			  .append(" exists (select mship.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as mship ")
		      .append("  inner join mship.identity as oIdent")
		      .append("  inner join oIdent.user as oUser")
	          .append("  where mship.securityGroup.key=item.ownerGroup.key and (");
			PersistenceHelper.appendFuzzyLike(sb, "oUser.firstName", "owner", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "oUser.lastName", "owner", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "oIdent.name", "owner", dbInstance.getDbVendor());
			sb.append(" ))");
		}
		if(StringHelper.containsNonWhitespace(params.getKeywords())) {
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "item.keywords", "keywords", dbInstance.getDbVendor());
		}
		if(StringHelper.containsNonWhitespace(params.getInformations())) {
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "item.additionalInformations", "informations", dbInstance.getDbVendor());
		}
		if(StringHelper.containsNonWhitespace(params.getCoverage())) {
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "item.coverage", "coverage", dbInstance.getDbVendor());
		}
		if(StringHelper.containsNonWhitespace(params.getLanguage())) {
			sb.and();
			PersistenceHelper.appendFuzzyLike(sb, "item.language", "language", dbInstance.getDbVendor());
		}
		
		if (StringHelper.containsNonWhitespace(params.getFormat())) {
			sb.and().append(" item.format=:format");
		}
		if (params.getMaxScoreFrom() != null) {
			sb.and().append(" item.maxScore>=:maxScoreFrom");
		}
		if (params.getMaxScoreTo() != null) {
			sb.and().append(" item.maxScore<=:maxScoreTo");
		}
		
		if(params.getTaxonomyLevels() != null && !params.getTaxonomyLevels().isEmpty()) {
			sb.and().append(" taxonomyLevel.key in (:taxonomyLevelKeys)");
		}
		if(StringHelper.containsNonWhitespace(params.getLikeTaxonomyLevelPath())) {
			sb.and().append(" taxonomyLevel.materializedPathKeys like :pathKeys");
		}
		if (params.isWithoutTaxonomyLevelOnly()) {
			sb.and().append(" taxonomyLevel is null");
		}
		
		if(params.getLicenseTypes() != null && !params.getLicenseTypes().isEmpty()) {
			sb.and().append(" exists (select license from license as license")
			  .append(" where license.resName=:licenseResName and license.resId=item.key and license.licenseType.key in (:licenseTypeKeys)")
			  .append(" )");
		}
		
		if(params.getAssessmentTypes() != null && !params.getAssessmentTypes().isEmpty()) {
			sb.and().append(" item.assessmentType in (:assessmentTypes)");
		}

		if(params.getLevels() != null && !params.getLevels().isEmpty()) {
			sb.and().append(" item.educationalContext.key in (:levelKeys)");
		}
		
		if(params.getQuestionStatus() != null && !params.getQuestionStatus().isEmpty()) {
			sb.and().append(" item.status in (:questionStatus)");
		}
		
		if(params.getOnlyAuthor() != null) {
			sb.and()
			  .append(" exists (").append("select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			  .append("   where sgmi.identity.key=:onlyAuthorKey and sgmi.securityGroup.key=item.ownerGroup.key")
			  .append(" )");
		}
		if(params.getAuthor() != null) {
			sb.and()
			  .append(" exists (").append("select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			  .append("   where sgmi.identity.key=:authorKey and sgmi.securityGroup.key=item.ownerGroup.key")
			  .append(" )");
		}
		if(params.getExcludeAuthor() != null) {
			sb.and()
			  .append(" not exists (").append("select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			  .append("   where sgmi.identity.key=:excludeAuthorKey and sgmi.securityGroup.key=item.ownerGroup.key")
			  .append(" )");
		}
		
		if(params.getExcludeRater() != null) {
			sb.and()
			  .append(" not exists (select rating.key from userrating as rating")
			  .append("  where rating.resId=item.key and rating.resName='QuestionItem'")
			  .append("    and rating.creator.key=:excludeRatorKey)");
		}
		
		if(params.isWithoutTaxonomyLevelOnly()) {
			sb.and().append(" taxonomyLevel is null");
		}
		
		if(params.isWithoutAuthorOnly()) {
			sb.and()
			  .append(" not exists (").append("select sgmi.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi")
			  .append("  inner join sgmi.identity ident")
			  .append("  where sgmi.securityGroup.key=item.ownerGroup.key")
			  .append(" )");
		}
		
		if(params.getItemKeys() != null && !params.getItemKeys().isEmpty()) {
			sb.and().append(" item.key in (:inKeys)");
		}
		
		if(params.isFavoritOnly()) {
			sb.and()
			  .append(" exists (select mark.key from ").append(MarkImpl.class.getName()).append(" as mark")
			  .append("   where mark.creator.key=:identityKey and mark.resId=item.key and mark.resName='QuestionItem'")
			  .append(" )");
		}
	}

	private void appendParameters(SearchQuestionItemParams params, TypedQuery<?> query) {
		if(params.getCollection() != null ) {
			query.setParameter("collectionKey", params.getCollection().getKey());
		}
		if(params.getPoolKey() != null) {
			query.setParameter("poolKey", params.getPoolKey());
		}
		if(params.getResource() != null) {
			query.setParameter("resourceKey", params.getResource().getKey());
		}
		
		if(StringHelper.containsNonWhitespace(params.getSearchString())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getSearchString());
			query.setParameter("searchString", fuzzySearch);
		}
		
		if(params.getTaxonomyLevels() != null && !params.getTaxonomyLevels().isEmpty()) {
			List<Long> levelsKeys = params.getTaxonomyLevels().stream()
					.map(TaxonomyLevelRef::getKey).toList();
			query.setParameter("taxonomyLevelKeys", levelsKeys);
		}
		
		if(StringHelper.containsNonWhitespace(params.getTitle())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getTitle());
			query.setParameter("title", fuzzySearch);
		}
		if(StringHelper.containsNonWhitespace(params.getTopic())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getTopic());
			query.setParameter("topic", fuzzySearch);
		}
		if(StringHelper.containsNonWhitespace(params.getOwner())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getOwner());
			query.setParameter("owner", fuzzySearch);
		}
		if(StringHelper.containsNonWhitespace(params.getKeywords())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getKeywords());
			query.setParameter("keywords", fuzzySearch);
		}
		if(StringHelper.containsNonWhitespace(params.getInformations())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getInformations());
			query.setParameter("informations", fuzzySearch);
		}
		if(StringHelper.containsNonWhitespace(params.getCoverage())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getCoverage());
			query.setParameter("coverage", fuzzySearch);
		}
		if(StringHelper.containsNonWhitespace(params.getLanguage())) {
			String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(params.getLanguage());
			query.setParameter("language", fuzzySearch);
		}
		
		if(params.getItemTypes() != null && !params.getItemTypes().isEmpty()) {
			List<QItemType> types = params.getItemTypes();
			if(params.getExcludedItemTypes() != null) {
				types = new ArrayList<>(types);
				types.removeAll(params.getExcludedItemTypes());
			}
			List<Long> itemTypeKeys = types.stream().map(QItemType::getKey).toList();
			query.setParameter("itemTypeKey", itemTypeKeys);
		} else if(params.getExcludedItemTypes() != null && !params.getExcludedItemTypes().isEmpty()) {
			List<Long> excludedItemTypeKeys = params.getExcludedItemTypes()
					.stream().map(QItemType::getKey).toList();
			query.setParameter("excludedItemTypeKeys", excludedItemTypeKeys);
		}
		
		if(params.getLicenseTypes() != null && !params.getLicenseTypes().isEmpty()) {
			List<Long> licenceTypeKeys = params.getLicenseTypes()
					.stream().map(LicenseType::getKey).toList();
			query.setParameter("licenseResName", "QuestionItem");
			query.setParameter("licenseTypeKeys", licenceTypeKeys);
		}
		if(params.getLevels() != null && !params.getLevels().isEmpty()) {
			List<Long> levelsKeys = params.getLevels().stream()
					.map(QEducationalContext::getKey).toList();
			query.setParameter("levelKeys", levelsKeys);
		}
		
		if(params.getAssessmentTypes() != null && !params.getAssessmentTypes().isEmpty()) {
			query.setParameter("assessmentTypes", params.getAssessmentTypes());
		}

		if(StringHelper.containsNonWhitespace(params.getFormat())) {
			query.setParameter("format", params.getFormat());
		}
		if(params.getMaxScoreFrom() != null) {
			query.setParameter("maxScoreFrom", params.getMaxScoreFrom());
		}
		if(params.getMaxScoreTo() != null) {
			query.setParameter("maxScoreTo", params.getMaxScoreTo());
		}
		
		if (StringHelper.containsNonWhitespace(params.getLikeTaxonomyLevelPath())) {
			query.setParameter("pathKeys", params.getLikeTaxonomyLevelPath() + "%");
		}
		if (params.getQuestionStatus() != null && !params.getQuestionStatus().isEmpty()) {
			List<String> status = params.getQuestionStatus().stream()
					.map(QuestionStatus::name).toList();
			query.setParameter("questionStatus", status);
		}
		
		if (params.getOnlyAuthor() != null) {
			query.setParameter("onlyAuthorKey", params.getOnlyAuthor().getKey());
		}
		if (params.getAuthor() != null) {
			query.setParameter("authorKey", params.getAuthor().getKey());
		}
		if (params.getExcludeAuthor() != null) {
			query.setParameter("excludeAuthorKey", params.getExcludeAuthor().getKey());
		}
		if (params.getExcludeRater() != null) {
			query.setParameter("excludeRatorKey", params.getExcludeRater().getKey());
		}
		
		if(params.getItemKeys() != null && !params.getItemKeys().isEmpty()) {
			query.setParameter("inKeys", params.getItemKeys());
		}
	}
	
	private void appendOrderBy(QueryBuilder sb, String itemDbRef, String taxonomyDbRef, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			String sortKey = orderBy[0].getKey();
			boolean asc = orderBy[0].isAsc();
			sb.append(" order by ");
			switch(sortKey) {
				case "itemType":
					sb.append(itemDbRef).append(".type.type ");
					appendAsc(sb, asc);
					break;
				case "mark":
				case "marks":
					sb.append("marks");
					appendAsc(sb, asc);
					break;
				case "rating":
					sb.append("rating");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
				case "numberOfRatings":
					sb.append("numberOfRatingsTotal");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
				case "keywords":
				case "coverage":
				case "additionalInformations":
					sb.append("lower(").append(itemDbRef).append(".").append(sortKey).append(")");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
				case "taxonomyLevel":
					sb.append("lower(").append(taxonomyDbRef).append(".displayName)");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
				case "taxonomyPath":
					sb.append("lower(").append(taxonomyDbRef).append(".materializedPathIdentifiers)");
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
				default:
					sb.append(itemDbRef).append(".").append(sortKey);
					appendAsc(sb, asc);
					sb.append(" nulls last");
					break;
			}
		} else {
			sb.append(" order by item.key asc ");
		}
	}
	
	private final QueryBuilder appendAsc(QueryBuilder sb, boolean asc) {
		if(asc) {
			sb.append(" asc");
		} else {
			sb.append(" desc");
		}
		return sb;
	}
}
