/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client.protocol.convertor;

import java.util.ArrayList;
import java.util.List;

/**
 * 将多行字符串回复拆分为 {@link List}{@code <String>}。
 * <p>
 * 按 {@code \r\n} 或 {@code \n} 分行，适用于 INFO、CLUSTER 等块状文本回复。
 *
 * @author Nikita Koksharov
 *
 */
public class StringToListConvertor implements Convertor<List<String>> {

    /** 按换行符拆分字符串，每行作为列表元素。 */
    @Override
    public List<String> convert(Object obj) {
        String value = (String) obj;
        List<String> result = new ArrayList<String>();
        for (String entry : value.split("\r\n|\n")) {
            result.add(entry);
        }
        return result;
    }

}
