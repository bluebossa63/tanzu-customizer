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
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.tanzu.customizer.dto.DnsRecord;
import ch.tanzu.customizer.dto.httpproxy.HTTPProxyWatch;
import ch.tanzu.customizer.service.MicrosoftDNSManagementService.FQDN;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.auth.ApiKeyAuth;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;

/**
 * The Class KubernetesHTTPProxyWatcherService.
 *
 * @author daniele
 */
@Service
@Profile(value = { "contour" })
public class KubernetesHTTPProxyWatcherService {

	static Logger log = LoggerFactory.getLogger(KubernetesHTTPProxyWatcherService.class);

	/** The dns management service. */
	@Autowired
	MicrosoftDNSManagementService dnsManagementService;

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
			//ApiClient client = Config.fromToken(kubeConfig.getServer(), "eyJhbGciOiJSUzI1NiIsImtpZCI6InlIa0xaay1XV3BCeGltY1hOTi02YXdyeThjemVEbG53Z2JncjVtLXF5dE0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJ0YW56dS1zeXN0ZW0taW5ncmVzcyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJjb250b3VyLXRva2VuLWh4YndqIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImNvbnRvdXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiJhYTA0YjY4OC0zYzM4LTRlNDItYTU1NS1lYzZlZTQyZDc1ZmYiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6dGFuenUtc3lzdGVtLWluZ3Jlc3M6Y29udG91ciJ9.uJWLoHNrDCjbqt3gzam92ECdikeZo4ed0VLYQzK2eerS3GmxjaBgQpyfSP3QPRj1MCN7vWKPj5IRR-xbIeX4RKEEqzr-09cwTmp06doEX1JboFmV71aJm4PHlrd6Hr0iv5X_z4Qwis1zO0NckKVLA4WQXrx7ggF-M2_zNo0Hr8F6TdKAwzZDbodAJ78NwHICVC70G5n0BE6L2O6zvoZy40jlI45KTEFczmNzpYtRAXbj8SQsdB4titKF4C0C7repFZROVCGGYIYMrylF_nDGbvF3I4oCu_a7BSn6WRq-SJ9rSsAV_A6ge10hxfH6ByzFBvTCV49euGEDMIkkjvHcwA", false);
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
					throw new IllegalArgumentException("cannot get httpproxies, reason: \n" + response.readEntity(String.class));
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
