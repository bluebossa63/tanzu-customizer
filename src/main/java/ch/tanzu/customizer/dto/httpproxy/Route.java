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
 * Route.java  generated with http://www.jsonschema2pojo.org/
 *
 *  Created on: February 6, 2021
 *      Author: daniele
 */
package ch.tanzu.customizer.dto.httpproxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "conditions",
    "pathRewritePolicy",
    "services"
})
public class Route {

    @JsonProperty("conditions")
    private List<Condition> conditions = new ArrayList<Condition>();
    @JsonProperty("pathRewritePolicy")
    private PathRewritePolicy pathRewritePolicy;
    @JsonProperty("services")
    private List<Service> services = new ArrayList<Service>();
    @JsonIgnore
    private Map<String, java.lang.Object> additionalProperties = new HashMap<String, java.lang.Object>();

    @JsonProperty("conditions")
    public List<Condition> getConditions() {
        return conditions;
    }

    @JsonProperty("conditions")
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @JsonProperty("pathRewritePolicy")
    public PathRewritePolicy getPathRewritePolicy() {
        return pathRewritePolicy;
    }

    @JsonProperty("pathRewritePolicy")
    public void setPathRewritePolicy(PathRewritePolicy pathRewritePolicy) {
        this.pathRewritePolicy = pathRewritePolicy;
    }

    @JsonProperty("services")
    public List<Service> getServices() {
        return services;
    }

    @JsonProperty("services")
    public void setServices(List<Service> services) {
        this.services = services;
    }

    @JsonAnyGetter
    public Map<String, java.lang.Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, java.lang.Object value) {
        this.additionalProperties.put(name, value);
    }

}