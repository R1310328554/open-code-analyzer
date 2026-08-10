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

package com.alibaba.nacos.config.server.model.gray;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.Map;
import java.util.Objects;

import static com.alibaba.nacos.api.common.Constants.VIPSERVER_TAG;

/**
 * 传统 Tag 灰度规则：按 VipserverTag 标签值精确匹配，用于旧版 Tag 配置下发。
 * 表达式即为目标 tag 值；优先级仅次于 Beta 规则。
 * Tag gray rule.
 *
 * @author shiyiyue
 */
public class TagGrayRule extends AbstractGrayRule {
    
    /** 期望匹配的 VipserverTag 值 */
    String tagValue;
    
    /** 连接标签中 VipserverTag 的键名 */
    public static final String VIP_SERVER_TAG_LABEL = VIPSERVER_TAG;
    
    /** Tag 灰度规则类型标识 */
    public static final String TYPE_TAG = "tag";
    
    /** Tag 规则版本号 */
    public static final String VERSION = "1.0.0";
    
    /** Tag 规则默认优先级（略低于 Beta） */
    public static final int PRIORITY = Integer.MAX_VALUE - 1;
    
    /** 无参构造，供 SPI 加载 */
    public TagGrayRule() {
        super();
    }
    
    /**
     * 构造 Tag 灰度规则。
     *
     * @param rawGrayRuleExp 目标 tag 值表达式
     * @param priority       优先级
     */
    public TagGrayRule(String rawGrayRuleExp, int priority) {
        super(rawGrayRuleExp, priority);
    }
    
    /** 将原始表达式直接作为 tagValue；空串则跳过 */
    @Override
    protected void parse(String rawGrayRule) throws NacosException {
        if (StringUtils.isBlank(rawGrayRule)) {
            return;
        }
        this.tagValue = rawGrayRule;
    }
    
    /** 标签存在且值与 tagValue 相等则命中 */
    @Override
    public boolean match(Map<String, String> labels) {
        return labels.containsKey(VIP_SERVER_TAG_LABEL)
            && tagValue.equals(labels.get(VIP_SERVER_TAG_LABEL));
    }
    
    /** 返回 {@link #TYPE_TAG} */
    @Override
    public String getType() {
        return TYPE_TAG;
    }
    
    /** 返回 {@link #VERSION} */
    @Override
    public String getVersion() {
        return VERSION;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagGrayRule that = (TagGrayRule) o;
        return tagValue.equals(that.tagValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(tagValue);
    }
}
