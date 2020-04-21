package com.atguigu.gmall.cart.interceptor;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.core.bean.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKeyName());

        if (StringUtils.isBlank(userKey)){
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,jwtProperties.getUserKeyName(),userKey,jwtProperties.getExpireTime());
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);

        if (StringUtils.isEmpty(token)) {
            // 把userInfo传递给后续的业务。TODO
            THREAD_LOCAL.set(userInfo);
            return true;
        }
        try {
            Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            Long id = Long.valueOf(infoFromToken.get("id").toString());
            userInfo.setUserId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 把userInfo传递给后续的业务。TODO
        THREAD_LOCAL.set(userInfo);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }

    public static UserInfo userInfo(){
        return THREAD_LOCAL.get();
    }
}
