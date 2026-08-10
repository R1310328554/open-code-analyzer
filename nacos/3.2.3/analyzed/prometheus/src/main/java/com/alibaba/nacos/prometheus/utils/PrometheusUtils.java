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

package com.alibaba.nacos.prometheus.utils;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Prometheus 服务发现 JSON 组装工具。
 *
 * <p>将 Nacos {@link Instance} 集合转为 Prometheus HTTP SD 所需的 {@code targets} 与 {@code labels} 结构，并规范化 metadata 标签名。</p>
 *
 * @author Joey777210
 */
public class PrometheusUtils {
    
    /**
     * 按集群名分组实例并追加到 SD 数组节点。
     */
    /** 遍历分组后的实例，逐个写入 arrayNode。 */
    public static void assembleArrayNodes(Set<Instance> targetSet, ArrayNode arrayNode) {
        Map<String, List<Instance>> groupingInsMap =
            targetSet.stream().collect(groupingBy(Instance::getClusterName));
        groupingInsMap.forEach((key, value) -> {
            for (Instance instance : value) {
                ObjectNode jsonNode = assembleInstanceToArrayNode(key, instance);
                arrayNode.add(jsonNode);
            }
        });
    }
    
    /**
     * 将单个实例转为 Prometheus SD 条目 JSON。
     *
     * <p>targets 为 ip:port，labels 含集群名与实例 metadata（键名中 . 与 - 转为 _）。</p>
     *
     * @param clusterName 集群名称
     * @param instance 实例信息
     */
    private static ObjectNode assembleInstanceToArrayNode(String clusterName, Instance instance) {
        
        ArrayNode targetsNode = JacksonUtils.createEmptyArrayNode();
        targetsNode.add(instance.getIp() + ":" + instance.getPort());
        ObjectNode labelNode = JacksonUtils.createEmptyJsonNode();
        // 写入 __meta_clusterName 标签
        labelNode.put("__meta_clusterName", clusterName);
        // 导出实例 metadata 为 Prometheus labels
        Map<String, String> metadata = instance.getMetadata();
        // 标签名中的点与横线自动替换为下划线
        metadata = metadata.entrySet().stream().collect(Collectors
            .toMap(e -> e.getKey().replace(".", "_").replace("-", "_"), e -> e.getValue()));
        
        metadata.forEach(labelNode::put);
        ObjectNode jsonNode = JacksonUtils.createEmptyJsonNode();
        jsonNode.replace("targets", targetsNode);
        jsonNode.replace("labels", labelNode);
        return jsonNode;
    }
}
