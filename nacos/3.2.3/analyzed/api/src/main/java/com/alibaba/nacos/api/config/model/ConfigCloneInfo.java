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

package com.alibaba.nacos.api.config.model;

import java.io.Serializable;

/**
 * Nacos 配置克隆请求信息。
 *
 * <p>指定源配置存储 ID 及克隆后的目标 group/dataId，用于批量克隆 Open API。</p>
 *
 * @author xiweng.yy
 */
public class ConfigCloneInfo implements Serializable {
    
    private static final long serialVersionUID = -53761233218121703L;
    
    /** 待克隆配置的存储层 ID（非 dataId），取自 {@link ConfigBasicInfo#getId()}。 */
    private Long configId;
    
    /** 克隆后的目标 group；未设置时沿用源配置 group。 */
    private String targetGroupName;
    
    /** 克隆后的目标 dataId；未设置时沿用源配置 dataId。 */
    private String targetDataId;
    
    /** 获取待克隆配置的存储 ID。 */
    public Long getConfigId() {
        return configId;
    }
    
    /** 设置待克隆配置的存储 ID。 */
    public void setConfigId(Long configId) {
        this.configId = configId;
    }
    
    /** 获取克隆目标 group。 */
    public String getTargetGroupName() {
        return targetGroupName;
    }
    
    /** 设置克隆目标 group。 */
    public void setTargetGroupName(String targetGroupName) {
        this.targetGroupName = targetGroupName;
    }
    
    /** 获取克隆目标 dataId。 */
    public String getTargetDataId() {
        return targetDataId;
    }
    
    /** 设置克隆目标 dataId。 */
    public void setTargetDataId(String targetDataId) {
        this.targetDataId = targetDataId;
    }
}
