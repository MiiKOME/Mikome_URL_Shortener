const http = require('http');
const https = require('https');
const { performance } = require('perf_hooks');

// 配置参数
const CONFIG = {
  host: 'localhost',
  port: 8080,
  concurrentUsers: 100,    // 并发用户数
  requestsPerUser: 10,     // 每用户请求数
  testDurationMs: 30000,   // 测试持续时间(毫秒)
  rampUpTimeMs: 5000       // 并发递增时间
};

// 统计数据
let stats = {
  totalRequests: 0,
  successCount: 0,
  errorCount: 0,
  timeoutCount: 0,
  responseTimes: [],
  errors: {},
  startTime: 0,
  endTime: 0
};

// 测试用的URL列表
const testUrls = [
  'https://www.google.com',
  'https://www.github.com',
  'https://www.stackoverflow.com',
  'https://www.youtube.com',
  'https://www.facebook.com',
  'https://www.twitter.com',
  'https://www.linkedin.com',
  'https://www.reddit.com',
  'https://www.amazon.com',
  'https://www.netflix.com'
];

/**
 * 发送短链创建请求
 */
function createShortUrl(originalUrl) {
  return new Promise((resolve, reject) => {
    const startTime = performance.now();

    const data = JSON.stringify({
      url: originalUrl,
      expiresAt: null
    });

    const options = {
      hostname: CONFIG.host,
      port: CONFIG.port,
      path: '/api/urls/shorten',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data)
      },
      timeout: 5000 // 5秒超时
    };

    const req = http.request(options, (res) => {
      let responseData = '';

      res.on('data', (chunk) => {
        responseData += chunk;
      });

      res.on('end', () => {
        const endTime = performance.now();
        const responseTime = endTime - startTime;

        stats.totalRequests++;
        stats.responseTimes.push(responseTime);

        if (res.statusCode === 200) {
          stats.successCount++;
          try {
            const result = JSON.parse(responseData);
            resolve({
              success: true,
              responseTime,
              shortUrl: result.shortUrl,
              shortCode: result.shortCode
            });
          } catch (e) {
            stats.errorCount++;
            recordError('JSON_PARSE_ERROR', e.message);
            resolve({ success: false, responseTime, error: e.message });
          }
        } else {
          stats.errorCount++;
          recordError(`HTTP_${res.statusCode}`, responseData);
          resolve({ success: false, responseTime, error: `HTTP ${res.statusCode}` });
        }
      });
    });

    req.on('timeout', () => {
      stats.timeoutCount++;
      stats.errorCount++;
      recordError('TIMEOUT', 'Request timeout');
      req.destroy();
      resolve({ success: false, responseTime: 5000, error: 'Timeout' });
    });

    req.on('error', (e) => {
      stats.errorCount++;
      recordError('NETWORK_ERROR', e.message);
      resolve({ success: false, responseTime: 0, error: e.message });
    });

    req.write(data);
    req.end();
  });
}

/**
 * 记录错误
 */
function recordError(type, message) {
  if (!stats.errors[type]) {
    stats.errors[type] = { count: 0, messages: new Set() };
  }
  stats.errors[type].count++;
  stats.errors[type].messages.add(message);
}

/**
 * 模拟单个用户的行为
 */
async function simulateUser(userId) {
  console.log(`用户 ${userId} 开始测试...`);

  for (let i = 0; i < CONFIG.requestsPerUser; i++) {
    const randomUrl = testUrls[Math.floor(Math.random() * testUrls.length)];
    const uniqueUrl = `${randomUrl}?user=${userId}&req=${i}&t=${Date.now()}`;

    try {
      const result = await createShortUrl(uniqueUrl);

      if (result.success) {
        // 可以添加对生成的短链的访问测试
        // await testShortUrlAccess(result.shortCode);
      }

      // 随机等待 0-1 秒，模拟真实用户行为
      await sleep(Math.random() * 1000);

    } catch (error) {
      console.error(`用户 ${userId} 请求 ${i} 失败:`, error);
    }
  }

  console.log(`用户 ${userId} 测试完成`);
}

/**
 * 测试短链访问
 */
function testShortUrlAccess(shortCode) {
  return new Promise((resolve) => {
    const options = {
      hostname: CONFIG.host,
      port: CONFIG.port,
      path: `/${shortCode}`,
      method: 'GET',
      timeout: 3000
    };

    const req = http.request(options, (res) => {
      resolve({ success: res.statusCode === 302 });
    });

    req.on('timeout', () => {
      req.destroy();
      resolve({ success: false });
    });

    req.on('error', () => {
      resolve({ success: false });
    });

    req.end();
  });
}

/**
 * 延迟函数
 */
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * 计算统计数据
 */
function calculateStats() {
  if (stats.responseTimes.length === 0) {
    return {};
  }

  const sortedTimes = stats.responseTimes.sort((a, b) => a - b);
  const totalTime = stats.endTime - stats.startTime;

  return {
    totalRequests: stats.totalRequests,
    successRate: (stats.successCount / stats.totalRequests * 100).toFixed(2) + '%',
    errorRate: (stats.errorCount / stats.totalRequests * 100).toFixed(2) + '%',
    timeoutRate: (stats.timeoutCount / stats.totalRequests * 100).toFixed(2) + '%',
    avgResponseTime: (stats.responseTimes.reduce((a, b) => a + b, 0) / stats.responseTimes.length).toFixed(2) + 'ms',
    minResponseTime: sortedTimes[0].toFixed(2) + 'ms',
    maxResponseTime: sortedTimes[sortedTimes.length - 1].toFixed(2) + 'ms',
    p50ResponseTime: sortedTimes[Math.floor(sortedTimes.length * 0.5)].toFixed(2) + 'ms',
    p95ResponseTime: sortedTimes[Math.floor(sortedTimes.length * 0.95)].toFixed(2) + 'ms',
    p99ResponseTime: sortedTimes[Math.floor(sortedTimes.length * 0.99)].toFixed(2) + 'ms',
    requestsPerSecond: (stats.totalRequests / (totalTime / 1000)).toFixed(2),
    testDuration: (totalTime / 1000).toFixed(2) + 's',
    errors: Object.keys(stats.errors).map(type => ({
      type,
      count: stats.errors[type].count,
      percentage: (stats.errors[type].count / stats.totalRequests * 100).toFixed(2) + '%',
      samples: Array.from(stats.errors[type].messages).slice(0, 3) // 只显示前3个错误样本
    }))
  };
}

/**
 * 打印结果
 */
function printResults() {
  console.log('\n' + '='.repeat(80));
  console.log('高并发测试结果报告');
  console.log('='.repeat(80));

  const results = calculateStats();

  console.log(`📊 基本统计:`);
  console.log(`   总请求数: ${results.totalRequests}`);
  console.log(`   成功率: ${results.successRate}`);
  console.log(`   错误率: ${results.errorRate}`);
  console.log(`   超时率: ${results.timeoutRate}`);
  console.log(`   测试时长: ${results.testDuration}`);
  console.log(`   QPS: ${results.requestsPerSecond}`);

  console.log(`\n⏱️  响应时间:`);
  console.log(`   平均响应时间: ${results.avgResponseTime}`);
  console.log(`   最小响应时间: ${results.minResponseTime}`);
  console.log(`   最大响应时间: ${results.maxResponseTime}`);
  console.log(`   P50 (中位数): ${results.p50ResponseTime}`);
  console.log(`   P95: ${results.p95ResponseTime}`);
  console.log(`   P99: ${results.p99ResponseTime}`);

  if (results.errors && results.errors.length > 0) {
    console.log(`\n❌ 错误详情:`);
    results.errors.forEach(error => {
      console.log(`   ${error.type}: ${error.count}次 (${error.percentage})`);
      if (error.samples.length > 0) {
        console.log(`     样本: ${error.samples.join(', ')}`);
      }
    });
  }

  console.log('\n' + '='.repeat(80));
}

/**
 * 检查服务器是否可用
 */
function checkServerHealth() {
  return new Promise((resolve) => {
    const options = {
      hostname: CONFIG.host,
      port: CONFIG.port,
      path: '/api/urls/statistics',
      method: 'GET',
      timeout: 5000
    };

    const req = http.request(options, (res) => {
      resolve(res.statusCode === 200);
    });

    req.on('timeout', () => {
      req.destroy();
      resolve(false);
    });

    req.on('error', () => {
      resolve(false);
    });

    req.end();
  });
}

/**
 * 主测试函数
 */
async function runLoadTest() {
  console.log('🚀 开始高并发测试...');
  console.log(`配置: ${CONFIG.concurrentUsers} 并发用户, 每用户 ${CONFIG.requestsPerUser} 请求`);

  // 检查服务器健康状态
  console.log('检查服务器状态...');
  const isHealthy = await checkServerHealth();
  if (!isHealthy) {
    console.error('❌ 服务器不可用，请确保后端服务正在运行在 http://localhost:8080');
    process.exit(1);
  }
  console.log('✅ 服务器状态正常');

  stats.startTime = performance.now();

  // 创建并发用户
  const userPromises = [];
  for (let i = 0; i < CONFIG.concurrentUsers; i++) {
    // 渐进式增加并发，避免一次性冲击
    const delay = (i / CONFIG.concurrentUsers) * CONFIG.rampUpTimeMs;

    userPromises.push(
      sleep(delay).then(() => simulateUser(i + 1))
    );
  }

  // 等待所有用户完成测试
  await Promise.all(userPromises);

  stats.endTime = performance.now();

  // 打印结果
  printResults();
}

// 处理进程退出
process.on('SIGINT', () => {
  console.log('\n测试被中断，正在生成报告...');
  stats.endTime = performance.now();
  printResults();
  process.exit(0);
});

// 启动测试
if (require.main === module) {
  runLoadTest().catch(console.error);
}

module.exports = { runLoadTest, CONFIG };