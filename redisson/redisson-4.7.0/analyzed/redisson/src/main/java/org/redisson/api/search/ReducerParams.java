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
package org.redisson.api.search;

import java.util.Arrays;
import java.util.List;

/**
 * {@link Reducer} 的归约器参数实现。
 * <p>
 * 持有归约函数名、参数列表及可选的结果别名。
 *
 * @author Nikita Koksharov
 *
 */
public final class ReducerParams implements Reducer {

    private String as;
    private String functionName;
    private List<String> args;

    ReducerParams(String functionName, String... args) {
        this.functionName = functionName;
        this.args = Arrays.asList(args);
    }

    @Override
    public Reducer as(String alias) {
        this.as = alias;
        return this;
    }

    /**
     * 返回归约结果的别名。
     *
     * @return 别名字符串，未设置时返回 null
     */
    public String getAs() {
        return as;
    }

    /**
     * 返回归约函数名（如 AVG、SUM、COUNT）。
     *
     * @return 函数名
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 返回归约函数的参数列表。
     *
     * @return 参数列表
     */
    public List<String> getArgs() {
        return args;
    }
}
