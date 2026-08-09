/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.codec.dns;

import io.netty.util.ReferenceCounted;

/**
 * DNS 报文公共接口，{@link DnsQuery} 与 {@link DnsResponse} 均继承此接口。
 * <p>
 * 定义 ID、opCode、RD/Z 标志及各 section 记录的读写操作。
 */
public interface DnsMessage extends ReferenceCounted {

    /** 返回报文标识符 {@code ID}。 */
    int id();

    /** 设置报文标识符 {@code ID}。 */
    DnsMessage setId(int id);

    /** 返回操作码 {@code opCode}。 */
    DnsOpCode opCode();

    /** 设置操作码 {@code opCode}。 */
    DnsMessage setOpCode(DnsOpCode opCode);

    /** 返回期望递归（{@code RD}）标志。 */
    boolean isRecursionDesired();

    /** 设置期望递归（{@code RD}）标志。 */
    DnsMessage setRecursionDesired(boolean recursionDesired);

    /** 返回保留字段 {@code Z}（3 位，供将来扩展）。 */
    int z();

    /** 设置保留字段 {@code Z}。 */
    DnsMessage setZ(int z);

    /** 返回指定 {@code section} 中的记录条数。 */
    int count(DnsSection section);

    /** 返回报文中全部 section 的记录总数。 */
    int count();

    /**
     * 返回指定 {@code section} 的首条记录。
     * {@code section} 为 {@link DnsSection#QUESTION} 时返回类型恒为 {@link DnsQuestion}。
     *
     * @return 该 section 无记录时返回 {@code null}
     */
    <T extends DnsRecord> T recordAt(DnsSection section);

    /**
     * 返回指定 {@code section} 中第 {@code index} 条记录。
     * {@code section} 为 {@link DnsSection#QUESTION} 时返回类型恒为 {@link DnsQuestion}。
     *
     * @throws IndexOutOfBoundsException if the specified {@code index} is out of bounds
     */
    <T extends DnsRecord> T recordAt(DnsSection section, int index);

    /**
     * 将指定 {@code section} 设为仅含一条 {@code record}。
     * {@code section} 为 {@link DnsSection#QUESTION} 时 {@code record} 须为 {@link DnsQuestion}。
     */
    DnsMessage setRecord(DnsSection section, DnsRecord record);

    /**
     * 在指定 {@code section} 的 {@code index} 处设置 {@code record}，返回被替换的旧记录。
     * {@code section} 为 {@link DnsSection#QUESTION} 时 {@code record} 须为 {@link DnsQuestion}。
     *
     * @return the old record
     * @throws IndexOutOfBoundsException if the specified {@code index} is out of bounds
     */
    <T extends DnsRecord> T setRecord(DnsSection section, int index, DnsRecord record);

    /** 在指定 {@code section} 末尾追加 {@code record}。 */
    DnsMessage addRecord(DnsSection section, DnsRecord record);

    /** 在指定 {@code section} 的 {@code index} 处插入 {@code record}。 */
    DnsMessage addRecord(DnsSection section, int index, DnsRecord record);

    /** 移除指定 {@code section} 中第 {@code index} 条记录并返回。 */
    <T extends DnsRecord> T removeRecord(DnsSection section, int index);

    /** 清空指定 {@code section} 的全部记录。 */
    DnsMessage clear(DnsSection section);

    /** 清空报文全部 section 的记录。 */
    DnsMessage clear();

    @Override
    DnsMessage touch();

    @Override
    DnsMessage touch(Object hint);

    @Override
    DnsMessage retain();

    @Override
    DnsMessage retain(int increment);
}
