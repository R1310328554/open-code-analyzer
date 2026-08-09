/*  Copyright 2016 Gil Tene
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

package com.lmax.disruptor.util;

/**
 * 封装部分运行时可用于提升性能的线程提示 that may be used by some
 * runtimes to improve code performance. It is intended to capture hinting
 * behaviours that are implemented in or anticipated to be spec'ed under the
 * {@link java.lang.Thread} class in some Java SE versions, but missing in prior
 * versions.
 * @deprecated 请直接使用 Thread.onSpinWait()。 This class previously existed to accommodate
 * Java versions which do not have Thread.onSpinWait().
 */
@Deprecated
public final class ThreadHints
{

    private ThreadHints()
    {
    }

    /**
     * 提示调用方暂时无法继续执行, until the
     * occurrence of one or more actions on the part of other activities.  By
     * invoking this method within each iteration of a spin-wait loop construct,
     * the calling thread indicates to the runtime that it is busy-waiting. The runtime
     * may take action to improve the performance of invoking spin-wait loop constructions.
     *
     * @deprecated 请直接使用 Thread.onSpinWait()。 This method previously existed to accommodate
     * Java versions which do not have Thread.onSpinWait().
     */
    @Deprecated
    public static void onSpinWait()
    {
        Thread.onSpinWait();
    }
}