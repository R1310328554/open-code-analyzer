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


import java.util.List;

/**
 * Keycloak 运行 Profile 的 REST 表示，汇总当前 profile 名称及各类功能开关列表。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ProfileInfoRepresentation {

    /** 当前激活的 profile 名称（如 {@code prod}、{@code preview}）。 */
    private String name;
    /** 已禁用的功能名称列表。 */
    private List<String> disabledFeatures;
    /** 处于预览阶段的功能名称列表。 */
    private List<String> previewFeatures;
    /** 处于实验阶段的功能名称列表。 */
    private List<String> experimentalFeatures;

    /** @return profile 名称 */
    public String getName() {
        return name;
    }

    /** @param name profile 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 已禁用功能列表 */
    public List<String> getDisabledFeatures() {
        return disabledFeatures;
    }

    /** @param disabledFeatures 已禁用功能列表 */
    public void setDisabledFeatures(List<String> disabledFeatures) {
        this.disabledFeatures = disabledFeatures;
    }

    /** @return 预览功能列表 */
    public List<String> getPreviewFeatures() {
        return previewFeatures;
    }

    /** @param previewFeatures 预览功能列表 */
    public void setPreviewFeatures(List<String> previewFeatures) {
        this.previewFeatures = previewFeatures;
    }

    /** @return 实验功能列表 */
    public List<String> getExperimentalFeatures() {
        return experimentalFeatures;
    }

    /** @param experimentalFeatures 实验功能列表 */
    public void setExperimentalFeatures(List<String> experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

}
