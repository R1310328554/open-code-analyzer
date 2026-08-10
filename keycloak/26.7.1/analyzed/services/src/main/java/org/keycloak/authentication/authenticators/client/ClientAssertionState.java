package org.keycloak.authentication.authenticators.client;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.ClientAuthenticationFlowContextSupplier;
import org.keycloak.events.Details;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.representations.JsonWebToken;

/**
 * 客户端断言状态：从 OAuth 请求中解析 client_assertion_type、client_assertion 及 JWT 内容，供 JWT 客户端认证校验器使用。
 */
public class ClientAssertionState {

    /** 从请求参数构建状态的 Supplier 单例。 */
    private static final Supplier SUPPLIER = new Supplier();

    /** 已解析的客户端模型（校验后设置）。 */
    private ClientModel client;
    /** client_assertion_type 参数值。 */
    private final String clientAssertionType;
    /** client_assertion 参数值（JWT 字符串）。 */
    private final String clientAssertion;
    /** 解析后的 JWS 输入。 */
    private final JWSInput jws;
    /** 解析后的 JWT 载荷。 */
    private final JsonWebToken token;

    public ClientAssertionState(String clientAssertionType, String clientAssertion, JWSInput jws, JsonWebToken token) {
        this.clientAssertionType = clientAssertionType;
        this.clientAssertion = clientAssertion;
        this.jws = jws;
        this.token = token;
    }

    /** 设置已校验通过的客户端模型。 */
    public void setClient(ClientModel client) {
        this.client = client;
    }

    public String getClientAssertionType() {
        return clientAssertionType;
    }

    public String getClientAssertion() {
        return clientAssertion;
    }

    public JWSInput getJws() {
        return jws;
    }

    public JsonWebToken getToken() {
        return token;
    }

    public ClientModel getClient() {
        return client;
    }

    /** @return 从 HTTP 请求构建 {@link ClientAssertionState} 的 Supplier */
    public static ClientAuthenticationFlowContextSupplier<ClientAssertionState> supplier() {
        return SUPPLIER;
    }

    /** 从表单参数解析 client_assertion 并记录审计事件详情。 */
    private static class Supplier implements ClientAuthenticationFlowContextSupplier<ClientAssertionState> {

        @Override
        public ClientAssertionState get(ClientAuthenticationFlowContext context) throws JWSInputException {
            MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();

            String clientAssertionType = params.getFirst(OAuth2Constants.CLIENT_ASSERTION_TYPE);
            String clientAssertion = params.getFirst(OAuth2Constants.CLIENT_ASSERTION);

            JWSInput jws = null;
            JsonWebToken token = null;

            if (clientAssertion != null) {
                jws = new JWSInput(clientAssertion);
                token = jws.readJsonContent(JsonWebToken.class);

                var event = context.getEvent();
                event.detail(Details.CLIENT_ASSERTION_ID, token.getId());
                event.detail(Details.CLIENT_ASSERTION_ISSUER, token.getIssuer());
                event.detail(Details.CLIENT_ASSERTION_SUB, token.getSubject());
            }

            return new ClientAssertionState(clientAssertionType, clientAssertion, jws, token);
        }

    }

}
