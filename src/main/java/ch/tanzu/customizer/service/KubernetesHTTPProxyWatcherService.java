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
 * KubernetesHTTPProxyWatcherService.java
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
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.tanzu.customizer.dto.DnsRecord;
import ch.tanzu.customizer.dto.httpproxy.HTTPProxyWatch;
import ch.tanzu.customizer.service.DNSManagementService.FQDN;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.auth.ApiKeyAuth;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

/**
 * The Class KubernetesHTTPProxyWatcherService.
 *
 * @author daniele
 */
@Service
public class KubernetesHTTPProxyWatcherService {

	static Logger log = LoggerFactory.getLogger(KubernetesHTTPProxyWatcherService.class);

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
	@EventListener
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		try {

			KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath));
			ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
			Configuration.setDefaultApiClient(client);

			log.info("start to watch deployments");
			// URL url = new URL(client.getBasePath());
			// FQDN baseFqdn = dnsManagementService.split(url.getHost());

			log.info("k8s api host: " + kubeConfig.getCurrentContext());
			try {
				dnsManagementService.addZone("ne.local");
			} catch (IllegalArgumentException e) {
				// ignore, zone exists
			}

			CoreV1Api coreV1Api = new CoreV1Api();

			V1ServiceList services = coreV1Api.listServiceForAllNamespaces(null, null, null, null, null, null, null,
					null, null, null);

			List<V1Service> envoiLoadBalancers = services.getItems().stream()
					.filter(s -> s.getSpec().getType().equalsIgnoreCase("LoadBalancer")
							&& s.getMetadata().getName().equals("envoy"))
					.collect(Collectors.toList());
			if (!envoiLoadBalancers.isEmpty()) {

				try {
					dnsManagementService.addARecord(kubeConfig.getCurrentContext() + ".ne.local",
							envoiLoadBalancers.get(0).getStatus().getLoadBalancer().getIngress().get(0).getIp(),
							"240000");
				} catch (IllegalArgumentException e) {
					// ignore, host exists
				}

			}

			Builder builder = clientBuilder.build().target(client.getBasePath())
					.path("/apis/projectcontour.io/v1/watch/httpproxies").request()
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
					HTTPProxyWatch httpProxyWatch = om.readValue(line, HTTPProxyWatch.class);
					log.info(om.writerWithDefaultPrettyPrinter().writeValueAsString(httpProxyWatch));

					final String action = httpProxyWatch.getType();
					String serverFqdn = httpProxyWatch.getHTTPProxy().getSpec().getVirtualhost().getFqdn();
					FQDN fqdn = dnsManagementService.split(serverFqdn);
					switch (action) {
					case "ADDED":
						log.info("ADDING: " + serverFqdn);
						try {
							dnsManagementService.addZone(fqdn.getDomain());
						} catch (IllegalArgumentException e) {
							if (e.getMessage().contains("ResourceExists:")) {
								// ignore, zone exists
							} else {
								log.warn(serverFqdn, e);
							}
						}
						try {
							dnsManagementService.addCNameRecord(serverFqdn,
									kubeConfig.getCurrentContext() + ".ne.local", "01:00:00");
						} catch (IllegalArgumentException e) {
							if (e.getMessage().contains("ResourceExists:")) {
								// ignore, record exists
							} else {
								log.warn(serverFqdn, e);
							}
						}
						break;
					case "DELETED":
						log.info("DELETING: " + serverFqdn);
						try {
							List<DnsRecord> records = dnsManagementService.getHostRecords(serverFqdn);
							try {
								dnsManagementService.deleteRecord(serverFqdn, records.get(0).getRecordType(),
										records.get(0).getRecordData());
							} catch (IllegalArgumentException e) {
								log.error(e.getLocalizedMessage());
							}
						} catch (IllegalArgumentException e) {
							log.error(e.getLocalizedMessage());
						}
						break;
					default:
						log.warn("missing type: " + action);
					}
					log.info("continuing to watch");
				}
			}
		} catch (

		IOException e) {
			log.error(e.getLocalizedMessage());
		} catch (ApiException e1) {
			log.error(e1.getResponseBody());
		}

	}

}
