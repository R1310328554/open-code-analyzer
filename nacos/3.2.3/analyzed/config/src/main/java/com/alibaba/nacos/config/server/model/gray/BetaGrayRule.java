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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Beta 灰度规则：按客户端 IP 白名单决定是否下发 Beta 配置。
 * 表达式为逗号分隔的 IP 列表，匹配标签 {@link #CLIENT_IP_LABEL}。
 * beta gray rule for beta ips.
 * @author shiyiyue1102
 */
public class BetaGrayRule extends AbstractGrayRule {
    
    /** 允许命中 Beta 灰度的客户端 IP 集合 */
    Set<String> betaIps;
    
    /** 连接标签中客户端 IP 的键名 */
    public static final String CLIENT_IP_LABEL = "ClientIp";
    
    /** Beta 灰度规则类型标识 */
    public static final String TYPE_BETA = "beta";
    
    /** Beta 规则版本号 */
    public static final String VERSION = "1.0.0";
    
    /** Beta 规则默认最高优先级 */
    public static final int PRIORITY = Integer.MAX_VALUE;
    
    /** 无参构造，供 SPI 加载 */
    public BetaGrayRule() {
        super();
    }
    
    /**
     * 根据 IP 列表字符串与优先级构造 Beta 规则。
     *
     * @param betaIps  逗号分隔 IP 表达式
     * @param priority 优先级
     */
    public BetaGrayRule(String betaIps, int priority) {
        super(betaIps, priority);
    }
    
    /**
     * 解析逗号分隔的 IP 列表为 {@link #betaIps} 集合。
     *
     * @param rawGrayRule 原始 IP 表达式
     * @throws NacosException 解析异常
     */
    @Override
    protected void parse(String rawGrayRule) throws NacosException {
        Set<String> betaIps = new HashSet<>();
        String[] ips = rawGrayRule.split(",");
        for (String ip : ips) {
            betaIps.add(ip);
        }
        this.betaIps = betaIps;
    }
    
    /** 客户端 IP 标签存在且落在白名单内则命中 */
    @Override
    public boolean match(Map<String, String> labels) {
        return labels.containsKey(CLIENT_IP_LABEL) && betaIps.contains(labels.get(CLIENT_IP_LABEL));
    }
    
    /** 返回 {@link #TYPE_BETA} */
    @Override
    public String getType() {
        return TYPE_BETA;
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
        BetaGrayRule that = (BetaGrayRule) o;
        return Objects.equals(betaIps, that.betaIps);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(betaIps);
    }
}
