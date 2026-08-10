package org.keycloak.services.client.query;

/** 客户端查询解析或校验失败时抛出的运行时异常。 */
public class ClientQueryException extends RuntimeException {

    public ClientQueryException(String message) {
        super(message);
    }
}
