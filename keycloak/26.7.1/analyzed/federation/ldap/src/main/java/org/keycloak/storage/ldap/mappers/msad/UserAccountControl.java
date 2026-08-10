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

package org.keycloak.storage.ldap.mappers.msad;

/**
 * Active Directory userAccountControl 位标志封装，用于读写 MSAD 账户状态。
 * <p>
 * 参见 https://support.microsoft.com/en-us/kb/305144
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserAccountControl {

    /** 登录脚本。 */
    public static final long SCRIPT = 0x0001L;
    /** 账户已禁用。 */
    public static final long ACCOUNTDISABLE = 0x0002L;
    /** 需要主目录。 */
    public static final long HOMEDIR_REQUIRED = 0x0008L;
    /** 账户已锁定。 */
    public static final long LOCKOUT = 0x0010L;
    /** 不需要密码。 */
    public static final long PASSWD_NOTREQD = 0x0020L;
    /** 用户不能更改密码。 */
    public static final long PASSWD_CANT_CHANGE = 0x0040L;
    /** 允许加密文本密码。 */
    public static final long ENCRYPTED_TEXT_PWD_ALLOWED = 0x0080L;
    /** 临时重复账户。 */
    public static final long TEMP_DUPLICATE_ACCOUNT = 0x0100L;
    /** 普通用户账户。 */
    public static final long NORMAL_ACCOUNT = 0x0200L;
    /** 域间信任账户。 */
    public static final long INTERDOMAIN_TRUST_ACCOUNT = 0x0800L;
    /** 工作站信任账户。 */
    public static final long WORKSTATION_TRUST_ACCOUNT = 0x1000L;
    /** 服务器信任账户。 */
    public static final long SERVER_TRUST_ACCOUNT = 0x2000L;
    /** 密码永不过期。 */
    public static final long DONT_EXPIRE_PASSWORD = 0x10000L;
    /** MNS 登录账户。 */
    public static final long MNS_LOGON_ACCOUNT = 0x20000L;
    /** 需要智能卡。 */
    public static final long SMARTCARD_REQUIRED = 0x40000L;
    /** 信任委派。 */
    public static final long TRUSTED_FOR_DELEGATION = 0x80000L;
    /** 不可委派。 */
    public static final long NOT_DELEGATED = 0x100000L;
    /** 仅使用 DES 密钥。 */
    public static final long USE_DES_KEY_ONLY = 0x200000L;
    /** 不需要 Kerberos 预认证。 */
    public static final long DONT_REQ_PREAUTH = 0x400000L;
    /** 密码已过期。 */
    public static final long PASSWORD_EXPIRED = 0x800000L;
    /** 信任以进行委派认证。 */
    public static final long TRUSTED_TO_AUTH_FOR_DELEGATION = 0x1000000L;
    /** 部分机密账户。 */
    public static final long PARTIAL_SECRETS_ACCOUNT = 0x04000000L;

    private static final UserAccountControl EMPTY = new UserAccountControl(0);

    /** 返回值为 0 的空控制对象。 */
    public static UserAccountControl empty() {
        return EMPTY;
    }

    /** 从 LDAP userAccountControl 字符串解析位标志。 */
    public static UserAccountControl of(String userAccountControl) {
        if (userAccountControl == null) {
            return empty();
        }
        return new UserAccountControl(Long.parseLong(userAccountControl));
    }

    private long value;

    private UserAccountControl(long value) {
        this.value = value;
    }

    /** 是否设置了指定位标志。 */
    public boolean has(long feature) {
        return (this.value & feature) > 0;
    }

    /** 添加指定位标志（若尚未设置）。 */
    public void add(long feature) {
        if (!has(feature)) {
            this.value += feature;
        }
    }

    /** 移除指定位标志（若已设置）。 */
    public void remove(long feature) {
        if (has(feature)) {
            this.value -= feature;
        }
    }

    /** 返回当前整型位掩码值。 */
    public long getValue() {
        return value;
    }

    /** 是否设置了任意位标志。 */
    public boolean isAnySet() {
        return value != 0;
    }
}
