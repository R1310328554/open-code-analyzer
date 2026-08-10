//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package utility

// embedding_lru.go 线程安全 embedding 向量 LRU 缓存。

import (
	"container/list"
	"sync"
)

// EmbeddingLRU 以 question+embeddingID 为键的线程安全 LRU 缓存。
type EmbeddingLRU struct {
	capacity int
	cache    map[string]*list.Element
	list     *list.List
	mu       sync.RWMutex
}

// entry LRU 链表节点，保存复合键与 embedding 向量。
type entry struct {
	key   string
	value []float64
}

// NewEmbeddingLRU 创建指定容量的 LRU 缓存。
func NewEmbeddingLRU(capacity int) *EmbeddingLRU {
	return &EmbeddingLRU{
		capacity: capacity,
		cache:    make(map[string]*list.Element),
		list:     list.New(),
	}
}

// buildKey 用 "::" 拼接 question 与 embeddingID 作为缓存键。
func buildKey(question, embeddingID string) string {
	// 使用 unlikely 出现在正文中的分隔符；必要时可换更稳健键算法。
	return question + "::" + embeddingID
}

// Get 命中则移至 MRU 并返回向量副本；未命中返回 nil,false。
func (lru *EmbeddingLRU) Get(question, embeddingID string) ([]float64, bool) {
	key := buildKey(question, embeddingID)
	lru.mu.RLock()
	defer lru.mu.RUnlock()

	if elem, ok := lru.cache[key]; ok {
		// 命中后移至链表头（最近使用）
		lru.list.MoveToFront(elem)
		ent := elem.Value.(*entry)
		// 返回副本防止调用方修改缓存内切片
		embedding := make([]float64, len(ent.value))
		copy(embedding, ent.value)
		return embedding, true
	}
	return nil, false
}

// Put 写入或更新 embedding；超容量则 evictOldest 淘汰 LRU 项。
func (lru *EmbeddingLRU) Put(question, embeddingID string, embedding []float64) {
	key := buildKey(question, embeddingID)
	lru.mu.Lock()
	defer lru.mu.Unlock()

	// 键已存在则更新并移到链表头
	if elem, ok := lru.cache[key]; ok {
		lru.list.MoveToFront(elem)
		ent := elem.Value.(*entry)
		// Replace the embedding slice
		ent.value = make([]float64, len(embedding))
		copy(ent.value, embedding)
		return
	}

	// 新键 PushFront 并写入 map
	ent := &entry{key: key, value: make([]float64, len(embedding))}
	copy(ent.value, embedding)
	elem := lru.list.PushFront(ent)
	lru.cache[key] = elem

	// 超过 capacity 时淘汰最久未用项
	if lru.list.Len() > lru.capacity {
		lru.evictOldest()
	}
}

// evictOldest 移除链表尾 LRU 项；调用方须已持写锁。
func (lru *EmbeddingLRU) evictOldest() {
	elem := lru.list.Back()
	if elem != nil {
		lru.list.Remove(elem)
		ent := elem.Value.(*entry)
		delete(lru.cache, ent.key)
	}
}

// Remove 删除指定键的缓存项。
func (lru *EmbeddingLRU) Remove(question, embeddingID string) {
	key := buildKey(question, embeddingID)
	lru.mu.Lock()
	defer lru.mu.Unlock()

	if elem, ok := lru.cache[key]; ok {
		lru.list.Remove(elem)
		delete(lru.cache, key)
	}
}

// Clear 清空 map 并重置链表。
func (lru *EmbeddingLRU) Clear() {
	lru.mu.Lock()
	defer lru.mu.Unlock()

	lru.cache = make(map[string]*list.Element)
	lru.list.Init()
}

// Len 返回当前缓存条目数。
func (lru *EmbeddingLRU) Len() int {
	lru.mu.RLock()
	defer lru.mu.RUnlock()
	return lru.list.Len()
}
// embedding_lru.go — 线程安全 embedding LRU 缓存（question+model 复合键）。
