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

package com.alibaba.nacos.consistency;

import java.io.Serializable;
import java.util.Set;

/**
 * 一致性协议配置对象：管理集群成员与键值配置项。
 *
 * <p>{@link RequestProcessor} 抽象了各业务的事务处理，使不同服务的日志处理互不阻塞；
 * 各一致性协议实现会主动发现并绑定对应的 LogProcessor（如 LogProcessor4AP / LogProcessor4CP）。
 *
 * <p>Consistent protocol related configuration objects.
 *
 * <p>{@link RequestProcessor} : The consistency protocol provides services for all businesses, but each business only cares
 * about the transaction information belonging to that business, and the transaction processing between the various
 * services should not block each other. Therefore, the LogProcessor is abstracted to implement the parallel processing
 * of transactions of different services. Corresponding LogProcessor sub-interface: LogProcessor4AP or LogProcessor4CP,
 * different consistency protocols will actively discover the corresponding LogProcessor
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface Config<L extends RequestProcessor> extends Serializable {
    
    /**
     * 设置集群节点信息以完成初始化，格式如 [ip:port, ip:port, ...]。
     * Set the cluster node information to initialize，like [ip:port, ip:port, ip:port].
     *
     * @param self    local node address information, ip:port
     * @param members {@link Set}
     */
    void setMembers(String self, Set<String> members);
    
    /**
     * 集群成员加入。
     * members join.
     *
     * @param members {@link Set}
     */
    void addMembers(Set<String> members);
    
    /**
     * 集群成员离开。
     * members leave.
     *
     * @param members {@link Set}
     */
    void removeMembers(Set<String> members);
    
    /**
     * 获取本节点地址（ip:port）。
     * get local node address info.
     *
     * @return address
     */
    String getSelfMember();
    
    /**
     * 获取集群全部成员地址列表。
     * get the cluster node information.
     *
     * @return members info, like [ip:port, ip:port, ip:port]
     */
    Set<String> getMembers();
    
    /**
     * 写入配置项。
     * Add configuration content.
     *
     * @param key   config key
     * @param value config value
     */
    void setVal(String key, String value);
    
    /**
     * 按 key 读取配置项。
     * get configuration content by key.
     *
     * @param key config key
     * @return config value
     */
    String getVal(String key);
    
    /**
     * 按 key 读取配置项，不存在时返回默认值。
     * get configuration content by key, if not found, use default-val.
     *
     * @param key        config key
     * @param defaultVal default value
     * @return config value
     */
    String getValOfDefault(String key, String defaultVal);
    
}
