/*
 * Copyright 2011 LMAX Ltd.
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
package com.lmax.disruptor.support;

/**
 * 性能测试辅助工具：基准累加与结果断言。
 */
public final class PerfTestUtil
{
    /** 计算 0 至 iterations-1 的累加和，用作对照基准。 */
    public static long accumulatedAddition(final long iterations)
    {
        long temp = 0L;
        for (long i = 0L; i < iterations; i++)
        {
            temp += i;
        }

        return temp;
    }

    /** 若 a 与 b 相等则抛出异常（用于检测意外一致）。 */
    public static void failIf(final long a, final long b)
    {
        if (a == b)
        {
            throw new RuntimeException();
        }
    }

    /** 若 a 与 b 不等则抛出异常（用于验证预期结果）。 */
    public static void failIfNot(final long a, final long b)
    {
        if (a != b)
        {
            throw new RuntimeException();
        }
    }
}
