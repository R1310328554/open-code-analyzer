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
package org.redisson.spring.data.connection;

import org.reactivestreams.Publisher;
import org.redisson.api.stream.*;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.decoder.*;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveStreamCommands;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Data Redis 响应式 Stream 命令实现。
 * <p>封装 XADD/XACK/XDEL、XRANGE/XREVRANGE、XREAD/XREADGROUP、
XGROUP 消费者组管理及 XTRIM 等 Redis Stream 操作。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveStreamCommands extends RedissonBaseReactive implements ReactiveStreamCommands {

    /** 注入响应式命令执行器。 */
    RedissonReactiveStreamCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    /** 将 {@link RecordId} 列表转为 Redis 命令所需的字符串 ID 列表。 */
    /** 将 RecordId 列表转为 Redis 命令参数字符串列表。 */
    private static List<String> toStringList(List<RecordId> recordIds) {
        return recordIds.stream().map(RecordId::getValue).collect(Collectors.toList());
    }

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<XClaimCommand, Flux<RecordId>>> xClaimJustId(Publisher<XClaimCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroupName(), "Group name must not be null!");
            Assert.notNull(command.getNewOwner(), "NewOwner must not be null!");
            Assert.notEmpty(command.getOptions().getIds(), "Ids collection must not be empty!");

            List<Object> params = new ArrayList<>();
            byte[] k = toByteArray(command.getKey());
            params.add(k);
            params.add(command.getGroupName());
            params.add(command.getNewOwner());
            params.add(Objects.requireNonNull(command.getOptions().getMinIdleTime()).toMillis());
            params.addAll(Arrays.asList(command.getOptions().getIdsAsStringArray()));
            params.add("JUSTID");

            Mono<Map<StreamMessageId, Map<byte[], byte[]>>> m = write(k, ByteArrayCodec.INSTANCE, RedisCommands.XCLAIM, params.toArray());
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, Flux.fromStream(v.entrySet().stream()).map(e -> {
                return RecordId.of(e.getKey().toString());
            })));
        });
    }

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<XClaimCommand, Flux<ByteBufferRecord>>> xClaim(Publisher<XClaimCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroupName(), "Group name must not be null!");
            Assert.notNull(command.getNewOwner(), "NewOwner must not be null!");
            Assert.notEmpty(command.getOptions().getIds(), "Ids collection must not be empty!");

            List<Object> params = new ArrayList<>();
            byte[] k = toByteArray(command.getKey());
            params.add(k);
            params.add(command.getGroupName());
            params.add(command.getNewOwner());
            params.add(Objects.requireNonNull(command.getOptions().getMinIdleTime()).toMillis());
            params.addAll(Arrays.asList(command.getOptions().getIdsAsStringArray()));

            Mono<Map<StreamMessageId, Map<byte[], byte[]>>> m = write(k, ByteArrayCodec.INSTANCE, RedisCommands.XCLAIM, params.toArray());
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, Flux.fromStream(v.entrySet().stream()).map(e -> {
                Map<ByteBuffer, ByteBuffer> map = e.getValue().entrySet().stream()
                        .collect(Collectors.toMap(entry -> ByteBuffer.wrap(entry.getKey()),
                                entry -> ByteBuffer.wrap(entry.getValue())));
                return StreamRecords.newRecord()
                        .in(command.getKey())
                        .withId(RecordId.of(e.getKey().toString()))
                        .ofBuffer(map);
            })));
        });
    }

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<PendingRecordsCommand, PendingMessagesSummary>> xPendingSummary(Publisher<PendingRecordsCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroupName(), "Group name must not be null!");

            byte[] k = toByteArray(command.getKey());

            Mono<PendingResult> m = write(k, StringCodec.INSTANCE, RedisCommands.XPENDING, k, command.getGroupName());
            return m.map(v -> {
                PendingMessagesSummary s;
                if (v.getTotal() == 0) {
                    s = new PendingMessagesSummary(command.getGroupName(), 0, Range.unbounded(), v.getConsumerNames());

                } else {
                    Range<String> range = Range.open(v.getLowestId().toString(), v.getHighestId().toString());
                    s = new PendingMessagesSummary(command.getGroupName(), v.getTotal(), range, v.getConsumerNames());
                }
                return new ReactiveRedisConnection.CommandResponse<>(command, s);
            });
        });
    }

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<PendingRecordsCommand, PendingMessages>> xPending(Publisher<PendingRecordsCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroupName(), "Group name must not be null!");

            byte[] k = toByteArray(command.getKey());

            List<Object> params = new ArrayList<>();
            params.add(k);
            params.add(command.getGroupName());

            params.add(((Range.Bound<String>)command.getRange().getLowerBound()).getValue().orElse("-"));
            params.add(((Range.Bound<String>)command.getRange().getUpperBound()).getValue().orElse("+"));

            if (command.getCount() != null) {
                params.add(command.getCount());
            }
            if (command.getConsumerName() != null) {
                params.add(command.getConsumerName());
            }

            Mono<List<PendingEntry>> m = write(k, StringCodec.INSTANCE, RedisCommands.XPENDING_ENTRIES, params.toArray());
            return m.map(list -> {
                List<PendingMessage> msgs = list.stream().map(v -> new PendingMessage(RecordId.of(v.getId().toString()),
                        Consumer.from(command.getGroupName(), v.getConsumerName()),
                        Duration.of(v.getIdleTime(), ChronoUnit.MILLIS),
                        v.getDeliveryCount())).collect(Collectors.toList());
                PendingMessages s = new PendingMessages(command.getGroupName(), command.getRange(), msgs);
                return new ReactiveRedisConnection.CommandResponse<>(command, s);
            });
        });
    }

    private static final RedisCommand<org.redisson.api.stream.StreamInfo<Object, Object>> XINFO_STREAM = new RedisCommand<>("XINFO", "STREAM",
                new ListMultiDecoder2(
                        new StreamInfoDecoder(),
                        new ObjectDecoder(StringCodec.INSTANCE.getValueDecoder()),
                        new ObjectMapDecoder(false)));

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<XInfoCommand, StreamInfo.XInfoStream>> xInfo(Publisher<XInfoCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] k = toByteArray(command.getKey());

            Mono<org.redisson.api.stream.StreamInfo<byte[], byte[]>> m = write(k, ByteArrayCodec.INSTANCE, XINFO_STREAM, k);
            return m.map(i -> {

                Map<String, Object> res = new HashMap<>();
                res.put("length", (long) i.getLength());
                res.put("first-entry", i.getFirstEntry().getData());
                res.put("last-entry", i.getLastEntry().getData());
                res.put("radix-tree-keys", i.getRadixTreeKeys());
                res.put("radix-tree-nodes", i.getRadixTreeNodes());
                res.put("groups", (long) i.getGroups());
                res.put("last-generated-id", i.getLastGeneratedId().toString());

                List<Object> list = res.entrySet().stream()
                                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                                        .collect(Collectors.toList());
                return new ReactiveRedisConnection.CommandResponse<>(command, StreamInfo.XInfoStream.fromList(list));
            });
        });
    }

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<XInfoCommand, Flux<StreamInfo.XInfoGroup>>> xInfoGroups(Publisher<XInfoCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] k = toByteArray(command.getKey());

            Mono<List<StreamGroup>> m = write(k, StringCodec.INSTANCE, RedisCommands.XINFO_GROUPS, k);
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, Flux.fromStream(v.stream()).map(r -> {
                Map<String, Object> res = new HashMap<>();
                res.put("name", r.getName());
                res.put("consumers", (long) r.getConsumers());
                res.put("pending", (long) r.getPending());
                res.put("last-delivered-id", r.getLastDeliveredId().toString());

                List<Object> list = res.entrySet().stream()
                                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                                        .collect(Collectors.toList());
                return StreamInfo.XInfoGroup.fromList(list);
            })));
        });
    }

    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<XInfoCommand, Flux<StreamInfo.XInfoConsumer>>> xInfoConsumers(Publisher<XInfoCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroupName(), "Group name must not be null!");

            byte[] k = toByteArray(command.getKey());

            Mono<List<StreamConsumer>> m = write(k, StringCodec.INSTANCE, RedisCommands.XINFO_CONSUMERS, k, command.getGroupName());
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, Flux.fromStream(v.stream()).map(r -> {
                Map<String, Object> res = new HashMap<>();
                res.put("name", r.getName());
                res.put("idle", r.getIdleTime());
                res.put("pending", (long) r.getPending());

                List<Object> list = res.entrySet().stream()
                                        .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                                        .collect(Collectors.toList());
                return new StreamInfo.XInfoConsumer(command.getGroupName(), list);
            })));
        });
    }

    /** XACK：确认消费组已处理指定消息 ID。 */
    @Override
    public Flux<ReactiveRedisConnection.NumericResponse<AcknowledgeCommand, Long>> xAck(Publisher<AcknowledgeCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroup(), "Group must not be null!");
            Assert.notNull(command.getRecordIds(), "recordIds must not be null!");

            List<Object> params = new ArrayList<>();
            byte[] k = toByteArray(command.getKey());
            params.add(k);
            params.add(command.getGroup());
            params.addAll(toStringList(command.getRecordIds()));

            Mono<Long> m = write(k, StringCodec.INSTANCE, RedisCommands.XACK, params.toArray());
            return m.map(v -> new ReactiveRedisConnection.NumericResponse<>(command, v));
        });
    }

    /** XADD：向 Stream 追加一条记录，支持自动生成或指定 ID。 */
    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<AddStreamRecord, RecordId>> xAdd(Publisher<AddStreamRecord> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getBody(), "Body must not be null!");

            byte[] k = toByteArray(command.getKey());
            List<Object> params = new LinkedList<>();
            params.add(k);

            if (command.getMaxlen() != null) {
                params.add("MAXLEN");
                params.add(command.getMaxlen());
            }

            if (!command.getRecord().getId().shouldBeAutoGenerated()) {
                params.add(command.getRecord().getId().getValue());
            } else {
                params.add("*");
            }

            for (Map.Entry<ByteBuffer, ByteBuffer> entry : command.getBody().entrySet()) {
                params.add(toByteArray(entry.getKey()));
                params.add(toByteArray(entry.getValue()));
            }

            Mono<StreamMessageId> m = write(k, StringCodec.INSTANCE, RedisCommands.XADD, params.toArray());
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, RecordId.of(v.toString())));
        });
    }

    /** XDEL：按 ID 删除 Stream 中的消息。 */
    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<DeleteCommand, Long>> xDel(Publisher<DeleteCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getRecordIds(), "recordIds must not be null!");

            byte[] k = toByteArray(command.getKey());
            List<Object> params = new ArrayList<>();
            params.add(k);
            params.addAll(toStringList(command.getRecordIds()));

            Mono<Long> m = write(k, StringCodec.INSTANCE, RedisCommands.XDEL, params.toArray());
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, v));
        });
    }

    /** XLEN：返回 Stream 当前长度。 */
    @Override
    public Flux<ReactiveRedisConnection.NumericResponse<ReactiveRedisConnection.KeyCommand, Long>> xLen(Publisher<ReactiveRedisConnection.KeyCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");

            byte[] k = toByteArray(command.getKey());

            Mono<Long> m = write(k, StringCodec.INSTANCE, RedisCommands.XLEN, k);
            return m.map(v -> new ReactiveRedisConnection.NumericResponse<>(command, v));
        });
    }

    /** XRANGE：按 ID 范围正序读取 Stream 记录。 */
    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> xRange(Publisher<RangeCommand> publisher) {
        return range(RedisCommands.XRANGE, publisher);
    }

    /** XRANGE/XREVRANGE 共用实现：组装边界、COUNT 参数并解码为 {@link ByteBufferRecord} 流。 */
    private Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> range(RedisCommand<?> rangeCommand, Publisher<RangeCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getRange(), "Range must not be null!");
            Assert.notNull(command.getLimit(), "Limit must not be null!");

            byte[] k = toByteArray(command.getKey());

            List<Object> params = new LinkedList<>();
            params.add(k);

            if (rangeCommand.getName().equals(RedisCommands.XRANGE.getName())) {
                params.add(toLowerBound(command.getRange()));
                params.add(toUpperBound(command.getRange()));
            } else {
                params.add(toUpperBound(command.getRange()));
                params.add(toLowerBound(command.getRange()));
            }

            if (command.getLimit().getCount() > 0) {
                params.add("COUNT");
                params.add(command.getLimit().getCount());
            }

            Mono<Map<StreamMessageId, Map<byte[], byte[]>>> m = write(k, ByteArrayCodec.INSTANCE, rangeCommand, params.toArray());
            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, Flux.fromStream(v.entrySet().stream()).map(e -> {
                Map<ByteBuffer, ByteBuffer> map = e.getValue().entrySet().stream()
                        .collect(Collectors.toMap(entry -> ByteBuffer.wrap(entry.getKey()),
                                entry -> ByteBuffer.wrap(entry.getValue())));
                return StreamRecords.newRecord()
                        .in(command.getKey())
                        .withId(RecordId.of(e.getKey().toString()))
                        .ofBuffer(map);
            })));
        });
    }

    /** 将 Spring {@link Range} 下界转为 Redis Stream ID 字符串（含开区间前缀）。 */
    String toLowerBound(Range range) {
        StringBuilder s = new StringBuilder();
        if (!range.getLowerBound().isInclusive()) {
            s.append("(");
        }
        if (!range.getLowerBound().getValue().isPresent() || range.getLowerBound().getValue().get().toString()
                .isEmpty()) {
            s.append("-");
        } else {
            s.append(range.getLowerBound().getValue().get());
        }
        return s.toString();
    }

    /** 将 Spring {@link Range} 上界转为 Redis Stream ID 字符串（含开区间前缀）。 */
    String toUpperBound(Range range) {
        StringBuilder s = new StringBuilder();
        if (!range.getUpperBound().isInclusive()) {
            s.append("(");
        }
        if (!range.getUpperBound().getValue().isPresent() || range.getUpperBound().getValue().get().toString()
                .isEmpty()) {
            s.append("+");
        } else {
            s.append(range.getUpperBound().getValue().get());
        }
        return s.toString();
    }

    /** XREAD/XREADGROUP：阻塞或非阻塞读取一个或多个 Stream，支持 COUNT/BLOCK/NOACK。 */
    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<ReadCommand, Flux<ByteBufferRecord>>> read(Publisher<ReadCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getStreamOffsets(), "StreamOffsets must not be null!");
            Assert.notNull(command.getReadOptions(), "ReadOptions must not be null!");

            List<Object> params = new ArrayList<>();

            if (command.getConsumer() != null) {
                params.add("GROUP");
                params.add(command.getConsumer().getGroup());
                params.add(command.getConsumer().getName());
            }

            if (command.getReadOptions().getCount() != null && command.getReadOptions().getCount() > 0) {
                params.add("COUNT");
                params.add(command.getReadOptions().getCount());
            }

            if (command.getReadOptions().getBlock() != null && command.getReadOptions().getBlock() > 0) {
                params.add("BLOCK");
                params.add(command.getReadOptions().getBlock());
            }

            if (command.getConsumer() != null && command.getReadOptions().isNoack()) {
                params.add("NOACK");
            }

            params.add("STREAMS");
            for (StreamOffset<ByteBuffer> streamOffset : command.getStreamOffsets()) {
                params.add(toByteArray(streamOffset.getKey()));
            }

            for (StreamOffset<ByteBuffer> streamOffset : command.getStreamOffsets()) {
                params.add(streamOffset.getOffset().getOffset());
            }

            Mono<Map<String, Map<StreamMessageId, Map<byte[], byte[]>>>> m;

            if (command.getConsumer() == null) {
                if (command.getReadOptions().getBlock() != null && command.getReadOptions().getBlock() > 0) {
                    m = read(toByteArray(command.getStreamOffsets().get(0).getKey()), ByteArrayCodec.INSTANCE, RedisCommands.XREAD_BLOCKING, params.toArray());
                } else {
                    m = read(toByteArray(command.getStreamOffsets().get(0).getKey()), ByteArrayCodec.INSTANCE, RedisCommands.XREAD, params.toArray());
                }
            } else {
                if (command.getReadOptions().getBlock() != null && command.getReadOptions().getBlock() > 0) {
                    m = read(toByteArray(command.getStreamOffsets().get(0).getKey()), ByteArrayCodec.INSTANCE, RedisCommands.XREADGROUP_BLOCKING, params.toArray());
                } else {
                    m = read(toByteArray(command.getStreamOffsets().get(0).getKey()), ByteArrayCodec.INSTANCE, RedisCommands.XREADGROUP, params.toArray());
                }
            }

            return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, Flux.fromStream(v.entrySet().stream())
                    .map(ee -> {
                        return ee.getValue().entrySet().stream().map(e -> {
                            Map<ByteBuffer, ByteBuffer> map = e.getValue().entrySet().stream()
                                    .collect(Collectors.toMap(entry -> ByteBuffer.wrap(entry.getKey()),
                                            entry -> ByteBuffer.wrap(entry.getValue())));
                            return StreamRecords.newRecord()
                                    .in(ee.getKey())
                                    .withId(RecordId.of(e.getKey().toString()))
                                    .ofBuffer(map);
                        });
                    }).flatMap(Flux::fromStream)
            ));

        });
    }

    private static final RedisStrictCommand<String> XGROUP_STRING = new RedisStrictCommand<>("XGROUP");

    /** XGROUP：创建消费组、删除消费者或销毁消费组。 */
    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<GroupCommand, String>> xGroup(Publisher<GroupCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getGroupName(), "GroupName must not be null!");

            byte[] k = toByteArray(command.getKey());

            if (command.getAction().equals(GroupCommand.GroupCommandAction.CREATE)) {
                Assert.notNull(command.getReadOffset(), "ReadOffset must not be null!");

                Mono<String> m = write(k, StringCodec.INSTANCE, XGROUP_STRING, "CREATE", k, command.getGroupName(), command.getReadOffset().getOffset(), "MKSTREAM");
                return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, v));
            }

            if (command.getAction().equals(GroupCommand.GroupCommandAction.DELETE_CONSUMER)) {
                Assert.notNull(command.getConsumerName(), "ConsumerName must not be null!");

                Mono<Long> m = write(k, StringCodec.INSTANCE, RedisCommands.XGROUP_LONG, "DELCONSUMER", k, command.getGroupName(), command.getConsumerName());
                return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, v > 0 ? "OK" : "Error"));
            }

            if (command.getAction().equals(GroupCommand.GroupCommandAction.DESTROY)) {
                Mono<Long> m = write(k, StringCodec.INSTANCE, RedisCommands.XGROUP_LONG, "DESTROY", k, command.getGroupName());
                return m.map(v -> new ReactiveRedisConnection.CommandResponse<>(command, v > 0 ? "OK" : "Error"));
            }

            // 未知 XGROUP 子命令。
            throw new IllegalArgumentException("unknown command " + command.getAction());
        });
    }

    /** XREVRANGE：按 ID 范围逆序读取 Stream 记录。 */
    @Override
    public Flux<ReactiveRedisConnection.CommandResponse<RangeCommand, Flux<ByteBufferRecord>>> xRevRange(Publisher<RangeCommand> publisher) {
        return range(RedisCommands.XREVRANGE, publisher);
    }

    /** XTRIM MAXLEN：按最大长度裁剪 Stream。 */
    @Override
    public Flux<ReactiveRedisConnection.NumericResponse<ReactiveRedisConnection.KeyCommand, Long>> xTrim(Publisher<TrimCommand> publisher) {
        return execute(publisher, command -> {

            Assert.notNull(command.getKey(), "Key must not be null!");
            Assert.notNull(command.getCount(), "Count must not be null!");

            byte[] k = toByteArray(command.getKey());

            Mono<Long> m = write(k, StringCodec.INSTANCE, RedisCommands.XTRIM, k, "MAXLEN", command.getCount());
            return m.map(v -> new ReactiveRedisConnection.NumericResponse<>(command, v));
        });
    }
}
