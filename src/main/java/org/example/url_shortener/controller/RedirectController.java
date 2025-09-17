package org.example.url_shortener.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import org.example.url_shortener.service.UrlShortenerService;

/**
 * 短链接重定向控制器
 *
 * 这个Controller专门处理短链接的重定向功能
 * 与UrlShortenerController不同，这个Controller处理根路径的请求
 *
 * 为什么需要单独的Controller？
 * 1. 短链接通常直接访问，如 http://domain.com/abc123
 * 2. 不应该有 /api 前缀，因为这不是API调用而是重定向
 * 3. 分离关注点 - API管理和重定向是不同的功能
 *
 * 注意使用@Controller而不是@RestController：
 * @Controller 用于返回视图或重定向
 * @RestController 用于返回JSON数据
 */
@Controller  // 使用@Controller而不是@RestController，因为我们要返回重定向而不是JSON
public class RedirectController {

    /**
     * URL短链服务
     * 注入业务逻辑服务来处理短码查找和点击统计
     */
    @Autowired
    private UrlShortenerService urlShortenerService;

    /**
     * 短链接重定向接口
     *
     * HTTP GET /{shortCode}
     *
     * 这个接口处理所有形如 http://domain.com/abc123 的请求
     * 根据短码查找原始URL并进行重定向
     *
     * 工作流程：
     * 1. 接收短码参数
     * 2. 调用Service查找原始URL
     * 3. 增加点击统计
     * 4. 返回重定向响应
     *
     * @param shortCode 从URL路径中提取的短码
     * @return RedirectView对象，浏览器会自动跳转到目标URL
     */
    @GetMapping("/{shortCode}")  // 映射根路径下的短码访问
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {

        // 参数验证 - 确保短码不为空
        if (shortCode == null || shortCode.trim().isEmpty()) {
            // 如果短码为空，重定向到错误页面
            return new RedirectView("/error/invalid-url");
        }

        // 调用Service层获取原始URL并增加点击统计
        // 这个方法会自动处理：
        // 1. 短码查找
        // 2. 过期检查
        // 3. 点击次数增加
        String originalUrl = urlShortenerService.getOriginalUrlAndIncrementClick(shortCode);

        if (originalUrl != null) {
            // 找到了原始URL，创建重定向响应

            // 确保URL有协议前缀
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "http://" + originalUrl;
            }

            // 创建重定向视图
            // RedirectView会自动设置HTTP 302状态码
            // 浏览器会自动跳转到目标URL
            RedirectView redirectView = new RedirectView(originalUrl);

            // 设置为永久重定向（可选）
            // redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY); // 301重定向

            return redirectView;

        } else {
            // 未找到短码对应的URL，可能的原因：
            // 1. 短码不存在
            // 2. 短码已过期
            // 3. 短码被删除

            // 重定向到自定义错误页面
            // 在实际项目中，可以创建一个友好的404页面
            return new RedirectView("/error/not-found");
        }
    }

    /**
     * 处理根路径访问
     *
     * HTTP GET /
     *
     * 当用户直接访问网站首页时的处理
     * 可以重定向到管理页面、帮助页面或者API文档
     *
     * @return 重定向到首页或管理页面
     */
    @GetMapping("/")
    public RedirectView home() {
        // 可以重定向到：
        // 1. 静态首页
        // 2. API文档页面
        // 3. 管理界面
        // 4. 帮助页面

        // 这里重定向到一个简单的欢迎页面
        // 实际项目中可以改为具体的页面
        return new RedirectView("/welcome.html");
    }

    /**
     * 错误页面处理 - URL不存在
     *
     * HTTP GET /error/not-found
     *
     * 当短码不存在或已过期时显示的错误页面
     *
     * @return 重定向到错误页面或返回错误信息
     */
    @GetMapping("/error/not-found")
    public RedirectView notFound() {
        // 可以：
        // 1. 返回自定义的404页面
        // 2. 重定向到首页
        // 3. 显示错误信息页面

        // 这里简单地重定向到一个通用的错误页面
        return new RedirectView("/404.html");
    }

    /**
     * 错误页面处理 - 无效URL
     *
     * HTTP GET /error/invalid-url
     *
     * 当短码格式无效时显示的错误页面
     *
     * @return 重定向到错误页面
     */
    @GetMapping("/error/invalid-url")
    public RedirectView invalidUrl() {
        // 重定向到无效URL错误页面
        return new RedirectView("/400.html");
    }
}