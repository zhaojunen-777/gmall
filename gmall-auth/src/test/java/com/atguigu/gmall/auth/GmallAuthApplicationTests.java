package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class GmallAuthApplicationTests {

    private static final String pubKeyPath = "D:\\git\\gmall\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\git\\gmall\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1ODY0MDU0ODR9.bUyd-TV7pdndiJIKHLr4vx1IcqXtTvr9zHnifB2IbBxSWGMQikReO2OZQJYa00czkx33VJwm8_aRXeHFKtpq84TCpNPN42mGxw90oTgHCozP5nx61xsQA6z5_w6FpwJpdHVjYpzYViR_t5SZD8_D3hvXdUGMS6UIy7rMO6Y-zeNy6Y41u6efKiP60fcMGe1PjJ2aO1EF-q6BxWlgXtxX-nIz2HltDR3SZ9o4JKVYikHU8MMxNKDPenRhx7H9ha8pqn4G7YhxhOZ6d-17TTgf5GLX7lipA2kjN91_yld9XhPgXRkEQkeVA89eaFy-kY8EMtXkACCNSNFJ3Cv-2C1eIA\n";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
