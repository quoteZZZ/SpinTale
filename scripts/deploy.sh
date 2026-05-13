#!/bin/bash
set -e

echo "========================================"
echo "🚀 SpinTale AI 一键部署脚本"
echo "========================================"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker 未安装，请先安装 Docker${NC}"
        echo "安装指南：https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        echo -e "${RED}❌ Docker Compose 未安装${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Docker 已安装${NC}"
}

# 检查 MySQL 客户端
check_mysql() {
    if command -v mysql &> /dev/null; then
        echo -e "${GREEN}✅ MySQL 客户端已安装${NC}"
    else
        echo -e "${YELLOW}⚠️  MySQL 客户端未安装，数据库升级需要手动执行${NC}"
    fi
}

# 启动基础设施服务
start_infrastructure() {
    echo ""
    echo "📦 步骤 1: 启动基础设施服务 (Milvus + Temporal)"
    echo "----------------------------------------"
    
    if [ -f "docker-compose.yml" ]; then
        docker compose up -d
        
        echo ""
        echo "⏳ 等待服务启动..."
        
        # 等待 Milvus 就绪
        echo "   - 等待 Milvus..."
        for i in {1..30}; do
            if curl -s http://localhost:9091/healthz > /dev/null 2>&1; then
                echo -e "   ${GREEN}✅ Milvus 已就绪${NC}"
                break
            fi
            if [ $i -eq 30 ]; then
                echo -e "   ${RED}❌ Milvus 启动超时${NC}"
                exit 1
            fi
            sleep 2
        done
        
        # 等待 Temporal 就绪
        echo "   - 等待 Temporal..."
        for i in {1..30}; do
            if curl -s http://localhost:8233 > /dev/null 2>&1; then
                echo -e "   ${GREEN}✅ Temporal UI 已就绪${NC}"
                break
            fi
            if [ $i -eq 30 ]; then
                echo -e "   ${YELLOW}⚠️  Temporal UI 启动较慢，可稍后访问${NC}"
                break
            fi
            sleep 2
        done
        
    else
        echo -e "${RED}❌ docker-compose.yml 文件不存在${NC}"
        exit 1
    fi
}

# 构建应用
build_application() {
    echo ""
    echo "🔨 步骤 2: 构建 SpinTale AI 应用"
    echo "----------------------------------------"
    
    if [ -d "spintale-ai" ]; then
        cd spintale-ai
        
        # 检查 Maven
        if ! command -v mvn &> /dev/null; then
            echo -e "${RED}❌ Maven 未安装${NC}"
            echo "请安装 Maven: https://maven.apache.org/download.cgi"
            exit 1
        fi
        
        echo "开始编译..."
        mvn clean package -DskipTests -q
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ 构建成功${NC}"
            cd ..
        else
            echo -e "${RED}❌ 构建失败${NC}"
            exit 1
        fi
    else
        echo -e "${RED}❌ spintale-ai 目录不存在${NC}"
        exit 1
    fi
}

# 配置环境变量
setup_environment() {
    echo ""
    echo "⚙️  步骤 3: 配置环境变量"
    echo "----------------------------------------"
    
    if [ -f ".env.example" ]; then
        if [ ! -f ".env" ]; then
            cp .env.example .env
            echo -e "${GREEN}✅ 已创建 .env 文件${NC}"
            echo "请编辑 .env 文件，设置以下变量："
            echo "   - OPENAI_API_KEY=your-api-key-here"
            echo "   - MYSQL_HOST=localhost"
            echo "   - MYSQL_PASSWORD=your-password"
        else
            echo -e "${GREEN}✅ .env 文件已存在${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  未找到 .env.example 文件${NC}"
        echo "请手动设置环境变量："
        echo "   export OPENAI_API_KEY=sk-xxx"
    fi
}

# 显示完成信息
show_completion() {
    echo ""
    echo "========================================"
    echo -e "${GREEN}✅ 部署完成！${NC}"
    echo "========================================"
    echo ""
    echo "📊 服务访问地址："
    echo "   - Temporal UI:      http://localhost:8233"
    echo "   - Milvus Admin:     http://localhost:9091"
    echo "   - AI API:           http://localhost:8080/ai/chat/message"
    echo ""
    echo "📝 下一步操作："
    echo "   1. 编辑 application-ai.yml 配置 API Key"
    echo "   2. 运行数据库升级脚本:"
    echo "      mysql -u root -p spin_tale < scripts/upgrade_ai_schema.sql"
    echo "   3. 启动应用:"
    echo "      cd spintale-ai && mvn spring-boot:run -Dspring-boot.run.profiles=ai"
    echo ""
    echo "📚 查看文档："
    echo "   - docs/guides/DEPLOYMENT_GUIDE.md"
    echo "   - docs/guides/FINAL_OPTIMIZATION_REPORT.md"
    echo ""
}

# 主函数
main() {
    check_docker
    check_mysql
    start_infrastructure
    setup_environment
    
    echo ""
    read -p "是否现在构建应用？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        build_application
    fi
    
    show_completion
}

# 执行主函数
main "$@"
