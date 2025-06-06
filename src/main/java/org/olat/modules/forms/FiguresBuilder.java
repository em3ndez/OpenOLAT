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
package org.olat.modules.forms;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 9 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FiguresBuilder {

	private Long numberOfParticipations;
	private Long numberOfPubicParticipations;
	private List<Figure> figures = new ArrayList<>();
	
	public static FiguresBuilder builder() {
		return new FiguresBuilder();
	}
	
	private FiguresBuilder() {
	}
	
	public FiguresBuilder withNumberOfParticipations(Long numberOfParticipations) {
		this.numberOfParticipations = numberOfParticipations;
		return this;
	}
	
	public FiguresBuilder withNumberOfPublicParticipations(Long numberOfPubicParticipations) {
		this.numberOfPubicParticipations = numberOfPubicParticipations;
		return this;
	}
	
	public FiguresBuilder addCustomFigure(String key, String value) {
		figures.add(new Figure(key, value));
		return this;
	}
	
	public Figures build() {
		return new FiguresProviderImpl(this);
	}
	
	private static final class FiguresProviderImpl implements Figures {

		private final Long numberOfParticipations;
		private final Long numberOfPubicParticipations;
		private List<Figure> figures = new ArrayList<>();
		
		public FiguresProviderImpl(FiguresBuilder builder) {
			this.numberOfParticipations = builder.numberOfParticipations;
			this.numberOfPubicParticipations = builder.numberOfPubicParticipations;
			this.figures = new ArrayList<>(builder.figures);
		}
		
		@Override
		public Long getNumberOfParticipations() {
			return numberOfParticipations;
		}
		
		@Override
		public Long getNumberOfPublicParticipations() {
			return numberOfPubicParticipations;
		}

		@Override
		public List<Figure> getCustomFigures() {
			return figures;
		}
		
	}
}
