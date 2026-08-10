package org.keycloak.testframework.crypto;

import java.io.File;

import org.keycloak.representations.idm.CertificateRepresentation;

/**
 * 密钥库元数据封装，持有 {@link CertificateRepresentation} 与磁盘上的密钥库文件。
 * <p>
 * 测试框架在加载或生成测试用证书/密钥时，通过本类型统一传递证书信息与文件引用。
 */
public class KeystoreInfo {
    /** 证书元数据表示。 */
    private final CertificateRepresentation certificateInfo;
    /** 密钥库文件路径对象。 */
    private final File keystoreFile;

    /**
     * 构造密钥库信息。
     *
     * @param certificateInfo 证书表示
     * @param keystoreFile 密钥库文件
     */
    KeystoreInfo(CertificateRepresentation certificateInfo, File keystoreFile) {
        this.certificateInfo = certificateInfo;
        this.keystoreFile = keystoreFile;
    }

    /** 返回证书元数据表示。 */
    public CertificateRepresentation getCertificateInfo() {
        return certificateInfo;
    }

    /** 返回密钥库文件。 */
    public File getKeystoreFile() {
        return keystoreFile;
    }
}
