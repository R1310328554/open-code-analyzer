package org.keycloak.encoding;

import java.io.IOException;
import java.io.InputStream;

import org.keycloak.provider.Provider;

/**
 * 资源编码提供者 SPI：对主题/静态资源进行传输编码（如 Gzip）。
 * <p>由 {@link ResourceEncodingProviderFactory} 创建，供 {@link ResourceEncodingHelper} 按 Accept-Encoding 选择。</p>
 */
public interface ResourceEncodingProvider extends Provider {

    /** @param producer 原始资源流供应器 @param path 资源路径片段 @return 编码后的输入流 */
    InputStream getEncodedStream(StreamSupplier producer, String... path);

    /** @return 编码标识（如 {@code gzip}），对应 Accept-Encoding 值 */
    String getEncoding();

    @Override
    default void close() {
    }

    /** 延迟提供原始资源输入流的回调接口。 */
    interface StreamSupplier {

        /** @return 原始资源输入流 */
        InputStream getInputStream() throws IOException;

    }

}
