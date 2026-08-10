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

package com.alibaba.nacos.naming.core;

import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.alibaba.nacos.naming.push.NamingSubscriberServiceAggregationImpl;
import com.alibaba.nacos.naming.push.NamingSubscriberServiceLocalImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 服务订阅者查询管理器。
 *
 * <p>根据 {@code aggregation} 标志选择本地或集群聚合订阅者数据源，并对结果按 {@link Subscriber#toString()} 去重。</p>
 *
 * @author Nicholas
 * @author xiweng.yy
 * @since 1.0.1
 */
@org.springframework.stereotype.Service
public class SubscribeManager {
    
    /** 本节点订阅者查询实现。 */
    @Autowired
    private NamingSubscriberServiceLocalImpl localService;
    
    /** 集群聚合订阅者查询实现。 */
    @Autowired
    private NamingSubscriberServiceAggregationImpl aggregationService;
    
    /**
     * 按服务名与命名空间获取订阅者列表。
     *
     * @param serviceName 服务名（支持模糊匹配）
     * @param namespaceId 命名空间 ID
     * @param aggregation 是否聚合集群订阅者
     * @return 订阅者列表
     */
    public List<Subscriber> getSubscribers(String serviceName, String namespaceId,
        boolean aggregation) {
        if (aggregation) {
            Collection<Subscriber> result =
                aggregationService.getFuzzySubscribers(namespaceId, serviceName);
            return CollectionUtils.isNotEmpty(result)
                ? result.stream().filter(distinctByKey(Subscriber::toString))
                    .collect(Collectors.toList())
                : Collections.emptyList();
        } else {
            return new LinkedList<>(localService.getFuzzySubscribers(namespaceId, serviceName));
        }
    }
    
    /**
     * 按 {@link Service} 对象获取订阅者列表。
     *
     * @param service     服务对象
     * @param aggregation 是否聚合集群订阅者
     * @return 订阅者列表
     */
    public List<Subscriber> getSubscribers(Service service, boolean aggregation) {
        if (aggregation) {
            Collection<Subscriber> result = aggregationService.getSubscribers(service);
            return CollectionUtils.isNotEmpty(result)
                ? result.stream().filter(distinctByKey(Subscriber::toString))
                    .collect(Collectors.toList())
                : Collections.emptyList();
        } else {
            return new LinkedList<>(localService.getSubscribers(service));
        }
    }
    
    /** 基于键提取函数的去重谓词，线程安全。 */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>(128);
        return object -> seen.putIfAbsent(keyExtractor.apply(object), Boolean.TRUE) == null;
    }
}
