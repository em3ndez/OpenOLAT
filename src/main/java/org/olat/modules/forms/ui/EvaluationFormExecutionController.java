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
package org.olat.modules.forms.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateConfigBuilder;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.nodes.ms.MSEvaluationBackController;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.ValidatingController;
import org.olat.modules.ceditor.ui.ValidationMessage;
import org.olat.modules.ceditor.ui.ValidationMessage.Level;
import org.olat.modules.ceditor.ui.component.PageFragmentsElementImpl;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.handler.AllHandlerPageProvider;
import org.olat.modules.forms.handler.EvaluationFormElementHandler;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.rules.RulesEngine;
import org.olat.modules.forms.rules.VisibilityHandler;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.ExecutionFragment;
import org.olat.modules.forms.ui.model.ExecutionIdentity;
import org.olat.modules.forms.ui.model.Progress;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.04.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EvaluationFormExecutionController extends FormBasicController implements ValidatingController {

	private static final Logger log = Tracing.createLoggerFor(EvaluationFormExecutionController.class);
	
	private static final EmptyStateConfig EMPTY_STATE_DEFAULTS = defaultEmptyState().build();
	public static final String EMPTY_STATE_EDIT_HINT = "empty.state.edit.hint";
	public static final String EMPTY_STATE_EDIT_BUTTON = "empty.state.edit.button";
	public static final EmptyStateConfigBuilder defaultEmptyState() {
		return EmptyStateConfig.builder()
				.withMessageI18nKey("empty.state.message")
				.withIconCss("o_FileResource-FORM_icon");
	}

	private final Map<String, EvaluationFormElementHandler> handlerMap = new HashMap<>();
	private final List<ExecutionFragment> fragments = new ArrayList<>();
	private FormLink saveLink;
	private FormSubmit doneLink;
	private FormCancel cancelLink;
	private DialogBoxController confirmDoneCtrl;
	private PageFragmentsElementImpl fragmentsEl;

	private final Form form;
	private final DataStorage storage;
	private final ExecutionIdentity executionIdentity;
	private final Component header;
	private EmptyStateConfig emptyStateConfig;
	private EmptyState emptyState;
	private boolean readOnly;
	private boolean showDoneButton;
	private boolean showCancelButton;
	private final boolean doneSavesOnly;
	private boolean isRubricAssessment;

	private EvaluationFormSession session;
	private EvaluationFormResponses responses;
	

	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session,
			EmptyStateConfig emptyStateConfig) {
		this(ureq, wControl, null, null, session, null, null, false, true, false, false, emptyStateConfig);
	}

	/**
	 * Optimized to use already loaded responses and form.
	 * 
	 */
	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session,
			EvaluationFormResponses responses, Form form, DataStorage storage, Component header) {
		this(ureq, wControl, form, storage, session, null, header, false, true, false, false, null);
		this.responses = responses;
	}

	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session,
			boolean readOnly, boolean showDoneButton, boolean doneSavesOnly, EmptyStateConfig emptyState) {
		this(ureq, wControl, null, null, session, null, null, readOnly, showDoneButton, false, doneSavesOnly, emptyState);
	}

	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			EvaluationFormSession session, ExecutionIdentity executionIdentity, Component header, boolean readOnly,
			boolean showDoneButton, boolean showCancelButton, boolean doneSavesOnly, EmptyStateConfig emptyStateConfig) {
		super(ureq, wControl, "execute");

		this.session = session;
		this.header = header;
		this.emptyStateConfig = emptyStateConfig != null? emptyStateConfig: EMPTY_STATE_DEFAULTS;
		this.readOnly = readOnly;
		this.showDoneButton = showDoneButton;
		this.showCancelButton = showCancelButton;
		this.doneSavesOnly = doneSavesOnly;

		if (form != null) {
			this.form = form;
			this.storage = storage;
		} else {
			RepositoryEntry formEntry = session.getSurvey().getFormEntry();
			this.form = evaluationFormManager.loadForm(formEntry);
			this.storage = evaluationFormManager.loadStorage(formEntry);
		}
		
		if (executionIdentity != null) {
			this.executionIdentity = executionIdentity;
		} else {
			this.executionIdentity = ExecutionIdentity.ofIdentity(getIdentity());
		}

		initForm(ureq);
	}

	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, File formFile,
			DataStorage storage, EmptyStateConfig emptyStateConfig) {
		super(ureq, wControl, "execute");

		this.form = (Form) XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		this.storage = storage;

		this.session = null;
		this.responses = null;
		this.header = null;
		this.emptyStateConfig = emptyStateConfig != null? emptyStateConfig: EMPTY_STATE_DEFAULTS;
		this.readOnly = false;
		this.showDoneButton = false;
		this.doneSavesOnly = false;
		this.executionIdentity = ExecutionIdentity.ofIdentity(getIdentity());

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		AllHandlerPageProvider provider = new AllHandlerPageProvider(form, storage);
		for (EvaluationFormElementHandler handler : provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}

		fragmentsEl = new PageFragmentsElementImpl("fragments");
		formLayout.add("fragments", fragmentsEl);

		ajustFromSession();
		loadElements(ureq);
		loadResponses(ureq);
		initRulesEngine();
		propagateReadOnly();

		if (header != null) {
			flc.put("header", header);
		}
		
		emptyState = EmptyStateFactory.create("empty.state", flc.getFormItemComponent(), this, emptyStateConfig);
		
		boolean notAnonymous = session != null && session.getParticipation() != null
				&& !session.getParticipation().isAnonymous();
		boolean anonymous = !notAnonymous;
		flc.contextPut("anonymous", Boolean.valueOf(showDoneButton && anonymous));
		
		// force it to have always the same settings
		mainForm.setMultipartEnabled(true);

		String labelKey = doneSavesOnly? "save": "save.as.done";
		doneLink = uifactory.addFormSubmitButton("save.as.done", labelKey, formLayout);
		saveLink = uifactory.addFormLink("save.intermediate", "save.intermediate", null, flc, Link.BUTTON);
		cancelLink = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		showHideButtons();
		if(!doneLink.isVisible() && !saveLink.isVisible()) {
			flc.getRootForm().setHideDirtyMarkingMessage(true);
		}
	}
	
	public void setDoneI18nKey(String i18nKey) {
		doneLink.setI18nKey(i18nKey, null);
	}
	
	public void setSaveDisplayText(String text) {
		saveLink.getComponent().setCustomDisplayText(text);
	}

	private void ajustFromSession() {
		if (session == null)
			return;

		if (session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
			readOnly = true;
			showDoneButton = false;
		} else {
			Identity executor = null;
			if (session.getParticipation() != null) {
				executor = session.getParticipation().getExecutor();
			}
			if (executor != null && !executor.getKey().equals(executionIdentity.getIdentityKey())) {
				flc.contextPut("messageNotDone", Boolean.TRUE);
			}
		}

	}

	private void loadElements(UserRequest ureq) {
		fragments.clear();
		List<AbstractElement> elements = form.getElements();
		if (elements.isEmpty()) {
			flc.contextPut("noElements", Boolean.TRUE);
		}
		
		for (AbstractElement element : elements) {
			EvaluationFormElementHandler handler = handlerMap.get(element.getType());
			if (handler != null) {
				EvaluationFormExecutionElement executionElement = handler.getExecutionElement(ureq, getWindowControl(),
						mainForm, element, executionIdentity);
				if (executionElement != null) {
					String cmpId = "cpt-" + CodeHelper.getRAMUniqueID();
					fragments.add(new ExecutionFragment(handler.getType(), cmpId, executionElement, element));
				}
			}
		}
		fragmentsEl.setFragments(fragments);
	}
	
	private void loadResponses(UserRequest ureq) {
		if (session == null) return;
		
		if (responses == null) {
			SessionFilter filter = SessionFilterFactory.create(session);
			responses = evaluationFormManager.loadResponsesBySessions(filter);
		}
		
		for (ExecutionFragment fragment : fragments) {
			fragment.initResponse(ureq, session, responses);
		}
	}
	
	private void initRulesEngine() {
		RulesEngine rulesEngine = new RulesEngine(form.getRules());
		VisibilityHandler.registerListeners(rulesEngine, form, fragments);
		for (ExecutionFragment fragment : fragments) {
			fragment.initRulesEngine(rulesEngine);
		}
	}

	private void propagateReadOnly() {
		for (ExecutionFragment fragment : fragments) {
			fragment.setReadOnly(readOnly);
		}
	}

	private void showHideButtons() {
		saveLink.setVisible(!readOnly && !doneSavesOnly && session != null);
		doneLink.setVisible(showDoneButton);
		cancelLink.setVisible(showCancelButton);
	}

	@Override
	protected void doDispose() {
		for (ExecutionFragment fragment : fragments) {
			fragment.dispose();
		}
        super.doDispose();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == emptyState && event == EmptyState.EVENT) {
			fireEvent(ureq, event);
		} else {
			isRubricAssessment = source.getListenerInfo().contains(MSEvaluationBackController.class.getSimpleName());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (saveLink == source) {
			mainForm.forceSubmittedAndValid();
			boolean saved = doSaveResponses(ureq);
			if (saved) {
				Double progress = doCalculateProgress();
				fireEvent(ureq, new ProgressEvent(progress));
			}
		} else if(doneLink != source && !flc.getRootForm().isHideDirtyMarkingMessage()) {
			markDirty();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmDoneCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				saveAsDone(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Suppress unwanted submit e.g. by pressing enter
		if (!doneLink.isVisible()) return;
		
		boolean responsesSaved = doSaveResponses(ureq);
		if (responsesSaved && !doneSavesOnly) {
			doConfirmDone(ureq);
		}
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		areAllResponded(messages);
		for (ExecutionFragment fragment : fragments) {
			fragment.validate(ureq, messages);
		}
		return messages.isEmpty();
	}

	private void areAllResponded(List<ValidationMessage> messages) {
		boolean allResponded = true;
		for (ExecutionFragment fragment : fragments) {
			if (!fragment.hasResponse()) {
				allResponded &= false;
			}
		}
		if (!allResponded) {
			String msg = translate("warning.form.not.completed");
			messages.add(new ValidationMessage(Level.warning, msg));
		}
	}

	private boolean doSaveResponses(UserRequest ureq) {
		session = evaluationFormManager.loadSessionByKey(session);
		if (session == null) {
			showWarning("error.cannot.save");
			responses = null; // reload
			initForm(ureq);
			return false;
		}
		
		if (session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
			showWarning("error.session.done");
			responses = null; // reload
			initForm(ureq);
			return false;
		}
		
		boolean allSaved = true;
		for (ExecutionFragment fragment : fragments) {
			try {
				fragment.save(ureq, session);
			} catch (Exception e) {
				log.error("Saving evaluation form response failed!", e);
				allSaved = false;
			}
		}
		try {
			dbInstance.commit();
		} catch (Exception e) {
			log.error("Commiting saved evaluation form response failed!", e);
			allSaved = false;
		}
		if (!allSaved) {
			showError("error.cannot.save");
		}
		return allSaved;
	}
	
	private Double doCalculateProgress() {
		// The form is not done until it is submitted.
		// This step is additionally counted with the progress value 1.
		int progressMax = 1;
		int currentProgress = 0;
		for (ExecutionFragment fragment : fragments) {
			Progress progress = fragment.getProgress();
			currentProgress += progress.getCurrent();
			progressMax += progress.getMax();
		}
		return Double.valueOf((double)currentProgress / progressMax);
	}

	private void doConfirmDone(UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		List<ValidationMessage> messages = new ArrayList<>();
		validate(ureq, messages);
		if (!messages.isEmpty()) {
			for (ValidationMessage message : messages) {
				sb.append("<p class='o_warning_with_icon'>").append(message.getMessage()).append("</p>");
			}
		}
		String title;
		if (isRubricAssessment) {
			title = translate("confirm.done.title.rubric");
			sb.append("<p>").append(translate("confirm.done.rubric")).append("</p>");
		} else {
			title = translate("save.as.done");
			sb.append("<p>").append(translate("confirm.done")).append("</p>");
		}

		List<String> buttons = new ArrayList<>();
		buttons.add(translate("save.as.done"));
		buttons.add(translate("cancel"));
		confirmDoneCtrl = activateGenericDialog(ureq, title, sb.toString(), buttons, confirmDoneCtrl);
		confirmDoneCtrl.setPrimary(0);
	}

	private void saveAsDone(UserRequest ureq) {
		session = evaluationFormManager.finishSession(session);
		dbInstance.commit();
		readOnly = true;
		propagateReadOnly();
		showDoneButton = false;
		showHideButtons();
		flc.setDirty(true);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
