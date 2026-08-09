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
 * 数值变异运算：供 ValueMutation 系列处理器对累加器施加二元操作。
 */
public enum Operation
{
    /** 加法：lhs + rhs。 */
    ADDITION
        {
            @Override
            public long op(final long lhs, final long rhs)
            {
                return lhs + rhs;
            }
        },

    /** 减法：lhs - rhs。 */
    SUBTRACTION
        {
            @Override
            public long op(final long lhs, final long rhs)
            {
                return lhs - rhs;
            }
        },

    /** 按位与：lhs & rhs。 */
    AND
        {
            @Override
            public long op(final long lhs, final long rhs)
            {
                return lhs & rhs;
            }
        };

    /** 对左值与右值执行本枚举所代表的运算。 */
    public abstract long op(long lhs, long rhs);
}
