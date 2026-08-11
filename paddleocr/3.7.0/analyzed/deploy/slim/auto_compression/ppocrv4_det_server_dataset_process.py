# ppocrv4_det_server_dataset_process.py — 筛选 PP-OCRv4 检测服务端小图子集。
# 从完整测试集复制尺寸与长宽比符合条件的样本及标注，供压缩/蒸馏实验使用。

import osimport os
import cv2

# 原始 v4.4 测试集根目录与标注文件路径。
dataset_path = dataset_path = "datasets/v4_4_test_dataset"
annotation_file = "datasets/v4_4_test_dataset/label.txt"

# 筛选后小图输出目录及对应 label.txt。
small_images_path = small_images_path = "datasets/v4_4_test_dataset_small"
new_annotation_file = "datasets/v4_4_test_dataset_small/label.txt"

os.makedirs(small_images_path, exist_ok=True)

with open(annotation_file, "r") as f:
    lines = f.readlines()

    # 逐行读取标注，解析图像文件名并尝试加载。
for i, line in enumerate(lines):for i, line in enumerate(lines):
    image_name = line.split("   ")[0]

    image_path = os.path.join(dataset_path, image_name)

    try:
        image = cv2.imread(image_path)
        height, width, _ = image.shape

        # 筛选条件：宽高均 <2000 且长宽比 <2，满足则复制图像并追加标注行。
        # 如果图像的宽度和高度都小于2000而且长宽比小于2，将其复制到新的文件夹，并保存其标注信息
        if height < 2000 and width < 2000:
            if max(height, width) / min(height, width) < 2:
                print(i, height, width, image_path)
                small_image_path = os.path.join(small_images_path, image_name)
                cv2.imwrite(small_image_path, image)
                with open(new_annotation_file, "a") as f:
                    f.write(f"{line}")
    except:
        continue
