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
 * FizzBuzz 流水线步骤：分别判定整除 3、整除 5，或汇聚统计 fizz+buzz。
 */
public enum FizzBuzzStep
{
    /** 判定数值能否被 3 整除（fizz）。 */
    FIZZ,
    /** 判定数值能否被 5 整除（buzz）。 */
    BUZZ,
    /** 汇聚 fizz 与 buzz 结果并统计同时满足的次数。 */
    FIZZ_BUZZ,
}
