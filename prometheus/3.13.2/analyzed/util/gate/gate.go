// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 并发查询闸门：用带缓冲 channel 限制同时执行或等待中的 PromQL/TSDB 查询数量。

package gate

import "context"

// Gate 通过固定容量 channel 控制并发查询上限。
// A Gate controls the maximum number of concurrently running and waiting queries.
type Gate struct {
	ch chan struct{}
}

// New 创建容量为 length 的查询闸门。
// New returns a query gate that limits the number of queries
// being concurrently executed.
func New(length int) *Gate {
	return &Gate{
		ch: make(chan struct{}, length),
	}
}

// Start 阻塞直到获得槽位或 context 取消。
// Start blocks until the gate has a free spot or the context is done.
func (g *Gate) Start(ctx context.Context) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	case g.ch <- struct{}{}:
		return nil
	}
}

// Done 释放一个槽位；若 Done 次数多于 Start 则 panic。
// Done releases a single spot in the gate.
func (g *Gate) Done() {
	select {
	case <-g.ch:
	default:
		panic("gate.Done: more operations done than started")
	}
}
