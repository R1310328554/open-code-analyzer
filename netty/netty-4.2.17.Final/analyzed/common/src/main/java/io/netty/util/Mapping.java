/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util;

/**
 * Maintains the mapping from the objects of one type to the objects of the other type.
 * <p>维护从一种类型对象到另一种类型对象的映射关系。</p>
 */
public interface Mapping<IN, OUT> {

    /**
     * Returns mapped value of the specified input.
     * <p>根据输入查找并返回对应的映射值。</p>
     */
    OUT map(IN input);
}
