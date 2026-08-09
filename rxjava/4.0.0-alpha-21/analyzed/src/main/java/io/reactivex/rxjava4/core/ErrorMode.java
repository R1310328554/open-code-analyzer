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

package io.reactivex.rxjava4.core;

/**
 * 指示应何时处理任一参与源产生的错误。
 * <p>
 * 通常与 {@code concat}、{@code concatMap} 算子配合使用：外层与内层源可能在流式传输中途出错，
 * 用户希望在取消其余源并向消费者发出错误信号之前，先完成当前源。
 * @since 4.0.0
 */
public enum ErrorMode {
    /** 立即报告错误并取消活跃源。 */
    IMMEDIATE,
    /** 在内层源终止后报告错误。 */
    BOUNDARY,
    /** 在所有源终止后报告错误。 */
    END
}
