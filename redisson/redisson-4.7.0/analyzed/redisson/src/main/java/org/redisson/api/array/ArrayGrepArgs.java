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
package org.redisson.api.array;

/**
 * 数组 grep 搜索操作的参数对象。
 *
 * @author lamnt2008
 *
 */
public interface ArrayGrepArgs {

    /**
     * 定义精确匹配谓词。
     *
     * @param value 匹配值
     * @return 参数对象
     */
    static ArrayGrepArgs exact(Object value) {
        return new ArrayGrepParams().withExact(value);
    }

    /**
     * 定义子串匹配谓词。
     *
     * @param value 匹配值
     * @return 参数对象
     */
    static ArrayGrepArgs match(Object value) {
        return new ArrayGrepParams().withMatch(value);
    }

    /**
     * 定义 glob 通配符匹配谓词。
     *
     * @param pattern 匹配模式
     * @return 参数对象
     */
    static ArrayGrepArgs glob(String pattern) {
        return new ArrayGrepParams().withGlob(pattern);
    }

    /**
     * 定义正则表达式匹配谓词。
     *
     * @param pattern 匹配模式
     * @return 参数对象
     */
    static ArrayGrepArgs regex(String pattern) {
        return new ArrayGrepParams().withRegex(pattern);
    }

    /**
     * 定义精确匹配谓词。
     *
     * @param value 匹配值
     * @return 参数对象
     */
    ArrayGrepArgs withExact(Object value);

    /**
     * 定义子串匹配谓词。
     *
     * @param value 匹配值
     * @return 参数对象
     */
    ArrayGrepArgs withMatch(Object value);

    /**
     * 定义 glob 通配符匹配谓词。
     *
     * @param pattern 匹配模式
     * @return 参数对象
     */
    ArrayGrepArgs withGlob(String pattern);

    /**
     * 定义正则表达式匹配谓词。
     *
     * @param pattern 匹配模式
     * @return 参数对象
     */
    ArrayGrepArgs withRegex(String pattern);

    /**
     * 定义以 AND 逻辑组合的谓词。
     *
     * @return 参数对象
     */
    ArrayGrepArgs and();

    /**
     * 定义以 OR 逻辑组合的谓词。
     *
     * @return 参数对象
     */
    ArrayGrepArgs or();

    /**
     * 定义匹配结果数量上限。
     *
     * @param value 上限值
     * @return 参数对象
     */
    ArrayGrepArgs limit(long value);

    /**
     * 定义不区分大小写的匹配。
     *
     * @return 参数对象
     */
    ArrayGrepArgs noCase();

}
