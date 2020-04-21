package com.atguigu.gmall.cart.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "jwt.token")
public class JwtProperties {
    private String pubKeyPath;// 公钥

    private PublicKey publicKey; // 公钥

    private String userKeyName;

    private String cookieName;

    private Integer expireTime;

    @PostConstruct
    public void init(){
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
