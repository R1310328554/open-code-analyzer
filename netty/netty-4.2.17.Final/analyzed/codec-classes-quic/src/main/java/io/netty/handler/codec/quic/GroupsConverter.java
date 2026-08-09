/*
 * Copyright 2024 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

/**
 * 将 Java 椭圆曲线/密钥交换组命名转换为 BoringSSL 命名；无法映射时返回原名。
 */
final class GroupsConverter {

    // 参见 RFC4492 附录 A 与 Java 加密配置文档
    /**
     * 将 Java 侧组名转为 BoringSSL 可识别的名称。
     *
     * @param key Java 标准或常用别名（如 prime256v1、secp256r1）
     * @return BoringSSL 组名，或无法映射时的原字符串
     */
    static String toBoringSSL(String key) {
        switch (key) {
            case "secp224r1":
                return "P-224";
            case "prime256v1":
            case "secp256r1":
                return "P-256";
            case "secp384r1":
                return "P-384";
            case "secp521r1":
                return "P-521";
            case "x25519":
                return "X25519";
            default:
                return key;
        }
    }

    private GroupsConverter() { }
}
