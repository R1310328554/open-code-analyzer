package org.keycloak.protocol.docker.installation.compose;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.AbstractMap;
import java.util.Map;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.CertificateUtils;
import org.keycloak.crypto.KeyType;

/**
 * Docker Compose 安装 ZIP 中的证书目录内容：localhost Registry TLS 证书/私钥及 IdP 信任链。
 * <p>Registry 证书为自签名 V1 证书，IdP 信任链来自 Realm 活跃 RSA 密钥证书。</p>
 */
public class DockerComposeCertsDirectory {

    private final String directoryName;
    private final Map.Entry<String, byte[]> localhostCertFile;
    private final Map.Entry<String, byte[]> localhostKeyFile;
    private final Map.Entry<String, byte[]> idpTrustChainFile;

    /**
     * 生成证书目录下三个 PEM 文件条目。
     * @param directoryName 目录名
     * @param realmCert Realm RSA 公钥证书（IdP 信任链）
     * @param registryCertFilename localhost 证书文件名
     * @param registryKeyFilename localhost 私钥文件名
     * @param idpCertTrustChainFilename IdP 信任链文件名
     * @param realmName 用于自签名证书 CN
     */
    public DockerComposeCertsDirectory(final String directoryName, final Certificate realmCert, final String registryCertFilename, final String registryKeyFilename, final String idpCertTrustChainFilename, final String realmName) {
        this.directoryName = directoryName;

        try {
            final KeyPairGenerator keyGen = CryptoIntegration.getProvider().getKeyPairGen(KeyType.RSA);
            keyGen.initialize(2048, new SecureRandom());

            final KeyPair keypair = keyGen.generateKeyPair();
            final PrivateKey privateKey = keypair.getPrivate();
            final Certificate certificate = CertificateUtils.generateV1SelfSignedCertificate(keypair, realmName);

            localhostCertFile = new AbstractMap.SimpleImmutableEntry<>(registryCertFilename, DockerCertFileUtils.formatCrtFileContents(certificate).getBytes());
            localhostKeyFile = new AbstractMap.SimpleImmutableEntry<>(registryKeyFilename, DockerCertFileUtils.formatPrivateKeyContents(privateKey).getBytes());
            idpTrustChainFile = new AbstractMap.SimpleEntry<>(idpCertTrustChainFilename, DockerCertFileUtils.formatCrtFileContents(realmCert).getBytes());

        } catch (NoSuchAlgorithmException | NoSuchProviderException | CertificateEncodingException e) {
            // TODO：应抛出更具描述性的错误
            throw new RuntimeException(e);
        }
    }

    /** @return 证书目录名称 */
    public String getDirectoryName() {
        return directoryName;
    }

    /** @return localhost Registry TLS 证书（文件名 → PEM 字节） */
    public Map.Entry<String, byte[]> getLocalhostCertFile() {
        return localhostCertFile;
    }

    /** @return localhost Registry TLS 私钥（文件名 → PEM 字节） */
    public Map.Entry<String, byte[]> getLocalhostKeyFile() {
        return localhostKeyFile;
    }

    /** @return Keycloak IdP 信任链证书（文件名 → PEM 字节） */
    public Map.Entry<String, byte[]> getIdpTrustChainFile() {
        return idpTrustChainFile;
    }
}
