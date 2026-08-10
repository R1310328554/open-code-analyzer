package analytics

// 匿名用量统计 HTTP 处理器：暴露 JSON 格式的集群 usage report，
// 供本地调试或 Reporter 上报前预览 buildReport 输出内容。

import (
	"encoding/json"
	"net/http"
	"sync"
	"time"
)

var (
	seed = &ClusterSeed{}
	rw   sync.RWMutex
)

func setSeed(s *ClusterSeed) {
	rw.Lock()
	defer rw.Unlock()
	seed = s
}

func Handler() http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, _ *http.Request) {
		rw.RLock()
		defer rw.RUnlock()
		report := buildReport(seed, time.Now())
		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(report); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	})
}
