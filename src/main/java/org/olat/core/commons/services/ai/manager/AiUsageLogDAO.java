/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.ai.manager;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiUsageLog;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.AiUsageLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Data access object for AI usage logging.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiUsageLogDAO {

	@Autowired
	private DB dbInstance;

	public AiUsageLogImpl create(AiUsageLogImpl log) {
		log.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(log);
		return log;
	}

	public void createErrorLog(String spiId, String modelName, String aiFeature, AiUsageContext context,
			long durationMillis, Exception error) {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setModelProvider(spiId);
		log.setRequestModel(modelName);
		log.setAiFeature(aiFeature);
		log.setDurationMillis(durationMillis);
		log.setStatus(AiUsageLog.STATUS_FAILED);
		log.setErrorCode(error.getClass().getSimpleName());
		log.setErrorMessage(error.getMessage());
		if (context != null) {
			log.setUsageContextType(context.usageContextType());
			log.setUsageContextId(context.usageContextId());
			log.setIdentity(context.identity());
			log.setResourceType(context.resourceType());
			log.setResourceId(context.resourceId());
			log.setResourceSubId(context.resourceSubId());
			if (context.locale() != null) {
				log.setLocale(context.locale().toString());
			}
		}
		create(log);
	}

	public void updateInvocationFields(Long key, String invocationId, String serviceInterface, String serviceMethod) {
		if (key == null) {
			return;
		}
		AiUsageLogImpl log = dbInstance.getCurrentEntityManager().find(AiUsageLogImpl.class, key);
		if (log != null) {
			log.setInvocationId(invocationId);
			log.setServiceInterface(serviceInterface);
			log.setServiceMethod(serviceMethod);
		}
	}

}
