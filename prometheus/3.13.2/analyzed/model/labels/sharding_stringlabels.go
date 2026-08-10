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

// 默认 stringlabels 实现的 StableHash：逐条 decode 标签对，大集合时切换 xxhash.Digest 流式写入。

//go:build !slicelabels && !dedupelabels

package labels

import (
	"github.com/cespare/xxhash/v2"
)

// stringlabels 后端下的稳定哈希，算法输出与其他 build tag 一致。
// StableHash is a labels hashing implementation which is guaranteed to not change over time.
// This function should be used whenever labels hashing backward compatibility must be guaranteed.
func StableHash(ls Labels) uint64 {
	// Use xxhash.Sum64(b) for fast path as it's faster.
	b := make([]byte, 0, 1024)
	var h *xxhash.Digest
	for i := 0; i < len(ls.data); {
		var v Label
		v.Name, i = decodeString(ls.data, i)
		v.Value, i = decodeString(ls.data, i)
		if h == nil && len(b)+len(v.Name)+len(v.Value)+2 >= cap(b) {
// 超过 1KB 阈值后保留已拼接字节并改用 Digest。
			// If labels entry is 1KB+, switch to Write API. Copy in the values up to this point.
			h = xxhash.New()
			_, _ = h.Write(b)
		}
		if h != nil {
			_, _ = h.WriteString(v.Name)
			_, _ = h.Write(seps)
			_, _ = h.WriteString(v.Value)
			_, _ = h.Write(seps)
			continue
		}

		b = append(b, v.Name...)
		b = append(b, sep)
		b = append(b, v.Value...)
		b = append(b, sep)
	}
	if h != nil {
		return h.Sum64()
	}
	return xxhash.Sum64(b)
}
