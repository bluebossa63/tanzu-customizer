/*
 * Copyright 2021 Daniele Ulrich 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * DnsRecord.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.dto;

/**
 * The Class DnsRecord.
 *
 * @author daniele
 */
public class DnsRecord {

	/** The host name. */
	String hostName;

	/** The record type. */
	String recordType;

	/** The type. */
	String type;

	/** The time stamp. */
	String timeStamp;

	/** The ttl. */
	String ttl;

	/** The record data. */
	String recordData;

	/**
	 * Instantiates a new dns record.
	 */
	public DnsRecord() {
		super();
	}

	/**
	 * Instantiates a new dns record.
	 *
	 * @param hostName   the host name
	 * @param recordType the record type
	 * @param type       the type
	 * @param timeStamp  the time stamp
	 * @param ttl        the ttl
	 * @param recordData the record data
	 */
	public DnsRecord(String hostName, String recordType, String type, String timeStamp, String ttl, String recordData) {
		super();
		this.hostName = hostName;
		this.recordType = recordType;
		this.type = type;
		this.timeStamp = timeStamp;
		this.ttl = ttl;
		this.recordData = recordData;
	}

	/**
	 * Gets the host name.
	 *
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Sets the host name.
	 *
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Gets the record type.
	 *
	 * @return the recordType
	 */
	public String getRecordType() {
		return recordType;
	}

	/**
	 * Sets the record type.
	 *
	 * @param recordType the recordType to set
	 */
	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the time stamp.
	 *
	 * @return the timeStamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the time stamp.
	 *
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets the ttl.
	 *
	 * @return the ttl
	 */
	public String getTtl() {
		return ttl;
	}

	/**
	 * Sets the ttl.
	 *
	 * @param ttl the ttl to set
	 */
	public void setTtl(String ttl) {
		this.ttl = ttl;
	}

	/**
	 * Gets the record data.
	 *
	 * @return the recordData
	 */
	public String getRecordData() {
		return recordData;
	}

	/**
	 * Sets the record data.
	 *
	 * @param recordData the recordData to set
	 */
	public void setRecordData(String recordData) {
		this.recordData = recordData;
	}

}
