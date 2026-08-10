/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.jose.jws;

/**
 * JWS 算法族分类，供 {@link Algorithm} 枚举引用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public enum AlgorithmType {

    /** RSA 系算法。 */
    RSA,
    /** HMAC 系算法。 */
    HMAC,
    /** AES 系算法。 */
    AES,
    /** ECDSA 系算法。 */
    ECDSA,
    /** EdDSA 系算法。 */
    EDDSA

}
