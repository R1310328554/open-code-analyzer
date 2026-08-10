package org.keycloak.transaction;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;

/**
 * 异步响应事务包装器：在代码中直接调用 {@link AsyncResponse#resume(Object)} 时，HTTP 响应会在
 * 当前执行上下文的所有变更提交之前返回。因此需要将 {@link AsyncResponseTransaction} 实例登记到
 * 主事务的完成后回调中，待变更成功提交后再恢复 {@link AsyncResponse}。
 * <p>通过 {@link org.keycloak.models.KeycloakTransactionManager#enlistAfterCompletion(KeycloakTransaction)} 实现。</p>
 */
public class AsyncResponseTransaction implements KeycloakTransaction {

    private final KeycloakSession session;
    private final AsyncResponse responseToFinishInTransaction;
    private final Response responseToSend;

    /**
     * 创建 {@link AsyncResponseTransaction} 并在事务提交/回滚时恢复指定的 {@link AsyncResponse}。
     * 成功提交时发送 {@code responseToSend}，回滚时返回内部错误页。
     *
     * @param session 当前 {@link KeycloakSession}
     * @param responseToFinishInTransaction 待恢复完成的 {@link AsyncResponse}
     * @param responseToSend 提交成功时要发送的 {@link Response}
     */
    public static void finishAsyncResponseInTransaction(KeycloakSession session, AsyncResponse responseToFinishInTransaction, Response responseToSend) {
        session.getTransactionManager().enlistAfterCompletion(new AsyncResponseTransaction(session, responseToFinishInTransaction, responseToSend));
    }
    
    private AsyncResponseTransaction(KeycloakSession session, AsyncResponse responseToFinishInTransaction, Response responseToSend) {
        this.session = session;
        this.responseToFinishInTransaction = responseToFinishInTransaction;
        this.responseToSend = responseToSend;
    }

    @Override
    public void begin() {

    }

    /** 事务提交成功后恢复异步响应并发送响应体。 */
    @Override
    public void commit() {
        responseToFinishInTransaction.resume(responseToSend);
    }

    /** 事务回滚时恢复异步响应并返回内部服务器错误页。 */
    @Override
    public void rollback() {
        responseToFinishInTransaction.resume(ErrorPage.error(session, null, Response.Status.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR));
    }

    @Override
    public void setRollbackOnly() {

    }

    @Override
    public boolean getRollbackOnly() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
