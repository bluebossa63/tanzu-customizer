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
 * TanzuCustomizerController.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ch.tanzu.customizer.dto.RestartServiceOnNodeCommand;
import io.kubernetes.client.extended.controller.DefaultController;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.VmoperatorVmwareComV1alpha1Api;
import io.kubernetes.client.openapi.models.ComVmwareVmoperatorV1alpha1VirtualMachineList;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Api(authorizations = { @Authorization(value = "Basic Auth") })
@RequestMapping("/api")
@RestController
public class TanzuCustomizerController {

	private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);

	@Autowired
	ObjectMapper objectMapper;

	@SuppressWarnings("unchecked")
	@ApiOperation(value = "restart node service", notes = "this method allows to restart a service on a tanzu kubernetes node", nickname = "lifecycle", tags = {
			"lifecycle" })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Event successfully processed or completely ignored: codes 200 - 299 are considered as success"),
			@ApiResponse(code = 300, message = "Codes 300 - 499 are considered as error situations and the flag workflowSuggestion in the response defines it the workflow is cancelled at once (only if returned in the defined blocking events)"),
			@ApiResponse(code = 500, message = "Codes >=500 are considered as blocking unexpected server errors") })
	@PostMapping(path = "/restart", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public void restartService(@RequestBody RestartServiceOnNodeCommand nodeCommand) {

		try {
			logger.info("processing restartService for "
					+ objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeCommand));
		} catch (JsonProcessingException e1) {
			logger.warn("exception in objectMapper", e1);
		}

		try {

			//create kubernetes api client
			ApiClient client = ClientBuilder
					.kubeconfig(KubeConfig.loadKubeConfig(new FileReader("/etc/kubernetes/admin.conf"))).build();

			Configuration.setDefaultApiClient(client);

			VmoperatorVmwareComV1alpha1Api comVmwareVmoperatorV1alpha1VirtualMachineList = new VmoperatorVmwareComV1alpha1Api();
			CoreV1Api coreV1Api = new CoreV1Api();

			// get all tanzu virtual machines
			ComVmwareVmoperatorV1alpha1VirtualMachineList list = comVmwareVmoperatorV1alpha1VirtualMachineList
					.listVmoperatorVmwareComV1alpha1VirtualMachineForAllNamespaces(null, null, null, null, null, null,
							null, null, null);

			// TODO: swagger generator had some problems with this class definition!
			// process it as hashmap
			for (Object item : list.getItems()) {

				if (nodeCommand.getIp().equals(((Map<String, Object>) ((Map<String, Object>) item).get("status")).get("vmIp"))) {

					String name = (String) ((Map<String, Object>) ((Map<String, Object>) item).get("metadata"))
							.get("name");
					String namespace = (String) ((Map<String, Object>) ((Map<String, Object>) item).get("metadata"))
							.get("namespace");
					String clusterName = getClusterName(name);
					
					try {
						
						V1Secret secret = coreV1Api.readNamespacedSecret(clusterName + "-ssh", namespace, null, null,
								null);
						executeSSHRemoteCommand("vmware-system-user", nodeCommand.getIp(), clusterName,
								"sudo systemctl restart " + nodeCommand.getServiceName(), "restart service", secret);
						
					} catch (ApiException e) {
						logger.error("K8s API error", e.getResponseBody());
						throw new RuntimeException(e);
					} catch (JSchException e) {
						logger.error("SSH Session error: ", e);
						throw new RuntimeException(e);
					}
				}
			}
		} catch (IOException | ApiException e) {
			throw new RuntimeException(e);
		}
	}

	private String getClusterName(String name) {
		return name.split("(\\-workers\\-|\\-control\\-)")[0];
	}

	private void executeSSHRemoteCommand(String username, String ip, String clusterName, String command,
			String actionName, V1Secret secret) throws IOException, FileNotFoundException, JSchException {

		JSch jsch = new JSch();
		File file = copyPrivateKeyToTempFile(clusterName, secret);
		jsch.addIdentity(file.getAbsolutePath());
		
		Session session = jsch.getSession(username, ip, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();

		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		((ChannelExec) channel).setErrStream(bos);
		InputStream in = channel.getInputStream();
		channel.connect();

		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				logger.info(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				if (in.available() > 0)
					continue;
				logger.info(actionName + " " + "exit-status: " + channel.getExitStatus());
				byte[] errorStream = bos.toByteArray();
				bos.close();
				if (errorStream.length > 0)
					logger.error(actionName + " " + "error: " + new String(errorStream));
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		
		channel.disconnect();
		session.disconnect();
		file.delete();
	}

	private File copyPrivateKeyToTempFile(String clusterName, V1Secret secret)
			throws IOException, FileNotFoundException, JSchException {
		Entry<String, byte[]> entry = secret.getData().entrySet().iterator().next();
		File file = File.createTempFile(clusterName, null, null);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(entry.getValue());
		fos.close();
		return file;
	}
}
