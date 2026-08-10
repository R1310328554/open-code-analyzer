package server

// Promtail HTTP 管理面：targets/config/service-discovery 页面、/ready 探针与可选 /reload。
// 嵌入 dskit server，静态资源来自 ui.Assets，Disable 时使用 NoopServer 仅等信号退出。

import (
	"flag"
	"fmt"
	"net/http"
	"net/url"
	"os"
	"os/signal"
	"path"
	"sort"
	"strings"
	"sync"
	"syscall"
	"text/template"

	"github.com/felixge/fgprof"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	serverww "github.com/grafana/dskit/server"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/prometheus/common/version"

	"github.com/grafana/loki/v3/clients/pkg/promtail/server/ui"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

var (
	readinessProbeFailure = "Not ready: Unable to find any logs to tail. Please verify permissions, volumes, scrape_config, etc."
	readinessProbeSuccess = []byte("Ready")
)

// Promtail 对外 server 抽象：Run 阻塞监听，Shutdown 触发优雅退出。
type Server interface {
	Shutdown()
	Run() error
}

// PromtailServer 包装 dskit Server，持有 target managers 与热重载 channel。
// Server embed weaveworks server with static file and templating capability
type PromtailServer struct {
	*serverww.Server
	log               log.Logger
	mtx               sync.Mutex
	tms               *targets.TargetManagers
	externalURL       *url.URL
	reloadCh          chan chan error
	healthCheckTarget bool
	promtailCfg       string
}

// Config extends weaveworks server config
type Config struct {
	serverww.Config   `yaml:",inline"`
	ExternalURL       string `yaml:"external_url"`
	HealthCheckTarget *bool  `yaml:"health_check_target"`
	Disable           bool   `yaml:"disable"`
	ProfilingEnabled  bool   `yaml:"profiling_enabled"`
	Reload            bool   `yaml:"enable_runtime_reload"`
}

// RegisterFlags with prefix registers flags where every name is prefixed by
// prefix. If prefix is a non-empty string, prefix should end with a period.
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	// NOTE: weaveworks server's config can't be registered with a prefix.
	cfg.Config.RegisterFlags(f)

	f.BoolVar(&cfg.Disable, prefix+"server.disable", false, "Disable the http and grpc server.")
	f.BoolVar(&cfg.ProfilingEnabled, prefix+"server.profiling_enabled", false, "Enable the /debug/fgprof and /debug/pprof endpoints for profiling.")
	f.BoolVar(&cfg.Reload, prefix+"server.enable-runtime-reload", false, "Enable reload via HTTP request.")
}

// RegisterFlags adds the flags required to config this to the given FlagSet
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix("", f)
}

// New makes a new Server
// 注册 /targets、/config、/static 等路由；ProfilingEnabled 时暴露 pprof/fgprof。
func New(cfg Config, log log.Logger, tms *targets.TargetManagers, promtailCfg string) (Server, error) {
	if cfg.Disable {
		return newNoopServer(log), nil
	}

	// Don't let serverww.New() add /metrics and /debug/pprof routes when
	// cfg.Debug is false. We'll add the former bellow if
	// cfg.RegisterInstrumentation was true, and we'll add the latter only
	// if cfg.Debug is true.
	registerMetrics := cfg.RegisterInstrumentation && !cfg.ProfilingEnabled
	cfg.RegisterInstrumentation = cfg.ProfilingEnabled

	cfg.Log = log
	wws, err := serverww.New(cfg.Config)
	if err != nil {
		return nil, err
	}

	externalURL, err := computeExternalURL(cfg.ExternalURL, cfg.HTTPListenPort)
	if err != nil {
		return nil, errors.Wrapf(err, "parse external URL %q", cfg.ExternalURL)
	}
	externalURL.Path += cfg.PathPrefix

	healthCheckTargetFlag := true
	if cfg.HealthCheckTarget != nil {
		healthCheckTargetFlag = *cfg.HealthCheckTarget
	}

	serv := &PromtailServer{
		Server:            wws,
		log:               log,
		reloadCh:          make(chan chan error),
		tms:               tms,
		externalURL:       externalURL,
		healthCheckTarget: healthCheckTargetFlag,
		promtailCfg:       promtailCfg,
	}

	// Register the /metrics route if cfg.RegisterInstrumentation was true
	// initially.
	if registerMetrics {
		wws.HTTP.Handle("/metrics", promhttp.HandlerFor(prometheus.DefaultGatherer, promhttp.HandlerOpts{
			EnableOpenMetrics: true,
		}))
	}

	serv.HTTP.Path("/").Handler(http.RedirectHandler(path.Join(serv.externalURL.Path, "/targets"), http.StatusSeeOther))
	serv.HTTP.Path("/ready").Handler(http.HandlerFunc(serv.ready))
	serv.HTTP.PathPrefix("/static/").Handler(http.StripPrefix(externalURL.Path, http.FileServer(ui.Assets)))
	serv.HTTP.Path("/service-discovery").Handler(http.HandlerFunc(serv.serviceDiscovery))
	serv.HTTP.Path("/targets").Handler(http.HandlerFunc(serv.targets))
	serv.HTTP.Path("/config").Handler(http.HandlerFunc(serv.config))
	if cfg.ProfilingEnabled {
		serv.HTTP.Path("/debug/fgprof").Handler(fgprof.Handler())
	}
	if cfg.Reload {
		serv.HTTP.Path("/reload").Handler(http.HandlerFunc(serv.reload))
	}
	return serv, nil
}

// 渲染 service-discovery.html，每 job 最多展示 100 个 dropped target。
// serviceDiscovery serves the service discovery page.
func (s *PromtailServer) serviceDiscovery(rw http.ResponseWriter, req *http.Request) {
	s.mtx.Lock()
	defer s.mtx.Unlock()
	var index []string
	allTarget := s.tms.AllTargets()
	for job := range allTarget {
		index = append(index, job)
	}
	sort.Strings(index)
	scrapeConfigData := struct {
		Index   []string
		Targets map[string][]target.Target
		Active  []int
		Dropped []int
		Total   []int
	}{
		Index:   index,
		Targets: make(map[string][]target.Target),
		Active:  make([]int, len(index)),
		Dropped: make([]int, len(index)),
		Total:   make([]int, len(index)),
	}
	for i, job := range scrapeConfigData.Index {
		scrapeConfigData.Targets[job] = make([]target.Target, 0, len(allTarget[job]))
		scrapeConfigData.Total[i] = len(allTarget[job])
		for _, t := range allTarget[job] {
			// Do not display more than 100 dropped targets per job to avoid
			// returning too much data to the clients.
			if target.IsDropped(t) {
				scrapeConfigData.Dropped[i]++
				if scrapeConfigData.Dropped[i] > 100 {
					continue
				}
			} else {
				scrapeConfigData.Active[i]++
			}
			scrapeConfigData.Targets[job] = append(scrapeConfigData.Targets[job], t)
		}
	}

	executeTemplate(req.Context(), rw, templateOptions{
		Data:         scrapeConfigData,
		BuildVersion: version.Info(),
		Name:         "service-discovery.html",
		PageTitle:    "Service Discovery",
		ExternalURL:  s.externalURL,
		TemplateFuncs: template.FuncMap{
			"fileTargetDetails": func(details interface{}) map[string]int64 {
				// you can't cast with a text template in go so this is a helper
				return details.(map[string]int64)
			},
			"dropReason": func(details interface{}) string {
				if reason, ok := details.(string); ok {
					return reason
				}
				return ""
			},
			"numReady": func(ts []target.Target) (readies int) {
				for _, t := range ts {
					if t.Ready() {
						readies++
					}
				}
				return
			},
		},
	})
}

func (s *PromtailServer) config(rw http.ResponseWriter, req *http.Request) {
	executeTemplate(req.Context(), rw, templateOptions{
		Data:         s.promtailCfg,
		BuildVersion: version.Info(),
		Name:         "config.html",
		PageTitle:    "Config",
		ExternalURL:  s.externalURL,
	})
}

// 展示各 job 活跃 target 池，模板 helper 暴露 file/journal 详情与 ready 计数。
// targets serves the targets page.
func (s *PromtailServer) targets(rw http.ResponseWriter, req *http.Request) {
	s.mtx.Lock()
	defer s.mtx.Unlock()
	executeTemplate(req.Context(), rw, templateOptions{
		Data: struct {
			TargetPools map[string][]target.Target
		}{
			TargetPools: s.tms.ActiveTargets(),
		},
		BuildVersion: version.Info(),
		Name:         "targets.html",
		PageTitle:    "Targets",
		ExternalURL:  s.externalURL,
		TemplateFuncs: template.FuncMap{
			"fileTargetDetails": func(details interface{}) map[string]int64 {
				// you can't cast with a text template in go so this is a helper
				return details.(map[string]int64)
			},
			"journalTargetDetails": func(details interface{}) map[string]string {
				// you can't cast with a text template in go so this is a helper
				return details.(map[string]string)
			},
			"numReady": func(ts []target.Target) (readies int) {
				for _, t := range ts {
					if t.Ready() {
						readies++
					}
				}
				return
			},
		},
	})
}

func (s *PromtailServer) reload(rw http.ResponseWriter, _ *http.Request) {
	rc := make(chan error)
	s.reloadCh <- rc
	if err := <-rc; err != nil {
		http.Error(rw, fmt.Sprintf("failed to reload config: %s", err), http.StatusInternalServerError)
	}

}

// Reload returns the receive-only channel that signals configuration reload requests.
func (s *PromtailServer) Reload() <-chan chan error {
	return s.reloadCh
}

// Reload returns the receive-only channel that signals configuration reload requests.
func (s *PromtailServer) PromtailConfig() string {
	return s.promtailCfg
}

func (s *PromtailServer) ReloadServer(tms *targets.TargetManagers, promtailCfg string) {
	s.mtx.Lock()
	defer s.mtx.Unlock()
	s.tms = tms
	s.promtailCfg = promtailCfg
}

// ready serves the ready endpoint
// 就绪探针：health_check_target 开启时要求至少一个 target Ready。
func (s *PromtailServer) ready(rw http.ResponseWriter, _ *http.Request) {
	s.mtx.Lock()
	defer s.mtx.Unlock()
	if s.healthCheckTarget && !s.tms.Ready() {
		http.Error(rw, readinessProbeFailure, http.StatusInternalServerError)
		return
	}

	rw.WriteHeader(http.StatusOK)
	if _, err := rw.Write(readinessProbeSuccess); err != nil {
		level.Error(s.log).Log("msg", "error writing success message", "error", err)
	}
}

// computeExternalURL computes a sanitized external URL from a raw input. It infers unset
// URL parts from the OS and the given listen address.
// 推断或规范化 external_url，空值时默认 http://hostname:port。
func computeExternalURL(u string, port int) (*url.URL, error) {
	if u == "" {
		hostname, err := os.Hostname()
		if err != nil {
			return nil, err
		}

		u = fmt.Sprintf("http://%s:%d/", hostname, port)
	}

	eu, err := url.Parse(u)
	if err != nil {
		return nil, err
	}

	ppref := strings.TrimRight(eu.Path, "/")
	if ppref != "" && !strings.HasPrefix(ppref, "/") {
		ppref = "/" + ppref
	}
	eu.Path = ppref

	return eu, nil
}

type NoopServer struct {
	log  log.Logger
	sigs chan os.Signal
}

func newNoopServer(log log.Logger) *NoopServer {
	return &NoopServer{
		log:  log,
		sigs: make(chan os.Signal, 1),
	}
}

func (s *NoopServer) Run() error {
	signal.Notify(s.sigs, syscall.SIGINT, syscall.SIGTERM)
	sig := <-s.sigs
	level.Info(s.log).Log("msg", "received shutdown signal", "sig", sig)
	return nil
}

func (s *NoopServer) Shutdown() {
	s.sigs <- syscall.SIGTERM
}
