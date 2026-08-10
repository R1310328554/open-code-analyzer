package org.keycloak.crypto.fips;

import java.security.Key;

import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEHeader.JWEHeaderBuilder;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;

import org.bouncycastle.crypto.KeyUnwrapper;
import org.bouncycastle.crypto.KeyWrapper;
import org.bouncycastle.crypto.SymmetricKey;
import org.bouncycastle.crypto.SymmetricSecretKey;
import org.bouncycastle.crypto.fips.FipsAES;

/**
 * FIPS 140-2 环境下的 AES Key Wrap JWE 算法提供器。
 * <p>
 * {@link org.keycloak.jose.jwe.alg.AesKeyWrapAlgorithmProvider} 的 BCFIPS 变体。
 */
public class FIPSAesKeyWrapAlgorithmProvider implements JWEAlgorithmProvider {

    /** {@inheritDoc} 使用 BCFIPS AES Key Wrap 解封 CEK。 */
    @Override
    public byte[] decodeCek(byte[] encodedCek, Key encryptionKey, JWEHeader header, JWEEncryptionProvider encryptionProvider) throws Exception {
        byte[] keyBytes = encryptionKey.getEncoded(); // 用于 AES Wrap 的密钥字节
        SymmetricKey aesKey = new SymmetricSecretKey(FipsAES.KW, keyBytes);
        FipsAES.KeyWrapOperatorFactory factory = new FipsAES.KeyWrapOperatorFactory();
        KeyUnwrapper unwrapper = factory.createKeyUnwrapper(aesKey, FipsAES.KW);
        return unwrapper.unwrap(encodedCek, 0, encodedCek.length);
    }

    /** {@inheritDoc} 使用 BCFIPS AES Key Wrap 封装 CEK。 */
    @Override
    public byte[] encodeCek(JWEEncryptionProvider encryptionProvider, JWEKeyStorage keyStorage, Key encryptionKey, JWEHeaderBuilder headerBuilder) throws Exception {
        byte[] inputKeyBytes = keyStorage.getCekBytes(); // 待封装的 CEK 字节
        byte[] keyBytes = encryptionKey.getEncoded(); // bytes making up AES key doing the wrapping
        SymmetricKey aesKey = new SymmetricSecretKey(FipsAES.KW, keyBytes);
        FipsAES.KeyWrapOperatorFactory factory = new FipsAES.KeyWrapOperatorFactory();
        KeyWrapper wrapper = factory.createKeyWrapper(aesKey, FipsAES.KW);
        return wrapper.wrap(inputKeyBytes, 0, inputKeyBytes.length);
    }
}
