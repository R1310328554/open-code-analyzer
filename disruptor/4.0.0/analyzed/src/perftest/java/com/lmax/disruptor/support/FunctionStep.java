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
 * 三阶段函数流水线步骤：加法、加常数、位掩码计数。
 */
public enum FunctionStep
{
    /** 第一步：两操作数相加。 */
    ONE,
    /** 第二步：第一步结果加 3。 */
    TWO,
    /** 第三步：对第二步结果做位掩码判定并计数。 */
    THREE
}
