/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.ai.model.skills;

import java.util.List;
import java.util.Map;

/**
 * Skill index manifest stored in Nacos Config for client-side caching.
 * <p>存储于 Nacos Config 的 Skill 索引清单，供客户端本地缓存；包含标签→版本映射与各版本文件列表。</p>
 *
 * <p>Contains skill metadata (labels) and per-version file storage information.
 * Stored at group={@code skill_{name}}, dataId={@code skill_index.json}.</p>
 *
 * @author nacos
 */
public class SkillIndexManifest {
    
    /** 内置标签名：指向最新发布版本。 */
    public static final String LABEL_LATEST = "latest";
    
    /**
     * Label-to-version mapping, e.g. {"latest": "v3"}.
     * <p>标签到版本的映射，例如 {@code {"latest": "v3"}}。</p>
     */
    private Map<String, String> labels;
    
    /**
     * Version-to-files mapping, e.g. {"v1": ["SKILL.md", "script/run.sh"], "v3": ["SKILL.md"]}.
     * <p>版本到文件路径列表的映射，客户端据此拉取 Skill 包内容。</p>
     */
    private Map<String, List<String>> versions;
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    public Map<String, List<String>> getVersions() {
        return versions;
    }
    
    public void setVersions(Map<String, List<String>> versions) {
        this.versions = versions;
    }
}
