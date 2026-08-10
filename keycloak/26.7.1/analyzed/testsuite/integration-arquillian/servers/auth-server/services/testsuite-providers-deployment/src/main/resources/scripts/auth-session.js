// 认证会话校验脚本：验证 realm、client 与协议是否符合测试预期
AuthenticationFlowError = Java.type("org.keycloak.authentication.AuthenticationFlowError");

function authenticate(context) {

    // 仅允许 test 领域继续认证
    if (authenticationSession.getRealm().getName() != "test") {
        context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
        return;
    }

    // 客户端 ID 必须为 test-app
    if (authenticationSession.getClient().getClientId() != "test-app") {
        context.failure(AuthenticationFlowError.UNKNOWN_CLIENT);
        return;
    }

    // 协议须为 openid-connect
    if (authenticationSession.getProtocol() != "openid-connect") {
        context.failure(AuthenticationFlowError.INVALID_CLIENT_SESSION);
        return;
    }

    // 三项校验均通过则认证成功
    context.success();
}
