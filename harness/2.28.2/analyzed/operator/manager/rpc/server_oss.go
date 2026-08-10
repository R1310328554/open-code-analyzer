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

// +build oss

package rpc

import (
	"context"
	"errors"
	"io"
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/operator/manager"
)

// Server 是 OSS 构建下的空实现 RPC 服务器，所有方法返回未实现错误。
type Server struct {
	manager manager.BuildManager
	secret  string
}

// NewServer 返回不做任何操作的 OSS 版 RPC 服务器。
func NewServer(manager.BuildManager, string) *Server {
	return &Server{}
}

// Request OSS 版未实现，返回错误。
func (Server) Request(ctx context.Context, args *manager.Request) (*core.Stage, error) {
	return nil, errors.New("not implemented")
}

// Accept OSS 版未实现，返回错误。
func (Server) Accept(ctx context.Context, stage int64, machine string) error {
	return errors.New("not implemented")
}

// Netrc OSS 版未实现，返回错误。
func (Server) Netrc(ctx context.Context, repo int64) (*core.Netrc, error) {
	return nil, errors.New("not implemented")
}

// Details OSS 版未实现，返回错误。
func (Server) Details(ctx context.Context, stage int64) (*manager.Context, error) {
	return nil, errors.New("not implemented")
}

// Before OSS 版未实现，返回错误。
func (Server) Before(ctx context.Context, step *core.Step) error {
	return errors.New("not implemented")
}

// After OSS 版未实现，返回错误。
func (Server) After(ctx context.Context, step *core.Step) error {
	return errors.New("not implemented")
}

// BeforeAll OSS 版未实现，返回错误。
func (Server) BeforeAll(ctx context.Context, stage *core.Stage) error {
	return errors.New("not implemented")
}

// AfterAll OSS 版未实现，返回错误。
func (Server) AfterAll(ctx context.Context, stage *core.Stage) error {
	return errors.New("not implemented")
}

// Watch OSS 版未实现，返回错误。
func (Server) Watch(ctx context.Context, stage int64) (bool, error) {
	return false, errors.New("not implemented")
}

// Write OSS 版未实现，返回错误。
func (Server) Write(ctx context.Context, step int64, line *core.Line) error {
	return errors.New("not implemented")
}

// Upload OSS 版未实现，返回错误。
func (Server) Upload(ctx context.Context, step int64, r io.Reader) error {
	return errors.New("not implemented")
}

// UploadBytes OSS 版未实现，返回错误。
func (Server) UploadBytes(ctx context.Context, step int64, b []byte) error {
	return errors.New("not implemented")
}

// ServeHTTP OSS 版空处理器，不响应任何 RPC 请求。
func (Server) ServeHTTP(w http.ResponseWriter, r *http.Request) {}
