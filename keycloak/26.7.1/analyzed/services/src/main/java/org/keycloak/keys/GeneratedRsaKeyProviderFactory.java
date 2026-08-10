/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.keys;

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * 自动生成 RSA 签名密钥的 {@link KeyProviderFactory}，ID 为 {@code rsa-generated}。
 * <p>生成 RSA 密钥对及自签名证书，支持 RS256/PS256 等签名算法；向后兼容未设置 KEY_USE 的旧配置。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class GeneratedRsaKeyProviderFactory extends AbstractGeneratedRsaKeyProviderFactory {

    private static final Logger logger = Logger.getLogger(GeneratedRsaKeyProviderFactory.class);

    /** 工厂标识 {@code rsa-generated}。 */
    public static final String ID = "rsa-generated";

    private static final String HELP_TEXT = "Generates RSA signature keys and creates a self-signed certificate";

    /** 创建匿名 {@link AbstractRsaKeyProvider} 子类实例；未配置 KEY_USE 时默认为 SIG。 */
    @Override
    public KeyProvider create(KeycloakSession session, ComponentModel model) {
        if (model.getConfig().get(Attributes.KEY_USE) == null) {
            // 向后兼容：未设置 KEY_USE 时默认签名用途（旧版 rsa-generated 曾允许 enc）
            model.put(Attributes.KEY_USE, KeyUse.SIG.name());
        }
        return new AbstractRsaKeyProvider(session.getContext().getRealm(), model){};
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return generatedRsaKeyConfigurationBuilder()
                .property(Attributes.RS_ALGORITHM_PROPERTY)
                .build();
    }

    @Override
    public String getId() {
        return ID;
    }

    /** 仅接受签名用途 {@link KeyUse#SIG}。 */
    @Override
    protected boolean isValidKeyUse(KeyUse keyUse) {
        return keyUse.equals(KeyUse.SIG);
    }

    /** 校验 RSA 签名算法（RS256/PS256/RS384 等）。 */
    @Override
    protected boolean isSupportedRsaAlgorithm(String algorithm) {
        return algorithm.equals(Algorithm.RS256) 
                || algorithm.equals(Algorithm.PS256) 
                || algorithm.equals(Algorithm.RS384)
                || algorithm.equals(Algorithm.PS384) 
                || algorithm.equals(Algorithm.RS512) 
                || algorithm.equals(Algorithm.PS512);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

}
