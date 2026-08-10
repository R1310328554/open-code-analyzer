package org.keycloak.testframework.mail;

import jakarta.mail.internet.MimeMessage;

import org.keycloak.testframework.injection.ManagedTestResource;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.TokenValidator;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * 接收 Keycloak 服务器发出的邮件的测试资源。
 * <p>
 * 每次测试执行时会清空邮箱，因此 {@link #getReceivedMessages()} 仅返回当前测试期间收到的邮件。
 */
public class MailServer extends ManagedTestResource {

    private final GreenMail greenMail;

    /**
     * 在指定主机与端口启动 GreenMail SMTP 服务。
     *
     * @param host SMTP 监听主机
     * @param port SMTP 监听端口
     */
    public MailServer(String host, int port) {
        ServerSetup setup = new ServerSetup(port, host, "smtp");

        greenMail = new GreenMail(setup);
        greenMail.start();
    }

    /** 停止 GreenMail 服务器。 */
    public void stop() {
        greenMail.stop();
    }

    /**
     * 配置 SMTP 认证用户名与密码。
     *
     * @param username SMTP 用户名
     * @param password SMTP 密码
     */
    public void credentials(String username, String password) {
        greenMail.setUser(username, password);
    }

    /**
     * 配置 SMTP 认证用户名与自定义令牌校验器。
     *
     * @param username SMTP 用户名
     * @param validator OAuth 等场景的令牌校验器
     */
    public void credentials(String username, TokenValidator validator) {
        greenMail.setUser(username, null);
        GreenMailUser user = greenMail.getUserManager().getUser(username);
        // GreenMail 尚未公开 TokenValidator API，暂通过 UserImpl 设置；见 greenmail#838
        ((com.icegreen.greenmail.user.UserImpl)user).setTokenValidator(validator);
    }

    /**
     * 获取当前测试期间收到的全部邮件。
     *
     * @return 已接收的 MIME 邮件数组
     */
    public MimeMessage[] getReceivedMessages() {
        return greenMail.getReceivedMessages();
    }

    /**
     * 获取最后一封收到的邮件。
     *
     * @return 最后一封 MIME 邮件，若无邮件则返回 {@code null}
     */
    public MimeMessage getLastReceivedMessage() {
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        return receivedMessages != null && receivedMessages.length > 0 ? receivedMessages[receivedMessages.length - 1] : null;
    }

    /**
     * 在指定超时时间内等待收到指定数量的邮件。
     *
     * @param timeout 最长等待时间（毫秒）
     * @param emailCount 期望收到的邮件数量
     * @return 若在超时前收到足够邮件则返回 {@code true}
     */
    public boolean waitForIncomingEmail(long timeout, int emailCount) {
        return greenMail.waitForIncomingEmail(timeout, emailCount);
    }

    /**
     * 使用默认超时等待收到指定数量的邮件。
     *
     * @param emailCount 期望收到的邮件数量
     * @return 若在默认超时前收到足够邮件则返回 {@code true}
     */
    public boolean waitForIncomingEmail(int emailCount) {
        return greenMail.waitForIncomingEmail(emailCount);
    }

    /** {@inheritDoc} 清空所有邮箱中的邮件。 */
    @Override
    public void runCleanup() {
        try {
            greenMail.purgeEmailFromAllMailboxes();
        } catch (FolderException e) {
            throw new RuntimeException(e);
        }
    }
}
