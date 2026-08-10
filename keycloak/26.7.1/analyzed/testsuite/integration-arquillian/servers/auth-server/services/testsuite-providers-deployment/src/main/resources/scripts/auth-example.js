// 认证脚本示例：演示基于用户名的成功/失败分支
AuthenticationFlowError = Java.type("org.keycloak.authentication.AuthenticationFlowError");

function authenticate(context) {
    // 记录当前认证用户，便于集成测试追踪脚本执行路径
    LOG.info(script.name + " --> trace auth for: " + user.username);
    // 用户名为 fail 时模拟无效用户，触发认证失败
    if (user.username === "fail") {
        context.failure(AuthenticationFlowError.INVALID_USER);
        return;
    }
    // 其余用户直接标记认证成功
    context.success();
}
