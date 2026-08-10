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

package com.alibaba.nacos.api.selector;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.io.Serializable;
import java.util.List;

/**
 * 仅携带类型标识的选择器抽象基类。
 *
 * <p>用于 API 层声明选择器类型而不绑定具体筛选逻辑；Jackson 多态序列化以 {@code type} 字段区分实现，默认回退为 {@link NoneSelector}。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
@JsonTypeInfo(use = Id.NAME, property = "type", defaultImpl = NoneSelector.class)
public abstract class AbstractSelector
    implements Serializable, Selector<List<Instance>, List<Instance>, String> {
    
    private static final long serialVersionUID = 4530233098102379229L;
    
    /** 选择器类型名，各子类应使用唯一取值。 */
    private final String type;
    
    /** 由子类传入 {@link SelectorType} 名称。 */
    protected AbstractSelector(String type) {
        this.type = type;
    }
    
    /** 返回选择器类型名。 */
    public String getType() {
        return type;
    }
    
    /** {@inheritDoc} 占位实现，返回 {@code null}。 */
    @Override
    public Selector<List<Instance>, List<Instance>, String> parse(String expression)
        throws NacosException {
        return null;
    }
    
    /** {@inheritDoc} 默认原样返回输入列表（不做过滤）。 */
    @Override
    public List<Instance> select(List<Instance> context) {
        return context;
    }
    
    /** {@inheritDoc} 返回 {@link SelectorType#none} 上下文类型。 */
    @Override
    public String getContextType() {
        return SelectorType.none.name();
    }
}
