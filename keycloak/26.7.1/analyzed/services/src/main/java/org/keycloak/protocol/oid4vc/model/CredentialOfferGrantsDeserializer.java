package org.keycloak.protocol.oid4vc.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import static org.keycloak.protocol.oid4vc.model.AuthorizationCodeGrant.AUTH_CODE_GRANT_TYPE;
import static org.keycloak.protocol.oid4vc.model.PreAuthorizedCodeGrant.PRE_AUTH_GRANT_TYPE;

/**
 * 凭证发放 {@code grants} 对象的 Jackson 反序列化器。
 * <p>按 JSON 键名（授权类型）将各 grant 条目映射为 {@link AuthorizationCodeGrant} 或 {@link PreAuthorizedCodeGrant} 实例。</p>
 */
public final class CredentialOfferGrantsDeserializer extends JsonDeserializer<Map<String, CredentialOfferGrant>> {

    /**
     * 将 JSON 对象反序列化为 grant 类型键到 {@link CredentialOfferGrant} 的有序映射。
     * @param p JSON 解析器
     * @param ctx 反序列化上下文
     * @return 授权类型键到 grant 对象的映射
     * @throws InvalidFormatException 遇到未知 grant 类型键时抛出
     */
    @Override
    public Map<String, CredentialOfferGrant> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {

        Map<String, CredentialOfferGrant> grants = new LinkedHashMap<>();
        var mapper = (ObjectMapper) p.getCodec();
        var node = mapper.readTree(p);

        var fields = node.fieldNames();
        while (fields.hasNext()) {
            var grantType = fields.next();
            var valueNode = node.get(grantType);

            Class<? extends CredentialOfferGrant> target =
                    switch (grantType) {
                        case AUTH_CODE_GRANT_TYPE -> AuthorizationCodeGrant.class;
                        case PRE_AUTH_GRANT_TYPE -> PreAuthorizedCodeGrant.class;
                        default -> throw new InvalidFormatException(
                                p, "Unknown grant type key: " + grantType, grantType, CredentialOfferGrant.class);
                    };

            CredentialOfferGrant grant = mapper.treeToValue(valueNode, target);
            grants.put(grantType, grant);
        }
        return grants;
    }
}
