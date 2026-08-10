/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.common.util.SecretGenerator;

/**
 * OAuth 2.0 设备授权（Device Authorization Grant）用户码的默认生成与格式化实现。
 * <p>生成：8 位大写字母；展示：{@code XXXX-XXXX} 格式。</p>
 *
 * @author <a href="mailto:h2-wada@nri.co.jp">Hiroyuki Wada</a>
 */
public class DefaultOAuth2DeviceUserCodeProvider implements OAuth2DeviceUserCodeProvider {

    /** 用户码字符长度。 */
    private static final int LENGTH = 8;
    /** 展示格式分隔符。 */
    private static final String DELIMITER = "-";

    /** 生成 8 位大写随机用户码。 */
    @Override
    public String generate() {
        // 大小写不敏感场景使用大写
        return SecretGenerator.getInstance().randomString(LENGTH, SecretGenerator.UPPER);
    }

    /** 将用户码格式化为 {@code XXXX-XXXX} 展示形式。 */
    @Override
    public String display(String userCode) {
        return new StringBuilder(userCode).insert(4, DELIMITER).toString();
    }

    /** 去除分隔符并转为大写，用于服务端比对。 */
    @Override
    public String format(String userCode) {
        return String.join("", userCode.split(DELIMITER)).toUpperCase();
    }

    /** 无资源需释放。 */
    @Override
    public void close() {

    }
}
