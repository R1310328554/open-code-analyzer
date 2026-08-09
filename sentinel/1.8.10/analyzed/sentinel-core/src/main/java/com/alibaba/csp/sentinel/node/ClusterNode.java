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
package com.alibaba.csp.sentinel.node;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * <p>
 * 保存资源的汇总运行时统计（RT、线程数、QPS 等）。
 * 相同资源全局共享同一 {@link ClusterNode}，与所在 {@link com.alibaba.csp.sentinel.context.Context} 无关。
 * </p>
 * <p>
 * 为区分不同来源（origin，在 {@link ContextUtil#enter(String name, String origin)} 中声明）的调用，
 * 每个 {@link ClusterNode} 持有 {@link #originCountMap}，映射各来源对应的 {@link StatisticNode}。
 * 通过 {@link #getOrCreateOriginNode(String)} 获取指定来源的 {@link Node}。<br/>
 * 通常 origin 为服务消费者的应用名。
 * </p>
 *
 * @author qinan.qn
 * @author jialiang.linjl
 */
public class ClusterNode extends StatisticNode {

    private final String name;
    private final int resourceType;

    public ClusterNode(String name) {
        this(name, ResourceTypeConstants.COMMON);
    }

    public ClusterNode(String name, int resourceType) {
        AssertUtil.notEmpty(name, "name cannot be empty");
        this.name = name;
        this.resourceType = resourceType;
    }

    /**
     * <p>来源映射表，保存某资源下 (origin, originNode) 对。</p>
     * <p>
     * 应用运行越久映射越稳定；仅在初期创建节点时需要加锁，
     * 因此使用 HashMap + 锁，而非全程持锁的并发 Map。
     * </p>
     */
    private Map<String, StatisticNode> originCountMap = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 获取资源节点对应的资源名。
     *
     * @return 资源名
     * @since 1.7.0
     */
    public String getName() {
        return name;
    }

    /**
     * 获取资源分类（类型）。
     *
     * @return 资源类型
     * @since 1.7.0
     */
    public int getResourceType() {
        return resourceType;
    }

    /**
     * <p>获取指定来源的 {@link Node}，通常 origin 为服务消费者的应用名。</p>
     * <p>若该来源节点尚不存在，则创建并返回新的 {@link StatisticNode}。</p>
     *
     * @param origin 调用方名称，在 {@link ContextUtil#enter(String name, String origin)} 的 origin 参数中指定
     * @return 指定来源对应的 {@link Node}
     */
    public Node getOrCreateOriginNode(String origin) {
        StatisticNode statisticNode = originCountMap.get(origin);
        if (statisticNode == null) {
            lock.lock();
            try {
                statisticNode = originCountMap.get(origin);
                if (statisticNode == null) {
                    // 来源节点不存在，为该 origin 创建新节点。
                    statisticNode = new StatisticNode();
                    HashMap<String, StatisticNode> newMap = new HashMap<>(originCountMap.size() + 1);
                    newMap.putAll(originCountMap);
                    newMap.put(origin, statisticNode);
                    originCountMap = newMap;
                }
            } finally {
                lock.unlock();
            }
        }
        return statisticNode;
    }

    public Map<String, StatisticNode> getOriginCountMap() {
        return originCountMap;
    }

}
