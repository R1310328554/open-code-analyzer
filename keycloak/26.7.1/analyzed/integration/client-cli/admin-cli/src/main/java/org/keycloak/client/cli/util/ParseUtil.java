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
package org.keycloak.client.cli.util;

/**
 * CLI 键值参数字符串解析工具。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ParseUtil {

    /**
     * 将 {@code key=value} 字符串拆分为键与值。
     * <p>
     * 要求等号不在首位且至少出现一次；值部分保留等号后的全部内容。
     *
     * @param keyval 原始键值字符串
     * @return 长度为 2 的数组，索引 0 为键、1 为值
     * @throws IllegalArgumentException 格式无效时抛出
     */
    public static String[] parseKeyVal(String keyval) {
        // 期望以 = 作为分隔符
        int pos = keyval.indexOf("=");
        if (pos <= 0) {
            throw new IllegalArgumentException("Invalid key=value parameter: [" + keyval + "]");
        }

        String [] parsed = new String[2];
        parsed[0] = keyval.substring(0, pos);
        parsed[1] = keyval.substring(pos+1);

        return parsed;
    }
}
