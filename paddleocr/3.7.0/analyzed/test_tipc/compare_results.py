# TIPC 推理结果对比：解析日志与真值文件，按精度类型 assert_allclose 校验
import numpy as np
import os
import subprocess
import json
import argparse
import glob


# 构建命令行参数：atol/rtol 容差、真值路径、日志路径与精度类型
def init_args():
    parser = argparse.ArgumentParser()
    # params for testing assert allclose
    parser.add_argument("--atol", type=float, default=1e-3)
    parser.add_argument("--rtol", type=float, default=1e-3)
    parser.add_argument("--gt_file", type=str, default="")
    parser.add_argument("--log_file", type=str, default="")
    parser.add_argument("--precision", type=str, default="fp32")
    return parser


# 解析命令行并返回 Namespace
def parse_args():
    parser = init_args()
    return parser.parse_args()


# 执行 shell 命令，成功返回 stdout 字符串，失败返回 None
def run_shell_command(cmd):
    p = subprocess.Popen(
        cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True
    )
    out, err = p.communicate()

    if p.returncode == 0:
        return out.decode("utf-8")
    else:
        return None


# 从日志 grep 指定 key，解析 JSON 或四坐标数组为预测字典
def parser_results_from_log_by_name(log_path, names_list):
    if not os.path.exists(log_path):
        raise ValueError("The log file {} does not exists!".format(log_path))

    if names_list is None or len(names_list) < 1:
        return []

    parser_results = {}
    for name in names_list:
        cmd = "grep {} {}".format(name, log_path)
        outs = run_shell_command(cmd)
        outs = outs.split("\n")[0]
        result = outs.split("{}".format(name))[-1]
        try:
            result = json.loads(result)
        except:
            result = np.array([int(r) for r in result.split()]).reshape(-1, 4)
        parser_results[name] = result
    return parser_results


# 从 tab 分隔真值文件加载 {图像名: 检测结果} 字典
def load_gt_from_file(gt_file):
    if not os.path.exists(gt_file):
        raise ValueError("The log file {} does not exists!".format(gt_file))
    with open(gt_file, "r") as f:
        data = f.readlines()
        f.close()
    parser_gt = {}
    for line in data:
        image_name, result = line.strip("\n").split("\t")
        image_name = image_name.split("/")[-1]
        try:
            result = json.loads(result)
        except:
            result = np.array([int(r) for r in result.split()]).reshape(-1, 4)
        parser_gt[image_name] = result
    return parser_gt


# glob 匹配多真值文件，按 fp32/fp16/int8 文件名分类汇总
def load_gt_from_txts(gt_file):
    gt_list = glob.glob(gt_file)
    gt_collection = {}
    for gt_f in gt_list:
        gt_dict = load_gt_from_file(gt_f)
        basename = os.path.basename(gt_f)
        if "fp32" in basename:
            gt_collection["fp32"] = [gt_dict, gt_f]
        elif "fp16" in basename:
            gt_collection["fp16"] = [gt_dict, gt_f]
        elif "int8" in basename:
            gt_collection["int8"] = [gt_dict, gt_f]
        else:
            continue
    return gt_collection


# 批量解析推理日志，返回 {日志文件名: 预测字典}
def collect_predict_from_logs(log_path, key_list):
    log_list = glob.glob(log_path)
    pred_collection = {}
    for log_f in log_list:
        pred_dict = parser_results_from_log_by_name(log_f, key_list)
        key = os.path.basename(log_f)
        pred_collection[key] = pred_dict

    return pred_collection


# 逐 key 对两字典 ndarray 做 np.testing.assert_allclose
def testing_assert_allclose(dict_x, dict_y, atol=1e-7, rtol=1e-7):
    for k in dict_x:
        np.testing.assert_allclose(
            np.array(dict_x[k]), np.array(dict_y[k]), atol=atol, rtol=rtol
        )


if __name__ == "__main__":
    # Usage:
    # python3.7 tests/compare_results.py --gt_file=./tests/results/*.txt  --log_file=./tests/output/infer_*.log

    args = parse_args()

    gt_collection = load_gt_from_txts(args.gt_file)
    key_list = gt_collection["fp32"][0].keys()

    pred_collection = collect_predict_from_logs(args.log_file, key_list)
    for filename in pred_collection.keys():
        if "fp32" in filename:
            gt_dict, gt_filename = gt_collection["fp32"]
        elif "fp16" in filename:
            gt_dict, gt_filename = gt_collection["fp16"]
        elif "int8" in filename:
            gt_dict, gt_filename = gt_collection["int8"]
        else:
            continue
        pred_dict = pred_collection[filename]

        try:
            testing_assert_allclose(gt_dict, pred_dict, atol=args.atol, rtol=args.rtol)
            print(
                "Assert allclose passed! The results of {} and {} are consistent!".format(
                    filename, gt_filename
                )
            )
        except Exception as E:
            print(E)
            raise ValueError(
                "The results of {} and the results of {} are inconsistent!".format(
                    filename, gt_filename
                )
            )
