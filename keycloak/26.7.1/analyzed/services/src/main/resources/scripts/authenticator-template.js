/*
 * 基于 JavaScript 的身份验证器模板。
 * 参见 org.keycloak.authentication.authenticators.browser.ScriptBasedAuthenticatorFactory
 */

// 导入错误码枚举，供 context.failure 使用
AuthenticationFlowError = Java.type("org.keycloak.authentication.AuthenticationFlowError");

/**
 * 示例 authenticate 函数。
 *
 * 以下变量可直接使用：
 * user - 当前用户 {@see org.keycloak.models.UserModel}
 * realm - 当前领域 {@see org.keycloak.models.RealmModel}
 * session - 当前 KeycloakSession {@see org.keycloak.models.KeycloakSession}
 * httpRequest - 当前 HttpRequest {@see org.keycloak.http.HttpRequest}
 * script - 当前脚本 {@see org.keycloak.models.ScriptModel}
 * authenticationSession - 当前认证会话 {@see org.keycloak.sessions.AuthenticationSessionModel}
 * LOG - 当前日志器 {@see org.jboss.logging.Logger}
 *
 * 可通过以下方式读取 HTTP 请求头：
 * httpRequest.getHttpHeaders().getHeaderString("Forwarded")
 *
 * @param context {@see org.keycloak.authentication.AuthenticationFlowContext}
 */
function authenticate(context) {

    var username = user ? user.username : "anonymous";
    LOG.info(script.name + " trace auth for: " + username);

    // 演示失败分支；设为 true 可触发 INVALID_USER 错误
    if (authShouldFail) {

        context.failure(AuthenticationFlowError.INVALID_USER);
        return;
    }

    context.success();
}