/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户端/SDK 语言标识，随 Remoting 握手上报。
 */
public enum LanguageCode {
    /** Java 客户端。 */
    JAVA((byte) 0),
    /** C++ 客户端。 */
    CPP((byte) 1),
    /** .NET 客户端。 */
    DOTNET((byte) 2),
    /** Python 客户端。 */
    PYTHON((byte) 3),
    DELPHI((byte) 4),
    ERLANG((byte) 5),
    RUBY((byte) 6),
    OTHER((byte) 7),
    HTTP((byte) 8),
    /** Go 客户端。 */
    GO((byte) 9),
    PHP((byte) 10),
    OMS((byte) 11),
    /** Rust 客户端。 */
    RUST((byte) 12),
    /** Node.js 客户端。 */
    NODE_JS((byte) 13);

    /** 协议层单字节语言码。 */
    private byte code;

    /** 绑定协议码。 */
    LanguageCode(byte code) {
        this.code = code;
    }

    /** 按字节码查找枚举，未命中返回 null。 */
    public static LanguageCode valueOf(byte code) {
        for (LanguageCode languageCode : LanguageCode.values()) {
            if (languageCode.getCode() == code) {
                return languageCode;
            }
        }
        return null;
    }

    /** 返回协议语言码。 */
    public byte getCode() {
        return code;
    }
    
    private static final Map<String, LanguageCode> MAP = Arrays.stream(LanguageCode.values()).collect(Collectors.toMap(LanguageCode::name, Function.identity()));

    /** 按枚举名（如 JAVA）查找，未命中返回 null。 */
    public static LanguageCode getCode(String language) {
        return MAP.get(language);
    }
}
