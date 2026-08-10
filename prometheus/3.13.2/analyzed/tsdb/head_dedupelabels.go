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

// dedupelabels 构建：memSeries.labels 持锁访问；RebuildSymbolTable 去重标签符号表。

//go:build dedupelabels

package tsdb

import (
	"log/slog"

	"github.com/prometheus/prometheus/model/labels"
)

// labels 在 dedupelabels 模式下需加锁读取 lset，避免与符号表重建并发冲突。
// Helper method to access labels under lock.
func (s *memSeries) labels() labels.Labels {
	s.Lock()
	defer s.Unlock()
	return s.lset
}

// RebuildSymbolTable 遍历 Head 与 exemplar，用统一 SymbolTable 替换各 series 标签。
// RebuildSymbolTable goes through all the series in h, build a SymbolTable with all names and values,
// replace each series' Labels with one using that SymbolTable.
func (h *Head) RebuildSymbolTable(logger *slog.Logger) *labels.SymbolTable {
	logger.Info("RebuildSymbolTable starting")
	st := labels.NewSymbolTable()
	builder := labels.NewScratchBuilderWithSymbolTable(st, 0)
	rebuildLabels := func(lbls labels.Labels) labels.Labels {
		builder.Reset()
		lbls.Range(func(l labels.Label) {
			builder.Add(l.Name, l.Value)
		})
		return builder.Labels()
	}

	for i := 0; i < h.series.size; i++ {
		h.series.locks[i].Lock()

		for _, s := range h.series.hashes[i].unique {
			s.Lock()
			s.lset = rebuildLabels(s.lset)
			s.Unlock()
		}

		for _, all := range h.series.hashes[i].conflicts {
			for _, s := range all {
				s.Lock()
				s.lset = rebuildLabels(s.lset)
				s.Unlock()
			}
		}

		h.series.locks[i].Unlock()
	}
	type withReset interface{ ResetSymbolTable(*labels.SymbolTable) }
	if e, ok := h.exemplars.(withReset); ok {
		e.ResetSymbolTable(st)
	}
	logger.Info("RebuildSymbolTable finished", "size", st.Len())
	return st
}

// ResetSymbolTable 将 exemplar 存储中的 series/exemplar 标签重建为符号表引用。
func (ce *CircularExemplarStorage) ResetSymbolTable(st *labels.SymbolTable) {
	builder := labels.NewScratchBuilderWithSymbolTable(st, 0)
	rebuildLabels := func(lbls labels.Labels) labels.Labels {
		builder.Reset()
		lbls.Range(func(l labels.Label) {
			builder.Add(l.Name, l.Value)
		})
		return builder.Labels()
	}

	ce.lock.RLock()
	defer ce.lock.RUnlock()

	for _, v := range ce.index {
		v.seriesLabels = rebuildLabels(v.seriesLabels)
	}
	for i := range ce.exemplars {
		if ce.exemplars[i].ref == nil {
			continue
		}
		ce.exemplars[i].exemplar.Labels = rebuildLabels(ce.exemplars[i].exemplar.Labels)
	}
}
