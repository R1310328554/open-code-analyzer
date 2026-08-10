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

package com.alibaba.nacos.config.server.model.gray;

import com.alibaba.nacos.api.exception.NacosException;

/**
 * 基于标签键值匹配的灰度规则抽象基类：定义表达式正则常量与格式校验。
 * 子类实现单标签或多标签组合匹配；相等性比较包含表达式、优先级、type 与 version。
 * description.
 *
 * @author rong
 * @date 2024-03-13 14:31
 */
public abstract class AbstractTagMatchGrayRule extends AbstractGrayRule {
    
    /** 键值分隔符模式（等号） */
    protected static final String EQUAL_PATTERN = "=";
    
    /** 标签键合法字符模式 */
    protected static final String KEY_PATTERN = "[a-zA-Z0-9-_:\.]+";
    
    /** 多值分隔符模式（逗号） */
    protected static final String VALUE_SPLITER_PATTERN = ",";
    
    /** 标签值列表的正则模式（逗号分隔多个值） */
    protected static final String VALUE_PATTERN =
        KEY_PATTERN + "(\\s*" + VALUE_SPLITER_PATTERN + "\\s*" + KEY_PATTERN + ")*";
    
    /** 无参构造 */
    public AbstractTagMatchGrayRule() {
        super();
    }
    
    /**
     * 构造标签匹配灰度规则。
     *
     * @param rawGrayRuleExp 原始表达式
     * @param priority       优先级
     */
    public AbstractTagMatchGrayRule(String rawGrayRuleExp, int priority) {
        super(rawGrayRuleExp, priority);
    }
    
    /**
     * 校验字符串是否符合指定正则，不匹配则抛出参数异常。
     *
     * @param rawString 待校验字符串
     * @param pattern   正则模式
     * @throws NacosException 格式不合法
     */
    protected void isPatternMatch(String rawString, String pattern) throws NacosException {
        if (!rawString.matches(pattern)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                String.format(
                    "tagv2 gray rule parse failed: " + "raw string [%s] doesn't match pattern[%s].",
                    rawString, pattern));
        }
    }
    
    /**
     * 判断与另一标签灰度规则是否在表达式、优先级、type 与 version 上完全一致。
     *
     * @param obj 待比较对象
     * @return 等价返回 true
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof AbstractTagMatchGrayRule) {
            AbstractTagMatchGrayRule other = (AbstractTagMatchGrayRule) obj;
            return this.rawGrayRuleExp.equals(other.rawGrayRuleExp)
                && this.priority == other.priority && this.getType()
                    .equals(other.getType())
                && this.getVersion().equals(other.getVersion());
        }
        return false;
    }
}
