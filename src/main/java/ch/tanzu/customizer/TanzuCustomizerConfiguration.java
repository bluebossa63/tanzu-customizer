/*
66 * Copyright 2021 Daniele Ulrich 
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
 * TanzuCustomizerConfiguration.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.ClientBuilder;

import org.apache.http.client.config.AuthSchemes;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;

/**
 * The Class TanzuCustomizerConfiguration is the main configuration class.
 */
@Configuration
@ComponentScan({ "ch.tanzu.customizer" })
public class TanzuCustomizerConfiguration {

	/** The context. */
	@Autowired
	WinRmClientContext context;

	/**
	 * Object mapper.
	 *
	 * @return the object mapper
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		return objectMapper;
	}

	/**
	 * Gets the factory.
	 *
	 * @return the factory
	 */
	@Bean
	public ConfigurableBeanFactory getFactory() {
		return new DefaultListableBeanFactory();
	}

	/**
	 * Gets the rest template.
	 *
	 * @return the rest template
	 */
	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate(getRequestFactory());
	}

	/**
	 * Gets the win rm client context.
	 *
	 * @return the win rm client context
	 */
	@Bean
	public WinRmClientContext getWinRmClientContext() {
		return WinRmClientContext.newInstance();
	}

	/**
	 * Gets the win rm tool.
	 *
	 * @param winrmServer   the winrm server
	 * @param winrmUser     the winrm user
	 * @param winrmPassword the winrm password
	 * @return the win rm tool
	 */
	@Bean
	public WinRmTool getWinRmTool(@Value("${winrm.server}") String winrmServer,
			@Value("${winrm.user}") String winrmUser, @Value("${winrm.password}") String winrmPassword) {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		try {
			return WinRmTool.Builder.builder(winrmServer, winrmUser, winrmPassword).context(context).useHttps(true)
					.authenticationScheme(AuthSchemes.NTLM).disableCertificateChecks(true)
					.hostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					}).sslContext(org.apache.http.ssl.SSLContexts.custom()
							.loadTrustMaterial(null, acceptingTrustStrategy).build())
					.build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Builder.
	 *
	 * @return the client builder
	 */
	@Bean
	public ClientBuilder builder() {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		try {
			return JerseyClientBuilder
					.newBuilder().readTimeout(1, TimeUnit.DAYS).sslContext(org.apache.http.ssl.SSLContexts.custom()
							.loadTrustMaterial(null, acceptingTrustStrategy).build())
					.hostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					});
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Gets the request factory.
	 *
	 * @return the request factory
	 */
	@Bean
	public HttpComponentsClientHttpRequestFactory getRequestFactory() {
		try {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
			return requestFactory;
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

}
