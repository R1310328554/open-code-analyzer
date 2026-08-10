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

// react-app 的 react-app-rewired 配置：强制 CodeMirror/Lezer 依赖单例，避免 instanceof 失效。

const path = require('path');

// codemirror-promql 经 pnpm link 链入工作区，若不 dedupe 会加载两份 @codemirror/state。
// @prometheus-io/codemirror-promql is consumed via pnpm's "link:" protocol, so
// it is a symlink into the workspace and carries its own node_modules. Without
// deduplication, its transitive @codemirror/* and @lezer/* imports resolve to
// the workspace copy while react-app uses its own isolated copy. That loads two
// instances of @codemirror/state and breaks instanceof checks at runtime
// ("Unrecognized extension value in extension set"). Force these packages to
// resolve to react-app's single copy.
// singletons 列出必须与 react-app node_modules 对齐的包，保证扩展集使用同一实例。
const singletons = [
  '@codemirror/state',
  '@codemirror/view',
  '@codemirror/language',
  '@codemirror/commands',
  '@codemirror/search',
  '@codemirror/autocomplete',
  '@codemirror/lint',
  '@lezer/common',
  '@lezer/highlight',
  '@lezer/lr',
];

// override 为上述包设置 resolve.alias，并移除 ModuleScopePlugin 以允许绝对路径别名。
module.exports = function override(config) {
  config.resolve = config.resolve || {};
  config.resolve.alias = Object.assign(
    {},
    config.resolve.alias,
    Object.fromEntries(singletons.map((pkg) => [pkg, path.resolve(__dirname, 'node_modules', pkg)]))
  );
// CRA 的 ModuleScopePlugin 会拒绝 src 外导入；过滤该插件后别名才能生效。
  // The aliases above resolve to absolute node_modules paths, which Create
  // React App's ModuleScopePlugin rejects as imports outside src/. Drop it so
  // the deduplicating aliases take effect.
  config.resolve.plugins = (config.resolve.plugins || []).filter(
    (plugin) => !(plugin.constructor && plugin.constructor.name === 'ModuleScopePlugin')
  );
  return config;
};
