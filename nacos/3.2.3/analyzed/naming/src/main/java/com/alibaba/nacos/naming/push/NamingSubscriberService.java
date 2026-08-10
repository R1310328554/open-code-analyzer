/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.push;

import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.pojo.Subscriber;

import java.util.Collection;

/**
 * Naming 订阅者查询服务接口。
 *
 * <p>提供精确与模糊查询指定服务的推送目标订阅者列表，供运维与推送链路使用。</p>
 *
 * @author xiweng.yy
 */
public interface NamingSubscriberService {
    
    /**
     * 获取指定命名空间与服务名的全部推送目标订阅者。
     * TODO use {@link com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo} replaced return
     *
     * @param namespaceId 命名空间 ID
     * @param serviceName 服务名（可含分组）
     * @return 订阅者集合
     */
    Collection<Subscriber> getSubscribers(String namespaceId, String serviceName);
    
    /**
     * 获取指定 {@link Service} 的全部推送目标订阅者。
     * TODO use {@link com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo} replaced return
     *
     * @param service 服务对象
     * @return 订阅者集合
     */
    Collection<Subscriber> getSubscribers(Service service);
    
    /**
     * 模糊查询订阅者，仅支持服务名/分组名包含匹配。
     *
     * <p>警告：性能开销较大，应谨慎使用。
     * TODO use {@link com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo} replaced return
     *
     * @param namespaceId 命名空间 ID
     * @param serviceName 模糊服务名
     * @return 匹配的订阅者集合
     */
    Collection<Subscriber> getFuzzySubscribers(String namespaceId, String serviceName);
    
    /**
     * 按 {@link Service} 模糊查询订阅者。
     *
     * <p>警告：性能开销较大，应谨慎使用。
     * TODO use {@link com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo} replaced return
     *
     * @param service 服务对象
     * @return 匹配的订阅者集合
     */
    Collection<Subscriber> getFuzzySubscribers(Service service);
}
