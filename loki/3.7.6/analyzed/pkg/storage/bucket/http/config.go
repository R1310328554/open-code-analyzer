package http

// http 包为各对象存储后端提供共享 HTTP 客户端配置：连接池、TLS 握手超时及 mTLS 证书路径等，供 S3/Swift 等子包嵌入。

import (
	"flag"
	"net/http"
	"time"
)

// Config 定义 idle 超时、响应头超时、连接数上限及可选 Transport 注入。
// Config stores the http.Client configuration for the storage clients.
type Config struct {
	IdleConnTimeout       time.Duration `yaml:"idle_conn_timeout"`
	ResponseHeaderTimeout time.Duration `yaml:"response_header_timeout"`
	InsecureSkipVerify    bool          `yaml:"insecure_skip_verify"`
	TLSHandshakeTimeout   time.Duration `yaml:"tls_handshake_timeout"`
	ExpectContinueTimeout time.Duration `yaml:"expect_continue_timeout"`
	MaxIdleConns          int           `yaml:"max_idle_connections"`
	MaxIdleConnsPerHost   int           `yaml:"max_idle_connections_per_host"`
	MaxConnsPerHost       int           `yaml:"max_connections_per_host"`

	// Allow upstream callers to inject a round tripper
	Transport http.RoundTripper `yaml:"-"`

	TLSConfig TLSConfig `yaml:",inline"`
}

// TLSConfig 配置 CA、客户端证书与 SNI 服务器名，用于私有云或自签证书场景。
// TLSConfig configures the options for TLS connections.
type TLSConfig struct {
	CAPath     string `yaml:"tls_ca_path" category:"advanced"`
	CertPath   string `yaml:"tls_cert_path" category:"advanced"`
	KeyPath    string `yaml:"tls_key_path" category:"advanced"`
	ServerName string `yaml:"tls_server_name" category:"advanced"`
}

// RegisterFlags 注册无前缀的 http.* 与 tls 相关 CLI 标志。
// RegisterFlags registers the flags for the storage HTTP client.
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix("", f)
}

// RegisterFlagsWithPrefix 将 HTTP 与 TLS 子配置一并注册到给定前缀下。
// RegisterFlagsWithPrefix registers the flags for the storage HTTP client with the provided prefix.
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.DurationVar(&cfg.IdleConnTimeout, prefix+"http.idle-conn-timeout", 90*time.Second, "The time an idle connection will remain idle before closing.")
	f.DurationVar(&cfg.ResponseHeaderTimeout, prefix+"http.response-header-timeout", 2*time.Minute, "The amount of time the client will wait for a servers response headers.")
	f.BoolVar(&cfg.InsecureSkipVerify, prefix+"http.insecure-skip-verify", false, "If the client connects via HTTPS and this option is enabled, the client will accept any certificate and hostname.")
	f.DurationVar(&cfg.TLSHandshakeTimeout, prefix+"tls-handshake-timeout", 10*time.Second, "Maximum time to wait for a TLS handshake. 0 means no limit.")
	f.DurationVar(&cfg.ExpectContinueTimeout, prefix+"expect-continue-timeout", 1*time.Second, "The time to wait for a server's first response headers after fully writing the request headers if the request has an Expect header. 0 to send the request body immediately.")
	f.IntVar(&cfg.MaxIdleConns, prefix+"max-idle-connections", 100, "Maximum number of idle (keep-alive) connections across all hosts. 0 means no limit.")
	f.IntVar(&cfg.MaxIdleConnsPerHost, prefix+"max-idle-connections-per-host", 100, "Maximum number of idle (keep-alive) connections to keep per-host. If 0, a built-in default value is used.")
	f.IntVar(&cfg.MaxConnsPerHost, prefix+"max-connections-per-host", 0, "Maximum number of connections per host. 0 means no limit.")
	cfg.TLSConfig.RegisterFlagsWithPrefix(prefix, f)
}

// TLSConfig.RegisterFlagsWithPrefix 注册 tls-ca-path、tls-cert-path 等高级 TLS 选项。
// RegisterFlagsWithPrefix registers the flags for s3 storage with the provided prefix.
func (cfg *TLSConfig) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.StringVar(&cfg.CAPath, prefix+"http.tls-ca-path", "", "Path to the CA certificates to validate server certificate against. If not set, the host's root CA certificates are used.")
	f.StringVar(&cfg.CertPath, prefix+"http.tls-cert-path", "", "Path to the client certificate, which will be used for authenticating with the server. Also requires the key path to be configured.")
	f.StringVar(&cfg.KeyPath, prefix+"http.tls-key-path", "", "Path to the key for the client certificate. Also requires the client certificate to be configured.")
	f.StringVar(&cfg.ServerName, prefix+"http.tls-server-name", "", "Override the expected name on the server certificate.")
}
// MaxIdleConnsPerHost 为 0 时使用内置默认值；InsecureSkipVerify 仅建议在测试环境启用。
