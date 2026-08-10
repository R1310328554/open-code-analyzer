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

// 惰性 SeriesSet 包装：首次 Next 时才初始化底层集合，用于 merge 树中避免为未消费分支构造 querier。

package storage

import (
	"github.com/prometheus/prometheus/util/annotations"
)

// lazyGenericSeriesSet 延迟执行 init，仅在第一次 Next 时 materialize 真实 set。
// lazyGenericSeriesSet is a wrapped series set that is initialised on first call to Next().
type lazyGenericSeriesSet struct {
	init func() (genericSeriesSet, bool)

	set genericSeriesSet
}

func (c *lazyGenericSeriesSet) Next() bool {
	if c.set != nil {
		return c.set.Next()
	}
	var ok bool
	c.set, ok = c.init()
	return ok
}

func (c *lazyGenericSeriesSet) Err() error {
	if c.set != nil {
		return c.set.Err()
	}
	return nil
}

func (c *lazyGenericSeriesSet) At() Labels {
	if c.set != nil {
		return c.set.At()
	}
	return nil
}

func (c *lazyGenericSeriesSet) Warnings() annotations.Annotations {
	if c.set != nil {
		return c.set.Warnings()
	}
	return nil
}

// warningsOnlySeriesSet 仅携带 warnings、不产出任何 series 的空迭代器。
type warningsOnlySeriesSet annotations.Annotations

func (warningsOnlySeriesSet) Next() bool                          { return false }
func (warningsOnlySeriesSet) Err() error                          { return nil }
func (warningsOnlySeriesSet) At() Labels                          { return nil }
func (c warningsOnlySeriesSet) Warnings() annotations.Annotations { return annotations.Annotations(c) }

// errorOnlySeriesSet 立即以 Err 终止、不产出 series 的错误占位 set。
type errorOnlySeriesSet struct {
	err error
}

func (errorOnlySeriesSet) Next() bool                        { return false }
func (errorOnlySeriesSet) At() Labels                        { return nil }
func (s errorOnlySeriesSet) Err() error                      { return s.err }
func (errorOnlySeriesSet) Warnings() annotations.Annotations { return nil }
