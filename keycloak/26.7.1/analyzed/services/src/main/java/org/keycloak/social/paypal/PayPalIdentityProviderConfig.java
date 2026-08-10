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
package org.keycloak.social.paypal;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * PayPal 身份提供者配置。
 * <p>扩展 {@link org.keycloak.broker.oidc.OAuth2IdentityProviderConfig}，支持沙箱环境开关。</p>
 *
 * @author Petter Lysne (petterlysne at hotmail dot com)
 */
public class PayPalIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    /** 从 realm 中的 IdP 模型构造配置。 */
    public PayPalIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** 创建空配置（管理控制台新建 IdP 时使用）。 */
    public PayPalIdentityProviderConfig() {
        
    }

    /** 是否指向 PayPal 沙箱环境。 */
    public boolean targetSandbox() {
        String sandbox = getConfig().get("sandbox");
        return sandbox == null ? false : Boolean.valueOf(sandbox);
    }

    /** 设置是否使用沙箱环境。 */
    public void setSandbox(boolean sandbox) {
        getConfig().put("sandbox", String.valueOf(sandbox));
    }

}
