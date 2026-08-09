from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from . import __version__
from .annotate.pipeline import run_annotation_pipeline
from .arch.generator import generate_architecture_docs
from .fetch import fetch_source, sync_analyzed_from_original
from .resolve import normalize_repo_url, resolve_version
from .static.complexity import analyze_file_complexity, dump_complexity, filter_complex
from .static.inventory import build_inventory
from .static.scanner import dump_scan, scan_project
from .util.config import load_config
from .util.fs import write_text
from .util.paths import ProjectLayout, slugify_project_name


def _root_from_args(args: argparse.Namespace, cfg: dict) -> Path:
    root = getattr(args, "root", None) or cfg.get("project", {}).get("root") or "."
    return Path(root).resolve()


def _layout_for(args: argparse.Namespace, cfg: dict, project: str, version: str) -> ProjectLayout:
    out = cfg.get("outputs", {})
    return ProjectLayout(
        root=_root_from_args(args, cfg),
        project=project,
        version=version,
        original_dirname=out.get("original_dirname", "original"),
        analyzed_dirname=out.get("analyzed_dirname", "analyzed"),
        arch_dirname=out.get("arch_dirname", "架构说明"),
        module_docs_dirname=out.get("module_docs_dirname", "项目模块说明"),
        reports_dirname=out.get("reports_dirname", "_reports"),
    )


def cmd_resolve(args: argparse.Namespace) -> int:
    cfg = load_config(args.config)
    url = normalize_repo_url(args.project)
    target = resolve_version(url, args.version)
    project = args.name or slugify_project_name(args.project if "://" not in args.project and "/" not in args.project else target.repo)
    # 对 spring-framework URL，目录名用 springframework
    if project in {"springframework", "spring-framework"} or target.repo == "spring-framework":
        project = "springframework"
    payload = {
        "project": project,
        "repo_url": target.repo_url,
        "clone_url": target.clone_url,
        "version": target.version,
        "git_ref": target.git_ref,
        "source": target.source,
    }
    print(json.dumps(payload, ensure_ascii=False, indent=2))
    return 0


def cmd_fetch(args: argparse.Namespace) -> int:
    cfg = load_config(args.config)
    url = normalize_repo_url(args.project)
    target = resolve_version(url, args.version)
    project = args.name or (
        "springframework"
        if target.repo == "spring-framework"
        else slugify_project_name(target.repo)
    )
    layout = _layout_for(args, cfg, project, target.version)
    depth = args.depth or cfg.get("fetch", {}).get("depth", 1)
    fetch_source(target, layout, depth=depth)
    meta = {
        "project": project,
        "version": target.version,
        "git_ref": target.git_ref,
        "repo_url": target.repo_url,
        "source": target.source,
    }
    write_text(layout.version_dir / "META.json", json.dumps(meta, ensure_ascii=False, indent=2))
    print(f"[oca] fetch OK -> {layout.version_dir}")
    return 0


def cmd_scan(args: argparse.Namespace) -> int:
    cfg = load_config(args.config)
    layout = _layout_for(args, cfg, args.project, args.version)
    if not layout.original.exists():
        raise SystemExit(f"[oca] 找不到 original: {layout.original}，请先 fetch")
    ignore = cfg.get("analysis", {}).get("ignore", [])
    result = scan_project(layout.original, ignore=ignore)
    dump_scan(result, layout.reports / "scan.json", layout.reports / "scan.md")
    print(f"[oca] scan OK -> {layout.reports / 'scan.md'}")
    return 0


def cmd_complexity(args: argparse.Namespace) -> int:
    cfg = load_config(args.config)
    layout = _layout_for(args, cfg, args.project, args.version)
    root = layout.original if args.source == "original" else layout.analyzed
    if not root.exists():
        raise SystemExit(f"[oca] 找不到源码目录: {root}")
    hits = analyze_file_complexity(root)
    ccfg = cfg.get("analysis", {}).get("complexity", {})
    filtered = filter_complex(
        hits,
        cyclomatic_min=int(ccfg.get("cyclomatic_min", 8)),
        loc_min=int(ccfg.get("loc_min", 40)),
        skip_trivial_accessors=bool(cfg.get("analysis", {}).get("skip_trivial_accessors", True)),
    )
    dump_complexity(
        filtered,
        layout.reports / "complexity.json",
        layout.reports / "complexity.md",
    )
    build_inventory(
        root,
        filtered,
        layout.reports / "inventory.json",
        layout.reports / "inventory.md",
    )
    print(f"[oca] complexity OK -> {layout.reports / 'complexity.md'}")
    return 0


def cmd_arch(args: argparse.Namespace) -> int:
    cfg = load_config(args.config)
    layout = _layout_for(args, cfg, args.project, args.version)
    meta_path = layout.version_dir / "META.json"
    if not meta_path.exists():
        raise SystemExit("[oca] 缺少 META.json，请先 fetch")
    meta = json.loads(meta_path.read_text(encoding="utf-8"))
    from .resolve import ResolvedTarget

    target = ResolvedTarget(
        repo_url=meta["repo_url"],
        clone_url=meta.get("clone_url", meta["repo_url"] + ".git"),
        owner=meta["repo_url"].rstrip("/").split("/")[-2],
        repo=meta["repo_url"].rstrip("/").split("/")[-1],
        version=meta["version"],
        git_ref=meta["git_ref"],
        source=meta.get("source", "unknown"),
    )
    ignore = cfg.get("analysis", {}).get("ignore", [])
    scan = scan_project(layout.original, ignore=ignore)
    dump_scan(scan, layout.reports / "scan.json", layout.reports / "scan.md")
    generate_architecture_docs(layout, target, scan)
    print(f"[oca] arch docs OK -> {layout.arch}")
    return 0


def cmd_annotate(args: argparse.Namespace) -> int:
    cfg = load_config(args.config)
    layout = _layout_for(args, cfg, args.project, args.version)
    sync_analyzed_from_original(layout, force=args.force_sync)
    # 需要复杂度数据
    cpath = layout.reports / "complexity.json"
    if not cpath.exists() or args.refresh_complexity:
        hits_all = analyze_file_complexity(layout.original)
        ccfg = cfg.get("analysis", {}).get("complexity", {})
        hits = filter_complex(
            hits_all,
            cyclomatic_min=int(ccfg.get("cyclomatic_min", 8)),
            loc_min=int(ccfg.get("loc_min", 40)),
            skip_trivial_accessors=bool(cfg.get("analysis", {}).get("skip_trivial_accessors", True)),
        )
        dump_complexity(hits, cpath, layout.reports / "complexity.md")
    else:
        from .static.complexity import ComplexityHit

        raw = json.loads(cpath.read_text(encoding="utf-8"))
        hits = [ComplexityHit(**row) for row in raw]

    plan_path = Path(args.plan) if args.plan else layout.reports / "annotation-plan.json"
    stats = run_annotation_pipeline(
        layout,
        hits,
        plan_path=plan_path,
        annotate_fields=bool(cfg.get("analysis", {}).get("annotate_fields", True)),
        max_files=args.max_files,
    )
    print(f"[oca] annotate OK -> {stats}")
    return 0


def cmd_analyze(args: argparse.Namespace) -> int:
    """一键：resolve + fetch + scan + complexity + arch + annotate。"""
    # fetch
    args.depth = getattr(args, "depth", None)
    rc = cmd_fetch(args)
    if rc != 0:
        return rc

    # 回填 project/version 到后续命令
    cfg = load_config(args.config)
    url = normalize_repo_url(args.project)
    target = resolve_version(url, args.version)
    project = args.name or (
        "springframework"
        if target.repo == "spring-framework"
        else slugify_project_name(target.repo)
    )
    args.project = project
    args.version = target.version
    args.source = "original"
    args.force_sync = getattr(args, "force_sync", False)
    args.refresh_complexity = True
    args.plan = getattr(args, "plan", None)
    args.max_files = getattr(args, "max_files", None)

    cmd_scan(args)
    cmd_complexity(args)
    cmd_arch(args)
    cmd_annotate(args)
    layout = _layout_for(args, cfg, project, target.version)
    print(
        "\n".join(
            [
                "[oca] 全量分析完成",
                f"  project : {project}",
                f"  version : {target.version}",
                f"  original: {layout.original}",
                f"  analyzed: {layout.analyzed}",
                f"  arch    : {layout.arch}",
                f"  reports : {layout.reports}",
            ]
        )
    )
    return 0


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="oca",
        description="open-code-analyzer：开源项目源码意图/架构分析（CLI，无 UI）",
    )
    p.add_argument("--version", action="version", version=f"oca {__version__}")
    p.add_argument("--config", default=None, help="配置文件路径，默认 config/defaults.yaml")
    p.add_argument("--root", default=None, help="分析结果根目录，默认仓库根目录")

    sp = p.add_subparsers(dest="command", required=True)

    def add_project_version(sub):
        sub.add_argument("project", help="项目名 / org/repo / GitHub URL")
        sub.add_argument(
            "version",
            nargs="?",
            default="latest",
            help="版本号或 latest（默认取最新 Release）",
        )
        sub.add_argument("--name", default=None, help="本地目录项目名（默认从仓库名推导）")

    s = sp.add_parser("resolve", help="解析仓库与版本，不落盘")
    add_project_version(s)
    s.set_defaults(func=cmd_resolve)

    s = sp.add_parser("fetch", help="浅克隆源码到 original/")
    add_project_version(s)
    s.add_argument("--depth", type=int, default=None)
    s.set_defaults(func=cmd_fetch)

    s = sp.add_parser("scan", help="扫描 original，生成模块/语言报告")
    s.add_argument("project")
    s.add_argument("version")
    s.set_defaults(func=cmd_scan)

    s = sp.add_parser("complexity", help="复杂度分析与意图候选清单")
    s.add_argument("project")
    s.add_argument("version")
    s.add_argument("--source", choices=["original", "analyzed"], default="original")
    s.set_defaults(func=cmd_complexity)

    s = sp.add_parser("arch", help="生成架构说明/项目模块说明")
    s.add_argument("project")
    s.add_argument("version")
    s.set_defaults(func=cmd_arch)

    s = sp.add_parser("annotate", help="同步 analyzed 并写入中文意图注释")
    s.add_argument("project")
    s.add_argument("version")
    s.add_argument("--plan", default=None, help="annotation-plan.json 路径")
    s.add_argument("--max-files", type=int, default=None)
    s.add_argument("--force-sync", action="store_true")
    s.add_argument("--refresh-complexity", action="store_true")
    s.set_defaults(func=cmd_annotate)

    s = sp.add_parser("analyze", help="一键全量分析：fetch→scan→complexity→arch→annotate")
    add_project_version(s)
    s.add_argument("--depth", type=int, default=None)
    s.add_argument("--plan", default=None)
    s.add_argument("--max-files", type=int, default=None)
    s.add_argument("--force-sync", action="store_true")
    s.set_defaults(func=cmd_analyze)

    return p


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    try:
        return int(args.func(args))
    except KeyboardInterrupt:
        print("\n[oca] interrupted", file=sys.stderr)
        return 130


if __name__ == "__main__":
    raise SystemExit(main())
