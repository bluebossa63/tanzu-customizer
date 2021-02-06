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
 * KubernetesAccessTestCase.java
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.tanzu.customizer.service.KubernetesHTTPProxyWatcherService;
import io.kubernetes.client.openapi.ApiException;

@SpringBootTest
class KubernetesAccessTestCase {

	@Autowired
	KubernetesHTTPProxyWatcherService kubernetesHTTPProxyWatcherService;
	
	@Test
	void test() throws ApiException, FileNotFoundException, IOException, InterruptedException {

		Thread.sleep(100000000);
		
	}
}
