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

package org.keycloak.storage.jpa;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.keycloak.crypto.HashException;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.models.jpa.entities.UserEntity;

/**
 * 为数据库中长属性值生成哈希的工具类，支持精确匹配与小写匹配两种变体。
 * <p>
 * Keycloak 使用小写哈希近似实现大小写不敏感搜索；小写转换固定使用 {@link Locale#ENGLISH}，
 * 避免因 JVM 区域设置变化导致哈希不一致（全量重算代价极高）。
 *
 * @author Alexander Schwartz
 */
public class JpaHashUtils {

    /** 对输入字节序列计算 SHA-512 哈希。 */
    private static byte[] hash(byte[] inputBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(JavaAlgorithm.SHA512);
            md.update(inputBytes);
            return md.digest();
        } catch (Exception e) {
            throw new HashException("Error when creating token hash", e);
        }
    }

    /** 为属性值（精确匹配）生成哈希。 */
    public static byte[] hashForAttributeValue(String value) {
        return JpaHashUtils.hash(value.getBytes(StandardCharsets.UTF_8));
    }

    /** 为属性值（转英文小写后）生成哈希，用于大小写不敏感搜索。 */
    public static byte[] hashForAttributeValueLowerCase(String value) {
        return JpaHashUtils.hash(value.toLowerCase(Locale.ENGLISH).getBytes(StandardCharsets.UTF_8));
    }

    /** 以英文小写比较两个源字符串是否相等。 */
    public static boolean compareSourceValueLowerCase(String value1, String value2) {
        return Objects.equals(value1.toLowerCase(Locale.ENGLISH), value2.toLowerCase(Locale.ENGLISH));
    }

    /** 精确比较两个源字符串是否相等。 */
    public static boolean compareSourceValue(String value1, String value2) {
        return Objects.equals(value1, value2);
    }

    /**
     * 返回谓词：当用户拥有 {@code customLongValueSearchAttributes} 中全部属性时为 true。
     * <p>
     * 按属性名与值精确比对；长属性哈希查询可能因碰撞返回不含该属性的用户，故需二次过滤。
     *
     * @param customLongValueSearchAttributes 必需属性名值映射
     * @param valueComparator                 属性值比较器
     * @return 按属性映射过滤用户的谓词
     */
    public static java.util.function.Predicate<UserEntity> predicateForFilteringUsersByAttributes(Map<String, String> customLongValueSearchAttributes, BiPredicate<String, String> valueComparator) {
        return userEntity -> customLongValueSearchAttributes.isEmpty() || // are there some long attribute values
                customLongValueSearchAttributes
                        .entrySet()
                        .stream()
                        .allMatch(longAttrEntry -> //for all long search attributes
                                userEntity
                                        .getAttributes()
                                        .stream()
                                        .anyMatch(userAttribute -> //check whether the user indeed has the attribute
                                                Objects.equals(longAttrEntry.getKey().toLowerCase(), userAttribute.getName().toLowerCase())
                                                        && valueComparator.test(longAttrEntry.getValue(), userAttribute.getValue())
                                        )
                        );
    }

}
