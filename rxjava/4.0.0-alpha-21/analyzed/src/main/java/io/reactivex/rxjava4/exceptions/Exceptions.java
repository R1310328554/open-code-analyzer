/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.exceptions;

import java.util.concurrent.CompletionException;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 帮助传播 checked 异常并重新抛出被标记为致命的异常的工具类。
 */
public final class Exceptions {

    /** 工具类。 */
    private Exceptions() {
        throw new IllegalStateException("No instances!");
    }
    /**
     * 便捷方法：直接抛出 {@code RuntimeException} 与 {@code Error}，
     * 或将其它异常类型包装为 {@link CompletionException}。
     * @param t 要直接抛出或包装的异常
     * @return 由于 {@code propagate} 本身会抛出异常或错误，此为一种“幻影”返回值；
     *         {@code propagate} 实际上不会返回任何值
     */
    @NonNull
    public static RuntimeException propagate(@NonNull Throwable t) {
        /*
         * 返回类型 RuntimeException 是一种技巧，使代码可写成：
         *
         * throw Exceptions.propagate(e);
         *
         * 尽管该 throw 不会真正返回，但代码形式更易读，
         * 便于理解其始终会导致抛出。
         */
        throw ExceptionHelper.wrapOrThrow(t);
    }

    /**
     * 仅当特定 {@code Throwable} 属于一组“致命”错误类型时才抛出。这些类型包括：
     * <ul>
     * <li>{@code VirtualMachineError}</li>
     * <li>{@code ThreadDeath}</li>
     * <li>{@code LinkageError}</li>
     * </ul>
     * 若编写调用用户代码的算子，并希望通过调用订阅者的 {@code onError} 方法通知其中遇到的错误，
     * 但仅当错误尚未严重到此类调用无意义时，本方法很有用；否则应直接重新抛出错误。
     *
     * @param t
     *         要测试并可能抛出的 {@code Throwable}
     * @see <a href="https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495">RxJava: StackOverflowError is swallowed (Issue #748)</a>
     */
    public static void throwIfFatal(@NonNull Throwable t) {
        // values here derived from https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        } else if (t instanceof LinkageError) {
            throw (LinkageError) t;
        }
    }
}
