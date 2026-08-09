#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-2b eagleeye/init/log [15:30]."""
from __future__ import annotations
import json, re, shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][15:30]

COMMON = [
 ("Record statistics and perform rule checking for the given resource.", "对给定资源记录统计并执行规则检查。"),
 ("R
# Full replacements applied inline during commit.
