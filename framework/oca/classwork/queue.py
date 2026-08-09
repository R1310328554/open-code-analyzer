from __future__ import annotations

import json
import time
from dataclasses import dataclass
from pathlib import Path


@dataclass
class QueuePaths:
    root: Path

    @property
    def dir(self) -> Path:
        return self.root

    @property
    def pending(self) -> Path:
        return self.root / "pending.txt"

    @property
    def done(self) -> Path:
        return self.root / "done.txt"

    @property
    def failed(self) -> Path:
        return self.root / "failed.txt"

    @property
    def current(self) -> Path:
        return self.root / "CURRENT.json"

    @property
    def log(self) -> Path:
        return self.root / "worker.log"


PRIORITY_MODULES = [
    "spring-beans",
    "spring-context",
    "spring-core",
    "spring-aop",
    "spring-tx",
    "spring-webmvc",
    "spring-web",
    "spring-jdbc",
    "spring-expression",
]

# 每个模块内优先精读的类（相对 src/main/java 之后的路径片段或文件名）
PRIORITY_FILES = [
    "DefaultListableBeanFactory.java",
    "AbstractBeanFactory.java",
    "AbstractAutowireCapableBeanFactory.java",
    "DefaultSingletonBeanRegistry.java",
    "ConstructorResolver.java",
    "AutowiredAnnotationBeanPostProcessor.java",
    "AbstractApplicationContext.java",
    "ConfigurationClassPostProcessor.java",
    "ConfigurationClassParser.java",
    "DispatcherServlet.java",
    "JdkDynamicAopProxy.java",
    "CglibAopProxy.java",
    "TransactionAspectSupport.java",
    "TransactionInterceptor.java",
    "ResolvableType.java",
    "DataClassRowMapper.java",
    "JdbcTemplate.java",
    "BeanPropertyRowMapper.java",
]


SOURCE_GLOBS = ("*.java", "*.kt", "*.py", "*.go", "*.ts", "*.js", "*.rs")
SKIP_DIR_PARTS = {
    ".git", "build", "target", "node_modules", ".gradle", "out", "dist",
    "testdata", "vendor", "__pycache__", ".tox", ".venv", "venv",
    "third_party", "third-party", "examples", "example", "samples", "sample",
    "docs", "doc", "benchmarks", "benchmark",
}


def _should_skip(rel: str) -> bool:
    parts = rel.split("/")
    name = parts[-1]
    if name.endswith("package-info.java"):
        return True
    if name.startswith("test_") or name.endswith("_test.go") or name.endswith("_test.py"):
        return True
    if "/test/" in f"/{rel}/" or "/tests/" in f"/{rel}/" or "/testing/" in f"/{rel}/":
        return True
    return any(p in SKIP_DIR_PARTS for p in parts)


def build_queue(analyzed_or_original: Path, out_dir: Path, modules: list[str] | None = None) -> int:
    """扫描源码生成 pending 队列（优先核心类）。

    - 若给出 modules 且存在 ``<mod>/src/main/java``：按 Spring 风格模块扫描
    - 否则：在整棵 original 树中按语言扩展名扫描（Java/Python/Go 等）
    """
    out_dir.mkdir(parents=True, exist_ok=True)
    mods = modules or []
    files: list[str] = []

    spring_style = False
    if mods:
        for mod in mods:
            base = analyzed_or_original / mod / "src" / "main" / "java"
            if base.exists():
                spring_style = True
                for p in sorted(base.rglob("*.java")):
                    rel = str(p.relative_to(analyzed_or_original)).replace("\\", "/")
                    if not _should_skip(rel):
                        files.append(rel)
            else:
                # 模块可能是路径前缀（如 src/java.base）
                base2 = analyzed_or_original / mod
                if base2.exists():
                    for pat in SOURCE_GLOBS:
                        for p in sorted(base2.rglob(pat)):
                            rel = str(p.relative_to(analyzed_or_original)).replace("\\", "/")
                            if not _should_skip(rel):
                                files.append(rel)

    if not files and not spring_style:
        # 全仓多语言扫描
        for pat in SOURCE_GLOBS:
            for p in sorted(analyzed_or_original.rglob(pat)):
                rel = str(p.relative_to(analyzed_or_original)).replace("\\", "/")
                if _should_skip(rel):
                    continue
                files.append(rel)
        # 若未指定 modules，保留 PRIORITY_MODULES 元信息供 spring 仓使用
        if not mods:
            mods = list(PRIORITY_MODULES)

    def sort_key(rel: str) -> tuple:
        name = Path(rel).name
        try:
            mod = rel.split("/", 1)[0]
            mod_i = mods.index(mod) if mod in mods else 999
        except Exception:
            mod_i = 999
        pri = PRIORITY_FILES.index(name) if name in PRIORITY_FILES else 10_000
        return (mod_i, pri, rel)

    files = sorted(set(files), key=sort_key)
    pending = out_dir / "pending.txt"
    pending.write_text("\n".join(files) + ("\n" if files else ""), encoding="utf-8")
    for name in ("done.txt", "failed.txt"):
        p = out_dir / name
        if not p.exists():
            p.write_text("", encoding="utf-8")
    meta = {
        "created_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "total": len(files),
        "modules": mods,
        "mode": "class-by-class-understand",
        "note": "逐个类精读：读懂代码后再英译中/补充注释，禁止批量查找替换机翻；版权头保留英文",
    }
    (out_dir / "meta.json").write_text(json.dumps(meta, ensure_ascii=False, indent=2), encoding="utf-8")
    return len(files)


def _read_lines(path: Path) -> list[str]:
    if not path.exists():
        return []
    return [ln.strip() for ln in path.read_text(encoding="utf-8").splitlines() if ln.strip()]


def _write_lines(path: Path, lines: list[str]) -> None:
    path.write_text(("\n".join(lines) + ("\n" if lines else "")), encoding="utf-8")


def claim_next(out_dir: Path) -> str | None:
    """取出下一个待处理文件，写入 CURRENT.json；若已有未完成 CURRENT 则返回它。"""
    qp = QueuePaths(out_dir)
    if qp.current.exists():
        cur = json.loads(qp.current.read_text(encoding="utf-8"))
        if cur.get("status") in {"claimed", "processing"}:
            return cur.get("file")
    pending = _read_lines(qp.pending)
    done = set(_read_lines(qp.done))
    failed = set(_read_lines(qp.failed))
    while pending:
        rel = pending.pop(0)
        if rel in done or rel in failed:
            continue
        _write_lines(qp.pending, pending)
        payload = {
            "file": rel,
            "status": "claimed",
            "claimed_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            "instruction": (
                "请完整阅读 original 中该类源码，先理解其职责与关键方法，"
                "再改写 analyzed 对应文件：把英文注释译为通顺中文，并为字段/复杂方法补充中文说明。"
                "禁止批量机翻或无理解的查找替换。"
            ),
        }
        qp.current.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
        return rel
    return None


def mark_done(out_dir: Path, rel: str, *, note: str = "") -> None:
    qp = QueuePaths(out_dir)
    done = _read_lines(qp.done)
    if rel not in done:
        done.append(rel)
        _write_lines(qp.done, done)
    if qp.current.exists():
        cur = json.loads(qp.current.read_text(encoding="utf-8"))
        if cur.get("file") == rel:
            cur["status"] = "done"
            cur["done_at"] = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
            if note:
                cur["note"] = note
            qp.current.write_text(json.dumps(cur, ensure_ascii=False, indent=2), encoding="utf-8")
            # 完成后清除 CURRENT，便于领取下一个
            qp.current.unlink(missing_ok=True)
    with qp.log.open("a", encoding="utf-8") as f:
        f.write(f"{time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())} DONE {rel} {note}\n")


def mark_failed(out_dir: Path, rel: str, *, reason: str) -> None:
    qp = QueuePaths(out_dir)
    failed = _read_lines(qp.failed)
    if rel not in failed:
        failed.append(rel)
        _write_lines(qp.failed, failed)
    if qp.current.exists():
        qp.current.unlink(missing_ok=True)
    with qp.log.open("a", encoding="utf-8") as f:
        f.write(f"{time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())} FAIL {rel} {reason}\n")


def stats(out_dir: Path) -> dict:
    qp = QueuePaths(out_dir)
    return {
        "pending": len(_read_lines(qp.pending)),
        "done": len(_read_lines(qp.done)),
        "failed": len(_read_lines(qp.failed)),
        "current": json.loads(qp.current.read_text(encoding="utf-8"))["file"]
        if qp.current.exists()
        else None,
    }
