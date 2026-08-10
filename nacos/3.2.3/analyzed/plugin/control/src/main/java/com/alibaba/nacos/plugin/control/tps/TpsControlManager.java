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

package com.alibaba.nacos.plugin.control.tps;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.rule.parser.NacosTpsControlRuleParser;
import com.alibaba.nacos.plugin.control.rule.parser.TpsControlRuleParser;
import com.alibaba.nacos.plugin.control.rule.storage.RuleStorageProxy;
import com.alibaba.nacos.plugin.control.tps.barrier.TpsBarrier;
import com.alibaba.nacos.plugin.control.tps.barrier.creator.DefaultNacosTpsBarrierCreator;
import com.alibaba.nacos.plugin.control.tps.barrier.creator.TpsBarrierCreator;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;

import java.util.Map;

/**
 * TPS 限流管理器抽象基类。
 *
 * <p>封装规则解析器与屏障创建器的初始化，提供从本地/外部存储加载规则、
 * 注册限流点、应用规则及执行 TPS 检查的模板流程，具体限流策略由子类实现。</p>
 *
 * @author shiyiyue
 */
public abstract class TpsControlManager {
    
    /** TPS 管控规则解析器。 */
    private final TpsControlRuleParser tpsControlRuleParser;
    
    /** TPS 屏障创建器，用于为各限流点实例化屏障。 */
    protected final TpsBarrierCreator tpsBarrierCreator;
    
    protected TpsControlManager() {
        this.tpsControlRuleParser = buildTpsControlRuleParser();
        this.tpsBarrierCreator = buildTpsBarrierCreator();
    }
    
    /**
     * 获取 TPS 规则解析器。
     *
     * @return 规则解析器实例
     */
    public TpsControlRuleParser getTpsControlRuleParser() {
        return tpsControlRuleParser;
    }
    
    /** 构建 TPS 规则解析器，默认使用 Nacos 内置解析器。 */
    protected TpsControlRuleParser buildTpsControlRuleParser() {
        return new NacosTpsControlRuleParser();
    }
    
    /**
     * 构建 TPS 屏障创建器，用于为各限流点创建屏障实例。
     *
     * @return 当前插件使用的 {@link TpsBarrierCreator} 实现
     */
    protected TpsBarrierCreator buildTpsBarrierCreator() {
        return new DefaultNacosTpsBarrierCreator();
    }
    
    /**
     * 从本地磁盘或外部存储加载指定限流点的规则并应用。
     *
     * @param pointName 限流点名称
     */
    protected void initTpsRule(String pointName) {
        RuleStorageProxy ruleStorageProxy = RuleStorageProxy.getInstance();
        
        String localRuleContent = ruleStorageProxy.getLocalDiskStorage().getTpsRule(pointName);
        if (StringUtils.isNotBlank(localRuleContent)) {
            Loggers.CONTROL.info("Found local disk tps control rule of {},content ={}", pointName,
                localRuleContent);
        } else if (ruleStorageProxy.getExternalStorage() != null
            && ruleStorageProxy.getExternalStorage().getTpsRule(pointName) != null) {
            localRuleContent = ruleStorageProxy.getExternalStorage().getTpsRule(pointName);
            if (StringUtils.isNotBlank(localRuleContent)) {
                Loggers.CONTROL.info("Found external  tps control rule of {},content ={}",
                    pointName, localRuleContent);
            }
        }
        
        if (StringUtils.isNotBlank(localRuleContent)) {
            TpsControlRule tpsLimitRule = tpsControlRuleParser.parseRule(localRuleContent);
            this.applyTpsRule(pointName, tpsLimitRule);
        } else {
            Loggers.CONTROL.info("No tps control rule of {} found,content ={}  ", pointName,
                localRuleContent);
        }
    }
    
    /**
     * 注册 TPS 限流点。
     *
     * @param pointName 限流点名称
     */
    public abstract void registerTpsPoint(String pointName);
    
    /**
     * 获取所有已注册限流点及其屏障。
     *
     * @return 限流点名称到屏障的映射
     */
    public abstract Map<String, TpsBarrier> getPoints();
    
    /**
     * 获取所有限流点当前生效的规则。
     *
     * @return 限流点名称到规则的映射
     */
    public abstract Map<String, TpsControlRule> getRules();
    
    /**
     * 应用或更新指定限流点的 TPS 规则。
     *
     * @param pointName 限流点名称
     * @param rule      新规则
     */
    public abstract void applyTpsRule(String pointName, TpsControlRule rule);
    
    /**
     * 执行 TPS 限流检查。
     *
     * @param tpsRequest TPS 检查请求
     * @return 检查结果，包含是否放行及原因码
     */
    public abstract TpsCheckResponse check(TpsCheckRequest tpsRequest);
    
    /**
     * 获取管控管理器名称标识。
     *
     * @return 管理器名称
     */
    public abstract String getName();
}
