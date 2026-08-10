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

package com.alibaba.nacos.api.selector;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.selector.context.CmdbContext;

import java.util.List;

import static com.alibaba.nacos.api.common.Constants.Naming.CMDB_CONTEXT_TYPE;

/**
 * 基于 CMDB 上下文的服务实例选择器抽象基类。
 *
 * <p>实现 {@link Selector} 的通用流程：{@link #parse(String)} 解析表达式，{@link #select(CmdbContext)} 在 {@link CmdbContext} 上执行筛选；子类实现 {@link #doParse} 与 {@link #doSelect}。返回结果为 {@link Instance} 子类列表。</p>
 *
 * @author chenglu
 * @date 2021-07-09 21:29
 */
public abstract class AbstractCmdbSelector<T extends Instance>
    implements Selector<List<T>, CmdbContext<T>, String> {
    
    private static final long serialVersionUID = 56587385358330901L;
    
    /** 标签/条件表达式字符串。 */
    protected String expression;
    
    /** 返回当前解析后的表达式。 */
    public String getExpression() {
        return expression;
    }
    
    /** 设置表达式（通常由 {@link #parse} 调用）。 */
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    /** {@inheritDoc} 保存表达式并委托 {@link #doParse} 完成解析。 */
    @Override
    public Selector<List<T>, CmdbContext<T>, String> parse(String expression)
        throws NacosException {
        this.expression = expression;
        doParse(expression);
        return this;
    }
    
    /**
     * 子类实现的表达式解析逻辑。
     *
     * @param expression 待解析表达式
     * @throws NacosException 解析失败时抛出
     */
    protected abstract void doParse(String expression) throws NacosException;
    
    /** {@inheritDoc} 委托 {@link #doSelect} 执行筛选。 */
    @Override
    public List<T> select(CmdbContext<T> context) {
        return doSelect(context);
    }
    
    /**
     * 子类实现的实例筛选逻辑。
     *
     * @param context CMDB 选择上下文 {@link CmdbContext}
     * @return 筛选后的实例列表
     */
    protected abstract List<T> doSelect(CmdbContext<T> context);
    
    /** {@inheritDoc} 固定返回 CMDB 上下文类型常量。 */
    @Override
    public String getContextType() {
        return CMDB_CONTEXT_TYPE;
    }
}
