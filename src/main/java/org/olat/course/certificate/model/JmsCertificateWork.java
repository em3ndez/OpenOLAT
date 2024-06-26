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
package org.olat.course.certificate.model;

import java.io.Serializable;

/**
 * 
 * Initial date: 19.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class JmsCertificateWork implements Serializable {

	private static final long serialVersionUID = 4462884019283948487L;
	
	private Float score;
	private Float maxScore;
	private Boolean passed;
	private Double completion;
	private Long templateKey;
	private Long certificateKey;
	private String grade;
	private CertificateConfig config;
	
	public JmsCertificateWork() {
		//
	}
	
	public JmsCertificateWork(Long certificateKey, Long templateKey, Float score, Float maxScore, Boolean passed,
			Double completion, CertificateConfig config) {
		this.score = score;
		this.maxScore = maxScore;
		this.passed = passed;
		this.completion = completion;
		this.config = config;
		this.templateKey = templateKey;
		this.certificateKey = certificateKey;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Float getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public Double getCompletion() {
		return completion;
	}

	public void setCompletion(Double completion) {
		this.completion = completion;
	}

	public CertificateConfig getConfig() {
		return config;
	}

	public void setConfig(CertificateConfig config) {
		this.config = config;
	}

	public Long getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(Long templateKey) {
		this.templateKey = templateKey;
	}

	public Long getCertificateKey() {
		return certificateKey;
	}

	public void setCertificateKey(Long certificateKey) {
		this.certificateKey = certificateKey;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	@Override
	public int hashCode() {
		return certificateKey == null ? 87580 : certificateKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof JmsCertificateWork work) {
			return certificateKey != null && certificateKey.equals(work.certificateKey);
		}
		return false;
	}
}