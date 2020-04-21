package com.atguigu.gmall.auth.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.client.GmallUmsClient;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private GmallUmsClient gmallUmsClient;

    @Autowired
    private JwtProperties jwtProperties;

    public String authentication(String username, String password) {
        try {
            Resp<MemberEntity> memberEntityResp = gmallUmsClient.queryUser(username, password);
            MemberEntity memberEntity = memberEntityResp.getData();
            if (memberEntity == null){
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", memberEntity.getId());
            map.put("username", memberEntity.getUsername());
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpireTime());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
