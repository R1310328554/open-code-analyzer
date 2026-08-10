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

package com.alibaba.nacos.client.auth.ram.injector;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.auth.ram.RamConstants;
import com.alibaba.nacos.client.auth.ram.RamContext;
import com.alibaba.nacos.client.auth.ram.identify.IdentifyConstants;
import com.alibaba.nacos.client.auth.ram.identify.StsConfig;
import com.alibaba.nacos.client.auth.ram.identify.StsCredential;
import com.alibaba.nacos.client.auth.ram.identify.StsCredentialHolder;
import com.alibaba.nacos.client.auth.ram.utils.CalculateV4SigningKeyUtil;
import com.alibaba.nacos.client.auth.ram.utils.SignUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Injector for naming module.
 * <p>服务发现（Naming）模块 RAM 资源注入器：对分组服务名构造 signData，使用 {@link SignUtil} HMAC-SHA1 签名并注入 signature/data/ak 及可选 STS Token。</p>
 *
 * @author xiweng.yy
 */
public class NamingResourceInjector extends AbstractResourceInjector {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NamingResourceInjector.class);
    
    /** 登录上下文中签名字段名 */
    private static final String SIGNATURE_FILED = "signature";
    
    /** 登录上下文中待签名原文字段名 */
    private static final String DATA_FILED = "data";
    
    /** 登录上下文中 AccessKey 字段名 */
    private static final String AK_FILED = "ak";
    
    /** 校验 RamContext 后计算签名并注入 naming 鉴权三元组；异常时记录日志不抛出 */
    @Override
    public void doInject(RequestResource resource, RamContext context,
        LoginIdentityContext result) {
        if (context.validate()) {
            try {
                String accessKey = context.getAccessKey();
                String secretKey = context.getSecretKey();
                // STS 临时凭证鉴权的优先级高于 AK/SK 鉴权
                if (StsConfig.getInstance().isStsOn()) {
                    StsCredential stsCredential =
                        StsCredentialHolder.getInstance().getStsCredential();
                    accessKey = stsCredential.getAccessKeyId();
                    secretKey = stsCredential.getAccessKeySecret();
                    result.setParameter(IdentifyConstants.SECURITY_TOKEN_HEADER,
                        stsCredential.getSecurityToken());
                }
                String signatureKey = secretKey;
                if (StringUtils.isNotEmpty(context.getRegionId())) {
                    signatureKey = CalculateV4SigningKeyUtil
                        .finalSigningKeyStringWithDefaultInfo(secretKey, context.getRegionId());
                    result.setParameter(RamConstants.SIGNATURE_VERSION, RamConstants.V4);
                }
                String signData = getSignData(getGroupedServiceName(resource));
                String signature = SignUtil.sign(signData, signatureKey);
                result.setParameter(SIGNATURE_FILED, signature);
                result.setParameter(DATA_FILED, signData);
                result.setParameter(AK_FILED, accessKey);
            } catch (Exception e) {
                LOGGER.error("inject ak/sk failed.", e);
            }
        }
    }
    
    /** 解析分组服务名：已含分隔符或 group 为空则直接用 resource，否则拼接 group@@service */
    private String getGroupedServiceName(RequestResource resource) {
        if (resource.getResource().contains(Constants.SERVICE_INFO_SPLITER) || StringUtils
            .isBlank(resource.getGroup())) {
            return resource.getResource();
        }
        return NamingUtils.getGroupedNameOptional(resource.getResource(), resource.getGroup());
    }
    
    /** 构造签名原文：时间戳@@服务名，无服务名时仅时间戳 */
    private String getSignData(String serviceName) {
        return StringUtils.isNotEmpty(serviceName)
            ? System.currentTimeMillis() + Constants.SERVICE_INFO_SPLITER
                + serviceName
            : String.valueOf(System.currentTimeMillis());
    }
}
