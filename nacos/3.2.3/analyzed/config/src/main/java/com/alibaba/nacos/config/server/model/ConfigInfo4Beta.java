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

package com.alibaba.nacos.config.server.model;

/**
 * Beta 灰度配置实体：继承 {@link ConfigInfo} 并附加 {@code betaIps} 字段，
 * 限定仅指定 IP 列表的客户端可拉取该 Beta 版本配置。
 * ConfigInfo4Beta.
 *
 * @author Nacos
 */
public class ConfigInfo4Beta extends ConfigInfo {
    
    private static final long serialVersionUID = 296578467953931353L;
    
    /** 允许拉取 Beta 配置的客户端 IP 列表，逗号分隔 */
    private String betaIps;
    
    public ConfigInfo4Beta() {
    }
    
    /** 构造带 Beta IP 白名单的配置实体 */
    public ConfigInfo4Beta(String dataId, String group, String appName, String content,
        String betaIps) {
        super(dataId, group, appName, content);
        this.betaIps = betaIps;
    }
    
    public String getBetaIps() {
        return betaIps;
    }
    
    public void setBetaIps(String betaIps) {
        this.betaIps = betaIps;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
