/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.crypto;

import java.nio.charset.StandardCharsets;

import org.keycloak.provider.Provider;

/**
 * 通用哈希计算提供者 SPI（非密码存储场景）。
 * <p>提供字符串或字节数组的单向哈希，具体算法由实现决定。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface HashProvider extends Provider {


    /** 以 UTF-8 编码字符串后调用 {@link #hash(byte[])}。 */
    default byte[] hash(String input) throws HashException {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        return hash(inputBytes);
    }


    /**
     * 对输入字节数组计算哈希。
     * @param input 原始数据
     * @return 哈希结果字节
     * @throws HashException 算法不可用或计算失败
     */
    byte[] hash(byte[] input) throws HashException;


    /** 默认空实现，无资源需释放。 */
    @Override
    default void close() {
    }

}
