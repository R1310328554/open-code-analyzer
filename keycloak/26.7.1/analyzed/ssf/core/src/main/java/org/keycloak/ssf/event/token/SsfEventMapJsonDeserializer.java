package org.keycloak.ssf.event.token;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.ssf.Ssf;
import org.keycloak.ssf.event.GenericSsfEvent;
import org.keycloak.ssf.event.SsfEvent;
import org.keycloak.ssf.event.SsfEventProvider;
import org.keycloak.ssf.event.SsfEventRegistry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SET {@code events} 声明的自定义 Jackson 反序列化器。
 * <p>{@code events} 声明包含一组事件语句，每条描述关于安全主体发生的一个逻辑事件
 * （如主体状态变更）。同一事件标识符 MUST NOT 重复；MUST NOT 用单一 {@code events}
 * 声明表达多个独立逻辑事件。</p>
 * <p>值为 JSON 对象，成员名为标识事件语句的 URI，对应值 MUST 为 JSON 对象
 * （可为空对象 {@code {}}，或按 profiling 规范携带数据）。</p>
 * <p>定义见 https://datatracker.ietf.org/doc/html/rfc8417#section-2.2</p>
 */
public class SsfEventMapJsonDeserializer extends JsonDeserializer<Map<String, SsfEvent>> {

    @Override
    public Map<String, SsfEvent> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        SsfEventRegistry registry = resolveRegistry();

        Map<String, SsfEvent> eventsMap = new HashMap<>();

        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String eventType = entry.getKey();  // Extracts event type key
            JsonNode eventData = entry.getValue(); // Extracts event data

            Class<? extends SsfEvent> eventClass = resolveEventClass(registry, eventType);

            SsfEvent event = mapper.treeToValue(eventData, eventClass);
            event.setEventType(eventType);  // Manually set event type since it's not in JSON
            eventsMap.put(eventType, event);
        }

        return eventsMap;
    }

    /**
     * 解析用于将事件类型 URI 映射到具体 {@link SsfEvent} 子类的 {@link SsfEventRegistry}。
     * <p>当前线程绑定 Keycloak 会话时使用 per-session {@link SsfEventProvider}；
     * 否则返回 {@code null}，反序列化器对所有事件类型降级为 {@link GenericSsfEvent}，
     * 避免 NPE——使 SET 解析在请求作用域外（如测试、后台 worker）仍可用。</p>
     */
    protected SsfEventRegistry resolveRegistry() {

        SsfEventProvider events = Ssf.events();
        if (events == null) {
            return null;
        }
        return events.getRegistry();
    }

    protected Class<? extends SsfEvent> resolveEventClass(SsfEventRegistry registry, String eventType) {
        if (registry == null) {
            return GenericSsfEvent.class;
        }
        return registry.getEventClassByType(eventType).orElse(GenericSsfEvent.class);
    }
}
