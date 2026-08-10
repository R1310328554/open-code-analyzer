/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.representations.idm.authorization;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用授权策略的 REST 表示，通过键值对 {@code config} 承载策略提供方特定配置。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyRepresentation extends AbstractPolicyRepresentation {

    /** 策略提供方特定的配置项（键 → 值）。 */
    private Map<String, String> config = new HashMap();

    /** @return 策略配置映射 */
    public Map<String, String> getConfig() {
        return this.config;
    }

    /** @param config 策略配置映射 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
