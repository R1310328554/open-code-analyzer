/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.crypto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 公钥集合包装：持有 JWKS 密钥列表及可选的缓存过期时间，用于按 kid/alg 查找匹配密钥。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PublicKeysWrapper {

    /** JWKS 中的密钥列表。 */
    private final List<KeyWrapper> keys;
    /** 密钥集合的缓存过期时间戳（毫秒），可为 {@code null}。 */
    private final Long expirationTime;

    /** 空密钥集合常量。 */
    public static final PublicKeysWrapper EMPTY = new PublicKeysWrapper(Collections.emptyList());

    /**
     * @param keys 公钥列表
     */
    public PublicKeysWrapper(List<KeyWrapper> keys) {
        this(keys, null);
    }

    /**
     * @param keys 公钥列表
     * @param expirationTime 缓存过期时间戳（毫秒）
     */
    public PublicKeysWrapper(List<KeyWrapper> keys, Long expirationTime) {
        this.keys = keys;
        this.expirationTime = expirationTime;
    }

    /** @return 缓存过期时间戳，未设置时返回 {@code null} */
    public Long getExpirationTime() {
        return expirationTime;
     }

    /** @return JWKS 密钥列表 */
    public List<KeyWrapper> getKeys() {
        return keys;
    }

    /** @return 所有密钥的 kid 列表 */
    public List<String> getKids() {
        return keys.stream()
                .map(KeyWrapper::getKid)
                .collect(Collectors.toList());
    }

    /**
     * 按 kid 与 alg 查找匹配的密钥。
     * 优先同时匹配 kid 与 alg；若仅 kid 匹配也可接受。kid 为空时退而按 alg 或默认客户端证书查找。
     *
     * @param kid RFC 7517 kid 参数
     * @param alg RFC 7517 alg 参数
     * @return 匹配给定参数的 {@link KeyWrapper}，未找到时返回 {@code null}
     */
    public KeyWrapper getKeyByKidAndAlg(String kid, String alg) {

        Stream<KeyWrapper> potentialMatches = Stream.concat(
            keys.stream().filter(keyWrapper -> Objects.equals(kid, keyWrapper.getKid()) && Objects.equals(alg, keyWrapper.getAlgorithm())),
            keys.stream().filter(keyWrapper -> Objects.equals(kid, keyWrapper.getKid())));

        if (kid == null) {
            potentialMatches = Stream.of(
                    potentialMatches,
                    keys.stream().filter(keyWrapper -> Objects.equals(alg, keyWrapper.getAlgorithmOrDefault())),
                    keys.stream().filter(KeyWrapper::isDefaultClientCertificate)
                ).flatMap(i -> i);
        }

        return potentialMatches.findFirst().orElse(null);
    }

    /**
     * 返回第一个满足给定谓词的密钥。
     *
     * @param predicate 匹配条件
     * @return 首个匹配的密钥，未找到时返回 {@code null}
     */
    public KeyWrapper getKeyByPredicate(Predicate<KeyWrapper> predicate) {
        return keys.stream().filter(predicate).findFirst().orElse(null);
    }
}
