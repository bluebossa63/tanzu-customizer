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
 * ObjectMapperProvider.java
 *
 *  Created on: January 2, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class ObjectMapperProvider.
 *
 * @author daniele
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	/** The mapper. */
	private ObjectMapper mapper;

	/**
	 * Instantiates a new object mapper provider.
	 */
	public ObjectMapperProvider() {
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	/**
	 * Gets the context.
	 *
	 * @param type the type
	 * @return the context
	 */
	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

}
