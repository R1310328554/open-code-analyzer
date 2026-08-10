/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.protocol.oid4vc.issuance.signing;

import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.CredentialBody;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;
import org.keycloak.provider.Provider;

/**
 * 可验证凭证签名器接口。
 * <p>各凭证格式（JWT VC、LDP VC、SD-JWT VC）通过 SPI 注册具体实现。</p>
 */
public interface CredentialSigner<T> extends Provider {

    /** 关闭签名器；默认无资源需释放。 */
    @Override
    default void close() {
    }

    /**
     * 对可验证凭证进行签名，返回可在凭证请求端点交付的已签名表示。
     *
     * @param credentialBody        待签名的部分构建凭证体
     * @param credentialBuildConfig 凭证构建附加配置
     * @return 已签名的凭证表示（类型因实现而异）
     * @throws CredentialSignerException 签名失败
     */
    T signCredential(CredentialBody credentialBody, CredentialBuildConfig credentialBuildConfig)
            throws CredentialSignerException;
}
