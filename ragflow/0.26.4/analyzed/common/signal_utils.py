"""
Unix 信号处理：SIGUSR1 启动 tracemalloc 快照，SIGUSR2 停止内存追踪。
"""

#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
Unix 信号处理：SIGUSR1 启动 tracemalloc 快照，SIGUSR2 停止内存追踪。
"""



import os
import sys
from datetime import datetime
import logging
import tracemalloc
from common.log_utils import get_project_base_directory


# SIGUSR1：启动 tracemalloc 并写入内存快照到 logs 目录
def start_tracemalloc_and_snapshot(signum, frame):
    # 首次调用启动追踪；每次触发 dump 快照并记录 RSS/峰值
    if not tracemalloc.is_tracing():
        logging.info("start tracemalloc")
        tracemalloc.start()
    else:
        logging.info("tracemalloc is already running")

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    snapshot_file = f"snapshot_{timestamp}.trace"
    snapshot_file = os.path.abspath(os.path.join(get_project_base_directory(), "logs", f"{os.getpid()}_snapshot_{timestamp}.trace"))

    snapshot = tracemalloc.take_snapshot()
    snapshot.dump(snapshot_file)
    current, peak = tracemalloc.get_traced_memory()
    if sys.platform == "win32":
        import psutil

        process = psutil.Process()
        max_rss = process.memory_info().rss / 1024
    else:
        import resource

        max_rss = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss
    logging.info(f"taken snapshot {snapshot_file}. max RSS={max_rss / 1000:.2f} MB, current memory usage: {current / 10**6:.2f} MB, Peak memory usage: {peak / 10**6:.2f} MB")


# SIGUSR2：停止 tracemalloc 追踪
def stop_tracemalloc(signum, frame):
    # 若正在追踪则停止，否则仅记录日志
    if tracemalloc.is_tracing():
        logging.info("stop tracemalloc")
        tracemalloc.stop()
    else:
        logging.info("tracemalloc not running")
