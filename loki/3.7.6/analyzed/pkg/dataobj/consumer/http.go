package consumer

// http 模块为 dataobj consumer Service 提供缩容相关 HTTP 处理器：
// 供 rollout operator 在缩容前检查许可或执行延迟缩容准备。

import (
	"net/http"

	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/ring"
	"github.com/grafana/dskit/services"

	"github.com/grafana/loki/v3/pkg/util"
)

// PrepareDownscaleHandler 在立即缩容前被调用，不允许时返回非 2xx 阻止缩容。
// PrepareDownscaleHandler is a special handler called by the rollout operator
// immediately before the pod is downscaled. It can stop a downscale by
// responding with a non 2xx status code.
func (s *Service) PrepareDownscaleHandler(w http.ResponseWriter, r *http.Request) {
	isDownscalePermitted, err := s.downscalePermitted(r.Context())
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to check if downscale is permitted", "err", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if !isDownscalePermitted {
		w.WriteHeader(http.StatusBadRequest)
		return
	}
	s.partitionInstanceLifecycler.SetRemoveOwnerOnShutdown(true)
}

// PrepareDelayedDownscaleHandler 处理延迟缩容的 GET/POST/DELETE 请求。
// PrepareDelayedDownscaleHandler is a special handler called by the rollout
// operator to prepare for a delayed downscale. This allows the service to
// perform any number of actions in preparation of being scaled down at the
// end of the delayed downscale window.
//
// A delayed downscale is prepared via a POST request to this handler. The
// handler prepares the service to be downscaled and responds with the number
// of seconds since it first prepared for the delayed downscale. The handler
// should be idempotent if it has previously prepared for a delayed downscale.
//
// A delayed downscale can also be canceled via a DELETE request to the same
// handler. The handler restores the service to its running state and then
// responds with a zero timestamp. The handler should be idempotent if it has
// previously canceled a delayed downscale.
func (s *Service) PrepareDelayedDownscaleHandler(w http.ResponseWriter, r *http.Request) {
	// We don't allow changes while we are starting or shutting down.
	if s.State() != services.Running {
		w.WriteHeader(http.StatusServiceUnavailable)
		return
	}
	switch r.Method {
	case http.MethodGet:
		s.respondWithCurrentPartitionState(w, r)
	case http.MethodPost:
		s.prepareDelayedDownscale(w, r)
	case http.MethodDelete:
		s.cancelDelayedDownscale(w, r)
	}
}

// prepareDelayedDownscale 将分区状态改为 Inactive，PENDING 状态下拒绝请求。
func (s *Service) prepareDelayedDownscale(w http.ResponseWriter, r *http.Request) {
	// We don't accept prepare downscale requests while in PENDING state because
	// if the downscale is canceled we don't know what the original state was.
	// Given a partition is expected to stay in PENDING state for a short period
	// of time we choose to reject this case.
	state, _, err := s.partitionInstanceLifecycler.GetPartitionState(r.Context())
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to check partition state in the ring", "err", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if state == ring.PartitionPending {
		level.Warn(s.logger).Log("msg", "received a request to prepare partition for shutdown, but the request can't be satisfied because the partition is in PENDING state")
		w.WriteHeader(http.StatusConflict)
		return
	}
	if err := s.partitionInstanceLifecycler.ChangePartitionState(r.Context(), ring.PartitionInactive); err != nil {
		level.Error(s.logger).Log("msg", "failed to change partition state to inactive", "err", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	s.respondWithCurrentPartitionState(w, r)
}

// cancelDelayedDownscale 取消延迟缩容，将 Inactive 分区恢复为 Active。
func (s *Service) cancelDelayedDownscale(w http.ResponseWriter, r *http.Request) {
	state, _, err := s.partitionInstanceLifecycler.GetPartitionState(r.Context())
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to check partition state in the ring", "err", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// If the partition is inactive, we must have prepared it for delayed
	// downscale in the past. Mark it as active again.
	if state == ring.PartitionInactive {
		if err := s.partitionInstanceLifecycler.ChangePartitionState(r.Context(), ring.PartitionActive); err != nil {
			level.Error(s.logger).Log("msg", "failed to change partition state to active", "err", err)
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}
	s.respondWithCurrentPartitionState(w, r)
}

// respondWithCurrentPartitionState 返回 JSON timestamp，Inactive 时为进入该状态的时间戳。
func (s *Service) respondWithCurrentPartitionState(w http.ResponseWriter, r *http.Request) {
	state, stateTimestamp, err := s.partitionInstanceLifecycler.GetPartitionState(r.Context())
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to check partition state in the ring", "err", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if state == ring.PartitionInactive {
		util.WriteJSONResponse(w, map[string]any{"timestamp": stateTimestamp.Unix()})
	} else {
		util.WriteJSONResponse(w, map[string]any{"timestamp": 0})
	}
}
