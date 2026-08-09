/*
 * Copyright 2012 LMAX Ltd.
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
package com.lmax.disruptor.dsl;

/**
 * 定义生产者类型，用于创建配备正确 {@link com.lmax.disruptor.Sequencer} 与发布语义的 {@link com.lmax.disruptor.RingBuffer}。
 */
public enum ProducerType
{
    /**
     * 单生产者：环形缓冲区仅允许一个线程发布事件。
     */
    SINGLE,

    /**
     * 多生产者：环形缓冲区允许多个线程并发发布事件。
     */
    MULTI
}
