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
 * KubernetesGatewayWatcherService.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.tanzu.customizer.dto.DnsRecord;
import ch.tanzu.customizer.service.DNSManagementService.FQDN;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.auth.ApiKeyAuth;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

/**
 * The Class KubernetesGatewayWatcherService.
 *
 * @author daniele
 */
@Service
@Profile(value = { "istio" })
public class KubernetesGatewayWatcherService {
	
	static Logger log = LoggerFactory.getLogger(KubernetesGatewayWatcherService.class);

	/** The dns management service. */
	@Autowired
	DNSManagementService dnsManagementService;

	/** The client builder. */
	@Autowired
	private javax.ws.rs.client.ClientBuilder clientBuilder;

	/** The rest template. */
	@Autowired
	RestTemplate restTemplate;

	/** The om. */
	@Autowired
	ObjectMapper om;

	/** The kube config path. */
	@Value("${kube.conf}")
	String kubeConfigPath = "/home/daniele/.kube/config";

	/**
	 * On application event.
	 *
	 * @param event the event
	 */
	@SuppressWarnings("unchecked")
	@EventListener
	
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		try {

			ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
					.build();
			Configuration.setDefaultApiClient(client);
			
			log.info("start to watch gateways");
			URL url = new URL(client.getBasePath());
			FQDN baseFqdn = dnsManagementService.split(url.getHost());

			log.info("k8s api host: " + url.getHost());
			try {
				dnsManagementService.addZone(baseFqdn.getDomain());
			} catch (IllegalArgumentException e) {
				// ignore, zone exists
			}

			Builder builder = clientBuilder.build().target(client.getBasePath())
					.path("/apis/networking.istio.io/v1alpha3/watch/gateways").request()
					.header("Authorization",
							"Bearer " + ((ApiKeyAuth) client.getAuthentication("BearerToken")).getApiKey())
					.header("Content-Type", MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_VALUE);

			log.info("starting to watch");
			
			while (true) {

				Response response = builder.get();
				if (!Response.Status.Family.SUCCESSFUL.equals(Response.Status.Family.familyOf(response.getStatus()))) {
					throw new IllegalArgumentException("cannot get gateways");
				}
				
				InputStream is = (InputStream) response.getEntity();
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				String line = null;

				while ((line = in.readLine()) != null) {
					Map<String, Object> objs = om.readValue(line, new TypeReference<Map<String, Object>>() {
					});
					log.debug(om.writerWithDefaultPrettyPrinter().writeValueAsString(objs));
					
					String action = (String) objs.get("type");
					Map<String, Object> object = (Map<String, Object>) objs.get("object");
					Map<String, Object> spec = (Map<String, Object>) object.get("spec");
					List<Map<String, Object>> servers = (List<Map<String, Object>>) spec.get("servers");
					for (Map<String, Object> server : servers) {
						List<String> hosts = (List<String>) server.get("hosts");
						for (String item : hosts) {
							if (!"*.global".equals(item) && !"*".equals(item)) {
								FQDN fqdn = dnsManagementService.split(item);
								if ("ADDED".equals(action)) {
									log.info("ADDING: " + item);
									try {
										dnsManagementService.addZone(fqdn.getDomain());
									} catch (IllegalArgumentException e) {
										// ignore, zone exists
									}
									try {
										dnsManagementService.addCNameRecord(item,
												"istio-ingressgateway." + baseFqdn.getDomain(), "01:00:00");
									} catch (IllegalArgumentException e) {
										// ignore, zone exists
									}
								} else if ("DELETED".equals(action)) {
									log.info("DELETING: " + item);
									try {
										List<DnsRecord> records = dnsManagementService.getHostRecords(item);
										try {
											dnsManagementService.deleteRecord(item, records.get(0).getRecordType(),
													records.get(0).getRecordData());
										} catch (IllegalArgumentException e) {
											log.error(e.getLocalizedMessage());
										}
									} catch (IllegalArgumentException e) {
										log.error(e.getLocalizedMessage());
									}
								} else {
									log.warn("missing type: " + action);
								}
							}
						}
					}
				}
				log.info("continuing to watch");
			}
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}

	}

}
