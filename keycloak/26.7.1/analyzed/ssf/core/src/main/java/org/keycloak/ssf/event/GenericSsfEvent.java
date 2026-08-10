package org.keycloak.ssf.event;

/**
 * 遇到未知 SSF 事件类型时使用的兜底 {@link SsfEvent} 实现。
 */
public class GenericSsfEvent extends SsfEvent {

    public GenericSsfEvent() {
        super(null);

        // Generic events don't have an alias by default
        setAlias(null);
    }

    @Override
    public String toString() {
        return "GenericSecurityEvent{" +
               "subjectId=" + subjectId +
               ", eventType='" + eventType + '\'' +
               ", eventTimestamp=" + eventTimestamp +
               ", initiatingEntity=" + initiatingEntity +
               ", reasonAdmin=" + reasonAdmin +
               ", reasonUser=" + reasonUser +
               ", attributes=" + attributes +
               '}';
    }
}
