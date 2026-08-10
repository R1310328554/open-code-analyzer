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

package org.keycloak.models;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
* 必需操作 Provider 模型：描述 Realm 中注册的必需操作及其优先级与配置。
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Revision: 1 $
*/
public class RequiredActionProviderModel implements Serializable {

    /** 按优先级与名称排序必需操作的比较器。 */
    public interface RequiredActionComparator extends Comparator<RequiredActionProviderModel> {
        RequiredActionComparator SINGLETON = Comparator
            .comparingInt(RequiredActionProviderModel::getPriority)
            .thenComparing(RequiredActionProviderModel::getName, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
            ::compare;
    }

    private String id;
    private String alias;
    private String name;
    private String providerId;
    private boolean enabled;
    private boolean defaultAction;
    private int priority;
    private Map<String, String> config = new HashMap<>();


    /** @return 唯一标识符 */
    public String getId() {
        return id;
    }

    /** @param id 唯一标识符 */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 必需操作别名 */
    public String getAlias() {
        return alias;
    }

    /** @param alias 必需操作别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 用于界面展示的友好名称（与 alias 可能不同）。
     * Used for display purposes.  Probably should clean this code up and make alias and name the same, but
     * the old code references an Enum and the admin console creates a "friendly" name for each enum.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /** @param name 显示名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 是否启用 */
    public boolean isEnabled() {
        return enabled;
    }

    /** @param enabled 是否启用 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 是否为默认必需操作 */
    public boolean isDefaultAction() {
        return defaultAction;
    }

    /** @param defaultAction 是否为默认必需操作 */
    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    /** @return Provider 实现 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId Provider 实现 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 执行优先级 */
    public int getPriority() {
        return priority;
    }

    /** @param priority 执行优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /** @return 配置项映射 */
    public Map<String, String> getConfig() {
        return config;
    }

    /** @param config 配置项映射 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
