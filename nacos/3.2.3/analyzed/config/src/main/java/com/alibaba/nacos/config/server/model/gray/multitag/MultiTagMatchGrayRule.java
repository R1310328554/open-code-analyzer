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

package com.alibaba.nacos.config.server.model.gray.multitag;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.model.gray.AbstractTagMatchGrayRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.common.Constants.TAG_V2;

/**
 * TagV2 多标签组合灰度规则：支持 key=value 与 ||、&& 逻辑组合，版本 1.1.0。
 * 表达式形如 a=v1,v2&&b=v3||c=v4；匹配时对标签 Map 做 OR 子句内 AND 合取判定。
 * tag v2 gray rule.
 *
 * @author rong
 */
public class MultiTagMatchGrayRule extends AbstractTagMatchGrayRule {
    
    /** 解析后的规则项列表（含连接符 OR/AND 语义） */
    private List<TagV2GrayRuleItem> ruleItems;
    
    /** TagV2 规则类型常量 */
    public static final String TYPE_TAGV2 = TAG_V2;
    
    /** 多标签规则版本 1.1.0 */
    public static final String VERSION_1_1_0 = "1.1.0";
    
    private static final String ELEM_PATTERN =
        "\\s*" + KEY_PATTERN + "\\s*" + EQUAL_PATTERN + "\\s*" + VALUE_PATTERN + "\\s*";
    
    private static final String JOINT_PATTERN = "(\\|\\||&&)";
    
    private static final String EXPRESSION_PATTERN =
        "^" + ELEM_PATTERN + "(" + JOINT_PATTERN + ELEM_PATTERN + ")*$";
    
    /** 无参构造 */
    public MultiTagMatchGrayRule() {
        super();
    }
    
    /**
     * 构造多标签匹配规则。
     *
     * @param rawGrayRuleExp 含 ||、&& 的复合表达式
     * @param priority       优先级
     */
    public MultiTagMatchGrayRule(String rawGrayRuleExp, int priority) {
        super(rawGrayRuleExp, priority);
    }
    
    @Override
    protected void parse(String rawRule) throws NacosException {
        this.isPatternMatch(rawRule, EXPRESSION_PATTERN);
        String[] splitSubExpressionByOrArray = rawRule.trim()
            .split(MultiTagMatchGrayRule.TagV2GrayRuleJoint.OR_REGEXP.getExpression());
        for (String s : splitSubExpressionByOrArray) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            // 每个 OR 子句内由一条或多条 && 连接的键值项组成
            String[] splitSubExpressionByAndArray = s.trim()
                .split(MultiTagMatchGrayRule.TagV2GrayRuleJoint.AND_REGEXP.getExpression());
            for (int andIndex = 0; andIndex < splitSubExpressionByAndArray.length; andIndex++) {
                if (StringUtils.isBlank(splitSubExpressionByAndArray[andIndex])) {
                    continue;
                }
                String[] keyValueArray =
                    splitSubExpressionByAndArray[andIndex].trim().split(EQUAL_PATTERN);
                if (keyValueArray.length != 2) {
                    throw new NacosException(NacosException.INVALID_PARAM, String.format(
                        "tagv2 gray rule parse failed: key and value's[%s] doesn't match pattern[%s].",
                        splitSubExpressionByAndArray[andIndex],
                        KEY_PATTERN + EQUAL_PATTERN + VALUE_PATTERN));
                }
                isPatternMatch(keyValueArray[0].trim(), KEY_PATTERN);
                isPatternMatch(keyValueArray[1].trim(), VALUE_PATTERN);
                Set<String> values =
                    Arrays.stream(keyValueArray[1].split(VALUE_SPLITER_PATTERN)).map(String::trim)
                        .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                MultiTagMatchGrayRule.TagV2GrayRuleItem tagV2GrayRuleItem =
                    new MultiTagMatchGrayRule.TagV2GrayRuleItem(
                        keyValueArray[0].trim(), values);
                if (andIndex == 0) {
                    tagV2GrayRuleItem.setJoint(MultiTagMatchGrayRule.TagV2GrayRuleJoint.OR);
                }
                if (this.ruleItems == null) {
                    this.ruleItems = new ArrayList<>();
                }
                ruleItems.add(tagV2GrayRuleItem);
            }
        }
    }
    
    /**
     * 按 OR 子句划分规则项，子句内 AND 合取匹配 labelsMap。
     *
     * @param labelsMap 客户端标签 Map
     * @return 任一 OR 子句全部 AND 项命中则 true
     * @date 2024/2/6
     */
    public boolean match(Map<String, String> labelsMap) {
        if (ruleItems.isEmpty() || labelsMap == null || labelsMap.isEmpty()) {
            return false;
        }
        ArrayList<TagV2GrayRuleItem> localRuleItems =
            ruleItems.stream().map(TagV2GrayRuleItem::clone)
                .collect(Collectors.toCollection(ArrayList::new));
        int result = 0;
        int tempResult = 0;
        boolean subRuleMatchFlag = true;
        HashSet<String> tempKeyExistSet = new HashSet<>();
        
        for (int index = 0; index < localRuleItems.size(); index++) {
            if (result > 0) {
                return true;
            }
            TagV2GrayRuleItem curTagV2GrayRuleItem = localRuleItems.get(index);
            
            if (curTagV2GrayRuleItem.getJoint() == TagV2GrayRuleJoint.AND) {
                // AND：当前项属于同一 OR 子句，需与前项同时满足
                
                // 子句内已有项未命中则跳过该子句后续项
                if (!subRuleMatchFlag) {
                    continue;
                }
                
                // 同一 OR 子句内重复 key 视为语法错误，子句不匹配
                // another item with the same key appears which will be considered as a syntax error.
                if (tempKeyExistSet.contains(curTagV2GrayRuleItem.getKey())) {
                    subRuleMatchFlag = false;
                    continue;
                } else {
                    tempKeyExistSet.add(curTagV2GrayRuleItem.getKey());
                }
                
                // 校验当前键值项是否命中 labelsMap
                if (!curTagV2GrayRuleItem.match(labelsMap.get(curTagV2GrayRuleItem.getKey()))) {
                    subRuleMatchFlag = false;
                }
                tempResult++;
            } else if (curTagV2GrayRuleItem.getJoint() == TagV2GrayRuleJoint.OR) {
                // OR：结束当前子句，开启下一子句（首项 joint 改为 AND）
                // and this subRule contains items between [subRuleBeginIndex, index).
                
                // 仅当上一子句匹配成功时更新已匹配项计数
                if (subRuleMatchFlag) {
                    result = Math.max(result, tempResult);
                }
                curTagV2GrayRuleItem.setJoint(TagV2GrayRuleJoint.AND);
                subRuleMatchFlag = true;
                tempKeyExistSet.clear();
                index--;
            }
        }
        if (subRuleMatchFlag) {
            return Math.max(result, tempResult) > 0;
        }
        return result > 0;
    }
    
    /**
     * 校验规则项非空、各项 key 合法且同一 OR 子句内无重复 key。
     *
     * @return 语义合法返回 true
     * @date 2024/2/7
     */
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }
        if (ruleItems.isEmpty()) {
            return true;
        }
        HashSet<String> tempKeyExistSet = new HashSet<>();
        
        ArrayList<TagV2GrayRuleItem> localRuleItems =
            ruleItems.stream().map(TagV2GrayRuleItem::clone)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int index = 0; index < localRuleItems.size(); index++) {
            TagV2GrayRuleItem curTagV2GrayRuleItem = localRuleItems.get(index);
            
            if (!curTagV2GrayRuleItem.isValid()) {
                return false;
            }
            
            if (curTagV2GrayRuleItem.getJoint() == TagV2GrayRuleJoint.AND) {
                //if AND, will consider the current ruleItem belong to this subRule.
                
                //if the key has already existed in this subRule,
                // another item with the same key appears which will be considered as a syntax error.
                if (tempKeyExistSet.contains(curTagV2GrayRuleItem.getKey())) {
                    return false;
                } else {
                    tempKeyExistSet.add(curTagV2GrayRuleItem.getKey());
                }
            } else if (curTagV2GrayRuleItem.getJoint() == TagV2GrayRuleJoint.OR) {
                //if OR, will consider the current ruleItem belong to the next subRule,
                // and this subRule contains items between [subRuleBeginIndex, index).
                
                //only when subRuleMatchFlag is true, update result.
                curTagV2GrayRuleItem.setJoint(TagV2GrayRuleJoint.AND);
                tempKeyExistSet.clear();
                index--;
            }
        }
        return true;
    }
    
    /** 返回 {@link #TYPE_TAGV2} */
    @Override
    public String getType() {
        return TYPE_TAGV2;
    }
    
    /** 返回 {@link #VERSION_1_1_0} */
    @Override
    public String getVersion() {
        return VERSION_1_1_0;
    }
    
    /**
     * TagV2 单条规则项：键、运算符、允许值集合及与子句内前项的连接关系。
     *
     * @author rong
     */
    public static class TagV2GrayRuleItem implements Cloneable {
        
        /** 标签键名 */
        public String key;
        
        /** 匹配运算符，默认 IN（值在集合内） */
        public TagV2GrayRuleOperator operator = TagV2GrayRuleOperator.IN;
        
        /** 参与 IN/NOT_IN 判定的值集合 */
        public final Set<String> values = new HashSet<>();
        
        /** 与前一项的逻辑连接（OR 子句边界或 AND 合取） */
        public TagV2GrayRuleJoint joint = TagV2GrayRuleJoint.AND;
        
        /** 仅指定键的构造 */
        public TagV2GrayRuleItem(String key) {
            this.key = key;
        }
        
        /**
         * 指定键与允许值集合。
         *
         * @param key    标签键
         * @param values 允许值集合
         */
        public TagV2GrayRuleItem(String key, Set<String> values) {
            this.key = key;
            this.values.addAll(values);
        }
        
        /**
         * 按 operator 判断单个标签值是否满足本项。
         *
         * @param value 标签实际值
         * @return 匹配返回 true
         * @date 2024/2/8
         */
        public boolean match(String value) {
            switch (operator) {
                case IN:
                    if (null == value) {
                        return false;
                    } else {
                        return values.contains(value);
                    }
                case NOT_IN:
                    if (null == value) {
                        return false;
                    } else {
                        return !values.contains(value);
                    }
                case EXIST:
                    return value != null;
                case NOT_EXIST:
                    return value == null;
                default:
            }
            return false;
        }
        
        /**
         * 规则项是否有效（当前实现要求 key 非空）。
         *
         * @return 有效返回 true
         * @date 2024/2/8
         */
        public boolean isValid() {
            return !StringUtils.isBlank(key);
        }
        
        /** 获取规则项建造者 */
        public static TagV2GrayRuleItemBuilder builder() {
            return new TagV2GrayRuleItemBuilder();
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public TagV2GrayRuleOperator getOperator() {
            return operator;
        }
        
        public void setOperator(TagV2GrayRuleOperator operator) {
            this.operator = operator;
        }
        
        public Set<String> getValues() {
            return values;
        }
        
        public TagV2GrayRuleJoint getJoint() {
            return joint;
        }
        
        public void setJoint(TagV2GrayRuleJoint joint) {
            this.joint = joint;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TagV2GrayRuleItem)) {
                return false;
            }
            TagV2GrayRuleItem that = (TagV2GrayRuleItem) o;
            return Objects.equals(key, that.key) && operator == that.operator
                && values.size() == that.values.size()
                && values.containsAll(that.values) && joint == that.joint;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(key, operator, values, joint);
        }
        
        @Override
        public String toString() {
            return "{" + "key='" + key + '\'' + ", operator=" + operator + ", values=" + values
                + ", joint=" + joint
                + '}';
        }
        
        /** 深拷贝规则项（含 values 集合） */
        @Override
        public TagV2GrayRuleItem clone() {
            try {
                TagV2GrayRuleItem clone = (TagV2GrayRuleItem) super.clone();
                clone.setKey(key);
                clone.setJoint(joint);
                clone.setOperator(operator);
                clone.getValues().addAll(values);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
        
        /** TagV2 规则项流式构造器 */
        public static final class TagV2GrayRuleItemBuilder {
            
            private final TagV2GrayRuleItem item;
            
            private TagV2GrayRuleItemBuilder() {
                item = new TagV2GrayRuleItem(null);
            }
            
            /** 设置标签键并返回 builder */
            public TagV2GrayRuleItemBuilder key(String key) {
                item.key = key;
                return this;
            }
            
            /** 构建规则项实例 */
            public TagV2GrayRuleItem build() {
                return item;
            }
        }
    }
    
    /**
     * TagV2 逻辑连接符：表达式中的 ||、&& 及对应 split 正则。
     *
     * @author rong
     */
    public enum TagV2GrayRuleJoint {
        
        /** 逻辑与（&&） */
        AND("&&", "AND"),
        
        /** 逻辑或（||），亦标记 OR 子句起始 */
        OR("||", "OR"),
        
        /** 用于 split 的 AND 正则 */
        AND_REGEXP("&&", "AND_REGEXP"),
        
        /** 用于 split 的 OR 正则（转义 ||） */
        OR_REGEXP("\\|\\|", "OR_REGEXP");
        
        /** 连接符或正则字面量 */
        public final String expression;
        
        /** 枚举名称标识 */
        public final String name;
        
        TagV2GrayRuleJoint(String expression, String name) {
            this.expression = expression;
            this.name = name;
        }
        
        public String getExpression() {
            return expression;
        }
        
        public String getName() {
            return name;
        }
    }
    
    /**
     * TagV2 值匹配运算符：IN、NOT_IN、EXIST、NOT_EXIST。
     *
     * @author rong
     */
    public enum TagV2GrayRuleOperator {
        
        /** 值在允许集合内 */
        IN("in", "IN"),
        
        /** 值不在允许集合内 */
        NOT_IN("not in", "NOT_IN"),
        
        /** 标签值存在（非 null） */
        EXIST("exist", "EXIST"),
        
        /** 标签值不存在（为 null） */
        NOT_EXIST("not exist", "NOT_EXIST");
        
        public final String expression;
        
        public final String name;
        
        TagV2GrayRuleOperator(String expression, String name) {
            this.expression = expression;
            this.name = name;
        }
    }
}
