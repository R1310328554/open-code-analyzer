/*
 * Copyright 2023 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lmax.disruptor;

/**
 * 批次回卷时决定下一处理序号的回调接口。
 */
public interface RewindHandler
{
    /**
     * 在捕获 {@link RewindableException} 后尝试回卷，并返回下一应处理的序号。
     *
     * @param e 可回卷异常
     * @param startOfBatchSequence 当前批次起始序号
     * @return 回卷后应继续处理的序号
     * @throws RewindableException 若仍无法继续处理可再次抛出
     */
    long attemptRewindGetNextSequence(RewindableException e, long startOfBatchSequence) throws RewindableException;
}
