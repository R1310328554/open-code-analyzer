package docker

// 单个 Docker daemon 的 targetGroup：SD 同步容器列表，懒创建 API client，
// 按 container ID 维护 Target 映射，HTTP/HTTPS 主机可配置 TLS 与 User-Agent。

import (
	"fmt"
	"net/http"
	"net/url"
	"sync"
	"time"

	"github.com/docker/docker/client"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/common/config"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/discovery/targetgroup"
	"github.com/prometheus/prometheus/model/relabel"

	"github.com/grafana/loki/v3/pkg/util/build"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/positions"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

const DockerSource = "Docker"

// 一把锁保护 targets 与 client，sync 仅处理 Source=Docker 的 targetgroup。
// targetGroup manages all container targets of one Docker daemon.
type targetGroup struct {
	metrics          *Metrics
	logger           log.Logger
	positions        positions.Positions
	entryHandler     api.EntryHandler
	defaultLabels    model.LabelSet
	relabelConfig    []*relabel.Config
	host             string
	httpClientConfig config.HTTPClientConfig
	client           client.APIClient
	refreshInterval  model.Duration
	maxLineSize      int

	mtx     sync.Mutex
	targets map[string]*Target
}

// 从 SD 推送的 Group 中提取 __meta_docker_container_id 并 addTarget。
func (tg *targetGroup) sync(groups []*targetgroup.Group) {
	tg.mtx.Lock()
	defer tg.mtx.Unlock()

	for _, group := range groups {
		if group.Source != DockerSource {
			continue
		}

		for _, t := range group.Targets {
			containerID, ok := t[dockerLabelContainerID]
			if !ok {
				level.Debug(tg.logger).Log("msg", "Docker target did not include container ID")
				continue
			}

			err := tg.addTarget(string(containerID), t)
			if err != nil {
				level.Error(tg.logger).Log("msg", "could not add target", "containerID", containerID, "err", err)
			}
		}
	}
}

// 首次连接按 host scheme 选择是否注入 HTTPClientConfig，已存在则 startIfNotRunning。
// addTarget checks whether the container with given id is already known. If not it's added to the this group
func (tg *targetGroup) addTarget(id string, discoveredLabels model.LabelSet) error {
	if tg.client == nil {
		hostURL, err := url.Parse(tg.host)
		if err != nil {
			return err
		}

		opts := []client.Opt{
			client.WithHost(tg.host),
			client.WithAPIVersionNegotiation(),
		}

		// There are other protocols than HTTP supported by the Docker daemon, like
		// unix, which are not supported by the HTTP client. Passing HTTP client
		// options to the Docker client makes those non-HTTP requests fail.
		if hostURL.Scheme == "http" || hostURL.Scheme == "https" {
			rt, err := config.NewRoundTripperFromConfig(tg.httpClientConfig, "docker_sd")
			if err != nil {
				return err
			}
			opts = append(opts,
				client.WithHTTPClient(&http.Client{
					Transport: rt,
					Timeout:   time.Duration(tg.refreshInterval),
				}),
				client.WithScheme(hostURL.Scheme),
				client.WithHTTPHeaders(map[string]string{
					"User-Agent": fmt.Sprintf("loki-promtail/%s", build.Version),
				}),
			)
		}

		tg.client, err = client.NewClientWithOpts(opts...)
		if err != nil {
			level.Error(tg.logger).Log("msg", "could not create new Docker client", "err", err)
			return err
		}
	}

	if t, ok := tg.targets[id]; ok {
		level.Debug(tg.logger).Log("msg", "container target already exists", "container", id)
		t.startIfNotRunning()
		return nil
	}

	t, err := NewTarget(
		tg.metrics,
		log.With(tg.logger, "target", fmt.Sprintf("docker/%s", id)),
		tg.entryHandler,
		tg.positions,
		id,
		discoveredLabels.Merge(tg.defaultLabels),
		tg.relabelConfig,
		tg.client,
		tg.maxLineSize,
	)
	if err != nil {
		return err
	}
	tg.targets[id] = t
	level.Info(tg.logger).Log("msg", "added Docker target", "containerID", id)
	return nil
}

// 遍历 targets 检查 Ready；无运行中 target 时仍返回 true（与 upstream 行为一致）。
// Ready returns true if at least one target is running.
func (tg *targetGroup) Ready() bool {
	tg.mtx.Lock()
	defer tg.mtx.Unlock()

	for _, t := range tg.targets {
		if t.Ready() {
			return true
		}
	}

	return true
}

// Stop all targets
func (tg *targetGroup) Stop() {
	tg.mtx.Lock()
	defer tg.mtx.Unlock()

	for _, t := range tg.targets {
		t.Stop()
	}
	tg.entryHandler.Stop()
}

// ActiveTargets return all targets that are ready.
func (tg *targetGroup) ActiveTargets() []target.Target {
	tg.mtx.Lock()
	defer tg.mtx.Unlock()

	result := make([]target.Target, 0, len(tg.targets))
	for _, t := range tg.targets {
		if t.Ready() {
			result = append(result, t)
		}
	}
	return result
}

// AllTargets returns all targets of this group.
func (tg *targetGroup) AllTargets() []target.Target {
	result := make([]target.Target, 0, len(tg.targets))
	for _, t := range tg.targets {
		result = append(result, t)
	}
	return result
}
