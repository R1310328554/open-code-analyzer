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

package com.alibaba.nacos.plugin.control.tps;

import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.tps.barrier.TpsBarrier;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.response.TpsResultCode;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认 TPS 限流管理器，无实际限流能力（始终放行）。
 *
 * <p>作为降级兜底实现：注册限流点并维护规则与屏障映射，但 {@link #check} 始终返回跳过结果。
 * 适用于未配置管控插件或需要关闭限流的场景。</p>
 *
 * @author shiyiyue
 */
public class DefaultTpsControlManager extends TpsControlManager {
    
    /**
     * 限流点名称 → TPS 屏障实例。
     */
    protected final Map<String, TpsBarrier> points = new ConcurrentHashMap<>(16);
    
    /**
     * 限流点名称 → TPS 管控规则。
     */
    protected final Map<String, TpsControlRule> rules = new ConcurrentHashMap<>(16);
    
    public DefaultTpsControlManager() {
    }
    
    /**
     * 注册 TPS 限流点并创建对应屏障；若已有规则则立即应用，否则从存储加载。
     *
     * @param pointName 限流点名称
     */
    public synchronized void registerTpsPoint(String pointName) {
        if (!points.containsKey(pointName)) {
            points.put(pointName, tpsBarrierCreator.createTpsBarrier(pointName));
            if (rules.containsKey(pointName)) {
                points.get(pointName).applyRule(rules.get(pointName));
            } else {
                initTpsRule(pointName);
            }
        }
        Loggers.CONTROL
            .warn(
                "Tps point for {} registered, But tps control manager is no limit implementation.",
                pointName);
    }
    
    /**
     * 应用或清除指定限流点的 TPS 规则。
     *
     * @param pointName 限流点名称
     * @param rule      新规则，{@code null} 或空规则表示清除
     */
    public synchronized void applyTpsRule(String pointName, TpsControlRule rule) {
        if (rule == null || rule.getPointRule() == null) {
            rules.remove(pointName);
        } else {
            rules.put(pointName, rule);
        }
        if (points.containsKey(pointName)) {
            points.get(pointName).applyRule(rule);
        }
        Loggers.CONTROL.warn(
            "Tps rule for point name {} updated, But tps control manager is no limit implementation.",
            pointName);
    }
    
    /**
     * 获取所有已注册限流点及其屏障。
     *
     * @return 限流点名称到屏障的映射
     */
    public Map<String, TpsBarrier> getPoints() {
        return points;
    }
    
    /**
     * 获取所有限流点当前生效的规则。
     *
     * @return 限流点名称到规则的映射
     */
    public Map<String, TpsControlRule> getRules() {
        return rules;
    }
    
    /**
     * 执行 TPS 检查；本实现始终跳过限流并返回放行。
     *
     * @param tpsRequest TPS 检查请求
     * @return 始终为 {@link TpsResultCode#CHECK_SKIP} 的放行响应
     */
    public TpsCheckResponse check(TpsCheckRequest tpsRequest) {
        return new TpsCheckResponse(true, TpsResultCode.CHECK_SKIP, "skip");
        
    }
    
    /** 返回管理器标识，表示无限制模式。 */
    @Override
    public String getName() {
        return "noLimit";
    }
}
