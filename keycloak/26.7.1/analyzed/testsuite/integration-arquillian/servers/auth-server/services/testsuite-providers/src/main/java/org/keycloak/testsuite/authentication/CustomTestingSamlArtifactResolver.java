package org.keycloak.testsuite.authentication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.saml.ArtifactResolver;

import static org.keycloak.testsuite.authentication.CustomTestingSamlArtifactResolverFactory.TYPE_CODE_AND_INDEX;


/**
 * 仅用于测试的 SAML Artifact 解析器，通过内存列表存储并索引 artifact 响应内容。
 */
public class CustomTestingSamlArtifactResolver implements ArtifactResolver {

    /** 内存中保存的 artifact 响应字符串列表，按索引检索。 */
    public static List<String> list = new ArrayList<>();

    /** {@inheritDoc} 测试实现不选择源客户端，始终返回 {@code null}。 */
    @Override
    public ClientModel selectSourceClient(KeycloakSession session, String artifact) {
        return null;
    }

    /**
     * 将 artifact 响应写入内存列表，并编码为 Base64 artifact 字符串。
     *
     * @param clientSessionModel 已认证的客户端会话
     * @param entityId 实体标识符
     * @param artifactResponse 待存储的 artifact 响应内容
     * @return Base64 编码的 artifact 字节序列
     */
    @Override
    public String buildArtifact(AuthenticatedClientSessionModel clientSessionModel, String entityId, String artifactResponse) {
        int artifactIndex = list.size();
        list.add(artifactResponse);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(TYPE_CODE_AND_INDEX);
            bos.write(artifactIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] artifact = bos.toByteArray();
        return Base64.getEncoder().encodeToString(artifact);
    }

    /**
     * 解码 Base64 artifact 并从内存列表中取出对应索引的响应内容。
     *
     * @param clientSessionModel 已认证的客户端会话
     * @param artifact Base64 编码的 artifact 字符串
     * @return 索引对应的 artifact 响应
     */
    @Override
    public String resolveArtifact(AuthenticatedClientSessionModel clientSessionModel, String artifact) {
        byte[] byteArray = Base64.getDecoder().decode(artifact);
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        bis.skip(TYPE_CODE_AND_INDEX.length);
        int index = bis.read();

        return list.get(index);
    }

    /** {@inheritDoc} 测试实现无需释放资源。 */
    @Override
    public void close() {

    }
}
