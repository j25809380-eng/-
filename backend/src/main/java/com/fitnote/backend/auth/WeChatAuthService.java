package com.fitnote.backend.auth;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeChatAuthService {

    private static final Logger log = LoggerFactory.getLogger(WeChatAuthService.class);

    private final boolean mockLogin;
    private final String appId;
    private final String secret;
    private final RestClient restClient;

    public WeChatAuthService(@Value("${app.wechat.mock-login:false}") boolean mockLogin,
                             @Value("${app.wechat.app-id:}") String appId,
                             @Value("${app.wechat.secret:}") String secret) {
        this.mockLogin = mockLogin;
        this.appId = appId;
        this.secret = secret;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.weixin.qq.com")
            .build();
    }

    public SessionInfo resolveSession(String code) {
        if (mockLogin || isCredentialsMissing()) {
            if (isCredentialsMissing()) {
                log.warn("WECHAT_APP_ID / WECHAT_APP_SECRET not configured, using mock-login fallback. "
                    + "Set env vars WECHAT_APP_ID and WECHAT_APP_SECRET for real WeChat login.");
            }
            return new SessionInfo("wx_" + (code == null ? "mock" : code), null);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/sns/jscode2session")
                .queryParam("appid", appId)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build())
            .retrieve()
            .body(Map.class);

        if (response == null) {
            throw new IllegalArgumentException("code2Session returned empty response");
        }

        if (response.get("errcode") != null && !"0".equals(String.valueOf(response.get("errcode")))) {
            String message = String.valueOf(response.getOrDefault("errmsg", "unknown wechat error"));
            throw new IllegalArgumentException("wechat login failed: " + message);
        }

        Object openId = response.get("openid");
        if (openId == null || String.valueOf(openId).isBlank()) {
            throw new IllegalArgumentException("wechat login failed: openid missing");
        }

        Object unionId = response.get("unionid");
        return new SessionInfo(String.valueOf(openId), unionId == null ? null : String.valueOf(unionId));
    }

    public boolean isMockLogin() {
        return mockLogin || isCredentialsMissing();
    }

    private boolean isCredentialsMissing() {
        return appId == null || appId.isBlank() || secret == null || secret.isBlank();
    }

    public record SessionInfo(String openId, String unionId) {
    }
}
