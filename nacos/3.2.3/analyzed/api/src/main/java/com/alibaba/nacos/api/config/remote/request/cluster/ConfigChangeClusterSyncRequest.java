/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.remote.request.cluster;

import com.alibaba.nacos.api.config.remote.request.AbstractConfigRequest;

/**
 * 集群间配置变更同步请求。
 *
 * <p>用于 Nacos 集群节点间传播配置变更事件，携带最后修改时间、灰度名称等元信息。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigChangeClusterSyncRequest.java, v 0.1 2020年08月11日 4:30 PM liuzunfei Exp $
 */
public class ConfigChangeClusterSyncRequest extends AbstractConfigRequest {
    
    /** 配置最后修改时间戳（毫秒）。 */
    long lastModified;
    
    /** 灰度发布名称。 */
    String grayName;
    
    /** @deprecated 已废弃，请使用 grayName。 */
    @Deprecated
    boolean isBeta;
    
    /** @deprecated 已废弃，请使用 grayName。 */
    @Deprecated
    String tag;
    
    /** @deprecated 是否为 Beta 配置，已废弃。 */
    public boolean isBeta() {
        return isBeta;
    }
    
    /** @deprecated 设置 Beta 标记，已废弃。 */
    public void setBeta(boolean beta) {
        isBeta = beta;
    }
    
    /**
     * 获取配置标签（已废弃）。
     *
     * @return 标签值
     * @deprecated 请使用 {@link #getGrayName()}
     */
    public String getTag() {
        return tag;
    }
    
    /**
     * 设置配置标签（已废弃）。
     *
     * @param tag 标签值
     * @deprecated 请使用 {@link #setGrayName(String)}
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    /** 获取灰度发布名称。 */
    public String getGrayName() {
        return grayName;
    }
    
    /** 设置灰度发布名称。 */
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    /**
     * 获取配置最后修改时间戳。
     *
     * @return 毫秒时间戳
     */
    public long getLastModified() {
        return lastModified;
    }
    
    /**
     * 设置配置最后修改时间戳。
     *
     * @param lastModified 毫秒时间戳
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
}
