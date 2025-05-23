/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.topicbroker.manager;

import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.modules.topicbroker.TBBroker;

/**
 * 
 * Initial date: 3 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(TopicBrokerMediaResource.class);
	
	public static final String TOPIC_FILES_PATH = "topic files";

	private final TBBroker broker;
	private final TopicBrokerFilesExport filesExport;

	private final TopicBrokerExcelExport excelExport;
	private final Map<String, TopicBrokerExcelExport> topicIdentToExcelExport;

	public TopicBrokerMediaResource(TBBroker broker, TopicBrokerFilesExport filesExport,
			TopicBrokerExcelExport excelExport, Map<String, TopicBrokerExcelExport> topicIdentToExcelExport) {
		this.broker = broker;
		this.filesExport = filesExport;
		this.excelExport = excelExport;
		this.topicIdentToExcelExport = topicIdentToExcelExport;
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(StringHelper.transformDisplayNameToFileSystemName("topicbroker") + ".zip");
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			String filesPath = TOPIC_FILES_PATH + "/";
			filesExport.export(zout, filesPath);
			
			String topicsPath = "topic enrollments/";
			if (topicIdentToExcelExport != null && !topicIdentToExcelExport.isEmpty()) {
				for (Entry<String, TopicBrokerExcelExport> entry : topicIdentToExcelExport.entrySet()) {
					TopicBrokerExcelExport topicExcelExport = entry.getValue();
					if (topicExcelExport != null) {
						try (ShieldOutputStream sout = new ShieldOutputStream(zout);) {
							String filename = topicsPath + StringHelper.transformDisplayNameToFileSystemName("enrollments_" + entry.getKey()) + ".xlsx";
							zout.putNextEntry(new ZipEntry(filename));
							topicExcelExport.export(sout);
							zout.closeEntry();
						} catch(Exception e) {
							log.error(e);
						}
					}
				}
			}
			
			if (excelExport != null) {
				try (ShieldOutputStream sout = new ShieldOutputStream(zout);) {
					zout.putNextEntry(new ZipEntry("topicbroker.xlsx"));
					excelExport.export(sout);
					zout.closeEntry();
				} catch(Exception e) {
					log.error(e);
				}
			}
			
		} catch (Exception e) {
			log.error("Error during export of topic broker (key::{})", broker.getKey(), e);
		}
	}

	@Override
	public void release() {
		//
	}

}
