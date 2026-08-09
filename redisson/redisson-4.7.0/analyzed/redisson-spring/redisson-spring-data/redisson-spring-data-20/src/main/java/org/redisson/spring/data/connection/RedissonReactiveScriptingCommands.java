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

import org.redisson.api.RFuture;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.misc.CompletableFutureWrapper;
import org.redisson.reactive.CommandReactiveExecutor;
import org.springframework.data.redis.connection.ReactiveScriptingCommands;
import org.springframework.data.redis.connection.ReturnType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Spring Data Redis 响应式 Lua 脚本命令实现。
 * <p>封装 SCRIPT FLUSH/LOAD/EXISTS 及 EVAL/EVALSHA；
 {@link ReturnType} 映射为对应 {@link RedisCommand} 与解码器。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonReactiveScriptingCommands extends RedissonBaseReactive implements ReactiveScriptingCommands {

    /** 注入响应式命令执行器。 */
    RedissonReactiveScriptingCommands(CommandReactiveExecutor executorService) {
        super(executorService);
    }

    /** SCRIPT FLUSH：清空所有节点脚本缓存。 */
    @Override
    public Mono<String> scriptFlush() {
        return executorService.reactive(() -> {
            RFuture<Void> f = executorService.writeAllVoidAsync(RedisCommands.SCRIPT_FLUSH);
            return toStringFuture(f);
        });
    }

    /** SCRIPT KILL：当前未实现。 */
    @Override
    public Mono<String> scriptKill() {
        throw new UnsupportedOperationException();
    }

    /** SCRIPT LOAD：向所有节点加载脚本并返回 SHA1。 */
    @Override
    public Mono<String> scriptLoad(ByteBuffer script) {
        return executorService.reactive(() -> {
            List<CompletableFuture<String>> futures = executorService.executeAllAsync(RedisCommands.SCRIPT_LOAD, (Object)toByteArray(script));
            CompletableFuture<Void> f = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            CompletableFuture<String> s = f.thenApply(r -> futures.get(0).getNow(null));
            return new CompletableFutureWrapper<>(s);
        });
    }

    @Override
    public Flux<Boolean> scriptExists(List<String> scriptShas) {
        Mono<List<Boolean>> m = executorService.reactive(() -> {
            List<CompletableFuture<List<Boolean>>> futures = executorService.writeAllAsync(RedisCommands.SCRIPT_EXISTS, scriptShas.toArray());
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
            return new CompletableFutureWrapper<>(s);
        });
        return m.flatMapMany(v -> Flux.fromIterable(v));
    }

    /** 将 Spring {@link ReturnType} 映射为 Redisson {@link RedisCommand}。 */
    protected RedisCommand<?> toCommand(ReturnType returnType, String name) {
        RedisCommand<?> c = null; 
        if (returnType == ReturnType.BOOLEAN) {
            c = org.redisson.api.RScript.ReturnType.BOOLEAN.getCommand();
        } else if (returnType == ReturnType.INTEGER) {
            c = org.redisson.api.RScript.ReturnType.LONG.getCommand();
        // MULTI 返回列表，MULTI/VALUE 使用 BinaryConvertor 解码。
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
    
    /** EVAL：执行脚本并将 byte[]/List 转为 {@link ByteBuffer}。 */
    @Override
    public <T> Flux<T> eval(ByteBuffer script, ReturnType returnType, int numKeys, ByteBuffer... keysAndArgs) {
        RedisCommand<?> c = toCommand(returnType, "EVAL");
        List<Object> params = new ArrayList<Object>();
        params.add(toByteArray(script));
        params.add(numKeys);
        params.addAll(Arrays.stream(keysAndArgs).map(m -> toByteArray(m)).collect(Collectors.toList()));
        Mono<T> m = write(null, ByteArrayCodec.INSTANCE, c, params.toArray());
        return convert(m);
    }

    /** 将脚本返回值中的 byte[] 与嵌套列表元素包装为 {@link ByteBuffer}。 */
    protected <T> Flux<T> convert(Mono<T> m) {
        return (Flux<T>) m.map(e -> {
            if (e.getClass().isArray()) {
                return ByteBuffer.wrap((byte[])e);
            }
            if (e instanceof List) {
                List l = (List) e;
                if (!l.isEmpty()) {
                    for (int i = 0; i < l.size(); i++) {
                        if (l.get(i).getClass().isArray()) {
                            l.set(i, ByteBuffer.wrap((byte[])l.get(i)));
                        }
                    }
                    return l;
                }
            }
            return e;
        }).flux();
    }

    /** EVALSHA：按 SHA1 执行已加载脚本。 */
    @Override
    public <T> Flux<T> evalSha(String scriptSha, ReturnType returnType, int numKeys, ByteBuffer... keysAndArgs) {
        RedisCommand<?> c = toCommand(returnType, "EVALSHA");
        List<Object> params = new ArrayList<Object>();
        params.add(scriptSha);
        params.add(numKeys);
        params.addAll(Arrays.stream(keysAndArgs).map(m -> toByteArray(m)).collect(Collectors.toList()));
        Mono<T> m = write(null, ByteArrayCodec.INSTANCE, c, params.toArray());
        return convert(m);
    }

}
