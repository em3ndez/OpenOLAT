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
package org.olat.repository.wizard.ui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.iq.QTI21EditForm;
import org.olat.course.nodes.ms.MSIdentityListCourseNodeController;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.AssessmentTestInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.wizard.ui.RepositoryEntryOverviewController.Figure;
import org.olat.repository.wizard.ui.RepositoryEntryOverviewController.MoreFigures;

/**
 * 
 * Initial date: 18 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QTI21Figures implements MoreFigures {

	@Override
	public List<Figure> getMainFigures(RepositoryEntry entry, Locale locale) {
		if (entry == null) return Collections.emptyList();
		
		QTI21Service qti21Service = CoreSpringFactory.getImpl(QTI21Service.class);
		Translator translator = Util.createPackageTranslator(QTI21EditForm.class, locale);
		translator = Util.createPackageTranslator(MSIdentityListCourseNodeController.class, locale, translator);
		
		Double minValue = null;
		Double maxValue = null;
		Double cutValue = null;
		
		AssessmentTestInfos assessmentTestInfos = qti21Service.getAssessmentTestInfos(entry);
		if(assessmentTestInfos != null) {
			maxValue = assessmentTestInfos.estimatedMaxScore();
			if(maxValue == null) {
				maxValue = assessmentTestInfos.maxScore();
			}
			cutValue = assessmentTestInfos.cutValue();
			minValue = assessmentTestInfos.minScore();
		}
		String formatedMinValue = minValue == null ? "-" : AssessmentHelper.getRoundedScore(minValue);
		String formatedMaxValue = maxValue == null ? "-" : AssessmentHelper.getRoundedScore(maxValue);
		
		QTI21DeliveryOptions deliveryOptions = qti21Service.getDeliveryOptions(entry);
		PassedType passedType = deliveryOptions.getPassedType(cutValue);
		String passedTypeValue;
		switch (passedType) {
		case cutValue:
			passedTypeValue = translator.translate("score.passed.cut.value", new String[] { AssessmentHelper.getRoundedScore(cutValue) });
			break;
		case manually:
			passedTypeValue = translator.translate("score.passed.manually");
			break;
		default:
			passedTypeValue = translator.translate("score.passed.none");
			break;
		}
		
		return List.of(
				new Figure(translator.translate("score.min"), formatedMinValue),
				new Figure(translator.translate("score.max"), formatedMaxValue),
				new Figure(translator.translate("score.passed"), passedTypeValue));
	}

}
