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

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.entity.WriteRequest;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 一致性协议抽象接口，与具体实现（Raft、Distro 等）解耦；典型初始化顺序：{@code init(Config)}。
 *
 * <ul>
 *     <li>{@link Config}：协议所需配置，如 Raft 选举超时、日志存储路径、快照间隔等</li>
 *     <li>{@link ConsistencyProtocol#protocolMetaData()}：返回协议元数据，如 Raft 的 leader、term 等</li>
 * </ul>
 *
 * Has nothing to do with the specific implementation of the consistency protocol Initialization sequence： init(Config).
 *
 * <ul>
 *     <li>{@link Config} : Relevant configuration information required by the consistency protocol,
 *     for example, the Raft protocol needs to set the election timeout time, the location where
 *     the Log is stored, and the snapshot task execution interval</li>
 *     <li>{@link ConsistencyProtocol#protocolMetaData()} : Returns metadata information of the consistency
 *     protocol, such as leader, term, and other metadata information in the Raft protocol</li>
 * </ul>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface ConsistencyProtocol<T extends Config, P extends RequestProcessor>
    extends CommandOperations {
    
    /**
     * 一致性协议初始化，根据 {@link Config} 实现类完成成员与参数配置。
     * Consistency protocol initialization: perform initialization operations based on the incoming.
     * Config 一致性协议初始化，根据Config 实现类
     *
     * @param config {@link Config}
     */
    void init(T config);
    
    /**
     * 注册请求处理器集合，按业务 group 路由读写请求。
     * Add a request handler.
     *
     * @param processors {@link RequestProcessor}
     */
    void addRequestProcessors(Collection<P> processors);
    
    /**
     * 返回该一致性协议的元数据快照（如 leader、term）。
     * Copy of metadata information for this consensus protocol.
     * 该一致性协议的元数据信息
     *
     * @return metaData {@link ProtocolMetaData}
     */
    ProtocolMetaData protocolMetaData();
    
    /**
     * 同步读取数据。
     * Obtain data according to the request.
     *
     * @param request request
     * @return data {@link Response}
     * @throws Exception {@link Exception}
     */
    Response getData(ReadRequest request) throws Exception;
    
    /**
     * 异步读取数据。
     * Get data asynchronously.
     *
     * @param request request
     * @return data {@link CompletableFuture}
     */
    CompletableFuture<Response> aGetData(ReadRequest request);
    
    /**
     * 同步写提交：{@link WriteRequest} 中已携带数据操作类型与载荷。
     * Data operation, returning submission results synchronously.
     * 同步数据提交，在 Datum 中已携带相应的数据操作信息
     *
     * @param request {@link com.alibaba.nacos.consistency.entity.WriteRequest}
     * @return submit operation result {@link Response}
     * @throws Exception {@link Exception}
     */
    Response write(WriteRequest request) throws Exception;
    
    /**
     * 异步写提交：返回 {@link CompletableFuture}，异常在 Future 完成时体现。
     * Data submission operation, returning submission results asynchronously.
     * 异步数据提交，在 Datum中已携带相应的数据操作信息，返回一个Future，自行操作，提交发生的异常会在CompleteFuture中
     *
     * @param request {@link com.alibaba.nacos.consistency.entity.WriteRequest}
     * @return {@link CompletableFuture} submit result
     * @throws Exception when submit throw Exception
     */
    CompletableFuture<Response> writeAsync(WriteRequest request);
    
    /**
     * 更新成员节点列表，协议自行处理加入或离开逻辑。
     * New member list .
     * 新的成员节点列表，一致性协议自行处理相应的成员节点是加入还是离开
     *
     * @param addresses [ip:port, ip:port, ...]
     */
    void memberChange(Set<String> addresses);
    
    /**
     * 协议是否已就绪（如已选出 leader、快照加载完成等）。
     * Whether protocol is ready to work, such as contain leader, finish load snapshot and so on.
     *
     * @return {@code true} when protocol ready to work, otherwise {@code false}
     */
    boolean isReady();
    
    /**
     * 关闭一致性协议服务并释放资源。
     * Consistency agreement service shut down .
     * 一致性协议服务关闭
     */
    void shutdown();
    
}
