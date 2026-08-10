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

package org.keycloak.adapters.saml;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Set;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.SignatureAlgorithm;

import org.apache.http.client.HttpClient;

/**
 * SAML 部署运行时配置接口，聚合 IdP/SP 实体、绑定、签名、密钥与角色映射等设置。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SamlDeployment {

    /** SAML HTTP 绑定类型：POST 表单或 Redirect 重定向。 */
    enum Binding {
        POST,
        REDIRECT;

        /** 解析配置字符串为绑定枚举，{@code null} 时默认 POST。 */
        public static Binding parseBinding(String val) {
            if (val == null) return POST;
            return Binding.valueOf(val);
        }
    }

    /** 身份提供方（IdP）运行时配置。 */
    public interface IDP {
        /**
         * 返回 IdP 实体标识符。
         * @return 实体 ID
         */
        String getEntityID();

        /**
         * 返回 IdP 单点登录（SSO）服务配置。
         * @return SSO 服务配置
         */
        SingleSignOnService getSingleSignOnService();

        /**
         * 返回 IdP 单点登出（SLO）服务配置。
         * @return SLO 服务配置
         */
        SingleLogoutService getSingleLogoutService();

        /**
         * 返回用于校验 IdP 签名的 {@link KeyLocator}。
         * @return 签名验证密钥定位器
         */
        KeyLocator getSignatureValidationKeyLocator();

        /**
         * 请求 IdP SAML 元数据描述符的最小间隔（秒），防止密钥轮询过于频繁。
         *
         * @return 最小请求间隔（秒）
         */
        int getMinTimeBetweenDescriptorRequests();

        /**
         * 与 IdP 通信使用的 {@link HttpClient} 实例。
         * @return HTTP 客户端
         */
        HttpClient getClient();

        /**
         * IdP 与 SP 之间允许的时钟偏差（毫秒）。
         * @return 时钟偏移毫秒数
         */
        int getAllowedClockSkew();

        /** IdP 单点登录服务配置。 */
        public interface SingleSignOnService {
            /**
             * SP 发往 IdP 的请求是否需 SP 密钥签名。
             * @return 是否签名请求
             */
            boolean signRequest();
            /**
             * 是否校验 IdP 响应消息的完整签名。
             * @return 是否校验响应签名
             */
            boolean validateResponseSignature();
            /**
             * 是否校验 IdP 响应中各断言的签名。
             * @return 是否校验断言签名
             */
            boolean validateAssertionSignature();
            /** @return 出站 AuthnRequest 使用的绑定类型 */
            Binding getRequestBinding();
            /**
             * 客户端期望的认证响应绑定类型；默认不指定，由 IdP 决定。
             * @return 响应绑定类型
             */
            Binding getResponseBinding();
            /**
             * SP 发送登录请求的 IdP SSO URL。
             * @return SSO 端点 URL
             */
            String getRequestBindingUrl();
            /**
             * 指定 IdP 应投递断言的消费者服务 URL；通常与 ProtocolBinding 配合使用。
             * @return 断言消费者服务 URI
             */
            URI getAssertionConsumerServiceUrl();
        }

        /** IdP 单点登出服务配置。 */
        public interface SingleLogoutService {
            /** @return 是否校验入站 SLO 请求签名 */
            boolean validateRequestSignature();
            /** @return 是否校验 SLO 响应签名 */
            boolean validateResponseSignature();
            /** @return SP 发出的 SLO 请求是否签名 */
            boolean signRequest();
            /** @return SP 发出的 SLO 响应是否签名 */
            boolean signResponse();
            /** @return SLO 请求绑定类型 */
            Binding getRequestBinding();
            /** @return SLO 响应绑定类型 */
            Binding getResponseBinding();
            /** @return SLO 请求端点 URL */
            String getRequestBindingUrl();
            /** @return SLO 响应端点 URL */
            String getResponseBindingUrl();
        }
    }

    /**
     * 返回本部署关联的 IdP 配置。
     * @return IdP 配置
     */
    public IDP getIDP();

    /** @return 部署是否已完成配置加载 */
    public boolean isConfigured();
    /** @return SSL/TLS 策略要求 */
    SslRequired getSslRequired();

    /**
     * 返回 SP 实体标识符。
     * @return SP 实体 ID
     */
    String getEntityID();
    /** @return NameID 策略格式 URI */
    String getNameIDPolicyFormat();
    /** @return 是否强制重新认证 */
    boolean isForceAuthentication();
    /** @return 是否为被动模式（不可交互登录 UI） */
    boolean isIsPassive();
    /** @return 登录时是否禁用变更 HTTP 会话 ID */
    boolean turnOffChangeSessionIdOnLogin();
    /** @return 断言解密私钥 */
    PrivateKey getDecryptionKey();
    /** @return SP 签名密钥对 */
    KeyPair getSigningKeyPair();
    /** @return 签名规范化算法 URI */
    String getSignatureCanonicalizationMethod();
    /** @return 签名算法 */
    SignatureAlgorithm getSignatureAlgorithm();
    /** @return 本地登出页面路径 */
    String getLogoutPage();

    /** @return 从断言属性映射角色的属性名集合 */
    Set<String> getRoleAttributeNames();

    /**
     * 获取为 SP 配置的 {@link RoleMappingsProvider}。
     *
     * @return 角色映射提供者实例
     */
    RoleMappingsProvider getRoleMappingsProvider();

    /** 主体名称解析策略。 */
    enum PrincipalNamePolicy {
        /** 从 SAML NameID 取值。 */
        FROM_NAME_ID,
        /** 从指定断言属性取值。 */
        FROM_ATTRIBUTE
    }
    /** @return 主体名称解析策略 */
    PrincipalNamePolicy getPrincipalNamePolicy();
    /** @return 当策略为 FROM_ATTRIBUTE 时的属性名 */
    String getPrincipalAttributeName();
    /** @return 是否自动检测 Bearer-only 资源 */
    boolean isAutodetectBearerOnly();

    /** @return 是否在会话中保留断言 DOM 结构 */
    boolean isKeepDOMAssertion();

}
