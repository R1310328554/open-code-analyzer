package org.keycloak.ssf.event.caep;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * {@link CaepCredentialChange.ChangeType} 的 JSON 反序列化器。
 * <p>官方变更类型为 create、revoke、update、delete，部分旧实现使用 created 等非标准形式。</p>
 * <p>参见 https://openid.net/specs/openid-caep-specification-1_0.html#rfc.section.3.3.1</p>
 */
public class CaepCredentialChangeChangeTypeDeserializer extends JsonDeserializer<CaepCredentialChange.ChangeType> {

    private static final Map<String, CaepCredentialChange.ChangeType> CHANGE_TYPE_MAP = new HashMap<>();

    static {
        // some existing SSF transmitters use (older) non standard change type identifiers.
        CHANGE_TYPE_MAP.put("create", CaepCredentialChange.ChangeType.CREATE);
        CHANGE_TYPE_MAP.put("created", CaepCredentialChange.ChangeType.CREATE); // Handle non-standard form

        CHANGE_TYPE_MAP.put("revoke", CaepCredentialChange.ChangeType.REVOKE);
        CHANGE_TYPE_MAP.put("revoked", CaepCredentialChange.ChangeType.REVOKE); // Handle non-standard form

        CHANGE_TYPE_MAP.put("update", CaepCredentialChange.ChangeType.UPDATE);
        CHANGE_TYPE_MAP.put("updated", CaepCredentialChange.ChangeType.UPDATE); // Handle non-standard form

        CHANGE_TYPE_MAP.put("delete", CaepCredentialChange.ChangeType.DELETE);
        CHANGE_TYPE_MAP.put("deleted", CaepCredentialChange.ChangeType.DELETE); // Handle non-standard form
    }

    @Override
    public CaepCredentialChange.ChangeType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().toLowerCase(); // Normalize input
        CaepCredentialChange.ChangeType changeType = CHANGE_TYPE_MAP.get(value);

        if (changeType == null) {
            throw new IOException("Unknown changeType value: " + value);
        }

        return changeType;
    }
}
