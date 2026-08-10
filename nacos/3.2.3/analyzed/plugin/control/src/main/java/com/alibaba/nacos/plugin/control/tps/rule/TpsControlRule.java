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

package com.alibaba.nacos.plugin.control.tps.rule;

/**
 * TPS 限流点管控规则。
 *
 * <p>将限流点名称与其对应的 {@link RuleDetail} 绑定，供 TPS 屏障加载与应用。</p>
 *
 * @author liuzunfei
 * @version $Id: TpsControlPoint.java, v 0.1 2021年01月09日 12:38 PM liuzunfei Exp $
 */
public class TpsControlRule {
    
    /** 限流点名称。 */
    private String pointName;
    
    /** 限流点级规则明细。 */
    private RuleDetail pointRule;
    
    /**
     * 获取限流点名称。
     *
     * @return 限流点名称
     */
    public String getPointName() {
        return pointName;
    }
    
    /**
     * 设置限流点名称。
     *
     * @param pointName 限流点名称
     */
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
    /**
     * 获取限流点规则明细。
     *
     * @return 规则明细
     */
    public RuleDetail getPointRule() {
        return pointRule;
    }
    
    /**
     * 设置限流点规则明细。
     *
     * @param pointRule 规则明细
     */
    public void setPointRule(RuleDetail pointRule) {
        this.pointRule = pointRule;
    }
    
    @Override
    public String toString() {
        return "TpsControlRule{" + "pointName='" + pointName + '\'' + ", pointRule=" + pointRule
            + "}'";
    }
}
