package org.keycloak.encoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

import org.keycloak.theme.ResourceLoader;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Gzip 资源编码提供者：将主题/静态资源压缩为 {@code .gz} 缓存文件。
 * <p>首次请求时生成压缩缓存，后续直接读取磁盘缓存以提升传输效率。</p>
 */
public class GzipResourceEncodingProvider implements ResourceEncodingProvider {

    private static final Logger logger = Logger.getLogger(ResourceEncodingProvider.class);

    /** Gzip 压缩文件缓存根目录。 */
    private final File cacheDir;

    /** @param cacheDir Gzip 缓存目录 */
    public GzipResourceEncodingProvider(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    /** @param producer 原始资源流供应器 @param path 资源路径片段 @return Gzip 编码后的输入流，失败时返回 {@code null} */
    public InputStream getEncodedStream(StreamSupplier producer, String... path) {
        try {
            File encodedFile = ResourceLoader.getFile(cacheDir, String.join("/", path) +  ".gz");
            if (encodedFile == null) {
                return null;
            }

            if (!encodedFile.exists()) {
                encodedFile = createEncodedFile(producer, encodedFile);
            }

            return encodedFile != null ? new FileInputStream(encodedFile) : null;
        } catch (Exception e) {
            logger.warn("Failed to encode resource", e);
            return null;
        }
    }

    /** @return 编码标识 {@code gzip} */
    public String getEncoding() {
        return "gzip";
    }

    /** 将原始资源压缩写入目标缓存文件。 */
    private File createEncodedFile(StreamSupplier producer, File target) throws IOException {
        InputStream is = producer.getInputStream();
        if (is == null) {
            return null;
        }

        File parent = target.getParentFile();
        if (!parent.isDirectory()) {
            if (parent.mkdirs() && !parent.isDirectory()) {
                logger.warnf("Fail to create cache directory %s", parent.toString());
            }
        }
        File tmpEncodedFile = File.createTempFile(target.getName(), "tmp", parent);

        try (is; GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(tmpEncodedFile))) {
            IOUtils.copy(is, gos);
        }

        try {
            Files.move(tmpEncodedFile.toPath(), target.toPath(), REPLACE_EXISTING);
            return target;
        } catch (IOException io) {
            logger.warnf(io, "Fail to move temporary file to %s", target.toString());
            return null;
        }
    }

}
