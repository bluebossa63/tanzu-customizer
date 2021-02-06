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
 * DNSManagementService.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.tanzu.customizer.dto.DnsRecord;
import ch.tanzu.customizer.dto.DnsZone;
import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

/**
 * The Class DNSManagementService.
 *
 * @author daniele
 */
@Service
public class DNSManagementService {

	/** The tool. */
	@Autowired
	WinRmTool tool;

	/** The context. */
	@Autowired
	WinRmClientContext context;

	/** The to skip. */
	List<String> toSkip = new ArrayList<String>();

	/**
	 * Instantiates a new DNS management service.
	 */
	public DNSManagementService() {
		toSkip.add("SRV");
		toSkip.add("SOA");
		toSkip.add("PTR");
		toSkip.add("NS");
		toSkip.add("@");
		toSkip.add("DomainDnsZones");
		toSkip.add("ForestDnsZones");
	}

	/**
	 * Gets the zones.
	 *
	 * @return the zones
	 */
	public List<DnsZone> getZones() {
		List<DnsZone> records = new ArrayList<DnsZone>();
		String command = "Get-DnsServerZone | Format-Table -AutoSize | out-string -width 500";
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);

		String[] retVal = response.getStdOut().split("\n");
		int zoneTypeStart = retVal[1].indexOf("ZoneType");
		int autoCreatedStart = retVal[1].indexOf("IsAutoCreated");
		int dsIntegratedStart = retVal[1].indexOf("IsDsIntegrated");
		int reverseLookupStart = retVal[1].indexOf("IsReverseLookupZone");
		int signedStart = retVal[1].indexOf("IsSigned");

		for (int i = 3; i < retVal.length; i++) {
			if (retVal[i].length() > 80) {
				String returnLine = retVal[i];
				records.add(new DnsZone(returnLine.substring(0, zoneTypeStart).trim(),
						returnLine.substring(zoneTypeStart, autoCreatedStart).trim(),
						Boolean.valueOf(returnLine.substring(autoCreatedStart, dsIntegratedStart).trim().toLowerCase()),
						Boolean.valueOf(
								returnLine.substring(dsIntegratedStart, reverseLookupStart).trim().toLowerCase()),
						Boolean.valueOf(returnLine.substring(reverseLookupStart, signedStart).trim().toLowerCase()),
						Boolean.valueOf(returnLine.substring(signedStart).trim().toLowerCase())));
			}
		}
		return records;
	}

	/**
	 * Adds the zone.
	 *
	 * @param zoneName the zone name
	 */
	public void addZone(String zoneName) {
		String command = String.format("Add-DnsServerPrimaryZone -Name \"%s\" -ZoneFile \"%s\"", zoneName,
				zoneName + ".dns");
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);
	}

	/**
	 * Delete zone.
	 *
	 * @param zoneName the zone name
	 */
	public void deleteZone(String zoneName) {
		String command = String.format("Remove-DnsServerZone -Name \"%s\" -Force", zoneName);
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);
	}

	/**
	 * Gets the zone records.
	 *
	 * @param zoneName the zone name
	 * @return the zone records
	 */
	public List<DnsRecord> getZoneRecords(String zoneName) {
		List<DnsRecord> records = new ArrayList<DnsRecord>();
		String command = String.format(
				"Get-DnsServerResourceRecord -ZoneName \"%s\" | Format-Table -AutoSize | out-string -width 500",
				zoneName);
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);

		String[] retVal = response.getStdOut().split("\n");
		int recordTypeStart = retVal[1].indexOf("RecordType");
		int typeStart = retVal[1].indexOf("Type", recordTypeStart + 10);
		int timeStampStart = retVal[1].indexOf("Timestamp");
		int timeToLiveStart = retVal[1].indexOf("TimeToLive");
		int recordDataStart = retVal[1].indexOf("RecordData");

		for (int i = 3; i < retVal.length; i++) {
			if (retVal[i].length() > 10) {
				String returnLine = retVal[i];
				DnsRecord rec = new DnsRecord(returnLine.substring(0, recordTypeStart).trim(),
						returnLine.substring(recordTypeStart, typeStart).trim(),
						returnLine.substring(typeStart, timeStampStart).trim(),
						returnLine.substring(timeStampStart, timeToLiveStart).trim(),
						returnLine.substring(timeToLiveStart, recordDataStart).trim(),
						returnLine.substring(recordDataStart).trim());
				if (!toSkip.contains(rec.getRecordType()) && !toSkip.contains(rec.getHostName())) {
					records.add(rec);
				}
			}
		}
		return records;
	}

	/**
	 * Gets the host records.
	 *
	 * @param fqdn the fqdn
	 * @return the host records
	 */
	public List<DnsRecord> getHostRecords(String fqdn) {
		FQDN f = split(fqdn);
		List<DnsRecord> records = new ArrayList<DnsRecord>();
		String command = String.format(
				"Get-DnsServerResourceRecord -ZoneName \"%s\" -Name \"%s\" | Format-Table -AutoSize | out-string -width 500",
				f.domain, f.hostname);
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);

		String[] retVal = response.getStdOut().split("\n");
		int recordTypeStart = retVal[1].indexOf("RecordType");
		int typeStart = retVal[1].indexOf("Type", recordTypeStart + 10);
		int timeStampStart = retVal[1].indexOf("Timestamp");
		int timeToLiveStart = retVal[1].indexOf("TimeToLive");
		int recordDataStart = retVal[1].indexOf("RecordData");

		for (int i = 3; i < retVal.length; i++) {
			if (retVal[i].length() > 10) {
				String returnLine = retVal[i];
				DnsRecord rec = new DnsRecord(returnLine.substring(0, recordTypeStart).trim(),
						returnLine.substring(recordTypeStart, typeStart).trim(),
						returnLine.substring(typeStart, timeStampStart).trim(),
						returnLine.substring(timeStampStart, timeToLiveStart).trim(),
						returnLine.substring(timeToLiveStart, recordDataStart).trim(),
						returnLine.substring(recordDataStart).trim());
				if (!toSkip.contains(rec.getRecordType()) && !toSkip.contains(rec.getHostName())) {
					records.add(rec);
				}
			}
		}
		return records;
	}

	/**
	 * Adds the C name record.
	 *
	 * @param fqdn  the fqdn
	 * @param alias the alias
	 * @param ttl   the ttl
	 */
	public void addCNameRecord(String fqdn, String alias, String ttl) {
		FQDN f = split(fqdn);
		String command = String.format(
				"Add-DnsServerResourceRecord -CName -Name \"%s\" -HostNameAlias \"%s\" -ZoneName \"%s\" -AllowUpdateAny -TimeToLive \"%s\"",
				f.hostname, alias, f.domain, ttl);
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);
	}

	/**
	 * Adds the A record.
	 *
	 * @param fqdn the fqdn
	 * @param ip   the ip
	 * @param ttl  the ttl
	 */
	public void addARecord(String fqdn, String ip, String ttl) {
		FQDN f = split(fqdn);
//	    TODO: implement reverse PTR.		
//		String[] ipParts = ip.split("\\.");
//		String inverse = "";
//		for (int i = ipParts.length - 1; i > 0; i--) {
//			inverse += ipParts[i] + ".";
//		}
		String command = String.format(
				"Add-DnsServerResourceRecord -ZoneName \"%s\" -A -Name \"%s\" -IPv4Address \"%s\"", f.domain,
				f.hostname, ip);
		// "Add-DnsServerResourceRecord -Name %s -Ptr -ZoneName %s -AllowUpdateAny
		// -PtrDomainName %s"
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);
	}

	/**
	 * Delete record.
	 *
	 * @param fqdn       the fqdn
	 * @param rrType     the rr type
	 * @param recordData the record data
	 */
	public void deleteRecord(String fqdn, String rrType, String recordData) {
		FQDN f = split(fqdn);
		String command = String.format(
				"Remove-DnsServerResourceRecord -ZoneName \"%s\" -RRType \"%s\" -Name \"%s\" -RecordData \"%s\" -Force",
				f.domain, rrType, f.hostname, recordData);
		WinRmToolResponse response = tool.executePs(command);
		checkForErrors(response);
	}

	/**
	 * The Class FQDN.
	 */
	public static class FQDN {

		/** The hostname. */
		String hostname;

		/** The domain. */
		String domain;

		/**
		 * Gets the hostname.
		 *
		 * @return the hostname
		 */
		public String getHostname() {
			return hostname;
		}

		/**
		 * Gets the domain.
		 *
		 * @return the domain
		 */
		public String getDomain() {
			return domain;
		}
	}

	/**
	 * Split.
	 *
	 * @param fqdn the fqdn
	 * @return the fqdn
	 */
	public FQDN split(String fqdn) {
		FQDN retVal = new FQDN();
		String[] parts = fqdn.split("\\.");
		String zoneName = "";
		for (int i = 1; i < parts.length; i++) {
			zoneName += parts[i];
			if (i < parts.length - 1) {
				zoneName += ".";
			}
		}
		retVal.hostname = parts[0];
		retVal.domain = zoneName;
		return retVal;
	}

	/**
	 * Check for errors.
	 *
	 * @param response the response
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void checkForErrors(WinRmToolResponse response) throws IllegalArgumentException {
		if (response.getStatusCode() != 0) {
			throw new IllegalArgumentException(response.getStdErr().split("\n")[1]);
		}

	}

	/**
	 * Destroy.
	 */
	@PreDestroy
	public void destroy() {
		context.shutdown();
	}
}
