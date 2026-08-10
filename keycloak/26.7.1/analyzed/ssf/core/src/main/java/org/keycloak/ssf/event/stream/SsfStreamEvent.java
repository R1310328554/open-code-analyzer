package org.keycloak.ssf.event.stream;

import org.keycloak.ssf.event.SsfEvent;

/**
 * 所有 SSF 流（stream）相关事件的基类。
 */
public abstract class SsfStreamEvent extends SsfEvent {

    public SsfStreamEvent(String eventType) {
        super(eventType);
    }
}
