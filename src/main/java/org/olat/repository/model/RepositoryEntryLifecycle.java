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
package org.olat.repository.model;

import java.util.Date;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.DateRange;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="repositoryentrylifecycle")
@Table(name="o_repositoryentry_cycle")
@NamedQuery(name="loadReLifeCycle", query="select relifecycle from repositoryentrylifecycle relifecycle where relifecycle.key=:key order by relifecycle.validFrom desc")
@NamedQuery(name="loadPublicReLifeCycle", query="select relifecycle from repositoryentrylifecycle relifecycle where relifecycle.privateCycle=false order by relifecycle.validFrom desc")
public class RepositoryEntryLifecycle implements Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = -8484159601386853047L;
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	

	@Column(name="r_softkey", nullable=true, insertable=true, updatable=true)
	private String softKey;
	@Column(name="r_label", nullable=true, insertable=true, updatable=true)
	private String label;
	@Column(name="r_privatecycle", nullable=false, insertable=true, updatable=false)
	private boolean privateCycle;
	@Column(name="r_defaultpubliccycle", nullable=false, insertable=true, updatable=true)
	private boolean defaultPublicCycle;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_validfrom", nullable=true, insertable=true, updatable=true)
	private Date validFrom;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_validto", nullable=true, insertable=true, updatable=true)
	private Date validTo;

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getSoftKey() {
		return softKey;
	}
	
	public void setSoftKey(String softKey) {
		this.softKey = softKey;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isPrivateCycle() {
		return privateCycle;
	}
	
	public void setPrivateCycle(boolean privateCycle) {
		this.privateCycle = privateCycle;
	}

	public boolean isDefaultPublicCycle() {
		return defaultPublicCycle;
	}

	public void setDefaultPublicCycle(boolean defaultPublicCycle) {
		this.defaultPublicCycle = defaultPublicCycle;
	}
	
	public Date getValidFrom() {
		return validFrom;
	}
	
	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}
	
	public Date getValidTo() {
		return validTo;
	}
	
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}
	
	@Transient
	public DateRange getDateRange() {
		return new DateRange(getValidFrom(), getValidTo());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RepositoryEntryLifecycle relc) {
			return getKey() != null && getKey().equals(relc.getKey());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 48790 : getKey().hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}