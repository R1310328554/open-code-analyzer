package org.keycloak.ssf.transmitter.delivery;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.ssf.event.token.SseCaepSecurityEventToken;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;
import org.keycloak.ssf.subject.ComplexSubjectId;
import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.transmitter.support.SsfUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 将 SSF 1.0 格式的 {@link SsfSecurityEventToken} 转换为旧版 SSE CAEP 格式的
 * {@link SseCaepSecurityEventToken}，供需要 legacy 事件形状的接收方使用。
 */
public class SseCaepEventConverter {

    /**
     * 从 SSE 风格事件 JSON 中提取嵌套的 {@code subject} 映射。
     *
     * @param events 事件 JSON 节点
     * @return subject 映射，未找到时返回 null
     */
    public static Map<String, Object> extractSseSubjectIdMap(JsonNode events) {
        if (events.isObject() && events.fieldNames().hasNext()) {
            String firstEventKey = events.fieldNames().next();
            JsonNode subject = events.path(firstEventKey).path("subject");
            if (subject.isObject()) {
                return SsfUtil.treeToMap(subject);
            }
        }
        return null;
    }


    /**
     * 将 SSF 1.0 令牌转换为 SSE CAEP 令牌，含事件体字段映射与主体形状调整。
     *
     * @param ssfEventToken SSF 1.0 安全事件令牌
     * @return 转换后的 SSE CAEP 令牌
     */
    public static SseCaepSecurityEventToken convert(SsfSecurityEventToken ssfEventToken) {

        SseCaepSecurityEventToken sseCaepToken = new SseCaepSecurityEventToken();
        sseCaepToken.setJti(ssfEventToken.getJti());
        sseCaepToken.setIss(ssfEventToken.getIss());
        sseCaepToken.setAud(ssfEventToken.getAud());
        sseCaepToken.setIat(ssfEventToken.getIat());

        Map<String, Object> sseCaepEventData = new HashMap<>();
        for (String eventType : ssfEventToken.getEvents().keySet()) {

            Map<String, Object> eventData = new ObjectMapper().convertValue(ssfEventToken.getEvents().get(eventType), Map.class);
            Map<String, Object> adjustedEventData = new HashMap<>();
            adjustedEventData.put("reason_admin", eventData.get("reason_admin"));
            adjustedEventData.put("reason_user", eventData.get("reason_user"));
            adjustedEventData.put("event_timestamp", eventData.get("event_timestamp"));
            adjustedEventData.put("initiating_entity", eventData.get("initiating_entity"));

            adjustedEventData.put("credential_type", eventData.get("credential_type"));
            adjustedEventData.put("change_type", eventData.get("change_type"));

            adjustedEventData.put("subject", buildSseCaepSubject(ssfEventToken.getSubjectId()));

            sseCaepEventData.put(eventType, adjustedEventData);
        }
        sseCaepToken.setEvents(sseCaepEventData);

        return sseCaepToken;
    }

    /**
     * 构建 SSE 1.0 {@code subject} 对象，替代 SSF 1.0 顶层 {@code sub_id}。
     *
     * <p>按 SSE 1.0 §3.2，复合主体将各分面（{@code user}、{@code session}、{@code device}、
     * {@code application}、{@code tenant}、{@code org_unit}、{@code group}）作为
     * {@code subject} 下的同级键。简单（非 complex）主体包装为 {@code user} 分面——
     * 适用于仅携带用户身份的 {@code CaepCredentialChange} 等事件。</p>
     */
    protected static Map<String, Object> buildSseCaepSubject(SubjectId subjectId) {
        Map<String, Object> subjectMap = new HashMap<>();
        if (subjectId == null) {
            return subjectMap;
        }
        if (subjectId instanceof ComplexSubjectId complex) {
            // Flatten each non-null facet into siblings of subject — this
            // is the shape SSE 1.0 receivers expect for complex subjects.
            if (complex.getUser() != null) subjectMap.put("user", complex.getUser());
            if (complex.getSession() != null) subjectMap.put("session", complex.getSession());
            if (complex.getDevice() != null) subjectMap.put("device", complex.getDevice());
            if (complex.getApplication() != null) subjectMap.put("application", complex.getApplication());
            if (complex.getTenant() != null) subjectMap.put("tenant", complex.getTenant());
            if (complex.getOrgUnit() != null) subjectMap.put("org_unit", complex.getOrgUnit());
            if (complex.getGroup() != null) subjectMap.put("group", complex.getGroup());
            return subjectMap;
        }
        subjectMap.put("user", subjectId);
        return subjectMap;
    }
}
