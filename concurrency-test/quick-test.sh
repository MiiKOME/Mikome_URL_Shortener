#!/bin/bash

# 快速并发测试脚本 - 使用 curl 和 GNU parallel

echo "🚀 快速并发测试开始..."

# 配置参数
BASE_URL="http://localhost:8080"
CONCURRENT_USERS=50
REQUESTS_PER_USER=5

# 检查依赖
if ! command -v parallel &> /dev/null; then
    echo "❌ GNU parallel 未安装"
    echo "安装方法: brew install parallel (macOS) 或 apt-get install parallel (Ubuntu)"
    exit 1
fi

# 检查服务器状态
echo "检查服务器状态..."
if ! curl -s "${BASE_URL}/api/urls/statistics" > /dev/null; then
    echo "❌ 服务器不可用，请确保后端服务正在运行"
    exit 1
fi
echo "✅ 服务器状态正常"

# 创建测试函数
test_create_url() {
    local user_id=$1
    local request_id=$2
    local url="https://example.com/user${user_id}/req${request_id}?t=$(date +%s%N)"

    # 记录开始时间
    local start_time=$(date +%s%N)

    # 发送请求
    local response=$(curl -s -w "%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -d "{\"url\":\"${url}\"}" \
        "${BASE_URL}/api/urls/shorten" 2>/dev/null)

    # 记录结束时间
    local end_time=$(date +%s%N)
    local response_time=$(( (end_time - start_time) / 1000000 )) # 转换为毫秒

    local http_code="${response: -3}"
    local body="${response%???}"

    # 输出结果（格式: user_id,request_id,http_code,response_time_ms,success）
    if [[ "$http_code" == "200" ]]; then
        echo "${user_id},${request_id},${http_code},${response_time},1"
    else
        echo "${user_id},${request_id},${http_code},${response_time},0"
    fi
}

# 导出函数供parallel使用
export -f test_create_url
export BASE_URL

# 生成测试任务
echo "生成测试任务..."
tasks_file=$(mktemp)
for user in $(seq 1 $CONCURRENT_USERS); do
    for req in $(seq 1 $REQUESTS_PER_USER); do
        echo "$user $req" >> "$tasks_file"
    done
done

total_requests=$((CONCURRENT_USERS * REQUESTS_PER_USER))
echo "总请求数: $total_requests"
echo "并发用户数: $CONCURRENT_USERS"
echo "每用户请求数: $REQUESTS_PER_USER"

# 创建结果文件
results_file=$(mktemp)

echo "开始并发测试..."
start_time=$(date +%s)

# 使用parallel执行并发测试
parallel -j $CONCURRENT_USERS --colsep ' ' test_create_url {1} {2} < "$tasks_file" > "$results_file"

end_time=$(date +%s)
test_duration=$((end_time - start_time))

echo "测试完成，分析结果..."

# 分析结果
total_requests=$(wc -l < "$results_file")
success_count=$(awk -F',' '$5==1' "$results_file" | wc -l)
error_count=$(awk -F',' '$5==0' "$results_file" | wc -l)

# 计算响应时间统计
response_times=$(awk -F',' '{print $4}' "$results_file" | sort -n)
avg_response_time=$(echo "$response_times" | awk '{sum+=$1} END {print sum/NR}')
min_response_time=$(echo "$response_times" | head -1)
max_response_time=$(echo "$response_times" | tail -1)

# 计算百分位数
p50_line=$((total_requests / 2))
p95_line=$((total_requests * 95 / 100))
p99_line=$((total_requests * 99 / 100))

p50_response_time=$(echo "$response_times" | sed -n "${p50_line}p")
p95_response_time=$(echo "$response_times" | sed -n "${p95_line}p")
p99_response_time=$(echo "$response_times" | sed -n "${p99_line}p")

# 计算QPS
qps=$(echo "scale=2; $total_requests / $test_duration" | bc)

# 统计HTTP状态码
http_codes=$(awk -F',' '{print $3}' "$results_file" | sort | uniq -c | sort -nr)

# 打印结果
echo ""
echo "================== 测试结果报告 =================="
echo "📊 基本统计:"
echo "   总请求数: $total_requests"
echo "   成功请求: $success_count"
echo "   失败请求: $error_count"
echo "   成功率: $(echo "scale=2; $success_count * 100 / $total_requests" | bc)%"
echo "   测试时长: ${test_duration}s"
echo "   QPS: $qps"
echo ""
echo "⏱️  响应时间 (毫秒):"
echo "   平均响应时间: ${avg_response_time}ms"
echo "   最小响应时间: ${min_response_time}ms"
echo "   最大响应时间: ${max_response_time}ms"
echo "   P50 (中位数): ${p50_response_time}ms"
echo "   P95: ${p95_response_time}ms"
echo "   P99: ${p99_response_time}ms"
echo ""
echo "📈 HTTP状态码分布:"
echo "$http_codes"
echo ""

# 检查是否有重复的短码（并发问题检测）
if [[ -f "$results_file" ]]; then
    echo "🔍 检查短码重复问题..."

    # 提取所有成功响应的短码
    short_codes_file=$(mktemp)
    awk -F',' '$5==1' "$results_file" | while IFS=',' read user req http_code response_time success; do
        # 这里需要实际解析JSON响应体来提取短码
        # 由于bash解析JSON比较复杂，我们用一个简化的方法
        echo "检查短码重复需要解析JSON响应，建议使用Node.js版本的测试工具"
    done

    rm -f "$short_codes_file"
fi

echo "=================================================="

# 清理临时文件
rm -f "$tasks_file" "$results_file"

echo "✅ 测试完成！"
echo ""
echo "💡 测试建议："
echo "   - 如果成功率低于95%，说明系统存在并发问题"
echo "   - 如果P95响应时间超过1000ms，说明性能有待优化"
echo "   - 建议运行Node.js版本的详细测试：node concurrency-test/load-test.js"