package org.keycloak.testframework.remote.providers.runonserver;

import java.io.IOException;
import java.io.Serializable;

import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;

/**
 * 在 Keycloak 服务器端执行、无返回值的远程可序列化任务。
 * <p>
 * 典型用法是修改服务器内部状态或触发副作用；执行完成后 HTTP 响应体为空。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface RunOnServer extends Serializable {

    /**
     * 在服务器 {@link KeycloakSession} 上下文中执行任务。
     *
     * @param session 当前 Keycloak 会话
     * @throws IOException 序列化或 I/O 失败时抛出
     * @throws VerificationException 校验失败时抛出
     * @throws ClassNotFoundException 依赖类无法加载时抛出
     */
    void run(KeycloakSession session) throws IOException, VerificationException, ClassNotFoundException;

}
