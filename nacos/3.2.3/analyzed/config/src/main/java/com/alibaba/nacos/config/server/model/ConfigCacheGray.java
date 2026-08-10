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

package com.alibaba.nacos.config.server.model;

import com.alibaba.nacos.config.server.model.gray.GrayRule;
import com.alibaba.nacos.config.server.model.gray.GrayRuleManager;

import java.io.Serializable;
import java.util.Map;

/**
 * 灰度配置缓存：在 {@link ConfigCache} 基础上附加灰度名称与 {@link com.alibaba.nacos.config.server.model.gray.GrayRule}，
 * 支持按客户端标签匹配灰度规则并参与推送路由。
 * extensible config cache.
 *
 * @author rong
 */
public class ConfigCacheGray extends ConfigCache implements Serializable {
    
    /** 灰度配置名称，唯一标识一条灰度发布 */
    private String grayName;
    
    /** 解析后的灰度匹配规则对象 */
    private GrayRule grayRule;
    
    /**
     * 清空灰度缓存，委托父类重置 MD5 等基础字段。
     * clear cache.
     */
    @Override
    public void clear() {
        super.clear();
    }
    
    public ConfigCacheGray() {
    }
    
    public ConfigCacheGray(String grayName) {
        this.grayName = grayName;
    }
    
    public GrayRule getGrayRule() {
        return grayRule;
    }
    
    public String getGrayName() {
        return grayName;
    }
    
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    /**
     * 获取数据库中存储的原始灰度规则表达式。
     * get raw gray rule from db.
     *
     * @return raw gray rule from db.
     * @date 2024/3/14
     */
    public String getRawGrayRule() {
        return grayRule.getRawGrayRuleExp();
    }
    
    /**
     * 从数据库原始规则字符串重新解析并设置灰度规则，无效时抛出异常。
     * reset gray rule.
     *
     * @param grayRule raw gray rule from db.
     * @throws RuntimeException if gray rule is invalid.
     * @date 2024/3/14
     */
    public void resetGrayRule(String grayRule) throws RuntimeException {
        this.grayRule = GrayRuleManager
            .constructGrayRule(GrayRuleManager.deserializeConfigGrayPersistInfo(grayRule));
        if (this.grayRule == null || !this.grayRule.isValid()) {
            throw new RuntimeException("raw gray rule is invalid");
        }
    }
    
    /**
     * 判断客户端连接标签是否匹配当前灰度规则。
     * judge whether match gray rule.
     *
     * @param tags conn tags.
     * @return true if match, false otherwise.
     * @date 2024/3/14
     */
    public boolean match(Map<String, String> tags) {
        return grayRule.match(tags);
    }
    
    public int getPriority() {
        return grayRule.getPriority();
    }
    
    /**
     * 判断灰度规则是否已加载且合法。
     * if gray rule is valid.
     *
     * @return true if valid, false otherwise.
     * @date 2024/3/14
     */
    public boolean isValid() {
        return grayRule != null && grayRule.isValid();
    }
}
