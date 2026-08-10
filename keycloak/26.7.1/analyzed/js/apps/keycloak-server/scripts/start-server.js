#!/usr/bin/env node
/** 下载（或使用本地构建）Keycloak 开发服务器并以 start-dev 模式启动，支持 account/admin Vite 联调。 */
import { Octokit } from "@octokit/rest";
import gunzip from "gunzip-maybe";
import { spawn } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { pipeline } from "node:stream/promises";
import { fileURLToPath } from "node:url";
import { extract } from "tar-fs";
import { parseArgs } from "node:util";

const DIR_NAME = path.dirname(fileURLToPath(import.meta.url));
/** 解压后的 Keycloak 安装目录 */
const SERVER_DIR = path.resolve(DIR_NAME, "../server");
/** 本地 Quarkus 构建产物目录（--local 时使用） */
const LOCAL_QUARKUS = path.resolve(DIR_NAME, "../../../../quarkus/dist/target");
const LOCAL_DIST_NAME = "keycloak-999.0.0-SNAPSHOT.tar.gz";
const SCRIPT_EXTENSION = process.platform === "win32" ? ".bat" : ".sh";
/** 开发环境默认 bootstrap 管理员凭据 */
const ADMIN_USERNAME = "admin";
const ADMIN_PASSWORD = "admin";
const CLIENT_ID = "temporary-admin-service";
const CLIENT_SECRET = "temporary-admin-service";

/** 脚本自身识别的 CLI 选项（其余参数透传给 kc 命令） */
const options = {
  local: {
    type: "boolean",
  },
  "account-dev": {
    type: "boolean",
  },
  "admin-dev": {
    type: "boolean",
  },
};

await startServer();

/** 确保服务器已安装，配置环境变量并 spawn kc start-dev 子进程。 */
async function startServer() {
  let { scriptArgs, keycloakArgs } = handleArgs(process.argv.slice(2));

  await downloadServer(scriptArgs.local);

  const env = {
    KC_BOOTSTRAP_ADMIN_USERNAME: ADMIN_USERNAME,
    KC_BOOTSTRAP_ADMIN_PASSWORD: ADMIN_PASSWORD,
    KC_BOOTSTRAP_ADMIN_CLIENT_ID: CLIENT_ID,
    KC_BOOTSTRAP_ADMIN_CLIENT_SECRET: CLIENT_SECRET,
    ...process.env,
  };

  // account-ui Vite 开发服务器联调地址
  if (scriptArgs["account-dev"]) {
    env.KC_ACCOUNT_VITE_URL = "http://localhost:5173";
  }

  // admin-ui Vite 开发服务器联调地址
  if (scriptArgs["admin-dev"]) {
    env.KC_ADMIN_VITE_URL = "http://localhost:5174";
  }

  console.info("Starting server…");

  const child = spawn(
    path.join(SERVER_DIR, `bin/kc${SCRIPT_EXTENSION}`),
    [
      "start-dev",
      `--features="ssf,transient-users,oid4vc-vci,declarative-ui,quick-theme,spiffe,kubernetes-service-accounts,workflows,client-auth-federated,openapi,client-admin-api:v2"`,
      "--openapi-enabled=true",
      ...keycloakArgs,
    ],
    {
      shell: true,
      env,
    },
  );

  child.stdout.pipe(process.stdout);
  child.stderr.pipe(process.stderr);
}

/** 分离脚本选项与需透传给 Keycloak 的剩余参数。 */
function handleArgs(args) {
  const { values, tokens } = parseArgs({
    args,
    options,
    strict: false,
    tokens: true,
  });
  // 从 argv 中移除脚本专属选项，以便其余参数原样传给 kc
  tokens
    .filter((token) => Object.hasOwn(options, token.name))
    .forEach((token) => {
      let tokenRaw = token.rawName;
      if (token.value) {
        tokenRaw += `=${token.value}`;
      }
      args.splice(args.indexOf(tokenRaw), 1);
    });
  return { scriptArgs: values, keycloakArgs: args };
}

/** 若 server 目录不存在则从 nightly 或本地 tarball 下载并解压。 */
async function downloadServer(local) {
  const directoryExists = fs.existsSync(SERVER_DIR);

  if (directoryExists) {
    console.info("Server installation found, skipping download.");
    return;
  }

  let assetStream;
  if (local) {
    console.info(`Looking for ${LOCAL_DIST_NAME} at ${LOCAL_QUARKUS}`);
    assetStream = fs.createReadStream(
      path.join(LOCAL_QUARKUS, LOCAL_DIST_NAME),
    );
  } else {
    console.info("Downloading and extracting server…");
    const nightlyAsset = await getNightlyAsset();
    assetStream = await getAssetAsStream(nightlyAsset);
  }
  await extractTarball(assetStream, SERVER_DIR, { strip: 1 });
}

/** 从 GitHub nightly release 查找 Keycloak SNAPSHOT 压缩包资源。 */
async function getNightlyAsset() {
  const api = new Octokit();
  const release = await api.repos.getReleaseByTag({
    owner: "keycloak",
    repo: "keycloak",
    tag: "nightly",
  });

  return release.data.assets.find(
    ({ name }) => name === "keycloak-999.0.0-SNAPSHOT.tar.gz",
  );
}

/** 通过 browser_download_url 拉取 release 资产为可读流。 */
async function getAssetAsStream(asset) {
  const response = await fetch(asset.browser_download_url);

  if (!response.ok) {
    throw new Error("Something went wrong requesting the nightly release.");
  }

  return response.body;
}

/** gunzip 后解压 tar 到目标目录。 */
function extractTarball(stream, path, options) {
  return pipeline(stream, gunzip(), extract(path, options));
}
