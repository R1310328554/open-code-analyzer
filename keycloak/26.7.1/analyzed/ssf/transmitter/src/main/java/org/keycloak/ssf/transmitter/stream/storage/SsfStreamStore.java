package org.keycloak.ssf.transmitter.stream.storage;

import java.util.List;
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.ssf.stream.StreamStatus;
import org.keycloak.ssf.transmitter.stream.SsfEventsConfig;
import org.keycloak.ssf.transmitter.stream.StreamConfig;
import org.keycloak.ssf.transmitter.stream.StreamVerificationConfig;

/**
 * SSF 流配置的存储与检索接口，抽象持久化层（客户端属性等）。
 */
public interface SsfStreamStore {

    /**
     * 持久化流配置。
     *
     * @param streamConfig 待保存的流配置
     */
    void saveStream(StreamConfig streamConfig);

    /**
     * Update the stream status
     *
     * @param streamId
     * @param streamStatus
     * @return
     */
    StreamStatus updateStreamStatus(String streamId, StreamStatus streamStatus);

    /**
     * Get the stream status
     *
     * @param streamId
     * @return
     */
    StreamStatus getStreamStatus(String streamId);

    /**
     * 按流 ID 获取配置。
     *
     * @param streamId 流 ID
     * @return 流配置，未找到返回 null
     */
    StreamConfig getStream(String streamId);

    /**
     * Gets all stream configurations for the current client context.
     *
     * @return A list of all stream configurations
     */
    List<StreamConfig> getAvailableStreams(ClientModel receiverClient);

    /**
     * 返回符合事件投递条件的全部流配置——即绑定到<em>已启用</em> SSF 接收方客户端的流。
     * 实现 MUST 排除未配置为 SSF 接收方或客户端已禁用的接收方。
     *
     * <p>此为分发枚举入口，比 {@link #getStream}/{@link #getAvailableStreams} 更严格；
     * 后者仍解析已禁用客户端的流以供管理端读写/删除。</p>
     *
     * <p>不按单流 {@code StreamStatusValue} 过滤——分发器在
     * {@code SecurityEventTokenDispatcher#dispatchEvent} 中应用 per-stream 状态门控。</p>
     *
     * <p>用于 session 无特定客户端上下文时（例如事件监听器向所有接收方扇出）。</p>
     */
    List<StreamConfig> findStreamsForSsfReceiverClients();

    /**
     * Finds a stream configuration by stream ID across all clients in the realm.
     * This is used when there is no specific client context.
     *
     * @param streamId The stream ID
     * @return The stream configuration, or null if not found
     */
    StreamConfig findStreamById(String streamId);

    /**
     * Retrieves the stream configuration associated with the specified client.
     *
     * @param client The client model for which the stream configuration is being retrieved.
     * @return The stream configuration associated with the given client, or null if no associated stream configuration exists.
     */
    StreamConfig getStreamForClient(ClientModel client);

    /**
     * Delete the stream configuration associated with the specified client.
     * @param client
     * @return
     */
    boolean deleteStreamForClient(ClientModel client);

    /**
     * Deletes a stream configuration.
     *
     * @param streamId The stream ID
     */
    void deleteStream(String streamId);

    /**
     * Records the current time on the receiver client's "last verified at"
     * attribute for the stream identified by {@code streamId}. Called from
     * {@link org.keycloak.ssf.transmitter.stream.StreamVerificationService#triggerVerification}
     * so every verification path — receiver-initiated, admin-initiated, and
     * transmitter-initiated automatic post-create — records a consistent
     * timestamp without each caller having to stamp the attribute itself.
     *
     * <p>Silently no-ops if no client/stream can be resolved for
     * {@code streamId}; the verification dispatch itself is authoritative
     * for whether anything was actually sent.
     */
    void recordStreamVerification(String streamId);

    StreamVerificationConfig getStreamVerificationConfig(String streamId, ClientModel client);

    SsfEventsConfig getEventsConfig(ClientModel client, Set<String> eventsRequested);


}
