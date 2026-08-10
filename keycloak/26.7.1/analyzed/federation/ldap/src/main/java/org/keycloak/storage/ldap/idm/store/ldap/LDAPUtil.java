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

package org.keycloak.storage.ldap.idm.store.ldap;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.LDAPConfig;

import org.jboss.logging.Logger;

/**
 * <p>LDAP 通用工具类：日期格式化、GUID 编解码、Truststore SPI 判定等。</p>
 *
 * @author Pedro Igor
 */
public class LDAPUtil {

    private static final Logger logger = Logger.getLogger(LDAPUtil.class);

    /**
     * <p>将日期格式化为 LDAP 通用时间字符串。</p>
     *
     * @param date 待格式化的日期
     *
     * @return 格式化后的 UTC 时间字符串
     */
    public static final String formatDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("You must provide a date.");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(date);
    }

    /**
     * <p>
     * 解析 LDAP 中存储的日期/时间戳，支持多种常见格式：
     * </p>
     * <ul>
     *     <li>20020228150820</li>
     *     <li>20030228150820Z</li>
     *     <li>20050228150820.12</li>
     *     <li>20060711011740.0Z</li>
     * </ul>
     *
     * @param date LDAP 日期字符串
     *
     * @return 解析后的 {@link Date}
     */
    public static final Date parseDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            if (date.endsWith("Z")) {
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                dateFormat.setTimeZone(TimeZone.getDefault());
            }

            return dateFormat.parse(date);
        } catch (Exception e) {
            throw new ModelException("Error converting ldap date.", e);
        }
    }



    /**
     * <p>将 Active Directory {@code objectGUID} 原始字节数组转为 LDAP 过滤器可用的字节串表示。</p>
     *
     * <p>示例过滤器：</p>
     *
     * <p>
     * String filter = "(&(objectClass=*)(objectGUID" + EQUAL + convertObjectGUIDToByteString(objectGUID) + "))";
     * </p>
     *
     * @param objectGUID AD 返回的 objectGUID 字节数组
     *
     * @return 形如 \[0]\[1]...\[15] 的字节串
     */
    public static String convertObjectGUIDToByteString(byte[] objectGUID) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < objectGUID.length; i++) {
            String transformed = prefixZeros((int) objectGUID[i] & 0xFF);
            result.append("\\");
            result.append(transformed);
        }

        return result.toString();
    }

    /**
     * 将带连字符的 GUID 转为 eDirectory 十六进制转义串。
     * 参见 http://support.novell.com/docs/Tids/Solutions/10096551.html
     *
     * @param guid 带连字符的 GUID 字符串（参见 {@link #decodeObjectGUID(byte[])} 等）
     *
     * @return 形如 \[0][1]\[2]...\[15] 的转义串
     */
    public static String convertGUIDToEdirectoryHexString(String guid) {
        String withoutDash = guid.replace("-", "");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < withoutDash.length(); i++) {
            result.append("\\");
            result.append(withoutDash.charAt(i));
            result.append(withoutDash.charAt(++i));
        }

        return result.toString().toUpperCase();
    }

    /**
     * 将 eDirectory GUID 字符串编码为字节数组。
     * @param guid 带连字符或不带连字符的十六进制 GUID
     * @return 16 字节 GUID
     */
    public static byte[] encodeObjectEDirectoryGUID(String guid) {
        String withoutDash = guid.replace("-", "");
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        for (int i = 0; i < withoutDash.length(); i++) {
            String byteStr = new StringBuilder().append(withoutDash.charAt(i)).append(withoutDash.charAt(++i)).toString();
            result.write(Integer.parseInt(byteStr, 16));
        }

        return result.toByteArray();
    }

    /**
     * <p>将 AD objectGUID 显示字符串编码为原始 16 字节数组。</p>
     *
     * @param displayString 解码后的 GUID 字符串，形如 [3][2][1][0]-[5][4]-...
     *
     * @return objectGUID 原始字节数组
     */
    public static byte[] encodeObjectGUID(String displayString) {
        byte [] objectGUID = new byte[16];
        // [3][2][1][0]
        objectGUID[0] = (byte) ((Character.digit(displayString.charAt(6), 16) << 4)
                + Character.digit(displayString.charAt(7), 16));
        objectGUID[1] = (byte) ((Character.digit(displayString.charAt(4), 16) << 4)
                + Character.digit(displayString.charAt(5), 16));
        objectGUID[2] = (byte) ((Character.digit(displayString.charAt(2), 16) << 4)
                + Character.digit(displayString.charAt(3), 16));
        objectGUID[3] = (byte) ((Character.digit(displayString.charAt(0), 16) << 4)
                + Character.digit(displayString.charAt(1), 16));
        // [5][4]
        objectGUID[4] = (byte) ((Character.digit(displayString.charAt(11), 16) << 4)
                + Character.digit(displayString.charAt(12), 16));
        objectGUID[5] = (byte) ((Character.digit(displayString.charAt(9), 16) << 4)
                + Character.digit(displayString.charAt(10), 16));
        // [7][6]
        objectGUID[6] = (byte) ((Character.digit(displayString.charAt(16), 16) << 4)
                + Character.digit(displayString.charAt(17), 16));
        objectGUID[7] = (byte) ((Character.digit(displayString.charAt(14), 16) << 4)
                + Character.digit(displayString.charAt(15), 16));
        // [8][9]
        objectGUID[8] = (byte) ((Character.digit(displayString.charAt(19), 16) << 4)
                + Character.digit(displayString.charAt(20), 16));
        objectGUID[9] = (byte) ((Character.digit(displayString.charAt(21), 16) << 4)
                + Character.digit(displayString.charAt(22), 16));
        // [10][11][12][13][14][15]
        objectGUID[10] = (byte) ((Character.digit(displayString.charAt(24), 16) << 4)
                + Character.digit(displayString.charAt(25), 16));
        objectGUID[11] = (byte) ((Character.digit(displayString.charAt(26), 16) << 4)
                + Character.digit(displayString.charAt(27), 16));
        objectGUID[12] = (byte) ((Character.digit(displayString.charAt(28), 16) << 4)
                + Character.digit(displayString.charAt(29), 16));
        objectGUID[13] = (byte) ((Character.digit(displayString.charAt(30), 16) << 4)
                + Character.digit(displayString.charAt(31), 16));
        objectGUID[14] = (byte) ((Character.digit(displayString.charAt(32), 16) << 4)
                + Character.digit(displayString.charAt(33), 16));
        objectGUID[15] = (byte) ((Character.digit(displayString.charAt(34), 16) << 4)
                + Character.digit(displayString.charAt(35), 16));
        return objectGUID;
    }

    /**
     * <p>将 AD objectGUID 原始字节解码为带连字符的 GUID 字符串，可直接用于条目绑定。</p>
     *
     * <p>示例：</p>
     *
     * <p>
     * String bindingString = decodeObjectGUID(objectGUID);
     * <br/>
     * Attributes attributes = ctx.getAttributes(bindingString);
     * </p>
     *
     * @param objectGUID AD objectGUID 字节数组
     *
     * @return 形如 [3][2][1][0]-[5][4]-... 的 GUID 字符串
     */
    public static String decodeObjectGUID(byte[] objectGUID) {
        StringBuilder displayStr = new StringBuilder();

        displayStr.append(convertToDashedString(objectGUID));

        return displayStr.toString();
    }

    /**
     * <p>将 Novell eDirectory {@code guid} 属性字节解码为带连字符的 GUID 字符串。</p>
     *
     * @param guid eDirectory guid 字节数组
     *
     * @return 形如 [0][1][2][3]-[4][5]-... 的 GUID 字符串
     */
    public static String decodeGuid(byte[] guid) {
        byte[] withBigEndian = new byte[] { guid[3], guid[2], guid[1], guid[0],
            guid[5], guid[4],
            guid[7], guid[6],
            guid[8], guid[9], guid[10], guid[11], guid[12], guid[13], guid[14], guid[15]
        };
        return convertToDashedString(withBigEndian);
    }

    /**
     * 将 Base64 编码的二进制 UUID 属性解码为 GUID 字符串。
     * 根据 {@link LDAPConfig} 选用 {@link #decodeObjectGUID(byte[])}（AD）或 {@link #decodeGuid(byte[])}（eDirectory）；
     * 无法识别时返回原始 Base64 值。
     *
     * @param base64Value UUID 属性的 Base64 编码值
     * @param config 用于选择解码策略的 LDAP 配置
     * @return 解码后的 UUID 字符串，或原 Base64 值
     */
    public static String decodeBase64ToUuid(String base64Value, LDAPConfig config) {
        if (base64Value == null) return null;
        byte[] bytes = Base64.getDecoder().decode(base64Value);
        if (bytes.length != 16) {
            logger.warnf("Binary attribute value is %d bytes but a UUID requires exactly 16 bytes. Returning base64-encoded value.", bytes.length);
            return base64Value;
        }
        if (config.isObjectGUID()) {
            return decodeObjectGUID(bytes);
        }
        if (config.isEdirectory() && config.isEdirectoryGUID()) {
            return decodeGuid(bytes);
        }
        return base64Value;
    }

    /** 将 16 字节 GUID 转为带连字符的十六进制字符串（AD 字节序）。 */
    private static String convertToDashedString(byte[] objectGUID) {
        StringBuilder displayStr = new StringBuilder();

        displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));
        displayStr.append("-");
        displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF));
        displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF));

        return displayStr.toString();
    }

    /** 将 0–255 的整数格式化为两位十六进制（不足补零）。 */
    private static String prefixZeros(int value) {
        if (value <= 0xF) {
            StringBuilder sb = new StringBuilder("0");
            sb.append(Integer.toHexString(value));
            return sb.toString();
        } else {
            return Integer.toHexString(value);
        }
    }

    /**
     * 判断是否应使用 Keycloak Truststore SPI 提供的 SSL 套接字工厂。
     * LDAPS 或 StartTLS 时默认启用，除非配置为 {@code never}。
     *
     * @param ldapConfig LDAP 配置
     * @return 是否使用 Truststore SPI
     */
    public static boolean shouldUseTruststoreSpi(LDAPConfig ldapConfig) {
        boolean useSSL = ldapConfig.getConnectionUrl().toLowerCase().contains("ldaps://");
        boolean defaultUseTruststore = useSSL || ldapConfig.isStartTls();

        String useTruststoreSpi = ldapConfig.getUseTruststoreSpi();
        if (useTruststoreSpi == null) {
            return defaultUseTruststore;
        }

        if (LDAPConstants.USE_TRUSTSTORE_NEVER.equals(useTruststoreSpi)) {
            return false;
        }

        return defaultUseTruststore;
    }
}
