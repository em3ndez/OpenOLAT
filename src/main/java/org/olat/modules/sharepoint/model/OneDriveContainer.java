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
package org.olat.modules.sharepoint.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.modules.sharepoint.PermissionsDelegate;
import org.olat.modules.sharepoint.manager.ReadWritePermissionsDelegate;
import org.olat.modules.sharepoint.manager.SharePointDAO;

import com.azure.core.credential.TokenCredential;

/**
 * 
 * Initial date: 7 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OneDriveContainer extends AbstractRootDriveContainer {
	
	private static final PermissionsDelegate WRITE_PERMISSIONS = new ReadWritePermissionsDelegate();
	
	private MicrosoftDrive oneDrive;
	
	public OneDriveContainer(SharePointDAO sharePointDao, List<String> exclusionsLabels, TokenCredential tokenProvider) {
		super(null, "OneDrive", sharePointDao, exclusionsLabels, WRITE_PERMISSIONS, tokenProvider);
	}
	
	@Override
	public String getIconCSS() {
		return "o_icon_onedrive";
	}

	@Override
	public MicrosoftDrive getDrive() {
		if(oneDrive == null) {
			oneDrive = sharePointDao.getMeOneDrive(tokenProvider);
		}
		return oneDrive;
	}
	
	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		MicrosoftDrive drive = getDrive();
		if(drive != null) {
			List<MicrosoftDriveItem> driveItems = sharePointDao.searchDriveItems(drive.drive(), "", tokenProvider);
			return toVFS(drive, driveItems);
		}
		return new ArrayList<>();
	}
	
	@Override
	public VFSStatus canDescendants() {
		return VFSStatus.YES;
	}
}
