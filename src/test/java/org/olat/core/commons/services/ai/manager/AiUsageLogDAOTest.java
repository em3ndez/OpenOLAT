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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.model.AiUsageLogImpl;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class AiUsageLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	@Test
	public void createLogEntry_success() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-usage-log-1");

		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setIdentity(identity);
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setResourceType("QTI-Item");
		log.setResourceId(123L);
		log.setResourceSubId("resource-sub-123");
		log.setStatus("SUCCESS");
		log.setModelProvider("openai");
		log.setRequestModel("gpt-4");
		log.setRequestTemperature(0.5);
		log.setRequestTopP(0.9);
		log.setRequestMaxOutputTokens(4000L);
		log.setResponseId("response-123");
		log.setResponseModel("gpt-4");
		log.setResponseFinishReason("STOP");
		log.setInputTokens(100L);
		log.setOutputTokens(50L);
		log.setTotalTokens(150L);
		log.setDurationMillis(1000L);
		log.setRequestNumMessages(2L);
		log.setRequestTextLength(512L);
		log.setCacheCreationInputTokens(10L);

		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(created);
		Assert.assertNotNull(created.getKey());
		Assert.assertNotNull(created.getCreationDate());
		Assert.assertEquals(identity.getKey(), created.getIdentity().getKey());
		Assert.assertEquals("test-feature", created.getAiFeature());
		Assert.assertEquals("SUCCESS", created.getStatus());
		Assert.assertEquals(Long.valueOf(100L), created.getInputTokens());
		Assert.assertEquals(Long.valueOf(2L), created.getRequestNumMessages());
		Assert.assertEquals(Long.valueOf(512L), created.getRequestTextLength());
		Assert.assertEquals(Long.valueOf(10L), created.getCacheCreationInputTokens());
	}

	@Test
	public void createLogEntry_nullIdentity() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setStatus("SUCCESS");
		log.setDurationMillis(500L);

		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(created);
		Assert.assertNotNull(created.getKey());
		Assert.assertNull(created.getIdentity());
		Assert.assertEquals("SUCCESS", created.getStatus());
	}

	@Test
	public void createLogEntry_failed() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setStatus("FAILED");
		log.setErrorCode("ApiException");
		log.setErrorMessage("API connection failed");
		log.setDurationMillis(2000L);

		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(created);
		Assert.assertNotNull(created.getKey());
		Assert.assertEquals("FAILED", created.getStatus());
		Assert.assertEquals("ApiException", created.getErrorCode());
		Assert.assertEquals("API connection failed", created.getErrorMessage());
	}

	@Test
	public void updateInvocationFields() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setStatus("SUCCESS");
		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		aiUsageLogDAO.updateInvocationFields(created.getKey(), "inv-123", "MCQuestionAiService", "generateQuestions");
		dbInstance.commitAndCloseSession();

		AiUsageLogImpl reloaded = dbInstance.getCurrentEntityManager().find(AiUsageLogImpl.class, created.getKey());
		Assert.assertEquals("inv-123", reloaded.getInvocationId());
		Assert.assertEquals("MCQuestionAiService", reloaded.getServiceInterface());
		Assert.assertEquals("generateQuestions", reloaded.getServiceMethod());
	}
}
