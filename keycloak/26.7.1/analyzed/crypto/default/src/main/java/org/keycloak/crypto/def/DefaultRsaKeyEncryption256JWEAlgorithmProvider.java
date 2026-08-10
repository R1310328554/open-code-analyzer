package org.keycloak.crypto.def;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * RSA-OAEP-256 JWE 算法提供器，使用 SHA-256 作为 OAEP 哈希与 MGF1 参数。
 */
public class DefaultRsaKeyEncryption256JWEAlgorithmProvider extends DefaultRsaKeyEncryptionJWEAlgorithmProvider {

    /**
     * @param jcaAlgorithmName JCA 加密算法名称
     */
    public DefaultRsaKeyEncryption256JWEAlgorithmProvider(String jcaAlgorithmName) {
        super(jcaAlgorithmName);
    }

    /** 使用 SHA-256 OAEP 参数初始化 Cipher。 */
    @Override
    protected void initCipher(Cipher cipher, int mode, Key key) throws Exception {
        AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP");
        AlgorithmParameterSpec paramSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT);
        algp.init(paramSpec);
        cipher.init(mode, key, algp);
    }
}
