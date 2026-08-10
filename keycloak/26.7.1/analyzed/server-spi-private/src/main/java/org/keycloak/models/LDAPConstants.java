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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * LDAP 用户联邦组件配置键与标准 LDAP 属性名常量。
 * <p>供 LDAP 存储提供者、映射器与同步逻辑引用。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPConstants {

    /** LDAP 用户存储提供者 ID。 */
    public static final String LDAP_PROVIDER = "ldap";
    public static final String MSAD_USER_ACCOUNT_CONTROL_MAPPER = "msad-user-account-control-mapper";
    public static final String MSADLDS_USER_ACCOUNT_CONTROL_MAPPER = "msad-lds-user-account-control-mapper";

    /** LDAP 服务器厂商配置键。 */
    public static final String VENDOR = "vendor";
    public static final String VENDOR_RHDS = "rhds";
    /** Active Directory 厂商标识。 */
    public static final String VENDOR_ACTIVE_DIRECTORY = "ad";
    public static final String VENDOR_OTHER = "other";
    public static final String VENDOR_TIVOLI = "tivoli";
    public static final String VENDOR_NOVELL_EDIRECTORY="edirectory" ;

    // 可通过 rootDse supportedExtension 1.3.6.1.4.1.4203.1.11.1 探测
    public static final String USE_PASSWORD_MODIFY_EXTENDED_OP = "usePasswordModifyExtendedOp";

    public static final String USERNAME_LDAP_ATTRIBUTE = "usernameLDAPAttribute";
    public static final String RDN_LDAP_ATTRIBUTE = "rdnLDAPAttribute";
    public static final String UUID_LDAP_ATTRIBUTE = "uuidLDAPAttribute";
    public static final String USER_OBJECT_CLASSES = "userObjectClasses";

    /** LDAP 连接 URL 配置键。 */
    public static final String CONNECTION_URL = "connectionUrl";
    public static final String BASE_DN = "baseDn"; // used for tests only
    /** 用户搜索基准 DN 配置键。 */
    public static final String USERS_DN = "usersDn";
    public static final String RELATIVE_CREATE_DN = "relativeCreateDn";
    /** LDAP 绑定 DN 配置键。 */
    public static final String BIND_DN = "bindDn";
    /** LDAP 绑定凭证配置键。 */
    public static final String BIND_CREDENTIAL = "bindCredential";

    public static final String AUTH_TYPE = "authType";
    public static final String AUTH_TYPE_NONE = "none";
    public static final String AUTH_TYPE_SIMPLE = "simple";

    public static final String USE_TRUSTSTORE_SPI = "useTruststoreSpi";
    public static final String USE_TRUSTSTORE_ALWAYS = "always";
    public static final String USE_TRUSTSTORE_NEVER = "never";

    public static final String CONNECTION_TRACE_BER = "com.sun.jndi.ldap.trace.ber";

    /**
     * @deprecated Use {@link #USE_TRUSTSTORE_ALWAYS} instead.
     */
    @Deprecated
    public static final String USE_TRUSTSTORE_LDAPS_ONLY = "ldapsOnly";

    public static final String SEARCH_SCOPE = "searchScope";
    public static final String CONNECTION_POOLING = "connectionPooling";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String READ_TIMEOUT = "readTimeout";
    // Could be discovered by rootDse supportedControl: 1.2.840.113556.1.4.319
    public static final String PAGINATION = "pagination";
    public static final String MAX_CONDITIONS = "maxConditions";
    public static final int DEFAULT_MAX_CONDITIONS = 64;

    /** 用户编辑模式（READ_ONLY / WRITABLE / UNSYNCED）。 */
    public static final String EDIT_MODE = "editMode";

    public static final String VALIDATE_PASSWORD_POLICY = "validatePasswordPolicy";

    public static final String TRUST_EMAIL = "trustEmail";

    // 同步过程中单次事务处理的用户数量
    public static final String BATCH_SIZE_FOR_SYNC = "batchSizeForSync";
    public static final int DEFAULT_BATCH_SIZE_FOR_SYNC = 1000;

    // 是否将新注册用户同步到 LDAP
    public static final String SYNC_REGISTRATIONS = "syncRegistrations";

    // 自定义用户 LDAP 搜索过滤器
    public static final String CUSTOM_USER_SEARCH_FILTER = "customUserSearchFilter";

    // Could be discovered by rootDse supportedExtension: 1.3.6.1.4.1.1466.20037
    public static final String START_TLS = "startTls";

    // 映射到 LDAP 的 UserModel 自定义属性名
    /** UserModel 上存储 LDAP 条目 UUID 的属性名。 */
    public static final String LDAP_ID = "LDAP_ID";
    /** UserModel 上存储 LDAP 条目 DN 的属性名。 */
    public static final String LDAP_ENTRY_DN = "LDAP_ENTRY_DN";

    // 以下 LDAP 属性名源自 Picketlink
    public static final String GIVENNAME = "givenName";
    public static final String CN = "cn";
    public static final String SN = "sn";
    public static final String SAM_ACCOUNT_NAME = "sAMAccountName";
    public static final String EMAIL = "mail";
    public static final String POSTAL_CODE = "postalCode";
    public static final String STREET = "street";
    public static final String MEMBER = "member";
    public static final String MEMBER_OF = "memberOf";
    public static final String OBJECT_CLASS = "objectclass";
    public static final String UID = "uid";
    public static final String USER_PASSWORD_ATTRIBUTE = "userpassword";
    public static final String JPEG_PHOTO = "jpegPhoto";
    public static final String GROUP = "group";
    public static final String GROUP_OF_NAMES = "groupOfNames";
    public static final String GROUP_OF_ENTRIES = "groupOfEntries";
    public static final String GROUP_OF_UNIQUE_NAMES = "groupOfUniqueNames";
    /** AD userAccountControl 属性（账户启用/禁用等标志位）。 */
    public static final String USER_ACCOUNT_CONTROL = "userAccountControl";
    public static final String PWD_LAST_SET = "pwdLastSet";
    public static final String PWD_CHANGED_TIME = "pwdChangedTime";
    public static final String PWD_UPDATE_TIME = "pwdUpdateTime";
    public static final String MSDS_USER_ACCOUNT_DISABLED = "msDS-UserAccountDisabled";
    public static final String MSDS_USER_PASSWORD_NOTREQD = "msDS-UserPasswordNotRequired";
    public static final String MSDS_USER_PASSWORD_EXPIRED = "msDS-UserPasswordExpired"; // read-only

    public static final String COMMA = ",";
    public static final String EQUAL = "=";
    public static final String EMPTY_ATTRIBUTE_VALUE = " ";
    public static final String EMPTY_MEMBER_ATTRIBUTE_VALUE = "cn=empty-membership-placeholder";

    public static final String ENABLED = "enabled";


    public static final String CUSTOM_ATTRIBUTE_CREATE_DATE = "createDate";
    public static final String CUSTOM_ATTRIBUTE_EXPIRY_DATE = "expiryDate";
    public static final String ENTRY_UUID = "entryUUID";
    public static final String OBJECT_GUID = "objectGUID";
    public static final String NOVELL_EDIRECTORY_GUID = "guid";
    public static final String CREATE_TIMESTAMP = "createTimestamp";
    public static final String MODIFY_TIMESTAMP = "modifyTimestamp";

    public static final String LDAP_MATCHING_RULE_IN_CHAIN = ":1.2.840.113556.1.4.1941:";

    public static final String REFERRAL = "referral";
    public static final String ENABLE_LDAP_PASSWORD_POLICY = "enableLdapPasswordPolicy";

    public static final String CONNECTION_TRACE = "connectionTrace";

    /** 按 LDAP 厂商返回 UUID/唯一标识属性名。 */
    public static String getUuidAttributeName(String vendor) {
        if (vendor != null) {
            switch (vendor) {
                case VENDOR_RHDS:
                    return "nsuniqueid";
                case VENDOR_TIVOLI:
                    return "uniqueidentifier";
                case VENDOR_NOVELL_EDIRECTORY:
                    return "guid";
                case VENDOR_ACTIVE_DIRECTORY:
                    return OBJECT_GUID;
            }
        }

        return ENTRY_UUID;
    }

    /**
     * 将空格分隔的 LDAP URL 字符串拆分为 URL 列表。
     * @see com.sun.jndi.ldap.LdapURL#fromList(String) (Not using it directly to avoid usage of internal Java classes)
     *
     * @param ldapUrlList LDAP URL, which can possibly consists from multiple URLs like "ldaps://host1:636 ldaps://host2:636"
     * @return List of all URLs
     */
    public static List<String> toLdapUrls(String ldapUrlList) {
        if (ldapUrlList == null) return Collections.emptyList();
        return  Arrays.asList(ldapUrlList.split(" "));
    }
}
