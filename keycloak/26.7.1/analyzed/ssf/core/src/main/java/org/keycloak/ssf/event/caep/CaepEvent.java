package org.keycloak.ssf.event.caep;

import org.keycloak.ssf.event.SsfEvent;

/**
 * CAEP（Continuous Access Evaluation Profile）事件抽象基类。
 * <p>参见 https://openid.net/specs/openid-caep-1_0-final.html</p>
 */
public abstract class CaepEvent extends SsfEvent {

    public CaepEvent(String type) {
        super(type);
    }
}
