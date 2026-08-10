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

package org.keycloak.representations.info;

import java.util.Map;

/**
 * 单个 SPI Provider 实现的 REST 表示，包含 UI 排序优先级与运行时运维信息。
 */
public class ProviderRepresentation {

    /** Provider 在管理控制台中的显示顺序（数值越小越靠前）。 */
    private int order;

    /** 键值对形式的运维/版本信息，供 Admin UI 展示。 */
    private Map<String, String> operationalInfo;

    /** @return 显示顺序 */
    public int getOrder() {
        return order;
    }

    /** @param priorityUI 显示顺序 */
    public void setOrder(int priorityUI) {
        this.order = priorityUI;
    }

    /** @return 运维信息映射 */
    public Map<String, String> getOperationalInfo() {
        return operationalInfo;
    }

    /** @param operationalInfo 运维信息映射 */
    public void setOperationalInfo(Map<String, String> operationalInfo) {
        this.operationalInfo = operationalInfo;
    }

}
