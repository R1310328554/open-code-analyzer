/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common;

import java.io.Serializable;

/**
 * 二元组容器，封装两个关联对象。
 */
public class Pair<T1, T2> implements Serializable {
    /** 第一个元素。 */
    private T1 object1;
    /** 第二个元素。 */
    private T2 object2;

    /** 构造二元组。 */
    public Pair(T1 object1, T2 object2) {
        this.object1 = object1;
        this.object2 = object2;
    }

    /** 静态工厂创建 Pair。 */
    public static <T1, T2> Pair<T1, T2> of(T1 object1, T2 object2) {
        return new Pair<>(object1, object2);
    }

    /** 获取第一个元素。 */
    public T1 getObject1() {
        return object1;
    }

    /** 设置第一个元素。 */
    public void setObject1(T1 object1) {
        this.object1 = object1;
    }

    /** 获取第二个元素。 */
    public T2 getObject2() {
        return object2;
    }

    /** 设置第二个元素。 */
    public void setObject2(T2 object2) {
        this.object2 = object2;
    }
}
