package org.keycloak.testframework.remote.providers.runonserver;

/**
 * 远程 {@link RunOnServer} / {@link FetchOnServer} 执行失败时抛出的运行时异常。
 * <p>
 * 通常包装从服务器传回的已解码 {@link Throwable}。
 */
public class RunOnServerException extends RuntimeException {

    /**
     * 用原始服务器端异常构造包装异常。
     *
     * @param throwable 远程执行产生的根因
     */
    public RunOnServerException(Throwable throwable) {
        super(throwable);
    }

}
