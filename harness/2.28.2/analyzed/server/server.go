// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// server 包封装 HTTP/HTTPS 服务器启动、TLS 与 ACME 自动证书逻辑。
package server

import (
	"context"
	"crypto/tls"
	"net/http"
	"os"
	"path/filepath"
	"time"

	"golang.org/x/crypto/acme/autocert"
	"golang.org/x/sync/errgroup"
)

// Server 定义 HTTP 服务器运行参数。
type Server struct {
	Acme    bool
	Email   string
	Addr    string
	Cert    string
	Key     string
	Host    string
	Handler http.Handler
}

const timeoutGracefulShutdown = 5 * time.Second

// ListenAndServe 根据配置启动 HTTP、TLS 或 ACME 模式的服务器。
func (s Server) ListenAndServe(ctx context.Context) error {
	if s.Acme {
		return s.listenAndServeAcme(ctx)
	} else if s.Key != "" {
		return s.listenAndServeTLS(ctx)
	}
	err := s.listenAndServe(ctx)
	if err == http.ErrServerClosed {
		err = nil
	}
	return err
}

// listenAndServe 启动纯 HTTP 服务器并在 context 取消时优雅关闭。
func (s Server) listenAndServe(ctx context.Context) error {
	var g errgroup.Group
	s1 := &http.Server{
		Addr:    s.Addr,
		Handler: s.Handler,
	}
	g.Go(func() error {
		<-ctx.Done()

		ctxShutdown, cancelFunc := context.WithTimeout(context.Background(), timeoutGracefulShutdown)
		defer cancelFunc()

		return s1.Shutdown(ctxShutdown)
	})
	g.Go(s1.ListenAndServe)
	return g.Wait()
}

// listenAndServeTLS 同时监听 :http（重定向）与 :https（TLS 服务）。
func (s Server) listenAndServeTLS(ctx context.Context) error {
	var g errgroup.Group
	s1 := &http.Server{
		Addr:    ":http",
		Handler: http.HandlerFunc(redirect),
	}
	s2 := &http.Server{
		Addr:    ":https",
		Handler: s.Handler,
	}
	g.Go(s1.ListenAndServe)
	g.Go(func() error {
		return s2.ListenAndServeTLS(
			s.Cert,
			s.Key,
		)
	})
	g.Go(func() error {
		<-ctx.Done()

		var gShutdown errgroup.Group
		ctxShutdown, cancelFunc := context.WithTimeout(context.Background(), timeoutGracefulShutdown)
		defer cancelFunc()

		gShutdown.Go(func() error {
			return s1.Shutdown(ctxShutdown)
		})
		gShutdown.Go(func() error {
			return s2.Shutdown(ctxShutdown)
		})

		return gShutdown.Wait()
	})
	return g.Wait()
}

// listenAndServeAcme 使用 autocert 自动申请并续期 Let's Encrypt 证书。
func (s Server) listenAndServeAcme(ctx context.Context) error {
	var g errgroup.Group

	c := cacheDir()
	m := &autocert.Manager{
		Email:      s.Email,
		Cache:      autocert.DirCache(c),
		Prompt:     autocert.AcceptTOS,
		HostPolicy: autocert.HostWhitelist(s.Host),
	}
	s1 := &http.Server{
		Addr:    ":http",
		Handler: m.HTTPHandler(s.Handler),
	}
	s2 := &http.Server{
		Addr:    ":https",
		Handler: s.Handler,
		TLSConfig: &tls.Config{
			GetCertificate: m.GetCertificate,
			NextProtos:     []string{"h2", "http/1.1"},
			MinVersion:     tls.VersionTLS12,
		},
	}
	g.Go(s1.ListenAndServe)
	g.Go(func() error {
		return s2.ListenAndServeTLS("", "")
	})
	g.Go(func() error {
		<-ctx.Done()

		var gShutdown errgroup.Group
		ctxShutdown, cancelFunc := context.WithTimeout(context.Background(), timeoutGracefulShutdown)
		defer cancelFunc()

		gShutdown.Go(func() error {
			return s1.Shutdown(ctxShutdown)
		})
		gShutdown.Go(func() error {
			return s2.Shutdown(ctxShutdown)
		})

		return gShutdown.Wait()
	})
	return g.Wait()
}

// redirect 将 HTTP 请求 307 重定向到 HTTPS。
func redirect(w http.ResponseWriter, req *http.Request) {
	target := "https://" + req.Host + req.URL.Path
	http.Redirect(w, req, target, http.StatusTemporaryRedirect)
}

// cacheDir 返回 autocert 证书缓存目录（优先 XDG_CACHE_HOME）。
func cacheDir() string {
	const base = "golang-autocert"
	if xdg := os.Getenv("XDG_CACHE_HOME"); xdg != "" {
		return filepath.Join(xdg, base)
	}
	return filepath.Join(os.Getenv("HOME"), ".cache", base)
}
