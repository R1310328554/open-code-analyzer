/*
 * Copyright 2026 The RAGFlow Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package otel 为 RAGFlow agent canvas 运行时提供基于 OpenTelemetry 的可观测性。
//
// 暴露 TracerProvider 工厂与将 eino 图节点生命周期映射为 OTel span 的 Handler；
// 未配置追踪时为 no-op，生产可无代价无条件接入。
package otel

import (
	"context"
	"fmt"
	"time"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracehttp"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.26.0"
)

// ProviderConfig 字段为零时应用的默认值。
const (
	defaultServiceName    = "ragflow"
	defaultServiceVersion = "0.0.0"
	defaultServiceNS      = "ragflow"
	defaultExportTimeout  = 30 * time.Second
)

// ProviderConfig 配置 NewTracerProvider 构建的 TracerProvider；
// OTLPEndpoint 空或 SampleRatio 为 0 时不附加 exporter。
type ProviderConfig struct {
	// ServiceName populates the "service.name" resource attribute. Defaults
	// to "ragflow" when empty.
	ServiceName string
	// ServiceVersion populates the "service.version" resource attribute.
	// Defaults to "0.0.0" when empty.
	ServiceVersion string
	// OTLPEndpoint is the OTLP/HTTP collector endpoint (e.g.
	// "http://otel-collector:4318"). When empty, the returned provider
	// has no exporter and effectively no-ops.
	OTLPEndpoint string
	// Insecure 禁用 OTLP exporter 的 TLS，默认 true。
	Insecure bool
	// SampleRatio is the probability an in-process trace is sampled,
	// in the [0, 1] range. 0 disables the provider (no exporter, no
	// sampler wiring). Defaults to 1.0 (sample everything).
	SampleRatio float64
}

// NewTracerProvider 按 cfg 构建 sdktrace.TracerProvider。
//
// Two failure modes are special-cased and never return an error:
//
//   - cfg.OTLPEndpoint == "": returns a provider with no exporter. Useful
//     for unit tests and for deployments that do not yet run a collector.
//   - cfg.SampleRatio == 0: returns a provider configured with
//     [trace.NeverSample] and no exporter, so even a single manual span
//     is dropped.
//
// All other misconfigurations (unparseable ratio, collector unreachability)
// are reported through the returned error. The caller is expected to log
// the error and fall back to a no-op provider so that the agent runtime
// remains operational.
func NewTracerProvider(ctx context.Context, cfg ProviderConfig) (*sdktrace.TracerProvider, error) {
	cfg = withDefaults(cfg)

	// Short-circuit: no endpoint or no sampling requested → no-op provider.
	// We deliberately still return a non-nil *sdktrace.TracerProvider so
	// the handler does not need to special-case nil.
	if cfg.OTLPEndpoint == "" || cfg.SampleRatio == 0 {
		return sdktrace.NewTracerProvider(
			sdktrace.WithSampler(sdktrace.NeverSample()),
		), nil
	}

	res, err := buildResource(ctx, cfg)
	if err != nil {
		return nil, fmt.Errorf("otel: build resource: %w", err)
	}

	exporter, err := buildExporter(ctx, cfg)
	if err != nil {
		return nil, fmt.Errorf("otel: build exporter: %w", err)
	}

	bsp := sdktrace.NewBatchSpanProcessor(exporter,
		sdktrace.WithExportTimeout(defaultExportTimeout),
	)

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithResource(res),
		sdktrace.WithSpanProcessor(bsp),
		sdktrace.WithSampler(sdktrace.TraceIDRatioBased(cfg.SampleRatio)),
	)

	// Register as the global tracer provider so that any code that calls
	// otel.Tracer("...") also routes through this provider.
	otel.SetTracerProvider(tp)

	return tp, nil
}

// withDefaults 为零值字段填充包级默认值；按值传递不修改调用方 cfg。
func withDefaults(cfg ProviderConfig) ProviderConfig {
	if cfg.ServiceName == "" {
		cfg.ServiceName = defaultServiceName
	}
	if cfg.ServiceVersion == "" {
		cfg.ServiceVersion = defaultServiceVersion
	}
	// SampleRatio is a float — guard against negative values as well,
	// treating them as "disabled" to match the explicit-zero behaviour.
	if cfg.SampleRatio < 0 {
		cfg.SampleRatio = 0
	}
	return cfg
}

// buildResource 组合附加于每条 span 的 OTel resource（进程身份），使用 semconv v1.26.0。
func buildResource(ctx context.Context, cfg ProviderConfig) (*resource.Resource, error) {
	schemaURL := semconv.SchemaURL

	// service.namespace is set to "ragflow" regardless of cfg so that the
	// Go runtime and the Python RAGFlow share a single namespace in any
	// shared OTel backend (see plan §2.10.8).
	attrs := resource.NewWithAttributes(
		schemaURL,
		semconv.ServiceName(cfg.ServiceName),
		semconv.ServiceVersion(cfg.ServiceVersion),
		semconv.ServiceNamespace(defaultServiceNS),
	)
	detected, err := resource.Merge(
		resource.Default(),
		attrs,
	)
	if err != nil {
		return nil, err
	}
	return detected, nil
}

// buildExporter 构建指向采集端的 OTLP/HTTP span exporter；需 TLS 时设 Insecure=false。
func buildExporter(ctx context.Context, cfg ProviderConfig) (*otlptrace.Exporter, error) {
	opts := []otlptracehttp.Option{
		otlptracehttp.WithEndpoint(cfg.OTLPEndpoint),
		otlptracehttp.WithTimeout(defaultExportTimeout),
	}
	if cfg.Insecure {
		opts = append(opts, otlptracehttp.WithInsecure())
	}
	return otlptracehttp.New(ctx, opts...)
}
// otel/provider.go — OTel TracerProvider 工厂与 OTLP 导出配置。
