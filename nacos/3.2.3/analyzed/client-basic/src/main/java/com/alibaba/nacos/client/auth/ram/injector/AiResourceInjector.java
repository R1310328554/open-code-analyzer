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

package com.alibaba.nacos.client.auth.ram.injector;

import com.alibaba.nacos.client.auth.ram.RamConstants;
import com.alibaba.nacos.client.auth.ram.RamContext;
import com.alibaba.nacos.client.auth.ram.identify.IdentifyConstants;
import com.alibaba.nacos.client.auth.ram.identify.StsConfig;
import com.alibaba.nacos.client.auth.ram.identify.StsCredential;
import com.alibaba.nacos.client.auth.ram.identify.StsCredentialHolder;
import com.alibaba.nacos.client.auth.ram.utils.CalculateV4SigningKeyUtil;
import com.alibaba.nacos.client.auth.ram.utils.SpasAdapter;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;

import java.util.Map;

/**
 * AI module aliyun ram reousce injector.
 * <p>AI 模块 RAM 资源注入器：校验 {@link RamContext} 后注入 AccessKey、STS Token、V4 签名版本及 {@link SpasAdapter} 生成的 Spas 签名头；资源串为 namespace+group。</p>
 *
 * @author xiweng.yy
 */
public class AiResourceInjector extends AbstractResourceInjector {
    
    /** 请求头：Spas AccessKey 字段名 */
    private static final String ACCESS_KEY_HEADER = "Spas-AccessKey";
    
    /** 注入 AI 模块 RAM 鉴权参数：STS 优先、可选 V4 派生密钥、Spas 签名头 */
    @Override
    public void doInject(RequestResource resource, RamContext context,
        LoginIdentityContext result) {
        if (!context.validate()) {
            return;
        }
        String accessKey = context.getAccessKey();
        String secretKey = context.getSecretKey();
        if (StsConfig.getInstance().isStsOn()) {
            StsCredential stsCredential = StsCredentialHolder.getInstance().getStsCredential();
            accessKey = stsCredential.getAccessKeyId();
            secretKey = stsCredential.getAccessKeySecret();
            result.setParameter(IdentifyConstants.SECURITY_TOKEN_HEADER,
                stsCredential.getSecurityToken());
        }
        result.setParameter(ACCESS_KEY_HEADER, accessKey);
        String signatureKey = secretKey;
        if (StringUtils.isNotEmpty(context.getRegionId())) {
            signatureKey = CalculateV4SigningKeyUtil.finalSigningKeyStringWithDefaultInfo(secretKey,
                context.getRegionId());
            result.setParameter(RamConstants.SIGNATURE_VERSION, RamConstants.V4);
        }
        Map<String, String> signHeaders =
            SpasAdapter.getSignHeaders(buildResourceString(resource), signatureKey);
        result.setParameters(signHeaders);
    }
    
    /** 构造 AI 模块 Spas 资源串：tenant+"+"+group */
    private String buildResourceString(RequestResource resource) {
        return resource.getNamespace() + "+" + resource.getGroup();
    }
}
