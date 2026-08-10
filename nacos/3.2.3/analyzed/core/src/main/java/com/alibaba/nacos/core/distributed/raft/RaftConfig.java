/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.distributed.raft;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.consistency.Config;
import com.alibaba.nacos.consistency.cp.RequestProcessor4CP;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Raft 协议 Spring 配置 Bean：集群成员、键值参数与严格就绪模式，前缀 {@link RaftSysConstants#RAFT_CONFIG_PREFIX}。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Component
@ConfigurationProperties(prefix = RaftSysConstants.RAFT_CONFIG_PREFIX)
public class RaftConfig implements Config<RequestProcessor4CP> {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 9174789390266064002L;
    
    /** Raft 运行时键值配置（选举超时、快照间隔等）。 */
    private Map<String, String> data = Collections.synchronizedMap(new HashMap<>());
    
    /** 本机成员地址（ip:raftPort）。 */
    private String selfAddress;
    
    /** 集群全部成员地址集合。 */
    private Set<String> members = Collections.synchronizedSet(new HashSet<>());
    
    /** 严格模式：就绪检查要求各 Group 均已选出 Leader。 */
    private boolean strictMode;
    
    @Override
    /** 设置本机地址并替换成员列表。 */
    public void setMembers(String self, Set<String> members) {
        this.selfAddress = self;
        this.members.clear();
        this.members.addAll(members);
    }
    
    @Override
    /** 返回本机成员地址。 */
    public String getSelfMember() {
        return selfAddress;
    }
    
    @Override
    /** 返回集群成员地址集合。 */
    public Set<String> getMembers() {
        return members;
    }
    
    @Override
    /** 追加集群成员。 */
    public void addMembers(Set<String> members) {
        this.members.addAll(members);
    }
    
    @Override
    /** 移除指定成员。 */
    public void removeMembers(Set<String> members) {
        this.members.removeAll(members);
    }
    
    /** 返回键值配置 Map。 */
    public Map<String, String> getData() {
        return data;
    }
    
    /** 替换键值配置（线程安全包装）。 */
    public void setData(Map<String, String> data) {
        this.data = Collections.synchronizedMap(data);
    }
    
    @Override
    /** 设置单个配置项。 */
    public void setVal(String key, String value) {
        data.put(key, value);
    }
    
    @Override
    /** 获取配置项，不存在返回 null。 */
    public String getVal(String key) {
        return data.get(key);
    }
    
    @Override
    /** 获取配置项，不存在则返回默认值。 */
    public String getValOfDefault(String key, String defaultVal) {
        return data.getOrDefault(key, defaultVal);
    }
    
    /** 设置是否启用严格就绪模式。 */
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }
    
    /** 是否启用严格就绪模式。 */
    public boolean isStrictMode() {
        return strictMode;
    }
    
    @Override
    public String toString() {
        try {
            return JacksonUtils.toJson(this);
        } catch (Exception e) {
            return String.valueOf(data);
        }
    }
}
