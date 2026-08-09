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

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 封装 {@code FUNCTION STATS} 返回的运行中函数与各引擎统计信息。
 *
 * @author Nikita Koksharov
 */
public class FunctionStats {

    /** 单个函数引擎（如 {@code lua}）的库与函数计数。 */
    public static class Engine {

        private final Long libraries;
        private final Long functions;

        /** @param libraries 已加载库数量
         *  @param functions 已注册函数数量 */
        public Engine(Long libraries, Long functions) {
            this.libraries = libraries;
            this.functions = functions;
        }

        /** @return 已加载函数库数量 */

        public Long getLibraries() {
            return libraries;
        }

        /** @return 已注册函数数量 */

        public Long getFunctions() {
            return functions;
        }
    }

    /** 当前正在执行的函数及其调用参数与运行时长。 */
    public static class RunningFunction {

        private final String name;
        private final List<Object> command;
        private final Duration duration;

        /** @param name 函数名
         *  @param command FCALL 命令参数列表
         *  @param duration 已运行时长 */
        public RunningFunction(String name, List<Object> command, Duration duration) {
            this.name = name;
            this.command = command;
            this.duration = duration;
        }

        /** @return 正在执行的函数名 */

        public String getName() {
            return name;
        }

        /** @return FCALL 命令参数列表 */

        public List<Object> getCommand() {
            return command;
        }

        /** @return 函数已运行时长 */

        public Duration getDuration() {
            return duration;
        }
    }

    private final RunningFunction runningFunction;
    private final Map<String, Engine> engines;

    /** @param runningFunction 当前运行中的函数；无则为 {@code null}
     *  @param engines 按引擎名索引的统计信息 */
    public FunctionStats(RunningFunction runningFunction, Map<String, Engine> engines) {
        this.runningFunction = runningFunction;
        this.engines = engines;
    }

    /** @return 当前运行中的函数；无则 {@code null} */

    public RunningFunction getRunningFunction() {
        return runningFunction;
    }

    /** @return 按引擎名映射的 {@link Engine} 统计信息 */

    public Map<String, Engine> getEngines() {
        return engines;
    }
}
