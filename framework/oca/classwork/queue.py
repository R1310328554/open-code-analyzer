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


def build_queue(analyzed_or_original: Path, out_dir: Path, modules: list[str] | None = None) -> int:
    """从 original/analyzed 扫描 main java，生成 pending 队列（优先核心类）。"""
    out_dir.mkdir(parents=True, exist_ok=True)
    mods = modules or PRIORITY_MODULES
    files: list[str] = []
    for mod in mods:
        base = analyzed_or_original / mod / "src" / "main" / "java"
        if not base.exists():
            continue
        for p in sorted(base.rglob("*.java")):
            rel = str(p.relative_to(analyzed_or_original)).replace("\\", "/")
            if rel.endswith("package-info.java"):
                continue
            files.append(rel)

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
        "note": "逐个类精读：读懂代码后再英译中/补充注释，禁止批量查找替换机翻",
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
