/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.auth.ram.identify;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.client.remote.HttpClientManager;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sts credential holder.
 * <p>STS 临时凭证单例持有者：按 {@link StsConfig} 决定是否缓存、何时刷新，从静态 JSON 或元数据 HTTP 拉取并反序列化为 {@link StsCredential}。</p>
 *
 * @author xiweng.yy
 */
public class StsCredentialHolder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StsCredentialHolder.class);
    
    /** 全局单例实例 */
    private static final StsCredentialHolder INSTANCE = new StsCredentialHolder();
    
    /** 内存缓存的 STS 凭证（启用缓存时复用） */
    private StsCredential stsCredential;
    
    private StsCredentialHolder() {
    }
    
    public static StsCredentialHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get Sts Credential.
     * <p>获取有效 STS 凭证：缓存命中且未临近过期则直接返回，否则拉取最新响应并更新缓存。</p>
     *
     * @return StsCredential
     */
    public StsCredential getStsCredential() {
        boolean cacheSecurityCredentials = StsConfig.getInstance().isCacheSecurityCredentials();
        if (cacheSecurityCredentials && stsCredential != null) {
            long currentTime = System.currentTimeMillis();
            long expirationTime = stsCredential.getExpiration().getTime();
            int timeToRefreshInMillisecond =
                StsConfig.getInstance().getTimeToRefreshInMillisecond();
            if (expirationTime - currentTime > timeToRefreshInMillisecond) {
                return stsCredential;
            }
        }
        String stsResponse = getStsResponse();
        stsCredential = JacksonUtils.toObj(stsResponse, new TypeReference<StsCredential>() {
        });
        LOGGER.info("[getSTSCredential] code:{}, accessKeyId:{}, lastUpdated:{}, expiration:{}",
            stsCredential.getCode(), stsCredential.getAccessKeyId(),
            stsCredential.getLastUpdated(),
            stsCredential.getExpiration());
        return stsCredential;
    }
    
    /** 从静态配置或元数据 URL 获取 STS JSON 原始响应 */
    private static String getStsResponse() {
        String securityCredentials = StsConfig.getInstance().getSecurityCredentials();
        if (securityCredentials != null) {
            return securityCredentials;
        }
        String securityCredentialsUrl = StsConfig.getInstance().getSecurityCredentialsUrl();
        try {
            HttpRestResult<String> result = HttpClientManager.getInstance().getNacosRestTemplate()
                .get(securityCredentialsUrl, Header.EMPTY, Query.EMPTY, String.class);
            
            if (!result.ok()) {
                LOGGER.error(
                    "can not get security credentials, securityCredentialsUrl: {}, responseCode: {}, response: {}",
                    securityCredentialsUrl, result.getCode(), result.getMessage());
                throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                    "can not get security credentials, responseCode: " + result.getCode()
                        + ", response: " + result
                            .getMessage());
            }
            return result.getData();
        } catch (Exception e) {
            LOGGER.error("can not get security credentials", e);
            throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
        }
    }
}
