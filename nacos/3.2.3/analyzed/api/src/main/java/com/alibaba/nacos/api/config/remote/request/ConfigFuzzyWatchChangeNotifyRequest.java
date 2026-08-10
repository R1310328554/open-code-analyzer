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

package com.alibaba.nacos.api.config.remote.request;

/**
 * 模糊监听配置变更通知请求。
 *
 * <p>当匹配模糊订阅模式的配置发生变更时，服务端向客户端推送本请求。</p>
 *
 * @author stone-98
 * @date 2024/3/13
 */
public class ConfigFuzzyWatchChangeNotifyRequest extends AbstractFuzzyWatchNotifyRequest {
    
    /** 发生变更的配置 groupKey（tenant@@group@@dataId）。 */
    private String groupKey;
    
    /** 变更类型，如新增或删除。 */
    private String changeType;
    
    /** 无参构造，供序列化使用。 */
    public ConfigFuzzyWatchChangeNotifyRequest() {
    }
    
    /**
     * 构造模糊监听变更通知。
     *
     * @param groupKey   变更配置的 groupKey
     * @param changeType 变更类型
     */
    public ConfigFuzzyWatchChangeNotifyRequest(String groupKey, String changeType) {
        this.groupKey = groupKey;
        this.changeType = changeType;
    }
    
    /** 获取变更配置的 groupKey。 */
    public String getGroupKey() {
        return groupKey;
    }
    
    /** 设置变更配置的 groupKey。 */
    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
    
    /** 获取变更类型。 */
    public String getChangeType() {
        return changeType;
    }
    
    /** 设置变更类型。 */
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
    
    /**
     * 返回请求的字符串表示。
     *
     * @return 调试字符串
     */
    @Override
    public String toString() {
        return "FuzzyListenNotifyChangeRequest{" + '\'' + ", groupKey='" + groupKey + '\''
            + ", changeType="
            + changeType + '}';
    }
    
}
