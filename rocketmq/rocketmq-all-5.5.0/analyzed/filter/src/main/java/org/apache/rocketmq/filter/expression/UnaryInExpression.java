/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.filter.expression;

import org.apache.rocketmq.filter.constant.UnaryType;

import java.util.Collection;

/**
 * IN / NOT IN 布尔一元表达式：判断属性值是否属于给定集合。
 * <p>由 {@link UnaryExpression#createInExpression} 工厂方法创建匿名子类。</p>
 */
abstract public class UnaryInExpression extends UnaryExpression implements BooleanExpression {

    /** 是否为 NOT IN（取反语义）。 */
    private boolean not;

    /** 候选值集合，可为 List 或 HashSet。 */
    private Collection inList;

    /**
     * @param left 通常为 {@link PropertyExpression}
     * @param unaryType 固定为 {@link UnaryType#IN}
     * @param inList 比较用的值集合
     * @param not true 表示 NOT IN
     */
    public UnaryInExpression(Expression left, UnaryType unaryType,
        Collection inList, boolean not) {
        super(left, unaryType);
        this.setInList(inList);
        this.setNot(not);

    }

    /** 求值结果为 {@link Boolean#TRUE} 时视为匹配。 */
    public boolean matches(EvaluationContext context) throws Exception {
        Object object = evaluate(context);
        return object != null && object == Boolean.TRUE;
    }

    /** @return 是否为 NOT IN 模式 */
    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    /** @return IN 列表引用 */
    public Collection getInList() {
        return inList;
    }

    public void setInList(Collection inList) {
        this.inList = inList;
    }
}
