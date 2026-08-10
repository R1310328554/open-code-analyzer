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

package org.keycloak.common.constants;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

/**
 * Kerberos / SPNEGO 集成相关常量。
 *
 * <p>包含 GSS 机制 OID、HTTP 头值、LDAP 联合配置键及内部会话属性名。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KerberosConstants {

    /**
     * HTTP 头 {@code WWW-Authenticate} 或 {@code Authorization} 中 SPNEGO/Kerberos 使用的 scheme 值。
     **/
    public static final String NEGOTIATE = "Negotiate";


    /**
     * SPNEGO 机制 OID。参见 http://www.oid-info.com/get/1.3.6.1.5.5.2
     */
    private static final String SPNEGO_OID_STR = "1.3.6.1.5.5.2";
    /** SPNEGO GSS 机制对象标识符。 */
    public static final Oid SPNEGO_OID;


    /**
     * Kerberos v5 机制 OID。参见 http://www.oid-info.com/get/1.2.840.113554.1.2.2
     */
    private static final String KRB5_OID_STR = "1.2.840.113554.1.2.2";
    /** Kerberos v5 机制 OID。 */
    public static final Oid KRB5_OID;


    /**
     * Kerberos v5 名称 OID。参见 http://www.oid-info.com/get/1.2.840.113554.1.2.2.1
     */
    private static final String KRB5_NAME_OID_STR = "1.2.840.113554.1.2.2.1";
    /** Kerberos v5 主体名称 OID。 */
    public static final Oid KRB5_NAME_OID;


    static {
        try {
            KRB5_OID = new Oid(KerberosConstants.KRB5_OID_STR);
            KRB5_NAME_OID = new Oid(KerberosConstants.KRB5_NAME_OID_STR);
            SPNEGO_OID = new Oid(KerberosConstants.SPNEGO_OID_STR);
        } catch (GSSException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 用户联合（User Federation）Kerberos 提供方配置属性名。
     */
    public static final String ALLOW_KERBEROS_AUTHENTICATION = "allowKerberosAuthentication";
    public static final String KERBEROS_REALM = "kerberosRealm";
    public static final String SERVER_PRINCIPAL = "serverPrincipal";
    public static final String KEYTAB = "keyTab";
    public static final String DEBUG = "debug";
    public static final String KERBEROS_PRINCIPAL_ATTRIBUTE = "krbPrincipalAttribute";
    public static final String KERBEROS_PRINCIPAL_LDAP_ATTRIBUTE_KRB5_PRINCIPAL_NAME = "krb5PrincipalName"; // ApacheDS 等目录常用
    public static final String KERBEROS_PRINCIPAL_LDAP_ATTRIBUTE_KRB_PRINCIPAL_NAME = "krbPrincipalName"; // FreeIPA 等目录常用
    public static final String KERBEROS_PRINCIPAL_LDAP_ATTRIBUTE_USER_PRINCIPAL_NAME = "userPrincipalName"; // MSAD 等目录常用

    public static final String ALLOW_PASSWORD_AUTHENTICATION = "allowPasswordAuthentication";
    public static final String UPDATE_PROFILE_FIRST_LOGIN = "updateProfileFirstLogin";
    public static final String USE_KERBEROS_FOR_PASSWORD_AUTHENTICATION = "useKerberosForPasswordAuthentication";


    /**
     * 内部 state 映射键：存放需回传给浏览器以继续握手的 SPNEGO 响应令牌。
     */
    public static final String RESPONSE_TOKEN = "SpnegoResponseToken";


    /**
     * 用户会话注记与访问令牌声明键：SPNEGO/Kerberos 认证成功后获得的委派凭证。
     */
    public static final String GSS_DELEGATION_CREDENTIAL = "gss_delegation_credential";

    /**
     * 管理控制台与同意页面上 {@link #GSS_DELEGATION_CREDENTIAL} 的显示名称。
     */
    public static final String GSS_DELEGATION_CREDENTIAL_DISPLAY_NAME = "gss delegation credential";

    /**
     * 凭证附加属性：已认证的 SPNEGO 上下文。
     *
     * <p>当 LDAP/Kerberos 提供方通过 SPNEGO 认证用户但无法在本地 LDAP 树中定位该用户时，
     * 后续联合链中的其他提供方可复用此上下文再次执行 LDAP 查找。</p>
     */
    public static final String AUTHENTICATED_SPNEGO_CONTEXT = "authenticatedSpnegoContext";

    /*
     * Kerberos/LDAP 提供方创建的用户上存储 Kerberos 主体名的用户属性。
     */
    public static final String KERBEROS_PRINCIPAL = "KERBEROS_PRINCIPAL";
}
