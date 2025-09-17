package org.example.url_shortener.controller;

// Spring Web相关导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

// Java标准库导入
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 项目内部导入
import org.example.url_shortener.dto.ShortenUrlRequest;
import org.example.url_shortener.dto.ShortenUrlResponse;
import org.example.url_shortener.entity.UrlMapping;
import org.example.url_shortener.service.UrlShortenerService;

/**
 * URL短链服务控制器
 *
 * Controller是MVC架构中的控制层，负责：
 * 1. 接收HTTP请求
 * 2. 调用Service层处理业务逻辑
 * 3. 返回HTTP响应
 *
 * 关键注解说明：
 * @RestController - 组合了@Controller和@ResponseBody，返回JSON数据
 * @RequestMapping - 定义基础URL路径
 * @CrossOrigin - 允许跨域请求（前端开发时需要）
 */
@RestController  // 标记为REST控制器，自动序列化返回值为JSON
@RequestMapping("/api/urls")  // 基础路径，所有接口都以/api/urls开头
@CrossOrigin(origins = "*")  // 允许所有域名的跨域请求（生产环境应该限制）
public class UrlShortenerController {

    // ==================== 依赖注入 ====================

    /**
     * 业务逻辑服务
     * Spring会自动注入UrlShortenerService的实现类
     */
    @Autowired
    private UrlShortenerService urlShortenerService;

    /**
     * 系统基础URL配置
     * 从application.properties文件中读取app.base-url配置项
     * 如果没有配置，默认使用http://localhost:8080
     *
     * @Value注解用于注入配置文件中的值
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ==================== REST API接口 ====================

    /**
     * 创建短链接的API接口
     *
     * HTTP POST /api/urls/shorten
     * Content-Type: application/json
     *
     * 请求体示例：
     * {
     *   "url": "https://www.google.com",
     *   "expiresAt": "2024-12-31T23:59:59"  // 可选
     * }
     *
     * 响应示例：
     * {
     *   "shortCode": "abc123",
     *   "shortUrl": "http://localhost:8080/abc123",
     *   "originalUrl": "https://www.google.com",
     *   "createdAt": "2024-01-01T10:00:00",
     *   "expiresAt": "2024-12-31T23:59:59",
     *   "clickCount": 0
     * }
     *
     * @param request 缩短URL的请求对象
     * @return 响应实体，包含创建的短链接信息或错误信息
     */
    @PostMapping("/shorten")  // 映射POST请求到/api/urls/shorten
    public ResponseEntity<ShortenUrlResponse> shortenUrl(@RequestBody ShortenUrlRequest request) {
        try {
            // 步骤1: 验证请求数据
            if (request == null || !request.isValid()) {
                // 返回400 Bad Request错误
                return ResponseEntity.badRequest().build();
            }

            // 步骤2: 调用Service层处理业务逻辑
            UrlMapping urlMapping = urlShortenerService.shortenUrl(
                request.getUrl(),
                request.getExpiresAt()
            );

            // 步骤3: 将实体转换为响应DTO
            ShortenUrlResponse response = new ShortenUrlResponse(urlMapping, baseUrl);

            // 步骤4: 返回成功响应（200 OK）
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // URL格式错误，返回400 Bad Request
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            // 其他系统错误，返回500 Internal Server Error
            // 在生产环境中，应该记录错误日志
            System.err.println("创建短链接时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 短链接重定向接口
     *
     * HTTP GET /{shortCode}
     *
     * 注意：这个接口需要放在根路径下，不能在/api/urls路径下
     * 因为短链接通常直接访问，例如：http://localhost:8080/abc123
     * 所以我们需要创建一个单独的Controller来处理重定向
     * 这里先保留这个方法作为参考，实际使用时需要移到根路径Controller
     *
     * @param shortCode URL路径中的短码参数
     * @return 重定向响应，跳转到原始URL
     */
    @GetMapping("/{shortCode}")  // 这个路径实际是 /api/urls/{shortCode}，不是我们想要的
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {
        // 调用Service层获取原始URL并增加点击统计
        String originalUrl = urlShortenerService.getOriginalUrlAndIncrementClick(shortCode);

        if (originalUrl != null) {
            // 找到原始URL，创建重定向响应
            // RedirectView会自动设置HTTP 302重定向状态码
            return new RedirectView(originalUrl);
        } else {
            // 未找到或已过期，重定向到错误页面
            // 在实际项目中，可以重定向到自定义的404页面
            return new RedirectView("/error/not-found");
        }
    }

    /**
     * 获取短链接详细信息的API接口
     *
     * HTTP GET /api/urls/{shortCode}/info
     *
     * 响应示例：
     * {
     *   "shortCode": "abc123",
     *   "shortUrl": "http://localhost:8080/abc123",
     *   "originalUrl": "https://www.google.com",
     *   "createdAt": "2024-01-01T10:00:00",
     *   "expiresAt": "2024-12-31T23:59:59",
     *   "clickCount": 42
     * }
     *
     * @param shortCode 短码参数
     * @return 短链接的详细信息或404错误
     */
    @GetMapping("/{shortCode}/info")
    public ResponseEntity<ShortenUrlResponse> getUrlInfo(@PathVariable String shortCode) {
        Optional<UrlMapping> optionalMapping = urlShortenerService.getUrlStatistics(shortCode);

        if (optionalMapping.isPresent()) {
            UrlMapping mapping = optionalMapping.get();
            ShortenUrlResponse response = new ShortenUrlResponse(mapping, baseUrl);
            return ResponseEntity.ok(response);
        } else {
            // 未找到，返回404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取最近创建的短链接列表
     *
     * HTTP GET /api/urls/recent?limit=10
     *
     * @param limit 返回数量限制，默认10个
     * @return 最近创建的短链接列表
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ShortenUrlResponse>> getRecentUrls(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            // 参数验证
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().build();
            }

            // 获取数据并转换为响应DTO
            List<UrlMapping> recentUrls = urlShortenerService.getRecentUrls(limit);
            List<ShortenUrlResponse> responses = recentUrls.stream()
                .map(mapping -> new ShortenUrlResponse(mapping, baseUrl))
                .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            System.err.println("获取最近URL列表时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取点击次数最多的短链接列表
     *
     * HTTP GET /api/urls/top-clicked?limit=10
     *
     * @param limit 返回数量限制，默认10个
     * @return 点击次数最多的短链接列表
     */
    @GetMapping("/top-clicked")
    public ResponseEntity<List<ShortenUrlResponse>> getTopClickedUrls(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest().build();
            }

            List<UrlMapping> topUrls = urlShortenerService.getTopClickedUrls(limit);
            List<ShortenUrlResponse> responses = topUrls.stream()
                .map(mapping -> new ShortenUrlResponse(mapping, baseUrl))
                .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            System.err.println("获取热门URL列表时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取系统统计信息
     *
     * HTTP GET /api/urls/statistics
     *
     * 响应示例：
     * {
     *   "totalUrls": 1000,
     *   "totalClicks": 5000,
     *   "averageClicks": 5.0,
     *   "urlsToday": 50
     * }
     *
     * @return 系统统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<UrlShortenerService.SystemStatistics> getSystemStatistics() {
        try {
            UrlShortenerService.SystemStatistics stats = urlShortenerService.getSystemStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("获取系统统计时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 清理过期的短链接
     *
     * HTTP DELETE /api/urls/cleanup
     *
     * 这个接口可能需要管理员权限，实际项目中应该加上权限验证
     *
     * @return 清理结果，包含删除的记录数
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredUrls() {
        try {
            int deletedCount = urlShortenerService.cleanupExpiredUrls();
            return ResponseEntity.ok("成功清理 " + deletedCount + " 个过期链接");
        } catch (Exception e) {
            System.err.println("清理过期URL时发生错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("清理过程中发生错误");
        }
    }
}