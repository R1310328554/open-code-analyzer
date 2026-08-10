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

package com.alibaba.nacos.client.naming.selector;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.selector.NamingSelector;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 命名选择器工厂。
 *
 * <p>提供集群、IP 正则、元数据及健康状态等常用 {@link NamingSelector} 的静态构造方法，供订阅回调前过滤实例列表。</p>
 *
 * @author lideyou
 */
public final class NamingSelectorFactory {
    
    /** 空选择器：原样返回上下文中的全部实例。 */
    public static final NamingSelector EMPTY_SELECTOR = context -> context::getInstances;
    
    /** 健康实例选择器：仅保留 {@link Instance#isHealthy()} 为 true 的实例。 */
    public static final NamingSelector HEALTHY_SELECTOR =
        new DefaultNamingSelector(Instance::isHealthy);
    
    /** 按集群名过滤的内部选择器，支持基于 clusterString 的 equals/hashCode。 */
    private static class ClusterSelector extends DefaultNamingSelector {
        
        /** 排序后的集群名拼接串，用于选择器相等性判定。 */
        private final String clusterString;
        
        public ClusterSelector(Predicate<Instance> filter, String clusterString) {
            super(filter);
            this.clusterString = clusterString;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClusterSelector that = (ClusterSelector) o;
            return Objects.equals(this.clusterString, that.clusterString);
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(this.clusterString);
        }
    }
    
    /** 工具类，禁止实例化。 */
    private NamingSelectorFactory() {
    }
    
    /**
     * 创建集群选择器。
     *
     * <p>clusters 为空时返回 {@link #EMPTY_SELECTOR}。</p>
     *
     * @param clusters 目标集群名集合
     * @return 集群选择器
     */
    public static NamingSelector newClusterSelector(Collection<String> clusters) {
        if (CollectionUtils.isNotEmpty(clusters)) {
            final Set<String> set = new HashSet<>(clusters);
            Predicate<Instance> filter = instance -> set.contains(instance.getClusterName());
            String clusterString = getUniqueClusterString(clusters);
            return new ClusterSelector(filter, clusterString);
        } else {
            return EMPTY_SELECTOR;
        }
    }
    
    /**
     * 创建 IP 正则选择器。
     *
     * @param regex IP 匹配正则表达式
     * @return IP 选择器
     */
    public static NamingSelector newIpSelector(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("The parameter 'regex' cannot be null.");
        }
        return new DefaultNamingSelector(instance -> Pattern.matches(regex, instance.getIp()));
    }
    
    /**
     * 创建元数据选择器（全部键值均需匹配）。
     *
     * @param metadata 待匹配的元数据键值对
     * @return 元数据选择器
     */
    public static NamingSelector newMetadataSelector(Map<String, String> metadata) {
        return newMetadataSelector(metadata, false);
    }
    
    /**
     * 创建元数据选择器。
     *
     * @param metadata 目标元数据
     * @param isAny {@code true} 表示任一键值匹配即可；{@code false} 表示全部键值均需匹配
     * @return 元数据选择器
     */
    public static NamingSelector newMetadataSelector(Map<String, String> metadata, boolean isAny) {
        if (metadata == null) {
            throw new IllegalArgumentException("The parameter 'metadata' cannot be null.");
        }
        
        Predicate<Instance> filter = instance -> instance.getMetadata().size() >= metadata.size();
        
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            Predicate<Instance> nextFilter = instance -> {
                Map<String, String> map = instance.getMetadata();
                return Objects.equals(map.get(entry.getKey()), entry.getValue());
            };
            if (isAny) {
                filter = filter.or(nextFilter);
            } else {
                filter = filter.and(nextFilter);
            }
        }
        return new DefaultNamingSelector(filter);
    }
    
    /** 将集群名集排序后用逗号拼接，保证相同集合产生唯一字符串。 */
    public static String getUniqueClusterString(Collection<String> cluster) {
        TreeSet<String> treeSet = new TreeSet<>(cluster);
        return StringUtils.join(treeSet, ",");
    }
    
}
