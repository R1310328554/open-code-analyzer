package org.keycloak.protocol.oidc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OIDC 后端通道（Backchannel）登出聚合响应。
 * <p>记录本地登出是否成功及各下游客户端的后端登出 HTTP 结果。</p>
 */
public class BackchannelLogoutResponse {

    /** 本地（Keycloak）登出是否成功。 */
    private boolean localLogoutSucceeded;
    /** 各下游客户端后端登出响应列表。 */
    private List<DownStreamBackchannelLogoutResponse> clientResponses = new ArrayList<>();

    /** @return 下游客户端响应列表 */
    public List<DownStreamBackchannelLogoutResponse> getClientResponses() {
        return clientResponses;
    }

    /** @param clientResponse 单个下游响应 */
    public void addClientResponses(DownStreamBackchannelLogoutResponse clientResponse) {
        this.clientResponses.add(clientResponse);
    }

    /** @return 本地登出是否成功 */
    public boolean getLocalLogoutSucceeded() {
        return localLogoutSucceeded;
    }

    /** @param localLogoutSucceeded 本地登出结果 */
    public void setLocalLogoutSucceeded(boolean localLogoutSucceeded) {
        this.localLogoutSucceeded = localLogoutSucceeded;
    }

    /** 单个下游客户端的后端登出结果。 */
    public static class DownStreamBackchannelLogoutResponse {
        /** 是否配置了后端登出 URL。 */
        protected boolean withBackchannelLogoutUrl;
        /** HTTP 响应码（可为 null）。 */
        protected Integer responseCode;

        /** @return 是否使用后端登出 URL */
        public boolean isWithBackchannelLogoutUrl() {
            return withBackchannelLogoutUrl;
        }

        /** @param withBackchannelLogoutUrl 是否配置了 URL */
        public void setWithBackchannelLogoutUrl(boolean withBackchannelLogoutUrl) {
            this.withBackchannelLogoutUrl = withBackchannelLogoutUrl;
        }

        /** @return HTTP 响应码（Optional） */
        public Optional<Integer> getResponseCode() {
            return Optional.ofNullable(responseCode);
        }

        /** @param responseCode HTTP 状态码 */
        public void setResponseCode(Integer responseCode) {
            this.responseCode = responseCode;
        }
    }
}
