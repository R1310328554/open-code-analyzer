/**
 * 为 Admin API v2 文档生成 JavaScript 调用示例。
 *
 * 遍历 Kiota 生成的 NavigationMetadata 发现各端点操作，
 * 与 openapi.json 中的 operationId 匹配，产出：
 *   - src/generated/doc-examples/admin-v2-js-examples.json
 *   - src/generated/doc-examples/admin-v2-doc-examples-check.ts
 *   - src/generated/doc-examples/tsconfig.doc-check.json
 */

import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import type { NavigationMetadata } from "@microsoft/kiota-abstractions";
import { AdminClientNavigationMetadata } from "../src/generated/adminClient.js";

const projectDir = join(dirname(fileURLToPath(import.meta.url)), "..");
const outputDir = join(projectDir, "src", "generated", "doc-examples");

/** Kiota URI 模板中的 baseurl 占位前缀 */
const BASEURL_PREFIX = "{+baseurl}";
/** 类型检查占位：声明变量但不引入真实值 */
const VERIFY_ARG = "undefined as any";

/**
 * Kiota 导航链与用户面向封装 API 的映射。
 * 新增 v2 资源时需在此追加条目。
 */
const WRAPPERS = [
  {
    prefix: "kcAdminClient.clients.v2()",
    chainPrefix: ".admin.api.byRealmName(realmName).clients.v2",
  },
];

const spec = JSON.parse(
  readFileSync(join(projectDir, "openapi.json"), "utf-8"),
);
/** HTTP 方法 + 路径 → operationId 索引 */
const operationIds = new Map<string, string>();
for (const [path, methods] of Object.entries(spec.paths || {})) {
  for (const [method, operation] of Object.entries(
    methods as Record<string, any>,
  )) {
    if (operation?.operationId) {
      operationIds.set(
        `${method.toUpperCase()}:${path}`,
        operation.operationId,
      );
    }
  }
}

const examples: Record<string, { example: string }> = {};
/** 导航链中出现的全部路径/请求体参数名 */
const navParamNames = new Set<string>();

/**
 * 递归遍历 Kiota 导航元数据，为每个请求生成示例调用语句。
 */
function collectEndpointExamples(
  navEntries: Record<string, NavigationMetadata>,
  chain: string,
  pathPrefix: string,
): void {
  for (const [key, meta] of Object.entries(navEntries)) {
    const params = meta.pathParametersMappings;
    const segment = params?.length
      ? `.${key}(${params.join(", ")})`
      : `.${key}`;
    const currentChain = chain + segment;

    const urlSegment = params?.length
      ? params.map((p) => `{${p}}`).join("/")
      : key;
    const currentPath = pathPrefix + "/" + urlSegment;

    if (params) {
      params.forEach((p) => navParamNames.add(p));
    }

    if (meta.requestsMetadata) {
      for (const [method, reqMeta] of Object.entries(meta.requestsMetadata)) {
        const rawTemplate = reqMeta.uriTemplate as string;
        let uriTemplate: string;
        if (rawTemplate.startsWith(BASEURL_PREFIX)) {
          uriTemplate = rawTemplate.slice(BASEURL_PREFIX.length);
        } else {
          uriTemplate = currentPath;
        }
        // 去除可选查询串占位，便于与 OpenAPI 路径键对齐
        uriTemplate = uriTemplate.replace(/\{[?&][^}]*\}/g, "");
        const operationId = operationIds.get(
          `${method.toUpperCase()}:${uriTemplate}`,
        );
        if (!operationId) {
          throw new Error(
            `Cannot generate example — no operationId in openapi.json for ${method.toUpperCase()} ${uriTemplate}`,
          );
        }

        const wrapper = WRAPPERS.find((w) =>
          currentChain.startsWith(w.chainPrefix),
        );
        if (!wrapper) {
          throw new Error(
            `Cannot generate example — no wrapper prefix configured in generate-doc-examples.ts for: ${currentChain}`,
          );
        }

        const remainder = currentChain.slice(wrapper.chainPrefix.length);
        const contentType = reqMeta.requestBodyContentType as
          | string
          | undefined;
        // Kiota 元数据未暴露方法参数个数；若某方法参数超过一个需手动调整
        const bodyArg = contentType ? "requestBody" : "";

        const call = `${wrapper.prefix}${remainder}.${method}(${bodyArg});`;
        examples[operationId] = { example: call };
      }
    }

    if (meta.navigationMetadata) {
      collectEndpointExamples(
        meta.navigationMetadata as Record<string, NavigationMetadata>,
        currentChain,
        currentPath,
      );
    }
  }
}

collectEndpointExamples(
  AdminClientNavigationMetadata as unknown as Record<
    string,
    NavigationMetadata
  >,
  "",
  "",
);

if (Object.keys(examples).length === 0) {
  throw new Error(
    "Documentation build failed — no JS examples were generated from Kiota metadata",
  );
}

mkdirSync(outputDir, { recursive: true });

writeFileSync(
  join(outputDir, "admin-v2-js-examples.json"),
  JSON.stringify(examples, null, 2) + "\n",
);

navParamNames.add("requestBody");
const paramDeclarations = [...navParamNames]
  .map((p) => `const ${p} = ${VERIFY_ARG};`)
  .join("\n");

/** 生成 TypeScript 校验文件：声明占位变量并串联全部示例调用 */
const verifyContent =
  `import type { KeycloakAdminClient } from "../../client.js";\n` +
  `declare const kcAdminClient: KeycloakAdminClient;\n` +
  paramDeclarations +
  "\n" +
  Object.values(examples)
    .map((e) => e.example)
    .join("\n") +
  "\n";

writeFileSync(join(outputDir, "admin-v2-doc-examples-check.ts"), verifyContent);

writeFileSync(
  join(outputDir, "tsconfig.doc-check.json"),
  JSON.stringify(
    {
      extends: "../../../tsconfig.json",
      include: ["admin-v2-doc-examples-check.ts"],
      compilerOptions: { noEmit: true },
    },
    null,
    2,
  ) + "\n",
);
