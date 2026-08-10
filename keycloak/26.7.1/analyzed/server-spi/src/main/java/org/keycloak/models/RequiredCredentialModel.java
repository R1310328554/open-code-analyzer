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

package org.keycloak.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;

/**
 * 必需凭证模型：描述 Realm 要求的登录凭证类型及其表单属性。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RequiredCredentialModel implements Serializable {

    protected String type;
    protected boolean input;
    protected boolean secret;
    protected String formLabel;

    public RequiredCredentialModel() {
    }

    /** @return 凭证类型 */
    public String getType() {
        return type;
    }

    /** @param type 凭证类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 是否需要用户输入 */
    public boolean isInput() {
        return input;
    }

    /** @param input 是否需要用户输入 */
    public void setInput(boolean input) {
        this.input = input;
    }

    /** @return 是否为敏感凭证 */
    public boolean isSecret() {
        return secret;
    }

    /** @param secret 是否为敏感凭证 */
    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    /** @return 登录表单标签键 */
    public String getFormLabel() {
        return formLabel;
    }

    /** @param formLabel 登录表单标签键 */
    public void setFormLabel(String formLabel) {
        this.formLabel = formLabel;
    }

    /** 内置必需凭证类型映射。 */
    public static final Map<String, RequiredCredentialModel> BUILT_IN;
    /** 密码凭证。 */
    public static final RequiredCredentialModel PASSWORD;
    /** TOTP 一次性密码凭证。 */
    public static final RequiredCredentialModel TOTP;
    /** 客户端证书凭证。 */
    public static final RequiredCredentialModel CLIENT_CERT;
    /** 客户端密钥凭证。 */
    public static final RequiredCredentialModel SECRET;
    /** Kerberos 凭证。 */
    public static final RequiredCredentialModel KERBEROS;

    static {
        Map<String, RequiredCredentialModel> map = new HashMap<>();
        PASSWORD = new RequiredCredentialModel();
        PASSWORD.setType(PasswordCredentialModel.TYPE);
        PASSWORD.setInput(true);
        PASSWORD.setSecret(true);
        PASSWORD.setFormLabel("password");
        map.put(PASSWORD.getType(), PASSWORD);
        SECRET = new RequiredCredentialModel();
        SECRET.setType(UserCredentialModel.SECRET);
        SECRET.setInput(false);
        SECRET.setSecret(true);
        SECRET.setFormLabel("secret");
        map.put(SECRET.getType(), SECRET);
        TOTP = new RequiredCredentialModel();
        TOTP.setType(OTPCredentialModel.TYPE);
        TOTP.setInput(true);
        TOTP.setSecret(false);
        TOTP.setFormLabel("authenticatorCode");
        map.put(TOTP.getType(), TOTP);
        CLIENT_CERT = new RequiredCredentialModel();
        CLIENT_CERT.setType(UserCredentialModel.CLIENT_CERT);
        CLIENT_CERT.setInput(false);
        CLIENT_CERT.setSecret(false);
        CLIENT_CERT.setFormLabel("clientCertificate");
        map.put(CLIENT_CERT.getType(), CLIENT_CERT);
        KERBEROS = new RequiredCredentialModel();
        KERBEROS.setType(UserCredentialModel.KERBEROS);
        KERBEROS.setInput(false);
        KERBEROS.setSecret(false);
        KERBEROS.setFormLabel("kerberos");
        map.put(KERBEROS.getType(), KERBEROS);
        BUILT_IN = Collections.unmodifiableMap(map);
    }
}
