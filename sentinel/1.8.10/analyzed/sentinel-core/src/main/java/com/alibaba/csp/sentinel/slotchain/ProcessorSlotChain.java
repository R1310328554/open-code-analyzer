/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slotchain;

/**
 * 将所有 {@link ProcessorSlot} 串联成责任链的抽象基类。
 *
 * @author qinan.qn
 */
public abstract class ProcessorSlotChain extends AbstractLinkedProcessorSlot<Object> {

    /**
     * 在槽链头部插入处理器槽。
     *
     * @param protocolProcessor 待插入的处理器槽
     */
    public abstract void addFirst(AbstractLinkedProcessorSlot<?> protocolProcessor);

    /**
     * 在槽链尾部追加处理器槽。
     *
     * @param protocolProcessor 待追加的处理器槽
     */
    public abstract void addLast(AbstractLinkedProcessorSlot<?> protocolProcessor);
}
