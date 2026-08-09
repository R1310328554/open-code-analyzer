/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.misc;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程安全队列，对任意元素的删除操作为 O(1) 均摊复杂度。
 * <p>
 * 内部用 {@link ConcurrentHashMap} 索引元素到链表节点，
 * 用双向链表维护 FIFO 顺序；适合需要频繁按值移除的场景。
 *
 * @author Nikita Koksharov
 *
 * @param <E> 元素类型
 */
public final class FastRemovalQueue<E> implements Iterable<E> {

    /** 元素 → 链表节点的 O(1) 查找索引。 */
    private final Map<E, Node<E>> index = new ConcurrentHashMap<>();
    /** 维护 FIFO 顺序的双向链表。 */
    private final DoublyLinkedList<E> list = new DoublyLinkedList<>();

    /** 追加元素到队尾；重复元素仅保留一份。 */
    public void add(E element) {
        Node<E> newNode = new Node<>(element);
        if (index.putIfAbsent(element, newNode) == null) {
            list.add(newNode);
        }
    }

    /** 将已存在元素移到队尾（LRU 语义）；不存在则返回 false。 */
    public boolean moveToTail(E element) {
        Node<E> node = index.get(element);
        if (node != null) {
            list.moveToTail(node);
            return true;
        }
        return false;
    }

    /** 按值 O(1) 移除元素。 */
    public boolean remove(E element) {
        Node<E> node = index.remove(element);
        if (node != null) {
            return list.remove(node);
        }
        return false;
    }

    public boolean isEmpty() {
        return index.isEmpty();
    }

    public int size() {
        return index.size();
    }

    /** 弹出队头元素；空队列返回 null。 */
    public E poll() {
        Node<E> node = list.removeFirst();
        if (node != null) {
            index.remove(node.value);
            return node.value;
        }
        return null;
    }

    public void clear() {
        index.clear();
        list.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    /** 双向链表节点，含软删除标记。 */
    static class Node<E> {
        private final E value;
        private Node<E> prev;
        private volatile Node<E> next;
        /** 逻辑删除标记，迭代时跳过已删节点。 */
        private volatile boolean deleted;

        Node(E value) {
            this.value = value;
        }

        public void setDeleted() {
            deleted = true;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public E getValue() {
            return value;
        }
    }

    /** 带锁保护的双向链表，支持 O(1) 头删、尾加与任意节点移除。 */
    static class DoublyLinkedList<E> implements Iterable<E> {
        /** 串行化链表结构变更的轻量锁。 */
        private final WrappedLock lock = new WrappedLock();
        private Node<E> head;
        private Node<E> tail;

        DoublyLinkedList() {
        }

        public void clear() {
            lock.execute(() -> {
                head = null;
                tail = null;
            });
        }

        public void add(Node<E> newNode) {
            lock.execute(() -> {
                addNode(newNode);
            });
        }

        private void addNode(Node<E> newNode) {
            Node<E> currentTail = tail;
            tail = newNode;
            if (currentTail == null) {
                head = newNode;
            } else {
                newNode.prev = currentTail;
                currentTail.next = newNode;
            }
        }

        public boolean remove(Node<E> node) {
            Boolean r = lock.execute(() -> {
                if (node.isDeleted()) {
                    return false;
                }

                removeNode(node);
                node.setDeleted();
                return true;
            });
            return Boolean.TRUE.equals(r);
        }

        private void removeNode(Node<E> node) {
            Node<E> prevNode = node.prev;
            Node<E> nextNode = node.next;

            if (prevNode != null) {
                prevNode.next = nextNode;
            } else {
                head = nextNode;
            }

            if (nextNode != null) {
                nextNode.prev = prevNode;
            } else {
                tail = prevNode;
            }
        }

        /** 将节点从当前位置摘下并追加到队尾。 */
        public void moveToTail(Node<E> node) {
            lock.execute(() -> {
                if (node.isDeleted()) {
                    return;
                }

                removeNode(node);

                node.prev = null;
                node.next = null;
                addNode(node);
            });
        }

        /** 移除并返回队头节点，同时标记为已删除。 */
        public Node<E> removeFirst() {
            return lock.execute(() -> {
                Node<E> currentHead = head;
                if (head == tail) {
                    head = null;
                    tail = null;
                } else {
                    head = head.next;
                    head.prev = null;
                }
                if (currentHead != null) {
                    currentHead.setDeleted();
                }
                return currentHead;
            });
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private Node<E> current = head;

                @Override
                public boolean hasNext() {
                    while (current != null && current.isDeleted()) {
                        current = current.next;
                    }
                    return current != null;
                }

                @Override
                public E next() {
                    if (current == null) {
                        throw new NoSuchElementException();
                    }
                    E value = current.getValue();
                    current = current.next;
                    return value;
                }
            };
        }
    }
}