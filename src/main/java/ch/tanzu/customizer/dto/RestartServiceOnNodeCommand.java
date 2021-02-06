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
 * RestartServiceOnNodeCommand.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.dto;

/**
 * The Class RestartServiceOnNodeCommand.
 */
public class RestartServiceOnNodeCommand {

	/** The ip. */
	String ip;
	
	/** The service name. */
	String serviceName;

	/**
	 * Instantiates a new restart service on node command.
	 */
	public RestartServiceOnNodeCommand() {
	}

	/**
	 * Instantiates a new restart service on node command.
	 *
	 * @param ip the ip
	 * @param serviceName the service name
	 */
	public RestartServiceOnNodeCommand(String ip, String serviceName) {
		this.ip = ip;
		this.serviceName = serviceName;
	}

	/**
	 * Gets the ip.
	 *
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Sets the ip.
	 *
	 * @param ip the new ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Gets the service name.
	 *
	 * @return the service name
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Sets the service name.
	 *
	 * @param serviceName the new service name
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
