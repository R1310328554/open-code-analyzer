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

package com.alibaba.nacos.naming.selector.interpreter;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 标签选择器表达式解析器。
 *
 * <p>当前仅支持有限语法：{@code CONSUMER.label.X = PROVIDER.label.X & ...}，后续可扩展为标准 LL 解析器。</p>
 *
 * @author nokrange
 */
public class ExpressionInterpreter {
    
    /** 内层连接符集合（当前仅 {@code =}）。 */
    private static final Set<String> SUPPORTED_INNER_CONNCETORS = new HashSet<>();
    
    /** 外层连接符集合（当前仅 {@code &}）。 */
    private static final Set<String> SUPPORTED_OUTER_CONNCETORS = new HashSet<>();
    
    /** 消费者标签前缀。 */
    private static final String CONSUMER_PREFIX = "CONSUMER.label.";
    
    /** 提供者标签前缀。 */
    private static final String PROVIDER_PREFIX = "PROVIDER.label.";
    
    private static final char CEQUAL = '=';
    
    private static final char CAND = '&';
    
    static {
        SUPPORTED_INNER_CONNCETORS.add(String.valueOf(CEQUAL));
        SUPPORTED_OUTER_CONNCETORS.add(String.valueOf(CAND));
    }
    
    /**
     * 解析标签匹配表达式，提取参与匹配的标签键集合。
     *
     * <p>支持形如 {@code CONSUMER.label.A = PROVIDER.label.A & ...} 的表达式。
     *
     * @param expression 标签表达式字符串
     * @return 标签键集合
     */
    /** 词法切分并校验语法，返回标签键集合。 */
    public static Set<String> parseExpression(String expression) throws NacosException {
        
        if (StringUtils.isBlank(expression)) {
            return new HashSet<>();
        }
        
        expression = StringUtils.deleteWhitespace(expression);
        
        List<String> elements = getTerms(expression);
        Set<String> gotLabels = new HashSet<>();
        int index = 0;
        
        index = checkInnerSyntax(elements, index);
        
        if (index == -1) {
            throw new NacosException(NacosException.INVALID_PARAM, "parse expression failed!");
        }
        
        gotLabels.add(elements.get(index++).split(PROVIDER_PREFIX)[1]);
        
        while (index < elements.size()) {
            
            index = checkOuterSyntax(elements, index);
            
            if (index >= elements.size()) {
                return gotLabels;
            }
            
            if (index == -1) {
                throw new NacosException(NacosException.INVALID_PARAM, "parse expression failed!");
            }
            
            gotLabels.add(elements.get(index++).split(PROVIDER_PREFIX)[1]);
        }
        
        return gotLabels;
    }
    
    /** 按 {@code =} 与 {@code &} 切分表达式为词项列表。 */
    public static List<String> getTerms(String expression) {
        
        List<String> terms = new ArrayList<>();
        
        Set<Character> characters = new HashSet<>();
        characters.add(CEQUAL);
        characters.add(CAND);
        
        char[] chars = expression.toCharArray();
        
        int lastIndex = 0;
        for (int index = 0; index < chars.length; index++) {
            char ch = chars[index];
            if (characters.contains(ch)) {
                terms.add(expression.substring(lastIndex, index));
                terms.add(expression.substring(index, index + 1));
                index++;
                lastIndex = index;
            }
        }
        
        terms.add(expression.substring(lastIndex, chars.length));
        
        return terms;
    }
    
    /** 跳过空白词项。 */
    private static int skipEmpty(List<String> elements, int start) {
        while (start < elements.size() && StringUtils.isBlank(elements.get(start))) {
            start++;
        }
        return start;
    }
    
    /** 校验外层 {@code &} 连接符语法。 */
    private static int checkOuterSyntax(List<String> elements, int start) {
        
        int index = start;
        
        index = skipEmpty(elements, index);
        if (index >= elements.size()) {
            return index;
        }
        
        if (!SUPPORTED_OUTER_CONNCETORS.contains(elements.get(index++))) {
            return -1;
        }
        
        return checkInnerSyntax(elements, index);
    }
    
    /** 校验单条 CONSUMER/PROVIDER 标签等式语法。 */
    private static int checkInnerSyntax(List<String> elements, int start) {
        
        int index = start;
        
        index = skipEmpty(elements, index);
        if (index >= elements.size()) {
            return -1;
        }
        
        if (!elements.get(index).startsWith(CONSUMER_PREFIX)) {
            return -1;
        }
        
        final String labelConsumer = elements.get(index++).split(CONSUMER_PREFIX)[1];
        
        index = skipEmpty(elements, index);
        if (index >= elements.size()) {
            return -1;
        }
        
        if (!SUPPORTED_INNER_CONNCETORS.contains(elements.get(index++))) {
            return -1;
        }
        
        index = skipEmpty(elements, index);
        if (index >= elements.size()) {
            return -1;
        }
        
        if (!elements.get(index).startsWith(PROVIDER_PREFIX)) {
            return -1;
        }
        
        final String labelProvider = elements.get(index).split(PROVIDER_PREFIX)[1];
        
        if (!labelConsumer.equals(labelProvider)) {
            return -1;
        }
        
        return index;
    }
}
