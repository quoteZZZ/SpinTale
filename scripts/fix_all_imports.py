#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全面修复 AI 模块所有文件的 import 语句
根据新的目录结构自动修正 import 路径
"""

import re
from pathlib import Path

BASE_PATH = Path(r"E:\GitCode\SpinTale\spintale-ai\src\main\java\com\spintale\ai")

# 定义完整的包名映射规则（旧 -> 新）
IMPORT_MAPPINGS = {
    # core 模块
    r'import\s+com\.spintale\.ai\.model\.': 'import com.spintale.ai.core.model.',
    r'import\s+com\.spintale\.ai\.service\.': 'import com.spintale.ai.core.service.',
    r'import\s+com\.spintale\.ai\.annotation\.': 'import com.spintale.ai.core.annotation.',
    
    # features 模块（原 enhancement）
    r'import\s+com\.spintale\.ai\.enhancement\.': 'import com.spintale.ai.features.',
    
    # platform 模块（原 integration）
    r'import\s+com\.spintale\.ai\.integration\.': 'import com.spintale.ai.platform.',
    r'import\s+com\.spintale\.ai\.api\.': 'import com.spintale.ai.platform.client.',
    
    # agent 模块
    r'import\s+com\.spintale\.ai\.workflow\.': 'import com.spintale.ai.agent.workflow.',
    
    # observability 模块（原 infrastructure/metrics）
    r'import\s+com\.spintale\.ai\.infrastructure\.metrics\.': 'import com.spintale.ai.observability.',
    
    # generation 模块
    r'import\s+com\.spintale\.ai\.generation\.service\.': 'import com.spintale.ai.generation.text.',
    r'import\s+com\.spintale\.ai\.generation\.template\.': 'import com.spintale.ai.generation.template.',
    
    # retrieval 模块
    r'import\s+com\.spintale\.ai\.retrieval\.parser\.': 'import com.spintale.ai.retrieval.document.',
    
    # tool 模块
    r'import\s+com\.spintale\.ai\.tool\.AiTool': 'import com.spintale.ai.tool.registry.AiTool',
    r'import\s+com\.spintale\.ai\.tool\.ToolRegistry': 'import com.spintale.ai.tool.registry.ToolRegistry',
    r'import\s+com\.spintale\.ai\.tool\.ToolSchema': 'import com.spintale.ai.tool.registry.AiTool.ToolSchema',
}

def fix_imports(file_path):
    """修复单个文件的所有 import 语句"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # 应用所有 import 替换规则
    for pattern, replacement in IMPORT_MAPPINGS.items():
        content = re.sub(pattern, replacement, content)
    
    # 如果内容有变化，写回文件
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    print("开始全面修复 import 语句...")
    print(f"基路径: {BASE_PATH}\n")
    
    fixed_count = 0
    error_count = 0
    
    # 遍历所有 Java 文件
    for java_file in BASE_PATH.rglob('*.java'):
        try:
            if fix_imports(java_file):
                relative = java_file.relative_to(BASE_PATH)
                print(f"✓ 修复: {relative}")
                fixed_count += 1
        except Exception as e:
            print(f"✗ 失败: {java_file.relative_to(BASE_PATH)} - {e}")
            error_count += 1
    
    print(f"\n修复完成！成功: {fixed_count}, 失败: {error_count}")

if __name__ == '__main__':
    main()
