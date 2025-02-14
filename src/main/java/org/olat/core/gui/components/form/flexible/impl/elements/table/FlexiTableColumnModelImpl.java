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
* <p>
*/ 

package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Christian Guretzki
 */
public class FlexiTableColumnModelImpl implements FlexiTableColumnModel {

	private final List<FlexiColumnModel> columnModelList = new ArrayList<>();
	
	/**
	 * @return
	 */
	@Override
	public int getColumnCount() {
		return columnModelList.size();
	}

	/**
	 * @return
	 */
	@Override
	public FlexiColumnModel getColumnModel(int column) {
		return columnModelList.get(column);
	}

	@Override
	public void addFlexiColumnModel(FlexiColumnModel columnModel) {
		columnModelList.add(columnModel);
	}
	
	@Override
	public FlexiColumnModel getColumnModelByIndex(int columnIndex) {
		for (FlexiColumnModel columnModel : columnModelList) {
			if(columnModel.getColumnIndex() == columnIndex) {
				return columnModel;
			}
		}
		return null;
	}

	@Override
	public void addFlexiColumnModel(FlexiColumnModel... columnModels) {
		if (columnModels == null || columnModels.length == 0) {
			return;
		}
		
		for (FlexiColumnModel columnModel : columnModels) {
			columnModelList.add(columnModel);
		}
	}

	@Override
	public void clear() {
		columnModelList.clear();
	}
	
	
}