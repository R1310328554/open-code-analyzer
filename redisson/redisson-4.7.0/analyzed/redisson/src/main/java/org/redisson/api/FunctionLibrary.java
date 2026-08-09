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
package org.redisson.api;

import java.util.List;

/**
 * 封装 Redis {@code FUNCTION LIST} 返回的函数库元数据。
 * <p>
 * 包含库名、引擎、源码及库内各函数的名称、描述与标志位。
 *
 * @author Nikita Koksharov
 */
public class FunctionLibrary {

    /** 函数标志：禁止写、允许 OOM、允许 stale 读、禁止集群执行等。 */
    public enum Flag {NO_WRITES, ALLOW_OOM, ALLOW_STALE, NO_CLUSTER}

    /** 函数库内单个函数的元数据。 */
    public static class Function {

        private final String name;
        private final String description;
        private final List<Flag> flags;

        /** @param name 函数名
         *  @param description 描述
         *  @param flags 标志位列表 */
        public Function(String name, String description, List<Flag> flags) {
            this.name = name;
            this.description = description;
            this.flags = flags;
        }

        /** @return 函数标志位列表 */
        public List<Flag> getFlags() {
            return flags;
        }

        /** @return 函数描述 */
        public String getDescription() {
            return description;
        }

        /** @return 函数名 */
        /** @return 函数库名称 */
    public String getName() {
            return name;
        }
    }


    private final String name;
    private final String engine;
    private final String code;
    private final List<Function> functions;

    /** @param name 库名
     *  @param engine 引擎（如 {@code lua}）
     *  @param code 库源码
     *  @param functions 库内函数列表 */
    public FunctionLibrary(String name, String engine, String code, List<Function> functions) {
        this.name = name;
        this.engine = engine;
        this.code = code;
        this.functions = functions;
    }

    public String getName() {
        return name;
    }

    /** @return 执行引擎名称 */
    public String getEngine() {
        return engine;
    }

    /** @return 函数库 Lua 源码 */
    public String getCode() {
        return code;
    }

    /** @return 库内函数元数据列表 */
    public List<Function> getFunctions() {
        return functions;
    }
}
