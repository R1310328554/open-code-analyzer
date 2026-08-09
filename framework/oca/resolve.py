from __future__ import annotations

import json
import re
import subprocess
from dataclasses import dataclass
from urllib.parse import urlparse


@dataclass(frozen=True)
class ResolvedTarget:
    repo_url: str
    clone_url: str
    owner: str
    repo: str
    version: str  # 不含 v 前缀的目录版本号，如 7.0.8
    git_ref: str  # 实际 checkout 的 tag/branch/commit
    source: str  # release | tip | explicit


KNOWN_ALIASES = {
    "springframework": "https://github.com/spring-projects/spring-framework",
    "spring-framework": "https://github.com/spring-projects/spring-framework",
    "springframework-core": "https://github.com/spring-projects/spring-framework",
    "spring-boot": "https://github.com/spring-projects/spring-boot",
    "springboot": "https://github.com/spring-projects/spring-boot",
    "rocketmq": "https://github.com/apache/rocketmq",
    "redisson": "https://github.com/redisson/redisson",
    "jdk": "https://github.com/openjdk/jdk",
    "openjdk": "https://github.com/openjdk/jdk",
    "gson": "https://github.com/google/gson",
    "sentinel": "https://github.com/alibaba/Sentinel",
    "hikaricp": "https://github.com/brettwooldridge/HikariCP",
    "disruptor": "https://github.com/LMAX-Exchange/disruptor",
    "transformers": "https://github.com/huggingface/transformers",
    "django": "https://github.com/django/django",
    "flask": "https://github.com/pallets/flask",
    "sqlalchemy": "https://github.com/sqlalchemy/sqlalchemy",
    "pytorch": "https://github.com/pytorch/pytorch",
    "fastapi": "https://github.com/fastapi/fastapi",
    "paddleocr": "https://github.com/PaddlePaddle/PaddleOCR",
    "elasticsearch": "https://github.com/elastic/elasticsearch",
    "rxjava": "https://github.com/ReactiveX/RxJava",
    "arthas": "https://github.com/alibaba/arthas",
    "netty": "https://github.com/netty/netty",
    "nacos": "https://github.com/alibaba/nacos",
    "keycloak": "https://github.com/keycloak/keycloak",
    "gin": "https://github.com/gin-gonic/gin",
    "harness": "https://github.com/harness/harness",
    "ollama": "https://github.com/ollama/ollama",
    "kubernetes": "https://github.com/kubernetes/kubernetes",
    "k8s": "https://github.com/kubernetes/kubernetes",
    "go": "https://github.com/golang/go",
    "golang": "https://github.com/golang/go",
    "ragflow": "https://github.com/infiniflow/ragflow",
    "prometheus": "https://github.com/prometheus/prometheus",
    "loki": "https://github.com/grafana/loki",
}


AMBIGUOUS = {
    "spring": (
        "「spring」含义模糊，请确认具体仓库，例如：\n"
        "  - https://github.com/spring-projects/spring-framework\n"
        "  - https://github.com/spring-projects/spring-boot\n"
        "  - https://github.com/spring-cloud/spring-cloud-gateway"
    ),
    "java": (
        "「java」含义模糊，请确认具体仓库，例如：\n"
        "  - OpenJDK: https://github.com/openjdk/jdk\n"
        "  - Log4j2: https://github.com/apache/logging-log4j2\n"
        "  - 或其他具体 Java 开源项目的 GitHub/Gitee 地址"
    ),
}


def normalize_repo_url(project: str) -> str:
    p = project.strip()
    key = p.lower().replace("_", "-")
    if key in AMBIGUOUS:
        raise SystemExit(f"[oca] 目标不明确：{AMBIGUOUS[key]}")
    if key in KNOWN_ALIASES:
        return KNOWN_ALIASES[key]
    if p.startswith("http://") or p.startswith("https://") or p.startswith("git@"):
        return p
    if re.fullmatch(r"[\w.-]+/[\w.-]+", p):
        return f"https://github.com/{p}"
    raise SystemExit(
        f"[oca] 无法识别项目「{project}」。请提供 GitHub/Gitee 完整地址，"
        f"或 org/repo 形式，例如 spring-projects/spring-framework。"
    )


def parse_github_owner_repo(repo_url: str) -> tuple[str, str]:
    if repo_url.startswith("git@"):
        path = repo_url.split(":", 1)[-1]
    else:
        path = urlparse(repo_url).path.lstrip("/")
    if path.endswith(".git"):
        path = path[:-4]
    parts = path.split("/")
    if len(parts) < 2:
        raise SystemExit(f"[oca] 无法解析仓库 owner/repo: {repo_url}")
    return parts[0], parts[1]


def _gh_api(path: str) -> dict | list | None:
    try:
        out = subprocess.check_output(
            ["gh", "api", path],
            stderr=subprocess.STDOUT,
            text=True,
        )
        return json.loads(out)
    except (subprocess.CalledProcessError, FileNotFoundError, json.JSONDecodeError):
        return None


def _strip_v(tag: str) -> str:
    return tag[1:] if tag.startswith("v") and re.match(r"^v\d", tag) else tag


def resolve_version(repo_url: str, version: str | None) -> ResolvedTarget:
    owner, repo = parse_github_owner_repo(repo_url)
    clone_url = f"https://github.com/{owner}/{repo}.git"

    if version and version.lower() not in {"latest", "last", "newest", "最新"}:
        git_ref = version if version.startswith("v") else version
        # 优先尝试带 v 的 tag
        return ResolvedTarget(
            repo_url=f"https://github.com/{owner}/{repo}",
            clone_url=clone_url,
            owner=owner,
            repo=repo,
            version=_strip_v(version),
            git_ref=git_ref,
            source="explicit",
        )

    data = _gh_api(f"repos/{owner}/{repo}/releases/latest")
    if isinstance(data, dict) and data.get("tag_name"):
        tag = str(data["tag_name"])
        return ResolvedTarget(
            repo_url=f"https://github.com/{owner}/{repo}",
            clone_url=clone_url,
            owner=owner,
            repo=repo,
            version=_strip_v(tag),
            git_ref=tag,
            source="release",
        )

    # 无 Release：取默认分支 tip
    meta = _gh_api(f"repos/{owner}/{repo}")
    default_branch = "main"
    if isinstance(meta, dict) and meta.get("default_branch"):
        default_branch = str(meta["default_branch"])
    tip = _gh_api(f"repos/{owner}/{repo}/commits/{default_branch}")
    sha = "HEAD"
    if isinstance(tip, dict) and tip.get("sha"):
        sha = str(tip["sha"])[:12]
    return ResolvedTarget(
        repo_url=f"https://github.com/{owner}/{repo}",
        clone_url=clone_url,
        owner=owner,
        repo=repo,
        version=f"{default_branch}-{sha}",
        git_ref=default_branch,
        source="tip",
    )
