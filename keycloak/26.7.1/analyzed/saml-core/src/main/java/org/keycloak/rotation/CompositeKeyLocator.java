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
package org.keycloak.rotation;

import java.security.Key;
import java.security.KeyManagementException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 表示多个 {@link KeyLocator} 组合链的 {@link KeyLocator}。按列表顺序从第一个到最后一个
 * {@link KeyLocator} 查找密钥；若多个 {@link KeyLocator} 提供相同 key ID 的密钥，则返回首个匹配项。
 *
 * @author hmlnarik
 */
public class CompositeKeyLocator implements KeyLocator, Iterable<Key> {

    /** 已注册的密钥定位器列表。 */
    private final List<KeyLocator> keyLocators = new LinkedList<>();

    @Override
    public Key getKey(String kid) throws KeyManagementException {
        for (KeyLocator keyLocator : keyLocators) {
            Key k = keyLocator.getKey(kid);
            if (k != null) {
                return k;
            }
        }

        return null;
    }

    @Override
    public Key getKey(Key key) throws KeyManagementException {
        for (KeyLocator keyLocator : keyLocators) {
            Key k = keyLocator.getKey(key);
            if (k != null) {
                return k;
            }
        }

        return null;
    }

    @Override
    public void refreshKeyCache() {
        for (KeyLocator keyLocator : keyLocators) {
            keyLocator.refreshKeyCache();
        }
    }

    /**
     * 将给定 {@link KeyLocator} 注册为链中的第一个定位器。
     *
     * @param keyLocator 要插入链首的密钥定位器
     */
    public void addFirst(KeyLocator keyLocator) {
        this.keyLocators.add(0, keyLocator);
    }

    /**
     * 将给定 {@link KeyLocator} 注册为链中的最后一个定位器。
     *
     * @param keyLocator 要追加到链尾的密钥定位器
     */
    public void add(KeyLocator keyLocator) {
        this.keyLocators.add(keyLocator);
    }

    /**
     * 清空已注册的 {@link KeyLocator} 列表。
     */
    public void clear() {
        this.keyLocators.clear();
    }

    @Override
    public String toString() {
        if (this.keyLocators.size() == 1) {
            return this.keyLocators.get(0).toString();
        }

        StringBuilder sb = new StringBuilder("Key locator chain: [");
        for (Iterator<KeyLocator> it = keyLocators.iterator(); it.hasNext();) {
            KeyLocator keyLocator = it.next();
            sb.append(keyLocator.toString());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    @Override
    public Iterator<Key> iterator() {
        final Iterator<Iterable<Key>> iterablesIterator = getKeyLocatorIterators().iterator();

        return new JointKeyIterator(iterablesIterator).iterator();
    }

    @SuppressWarnings("unchecked")
    private Iterable<Iterable<Key>> getKeyLocatorIterators() {
        List<Iterable<Key>> res = new LinkedList<>();
        for (KeyLocator kl : this.keyLocators) {
            if (kl instanceof Iterable) {
                res.add(((Iterable<Key>) kl));
            }
        }
        return Collections.unmodifiableCollection(res);
    }

    /** 将多个 {@link Iterable}{@code <Key>} 串联为单一密钥迭代器的辅助类。 */
    private static class JointKeyIterator implements Iterable<Key> {

        // based on http://stackoverflow.com/a/34126154/6930869
        private final Iterator<Iterable<Key>> iterablesIterator;

        public JointKeyIterator(Iterator<Iterable<Key>> iterablesIterator) {
            this.iterablesIterator = iterablesIterator;
        }

        @Override
        public Iterator<Key> iterator() {
            if (! iterablesIterator.hasNext()) {
                return Collections.<Key>emptyIterator();
            }

            return new Iterator<Key>() {
                private Iterator<Key> currentIterator = nextIterator();

                @Override
                public boolean hasNext() {
                    return currentIterator.hasNext();
                }

                @Override
                public Key next() {
                    final Key next = currentIterator.next();
                    findNext();
                    return next;
                }

                private Iterator<Key> nextIterator() {
                    return iterablesIterator.next().iterator();
                }

                private Iterator<Key> findNext() {
                    while (! currentIterator.hasNext()) {
                        if (! iterablesIterator.hasNext()) {
                            break;
                        }
                        currentIterator = nextIterator();
                    }
                    return this;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("remove");  // Same as default implementation in JDK 8 - to support JDK 7 compilation
                }
            }.findNext();
        }
    }
}
