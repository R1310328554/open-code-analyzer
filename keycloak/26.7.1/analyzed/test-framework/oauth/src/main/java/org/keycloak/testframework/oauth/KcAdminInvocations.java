package org.keycloak.testframework.oauth;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.keycloak.representations.LogoutToken;
import org.keycloak.representations.adapters.action.LogoutAction;
import org.keycloak.representations.adapters.action.PushNotBeforeAction;
import org.keycloak.representations.adapters.action.TestAvailabilityAction;

/**
 * 收集 Keycloak 向模拟 OAuth 客户端发出的管理回调与 front-channel 登出事件。
 * <p>
 * 各类型事件存入独立阻塞队列，测试代码可在限定超时内轮询断言。
 */
public class KcAdminInvocations {

    private final BlockingQueue<LogoutAction> adminLogoutActions = new LinkedBlockingQueue<>();
    private final BlockingQueue<LogoutToken> frontChannelLogoutTokens = new LinkedBlockingQueue<>();
    private final BlockingQueue<PushNotBeforeAction> adminPushNotBeforeActions = new LinkedBlockingQueue<>();
    private final BlockingQueue<TestAvailabilityAction> adminTestAvailabilityAction = new LinkedBlockingQueue<>();

    /** 创建空的调用记录器。 */
    KcAdminInvocations() {
    }

    /**
     * 等待并取出管理推送 not-before 动作。
     *
     * @return 收到的动作；超时未收到时返回 {@code null}
     * @throws InterruptedException 等待被中断
     */
    public PushNotBeforeAction getAdminPushNotBefore() throws InterruptedException {
        return adminPushNotBeforeActions.poll(10, TimeUnit.SECONDS);
    }

    /** 记录一条管理推送 not-before 动作。 */
    void add(PushNotBeforeAction action) {
        adminPushNotBeforeActions.add(action);
    }

    /**
     * 等待并取出可用性探测动作。
     *
     * @return 收到的动作；超时未收到时返回 {@code null}
     * @throws InterruptedException 等待被中断
     */
    public TestAvailabilityAction getTestAvailable() throws InterruptedException {
        return adminTestAvailabilityAction.poll(10, TimeUnit.SECONDS);
    }

    /** 记录一条可用性探测动作。 */
    void add(TestAvailabilityAction action) {
        adminTestAvailabilityAction.add(action);
    }

    /**
     * 等待并取出管理登出动作。
     *
     * @return 收到的动作；超时未收到时返回 {@code null}
     * @throws InterruptedException 等待被中断
     */
    public LogoutAction getAdminLogoutAction() throws InterruptedException {
        return adminLogoutActions.poll(10, TimeUnit.SECONDS);
    }

    /** 记录一条管理登出动作。 */
    void add(LogoutAction action) {
        adminLogoutActions.add(action);
    }

    /**
     * 等待并取出 front-channel 登出令牌。
     *
     * @return 收到的 {@link LogoutToken}；超时未收到时返回 {@code null}
     * @throws InterruptedException 等待被中断
     */
    public LogoutToken getFrontChannelLogoutToken() throws InterruptedException {
        return frontChannelLogoutTokens.poll(10, TimeUnit.SECONDS);
    }

    /** 记录一条 front-channel 登出令牌。 */
    void add(LogoutToken token) {
        frontChannelLogoutTokens.add(token);
    }

    /** 清空所有已记录的回调与登出事件。 */
    public void clear() {
        adminLogoutActions.clear();
        frontChannelLogoutTokens.clear();
        adminPushNotBeforeActions.clear();
        adminTestAvailabilityAction.clear();
    }

}
