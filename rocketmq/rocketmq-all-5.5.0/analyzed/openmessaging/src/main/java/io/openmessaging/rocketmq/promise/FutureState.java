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

package io.openmessaging.rocketmq.promise;

/**
 * Promise 生命周期状态枚举。
 */
public enum FutureState {
    /** 任务进行中。 */
    DOING(0),
    /** 任务已完成。 */
    DONE(1),
    /** 任务已取消或超时。 */
    CANCELLED(2);

    /** 状态整型编码。 */
    public final int value;

    private FutureState(int value) {
        this.value = value;
    }

    /** 是否为取消状态。 */
    public boolean isCancelledState() {
        return this == CANCELLED;
    }

    /** 是否为完成状态。 */
    public boolean isDoneState() {
        return this == DONE;
    }

    /** 是否为进行中状态。 */
    public boolean isDoingState() {
        return this == DOING;
    }
}
