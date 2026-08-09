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

import org.redisson.Redisson;
import org.redisson.api.BatchOptions;
import org.redisson.api.BatchOptions.ExecutionMode;
import org.redisson.api.BatchResult;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.*;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.convertor.*;
import org.redisson.client.protocol.decoder.*;
import org.redisson.command.BatchPromise;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.misc.CompletableFutureWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.geo.*;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.redisson.client.protocol.RedisCommands.LRANGE;

/**
 * Spring Data Redis {@link RedisConnection} 的 Redisson 同步实现。
 * <p>封装 String/Hash/List/Set/ZSet/Geo/Stream 等命令，
支持管道、事务、Pub/Sub 与 Lua 脚本执行。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonConnection extends AbstractRedisConnection {

    /** 连接是否已关闭。 */
    private boolean closed;
    /** 底层 Redisson 客户端。 */
    protected final Redisson redisson;
    
    /** 异步命令执行器。 */
    CommandAsyncExecutor executorService;
    /** Pub/Sub 订阅管理器。 */
    private RedissonSubscription subscription;
    
    /** 绑定 {@link RedissonClient} 并初始化命令执行器。 */
    public RedissonConnection(RedissonClient redisson) {
        super();
        this.redisson = (Redisson) redisson;
        executorService = this.redisson.getCommandExecutor();
    }

    /** 关闭连接并释放 Redisson 资源。 */
    @Override
    public void close() throws DataAccessException {
        super.close();
        
        if (isQueueing()) {
            CommandBatchService es = (CommandBatchService) executorService;
            if (!es.isExecuted()) {
                discard();
            }
        }
        closed = true;
    }
    
    /** 返回连接是否已关闭。 */
    @Override
    public boolean isClosed() {
        return closed;
    }

    /** 返回底层 Redisson 原生连接对象。 */
    @Override
    public Object getNativeConnection() {
        return redisson;
    }

    /** 是否处于 MULTI 事务排队模式。 */
    @Override
    public boolean isQueueing() {
        if (executorService instanceof CommandBatchService) {
            CommandBatchService es = (CommandBatchService) executorService;
            return es.getOptions().getExecutionMode() == ExecutionMode.REDIS_WRITE_ATOMIC;
        }
        return false;
    }

    /** 是否已开启管道模式。 */
    @Override
    public boolean isPipelined() {
        if (executorService instanceof CommandBatchService) {
            CommandBatchService es = (CommandBatchService) executorService;
            return es.getOptions().getExecutionMode() == ExecutionMode.IN_MEMORY || es.getOptions().getExecutionMode() == ExecutionMode.IN_MEMORY_ATOMIC;
        }
        return false;
    }
    
    /** 管道是否以原子方式执行。 */
    public boolean isPipelinedAtomic() {
        if (executorService instanceof CommandBatchService) {
            CommandBatchService es = (CommandBatchService) executorService;
            return es.getOptions().getExecutionMode() == ExecutionMode.IN_MEMORY_ATOMIC;
        }
        return false;
    }

    /** 开启管道，后续命令批量发送。 */
    @Override
    public void openPipeline() {
        BatchOptions options = BatchOptions.defaults()
                .executionMode(ExecutionMode.IN_MEMORY);
        this.executorService = executorService.createCommandBatchService(options);
    }

    /** 关闭管道并返回各命令结果列表。 */
    @Override
    public List<Object> closePipeline() throws RedisPipelineException {
        if (isPipelined()) {
            CommandBatchService es = (CommandBatchService) executorService;
            try {
                BatchResult<?> result = es.execute();
                filterResults(result);
                if (isPipelinedAtomic()) {
                    return Arrays.<Object>asList((List<Object>) result.getResponses());
                }
                return (List<Object>) result.getResponses();
            } catch (Exception ex) {
                throw new RedisPipelineException(ex);
            } finally {
                resetConnection();
            }
        }
        return Collections.emptyList();
    }

    /** 执行任意 Redis 命令（反射或字符串形式）。 */
    @Override
    public Object execute(String command, byte[]... args) {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase(command)
                    && Modifier.isPublic(method.getModifiers())
                        && (method.getParameterTypes().length == args.length)) {
                try {
                    Object t = execute(method, args);
                    if (t instanceof String) {
                        return ((String) t).getBytes();
                    }
                    return t;
                } catch (IllegalArgumentException e) {
                    if (isPipelined()) {
                        throw new RedisPipelineException(e);
                    }

                    throw new InvalidDataAccessApiUsageException(e.getMessage(), e);
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    /** 执行任意 Redis 命令（反射或字符串形式）。 */
    private Object execute(Method method, byte[]... args) {
        if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] == byte[][].class) {
            return ReflectionUtils.invokeMethod(method, this, args);
        }
        if (args == null) {
            return ReflectionUtils.invokeMethod(method, this);
        }
        return ReflectionUtils.invokeMethod(method, this, Arrays.asList(args).toArray());
    }
    
    <V> V syncFuture(RFuture<V> future) {
        try {
            return executorService.get(future);
        } catch (Exception ex) {
            throw transform(ex);
        }
    }

    /** 将 Redisson 异常转为 Spring DataAccessException。 */
    protected RuntimeException transform(Exception ex) {
        DataAccessException exception = RedissonConnectionFactory.EXCEPTION_TRANSLATION.translate(ex);
        if (exception != null) {
            return exception;
        }
        return new RedisSystemException(ex.getMessage(), ex);
    }
    
    /** EXISTS：判断 key 是否存在。 */
    @Override
    public Boolean exists(byte[] key) {
        return read(key, StringCodec.INSTANCE, RedisCommands.EXISTS, key);
    }
    
    /** DEL：删除一个或多个 key。 */
    @Override
    public Long del(byte[]... keys) {
        return write(keys[0], LongCodec.INSTANCE, RedisCommands.DEL, Arrays.asList(keys).toArray());
    }

    /** UNLINK：异步删除 key。 */
    @Override
    public Long unlink(byte[]... keys) {
        return write(keys[0], LongCodec.INSTANCE, RedisCommands.UNLINK, Arrays.asList(keys).toArray());
    }

    private static final RedisStrictCommand<DataType> TYPE = new RedisStrictCommand<DataType>("TYPE", new DataTypeConvertor());

    /** TYPE：返回 key 的数据类型。 */
    @Override
    public DataType type(byte[] key) {
        return read(key, StringCodec.INSTANCE, TYPE, key);
    }

    private static final RedisStrictCommand<Set<byte[]>> KEYS = new RedisStrictCommand<Set<byte[]>>("KEYS", new ObjectSetReplayDecoder<byte[]>());
    
    /** KEYS：按模式匹配返回 key 集合。 */
    @Override
    public Set<byte[]> keys(byte[] pattern) {
        if (isQueueing()) {
            return read(null, ByteArrayCodec.INSTANCE, KEYS, pattern);
        }

        List<CompletableFuture<Set<byte[]>>> futures = executorService.readAllAsync(ByteArrayCodec.INSTANCE, KEYS, pattern);
        CompletableFuture<Void> ff = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<Set<byte[]>> future = ff.thenApply(r -> {
            return futures.stream().flatMap(f -> f.getNow(new HashSet<>()).stream()).collect(Collectors.toSet());
        }).toCompletableFuture();
        return sync(new CompletableFutureWrapper<>(future));
    }

    /** SCAN：增量迭代 key 空间。 */
    @Override
    public Cursor<byte[]> scan(ScanOptions options) {
        return new ScanCursor<byte[]>(0, options) {

            private RedisClient client;
            private Iterator<MasterSlaveEntry> entries = executorService.getConnectionManager().getEntrySet().iterator();
            private MasterSlaveEntry entry = entries.next();
            
            @Override
            protected ScanIteration<byte[]> doScan(long cursorId, ScanOptions options) {
                if (isQueueing() || isPipelined()) {
                    throw new UnsupportedOperationException("'SSCAN' cannot be called in pipeline / transaction mode.");
                }

                if (entry == null) {
                    return null;
                }

                List<Object> args = new ArrayList<Object>();
                if (cursorId == 101010101010101010L) {
                    cursorId = 0;
                }
                args.add(Long.toUnsignedString(cursorId));
                if (options.getPattern() != null) {
                    args.add("MATCH");
                    args.add(options.getPattern());
                }
                if (options.getCount() != null) {
                    args.add("COUNT");
                    args.add(options.getCount());
                }
                
                RFuture<ListScanResult<byte[]>> f = executorService.readAsync(client, entry, ByteArrayCodec.INSTANCE, RedisCommands.SCAN, args.toArray());
                ListScanResult<byte[]> res = syncFuture(f);
                String pos = res.getPos();
                client = res.getRedisClient();
                if ("0".equals(pos)) {
                    if (entries.hasNext()) {
                        pos = "101010101010101010";
                        entry = entries.next();
                        client = null;
                    } else {
                        entry = null;
                    }
                }
                
                return new ScanIteration<byte[]>(Long.parseUnsignedLong(pos), res.getValues());
            }
        }.open();
    }

    /** RANDOMKEY：随机返回一个 key。 */
    @Override
    public byte[] randomKey() {
        if (isQueueing()) {
            return read(null, ByteArrayCodec.INSTANCE, RedisCommands.RANDOM_KEY);
        }
        
        RFuture<byte[]> f = executorService.readRandomAsync(ByteArrayCodec.INSTANCE, RedisCommands.RANDOM_KEY);
        return sync(f);
    }

    /** RENAME：重命名 key。 */
    @Override
    public void rename(byte[] oldName, byte[] newName) {
        write(oldName, StringCodec.INSTANCE, RedisCommands.RENAME, oldName, newName);
    }

    /** RENAMENX：仅当新 key 不存在时重命名。 */
    @Override
    public Boolean renameNX(byte[] oldName, byte[] newName) {
        return write(oldName, StringCodec.INSTANCE, RedisCommands.RENAMENX, oldName, newName);
    }

    private static final RedisStrictCommand<Boolean> EXPIRE = new RedisStrictCommand<Boolean>("EXPIRE", new BooleanReplayConvertor());
    
    /** EXPIRE：以秒为单位设置过期时间。 */
    @Override
    public Boolean expire(byte[] key, long seconds) {
        return write(key, StringCodec.INSTANCE, EXPIRE, key, seconds);
    }

    /** PEXPIRE：以毫秒为单位设置过期时间。 */
    @Override
    public Boolean pExpire(byte[] key, long millis) {
        return write(key, StringCodec.INSTANCE, RedisCommands.PEXPIRE, key, millis);
    }

    private static final RedisStrictCommand<Boolean> EXPIREAT = new RedisStrictCommand<Boolean>("EXPIREAT", new BooleanReplayConvertor());
    
    /** EXPIREAT：按 Unix 秒时间戳设置过期。 */
    @Override
    public Boolean expireAt(byte[] key, long unixTime) {
        return write(key, StringCodec.INSTANCE, EXPIREAT, key, unixTime);
    }

    /** PEXPIREAT：按 Unix 毫秒时间戳设置过期。 */
    @Override
    public Boolean pExpireAt(byte[] key, long unixTimeInMillis) {
        return write(key, StringCodec.INSTANCE, RedisCommands.PEXPIREAT, key, unixTimeInMillis);
    }

    /** PERSIST：移除 key 的过期时间。 */
    @Override
    public Boolean persist(byte[] key) {
        return write(key, StringCodec.INSTANCE, RedisCommands.PERSIST, key);
    }

    /** MOVE：将 key 迁移到指定数据库。 */
    @Override
    public Boolean move(byte[] key, int dbIndex) {
        return write(key, StringCodec.INSTANCE, RedisCommands.MOVE, key, dbIndex);
    }

    private static final RedisStrictCommand<Long> TTL = new RedisStrictCommand<Long>("TTL");
    
    /** TTL：返回 key 剩余存活秒数。 */
    @Override
    public Long ttl(byte[] key) {
        return read(key, StringCodec.INSTANCE, TTL, key);
    }

    /** 阻塞等待 {@link RFuture} 完成并返回结果。 */
    protected <T> T sync(RFuture<T> f) {
        if (isPipelined()) {
            return null;
        }
        if (isQueueing()) {
            ((BatchPromise)f.toCompletableFuture()).getSentPromise().join();
            return null;
        }

        return syncFuture(f);
    }

    /** TTL：返回 key 剩余存活秒数。 */
    @Override
    public Long ttl(byte[] key, TimeUnit timeUnit) {
        return read(key, StringCodec.INSTANCE, new RedisStrictCommand<Long>("TTL", new SecondsConvertor(timeUnit, TimeUnit.SECONDS) {
            @Override
            public Long convert(Object obj) {
                if ((Long) obj < 0) {
                    return (Long) obj;
                }
                return super.convert(obj);
            }
        }), key);
    }

    /** PTTL：返回 key 剩余存活毫秒数。 */
    @Override
    public Long pTtl(byte[] key) {
        return read(key, StringCodec.INSTANCE, RedisCommands.PTTL, key);
    }

    /** PTTL：返回 key 剩余存活毫秒数。 */
    @Override
    public Long pTtl(byte[] key, TimeUnit timeUnit) {
        return read(key, StringCodec.INSTANCE, new RedisStrictCommand<Long>("PTTL", new SecondsConvertor(timeUnit, TimeUnit.MILLISECONDS) {
            @Override
            public Long convert(Object obj) {
                if ((Long) obj < 0) {
                    return (Long) obj;
                }
                return super.convert(obj);
            }
        }), key);
    }

    /** SORT：对列表/集合/有序集合排序，可选 STORE。 */
    @Override
    public List<byte[]> sort(byte[] key, SortParameters sortParams) {
        List<Object> params = new ArrayList<Object>();
        params.add(key);
        if (sortParams != null) {
            if (sortParams.getByPattern() != null) {
                params.add("BY");
                params.add(sortParams.getByPattern());
            }
            
            if (sortParams.getLimit() != null) {
                params.add("LIMIT");
                if (sortParams.getLimit().getStart() != -1) {
                    params.add(sortParams.getLimit().getStart());
                }
                if (sortParams.getLimit().getCount() != -1) {
                    params.add(sortParams.getLimit().getCount());
                }
            }
            
            if (sortParams.getGetPattern() != null) {
                for (byte[] pattern : sortParams.getGetPattern()) {
                    params.add("GET");
                    params.add(pattern);
                }
            }
            
            if (sortParams.getOrder() != null) {
                params.add(sortParams.getOrder());
            }
            
            Boolean isAlpha = sortParams.isAlphabetic();
            if (isAlpha != null && isAlpha) {
                params.add("ALPHA");
            }
        }
        
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.SORT_LIST, params.toArray());
    }
    
    private static final RedisCommand<Long> SORT_TO = new RedisCommand<Long>("SORT");

    /** SORT：对列表/集合/有序集合排序，可选 STORE。 */
    @Override
    public Long sort(byte[] key, SortParameters sortParams, byte[] storeKey) {
        List<Object> params = new ArrayList<Object>();
        params.add(key);
        if (sortParams != null) {
            if (sortParams.getByPattern() != null) {
                params.add("BY");
                params.add(sortParams.getByPattern());
            }
            
            if (sortParams.getLimit() != null) {
                params.add("LIMIT");
                if (sortParams.getLimit().getStart() != -1) {
                    params.add(sortParams.getLimit().getStart());
                }
                if (sortParams.getLimit().getCount() != -1) {
                    params.add(sortParams.getLimit().getCount());
                }
            }
            
            if (sortParams.getGetPattern() != null) {
                for (byte[] pattern : sortParams.getGetPattern()) {
                    params.add("GET");
                    params.add(pattern);
                }
            }
            
            if (sortParams.getOrder() != null) {
                params.add(sortParams.getOrder());
            }
            
            Boolean isAlpha = sortParams.isAlphabetic();
            if (isAlpha != null && isAlpha) {
                params.add("ALPHA");
            }
        }
        
        params.add("STORE");
        params.add(storeKey);
        
        return read(key, ByteArrayCodec.INSTANCE, SORT_TO, params.toArray());
    }

    /** DUMP：序列化 key 的值。 */
    @Override
    public byte[] dump(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.DUMP, key);
    }

    /** RESTORE：用 DUMP 数据恢复 key 并设置 TTL。 */
    @Override
    public void restore(byte[] key, long ttlInMillis, byte[] serializedValue) {
        write(key, StringCodec.INSTANCE, RedisCommands.RESTORE, key, ttlInMillis, serializedValue);
    }

    /** GET：读取字符串值。 */
    @Override
    public byte[] get(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.GET, key);
    }

    /** GETSET：设置新值并返回旧值。 */
    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return write(key, ByteArrayCodec.INSTANCE, RedisCommands.GETSET, key, value);
    }

    private static final RedisCommand<List<Object>> MGET = new RedisCommand<List<Object>>("MGET", new ObjectListReplayDecoder<Object>());
    
    /** MGET：批量读取多个 key。 */
    @Override
    public List<byte[]> mGet(byte[]... keys) {
        return read(keys[0], ByteArrayCodec.INSTANCE, MGET, Arrays.asList(keys).toArray());
    }

    private static final RedisCommand<Boolean> SET = new RedisCommand<>("SET", new BooleanNullSafeReplayConvertor());
    
    /** SET：写入字符串值，支持过期与 NX/XX 选项。 */
    @Override
    public Boolean set(byte[] key, byte[] value) {
        return write(key, StringCodec.INSTANCE, SET, key, value);
    }

    /** SET：写入字符串值，支持过期与 NX/XX 选项。 */
    @Override
    public Boolean set(byte[] key, byte[] value, Expiration expiration, SetOption option) {
        if (expiration == null) {
            return set(key, value);
        } else if (expiration.isPersistent()) {
            if (option == null || option == SetOption.UPSERT) {
                return set(key, value);
            }
            if (option == SetOption.SET_IF_ABSENT) {
                return write(key, StringCodec.INSTANCE, SET, key, value, "NX");
            }
            if (option == SetOption.SET_IF_PRESENT) {
                return write(key, StringCodec.INSTANCE, SET, key, value, "XX");
            }
        } else {
            if (option == null || option == SetOption.UPSERT) {
                return write(key, StringCodec.INSTANCE, SET, key, value, "PX", expiration.getExpirationTimeInMilliseconds());
            }
            if (option == SetOption.SET_IF_ABSENT) {
                return write(key, StringCodec.INSTANCE, SET, key, value, "PX", expiration.getExpirationTimeInMilliseconds(), "NX");
            }
            if (option == SetOption.SET_IF_PRESENT) {
                return write(key, StringCodec.INSTANCE, SET, key, value, "PX", expiration.getExpirationTimeInMilliseconds(), "XX");
            }
        }
        throw new IllegalArgumentException();
    }

    /** SETNX：仅当 key 不存在时写入。 */
    @Override
    public Boolean setNX(byte[] key, byte[] value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.SETNX, key, value);
    }

    private static final RedisCommand<Boolean> SETEX = new RedisCommand<Boolean>("SETEX", new BooleanReplayConvertor());
    
    /** SETEX：写入并设置秒级过期。 */
    @Override
    public Boolean setEx(byte[] key, long seconds, byte[] value) {
        return write(key, StringCodec.INSTANCE, SETEX, key, seconds, value);
    }

    private static final RedisCommand<Boolean> PSETEX = new RedisCommand<Boolean>("PSETEX", new BooleanReplayConvertor());
    
    /** PSETEX：写入并设置毫秒级过期。 */
    @Override
    public Boolean pSetEx(byte[] key, long milliseconds, byte[] value) {
        return write(key, StringCodec.INSTANCE, PSETEX, key, milliseconds, value);
    }
    
    private static final RedisCommand<Boolean> MSET = new RedisCommand<Boolean>("MSET", new BooleanReplayConvertor());

    /** MSET：批量写入 key-value。 */
    @Override
    public Boolean mSet(Map<byte[], byte[]> tuple) {
        List<byte[]> params = convert(tuple);
        return write(tuple.keySet().iterator().next(), StringCodec.INSTANCE, MSET, params.toArray());
    }

    /** 将键值对 Map 展开为 Redis 参数列表。 */
    protected List<byte[]> convert(Map<byte[], byte[]> tuple) {
        List<byte[]> params = new ArrayList<byte[]>(tuple.size()*2);
        for (Entry<byte[], byte[]> entry : tuple.entrySet()) {
            params.add(entry.getKey());
            params.add(entry.getValue());
        }
        return params;
    }

    /** MSETNX：全部 key 不存在时批量写入。 */
    @Override
    public Boolean mSetNX(Map<byte[], byte[]> tuple) {
        List<byte[]> params = convert(tuple);
        return write(tuple.keySet().iterator().next(), StringCodec.INSTANCE, RedisCommands.MSETNX, params.toArray());
    }

    /** INCR：字符串值自增 1。 */
    @Override
    public Long incr(byte[] key) {
        return write(key, StringCodec.INSTANCE, RedisCommands.INCR, key);
    }

    /** INCRBY：字符串值按整数增量自增。 */
    @Override
    public Long incrBy(byte[] key, long value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.INCRBY, key, value);
    }

    /** INCRBY：字符串值按整数增量自增。 */
    @Override
    public Double incrBy(byte[] key, double value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.INCRBYFLOAT, key, BigDecimal.valueOf(value).toPlainString());
    }

    /** DECR：字符串值自减 1。 */
    @Override
    public Long decr(byte[] key) {
        return write(key, StringCodec.INSTANCE, RedisCommands.DECR, key);
    }

    private static final RedisStrictCommand<Long> DECRBY = new RedisStrictCommand<Long>("DECRBY");
    
    /** DECRBY：字符串值按整数减量自减。 */
    @Override
    public Long decrBy(byte[] key, long value) {
        return write(key, StringCodec.INSTANCE, DECRBY, key, value);
    }

    private static final RedisStrictCommand<Long> APPEND = new RedisStrictCommand<Long>("APPEND");
    
    /** APPEND：在字符串末尾追加内容。 */
    @Override
    public Long append(byte[] key, byte[] value) {
        return write(key, StringCodec.INSTANCE, APPEND, key, value);
    }
    
    private static final RedisCommand<Object> GETRANGE = new RedisCommand<Object>("GETRANGE");

    /** GETRANGE：按区间读取子串。 */
    @Override
    public byte[] getRange(byte[] key, long begin, long end) {
        return read(key, ByteArrayCodec.INSTANCE, GETRANGE, key, begin, end);
    }

    private static final RedisCommand<Void> SETRANGE = new RedisCommand<Void>("SETRANGE", new VoidReplayConvertor());

    /** SETRANGE：从指定偏移覆写子串。 */
    @Override
    public void setRange(byte[] key, byte[] value, long offset) {
        write(key, ByteArrayCodec.INSTANCE, SETRANGE, key, offset, value);
    }

    /** GETBIT：读取指定偏移位。 */
    @Override
    public Boolean getBit(byte[] key, long offset) {
        return read(key, StringCodec.INSTANCE, RedisCommands.GETBIT, key, offset);
    }

    /** SETBIT：设置指定偏移位。 */
    @Override
    public Boolean setBit(byte[] key, long offset, boolean value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.SETBIT, key, offset, value ? 1 : 0);
    }

    /** BITCOUNT：统计字符串中置 1 的位数。 */
    @Override
    public Long bitCount(byte[] key) {
        return read(key, StringCodec.INSTANCE, RedisCommands.BITCOUNT, key);
    }

    /** BITCOUNT：统计字符串中置 1 的位数。 */
    @Override
    public Long bitCount(byte[] key, long begin, long end) {
        return read(key, StringCodec.INSTANCE, RedisCommands.BITCOUNT, key, begin, end);
    }

    private static final RedisStrictCommand<Long> BITOP = new RedisStrictCommand<Long>("BITOP");
    
    /** BITOP：对多个字符串执行 AND/OR/XOR/NOT 位运算。 */
    @Override
    public Long bitOp(BitOperation op, byte[] destination, byte[]... keys) {
        if (op == BitOperation.NOT && keys.length > 1) {
            throw new UnsupportedOperationException("NOT operation doesn't support more than single source key");
        }

        List<Object> params = new ArrayList<Object>(keys.length + 2);
        params.add(op);
        params.add(destination);
        params.addAll(Arrays.asList(keys));
        return write(keys[0], StringCodec.INSTANCE, BITOP, params.toArray());
    }

    /** STRLEN：返回字符串字节长度。 */
    @Override
    public Long strLen(byte[] key) {
        return read(key, StringCodec.INSTANCE, RedisCommands.STRLEN, key);
    }

    private static final RedisStrictCommand<Long> RPUSH = new RedisStrictCommand<Long>("RPUSH");
    
    /** RPUSH：从列表右侧入队。 */
    @Override
    public Long rPush(byte[] key, byte[]... values) {
        List<Object> args = new ArrayList<Object>(values.length + 1);
        args.add(key);
        args.addAll(Arrays.asList(values));
        return write(key, StringCodec.INSTANCE, RPUSH, args.toArray());
    }

    private static final RedisStrictCommand<Long> LPUSH = new RedisStrictCommand<Long>("LPUSH");
    
    /** LPUSH：从列表左侧入队。 */
    @Override
    public Long lPush(byte[] key, byte[]... values) {
        List<Object> args = new ArrayList<Object>(values.length + 1);
        args.add(key);
        args.addAll(Arrays.asList(values));
        return write(key, StringCodec.INSTANCE, LPUSH, args.toArray());
    }

    private static final RedisStrictCommand<Long> RPUSHX = new RedisStrictCommand<Long>("RPUSHX");
    
    /** RPUSHX：仅当列表存在时从右侧入队。 */
    @Override
    public Long rPushX(byte[] key, byte[] value) {
        return write(key, StringCodec.INSTANCE, RPUSHX, key, value);
    }

    private static final RedisStrictCommand<Long> LPUSHX = new RedisStrictCommand<Long>("LPUSHX");
    
    /** LPUSHX：仅当列表存在时从左侧入队。 */
    @Override
    public Long lPushX(byte[] key, byte[] value) {
        return write(key, StringCodec.INSTANCE, LPUSHX, key, value);
    }

    private static final RedisStrictCommand<Long> LLEN = new RedisStrictCommand<Long>("LLEN");
    
    /** LLEN：返回列表长度。 */
    @Override
    public Long lLen(byte[] key) {
        return read(key, StringCodec.INSTANCE, LLEN, key);
    }

    /** LRANGE：按闭区间下标返回列表片段。 */
    @Override
    public List<byte[]> lRange(byte[] key, long start, long end) {
        return read(key, ByteArrayCodec.INSTANCE, LRANGE, key, start, end);
    }

    /** LTRIM：裁剪列表到指定区间。 */
    @Override
    public void lTrim(byte[] key, long start, long end) {
        write(key, StringCodec.INSTANCE, RedisCommands.LTRIM, key, start, end);
    }

    /** LINDEX：按下标读取列表元素。 */
    @Override
    public byte[] lIndex(byte[] key, long index) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.LINDEX, key, index);
    }

    private static final RedisStrictCommand<Long> LINSERT = new RedisStrictCommand<Long>("LINSERT");
    
    /** LINSERT：在 pivot 前/后插入元素。 */
    @Override
    public Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value) {
        return write(key, StringCodec.INSTANCE, LINSERT, key, where, pivot, value);
    }

    private final List<String> commandsToRemove = Arrays.asList("SET", 
            "RESTORE", "LTRIM", "SETEX", "SETRANGE", "FLUSHDB", "LSET", "MSET", "HMSET", "RENAME");
    private final List<Integer> indexToRemove = new ArrayList<Integer>();
    private int index = -1;
    
    <T> T write(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {
        RFuture<T> f = executorService.writeAsync(key, codec, command, params);
        indexCommand(command);
        return sync(f);
    }

    /** 返回当前命令在管道/事务中的索引。 */
    protected void indexCommand(RedisCommand<?> command) {
        if (isQueueing() || isPipelined()) {
            index++;
            if (commandsToRemove.contains(command.getName())) {
                indexToRemove.add(index);
            }
        }
    }
    
    <T> T read(byte[] key, Codec codec, RedisCommand<?> command, Object... params) {
        RFuture<T> f = executorService.readAsync(key, codec, command, params);
        indexCommand(command);
        return sync(f);
    }
    
    /** LSET：按下标覆写列表元素。 */
    @Override
    public void lSet(byte[] key, long index, byte[] value) {
        write(key, StringCodec.INSTANCE, RedisCommands.LSET, key, index, value);
    }

    private static final RedisStrictCommand<Long> LREM = new RedisStrictCommand<Long>("LREM");
    
    /** LREM：删除列表中指定值的元素。 */
    @Override
    public Long lRem(byte[] key, long count, byte[] value) {
        return write(key, StringCodec.INSTANCE, LREM, key, count, value);
    }
    
    /** LPOP：从列表左侧弹出元素。 */
    @Override
    public byte[] lPop(byte[] key) {
        return write(key, ByteArrayCodec.INSTANCE, RedisCommands.LPOP, key);
    }

    /** RPOP：从列表右侧弹出元素。 */
    @Override
    public byte[] rPop(byte[] key) {
        return write(key, ByteArrayCodec.INSTANCE, RedisCommands.RPOP, key);
    }

    /** BLPOP：阻塞式从左侧弹出。 */
    @Override
    public List<byte[]> bLPop(int timeout, byte[]... keys) {
        List<Object> params = new ArrayList<Object>(keys.length + 1);
        params.addAll(Arrays.asList(keys));
        params.add(timeout);
        return write(keys[0], ByteArrayCodec.INSTANCE, RedisCommands.BLPOP, params.toArray());
    }

    /** BRPOP：阻塞式从右侧弹出。 */
    @Override
    public List<byte[]> bRPop(int timeout, byte[]... keys) {
        List<Object> params = new ArrayList<Object>(keys.length + 1);
        params.addAll(Arrays.asList(keys));
        params.add(timeout);
        return write(keys[0], ByteArrayCodec.INSTANCE, RedisCommands.BRPOP, params.toArray());
    }

    /** RPOPLPUSH：从源列表弹出并推入目标列表。 */
    @Override
    public byte[] rPopLPush(byte[] srcKey, byte[] dstKey) {
        return write(srcKey, ByteArrayCodec.INSTANCE, RedisCommands.RPOPLPUSH, srcKey, dstKey);
    }

    /** BRPOPLPUSH：阻塞式 RPOPLPUSH。 */
    @Override
    public byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey) {
        return write(srcKey, ByteArrayCodec.INSTANCE, RedisCommands.BRPOPLPUSH, srcKey, dstKey, timeout);
    }

    private static final RedisCommand<Long> SADD = new RedisCommand<Long>("SADD");
    
    /** SADD：向集合添加 member。 */
    @Override
    public Long sAdd(byte[] key, byte[]... values) {
        List<Object> args = new ArrayList<Object>(values.length + 1);
        args.add(key);
        args.addAll(Arrays.asList(values));
        
        return write(key, StringCodec.INSTANCE, SADD, args.toArray());
    }

    private static final RedisStrictCommand<Long> SREM = new RedisStrictCommand<Long>("SREM");
    
    /** SREM：从集合移除 member。 */
    @Override
    public Long sRem(byte[] key, byte[]... values) {
        List<Object> args = new ArrayList<Object>(values.length + 1);
        args.add(key);
        args.addAll(Arrays.asList(values));
        
        return write(key, StringCodec.INSTANCE, SREM, args.toArray());
    }

    /** SPOP：随机弹出集合 member。 */
    @Override
    public byte[] sPop(byte[] key) {
        return write(key, ByteArrayCodec.INSTANCE, RedisCommands.SPOP_SINGLE, key);
    }

    private static final RedisCommand<List<Object>> SPOP = new RedisCommand<List<Object>>("SPOP", new ObjectListReplayDecoder<Object>());
    
    /** SPOP：随机弹出集合 member。 */
    @Override
    public List<byte[]> sPop(byte[] key, long count) {
        return write(key, ByteArrayCodec.INSTANCE, SPOP, key, count);
    }
    
    /** SMOVE：将 member 从源集合移动到目标集合。 */
    @Override
    public Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value) {
        return write(srcKey, StringCodec.INSTANCE, RedisCommands.SMOVE, srcKey, destKey, value);
    }

    private static final RedisStrictCommand<Long> SCARD = new RedisStrictCommand<Long>("SCARD");
    
    /** SCARD：返回集合基数。 */
    @Override
    public Long sCard(byte[] key) {
        return read(key, StringCodec.INSTANCE, SCARD, key);
    }

    /** SISMEMBER：判断 member 是否在集合中。 */
    @Override
    public Boolean sIsMember(byte[] key, byte[] value) {
        return read(key, StringCodec.INSTANCE, RedisCommands.SISMEMBER, key, value);
    }

    /** SINTER：返回集合交集。 */
    @Override
    public Set<byte[]> sInter(byte[]... keys) {
        return write(keys[0], ByteArrayCodec.INSTANCE, RedisCommands.SINTER, Arrays.asList(keys).toArray());
    }

    /** SINTERSTORE：将交集写入目标 key。 */
    @Override
    public Long sInterStore(byte[] destKey, byte[]... keys) {
        List<Object> args = new ArrayList<Object>(keys.length + 1);
        args.add(destKey);
        args.addAll(Arrays.asList(keys));
        return write(keys[0], StringCodec.INSTANCE, RedisCommands.SINTERSTORE, args.toArray());
    }

    /** SUNION：返回集合并集。 */
    @Override
    public Set<byte[]> sUnion(byte[]... keys) {
        return write(keys[0], ByteArrayCodec.INSTANCE, RedisCommands.SUNION, Arrays.asList(keys).toArray());
    }

    /** SUNIONSTORE：将并集写入目标 key。 */
    @Override
    public Long sUnionStore(byte[] destKey, byte[]... keys) {
        List<Object> args = new ArrayList<Object>(keys.length + 1);
        args.add(destKey);
        args.addAll(Arrays.asList(keys));
        return write(keys[0], StringCodec.INSTANCE, RedisCommands.SUNIONSTORE, args.toArray());
    }

    /** SDIFF：返回集合差集。 */
    @Override
    public Set<byte[]> sDiff(byte[]... keys) {
        return write(keys[0], ByteArrayCodec.INSTANCE, RedisCommands.SDIFF, Arrays.asList(keys).toArray());
    }

    /** SDIFFSTORE：将差集写入目标 key。 */
    @Override
    public Long sDiffStore(byte[] destKey, byte[]... keys) {
        List<Object> args = new ArrayList<Object>(keys.length + 1);
        args.add(destKey);
        args.addAll(Arrays.asList(keys));
        return write(keys[0], StringCodec.INSTANCE, RedisCommands.SDIFFSTORE, args.toArray());
    }

    /** SMEMBERS：返回集合全部 member。 */
    @Override
    public Set<byte[]> sMembers(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.SMEMBERS, key);
    }

    /** SRANDMEMBER：随机返回集合 member。 */
    @Override
    public byte[] sRandMember(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.SRANDMEMBER_SINGLE, key);
    }

    private static final RedisCommand<List<Object>> SRANDMEMBER = new RedisCommand<>("SRANDMEMBER", new ObjectListReplayDecoder<>());

    /** SRANDMEMBER：随机返回集合 member。 */
    @Override
    public List<byte[]> sRandMember(byte[] key, long count) {
        return read(key, ByteArrayCodec.INSTANCE, SRANDMEMBER, key, count);
    }

    /** SSCAN：增量迭代集合 member。 */
    @Override
    public Cursor<byte[]> sScan(byte[] key, ScanOptions options) {
        return new KeyBoundCursor<byte[]>(key, 0, options) {

            private RedisClient client;

            @Override
            protected ScanIteration<byte[]> doScan(byte[] key, long cursorId, ScanOptions options) {
                if (isQueueing() || isPipelined()) {
                    throw new UnsupportedOperationException("'SSCAN' cannot be called in pipeline / transaction mode.");
                }

                List<Object> args = new ArrayList<Object>();
                args.add(key);
                args.add(Long.toUnsignedString(cursorId));
                if (options.getPattern() != null) {
                    args.add("MATCH");
                    args.add(options.getPattern());
                }
                if (options.getCount() != null) {
                    args.add("COUNT");
                    args.add(options.getCount());
                }
                
                RFuture<ListScanResult<byte[]>> f = executorService.readAsync(client, key, ByteArrayCodec.INSTANCE, RedisCommands.SSCAN, args.toArray());
                ListScanResult<byte[]> res = syncFuture(f);
                client = res.getRedisClient();
                return new ScanIteration<byte[]>(Long.parseUnsignedLong(res.getPos()), res.getValues());
            }
        }.open();
    }

    /** ZADD：向有序集合添加 member 及 score。 */
    @Override
    public Boolean zAdd(byte[] key, double score, byte[] value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.ZADD_BOOL, key, BigDecimal.valueOf(score).toPlainString(), value);
    }

    /** ZADD：向有序集合添加 member 及 score。 */
    @Override
    public Long zAdd(byte[] key, Set<Tuple> tuples) {
        List<Object> params = new ArrayList<Object>(tuples.size()*2+1);
        params.add(key);
        for (Tuple entry : tuples) {
            params.add(BigDecimal.valueOf(entry.getScore()).toPlainString());
            params.add(entry.getValue());
        }
        return write(key, StringCodec.INSTANCE, RedisCommands.ZADD, params.toArray());
    }

    /** ZREM：从有序集合移除 member。 */
    @Override
    public Long zRem(byte[] key, byte[]... values) {
        List<Object> params = new ArrayList<Object>(values.length+1);
        params.add(key);
        params.addAll(Arrays.asList(values));

        return write(key, StringCodec.INSTANCE, RedisCommands.ZREM_LONG, params.toArray());
    }

    /** ZINCRBY：member 的 score 按增量更新。 */
    @Override
    public Double zIncrBy(byte[] key, double increment, byte[] value) {
        return write(key, DoubleCodec.INSTANCE, RedisCommands.ZINCRBY,
                            key, new BigDecimal(increment).toPlainString(), value);
    }

    /** ZRANK/ZREVRANK：返回 member 排名。 */
    @Override
    public Long zRank(byte[] key, byte[] value) {
        return read(key, StringCodec.INSTANCE, RedisCommands.ZRANK, key, value);
    }

    /** ZREVRANK：返回 member 逆序排名。 */
    @Override
    public Long zRevRank(byte[] key, byte[] value) {
        return read(key, StringCodec.INSTANCE, RedisCommands.ZREVRANK, key, value);
    }

    private static final RedisCommand<Set<Object>> ZRANGE = new RedisCommand<Set<Object>>("ZRANGE", new ObjectSetReplayDecoder<Object>());
    
    /** ZRANGE：按 rank 区间返回 member 与 score 流。 */
    @Override
    public Set<byte[]> zRange(byte[] key, long start, long end) {
        return read(key, ByteArrayCodec.INSTANCE, ZRANGE, key, start, end);
    }

    private static final RedisCommand<Set<Tuple>> ZRANGE_ENTRY = new RedisCommand<Set<Tuple>>("ZRANGE", new ScoredSortedSetReplayDecoder());
    
    private static final RedisCommand<Set<Tuple>> ZRANGE_ENTRY_V2 = new RedisCommand<Set<Tuple>>("ZRANGE",
            new ListMultiDecoder2(new ObjectSetReplayDecoder(), new ScoredSortedSetReplayDecoderV2()));
    /** ZRANGE WITHSCORES：正序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRangeWithScores(byte[] key, long start, long end) {
        if (executorService.getServiceManager().isResp3()) {
            return read(key, ByteArrayCodec.INSTANCE, ZRANGE_ENTRY_V2, key, start, end, "WITHSCORES");
        }
        return read(key, ByteArrayCodec.INSTANCE, ZRANGE_ENTRY, key, start, end, "WITHSCORES");
    }

    /** value：Redis 命令实现。 */
    private String value(Range.Boundary boundary, String defaultValue) {
        if (boundary == null) {
            return defaultValue;
        }
        Object score = boundary.getValue();
        if (score == null) {
            return defaultValue;
        }
        StringBuilder element = new StringBuilder();
        if (!boundary.isIncluding()) {
            element.append("(");
        } else {
            if (!(score instanceof Double)) {
                element.append("[");
            }
        }
        if (score instanceof Double) {
            if (Double.isInfinite((Double) score)) {
                element.append((Double)score > 0 ? "+inf" : "-inf");
            } else {
                element.append(BigDecimal.valueOf((Double)score).toPlainString());
            }
        } else {
            element.append(score);
        }
        return element.toString();
    }

    /** ZRANGEBYSCORE：按 score 区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByScore(byte[] key, double min, double max) {
        return zRangeByScore(key, new Range().gte(min).lte(max));
    }

    /** ZRANGEBYSCORE WITHSCORES：正序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRangeByScoreWithScores(byte[] key, Range range) {
        return zRangeByScoreWithScores(key, range, null);
    }

    /** ZRANGEBYSCORE WITHSCORES：正序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max) {
        return zRangeByScoreWithScores(key, new Range().gte(min).lte(max));
    }

    /** ZRANGEBYSCORE：按 score 区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByScore(byte[] key, double min, double max, long offset, long count) {
        return zRangeByScore(key, new Range().gte(min).lte(max),
                new Limit().offset(Long.valueOf(offset).intValue()).count(Long.valueOf(count).intValue()));
    }

    /** ZRANGEBYSCORE WITHSCORES：正序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max, long offset, long count) {
        return zRangeByScoreWithScores(key, new Range().gte(min).lte(max),
                new Limit().offset(Long.valueOf(offset).intValue()).count(Long.valueOf(count).intValue()));
    }

    private static final RedisCommand<Set<Tuple>> ZRANGEBYSCORE = new RedisCommand<Set<Tuple>>("ZRANGEBYSCORE", new ScoredSortedSetReplayDecoder());

    private static final RedisCommand<Set<Tuple>> ZRANGEBYSCORE_V2 = new RedisCommand<Set<Tuple>>("ZRANGEBYSCORE",
            new ListMultiDecoder2(new ObjectSetReplayDecoder(), new ScoredSortedSetReplayDecoderV2()));

    /** ZRANGEBYSCORE WITHSCORES：正序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRangeByScoreWithScores(byte[] key, Range range, Limit limit) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");

        List<Object> args = new ArrayList<Object>();
        args.add(key);
        args.add(min);
        args.add(max);
        args.add("WITHSCORES");

        if (limit != null) {
            args.add("LIMIT");
            args.add(limit.getOffset());
            args.add(limit.getCount());
        }

        if (executorService.getServiceManager().isResp3()) {
            return read(key, ByteArrayCodec.INSTANCE, ZRANGEBYSCORE_V2, args.toArray());
        }
        return read(key, ByteArrayCodec.INSTANCE, ZRANGEBYSCORE, args.toArray());
    }

    private static final RedisCommand<Set<Object>> ZREVRANGE = new RedisCommand<Set<Object>>("ZREVRANGE", new ObjectSetReplayDecoder<Object>());

    /** ZREVRANGE：按 rank 区间逆序返回 member。 */
    @Override
    public Set<byte[]> zRevRange(byte[] key, long start, long end) {
        return read(key, ByteArrayCodec.INSTANCE, ZREVRANGE, key, start, end);
    }

    private static final RedisCommand<Set<Tuple>> ZREVRANGE_ENTRY = new RedisCommand<Set<Tuple>>("ZREVRANGE", new ScoredSortedSetReplayDecoder());

    private static final RedisCommand<Set<Tuple>> ZREVRANGE_ENTRY_V2 = new RedisCommand("ZREVRANGE",
            new ListMultiDecoder2(new ObjectSetReplayDecoder(), new ScoredSortedSetReplayDecoderV2()));

    /** ZREVRANGE WITHSCORES：逆序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRevRangeWithScores(byte[] key, long start, long end) {
        if (executorService.getServiceManager().isResp3()) {
            return read(key, ByteArrayCodec.INSTANCE, ZREVRANGE_ENTRY_V2, key, start, end, "WITHSCORES");
        }
        return read(key, ByteArrayCodec.INSTANCE, ZREVRANGE_ENTRY, key, start, end, "WITHSCORES");
    }

    /** ZREVRANGEBYSCORE：按 score 区间逆序返回 member。 */
    @Override
    public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max) {
        return zRevRangeByScore(key, new Range().gte(min).lte(max));
    }

    private static final RedisCommand<Set<byte[]>> ZREVRANGEBYSCORE = new RedisCommand<Set<byte[]>>("ZREVRANGEBYSCORE", new ObjectSetReplayDecoder<byte[]>());
    private static final RedisCommand<Set<Tuple>> ZREVRANGEBYSCOREWITHSCORES = new RedisCommand<Set<Tuple>>("ZREVRANGEBYSCORE", new ScoredSortedSetReplayDecoder());

    private static final RedisCommand<Set<Tuple>> ZREVRANGEBYSCOREWITHSCORES_V2 = new RedisCommand<Set<Tuple>>("ZREVRANGEBYSCORE",
            new ListMultiDecoder2(new ObjectSetReplayDecoder(), new ScoredSortedSetReplayDecoderV2()));

    /** ZREVRANGEBYSCORE：按 score 区间逆序返回 member。 */
    @Override
    public Set<byte[]> zRevRangeByScore(byte[] key, Range range) {
        return zRevRangeByScore(key, range, null);
    }

    /** ZREVRANGEBYSCORE WITHSCORES：逆序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min, double max) {
        return zRevRangeByScoreWithScores(key, new Range().gte(min).lte(max));
    }

    /** ZREVRANGEBYSCORE：按 score 区间逆序返回 member。 */
    @Override
    public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max, long offset, long count) {
        return zRevRangeByScore(key, new Range().gte(min).lte(max),
                new Limit().offset(Long.valueOf(offset).intValue()).count(Long.valueOf(count).intValue()));
    }

    /** ZREVRANGEBYSCORE：按 score 区间逆序返回 member。 */
    @Override
    public Set<byte[]> zRevRangeByScore(byte[] key, Range range, Limit limit) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");

        List<Object> args = new ArrayList<Object>();
        args.add(key);
        args.add(max);
        args.add(min);

        if (limit != null) {
            args.add("LIMIT");
            args.add(limit.getOffset());
            args.add(limit.getCount());
        }

        return read(key, ByteArrayCodec.INSTANCE, ZREVRANGEBYSCORE, args.toArray());
    }

    /** ZREVRANGEBYSCORE WITHSCORES：逆序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min, double max, long offset, long count) {
        return zRevRangeByScoreWithScores(key, new Range().gte(min).lte(max),
                new Limit().offset(Long.valueOf(offset).intValue()).count(Long.valueOf(count).intValue()));
    }

    /** ZREVRANGEBYSCORE WITHSCORES：逆序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, Range range) {
        return zRevRangeByScoreWithScores(key, range, null);
    }

    /** ZREVRANGEBYSCORE WITHSCORES：逆序返回 member 与 score。 */
    @Override
    public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, Range range, Limit limit) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");

        List<Object> args = new ArrayList<Object>();
        args.add(key);
        args.add(max);
        args.add(min);
        args.add("WITHSCORES");

        if (limit != null) {
            args.add("LIMIT");
            args.add(limit.getOffset());
            args.add(limit.getCount());
        }

        if (executorService.getServiceManager().isResp3()) {
            return read(key, ByteArrayCodec.INSTANCE, ZREVRANGEBYSCOREWITHSCORES_V2, args.toArray());
        }
        return read(key, ByteArrayCodec.INSTANCE, ZREVRANGEBYSCOREWITHSCORES, args.toArray());
    }

    /** ZCOUNT：统计 score 区间内 member 数。 */
    @Override
    public Long zCount(byte[] key, double min, double max) {
        return zCount(key, new Range().gte(min).lte(max));
    }

    private static final RedisStrictCommand<Long> ZCOUNT = new RedisStrictCommand<Long>("ZCOUNT");
    
    /** ZCOUNT：统计 score 区间内 member 数。 */
    @Override
    public Long zCount(byte[] key, Range range) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");
        return read(key, StringCodec.INSTANCE, ZCOUNT, key, min, max);
    }

    /** ZCARD：返回有序集合基数。 */
    @Override
    public Long zCard(byte[] key) {
        return read(key, StringCodec.INSTANCE, RedisCommands.ZCARD, key);
    }

    /** ZSCORE：返回 member 的 score。 */
    @Override
    public Double zScore(byte[] key, byte[] value) {
        return read(key, StringCodec.INSTANCE, RedisCommands.ZSCORE, key, value);
    }

    private static final RedisStrictCommand<Long> ZREMRANGEBYRANK = new RedisStrictCommand<Long>("ZREMRANGEBYRANK");
    private static final RedisStrictCommand<Long> ZREMRANGEBYSCORE = new RedisStrictCommand<Long>("ZREMRANGEBYSCORE");
    
    /** ZREMRANGEBYRANK：按 rank 区间删除 member。 */
    @Override
    public Long zRemRange(byte[] key, long start, long end) {
        return write(key, StringCodec.INSTANCE, ZREMRANGEBYRANK, key, start, end);
    }

    /** ZREMRANGEBYSCORE：按 score 区间删除 member。 */
    @Override
    public Long zRemRangeByScore(byte[] key, double min, double max) {
        return zRemRangeByScore(key, new Range().gte(min).lte(max));
    }

    /** ZREMRANGEBYSCORE：按 score 区间删除 member。 */
    @Override
    public Long zRemRangeByScore(byte[] key, Range range) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");
        return write(key, StringCodec.INSTANCE, ZREMRANGEBYSCORE, key, min, max);
    }

    /** ZUNIONSTORE：有序集合并集写入目标 key。 */
    @Override
    public Long zUnionStore(byte[] destKey, byte[]... sets) {
        return zUnionStore(destKey, null, (Weights)null, sets);
    }
    
    private static final RedisStrictCommand<Long> ZUNIONSTORE = new RedisStrictCommand<Long>("ZUNIONSTORE");

    /** ZUNIONSTORE：有序集合并集写入目标 key。 */
    @Override
    public Long zUnionStore(byte[] destKey, Aggregate aggregate, Weights weights, byte[]... sets) {
        List<Object> args = new ArrayList<Object>(sets.length*2 + 5);
        args.add(destKey);
        args.add(sets.length);
        args.addAll(Arrays.asList(sets));
        if (weights != null) {
            args.add("WEIGHTS");
            for (double weight : weights.toArray()) {
                args.add(BigDecimal.valueOf(weight).toPlainString());
            }
        }
        if (aggregate != null) {
            args.add("AGGREGATE");
            args.add(aggregate.name());
        }
        return write(destKey, LongCodec.INSTANCE, ZUNIONSTORE, args.toArray());
    }

    private static final RedisStrictCommand<Long> ZINTERSTORE = new RedisStrictCommand<Long>("ZINTERSTORE");
    
    /** ZINTERSTORE：有序集合交集写入目标 key。 */
    @Override
    public Long zInterStore(byte[] destKey, byte[]... sets) {
        return zInterStore(destKey, null, (Weights)null, sets);
    }

    /** ZINTERSTORE：有序集合交集写入目标 key。 */
    @Override
    public Long zInterStore(byte[] destKey, Aggregate aggregate, Weights weights, byte[]... sets) {
        List<Object> args = new ArrayList<Object>(sets.length*2 + 5);
        args.add(destKey);
        args.add(sets.length);
        args.addAll(Arrays.asList(sets));
        if (weights != null) {
            args.add("WEIGHTS");
            for (double weight : weights.toArray()) {
                args.add(BigDecimal.valueOf(weight).toPlainString());
            }
        }
        if (aggregate != null) {
            args.add("AGGREGATE");
            args.add(aggregate.name());
        }
        return write(destKey, StringCodec.INSTANCE, ZINTERSTORE, args.toArray());
    }

    private static final RedisCommand<ListScanResult<Object>> ZSCAN = new RedisCommand<>("ZSCAN", new ListMultiDecoder2(new ListScanResultReplayDecoder(), new ScoredSortedListReplayDecoder()));

    /** ZSCAN：增量扫描有序集合 member。 */
    @Override
    public Cursor<Tuple> zScan(byte[] key, ScanOptions options) {
        return new KeyBoundCursor<Tuple>(key, 0, options) {

            private RedisClient client;

            @Override
            protected ScanIteration<Tuple> doScan(byte[] key, long cursorId, ScanOptions options) {
                if (isQueueing() || isPipelined()) {
                    throw new UnsupportedOperationException("'ZSCAN' cannot be called in pipeline / transaction mode.");
                }

                List<Object> args = new ArrayList<Object>();
                args.add(key);
                args.add(Long.toUnsignedString(cursorId));
                if (options.getPattern() != null) {
                    args.add("MATCH");
                    args.add(options.getPattern());
                }
                if (options.getCount() != null) {
                    args.add("COUNT");
                    args.add(options.getCount());
                }
                
                RFuture<ListScanResult<Tuple>> f = executorService.readAsync(client, key, ByteArrayCodec.INSTANCE, ZSCAN, args.toArray());
                ListScanResult<Tuple> res = syncFuture(f);
                client = res.getRedisClient();
                return new ScanIteration<Tuple>(Long.parseUnsignedLong(res.getPos()), res.getValues());
            }
        }.open();
    }

    /** ZRANGEBYSCORE：按 score 区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByScore(byte[] key, String min, String max) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.ZRANGEBYSCORE, key, min, max);
    }

    /** ZRANGEBYSCORE：按 score 区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByScore(byte[] key, Range range) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.ZRANGEBYSCORE, key, min, max);
    }

    /** ZRANGEBYSCORE：按 score 区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByScore(byte[] key, String min, String max, long offset, long count) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.ZRANGEBYSCORE, key, min, max, "LIMIT", offset, count);
    }

    /** ZRANGEBYSCORE：按 score 区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByScore(byte[] key, Range range, Limit limit) {
        String min = value(range.getMin(), "-inf");
        String max = value(range.getMax(), "+inf");
        
        List<Object> args = new ArrayList<Object>();
        args.add(key);
        args.add(min);
        args.add(max);
        
        if (limit != null) {
            args.add("LIMIT");
            args.add(limit.getOffset());
            args.add(limit.getCount());
        }
        
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.ZRANGEBYSCORE, args.toArray());
    }

    /** ZRANGEBYLEX：按字典序区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByLex(byte[] key) {
        return zRangeByLex(key, Range.unbounded());
    }
    
    private static final RedisCommand<Set<Object>> ZRANGEBYLEX = new RedisCommand<Set<Object>>("ZRANGEBYLEX", new ObjectSetReplayDecoder<Object>());

    /** ZRANGEBYLEX：按字典序区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByLex(byte[] key, Range range) {
        List<Object> params = new ArrayList<Object>();
        params.add(key);
        if (range.getMin() != null) {
            String min = value(range.getMin(), "-");
            params.add(min);
        } else {
            params.add("-");
        }
        if (range.getMax() != null) {
            String max = value(range.getMax(), "+");
            params.add(max);
        } else {
            params.add("+");
        }
        return read(key, ByteArrayCodec.INSTANCE, ZRANGEBYLEX, params.toArray());
    }

    /** ZRANGEBYLEX：按字典序区间返回 member 流。 */
    @Override
    public Set<byte[]> zRangeByLex(byte[] key, Range range, Limit limit) {
        String min = value(range.getMin(), "-");
        String max = value(range.getMax(), "+");
        
        List<Object> args = new ArrayList<Object>();
        args.add(key);
        args.add(min);
        args.add(max);
        
        if (limit != null) {
            args.add("LIMIT");
            args.add(limit.getOffset());
            args.add(limit.getCount());
        }
        
        return read(key, ByteArrayCodec.INSTANCE, ZRANGEBYLEX, args.toArray());
    }

    /** HSET：写入 hash 字段。 */
    @Override
    public Boolean hSet(byte[] key, byte[] field, byte[] value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.HSET, key, field, value);
    }

    /** HSETNX：仅当字段不存在时写入 hash。 */
    @Override
    public Boolean hSetNX(byte[] key, byte[] field, byte[] value) {
        return write(key, StringCodec.INSTANCE, RedisCommands.HSETNX, key, field, value);
    }

    /** HGET：读取 hash 单个字段。 */
    @Override
    public byte[] hGet(byte[] key, byte[] field) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.HGET, key, field);
    }

    private static final RedisCommand<List<Object>> HMGET = new RedisCommand<List<Object>>("HMGET", new ObjectListReplayDecoder<Object>());
    
    /** HMGET：批量读取 hash 字段。 */
    @Override
    public List<byte[]> hMGet(byte[] key, byte[]... fields) {
        List<Object> args = new ArrayList<Object>(fields.length + 1);
        args.add(key);
        args.addAll(Arrays.asList(fields));
        return read(key, ByteArrayCodec.INSTANCE, HMGET, args.toArray());
    }

    /** HMSET：批量写入 hash 字段。 */
    @Override
    public void hMSet(byte[] key, Map<byte[], byte[]> hashes) {
        List<Object> params = new ArrayList<Object>(hashes.size()*2 + 1);
        params.add(key);
        for (Map.Entry<byte[], byte[]> entry : hashes.entrySet()) {
            params.add(entry.getKey());
            params.add(entry.getValue());
        }

        write(key, StringCodec.INSTANCE, RedisCommands.HMSET, params.toArray());
    }
    
    private static final RedisCommand<Long> HINCRBY = new RedisCommand<Long>("HINCRBY");

    /** HINCRBY：hash 字段按整数自增。 */
    @Override
    public Long hIncrBy(byte[] key, byte[] field, long delta) {
        return write(key, StringCodec.INSTANCE, HINCRBY, key, field, delta);
    }
    
    private static final RedisCommand<Double> HINCRBYFLOAT = new RedisCommand<Double>("HINCRBYFLOAT", new DoubleReplayConvertor());

    /** HINCRBY：hash 字段按整数自增。 */
    @Override
    public Double hIncrBy(byte[] key, byte[] field, double delta) {
        return write(key, StringCodec.INSTANCE, HINCRBYFLOAT, key, field, BigDecimal.valueOf(delta).toPlainString());
    }

    /** HEXISTS：判断 hash 字段是否存在。 */
    @Override
    public Boolean hExists(byte[] key, byte[] field) {
        return read(key, StringCodec.INSTANCE, RedisCommands.HEXISTS, key, field);
    }

    /** HDEL：删除 hash 字段。 */
    @Override
    public Long hDel(byte[] key, byte[]... fields) {
        List<Object> args = new ArrayList<Object>(fields.length + 1);
        args.add(key);
        args.addAll(Arrays.asList(fields));
        return write(key, StringCodec.INSTANCE, RedisCommands.HDEL, args.toArray());
    }
    
    private static final RedisStrictCommand<Long> HLEN = new RedisStrictCommand<Long>("HLEN");

    /** HLEN：返回 hash 字段数量。 */
    @Override
    public Long hLen(byte[] key) {
        return read(key, StringCodec.INSTANCE, HLEN, key);
    }

    /** HKEYS：返回 hash 全部字段名。 */
    @Override
    public Set<byte[]> hKeys(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.HKEYS, key);
    }

    /** HVALS：返回 hash 全部字段值。 */
    @Override
    public List<byte[]> hVals(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.HVALS, key);
    }

    /** hGetAll：Redis 命令实现。 */
    @Override
    public Map<byte[], byte[]> hGetAll(byte[] key) {
        return read(key, ByteArrayCodec.INSTANCE, RedisCommands.HGETALL, key);
    }

    /** hScan：Redis 命令实现。 */
    @Override
    public Cursor<Entry<byte[], byte[]>> hScan(byte[] key, ScanOptions options) {
        return new KeyBoundCursor<Entry<byte[], byte[]>>(key, 0, options) {

            private RedisClient client;

            @Override
            protected ScanIteration<Entry<byte[], byte[]>> doScan(byte[] key, long cursorId, ScanOptions options) {
                if (isQueueing() || isPipelined()) {
                    throw new UnsupportedOperationException("'HSCAN' cannot be called in pipeline / transaction mode.");
                }

                List<Object> args = new ArrayList<Object>();
                args.add(key);
                args.add(Long.toUnsignedString(cursorId));
                if (options.getPattern() != null) {
                    args.add("MATCH");
                    args.add(options.getPattern());
                }
                if (options.getCount() != null) {
                    args.add("COUNT");
                    args.add(options.getCount());
                }
                
                RFuture<MapScanResult<byte[], byte[]>> f = executorService.readAsync(client, key, ByteArrayCodec.INSTANCE, RedisCommands.HSCAN, args.toArray());
                MapScanResult<byte[], byte[]> res = syncFuture(f);
                client = res.getRedisClient();
                return new ScanIteration<Entry<byte[], byte[]>>(Long.parseUnsignedLong(res.getPos()), res.getValues());
            }
        }.open();
    }

    /** MULTI：开启事务。 */
    @Override
    public void multi() {
        if (isQueueing()) {
            return;
        }

        if (isPipelined()) {
            BatchOptions options = BatchOptions.defaults()
                    .executionMode(ExecutionMode.IN_MEMORY_ATOMIC);
            this.executorService = executorService.createCommandBatchService(options);
            return;
        }
        
        BatchOptions options = BatchOptions.defaults()
            .executionMode(ExecutionMode.REDIS_WRITE_ATOMIC);
        this.executorService = executorService.createCommandBatchService(options);
    }

    /** EXEC：执行事务命令队列。 */
    @Override
    public List<Object> exec() {
        if (isPipelinedAtomic()) {
            return null;
        }
        if (isQueueing()) {
            try {
                BatchResult<?> result = ((CommandBatchService)executorService).execute();
                filterResults(result);
                return (List<Object>) result.getResponses();
            } catch (Exception ex) {
                throw transform(ex);
            } finally {
                resetConnection();
            }
        } else {
            throw new InvalidDataAccessApiUsageException("Not in transaction mode. Please invoke multi method");
        }
    }

    /** 按过滤器筛选命令结果。 */
    protected void filterResults(BatchResult<?> result) {
        if (result.getResponses().isEmpty()) {
            return;
        }

        int t = 0;
        for (Integer index : indexToRemove) {
            index -= t;
            result.getResponses().remove((int)index);
            t++;
        }
        for (ListIterator<Object> iterator = (ListIterator<Object>) result.getResponses().listIterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof String) {
                iterator.set(((String) object).getBytes());
            }
        }
    }

    /** 重置底层连接状态。 */
    protected void resetConnection() {
        executorService = this.redisson.getCommandExecutor();
        index = -1;
        indexToRemove.clear();
    }

    /** DISCARD：放弃事务。 */
    @Override
    public void discard() {
        if (isQueueing()) {
            syncFuture(executorService.writeAsync(null, RedisCommands.DISCARD));
            resetConnection();
        } else {
            throw new InvalidDataAccessApiUsageException("Not in transaction mode. Please invoke multi method");
        }
    }

    /** WATCH：监视 key 以支持乐观事务。 */
    @Override
    public void watch(byte[]... keys) {
        if (isQueueing()) {
            throw new UnsupportedOperationException();
        }

        syncFuture(executorService.writeAsync(null, RedisCommands.WATCH, keys));
    }

    /** UNWATCH：取消全部 WATCH。 */
    @Override
    public void unwatch() {
        syncFuture(executorService.writeAsync(null, RedisCommands.UNWATCH));
    }

    /** 是否处于 Pub/Sub 订阅状态。 */
    @Override
    public boolean isSubscribed() {
        return subscription != null && subscription.isAlive();
    }

    /** 返回 Pub/Sub 订阅对象。 */
    @Override
    public Subscription getSubscription() {
        return subscription;
    }

    /** PUBLISH：向频道发布消息。 */
    @Override
    public Long publish(byte[] channel, byte[] message) {
        return write(channel, StringCodec.INSTANCE, RedisCommands.PUBLISH, channel, message);
    }

    /** SUBSCRIBE：订阅频道。 */
    @Override
    public void subscribe(MessageListener listener, byte[]... channels) {
        checkSubscription();
        
        subscription = new RedissonSubscription(redisson.getCommandExecutor(), listener);
        subscription.subscribe(channels);
    }

    /** checkSubscription：Redis 命令实现。 */
    private void checkSubscription() {
        if (subscription != null) {
            throw new RedisSubscribedConnectionException("Connection already subscribed");
        }
        
        if (isQueueing()) {
            throw new UnsupportedOperationException("Not supported in queueing mode");
        }
        if (isPipelined()) {
            throw new UnsupportedOperationException("Not supported in pipelined mode");
        }
    }

    /** PSUBSCRIBE：按模式订阅频道。 */
    @Override
    public void pSubscribe(MessageListener listener, byte[]... patterns) {
        checkSubscription();
        
        subscription = new RedissonSubscription(redisson.getCommandExecutor(), listener);
        subscription.pSubscribe(patterns);
    }

    /** SELECT：切换数据库编号。 */
    @Override
    public void select(int dbIndex) {
        throw new UnsupportedOperationException();
    }

    private static final RedisCommand<Object> ECHO = new RedisCommand<Object>("ECHO");
    
    /** ECHO：回显字符串。 */
    @Override
    public byte[] echo(byte[] message) {
        return read(null, ByteArrayCodec.INSTANCE, ECHO, message);
    }

    /** PING：测试连接可用性。 */
    @Override
    public String ping() {
        return read(null, StringCodec.INSTANCE, RedisCommands.PING);
    }
    
    /** BGWRITEAOF：后台写入 AOF。 */
    @Override
    public void bgWriteAof() {
        throw new UnsupportedOperationException();
    }

    /** BGREWRITEAOF：后台重写 AOF 文件。 */
    @Override
    public void bgReWriteAof() {
        write(null, StringCodec.INSTANCE, RedisCommands.BGREWRITEAOF);
    }
    
    /** BGSAVE：后台异步保存 RDB 快照。 */
    @Override
    public void bgSave() {
        write(null, StringCodec.INSTANCE, RedisCommands.BGSAVE);
    }
    
    /** LASTSAVE：返回上次成功保存的时间戳。 */
    @Override
    public Long lastSave() {
        return write(null, StringCodec.INSTANCE, RedisCommands.LASTSAVE);
    }
    
    private static final RedisStrictCommand<Void> SAVE = new RedisStrictCommand<Void>("SAVE", new VoidReplayConvertor());

    /** SAVE：同步保存 RDB 快照。 */
    @Override
    public void save() {
        write(null, StringCodec.INSTANCE, SAVE);
    }

    /** DBSIZE：返回当前库 key 数量。 */
    @Override
    public Long dbSize() {
        if (isQueueing()) {
            return read(null, StringCodec.INSTANCE, RedisCommands.DBSIZE);
        }

        List<CompletableFuture<Long>> futures = executorService.readAllAsync(RedisCommands.DBSIZE);
        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<Long> s = f.thenApply(r -> futures.stream().mapToLong(v -> v.getNow(0L)).sum());
        CompletableFutureWrapper<Long> ff = new CompletableFutureWrapper<>(s);
        return sync(ff);
    }

    /** FLUSHDB：清空当前数据库。 */
    @Override
    public void flushDb() {
        if (isQueueing() || isPipelined()) {
            write(null, StringCodec.INSTANCE, RedisCommands.FLUSHDB);
            return;
        }
        
        RFuture<Void> f = executorService.writeAllVoidAsync(RedisCommands.FLUSHDB);
        sync(f);
    }

    /** FLUSHALL：清空全部数据库。 */
    @Override
    public void flushAll() {
        RFuture<Void> f = executorService.writeAllVoidAsync(RedisCommands.FLUSHALL);
        sync(f);
    }

    private static final RedisStrictCommand<Properties> INFO_DEFAULT = new RedisStrictCommand<Properties>("INFO", "DEFAULT", new ObjectDecoder(new PropertiesDecoder()));
    private static final RedisStrictCommand<Properties> INFO = new RedisStrictCommand<Properties>("INFO", new ObjectDecoder(new PropertiesDecoder()));
    
    /** INFO：返回服务器/统计信息。 */
    @Override
    public Properties info() {
        return read(null, StringCodec.INSTANCE, INFO_DEFAULT);
    }

    /** INFO：返回服务器/统计信息。 */
    @Override
    public Properties info(String section) {
        return read(null, StringCodec.INSTANCE, INFO, section);
    }

    /** SHUTDOWN：关闭 Redis 服务器。 */
    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    /** SHUTDOWN：关闭 Redis 服务器。 */
    @Override
    public void shutdown(ShutdownOption option) {
        throw new UnsupportedOperationException();
    }

    private static final RedisStrictCommand<Properties> CONFIG_GET = new RedisStrictCommand<Properties>("CONFIG", "GET", new PropertiesListDecoder());
    
    /** CONFIG GET：读取配置项。 */
    @Override
    public Properties getConfig(String pattern) {
        return read(null, StringCodec.INSTANCE, CONFIG_GET, pattern);
    }

    /** CONFIG SET：设置 Redis 配置项。 */
    @Override
    public void setConfig(String param, String value) {
        write(null, StringCodec.INSTANCE, RedisCommands.CONFIG_SET, param, value);
    }

    /** CONFIG RESETSTAT：重置统计计数。 */
    @Override
    public void resetConfigStats() {
        write(null, StringCodec.INSTANCE, RedisCommands.CONFIG_RESETSTAT);
    }

    private static final RedisStrictCommand<Long> TIME = new RedisStrictCommand<Long>("TIME", new TimeLongObjectDecoder());
    
    /** TIME：返回服务器当前时间。 */
    @Override
    public Long time() {
        return read(null, LongCodec.INSTANCE, TIME);
    }

    /** CLIENT KILL：断开指定客户端连接。 */
    @Override
    public void killClient(String host, int port) {
        throw new UnsupportedOperationException();
    }

    /** CLIENT SETNAME：设置当前连接名称。 */
    @Override
    public void setClientName(byte[] name) {
        throw new UnsupportedOperationException("Should be defined through Redisson Config object");
    }

    /** CLIENT GETNAME：获取当前连接名称。 */
    @Override
    public String getClientName() {
        throw new UnsupportedOperationException();
    }

    /** CLIENT LIST：返回客户端连接列表。 */
    @Override
    public List<RedisClientInfo> getClientList() {
        return read(null, StringCodec.INSTANCE, RedisCommands.CLIENT_LIST);
    }

    /** SLAVEOF：配置当前节点为指定 master 的从节点。 */
    @Override
    public void slaveOf(String host, int port) {
        throw new UnsupportedOperationException();
    }

    /** SLAVEOF NO ONE：将从节点提升为独立 master。 */
    @Override
    public void slaveOfNoOne() {
        throw new UnsupportedOperationException();
    }

    /** MIGRATE：将 key 迁移到另一 Redis 实例。 */
    @Override
    public void migrate(byte[] key, RedisNode target, int dbIndex, MigrateOption option) {
        migrate(key, target, dbIndex, option, Long.MAX_VALUE);
    }

    /** MIGRATE：将 key 迁移到另一 Redis 实例。 */
    @Override
    public void migrate(byte[] key, RedisNode target, int dbIndex, MigrateOption option, long timeout) {
        write(key, StringCodec.INSTANCE, RedisCommands.MIGRATE, target.getHost(), target.getPort(), key, dbIndex, timeout);
    }

    /** SCRIPT FLUSH：清空脚本缓存。 */
    @Override
    public void scriptFlush() {
        if (isQueueing() || isPipelined()) {
            throw new UnsupportedOperationException();
        }

        RFuture<Void> f = executorService.writeAllVoidAsync(RedisCommands.SCRIPT_FLUSH);
        sync(f);
    }

    /** SCRIPT KILL：终止正在执行的脚本。 */
    @Override
    public void scriptKill() {
        throw new UnsupportedOperationException();
    }

    /** SCRIPT LOAD：加载 Lua 脚本并返回 SHA1。 */
    @Override
    public String scriptLoad(byte[] script) {
        if (isQueueing()) {
            throw new UnsupportedOperationException();
        }
        if (isPipelined()) {
            throw new UnsupportedOperationException();
        }

        List<CompletableFuture<String>> futures = executorService.executeAllAsync(RedisCommands.SCRIPT_LOAD, (Object)script);
        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<String> s = f.thenApply(r -> futures.get(0).getNow(null));
        return sync(new CompletableFutureWrapper<>(s));
    }

    /** SCRIPT EXISTS：检查脚本 SHA1 是否已缓存。 */
    @Override
    public List<Boolean> scriptExists(final String... scriptShas) {
        if (isQueueing() || isPipelined()) {
            throw new UnsupportedOperationException();
        }

        List<CompletableFuture<List<Boolean>>> futures = executorService.writeAllAsync(RedisCommands.SCRIPT_EXISTS, (Object[]) scriptShas);
        CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Boolean>> s = f.thenApply(r -> {
            List<Boolean> result = futures.get(0).getNow(new ArrayList<>());
            for (CompletableFuture<List<Boolean>> future : futures.subList(1, futures.size())) {
                List<Boolean> l = future.getNow(new ArrayList<>());
                for (int i = 0; i < l.size(); i++) {
                    result.set(i, result.get(i) | l.get(i));
                }
            }
            return result;
        });
        return sync(new CompletableFutureWrapper<>(s));
    }

    /** eval：Redis 命令实现。 */
    @Override
    public <T> T eval(byte[] script, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
        if (isQueueing()) {
            throw new UnsupportedOperationException();
        }
        if (isPipelined()) {
            throw new UnsupportedOperationException();
        }

        RedisCommand<?> c = toCommand(returnType, "EVAL");
        List<Object> params = new ArrayList<Object>();
        params.add(script);
        params.add(numKeys);
        params.addAll(Arrays.asList(keysAndArgs));

        byte[] key = getKey(numKeys, keysAndArgs);
        return write(key, ByteArrayCodec.INSTANCE, c, params.toArray());
    }

    /** 将 Spring 命令对象转为 Redis 参数。 */
    protected RedisCommand<?> toCommand(ReturnType returnType, String name) {
        RedisCommand<?> c = null;
        if (returnType == ReturnType.BOOLEAN) {
            c = org.redisson.api.RScript.ReturnType.BOOLEAN.getCommand();
        } else if (returnType == ReturnType.INTEGER) {
            c = org.redisson.api.RScript.ReturnType.LONG.getCommand();
        } else if (returnType == ReturnType.MULTI) {
            c = org.redisson.api.RScript.ReturnType.LIST.getCommand();
            return new RedisCommand(c, name, new BinaryConvertor());
        } else if (returnType == ReturnType.STATUS) {
            c = org.redisson.api.RScript.ReturnType.STRING.getCommand();
        } else if (returnType == ReturnType.VALUE) {
            c = org.redisson.api.RScript.ReturnType.VALUE.getCommand();
            return new RedisCommand(c, name, new BinaryConvertor());
        }
        return new RedisCommand(c, name);
    }

    /** evalSha：Redis 命令实现。 */
    @Override
    public <T> T evalSha(String scriptSha, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
        if (isQueueing()) {
            throw new UnsupportedOperationException();
        }
        if (isPipelined()) {
            throw new UnsupportedOperationException();
        }

        RedisCommand<?> c = toCommand(returnType, "EVALSHA");
        List<Object> params = new ArrayList<Object>();
        params.add(scriptSha);
        params.add(numKeys);
        params.addAll(Arrays.asList(keysAndArgs));

        byte[] key = getKey(numKeys, keysAndArgs);
        return write(key, ByteArrayCodec.INSTANCE, c, params.toArray());
    }

    /** evalSha：Redis 命令实现。 */
    @Override
    public <T> T evalSha(byte[] scriptSha, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
        RedisCommand<?> c = toCommand(returnType, "EVALSHA");
        List<Object> params = new ArrayList<Object>();
        params.add(scriptSha);
        params.add(numKeys);
        params.addAll(Arrays.asList(keysAndArgs));

        byte[] key = getKey(numKeys, keysAndArgs);
        return write(key, ByteArrayCodec.INSTANCE, c, params.toArray());
    }

    /** getKey：Redis 命令实现。 */
    private static byte[] getKey(int numKeys, byte[][] keysAndArgs) {
        if (numKeys > 0 && keysAndArgs.length > 0) {
            return keysAndArgs[0];
        }
        return null;
    }

    /** GEOADD：向 geo 集合添加坐标点。 */
    @Override
    public Long geoAdd(byte[] key, Point point, byte[] member) {
        return write(key, StringCodec.INSTANCE, RedisCommands.GEOADD, key, point.getX(), point.getY(), member);
    }

    /** GEOADD：向 geo 集合添加坐标点。 */
    @Override
    public Long geoAdd(byte[] key, GeoLocation<byte[]> location) {
        return write(key, StringCodec.INSTANCE, RedisCommands.GEOADD, key, location.getPoint().getX(), location.getPoint().getY(), location.getName());
    }

    /** GEOADD：向 geo 集合添加坐标点。 */
    @Override
    public Long geoAdd(byte[] key, Map<byte[], Point> memberCoordinateMap) {
        List<Object> params = new ArrayList<Object>(memberCoordinateMap.size()*3 + 1);
        params.add(key);
        for (Entry<byte[], Point> entry : memberCoordinateMap.entrySet()) {
            params.add(entry.getValue().getX());
            params.add(entry.getValue().getY());
            params.add(entry.getKey());
        }
        return write(key, StringCodec.INSTANCE, RedisCommands.GEOADD, params.toArray());
    }

    /** GEOADD：向 geo 集合添加坐标点。 */
    @Override
    public Long geoAdd(byte[] key, Iterable<GeoLocation<byte[]>> locations) {
        List<Object> params = new ArrayList<Object>();
        params.add(key);
        for (GeoLocation<byte[]> location : locations) {
            params.add(location.getPoint().getX());
            params.add(location.getPoint().getY());
            params.add(location.getName());
        }
        return write(key, StringCodec.INSTANCE, RedisCommands.GEOADD, params.toArray());
    }

    /** GEODIST：计算两点间距离。 */
    @Override
    public Distance geoDist(byte[] key, byte[] member1, byte[] member2) {
        return geoDist(key, member1, member2, DistanceUnit.METERS);
    }

    /** GEODIST：计算两点间距离。 */
    @Override
    public Distance geoDist(byte[] key, byte[] member1, byte[] member2, Metric metric) {
        return read(key, DoubleCodec.INSTANCE, new RedisCommand<Distance>("GEODIST", new DistanceConvertor(metric)), key, member1, member2, getAbbreviation(metric));
    }

    private static final RedisCommand<List<Object>> GEOHASH = new RedisCommand<List<Object>>("GEOHASH", new ObjectListReplayDecoder<Object>());

    /** GEOHASH：返回坐标的 geohash 字符串。 */
    @Override
    public List<String> geoHash(byte[] key, byte[]... members) {
        List<Object> params = new ArrayList<Object>(members.length + 1);
        params.add(key);
        for (byte[] member : members) {
            params.add(member);
        }
        return read(key, StringCodec.INSTANCE, GEOHASH, params.toArray());
    }

    private final MultiDecoder<Map<Object, Object>> geoDecoder = new ListMultiDecoder2(new ObjectListReplayDecoder2(), new PointDecoder());

    /** GEOPOS：返回 member 的经纬度。 */
    @Override
    public List<Point> geoPos(byte[] key, byte[]... members) {
        List<Object> params = new ArrayList<Object>(members.length + 1);
        params.add(key);
        params.addAll(Arrays.asList(members));
        
        RedisCommand<Map<Object, Object>> command = new RedisCommand<Map<Object, Object>>("GEOPOS", geoDecoder);
        return read(key, StringCodec.INSTANCE, command, params.toArray());
    }

    /** 将键值对 Map 展开为 Redis 参数列表。 */
    private String convert(double longitude) {
        return BigDecimal.valueOf(longitude).toPlainString();
    }

    private final MultiDecoder<GeoResults<GeoLocation<byte[]>>> postitionDecoder = new ListMultiDecoder2(new GeoResultsDecoder(), new CodecDecoder(), new PointDecoder(), new ObjectListReplayDecoder());
    
    /** GEORADIUS：按圆心半径查询附近 member。 */
    @Override
    public GeoResults<GeoLocation<byte[]>> geoRadius(byte[] key, Circle within) {
        RedisCommand<GeoResults<GeoLocation<byte[]>>> command = new RedisCommand<GeoResults<GeoLocation<byte[]>>>("GEORADIUS_RO", new GeoResultsDecoder());
        return read(key, ByteArrayCodec.INSTANCE, command, key, 
                        convert(within.getCenter().getX()), convert(within.getCenter().getY()), 
                        within.getRadius().getValue(), getAbbreviation(within.getRadius().getMetric()));
    }
    
    /** GEORADIUS：按圆心半径查询附近 member。 */
    @Override
    public GeoResults<GeoLocation<byte[]>> geoRadius(byte[] key, Circle within, GeoRadiusCommandArgs args) {
        List<Object> params = new ArrayList<Object>();
        params.add(key);
        params.add(convert(within.getCenter().getX()));
        params.add(convert(within.getCenter().getY()));
        params.add(within.getRadius().getValue());
        params.add(getAbbreviation(within.getRadius().getMetric()));
        
        RedisCommand<GeoResults<GeoLocation<byte[]>>> command;
        if (args.getFlags().contains(GeoRadiusCommandArgs.Flag.WITHCOORD)) {
            command = new RedisCommand<GeoResults<GeoLocation<byte[]>>>("GEORADIUS_RO", postitionDecoder);
            params.add("WITHCOORD");
        } else {
            MultiDecoder<GeoResults<GeoLocation<byte[]>>> distanceDecoder = new ListMultiDecoder2(new GeoResultsDecoder(within.getRadius().getMetric()), new GeoDistanceDecoder());
            command = new RedisCommand<GeoResults<GeoLocation<byte[]>>>("GEORADIUS_RO", distanceDecoder);
            params.add("WITHDIST");
        }
        
        if (args.getLimit() != null) {
            params.add("COUNT");
            params.add(args.getLimit());
        }
        if (args.getSortDirection() != null) {
            params.add(args.getSortDirection().name());
        }
        
        return read(key, ByteArrayCodec.INSTANCE, command, params.toArray());
    }

    /** getAbbreviation：Redis 命令实现。 */
    private String getAbbreviation(Metric metric) {
        if (ObjectUtils.nullSafeEquals(Metrics.NEUTRAL, metric)) {
            return DistanceUnit.METERS.getAbbreviation();
        }
        return metric.getAbbreviation();
    }

    /** GEORADIUSBYMEMBER：以 member 为圆心查询附近点。 */
    @Override
    public GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member, double radius) {
        return geoRadiusByMember(key, member, new Distance(radius, DistanceUnit.METERS));
    }

    private static final RedisCommand<GeoResults<GeoLocation<byte[]>>> GEORADIUSBYMEMBER = new RedisCommand<GeoResults<GeoLocation<byte[]>>>("GEORADIUSBYMEMBER_RO", new GeoResultsDecoder());
    
    /** GEORADIUSBYMEMBER：以 member 为圆心查询附近点。 */
    @Override
    public GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member, Distance radius) {
        return read(key, ByteArrayCodec.INSTANCE, GEORADIUSBYMEMBER, key, member, radius.getValue(), getAbbreviation(radius.getMetric()));
    }

    /** GEORADIUSBYMEMBER：以 member 为圆心查询附近点。 */
    @Override
    public GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member, Distance radius,
            GeoRadiusCommandArgs args) {
        List<Object> params = new ArrayList<Object>();
        params.add(key);
        params.add(member);
        params.add(radius.getValue());
        params.add(getAbbreviation(radius.getMetric()));
        
        RedisCommand<GeoResults<GeoLocation<byte[]>>> command;
        if (args.getFlags().contains(GeoRadiusCommandArgs.Flag.WITHCOORD)) {
            command = new RedisCommand<GeoResults<GeoLocation<byte[]>>>("GEORADIUSBYMEMBER_RO", postitionDecoder);
            params.add("WITHCOORD");
        } else {
            MultiDecoder<GeoResults<GeoLocation<byte[]>>> distanceDecoder = new ListMultiDecoder2(new GeoResultsDecoder(radius.getMetric()), new GeoDistanceDecoder());
            command = new RedisCommand<GeoResults<GeoLocation<byte[]>>>("GEORADIUSBYMEMBER_RO", distanceDecoder);
            params.add("WITHDIST");
        }
        
        if (args.getLimit() != null) {
            params.add("COUNT");
            params.add(args.getLimit());
        }
        if (args.getSortDirection() != null) {
            params.add(args.getSortDirection().name());
        }
        
        return read(key, ByteArrayCodec.INSTANCE, command, params.toArray());
    }

    /** ZREM：从 geo 集合移除 member。 */
    @Override
    public Long geoRemove(byte[] key, byte[]... members) {
        return zRem(key, members);
    }
    
    private static final RedisCommand<Long> PFADD = new RedisCommand<Long>("PFADD");

    /** PFADD：向 HyperLogLog 追加元素。 */
    @Override
    public Long pfAdd(byte[] key, byte[]... values) {
        List<Object> params = new ArrayList<Object>(values.length + 1);
        params.add(key);
        for (byte[] member : values) {
            params.add(member);
        }

        return write(key, StringCodec.INSTANCE, PFADD, params.toArray());
    }

    /** PFCOUNT：估算 HyperLogLog 基数。 */
    @Override
    public Long pfCount(byte[]... keys) {
        Assert.notEmpty(keys, "PFCOUNT requires at least one non 'null' key.");
        Assert.noNullElements(keys, "Keys for PFOUNT must not contain 'null'.");

        return write(keys[0], StringCodec.INSTANCE, RedisCommands.PFCOUNT, Arrays.asList(keys).toArray());
    }

    /** PFMERGE：合并多个 HyperLogLog。 */
    @Override
    public void pfMerge(byte[] destinationKey, byte[]... sourceKeys) {
        List<Object> args = new ArrayList<Object>(sourceKeys.length + 1);
        args.add(destinationKey);
        args.addAll(Arrays.asList(sourceKeys));
        write(destinationKey, StringCodec.INSTANCE, RedisCommands.PFMERGE, args.toArray());
    }

    private static final RedisCommand<Long> HSTRLEN = new RedisCommand<Long>("HSTRLEN");
    
    /** HSTRLEN：返回 hash 字段值字节长度。 */
    @Override
    public Long hStrLen(byte[] key, byte[] field) {
        return read(key, StringCodec.INSTANCE, HSTRLEN, key, field);
    }

    /** 返回 Stream 命令适配器。 */
    @Override
    public RedisStreamCommands streamCommands() {
        return new RedissonStreamCommands(this, executorService);
    }

    private static final RedisStrictCommand<List<Object>> BITFIELD = new RedisStrictCommand<>("BITFIELD", new ObjectListReplayDecoder<>());

    /** BITFIELD：读写/增减字符串位域。 */
    @Override
    public List<Long> bitField(byte[] key, BitFieldSubCommands subCommands) {
        List<Object> params = new ArrayList<>();
        params.add(key);

        boolean writeOp = false;
        for (BitFieldSubCommands.BitFieldSubCommand subCommand : subCommands) {
            String size = "u";
            if (subCommand.getType().isSigned()) {
                size = "i";
            }
            size += subCommand.getType().getBits();

			String offset = "#";
			if (subCommand.getOffset().isZeroBased()) {
				offset = "";
			}
			offset += subCommand.getOffset().getValue();

			if (subCommand instanceof BitFieldSubCommands.BitFieldGet) {
			    params.add("GET");
			    params.add(size);
			    params.add(offset);
			} else if (subCommand instanceof BitFieldSubCommands.BitFieldSet) {
			    writeOp = true;
			    params.add("SET");
			    params.add(size);
			    params.add(offset);
                params.add(((BitFieldSubCommands.BitFieldSet) subCommand).getValue());
			} else if (subCommand instanceof BitFieldSubCommands.BitFieldIncrBy) {
			    writeOp = true;
			    params.add("INCRBY");
			    params.add(size);
			    params.add(offset);
                params.add(((BitFieldSubCommands.BitFieldIncrBy) subCommand).getValue());

                BitFieldSubCommands.BitFieldIncrBy.Overflow overflow = ((BitFieldSubCommands.BitFieldIncrBy) subCommand).getOverflow();
                if (overflow != null) {
                    params.add("OVERFLOW");
                    params.add(overflow);
                }
			}
		}

        if (writeOp) {
            return write(key, StringCodec.INSTANCE, BITFIELD, params.toArray());
        }
        return read(key, StringCodec.INSTANCE, BITFIELD, params.toArray());
    }

    /** EXISTS：判断 key 是否存在。 */
    @Override
    public Long exists(byte[]... keys) {
        return read(keys[0], StringCodec.INSTANCE, RedisCommands.EXISTS_LONG, Arrays.asList(keys).toArray());
    }

    /** TOUCH：更新 key 的最后访问时间。 */
    @Override
    public Long touch(byte[]... keys) {
        return read(keys[0], StringCodec.INSTANCE, RedisCommands.TOUCH_LONG, Arrays.asList(keys).toArray());
    }

    private static final RedisStrictCommand<ValueEncoding> OBJECT_ENCODING = new RedisStrictCommand<ValueEncoding>("OBJECT", "ENCODING", obj -> ValueEncoding.of((String) obj));

    /** OBJECT ENCODING：返回 key 内部编码。 */
    @Override
    public ValueEncoding encodingOf(byte[] key) {
        Assert.notNull(key, "Key must not be null!");
        return read(key, StringCodec.INSTANCE, OBJECT_ENCODING, key);
    }

    private static final RedisStrictCommand<Duration> OBJECT_IDLETIME = new RedisStrictCommand<>("OBJECT", "IDLETIME", obj -> Duration.ofSeconds((Long)obj));

    /** OBJECT IDLETIME：返回 key 空闲秒数。 */
    @Override
    public Duration idletime(byte[] key) {
        Assert.notNull(key, "Key must not be null!");
        return read(key, StringCodec.INSTANCE, OBJECT_IDLETIME, key);
    }

    private static final RedisStrictCommand<Long> OBJECT_REFCOUNT = new RedisStrictCommand<Long>("OBJECT", "REFCOUNT");

    /** OBJECT REFCOUNT：返回 key 引用计数。 */
    @Override
    public Long refcount(byte[] key) {
        Assert.notNull(key, "Key must not be null!");
        return read(key, StringCodec.INSTANCE, OBJECT_REFCOUNT, key);
    }

    private static final RedisStrictCommand<Long> BITPOS = new RedisStrictCommand<>("BITPOS");

    /** BITPOS：查找第一个指定 bit 值的偏移。 */
    @Override
    public Long bitPos(byte[] key, boolean bit, org.springframework.data.domain.Range<Long> range) {
		Assert.notNull(key, "Key must not be null!");
		Assert.notNull(range, "Range must not be null! Use Range.unbounded() instead.");

        List<Object> params = new ArrayList<>();
        params.add(key);
        if (bit) {
            params.add(1);
        } else {
            params.add(0);
        }
        if (range.getLowerBound().isBounded()) {
            params.add(range.getLowerBound().getValue().get());
            if (range.getUpperBound().isBounded()) {
                params.add(range.getUpperBound().getValue().get());
            }
        }

		return read(key, StringCodec.INSTANCE, BITPOS, params.toArray());
    }

    /** RESTORE：用 DUMP 数据恢复 key 并设置 TTL。 */
    @Override
    public void restore(byte[] key, long ttlInMillis, byte[] serializedValue, boolean replace) {
        if (replace) {
            write(key, StringCodec.INSTANCE, RedisCommands.RESTORE, key, ttlInMillis, serializedValue, "REPLACE");
            return;
        }
        restore(key, ttlInMillis, serializedValue);
    }

}
