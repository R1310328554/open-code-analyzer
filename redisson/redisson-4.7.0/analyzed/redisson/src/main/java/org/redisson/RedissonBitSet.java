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
package org.redisson;

import org.redisson.api.bitset.BitFieldArgs;
import org.redisson.api.bitset.BitFieldOverflow;
import org.redisson.api.bitset.BitFieldParams;
import org.redisson.api.RBitSet;
import org.redisson.api.RFuture;
import org.redisson.api.bitset.BitOffset;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;

import java.util.*;
import org.redisson.config.ReadMode;

/**
 * 基于 Redis 字符串位操作的 {@link RBitSet} 实现。
 * <p>封装 GETBIT/SETBIT、BITCOUNT、BITOP 及有符号/无符号位域读写。
 *
 * @author Nikita Koksharov
 */
public class RedissonBitSet extends RedissonExpirable implements RBitSet {

    public RedissonBitSet(CommandAsyncExecutor connectionManager, String name) {
        super(null, connectionManager, name);
    }

    /** 获取 Signed。 */
    @Override
    public long getSigned(int size, long offset) {
        return get(getSignedAsync(size, offset));
    }

    /** 设置Signed。 */
    @Override
    public long setSigned(int size, long offset, long value) {
        return get(setSignedAsync(size, offset, value));
    }

    /** 位图 incrementAndGetSigned 操作。 */
    @Override
    public long incrementAndGetSigned(int size, long offset, long increment) {
        return get(incrementAndGetSignedAsync(size, offset, increment));
    }

    /** 获取 Unsigned。 */
    @Override
    public long getUnsigned(int size, long offset) {
        return get(getUnsignedAsync(size, offset));
    }

    /** 设置Unsigned。 */
    @Override
    public long setUnsigned(int size, long offset, long value) {
        return get(setUnsignedAsync(size, offset, value));
    }

    /** 位图 incrementAndGetUnsigned 操作。 */
    @Override
    public long incrementAndGetUnsigned(int size, long offset, long increment) {
        return get(incrementAndGetUnsignedAsync(size, offset, increment));
    }

    /** 位图 bitField 操作。 */
    @Override
    public List<Long> bitField(BitFieldArgs args) {
        return get(bitFieldAsync(args));
    }

    /** 异步获取 Signed 对象或执行 Signed 操作。 */
    @Override
    public RFuture<Long> getSignedAsync(int size, long offset) {
        if (size > 64) {
            throw new IllegalArgumentException("Size can't be greater than 64 bits");
        }
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "GET", "i" + size, offset);
    }

    /** 设置SignedAsync。 */
    @Override
    public RFuture<Long> setSignedAsync(int size, long offset, long value) {
        if (size > 64) {
            throw new IllegalArgumentException("Size can't be greater than 64 bits");
        }
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "SET", "i" + size, offset, value);
    }

    /** 异步执行 incrementAndGetSigned。 */
    @Override
    public RFuture<Long> incrementAndGetSignedAsync(int size, long offset, long increment) {
        if (size > 64) {
            throw new IllegalArgumentException("Size can't be greater than 64 bits");
        }
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "INCRBY", "i" + size, offset, increment);
    }

    /** 异步获取 Unsigned 对象或执行 Unsigned 操作。 */
    @Override
    public RFuture<Long> getUnsignedAsync(int size, long offset) {
        if (size > 63) {
            throw new IllegalArgumentException("Size can't be greater than 63 bits");
        }
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "GET", "u" + size, offset);
    }

    /** 设置UnsignedAsync。 */
    @Override
    public RFuture<Long> setUnsignedAsync(int size, long offset, long value) {
        if (size > 63) {
            throw new IllegalArgumentException("Size can't be greater than 63 bits");
        }
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "SET", "u" + size, offset, value);
    }

    /** 异步执行 incrementAndGetUnsigned。 */
    @Override
    public RFuture<Long> incrementAndGetUnsignedAsync(int size, long offset, long increment) {
        if (size > 63) {
            throw new IllegalArgumentException("Size can't be greater than 63 bits");
        }
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "INCRBY", "u" + size, offset, increment);
    }

    /** 异步执行 bitField。 */
    @Override
    public RFuture<List<Long>> bitFieldAsync(BitFieldArgs args) {
        if (args == null) {
            throw new IllegalArgumentException("Args can't be null");
        }

        if (!(args instanceof BitFieldParams)) {
            throw new IllegalArgumentException("Unsupported BitFieldArgs implementation");
        }

        BitFieldParams params = (BitFieldParams) args;
        if (params.getOperations().isEmpty()) {
            throw new IllegalArgumentException("No bitfield operations defined");
        }

        List<Object> commandArgs = new ArrayList<>();
        commandArgs.add(getRawName());

        boolean isReadOnly = true;
        for (BitFieldParams.Operation operation : params.getOperations()) {
            switch (operation.getType()) {
                case OVERFLOW:
                    BitFieldOverflow overflow = operation.getOverflow();
                    if (overflow == null) {
                        throw new IllegalArgumentException("Overflow can't be null");
                    }
                    commandArgs.add("OVERFLOW");
                    commandArgs.add(overflow.name());
                    break;
                case GET:
                    validateEncoding(operation.getEncoding());
                    validateOffset(operation.getOffset());
                    commandArgs.add("GET");
                    commandArgs.add(operation.getEncoding());
                    commandArgs.add(operation.getOffset().getValue());
                    break;
                case SET:
                    validateEncoding(operation.getEncoding());
                    validateOffset(operation.getOffset());
                    if (operation.getValue() == null) {
                        throw new IllegalArgumentException("Value can't be null");
                    }
                    commandArgs.add("SET");
                    commandArgs.add(operation.getEncoding());
                    commandArgs.add(operation.getOffset().getValue());
                    commandArgs.add(operation.getValue());
                    isReadOnly = false;
                    break;
                case INCRBY:
                    validateEncoding(operation.getEncoding());
                    validateOffset(operation.getOffset());
                    if (operation.getValue() == null) {
                        throw new IllegalArgumentException("Increment can't be null");
                    }
                    commandArgs.add("INCRBY");
                    commandArgs.add(operation.getEncoding());
                    commandArgs.add(operation.getOffset().getValue());
                    commandArgs.add(operation.getValue());
                    isReadOnly = false;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown bitfield operation");
            }
        }

        ReadMode effectiveReadMode = commandExecutor.getReadMode();
        if (effectiveReadMode == null) {
            effectiveReadMode = commandExecutor.getServiceManager().getConfig().getReadMode();
        }
        if (effectiveReadMode == ReadMode.SLAVE && isReadOnly) {
            return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_RO_LONG_LIST,
                    commandArgs.toArray());
        }

        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG_LIST,
                commandArgs.toArray());
    }

    /** 位图 validateEncoding 操作。 */
    private void validateEncoding(String encoding) {
        if (encoding == null || encoding.length() < 2) {
            throw new IllegalArgumentException("Invalid encoding");
        }
        char type = encoding.charAt(0);
        if (type != 'u' && type != 'i') {
            throw new IllegalArgumentException("Invalid encoding");
        }
        int size = Integer.parseInt(encoding.substring(1));
        if (type == 'u') {
            if (size > 63) {
                throw new IllegalArgumentException("Size can't be greater than 63 bits");
            }
        } else {
            if (size > 64) {
                throw new IllegalArgumentException("Size can't be greater than 64 bits");
            }
        }
    }

    /** 位图 validateOffset 操作。 */
    private void validateOffset(BitOffset offset) {
        if (offset == null) {
            throw new IllegalArgumentException("Offset can't be null");
        }
    }

    /** 获取 Byte。 */
    @Override
    public byte getByte(long offset) {
        return get(getByteAsync(offset));
    }

    /** 设置Byte。 */
    @Override
    public byte setByte(long offset, byte value) {
        return get(setByteAsync(offset, value));
    }

    /** 位图 incrementAndGetByte 操作。 */
    @Override
    public byte incrementAndGetByte(long offset, byte increment) {
        return get(incrementAndGetByteAsync(offset, increment));
    }

    /** 获取 Short。 */
    @Override
    public short getShort(long offset) {
        return get(getShortAsync(offset));
    }

    /** 设置Short。 */
    @Override
    public short setShort(long offset, short value) {
        return get(setShortAsync(offset, value));
    }

    /** 位图 incrementAndGetShort 操作。 */
    @Override
    public short incrementAndGetShort(long offset, short increment) {
        return get(incrementAndGetShortAsync(offset, increment));
    }

    /** 获取 Integer。 */
    @Override
    public int getInteger(long offset) {
        return get(getIntegerAsync(offset));
    }

    /** 设置Integer。 */
    @Override
    public int setInteger(long offset, int value) {
        return get(setIntegerAsync(offset, value));
    }

    /** 位图 incrementAndGetInteger 操作。 */
    @Override
    public int incrementAndGetInteger(long offset, int increment) {
        return get(incrementAndGetIntegerAsync(offset, increment));
    }

    /** 获取 Long。 */
    @Override
    public long getLong(long offset) {
        return get(getLongAsync(offset));
    }

    /** 设置Long。 */
    @Override
    public long setLong(long offset, long value) {
        return get(setLongAsync(offset, value));
    }

    /** 位图 incrementAndGetLong 操作。 */
    @Override
    public long incrementAndGetLong(long offset, long increment) {
        return get(incrementAndGetLongAsync(offset, increment));
    }

    /** 异步获取 Byte 对象或执行 Byte 操作。 */
    @Override
    public RFuture<Byte> getByteAsync(long offset) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_BYTE,
                                            getRawName(), "GET", "i8", offset);
    }

    /** 设置ByteAsync。 */
    @Override
    public RFuture<Byte> setByteAsync(long offset, byte value) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_BYTE,
                                            getRawName(), "SET", "i8", offset, value);
    }

    /** 异步执行 incrementAndGetByte。 */
    @Override
    public RFuture<Byte> incrementAndGetByteAsync(long offset, byte increment) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_BYTE,
                                            getRawName(), "INCRBY", "i8", offset, increment);
    }

    /** 异步获取 Short 对象或执行 Short 操作。 */
    @Override
    public RFuture<Short> getShortAsync(long offset) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_SHORT,
                                            getRawName(), "GET", "i16", offset);
    }

    /** 设置ShortAsync。 */
    @Override
    public RFuture<Short> setShortAsync(long offset, short value) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_SHORT,
                                            getRawName(), "SET", "i16", offset, value);
    }

    /** 异步执行 incrementAndGetShort。 */
    @Override
    public RFuture<Short> incrementAndGetShortAsync(long offset, short increment) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_SHORT,
                                            getRawName(), "INCRBY", "i16", offset, increment);
    }

    /** 异步获取 Integer 对象或执行 Integer 操作。 */
    @Override
    public RFuture<Integer> getIntegerAsync(long offset) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_INT,
                                            getRawName(), "GET", "i32", offset);
    }

    /** 设置IntegerAsync。 */
    @Override
    public RFuture<Integer> setIntegerAsync(long offset, int value) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_INT,
                                            getRawName(), "SET", "i32", offset, value);
    }

    /** 异步执行 incrementAndGetInteger。 */
    @Override
    public RFuture<Integer> incrementAndGetIntegerAsync(long offset, int increment) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_INT,
                                            getRawName(), "INCRBY", "i32", offset, increment);
    }

    /** 异步获取 Long 对象或执行 Long 操作。 */
    @Override
    public RFuture<Long> getLongAsync(long offset) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "GET", "i64", offset);
    }

    /** 设置LongAsync。 */
    @Override
    public RFuture<Long> setLongAsync(long offset, long value) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "SET", "i64", offset, value);
    }

    /** 异步执行 incrementAndGetLong。 */
    @Override
    public RFuture<Long> incrementAndGetLongAsync(long offset, long increment) {
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_LONG,
                                            getRawName(), "INCRBY", "i64", offset, increment);
    }

    /** 位图 length 操作。 */
    @Override
    public long length() {
        return get(lengthAsync());
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public void set(BitSet bs) {
        get(setAsync(bs));
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public void set(long[] indexArray, boolean value) {
        get(setAsync(indexArray, value));
    }

    /** 返回底层 Native MapCache 实例。 */
    @Override
    public boolean get(long bitIndex) {
        return get(getAsync(bitIndex));
    }

    /** 异步获取  对象或执行  操作。 */
    @Override
    public RFuture<Boolean> getAsync(long bitIndex) {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.GETBIT, getRawName(), bitIndex);
    }

    /** 返回底层 Native MapCache 实例。 */
    @Override
    public boolean[] get(long... bitIndexes) {
        return get(getAsync(bitIndexes));
    }

    /** 异步获取  对象或执行  操作。 */
    @Override
    public RFuture<boolean[]> getAsync(long... bitIndexes) {
        Object[] indexes = new Object[bitIndexes.length * 3 + 1];
        int j = 0;
        indexes[j++] = getRawName();
        for (long l : bitIndexes) {
            indexes[j++] = "get";
            indexes[j++] = "u1";
            indexes[j++] = l;
        }
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITFIELD_BOOLEANS, indexes);
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public boolean set(long bitIndex) {
        return get(setAsync(bitIndex, true));
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public void set(long fromIndex, long toIndex, boolean value) {
        get(setAsync(fromIndex, toIndex, value));
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public void set(long fromIndex, long toIndex) {
        get(setAsync(fromIndex, toIndex));
    }

    /** 按索引或键写入元素/值。 */
    @Override
    public boolean set(long bitIndex, boolean value) {
        return get(setAsync(bitIndex, value));
    }

    /** 设置Async。 */
    @Override
    public RFuture<Boolean> setAsync(long bitIndex, boolean value) {
        int val = toInt(value);
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.SETBIT, getRawName(), bitIndex, val);
    }

    /** 位图 toInt 操作。 */
    protected int toInt(boolean value) {
        return Boolean.compare(value, false);
    }

    /** 设置Async。 */
    @Override
    public RFuture<Void> setAsync(long[] indexArray, boolean value) {
        int val = toInt(value);
        Object[] paramArray = new Object[indexArray.length * 4 + 1];
        int j = 0;
        paramArray[j++] = getRawName();
        for (long l : indexArray) {
            paramArray[j++] = "set";
            paramArray[j++] = "u1";
            paramArray[j++] = l;
            paramArray[j++] = val;
        }
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.BITFIELD_VOID, paramArray);
    }

    /** 位图 toByteArray 操作。 */
    @Override
    public byte[] toByteArray() {
        return get(toByteArrayAsync());
    }

    /** 异步执行 toByteArray。 */
    @Override
    public RFuture<byte[]> toByteArrayAsync() {
        return commandExecutor.readAsync(getRawName(), ByteArrayCodec.INSTANCE, RedisCommands.GET, getRawName());
    }

    /** 位图 cardinality 操作。 */
    @Override
    public long cardinality() {
        return get(cardinalityAsync());
    }

    /** 返回列表/集合/过滤器当前元素数量。 */
    @Override
    public long size() {
        return get(sizeAsync());
    }

    /** 清空全部元素。 */
    @Override
    public void clear(long fromIndex, long toIndex) {
        get(clearAsync(fromIndex, toIndex));
    }

    /** 清空全部元素。 */
    @Override
    public boolean clear(long bitIndex) {
        return get(clearAsync(bitIndex));
    }

    /** 清空全部元素。 */
    @Override
    public void clear() {
        get(clearAsync());
    }

    /** 位图 or 操作。 */
    @Override
    public long or(String... bitSetNames) {
        return get(orAsync(bitSetNames));
    }

    /** 位图 and 操作。 */
    @Override
    public long and(String... bitSetNames) {
        return get(andAsync(bitSetNames));
    }

    /** 位图 xor 操作。 */
    @Override
    public long xor(String... bitSetNames) {
        return get(xorAsync(bitSetNames));
    }

    /** 位图 not 操作。 */
    @Override
    public long not() {
        return get(notAsync());
    }

    /** 异步执行 op。 */
    private RFuture<Long> opAsync(String op, String... bitSetNames) {
        List<Object> params = new ArrayList<>(bitSetNames.length + 3);
        params.add(op);
        params.add(getRawName());
        params.add(getRawName());
        params.addAll(Arrays.asList(bitSetNames));
        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITOP, params.toArray());
    }

    /** 位图 asBitSet 操作。 */
    @Override
    public BitSet asBitSet() {
        return fromByteArrayReverse(toByteArray());
    }

    //Copied from: https://github.com/xetorthio/jedis/issues/301
    private static BitSet fromByteArrayReverse(byte[] bytes) {
        if (bytes == null) {
            return new BitSet();
        }

        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    //Copied from: https://github.com/xetorthio/jedis/issues/301
    private static byte[] toByteArrayReverse(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                final int value = bytes[i / 8] | (1 << (7 - (i % 8)));
                bytes[i / 8] = (byte) value;
            }
        }
        return bytes;
    }

    /** 位图 toString 操作。 */
    @Override
    public String toString() {
        return asBitSet().toString();
    }

    /** 异步执行 length。 */
    @Override
    public RFuture<Long> lengthAsync() {
        return commandExecutor.evalReadAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.EVAL_LONG,
                "local i = redis.call('bitpos', KEYS[1], 1, -1); "
                        + "local pos = i < 0 and redis.call('bitpos', KEYS[1], 0, -1) or math.floor(i / 8) * 8; "
                        + "while  (pos >= 0) "
                        + "do "
                            + "i = redis.call('bitpos', KEYS[1], 1, math.floor(pos / 8), math.floor(pos / 8)); "
                            + "if i < 0 then "
                                + "pos = pos - 8; "
                            + "else "
                                + "for j = pos + 7, pos, -1 do "
                                    + "if redis.call('getbit', KEYS[1], j) == 1 then "
                                        + "return j + 1; "
                                    + "end; "
                                + "end; "
                            + "end; "
                        + "end; "
                        + "return 0; ",
                Collections.<Object>singletonList(getRawName()));
    }

    /** 设置Async。 */
    @Override
    public RFuture<Void> setAsync(long fromIndex, long toIndex, boolean value) {
        int val = toInt(value);
        CommandBatchService executorService = new CommandBatchService(commandExecutor);
        for (long i = fromIndex; i < toIndex; i++) {
            executorService.writeAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.SETBIT_VOID, getRawName(), i, val);
        }
        return executorService.executeAsyncVoid();
    }

    /** 异步执行 clear。 */
    @Override
    public RFuture<Void> clearAsync(long fromIndex, long toIndex) {
        return setAsync(fromIndex, toIndex, false);
    }

    /** 设置Async。 */
    @Override
    public RFuture<Void> setAsync(BitSet bs) {
        return commandExecutor.writeAsync(getRawName(), ByteArrayCodec.INSTANCE, RedisCommands.SET, getRawName(), toByteArrayReverse(bs));
    }

    /** 异步执行 not。 */
    @Override
    public RFuture<Long> notAsync() {
        return opAsync("NOT");
    }

    /** 设置Async。 */
    @Override
    public RFuture<Void> setAsync(long fromIndex, long toIndex) {
        return setAsync(fromIndex, toIndex, true);
    }

    /** 异步返回元素数量。 */
    @Override
    public RFuture<Long> sizeAsync() {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITS_SIZE, getRawName());
    }

    /** 设置Async。 */
    @Override
    public RFuture<Boolean> setAsync(long bitIndex) {
        return setAsync(bitIndex, true);
    }

    /** 异步执行 cardinality。 */
    @Override
    public RFuture<Long> cardinalityAsync() {
        return commandExecutor.readAsync(getRawName(), LongCodec.INSTANCE, RedisCommands.BITCOUNT, getRawName());
    }

    /** 异步执行 clear。 */
    @Override
    public RFuture<Boolean> clearAsync(long bitIndex) {
        return setAsync(bitIndex, false);
    }

    /** 异步执行 clear。 */
    @Override
    public RFuture<Void> clearAsync() {
        return commandExecutor.writeAsync(getRawName(), RedisCommands.DEL_VOID, getRawName());
    }

    /** 异步执行 or。 */
    @Override
    public RFuture<Long> orAsync(String... bitSetNames) {
        return opAsync("OR", bitSetNames);
    }

    /** 异步执行 and。 */
    @Override
    public RFuture<Long> andAsync(String... bitSetNames) {
        return opAsync("AND", bitSetNames);
    }

    /** 异步执行 xor。 */
    @Override
    public RFuture<Long> xorAsync(String... bitSetNames) {
        return opAsync("XOR", bitSetNames);
    }

    /** 位图 diff 操作。 */
    @Override
    public long diff(String... bitSetNames) {
        return get(diffAsync(bitSetNames));
    }

    /** 位图 diffInverse 操作。 */
    @Override
    public long diffInverse(String... bitSetNames) {
        return get(diffInverseAsync(bitSetNames));
    }

    /** 位图 andOr 操作。 */
    @Override
    public long andOr(String... bitSetNames) {
        return get(andOrAsync(bitSetNames));
    }

    /** 设置Exclusive。 */
    @Override
    public long setExclusive(String... bitSetNames) {
        return get(setExclusiveAsync(bitSetNames));
    }

    /** 异步执行 diff。 */
    @Override
    public RFuture<Long> diffAsync(String... bitSetNames) {
        return opAsync("DIFF", bitSetNames);
    }

    /** 异步执行 diffInverse。 */
    @Override
    public RFuture<Long> diffInverseAsync(String... bitSetNames) {
        return opAsync("DIFF1", bitSetNames);
    }

    /** 异步执行 andOr。 */
    @Override
    public RFuture<Long> andOrAsync(String... bitSetNames) {
        return opAsync("ANDOR", bitSetNames);
    }

    /** 设置ExclusiveAsync。 */
    @Override
    public RFuture<Long> setExclusiveAsync(String... bitSetNames) {
        return opAsync("ONE", bitSetNames);
    }
}
