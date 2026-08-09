/*
 * Copyright 2021 LMAX Ltd.
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
package com.lmax.disruptor;

/** 提供访问默认 {@link ExceptionHandler} 实例的静态方法。 */
public final class ExceptionHandlers
{

    /**
     * 获取默认 {@link ExceptionHandler} 实例的引用。
     *
     * @return 默认 {@link ExceptionHandler} 实例的引用
     */
    public static ExceptionHandler<Object> defaultHandler()
    {
        return DefaultExceptionHandlerHolder.HANDLER;
    }

    private ExceptionHandlers()
    {
    }

    // lazily initialize the default exception handler.
    // This nested object isn't strictly necessary unless additional utility functionality is
    // added to ExceptionHandlers, but it exists to ensure the code remains obvious.
    private static final class DefaultExceptionHandlerHolder
    {
        private static final ExceptionHandler<Object> HANDLER = new FatalExceptionHandler();
    }
}
