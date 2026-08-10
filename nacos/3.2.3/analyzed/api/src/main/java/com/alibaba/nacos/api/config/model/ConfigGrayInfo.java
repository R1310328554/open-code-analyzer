/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.model;

/**
 * Nacos 配置灰度信息，在 {@link ConfigDetailInfo} 基础上附加灰度名称与规则。
 *
 * <p>用于灰度发布场景下区分不同灰度版本。</p>
 *
 * @author xiweng.yy
 */
public class ConfigGrayInfo extends ConfigDetailInfo {
    
    private static final long serialVersionUID = 4462719176825261439L;
    
    /** 灰度版本名称。 */
    private String grayName;
    
    /** 灰度匹配规则表达式。 */
    private String grayRule;
    
    /** 获取灰度名称。 */
    public String getGrayName() {
        return grayName;
    }
    
    /** 设置灰度名称。 */
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    /** 获取灰度规则。 */
    public String getGrayRule() {
        return grayRule;
    }
    
    /** 设置灰度规则。 */
    public void setGrayRule(String grayRule) {
        this.grayRule = grayRule;
    }
}
