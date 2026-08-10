/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

import java.io.Serializable;
import java.util.Objects;

/**
 * 配置请求上下文：记录来源 IP、客户端类型、Beta IP、CAS MD5 及命名空间迁移等。
 * 持久化与鉴权链路据此做审计、Beta 校验与乐观锁比对。
 * ConfigRequestInfo.
 * @author dongyafei
 * @date 2022/8/11
 */
public class ConfigRequestInfo implements Serializable {
    
    private static final long serialVersionUID = 326726654448860273L;
    
    /** 请求来源 IP */
    private String srcIp;
    
    /** 来源类型（如 dubbo、http、grpc） */
    private String srcType;
    
    /** 请求 IP 关联的应用标识 */
    private String requestIpApp;
    
    /** Beta 发布允许的 IP 列表 */
    private String betaIps;
    
    /** 客户端 CAS 比对用 MD5（乐观锁） */
    private String casMd5;
    
    /** 是否命名空间已迁移 */
    private boolean namespaceTransferred;
    
    /** 配置已存在时是否允许更新，默认 true */
    private Boolean updateForExist = Boolean.TRUE;
    
    /**
     * 构造请求上下文。
     *
     * @param srcIp          来源 IP
     * @param srcType        来源类型
     * @param requestIpApp   请求 IP 应用
     * @param betaIps        Beta IP 列表
     * @param casMd5         CAS MD5
     */
    public ConfigRequestInfo(String srcIp, String srcType, String requestIpApp, String betaIps,
        String casMd5) {
        this.srcIp = srcIp;
        this.srcType = srcType;
        this.requestIpApp = requestIpApp;
        this.betaIps = betaIps;
        this.casMd5 = casMd5;
    }
    
    /** 无参构造 */
    public ConfigRequestInfo() {
    }
    
    /** 获取来源 IP */
    public String getSrcIp() {
        return srcIp;
    }
    
    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }
    
    public String getSrcType() {
        return srcType;
    }
    
    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }
    
    public String getRequestIpApp() {
        return requestIpApp;
    }
    
    public void setRequestIpApp(String requestIpApp) {
        this.requestIpApp = requestIpApp;
    }
    
    public String getBetaIps() {
        return betaIps;
    }
    
    public void setBetaIps(String betaIps) {
        this.betaIps = betaIps;
    }
    
    public String getCasMd5() {
        return casMd5;
    }
    
    public void setCasMd5(String casMd5) {
        this.casMd5 = casMd5;
    }
    
    public Boolean getUpdateForExist() {
        return updateForExist;
    }
    
    public void setUpdateForExist(Boolean updateForExist) {
        this.updateForExist = updateForExist;
    }
    
    /** 是否命名空间已迁移 */
    public boolean isNamespaceTransferred() {
        return namespaceTransferred;
    }
    
    /** 设置命名空间迁移标志 */
    public void setNamespaceTransferred(boolean namespaceTransferred) {
        this.namespaceTransferred = namespaceTransferred;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigRequestInfo that = (ConfigRequestInfo) o;
        return Objects.equals(srcIp, that.srcIp) && Objects.equals(requestIpApp, that.requestIpApp)
            && Objects.equals(
                betaIps, that.betaIps)
            && Objects.equals(casMd5, that.casMd5)
            && Objects.equals(updateForExist, that.updateForExist);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(srcIp, requestIpApp, betaIps, casMd5);
    }
    
    @Override
    public String toString() {
        return "ConfigRequestInfoVo{" + "srcIp='" + srcIp + '\'' + ", requestIpApp='" + requestIpApp
            + '\''
            + ", betaIps='" + betaIps + '\'' + ", casMd5='" + casMd5 + '\'' + ", updateForExist='"
            + updateForExist + '}';
    }
}
