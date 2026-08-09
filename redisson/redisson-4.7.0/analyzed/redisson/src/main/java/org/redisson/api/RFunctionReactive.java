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

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Redis Function Reactor API。
 * <p>各方法返回 {@link Mono}。
 *
 * @author Nikita Koksharov
 */
public interface RFunctionReactive {

    /**
     * 删除函数库；库不存在时抛出错误。
     *
     * @param libraryName 函数库名称
     */
    Mono<Void> delete(String libraryName);

    /**
     * 返回所有函数库的序列化状态。
     *
     * @return 序列化状态
     */
    Mono<byte[]> dump();

    /**
     * 删除所有已加载的函数库。
     *
     */
    Mono<Void> flush();

    /**
     * 终止当前正在执行的函数（仅适用于不修改数据的函数）。
     * Applied only to functions which don't modify data.
     *
     */
    Mono<Void> kill();

    /**
     * 返回各函数库及其包含函数的信息列表。
     *
     * @return 函数库列表
     */
    Mono<List<FunctionLibrary>> list();

    /**
     * 按名称模式（glob）返回匹配的函数库及函数信息。
     * <p>
     *  Supported glob-style patterns:
     *    h?llo matches hello, hallo and hxllo
     *    h*llo matches hllo and heeeello
     *    h[ae]llo matches hello and hallo, but not hillo
     *
     * @param namePattern 名称匹配模式
     * @return 函数库列表
     */
    Mono<List<FunctionLibrary>> list(String namePattern);

    /**
     * 加载函数库；库已存在时抛出错误。
     *
     * @param libraryName 函数库名称
     * @param code 函数库代码
     */
    Mono<Void> load(String libraryName, String code);

    /**
     * 加载函数库并覆盖同名已有库。
     *
     * @param libraryName 函数库名称
     * @param code 函数库代码
     */
    Mono<Void> loadAndReplace(String libraryName, String code);

    /**
     * 使用 {@link #dump()} 返回的状态恢复函数库并追加到现有库（冲突时报错）。
     * Restored libraries are appended to the existing libraries and throws error in case of collision.
     *
     * @param payload 序列化状态
     */
    Mono<Void> restore(byte[] payload);

    /**
     * 使用 {@link #dump()} 返回的状态恢复函数库并追加到现有库（冲突时报错）。
     * Restored libraries are appended to the existing libraries.
     *
     * @param payload 序列化状态
     */
    Mono<Void> restoreAndReplace(byte[] payload);

    /**
     * 使用 {@link #dump()} 返回的状态恢复函数库并追加到现有库（冲突时报错）。
     * Deletes all existing libraries before restoring.
     *
     * @param payload 序列化状态
     */
    Mono<Void> restoreAfterFlush(byte[] payload);

    /**
     * 返回当前正在运行的 Redis Function 及可用执行引擎信息。
     * Redis function and available execution engines.
     *
     * @return 函数运行信息
     */
    Mono<FunctionStats> stats();

    /**
     * 执行 Redis Function。
     *
     * @param <R>        - type of result
     * @param key 路由键（Cluster 定位节点）
     * @param mode 执行模式
     * @param name 函数名称
     * @param returnType 返回值类型
     * @param keys 脚本 KEYS 参数
     * @param values 脚本 ARGV 参数
     * @return 执行结果
     */
    <R> Mono<R> call(String key, FunctionMode mode, String name, FunctionResult returnType, List<Object> keys, Object... values);

    /**
     * 执行 Redis Function。
     *
     * @param <R>        - type of result
     * @param mode 执行模式
     * @param name 函数名称
     * @param returnType 返回值类型
     * @param keys 脚本 KEYS 参数
     * @param values 脚本 ARGV 参数
     * @return 执行结果
     */
    <R> Mono<R> call(FunctionMode mode, String name, FunctionResult returnType, List<Object> keys, Object... values);

    /**
     * 执行 Redis Function。
     *
     * @param <R>        - type of result
     * @param mode 执行模式
     * @param name 函数名称
     * @param returnType 返回值类型
     * @return 执行结果
     */
    <R> Mono<R> call(FunctionMode mode, String name, FunctionResult returnType);

}
