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

import org.redisson.api.RFuture;
import org.redisson.api.RMaps;
import org.redisson.api.RMapsImport;
import org.redisson.api.map.MapsImportArgs;
import org.redisson.api.map.MapsImportParams;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.misc.HashValue;
import org.redisson.misc.CompletableFutureWrapper;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * {@link org.redisson.api.RMaps} 的多 Map 批量写入门面。
 * <p>按字段集合分组后通过 {@link RedissonMapsImport} 批量导入，
 * 优先使用 Redis {@code HIMPORT}，不支持时回退 {@code HSET}。
 *
 * @author Nikita Koksharov
 * @param <K> Hash 字段类型
 * @param <V> Hash 值类型
 */
public class RedissonMaps<K, V> implements RMaps<K, V> {

    /** 未指定时的默认批量 flush 大小。 */
    private static final int DEFAULT_BATCH_SIZE = 500;

    private final Codec codec;
    private final CommandAsyncExecutor commandExecutor;

    /** 使用全局默认 codec 构造。 */
    public RedissonMaps(CommandAsyncExecutor commandExecutor) {
        this(commandExecutor.getServiceManager().getCfg().getCodec(), commandExecutor);
    }

    /** @param codec 字段与值的编解码器 */
    public RedissonMaps(Codec codec, CommandAsyncExecutor commandExecutor) {
        this.codec = commandExecutor.getServiceManager().getCodec(codec);
        this.commandExecutor = commandExecutor;
    }

    @Override
    public void set(Map<String, Map<K, V>> maps) {
        commandExecutor.get(setAsync(maps));
    }

    @Override
    public void set(Map<String, Map<K, V>> maps, int batchSize) {
        commandExecutor.get(setAsync(maps, batchSize));
    }

    @Override
    public RFuture<Void> setAsync(Map<String, Map<K, V>> maps) {
        return setAsync(maps, DEFAULT_BATCH_SIZE);
    }

    @Override
    /** 按字段签名分组后分批写入多个 Redis Hash。 */
    public RFuture<Void> setAsync(Map<String, Map<K, V>> maps, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize should be greater than zero");
        }
        if (maps.isEmpty()) {
            return CompletableFutureWrapper.completedNull();
        }

        CompletionStage<Void> f = writeGroups(groupByFields(maps).values().iterator(), batchSize);
        return new CompletableFutureWrapper<>(f);
    }

    @Override
    /** 根据 {@link MapsImportParams} 创建 {@link RedissonMapsImport} 实例。 */
    public RMapsImport<K, V> createImport(MapsImportArgs<K> args) {
        MapsImportParams<K> params = (MapsImportParams<K>) args;

        List<byte[]> fields = new ArrayList<>(params.getFields().size());
        for (K field : params.getFields()) {
            fields.add(RedissonMapsImport.encodeMapKey(commandExecutor, codec, field));
        }

        return new RedissonMapsImport<>(commandExecutor, codec, fields, params.getBatchSize());
    }

    private CompletionStage<Void> writeGroups(Iterator<Group> groups, int batchSize) {
        if (!groups.hasNext()) {
            return CompletableFuture.completedFuture(null);
        }

        Group group = groups.next();
        RedissonMapsImport<K, V> mapsImport = new RedissonMapsImport<>(commandExecutor, codec,
                                                                        group.fields, batchSize);
        for (Map.Entry<String, List<byte[]>> object : group.objects.entrySet()) {
            mapsImport.addEncoded(object.getKey(), object.getValue());
        }
        return mapsImport.flushAsync().thenCompose(r -> writeGroups(groups, batchSize));
    }

    /** 将具有相同字段集合（及顺序）的 Map 归为一组以便 HIMPORT。 */
    private Map<HashValue, Group> groupByFields(Map<String, Map<K, V>> maps) {
        Map<HashValue, Group> groups = new LinkedHashMap<>();
        for (Map.Entry<String, Map<K, V>> entry : maps.entrySet()) {
            List<Map.Entry<byte[], byte[]>> sortedEntries = encodeSortedByField(entry.getKey(), entry.getValue());

            List<byte[]> fields = new ArrayList<>(sortedEntries.size());
            List<byte[]> values = new ArrayList<>(sortedEntries.size());
            for (Map.Entry<byte[], byte[]> sortedEntry : sortedEntries) {
                fields.add(sortedEntry.getKey());
                values.add(sortedEntry.getValue());
            }

            groups.computeIfAbsent(RedissonMapsImport.fieldsHash(fields), k -> new Group(fields))
                    .add(entry.getKey(), values);
        }
        return groups;
    }

    private List<Map.Entry<byte[], byte[]>> encodeSortedByField(String name, Map<K, V> map) {
        if (map.isEmpty()) {
            throw new IllegalArgumentException("Map object " + name + " can't be empty");
        }

        List<Map.Entry<byte[], byte[]>> entries = new ArrayList<>(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            entries.add(new AbstractMap.SimpleEntry<>(
                    RedissonMapsImport.encodeMapKey(commandExecutor, codec, entry.getKey()),
                    RedissonMapsImport.encodeMapValue(commandExecutor, codec, entry.getValue())));
        }
        entries.sort((left, right) -> compare(left.getKey(), right.getKey()));
        return entries;
    }

    private static int compare(byte[] left, byte[] right) {
        int length = Math.min(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int diff = Byte.compare(left[i], right[i]);
            if (diff != 0) {
                return diff;
            }
        }
        return Integer.compare(left.length, right.length);
    }

    private static final class Group {

        private final List<byte[]> fields;
        private final Map<String, List<byte[]>> objects = new LinkedHashMap<>();

        Group(List<byte[]> fields) {
            this.fields = fields;
        }

        void add(String name, List<byte[]> values) {
            objects.put(name, values);
        }

    }

}
