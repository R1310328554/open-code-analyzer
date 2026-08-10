package org.keycloak.ssf.transmitter.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.ssf.transmitter.delivery.poll.PollDeliveryService;
import org.keycloak.ssf.transmitter.delivery.poll.PollErrorRepresentation;
import org.keycloak.ssf.transmitter.delivery.poll.PollRequest;
import org.keycloak.ssf.transmitter.delivery.poll.PollResponse;
import org.keycloak.ssf.transmitter.stream.StreamConfig;
import org.keycloak.ssf.transmitter.stream.storage.SsfStreamStore;
import org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore;
import org.keycloak.ssf.transmitter.support.SsfAuthUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * RFC 8936 基于轮询的 SET 投递端点。
 *
 * <p>由 {@link SsfTransmitterResource} 挂载于
 * {@code receivers/{clientId}/streams/{streamId}/poll}，使线上 URL 与
 * 发送方在流创建响应中写回的 {@code delivery.endpoint_url} 一致（SSF §6.1.2）。
 *
 * <p>授权分层：
 * <ol>
 *     <li>{@link SsfAuthUtil#canRead()} — bearer 有效、调用客户端启用 ssf、
 *         具备 {@code ssf.read} 范围，及可选的服务账户/必需角色检查。</li>
 *     <li>路径与令牌归属：URL 中 {@code {clientId}} 须与 bearer 解析客户端的 clientId 一致，
 *         {@code {streamId}} 须等于该客户端注册的 stream id。任一不匹配均静默返回
 *         {@code 404 stream_not_found}，避免 URL 表面泄露存在哪些客户端/流。</li>
 * </ol>
 */
public class SsfStreamPollResource {

    private static final Logger log = Logger.getLogger(SsfStreamPollResource.class);

    protected final KeycloakSession session;

    protected final SsfStreamStore streamStore;

    protected final PollDeliveryService pollDeliveryService;

    public SsfStreamPollResource(KeycloakSession session, SsfStreamStore streamStore, PollDeliveryService pollDeliveryService) {
        this.session = session;
        this.streamStore = streamStore;
        this.pollDeliveryService = pollDeliveryService;
    }

    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Ssf.Tags.TRANSMITTER)
    @Operation(
            summary = "Poll for pending events",
            description = "RFC 8936 polling endpoint. Acks the receiver's previously-acknowledged events (via the `ack` array) and returns the next batch of pending Security Event Tokens for the stream."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PollResponse.class))),
            @APIResponse(responseCode = "400", description = "Bad Request"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden — caller lacks ssf.read scope or required role"),
            @APIResponse(responseCode = "404", description = "Stream not found, or path components don't belong to the calling client (silent — no enumeration oracle)")
    })
    public Response poll(
            @Parameter(description = "OAuth client_id of the receiver (must match the bearer token's client)")
            @PathParam("clientId") String clientId,
            @Parameter(description = "Identifier of the stream the receiver wants to poll (must belong to the calling client)")
            @PathParam("streamId") String streamId,
            PollRequest request) {

        // 1. 标准 SSF 面向接收方的认证——与其他发送方端点相同。无效令牌、缺少范围等返回 401。
        if (!SsfAuthUtil.canRead()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        ClientModel callerClient = session.getContext().getClient();

        // 2. 路径与令牌归属检查。客户端与流不匹配均折叠为同一静默 404，
        //    避免向探测者泄露发送方上存在哪些 clientId/streamId。
        if (callerClient == null
                || clientId == null
                || !clientId.equals(callerClient.getClientId())) {
            log.debugf("SSF poll denied: path clientId mismatch. pathClientId=%s tokenClientId=%s",
                    clientId, callerClient == null ? null : callerClient.getClientId());
            return streamNotFound();
        }

        String registeredStreamId = callerClient.getAttribute(ClientStreamStore.SSF_STREAM_ID_KEY);
        if (registeredStreamId == null || !registeredStreamId.equals(streamId)) {
            log.debugf("SSF poll denied: stream id mismatch. pathStreamId=%s registeredStreamId=%s clientId=%s",
                    streamId, registeredStreamId, callerClient.getClientId());
            return streamNotFound();
        }

        // 3. 流必须真实存在且归此客户端所有——防止过期的 stream id 属性。
        StreamConfig stream = lookupStream(callerClient);
        if (stream == null || !streamId.equals(stream.getStreamId())) {
            log.debugf("SSF poll denied: stream lookup failed for clientId=%s streamId=%s",
                    callerClient.getClientId(), streamId);
            return streamNotFound();
        }

        PollRequest body = request != null ? request : new PollRequest();

        // 4. 将 ack 与 setErrs 批量上限设为 MAX_BATCH_CAP（各 1000）。
        //    限制请求载荷及后续 per-(client,jti) IN 查询。需 ack/NACK 超过上限的接收方应拆成多次轮询。
        if (body.getAck() != null && body.getAck().size() > PollDeliveryService.MAX_BATCH_CAP) {
            return invalidRequest("ack array exceeds " + PollDeliveryService.MAX_BATCH_CAP
                    + " entries — split into multiple polls");
        }
        if (body.getSetErrs() != null && body.getSetErrs().size() > PollDeliveryService.MAX_BATCH_CAP) {
            return invalidRequest("setErrs object exceeds " + PollDeliveryService.MAX_BATCH_CAP
                    + " entries — split into multiple polls");
        }

        PollResponse response = pollDeliveryService.poll(callerClient, body);
        return Response.ok(response).build();
    }

    protected Response invalidRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new PollErrorRepresentation("invalid_request", message))
                .build();
    }

    protected StreamConfig lookupStream(ClientModel callerClient) {
        return streamStore.getStreamForClient(callerClient);
    }

    protected Response streamNotFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new PollErrorRepresentation("stream_not_found", "Stream not found"))
                .build();
    }
}
