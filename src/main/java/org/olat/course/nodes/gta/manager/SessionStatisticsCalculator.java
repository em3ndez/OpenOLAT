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
package org.olat.course.nodes.gta.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.model.StepCountsBuilder;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 14 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SessionStatisticsCalculator {
	
	private final Form form;
	private final EvaluationFormResponses responses;
	
	public SessionStatisticsCalculator(EvaluationFormResponses responses, Form form) {
		this.form = form;
		this.responses = responses;
	}
	
	SessionStatistics calculateStatistics(Collection<EvaluationFormSession> sessions) {
		Collector collector = new Collector();
		for(EvaluationFormSession session:sessions) {
			Map<String, List<EvaluationFormResponse>> responsesMap = responses.getResponsesBySession(session);
			collectValuesBySession(collector, responsesMap);
		}
		return calculateStatistics(collector);
	}

	SessionStatistics calculateStatistics(EvaluationFormSession session) {
		Map<String, List<EvaluationFormResponse>> responsesMap = responses.getResponsesBySession(session);
		Collector collector = new Collector();
		collectValuesBySession(collector, responsesMap);
		return calculateStatistics(collector);
	}
	
	private SessionStatistics calculateStatistics(Collector collector) {
		List<Double> values = collector.getValues();
		if(values.isEmpty()) {
			return SessionStatistics.noStatistics();
		}
		Collections.sort(values);
		
		double min = values.get(0).doubleValue();
		double max = values.get(values.size() - 1).doubleValue();
		double median = calculateMedian(values);
		double firstQuartile = percentile(values, 25d);
		double thirdQuartile = percentile(values, 75d);
		
		// Average of the sum pro session
		double sum = calculateAverage(collector.getSessionsSum());
		double average = calculateAverage(values);
		double progress = calculateProgress(collector.getNumOfResponses() + collector.getNumOfResponsesWithNothing(), collector.getNumOfSilders());
		
		return new SessionStatistics(progress, min, max, average, sum,
				firstQuartile, median, thirdQuartile,
				collector.getNumOfResponses(), collector.getMaxSteps(), collector.getMaxStepsValue());
	}
	
	private void collectValuesBySession(Collector collector, Map<String, List<EvaluationFormResponse>> responsesMap) {
		List<Double> values = new ArrayList<>();
		
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				collector.addSliders(rubric.getSliders().size());
				
				double stepsValue = getMaxStepsValue(rubric);
				collector.addMaxStepsValue(stepsValue);
				collector.addSteps(rubric.getSteps());
				
				for(Slider slider:rubric.getSliders()) {
					int weight = slider.getWeight() == null ? 1 : slider.getWeight().intValue();

					List<EvaluationFormResponse> sliderResponses = responsesMap.get(slider.getId());
					if(sliderResponses != null && sliderResponses.size() == 1) {
						EvaluationFormResponse sliderResponse = sliderResponses.get(0);
						if(sliderResponse.isNoResponse()) {
							collector.incrementNumOfResponsesWithNothing();
						} else if(StringHelper.containsNonWhitespace(sliderResponse.getStringuifiedResponse())
								&& sliderResponse.getNumericalResponse() != null) {
							collector.incrementNumOfResponses();

							double val = getValue(rubric, sliderResponse) * weight;
							collector.addValue(val);
							values.add(val);
						}
					}
				}
			}
		}
		
		collector.addSessionSum(calculateSum(values));
	}
	
	private double getMaxStepsValue(Rubric rubric) {
		return rubric.getScaleType().getStepValue(rubric.getSteps(), rubric.getEnd());
	}
	
	private double getValue(Rubric rubric, EvaluationFormResponse sliderResponse) {
		BigDecimal response = sliderResponse.getNumericalResponse();
		StepCounts stepCounts = StepCountsBuilder.builder(rubric.getSteps())
				.withCount(response.intValue(), Long.valueOf(1))
				.build();
		
		double val = response.doubleValue();
		return rubric.getScaleType().getStepValue(stepCounts.getNumberOfSteps(), val);
	}
	
	private double calculateSum(List<Double> values) {
		double total = 0.0d;
		for(Double value:values) {
			total += value.doubleValue();
		}
		return total;
	}
	
	private double calculateAverage(List<Double> values) {
		double total = calculateSum(values);
		return total / values.size();
	}
	
	private double calculateProgress(int numOfResponses, double total) {
		double progress;
		if(total == 0) {
			progress = 1.0f;
		} else {
			progress = numOfResponses / total;
		}
		return progress;
	}
	
	private double calculateMedian(List<Double> values) {
		if(values.size() < 2) {
			return 0.0d;
		}
		
		double median;
		if (values.size() % 2 == 0) {
			median = (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2;
		} else {
			median = values.get(values.size() / 2);
		}
		return median;
	}
	
	public static double percentile(List<Double> values, double percentile) {
		int index = (int) Math.ceil((percentile / 100.0) * values.size());
		if(index <= 0) {
			return 0.0d;
		}
		return values.get(index - 1);
	}
	
	private static class Collector {
		
		private final List<Double> values = new ArrayList<>();
		private final List<Double> sessionsSum = new ArrayList<>();
		
		private int numOfSilders = 0;
		private int numOfResponses = 0;
		private int numOfResponsesWithNothing = 0;
		
		private int maxSteps = 0;
		private double maxStepsValue = 0.0d;
		
		public void addSteps(int steps) {
			if(steps > maxSteps) {
				maxSteps = steps;
			}
		}
		
		public void addMaxStepsValue(double val) {
			if(val > maxStepsValue) {
				maxStepsValue = val;
			}
		}
		
		public int getMaxSteps() {
			return maxSteps;
		}
		
		public double getMaxStepsValue() {
			return maxStepsValue;
		}
		
		public void addValue(double val) {
			values.add(Double.valueOf(val));
		}
		
		public void addSessionSum(double val) {
			sessionsSum.add(val);
		}
		
		public void addSliders(int val) {
			numOfSilders += val;
		}
		
		public void incrementNumOfResponses() {
			numOfResponses++;
		}
		
		public void incrementNumOfResponsesWithNothing() {
			numOfResponsesWithNothing++;
		}

		public List<Double> getValues() {
			return new ArrayList<>(values);
		}
		
		public List<Double> getSessionsSum() {
			return sessionsSum;
		}

		public int getNumOfSilders() {
			return numOfSilders;
		}

		public int getNumOfResponses() {
			return numOfResponses;
		}

		public int getNumOfResponsesWithNothing() {
			return numOfResponsesWithNothing;
		}
		
		
	}
}
