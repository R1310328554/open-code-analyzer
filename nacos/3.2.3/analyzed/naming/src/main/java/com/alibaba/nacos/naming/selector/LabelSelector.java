/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.naming.selector;

import com.alibaba.nacos.api.cmdb.pojo.Entity;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.selector.AbstractCmdbSelector;
import com.alibaba.nacos.api.selector.context.CmdbContext;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.selector.interpreter.ExpressionInterpreter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 CMDB 标签匹配的实例选择器。
 *
 * <p>按 {@link #labels} 中声明的标签键，比较消费者与提供者 CMDB 标签值；无匹配时回退返回全部提供者。</p>
 *
 * @author chenglu
 * @date 2021-07-16 16:26
 */
public class LabelSelector<T extends Instance> extends AbstractCmdbSelector<T> {
    
    /** 选择器类型标识：label。 */
    private static final String TYPE = "label";
    
    /** 参与匹配的标签键集合。 */
    private Set<String> labels;
    
    /** 返回参与匹配的标签键。 */
    public Set<String> getLabels() {
        return labels;
    }
    
    /** 设置参与匹配的标签键。 */
    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }
    
    @Override
    /** 按标签键值对等规则筛选实例列表。 */
    protected List<T> doSelect(CmdbContext<T> context) {
        if (CollectionUtils.isEmpty(labels)) {
            return context.getProviders()
                .stream()
                .map(CmdbContext.CmdbInstance::getInstance)
                .collect(Collectors.toList());
        }
        CmdbContext.CmdbInstance<T> consumer = context.getConsumer();
        Map<String, String> consumerLabels = Optional.ofNullable(consumer.getEntity())
            .map(Entity::getLabels)
            .orElse(Collections.emptyMap());
        
        // 过滤消费者与提供者标签值完全一致的实例
        List<T> result = context.getProviders()
            .stream()
            .filter(ci -> {
                Entity providerEntity = ci.getEntity();
                if (Objects.isNull(providerEntity)) {
                    return false;
                }
                Map<String, String> providerLabels = Optional.ofNullable(ci.getEntity().getLabels())
                    .orElse(Collections.emptyMap());
                return labels.stream()
                    .allMatch(label -> {
                        String consumerLabelValue = consumerLabels.get(label);
                        if (StringUtils.isBlank(consumerLabelValue)) {
                            return false;
                        }
                        return Objects.equals(consumerLabelValue, providerLabels.get(label));
                    });
            })
            .map(CmdbContext.CmdbInstance::getInstance)
            .collect(Collectors.toList());
        
        // 无匹配时回退返回全部提供者
        if (CollectionUtils.isEmpty(result)) {
            return context.getProviders()
                .stream()
                .map(CmdbContext.CmdbInstance::getInstance)
                .collect(Collectors.toList());
        }
        return result;
    }
    
    @Override
    /** 解析标签表达式为标签键集合。 */
    protected void doParse(String expression) throws NacosException {
        this.labels = ExpressionInterpreter.parseExpression(expression);
    }
    
    @Override
    /** 返回选择器类型 {@link #TYPE}。 */
    public String getType() {
        return TYPE;
    }
}
