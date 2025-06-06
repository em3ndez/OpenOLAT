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
package org.olat.core.commons.services.robots;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RobotsDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(RobotsDispatcher.class);

	@Autowired
	private RobotsService robotsService;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		try {
			response.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			//we do our best
		}
		response.setContentType("text/plain");
		try(PrintWriter writer = response.getWriter()) {
			writer.write("User-agent: *\n");
			writer.write("Disallow: /");
			List<String> allows = robotsService.getRobotsAllows();
			for (String allow : allows) {
				writer.write("\nAllow: " + allow);
			}
			
			String sitemapIndexPath = robotsService.getSitemapIndexPath();
			if (StringHelper.containsNonWhitespace(sitemapIndexPath)) {
				writer.write("\nAllow: " + sitemapIndexPath);
				writer.write("\n\nSitemap: " + Settings.getServerContextPathURI() + sitemapIndexPath);
			}
		} catch(IOException e) {
			log.error("", e);
		}
	}
}
