package org.keycloak.ssf.stream;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Receiver 创建 SSF 事件流的请求体。
 * <p>指定请求的事件类型、投递方式及可选描述。</p>
 */
public class CreateStreamRequest {

        /** Receiver 提供，OPTIONAL。Receiver 请求的事件类型 URI 集合；SHOULD 仅请求可理解且可处理的事件；Transmitter MUST 忽略不认识的值；SHOULD NOT 为空。 */

        @JsonProperty("events_requested")
        private Set<String> eventsRequested;

        /** Receiver 提供，OPTIONAL。SET 投递方式配置对象；{@code method} 键标识具体投递 URI（见规范 §10.3.1）。 */

        @JsonProperty("delivery")
        private AbstractDeliveryMethodRepresentation delivery;

        /** Receiver 提供，OPTIONAL。流的描述字符串，便于多流场景下人工识别；Transmitter MAY 截断超长内容。 */

        @JsonProperty("description")
        private String description;

        public Set<String> getEventsRequested() {
            return eventsRequested;
        }

        public void setEventsRequested(Set<String> eventsRequested) {
            this.eventsRequested = eventsRequested;
        }

        public AbstractDeliveryMethodRepresentation getDelivery() {
            return delivery;
        }

        public void setDelivery(AbstractDeliveryMethodRepresentation delivery) {
            this.delivery = delivery;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
