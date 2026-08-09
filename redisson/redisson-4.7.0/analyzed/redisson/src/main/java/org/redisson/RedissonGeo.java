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

import org.redisson.api.*;
import org.redisson.api.geo.*;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.decoder.*;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.connection.decoder.MapGetAllDecoder;

import java.math.BigDecimal;
import java.util.*;

/**
 * Geospatial items holder
 * 
 * @author Nikita Koksharov
 *
 * @param <V> value
 */
public class RedissonGeo<V> extends RedissonScoredSortedSet<V> implements RGeo<V> {

    private static final MultiDecoder<Map<Object, Object>> POSTITION_DECODER = new ListMultiDecoder2(
            new ObjectMapReplayDecoder2(),
            new CodecDecoder(),
            new GeoPositionDecoder());
    
    private static final MultiDecoder<Map<Object, Object>> DISTANCE_DECODER = new ListMultiDecoder2(
            new ObjectMapReplayDecoder2(),
            new GeoDistanceDecoder());
    
    private static final RedisCommand<Map<Object, Object>> GEORADIUS_RO_DISTANCE = new RedisCommand<Map<Object, Object>>(
            "GEORADIUS_RO", DISTANCE_DECODER);
    private static final RedisCommand<Map<Object, Object>> GEOSEARCH_DISTANCE = new RedisCommand<Map<Object, Object>>(
            "GEOSEARCH", DISTANCE_DECODER);
    private static final RedisCommand<Map<Object, Object>> GEOSEARCH_POS = new RedisCommand<Map<Object, Object>>(
            "GEOSEARCH", POSTITION_DECODER);
    private static final RedisCommand<Map<Object, Object>> GEORADIUS_RO_POS = new RedisCommand<Map<Object, Object>>(
            "GEORADIUS_RO", POSTITION_DECODER);
    private static final RedisCommand<Map<Object, Object>> GEORADIUSBYMEMBER_RO_DISTANCE = new RedisCommand<Map<Object, Object>>(
            "GEORADIUSBYMEMBER_RO", DISTANCE_DECODER);
    private static final RedisCommand<Map<Object, Object>> GEORADIUSBYMEMBER_RO_POS = new RedisCommand<Map<Object, Object>>(
            "GEORADIUSBYMEMBER_RO", POSTITION_DECODER);

    public RedissonGeo(CommandAsyncExecutor connectionManager, String name, RedissonClient redisson) {
        super(connectionManager, name, redisson);
    }

    public RedissonGeo(Codec codec, CommandAsyncExecutor connectionManager, String name, RedissonClient redisson) {
        super(codec, connectionManager, name, redisson);
    }

    /** 异步追加元素。 */
    @Override
    public RFuture<Long> addAsync(double longitude, double latitude, V member) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.GEOADD, getRawName(), convert(longitude),
                convert(latitude), encode(member));
    }

    /** 地理空间 convert 操作。 */
    private String convert(double longitude) {
        return BigDecimal.valueOf(longitude).toPlainString();
    }

    /** 追加元素（满则覆盖最旧）。 */
    @Override
    public long add(double longitude, double latitude, V member) {
        return get(addAsync(longitude, latitude, member));
    }

    /** 追加元素（满则覆盖最旧）。 */
    @Override
    public long add(GeoEntry... entries) {
        return get(addAsync(entries));
    }

    /** 异步追加元素。 */
    @Override
    public RFuture<Long> addAsync(GeoEntry... entries) {
        return addAsync("", entries);
    }

    /** 异步追加元素。 */
    private RFuture<Long> addAsync(String subCommand, GeoEntry... entries) {
        List<Object> params = new ArrayList<>(entries.length + 2);
        params.add(getRawName());
        if (!subCommand.isEmpty()) {
            params.add(subCommand);
        }
        for (GeoEntry entry : entries) {
            params.add(entry.getLongitude());
            params.add(entry.getLatitude());
            encode(params, entry.getMember());
        }
        return commandExecutor.writeAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.GEOADD, params.toArray());
    }

    /** 地理空间 addIfExists 操作。 */
    @Override
    public Boolean addIfExists(double longitude, double latitude, V member) {
        return get(addIfExistsAsync(longitude, latitude, member));
    }

    /** 地理空间 addIfExists 操作。 */
    @Override
    public long addIfExists(GeoEntry... entries) {
        return get(addIfExistsAsync(entries));
    }

    /** 异步执行 addIfExists。 */
    @Override
    public RFuture<Boolean> addIfExistsAsync(double longitude, double latitude, V member) {
        return commandExecutor.evalWriteAsync(getRawName(), codec, RedisCommands.EVAL_BOOLEAN,
            "local value = redis.call('geopos', KEYS[1], ARGV[3]); "
                + "if value[1] ~= false then "
                    + "redis.call('geoadd', KEYS[1], ARGV[1], ARGV[2], ARGV[3]); "
                    + "return 1; "
                + "end; "
                + "return 0; ",
                Collections.singletonList(getRawName()),
                convert(longitude), convert(latitude), encode(member));
    }

    /** 异步执行 addIfExists。 */
    @Override
    public RFuture<Long> addIfExistsAsync(GeoEntry... entries) {
        return addAsync("XX", entries);
    }

    /** 地理空间 tryAdd 操作。 */
    @Override
    public boolean tryAdd(double longitude, double latitude, V member) {
        return get(tryAddAsync(longitude, latitude, member));
    }

    /** 地理空间 tryAdd 操作。 */
    @Override
    public long tryAdd(GeoEntry... entries) {
        return get(tryAddAsync(entries));
    }

    /** 异步执行 tryAdd。 */
    @Override
    public RFuture<Boolean> tryAddAsync(double longitude, double latitude, V member) {
        return commandExecutor.writeAsync(getRawName(), codec, RedisCommands.GEOADD_BOOLEAN, getRawName(), "NX", convert(longitude),
                convert(latitude), encode(member));
    }

    /** 异步执行 tryAdd。 */
    @Override
    public RFuture<Long> tryAddAsync(GeoEntry... entries) {
        return addAsync("NX", entries);
    }

    /** 地理空间 dist 操作。 */
    @Override
    public Double dist(V firstMember, V secondMember, GeoUnit geoUnit) {
        return get(distAsync(firstMember, secondMember, geoUnit));
    }

    /** 异步执行 dist。 */
    @Override
    public RFuture<Double> distAsync(V firstMember, V secondMember, GeoUnit geoUnit) {
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, RedisCommands.GEODIST, getRawName(),
                encode(firstMember), encode(secondMember), geoUnit);
    }

    /** 地理空间 hash 操作。 */
    @Override
    public Map<V, String> hash(V... members) {
        return get(hashAsync(members));
    }

    /** 异步执行 hash。 */
    @Override
    public RFuture<Map<V, String>> hashAsync(V... members) {
        List<Object> params = new ArrayList<>(members.length + 1);
        params.add(getRawName());
        for (Object member : members) {
            encode(params, member);
        }
        RedisCommand<Map<Object, Object>> command = new RedisCommand<>("GEOHASH",
                new MapGetAllDecoder(Arrays.asList(members), 0));
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, command, params.toArray());
    }

    /** 地理空间 pos 操作。 */
    @Override
    public Map<V, GeoPosition> pos(V... members) {
        return get(posAsync(members));
    }

    /** 异步执行 pos。 */
    @Override
    public RFuture<Map<V, GeoPosition>> posAsync(V... members) {
        List<Object> params = new ArrayList<>(members.length + 1);
        params.add(getRawName());
        for (Object member : members) {
            encode(params, member);
        }

        MultiDecoder<Map<Object, Object>> decoder = new ListMultiDecoder2(
                new GeoPositionMapDecoder((List<Object>) Arrays.asList(members)),
                new GeoPositionDecoder());
        RedisCommand<Map<Object, Object>> command = new RedisCommand<Map<Object, Object>>("GEOPOS", decoder);
        return commandExecutor.readAsync(getRawName(), StringCodec.INSTANCE, command, params.toArray());
    }

    /** 地理空间 search 操作。 */
    @Override
    public List<V> search(GeoSearchArgs args) {
        return get(searchAsync(args));
    }

    /** 异步执行 search。 */
    @Override
    public RFuture<List<V>> searchAsync(GeoSearchArgs args) {
        GeoSearchParams params = (GeoSearchParams) args;

        List<Object> commandParams = new ArrayList<>();
        commandParams.add(getRawName());
        RedisCommand command = null;
        if (params.getLatitude() != null
                && params.getLongitude() != null) {
            command = RedisCommands.GEORADIUS_RO;
            if (params.getHeight() != null) {
                command = RedisCommands.GEOSEARCH;
                commandParams.add("FROMLONLAT");
            }
            commandParams.add(convert(params.getLongitude()));
            commandParams.add(convert(params.getLatitude()));
        }
        if (params.getMember() != null) {
            command = RedisCommands.GEORADIUSBYMEMBER_RO;
            if (params.getHeight() != null) {
                command = RedisCommands.GEOSEARCH;
                commandParams.add("FROMMEMBER");
            }
            commandParams.add(encode(params.getMember()));
        }
        if (params.getRadius() != null
            && params.getUnit() != null) {
            commandParams.add(params.getRadius());
            commandParams.add(params.getUnit());
        }
        if (params.getHeight() != null
            && params.getUnit() != null) {
            commandParams.add("BYBOX");
            commandParams.add(params.getWidth());
            commandParams.add(params.getHeight());
            commandParams.add(params.getUnit());

            if (params.getOrder() != null) {
                commandParams.add(params.getOrder());
            }
        }
        if (params.getCount() != null) {
            commandParams.add("COUNT");
            commandParams.add(params.getCount());
            if (params.isCountAny()) {
                commandParams.add("ANY");
            }
        }
        if (params.getHeight() == null
                && params.getOrder() != null) {
            commandParams.add(params.getOrder());
        }

        return commandExecutor.readAsync(getRawName(), codec, command, commandParams.toArray());
    }

    /** 地理空间 searchWithDistance 操作。 */
    @Override
    public Map<V, Double> searchWithDistance(GeoSearchArgs args) {
        return get(searchWithDistanceAsync(args));
    }

    /** 异步执行 searchWithDistance。 */
    @Override
    public RFuture<Map<V, Double>> searchWithDistanceAsync(GeoSearchArgs args) {
        GeoSearchParams params = (GeoSearchParams) args;

        List<Object> commandParams = new ArrayList<>();
        commandParams.add(getRawName());
        RedisCommand command = null;
        if (params.getLatitude() != null
                && params.getLongitude() != null) {
            command = GEORADIUS_RO_DISTANCE;
            if (params.getHeight() != null) {
                command = GEOSEARCH_DISTANCE;
                commandParams.add("FROMLONLAT");
            }
            commandParams.add(convert(params.getLongitude()));
            commandParams.add(convert(params.getLatitude()));
        }
        if (params.getMember() != null) {
            command = GEORADIUSBYMEMBER_RO_DISTANCE;
            if (params.getHeight() != null) {
                command = GEOSEARCH_DISTANCE;
                commandParams.add("FROMMEMBER");
            }
            commandParams.add(encode(params.getMember()));
        }
        if (params.getRadius() != null
            && params.getUnit() != null) {
            commandParams.add(params.getRadius());
            commandParams.add(params.getUnit());
        }
        if (params.getHeight() != null
            && params.getUnit() != null) {
            commandParams.add("BYBOX");
            commandParams.add(params.getWidth());
            commandParams.add(params.getHeight());
            commandParams.add(params.getUnit());

            if (params.getOrder() != null) {
                commandParams.add(params.getOrder());
            }
        }
        if (params.getHeight() == null) {
            commandParams.add("WITHDIST");
        }
        if (params.getCount() != null) {
            commandParams.add("COUNT");
            commandParams.add(params.getCount());
            if (params.isCountAny()) {
                commandParams.add("ANY");
            }
        }
        if (params.getHeight() == null
                && params.getOrder() != null) {
            commandParams.add(params.getOrder());
        }
        if (params.getHeight() != null) {
            commandParams.add("WITHDIST");
        }

        return commandExecutor.readAsync(getRawName(), codec, command, commandParams.toArray());
    }

    /** 地理空间 searchWithPosition 操作。 */
    @Override
    public Map<V, GeoPosition> searchWithPosition(GeoSearchArgs args) {
        return get(searchWithPositionAsync(args));
    }

    /** 异步执行 searchWithPosition。 */
    @Override
    public RFuture<Map<V, GeoPosition>> searchWithPositionAsync(GeoSearchArgs args) {
        GeoSearchParams params = (GeoSearchParams) args;

        List<Object> commandParams = new ArrayList<>();
        commandParams.add(getRawName());
        RedisCommand command = null;
        if (params.getLatitude() != null
                && params.getLongitude() != null) {
            command = GEORADIUS_RO_POS;
            if (params.getHeight() != null) {
                command = GEOSEARCH_POS;
                commandParams.add("FROMLONLAT");
            }
            commandParams.add(convert(params.getLongitude()));
            commandParams.add(convert(params.getLatitude()));
        }
        if (params.getMember() != null) {
            command = GEORADIUSBYMEMBER_RO_POS;
            if (params.getHeight() != null) {
                command = GEOSEARCH_POS;
                commandParams.add("FROMMEMBER");
            }
            commandParams.add(encode(params.getMember()));
        }
        if (params.getRadius() != null
                && params.getUnit() != null) {
            commandParams.add(params.getRadius());
            commandParams.add(params.getUnit());
        }
        if (params.getHeight() != null
            && params.getUnit() != null) {
            commandParams.add("BYBOX");
            commandParams.add(params.getWidth());
            commandParams.add(params.getHeight());
            commandParams.add(params.getUnit());

            if (params.getOrder() != null) {
                commandParams.add(params.getOrder());
            }
        }
        if (params.getHeight() == null) {
            commandParams.add("WITHCOORD");
        }
        if (params.getCount() != null) {
            commandParams.add("COUNT");
            commandParams.add(params.getCount());
            if (params.isCountAny()) {
                commandParams.add("ANY");
            }
        }
        if (params.getHeight() == null
                && params.getOrder() != null) {
            commandParams.add(params.getOrder());
        }
        if (params.getHeight() != null) {
            commandParams.add("WITHCOORD");
        }

        return commandExecutor.readAsync(getRawName(), codec, command, commandParams.toArray());
    }

    /** 地理空间 storeSearchTo 操作。 */
    @Override
    public long storeSearchTo(String destName, GeoSearchArgs args) {
        return get(storeSearchToAsync(destName, args));
    }

    /** 异步执行 storeSearchTo。 */
    @Override
    public RFuture<Long> storeSearchToAsync(String destName, GeoSearchArgs args) {
        GeoSearchParams params = (GeoSearchParams) args;

        List<Object> commandParams = new ArrayList<>();
        if (params.getHeight() != null) {
            commandParams.add(destName);
        }
        commandParams.add(getRawName());
        RedisCommand command = null;
        if (params.getLatitude() != null
                && params.getLongitude() != null) {
            command = RedisCommands.GEORADIUS_STORE;
            if (params.getHeight() != null) {
                command = RedisCommands.GEOSEARCHSTORE_STORE;
                commandParams.add("FROMLONLAT");
            }
            commandParams.add(convert(params.getLongitude()));
            commandParams.add(convert(params.getLatitude()));
        }
        if (params.getMember() != null) {
            command = RedisCommands.GEORADIUSBYMEMBER_STORE;
            if (params.getHeight() != null) {
                command = RedisCommands.GEOSEARCHSTORE_STORE;
                commandParams.add("FROMMEMBER");
            }
            commandParams.add(encode(params.getMember()));
        }
        if (params.getRadius() != null
                && params.getUnit() != null) {
            commandParams.add(params.getRadius());
            commandParams.add(params.getUnit());
        }
        if (params.getHeight() != null
            && params.getUnit() != null) {
            commandParams.add("BYBOX");
            commandParams.add(params.getWidth());
            commandParams.add(params.getHeight());
            commandParams.add(params.getUnit());

            if (params.getOrder() != null) {
                commandParams.add(params.getOrder());
            }
        }
        if (params.getCount() != null) {
            commandParams.add("COUNT");
            commandParams.add(params.getCount());
            if (params.isCountAny()) {
                commandParams.add("ANY");
            }
        }
        if (params.getHeight() == null
                && params.getOrder() != null) {
            commandParams.add(params.getOrder());
        }
        if (params.getHeight() == null) {
            commandParams.add("STORE");
            commandParams.add(destName);
        }

        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, command, commandParams.toArray());
    }

    /** 地理空间 storeSortedSearchTo 操作。 */
    @Override
    public long storeSortedSearchTo(String destName, GeoSearchArgs args) {
        return get(storeSortedSearchToAsync(destName, args));
    }

    /** 异步执行 storeSortedSearchTo。 */
    @Override
    public RFuture<Long> storeSortedSearchToAsync(String destName, GeoSearchArgs args) {
        GeoSearchParams params = (GeoSearchParams) args;

        List<Object> commandParams = new ArrayList<>();
        if (params.getHeight() != null) {
            commandParams.add(destName);
        }
        commandParams.add(getRawName());
        RedisCommand command = null;
        if (params.getLatitude() != null
                && params.getLongitude() != null) {
            command = RedisCommands.GEORADIUS_STORE;
            if (params.getHeight() != null) {
                command = RedisCommands.GEOSEARCHSTORE_STORE;
                commandParams.add("FROMLONLAT");
            }
            commandParams.add(convert(params.getLongitude()));
            commandParams.add(convert(params.getLatitude()));
        }
        if (params.getMember() != null) {
            command = RedisCommands.GEORADIUSBYMEMBER_STORE;
            if (params.getHeight() != null) {
                command = RedisCommands.GEOSEARCHSTORE_STORE;
                commandParams.add("FROMMEMBER");
            }
            commandParams.add(encode(params.getMember()));
        }
        if (params.getRadius() != null
                && params.getUnit() != null) {
            commandParams.add(params.getRadius());
            commandParams.add(params.getUnit());
        }
        if (params.getHeight() != null
            && params.getUnit() != null) {
            commandParams.add("BYBOX");
            commandParams.add(params.getWidth());
            commandParams.add(params.getHeight());
            commandParams.add(params.getUnit());

            if (params.getOrder() != null) {
                commandParams.add(params.getOrder());
            }
        }
        if (params.getCount() != null) {
            commandParams.add("COUNT");
            commandParams.add(params.getCount());
            if (params.isCountAny()) {
                commandParams.add("ANY");
            }
        }
        if (params.getHeight() == null
                && params.getOrder() != null) {
            commandParams.add(params.getOrder());
        }
        commandParams.add("STOREDIST");
        if (params.getHeight() == null) {
            commandParams.add(destName);
        }

        return commandExecutor.writeAsync(getRawName(), LongCodec.INSTANCE, command, commandParams.toArray());
    }

}
