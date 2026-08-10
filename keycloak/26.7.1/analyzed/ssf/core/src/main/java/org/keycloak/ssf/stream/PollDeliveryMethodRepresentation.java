package org.keycloak.ssf.stream;

import java.net.URI;

/**
 * POLL 投递方式的流配置表示：接收方通过 HTTP GET 从发送方拉取安全事件令牌（SET）。
 * <p>对应 {@link DeliveryMethod#POLL}，参见 SSF 规范 10.3.1 与 RFC 8936。</p>
 */
public class PollDeliveryMethodRepresentation extends AbstractDeliveryMethodRepresentation {

    public PollDeliveryMethodRepresentation(URI endpointUrl) {
        super(DeliveryMethod.POLL, endpointUrl);
    }
}
