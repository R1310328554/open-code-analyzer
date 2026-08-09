/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls.rule;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Envoy RLS 规则与 Sentinel {@link com.alibaba.csp.sentinel.slots.block.flow.FlowRule} 之间的转换器。
 *
 * @author Eric Zhao
 * @since 1.7.0
 */
public final class EnvoySentinelRuleConverter {

    /** 键值条目分隔符，当前使用 "|"。 */

    public static final String SEPARATOR = "|";

    /**
     * 将合法 {@link EnvoyRlsRule} 转换为 Sentinel 流控规则列表。
     *
     * @param rule 合法的 Envoy RLS 规则
     * @return 转换后的流控规则列表
     */
    public static List<FlowRule> toSentinelFlowRules(EnvoyRlsRule rule) {
        if (!EnvoyRlsRuleManager.isValidRule(rule)) {
            throw new IllegalArgumentException("Not a valid RLS rule");
        }
        return rule.getDescriptors().stream()
            .map(e -> toSentinelFlowRule(rule.getDomain(), e))
            .collect(Collectors.toList());
    }

    /**
     * 将单个资源描述符转换为集群模式 {@link com.alibaba.csp.sentinel.slots.block.flow.FlowRule}。
     *
     * @param domain 限流 domain
     * @param descriptor 资源描述符
     * @return 对应的流控规则
     */
    public static FlowRule toSentinelFlowRule(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {
        // 每个 descriptor 仅对应一条流控规则。
        String identifier = generateKey(domain, descriptor);
        long flowId = generateFlowId(identifier);
        return new FlowRule(identifier)
            .setCount(descriptor.getCount())
            .setClusterMode(true)
            .setClusterConfig(new ClusterFlowConfig()
                .setFlowId(flowId)
                .setThresholdType(ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL)
                .setSampleCount(1)
                .setFallbackToLocalWhenFail(false));
    }

    /**
     * 根据资源键生成集群流控规则 ID。
     *
     * @param key 资源标识键
     * @return 流控规则 ID，key 为空时返回 -1
     */
    public static long generateFlowId(String key) {
        if (StringUtil.isBlank(key)) {
            return -1L;
        }
        // 加偏移量以避免生成负 ID。
        return (long) Integer.MAX_VALUE + key.hashCode();
    }

    /**
     * 根据 domain 与 descriptor 中的键值资源生成 Sentinel 资源名。
     *
     * @param domain 限流 domain
     * @param descriptor 资源描述符
     * @return 拼接后的资源键
     */
    public static String generateKey(String domain, EnvoyRlsRule.ResourceDescriptor descriptor) {
        AssertUtil.assertNotBlank(domain, "domain cannot be blank");
        AssertUtil.notNull(descriptor, "EnvoyRlsRule.ResourceDescriptor cannot be null");
        AssertUtil.assertNotEmpty(descriptor.getResources(), "resources in descriptor cannot be null");

        StringBuilder sb = new StringBuilder(domain);
        for (EnvoyRlsRule.KeyValueResource resource : descriptor.getResources()) {
            sb.append(SEPARATOR).append(resource.getKey()).append(SEPARATOR).append(resource.getValue());
        }
        return sb.toString();
    }

    private EnvoySentinelRuleConverter() {}
}
