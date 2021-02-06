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
 * DnsZone.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.dto;

/**
 * The Class DnsZone.
 *
 * @author daniele
 */
public class DnsZone {

	/** The zone name. */
	String zoneName;

	/** The zone type. */
	String zoneType;

	/** The is auto created. */
	boolean isAutoCreated;

	/** The is ds integrated. */
	boolean isDsIntegrated;

	/** The is reverse lookup zone. */
	boolean isReverseLookupZone;

	/** The is signed. */
	boolean isSigned;

	/**
	 * Instantiates a new dns zone.
	 */
	public DnsZone() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new dns zone.
	 *
	 * @param zoneName            the zone name
	 * @param zoneType            the zone type
	 * @param isAutoCreated       the is auto created
	 * @param isDsIntegrated      the is ds integrated
	 * @param isReverseLookupZone the is reverse lookup zone
	 * @param isSigned            the is signed
	 */
	public DnsZone(String zoneName, String zoneType, boolean isAutoCreated, boolean isDsIntegrated,
			boolean isReverseLookupZone, boolean isSigned) {
		super();
		this.zoneName = zoneName;
		this.zoneType = zoneType;
		this.isAutoCreated = isAutoCreated;
		this.isDsIntegrated = isDsIntegrated;
		this.isReverseLookupZone = isReverseLookupZone;
		this.isSigned = isSigned;
	}

	/**
	 * Gets the zone name.
	 *
	 * @return the zoneName
	 */
	public String getZoneName() {
		return zoneName;
	}

	/**
	 * Sets the zone name.
	 *
	 * @param zoneName the zoneName to set
	 */
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	/**
	 * Gets the zone type.
	 *
	 * @return the zoneType
	 */
	public String getZoneType() {
		return zoneType;
	}

	/**
	 * Sets the zone type.
	 *
	 * @param zoneType the zoneType to set
	 */
	public void setZoneType(String zoneType) {
		this.zoneType = zoneType;
	}

	/**
	 * Checks if is auto created.
	 *
	 * @return the isAutoCreated
	 */
	public boolean isAutoCreated() {
		return isAutoCreated;
	}

	/**
	 * Sets the auto created.
	 *
	 * @param isAutoCreated the isAutoCreated to set
	 */
	public void setAutoCreated(boolean isAutoCreated) {
		this.isAutoCreated = isAutoCreated;
	}

	/**
	 * Checks if is ds integrated.
	 *
	 * @return the isDsIntegrated
	 */
	public boolean isDsIntegrated() {
		return isDsIntegrated;
	}

	/**
	 * Sets the ds integrated.
	 *
	 * @param isDsIntegrated the isDsIntegrated to set
	 */
	public void setDsIntegrated(boolean isDsIntegrated) {
		this.isDsIntegrated = isDsIntegrated;
	}

	/**
	 * Checks if is reverse lookup zone.
	 *
	 * @return the isReverseLookupZone
	 */
	public boolean isReverseLookupZone() {
		return isReverseLookupZone;
	}

	/**
	 * Sets the reverse lookup zone.
	 *
	 * @param isReverseLookupZone the isReverseLookupZone to set
	 */
	public void setReverseLookupZone(boolean isReverseLookupZone) {
		this.isReverseLookupZone = isReverseLookupZone;
	}

	/**
	 * Checks if is signed.
	 *
	 * @return the isSigned
	 */
	public boolean isSigned() {
		return isSigned;
	}

	/**
	 * Sets the signed.
	 *
	 * @param isSigned the isSigned to set
	 */
	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}

}
