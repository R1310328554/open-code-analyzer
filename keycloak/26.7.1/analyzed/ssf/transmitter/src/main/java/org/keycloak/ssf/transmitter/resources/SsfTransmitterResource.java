package org.keycloak.ssf.transmitter.resources;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.ssf.transmitter.SsfTransmitterProvider;

/**
 * SSF 发送方根 REST 资源，将子路径委托给流管理、状态、验证、主体管理与轮询端点。
 */
public class SsfTransmitterResource {

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** SSF 发送方 Provider，提供各子资源工厂。 */
    protected final SsfTransmitterProvider transmitter;

    /** 当前请求的认证结果。 */
    private final AuthenticationManager.AuthResult authResult;

    public SsfTransmitterResource(KeycloakSession session, AuthenticationManager.AuthResult authResult, SsfTransmitterProvider transmitter) {
        this.session = session;
        this.authResult = authResult;
        this.transmitter = transmitter;
    }

    /** 流 CRUD 管理子资源。 */
    @Path("/streams")
    public SsfStreamManagementResource getStreamManagementEndpoint() {
        return transmitter.streamManagementResource();
    }

    /** 流状态查询/更新子资源。 */
    @Path("/streams/status")
    public SsfStreamStatusResource getStreamStatusEndpoint() {
        return transmitter.streamStatusResource();
    }

    /** 流验证触发子资源。 */
    @Path("/verify")
    public SsfStreamVerificationResource getVerificationEndpoint() {
        return transmitter.streamVerificationResource();
    }

    /**
     * 单一子资源定位器，同时覆盖 {@code /subjects/add} 与 {@code /subjects/remove}。
     * 两个动作由 {@link SsfSubjectManagementResource} 上各方法的 {@code @Path("add")}/
     * {@code @Path("remove")} 区分。若拆成两个定位器（从 {@code /subjects/add} 与
     * {@code /subjects/remove} 均返回同一子资源）曾令 JAX-RS 混淆——两路径会路由到
     * 先被选中的 {@code @POST} 方法。
     */
    @Path("/subjects")
    public SsfSubjectManagementResource getSubjectManagementEndpoint() {
        if (!transmitter.getConfig().isSubjectManagementEnabled()) {
            throw new NotFoundException();
        }
        return transmitter.subjectManagementResource();
    }

    /**
     * RFC 8936 轮询端点（SSF §6.1.2）。子资源定位器——stream-id 与 client-id 路径参数
     * 由 {@link SsfStreamPollResource#poll} 消费。面向接收方的 URL 形态为
     * {@code /ssf/transmitter/receivers/{clientId}/streams/{streamId}/poll}，
     * 与发送方在流创建响应中写回的 {@code delivery.endpoint_url} 一致。
     */
    @Path("/receivers/{clientId}/streams/{streamId}/poll")
    public SsfStreamPollResource getStreamPollEndpoint() {
        return new SsfStreamPollResource(session, transmitter.streamStore(), transmitter.pollDeliveryService());
    }
}
