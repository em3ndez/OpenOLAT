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
package org.olat.course.nodes.st;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.FileUtils;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.ui.tool.AssessmentIdentiesPrintController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.component.ColorizedScoreCellRenderer;
import org.olat.modules.assessment.ui.component.ColorizedWeightedScoreCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * This specialized list of assessed identities show the certificates
 * and recertification date if configured.
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class STIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private FormLink pdfButton;

	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	
	public STIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback, showTitle);
	}
	
	public boolean isRoot() {
		return courseNode.getParent() == null;
	}

	@Override
	protected boolean isSelectable() {
		return isRoot();
	}

	@Override
	protected void initModificationDatesColumns(FlexiTableColumnModel columnsModel) {
		if(courseNode.getParent() == null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.initialLaunchDate, getSelectAction()));
		}
		super.initModificationDatesColumns(columnsModel);
	}

	@Override
	protected void initScoreColumns(FlexiTableColumnModel columnsModel) {
		if(ScoreScalingHelper.isEnabled(courseNode)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(isRoot(), IdentityCourseElementCols.weightedScore, new ColorizedWeightedScoreCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!isRoot(), IdentityCourseElementCols.score, new ColorizedScoreCellRenderer()));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.score, new ColorizedScoreCellRenderer()));
		}
	}

	@Override
	protected void initCalloutColumns(FlexiTableColumnModel columnsModel) {
		RepositoryEntryCertificateConfiguration certificateConfig = certificatesManager.getConfiguration(courseEntry);
		if(certificateConfig.isCertificateEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.certificate, new DownloadCertificateCellRenderer(getLocale())));
			if(certificateConfig.isValidityEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.certificateValidity, new DateFlexiCellRenderer(getLocale())));
			}
		}
		//no callout
	}
	
	@Override
	protected String getTableId() {
		StringBuilder id = new StringBuilder();
		id.append("st-assessment-tool-identity-list-v3-");
		if(isRoot()) {
			id.append("root");
			if(ScoreScalingHelper.isEnabled(getCourseEnvironment())) {
				id.append("-wscaling");
			} else {
				id.append("-wo");
			}
		} else {
			id.append("st");
		}
		return id.toString();
	}

	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		initGradeScaleEditButton(formLayout);
		initBulkApplyGradeTool(formLayout);
		initBulkAwardBadgeTool(ureq, formLayout);

		if(courseNode.getParent() == null && pdfModule.isEnabled()) {
			pdfButton = uifactory.addFormLink("bulk.pdf", formLayout, Link.BUTTON); 
			pdfButton.setIconLeftCSS("o_icon o_icon_tool_pdf");
			tableEl.addBatchButton(pdfButton);
		}
		
		if(courseNode.getParent() == null && getAssessmentCallback().canResetData()) {
			initResetPassedOverriddenButton(formLayout);
			initResetDataTool(formLayout);
		}
		
		tableEl.setMultiSelect(tableEl.isBatchButtonAvailable());
	}

	@Override
	public void reload(UserRequest ureq) {
		Map<Long, Date> initialLaunchDates = userInfosMgr
				.getInitialLaunchDates(getCourseRepositoryEntry().getOlatResource());
		super.reload(ureq);
		
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			Date initialLaunchDate = initialLaunchDates.get(row.getIdentityKey());
			row.setInitialCourseLaunchDate(initialLaunchDate);
		}

		AssessmentToolContainer toolContainer = getToolContainer();
		if(toolContainer.getCertificateMap() == null) {
			List<CertificateLight> certificates = certificatesManager.getLastCertificates(getCourseRepositoryEntry().getOlatResource());
			ConcurrentMap<Long, CertificateLight> certificateMap = new ConcurrentHashMap<>();
			for(CertificateLight certificate:certificates) {
				certificateMap.put(certificate.getIdentityKey(), certificate);
			}
			toolContainer.setCertificateMap(certificateMap);
		}
		usersTableModel.setCertificateMap(toolContainer.getCertificateMap());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(pdfButton == source) {
			doExportPdf(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	private void doExportPdf(UserRequest ureq) {
		List<Long> assessesIdentityKeys = getMultiSelectionIdentityKeys();
		if (assessesIdentityKeys.isEmpty()) {
			showWarning("bulk.no.selection");
			return;
		}
		ControllerCreator printControllerCreator = (lureq, lwControl) -> new AssessmentIdentiesPrintController(lureq, lwControl, courseEntry, coachCourseEnv,
				assessesIdentityKeys, getAssessmentCallback());
		String title = getPdfTitle();
		MediaResource resource = pdfService.convert(title, getIdentity(), printControllerCreator,
				getWindowControl(), PdfOutputOptions.defaultOptions());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	private List<Long> getMultiSelectionIdentityKeys() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<Long> identityKeys = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if (row != null) {
				identityKeys.add(row.getIdentityKey());
			}
		}
		return identityKeys;
	}
	
	private String getPdfTitle() {
		StringBuilder sb = new StringBuilder();
		sb.append(translate("bulk.pdf.prefix"));
		sb.append("_");
		sb.append(courseEntry.getDisplayname());
		return FileUtils.normalizeFilename(sb.toString());
	}
}
