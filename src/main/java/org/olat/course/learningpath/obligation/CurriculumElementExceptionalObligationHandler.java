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
package org.olat.course.learningpath.obligation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.Structure;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementExceptionalObligationHandler implements ExceptionalObligationHandler {
	
	public static final String TYPE = "curriculum.element";
	
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getSortValue() {
		return 20;
	}

	@Override
	public boolean isEnabled() {
		return curriculumModule.isEnabled();
	}

	@Override
	public String getAddI18nKey() {
		return "exceptional.obligation.curriculum.element.add";
	}
	
	@Override
	public boolean isShowAdd(RepositoryEntry courseEntry) {
		return isEnabled() && curriculumService.getCuriculumElementCount(courseEntry).longValue() > 0;
	}

	@Override
	public String getDisplayType(Translator translator, ExceptionalObligation exceptionalObligation) {
		return translator.translate("exceptional.obligation.curriculum.element.type");
	}

	@Override
	public String getDisplayName(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry) {
		if (exceptionalObligation instanceof CurriculumElementExceptionalObligation curEleExceptionalObligation) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(curEleExceptionalObligation.getCurriculumElementRef());
			if (curriculumElement != null) {
				return StringHelper.escapeHtml(curriculumElement.getDisplayName());
			}
		}
		return null;
	}
	
	@Override
	public String getDisplayText(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry) {
		String displayName = getDisplayName(translator, exceptionalObligation, courseEntry);
		if (displayName != null) {
			return translator.translate("exceptional.obligation.curriculum.element.display", new String[] {displayName});
		}
		return null;
	}

	@Override
	public boolean hasScoreAccountingTrigger() {
		return true;
	}

	@Override
	public ScoreAccountingTriggerData getScoreAccountingTriggerData(ExceptionalObligation exceptionalObligation) {
		if (exceptionalObligation instanceof CurriculumElementExceptionalObligation) {
			CurriculumElementExceptionalObligation curEleExceptionalObligation = (CurriculumElementExceptionalObligation)exceptionalObligation;
			ScoreAccountingTriggerData data = new ScoreAccountingTriggerData();
			data.setCurriculumElementRef(curEleExceptionalObligation.getCurriculumElementRef());
			return data;
		}
		return null;
	}

	@Override
	public boolean matchesIdentity(ExceptionalObligation exceptionalObligation, Identity identity,
			ObligationContext obligationContext, RepositoryEntry courseEntry, Structure runStructure, ScoreAccounting scoreAccounting) {
		if (exceptionalObligation instanceof CurriculumElementExceptionalObligation) {
			CurriculumElementExceptionalObligation curEleExceptionalObligation = (CurriculumElementExceptionalObligation)exceptionalObligation;
			return obligationContext.isParticipant(identity, curEleExceptionalObligation.getCurriculumElementRef());
		}
		return false;
	}

	@Override
	public ExceptionalObligationController createCreationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode) {
		return new CurriculumElementExceptionalObligationController(ureq, wControl, courseEntry);
	}

}
