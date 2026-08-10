package org.keycloak.ssf.services;

import jakarta.ws.rs.Path;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.ssf.Ssf;
import org.keycloak.ssf.transmitter.SsfTransmitter;
import org.keycloak.ssf.transmitter.resources.SsfTransmitterResource;
import org.keycloak.ssf.transmitter.support.SsfAuthUtil;

/**
 * 暴露 realm 级 SSF 资源端点。
 */
public class SsfRealmResourceProvider implements RealmResourceProvider {

    protected final KeycloakSession session;

    public SsfRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    /**
     * SSF 发送方入口。
     *
     * 端点路径：{@code $KC_ISSUER_URL/ssf/transmitter}
     */
    @Path(Ssf.SSF_TRANSMITTER_PATH)
    public SsfTransmitterResource transmitter() {
        if (!Ssf.isTransmitterEnabled(session.getContext().getRealm())) {
            return null;
        }
        var authResult = SsfAuthUtil.authenticate();
        return new SsfTransmitterResource(session, authResult, SsfTransmitter.of(session));
    }

    @Override
    public void close() {
        // NOOP
    }

}
