package org.keycloak.crypto.fips;

import java.security.Key;
import java.security.SecureRandom;

import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEHeader.JWEHeaderBuilder;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;

import org.bouncycastle.crypto.KeyUnwrapperUsingSecureRandom;
import org.bouncycastle.crypto.KeyWrapperUsingSecureRandom;
import org.bouncycastle.crypto.asymmetric.AsymmetricRSAPrivateKey;
import org.bouncycastle.crypto.asymmetric.AsymmetricRSAPublicKey;
import org.bouncycastle.crypto.fips.FipsRSA;

/**
 * FIPS 140-2 环境下的 RSA 密钥封装 JWE 算法提供器（PKCS#1 v1.5 或 OAEP）。
 * <p>
 * 批准模式下 RSA 仅可用于密钥封装/传输，故通过 {@link FipsRSA.KeyWrapOperatorFactory} 封装 CEK。
 */
public class FIPSRsaKeyEncryptionJWEAlgorithmProvider implements JWEAlgorithmProvider {

    private final FipsRSA.WrapParameters wrapParameters;

    /**
     * @param wrapParameters RSA 封装参数（PKCS1v1_5 或 OAEP 变体）
     */
    public FIPSRsaKeyEncryptionJWEAlgorithmProvider(FipsRSA.WrapParameters wrapParameters) {
        this.wrapParameters = wrapParameters;
    }

    /** {@inheritDoc} 使用 RSA 私钥解封 CEK。 */
    @Override
    public byte[] decodeCek(byte[] encodedCek, Key privateKey, JWEHeader header, JWEEncryptionProvider encryptionProvider) throws Exception {
        AsymmetricRSAPrivateKey rsaPrivateKey =
                new AsymmetricRSAPrivateKey(FipsRSA.ALGORITHM, privateKey.getEncoded());

        FipsRSA.KeyWrapOperatorFactory wrapFact =
                new FipsRSA.KeyWrapOperatorFactory();
        KeyUnwrapperUsingSecureRandom<FipsRSA.WrapParameters> unwrapper =
                wrapFact.createKeyUnwrapper(rsaPrivateKey, wrapParameters)
                        .withSecureRandom(SecureRandom.getInstance("DEFAULT"));
        return unwrapper.unwrap(encodedCek, 0, encodedCek.length);
    }


    /** {@inheritDoc} 使用 RSA 公钥封装 CEK。 */
    @Override
    public byte[] encodeCek(JWEEncryptionProvider encryptionProvider, JWEKeyStorage keyStorage, Key publicKey, JWEHeaderBuilder headerBuilder) throws Exception {
        AsymmetricRSAPublicKey rsaPubKey =
                new AsymmetricRSAPublicKey(FipsRSA.ALGORITHM, publicKey.getEncoded());
        byte[] inputKeyBytes = keyStorage.getCekBytes();
        FipsRSA.KeyWrapOperatorFactory wrapFact =
                new FipsRSA.KeyWrapOperatorFactory();

        KeyWrapperUsingSecureRandom<FipsRSA.WrapParameters> wrapper =
                wrapFact.createKeyWrapper(rsaPubKey, wrapParameters).withSecureRandom( SecureRandom.getInstance("DEFAULT"));
        return wrapper.wrap(inputKeyBytes, 0, inputKeyBytes.length);
    }

}
