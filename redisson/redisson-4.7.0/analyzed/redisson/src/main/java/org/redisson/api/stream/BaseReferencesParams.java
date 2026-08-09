/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.stream;

/**
 * 流引用参数配置的抽象基类。
 * <p>
 * 提供删除引用、保留引用及仅删除已确认消息等策略的链式配置方法。
 *
 * @author seakider
 *
 */
public abstract class BaseReferencesParams<T> implements StreamReferencesArgs<T> {
    /** 当前选择的引用策略。 */
    private RefPolicy refPolicy;

    @Override
    public T removeReferences() {
        this.refPolicy = RefPolicy.DELREF;
        return (T) this;
    }

    @Override
    public T keepReferences() {
        this.refPolicy = RefPolicy.KEEPREF;
        return (T) this;
    }

    @Override
    public T removeAcknowledgedOnly() {
        this.refPolicy = RefPolicy.ACKED;
        return (T) this;
    }

    /** 返回当前配置的引用策略。 */
    public RefPolicy getRefPolicy() {
        return refPolicy;
    }

}
