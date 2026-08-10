package org.keycloak.models.utils;

import java.util.function.Function;

import org.keycloak.models.UserModel;

/**
 * 存储不可用时的只读用户委托：强制 {@link #isEnabled()} 为 {@code false}。
 * <p>联邦存储临时不可达时使用自定义异常工厂。</p>
 */
public class StorageUnavailableUserModelDelegate extends ReadOnlyUserModelDelegate {

    /**
     * @param delegate 底层用户
     * @param exceptionCreator 存储不可用时的异常工厂
     */
    public StorageUnavailableUserModelDelegate(UserModel delegate, Function<String, RuntimeException> exceptionCreator) {
        super(delegate, false, exceptionCreator);
    }
}
