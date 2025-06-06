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

package org.olat.course.nodes;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.nodes.sp.SPPeekviewController;
import org.olat.course.nodes.sp.SPRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen
 *         GmbH</a>)
 */
public class SPCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(SPCourseNode.class);

	private static final long serialVersionUID = -4565145351110778757L;
	public static final String TYPE = "sp";

	public SPCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		SPEditController childTabCntrllr = new SPEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController nodeEditController = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
		// special case: listen to sp edit controller, must be informed when the short title is being modified
		nodeEditController.addControllerListener(childTabCntrllr); 
		return nodeEditController;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		VFSContainer container = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
		SPRunController runController = new SPRunController(wControl, ureq, userCourseEnv, this, container);
		return new NodeRunConstructionResult(runController);
	}

	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		if (nodeSecCallback.isAccessible()) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class,
					userCourseEnv.getCourseEnvironment().getCourseResourceableId());
			ModuleConfiguration config = getModuleConfiguration();
			return new SPPeekviewController(ureq, wControl, userCourseEnv, config, ores);
		} else {
			// use standard peekview
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback, small);
		}
	}

	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null, null).getRunController();
	}

	@Override
	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		String file = (String) getModuleConfiguration().get(SPEditController.CONFIG_KEY_FILE);
		boolean isValid = file != null;
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			// FIXME: refine statusdescriptions by moving the statusdescription
			// generation to the MSEditForm
			String shortKey = "error.missingfile.short";
			String longKey = "error.missingfile.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(SPEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(SPEditController.PANE_TAB_SPCONFIG);
		}
		return sd;

	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(SPEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		sds.forEach(s -> s.setActivateableViewIdentifier(SPEditController.PANE_TAB_SPCONFIG));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		SPCourseNode cNode = (SPCourseNode)super.createInstanceForCopy(isNewTitle, course, author);
		
		VFSContainer courseFolderCont = course.getCourseEnvironment()
				.getCourseFolderContainer(CourseContainerOptions.withoutElements());
		String filePath = getModuleConfiguration().getStringValue(SPEditController.CONFIG_KEY_FILE);
		VFSItem sourceItem = courseFolderCont.resolve(filePath);
		if(filePath != null && !(filePath.contains("/_sharedfolder")
				&& course.getCourseEnvironment().getCourseConfig().isSharedFolderReadOnlyMount())
				&& sourceItem instanceof VFSLeaf sourceLeaf) {
			VFSContainer container = sourceItem.getParentContainer();
			if(container != null) {
				String copyName = nameOfFileCopy(sourceLeaf, container);
				VFSLeaf copyLeaf = container.createChildLeaf(copyName);
				if(copyLeaf != null && VFSManager.copyContent(sourceLeaf, copyLeaf, true, author)) {
					String copyPath = VFSManager.getRelativeItemPath(copyLeaf, courseFolderCont, "");
					if(!copyPath.startsWith("/")) {
						copyPath = "/" + copyPath;
					}
					cNode.getModuleConfiguration().setStringValue(SPEditController.CONFIG_KEY_FILE, copyPath);
				}
			}
		}
		return cNode;
	}
	
	private String nameOfFileCopy(VFSLeaf sourceLeaf, VFSContainer container) {
		String name = sourceLeaf.getName();
		int extension = name.lastIndexOf('.');
		String filename = name;
		if(extension > 0) {
			filename = name.substring(0, extension) + "_copy_" + name.substring(extension);
		}
		return VFSManager.rename(container, filename);
	}
	
	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		
		if(settings.getCopyType() == CopyType.copy) {
			VFSContainer sourceCourseFolderCont = sourceCourse.getCourseEnvironment()
					.getCourseFolderContainer(CourseContainerOptions.withoutElements());
			VFSContainer targetCourseFolderCont = course.getCourseEnvironment()
					.getCourseFolderContainer(CourseContainerOptions.withoutElements());
			
			String filePath = sourceCourseNode.getModuleConfiguration().getStringValue(SPEditController.CONFIG_KEY_FILE);
			VFSLeaf sourceLeaf = (VFSLeaf)sourceCourseFolderCont.resolve(filePath);
			
			String targetRelPath = envMapper.getRenamedPathOrSource(filePath);
			VFSItem targetItem = targetCourseFolderCont.resolve(targetRelPath);
			if(targetItem == null && sourceLeaf.exists()) {
				// document is copied by the process before this step
				log.warn("Single page's file not copied: {}", targetRelPath);
			}
			if(StringHelper.containsNonWhitespace(targetRelPath)) {
				targetRelPath = VFSManager.appendLeadingSlash(targetRelPath);
				getModuleConfiguration().setStringValue(SPEditController.CONFIG_KEY_FILE, targetRelPath);
			}
		}
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setBooleanEntry(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, false);
			
			DeliveryOptions defaultOptions = DeliveryOptions.defaultWithGlossary();
			config.set(SPEditController.CONFIG_KEY_DELIVERYOPTIONS, defaultOptions);
			
			// new since config version 3
			config.setConfigurationVersion(4);
		} else {
			config.remove(NodeEditController.CONFIG_INTEGRATION);
			int version = config.getConfigurationVersion();
			if (version < 2) {
				// use values accoring to previous functionality
				config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
				config.setBooleanEntry(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, Boolean.FALSE.booleanValue());
				config.setConfigurationVersion(2);
			}
			if(version < 4) {
				if(config.get(SPEditController.CONFIG_KEY_DELIVERYOPTIONS) == null) {
					DeliveryOptions defaultOptions = DeliveryOptions.defaultWithGlossary();
					config.set(SPEditController.CONFIG_KEY_DELIVERYOPTIONS, defaultOptions);
				}
				config.setConfigurationVersion(4);
			}
			
			//there was a version 3 but all keys new in this version have been removed
		}
	}
	
}
