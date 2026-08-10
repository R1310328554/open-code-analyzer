// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// pool 提供按容量分桶的 sync.Pool 封装，用于复用可变长度字节切片，降低 GC 压力。

package pool

import (
	"fmt"
	"reflect"
	"sync"
)

// Pool 按 sizes 阶梯维护多个 sync.Pool，make 用于桶为空时创建新切片。
// Pool is a bucketed pool for variably sized byte slices.
type Pool struct {
	buckets []sync.Pool
	sizes   []int
	// make is the function used to create an empty slice when none exist yet.
	make func(int) any
}

// New 以 factor 几何递增生成 [minSize, maxSize] 内的桶边界；参数非法时 panic。
// New returns a new Pool with size buckets for minSize to maxSize
// increasing by the given factor.
func New(minSize, maxSize int, factor float64, makeFunc func(int) any) *Pool {
	if minSize < 1 {
		panic("invalid minimum pool size")
	}
	if maxSize < 1 {
		panic("invalid maximum pool size")
	}
	if factor < 1 {
		panic("invalid factor")
	}

	var sizes []int

	for s := minSize; s <= maxSize; s = int(float64(s) * factor) {
		sizes = append(sizes, s)
	}

	p := &Pool{
		buckets: make([]sync.Pool, len(sizes)),
		sizes:   sizes,
		make:    makeFunc,
	}

	return p
}

// Get 选择首个 cap≥sz 的桶取出或新建切片；超出最大桶则直接 make(sz)。
// Get returns a new byte slices that fits the given size.
func (p *Pool) Get(sz int) any {
	for i, bktSize := range p.sizes {
		if sz > bktSize {
			continue
		}
		b := p.buckets[i].Get()
		if b == nil {
			b = p.make(bktSize)
		}
		return b
	}
	return p.make(sz)
}

// Put 按切片 cap 归入合适桶，并将 len 重置为 0 后放回 Pool；非 slice 类型 panic。
// Put adds a slice to the right bucket in the pool.
func (p *Pool) Put(s any) {
	slice := reflect.ValueOf(s)

	if slice.Kind() != reflect.Slice {
		panic(fmt.Sprintf("%+v is not a slice", slice))
	}
	for i, size := range p.sizes {
		if slice.Cap() > size {
			continue
		}
		p.buckets[i].Put(slice.Slice(0, 0).Interface())
		return
	}
}
