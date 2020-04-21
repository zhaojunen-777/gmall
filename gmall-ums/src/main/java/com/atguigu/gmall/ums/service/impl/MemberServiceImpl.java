package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.exception.UmsException;
import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        switch (type){
            case 1:
                queryWrapper.eq("username", data);
                break;
            case 2:
                queryWrapper.eq("mobile", data);
                break;
            case 3:
                queryWrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return this.count(queryWrapper) == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {

        String cacheCode = stringRedisTemplate.opsForValue().get(memberEntity.getMobile());
        if (!StringUtils.equals(code,cacheCode)){
            throw new UmsException("验证码错误");
        }
        // 生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        memberEntity.setSalt(salt);

        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword()+salt));
        memberEntity.setCreateTime(new Date());
        memberEntity.setLevelId(1L);
        memberEntity.setSourceType(1);
        memberEntity.setIntegration(1000);
        memberEntity.setGrowth(1000);
        memberEntity.setStatus(1);
        this.save(memberEntity);

        stringRedisTemplate.delete(memberEntity.getMobile());
    }

    @Override
    public MemberEntity queryUser(String username, String password) {
        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        MemberEntity memberEntity = this.getOne(queryWrapper);
        if (memberEntity == null){
            throw new UmsException("用户名不存在");
        }
        String entityPassword = memberEntity.getPassword();
        String salt = memberEntity.getSalt();
        String dlpassword = DigestUtils.md5Hex(password + salt);
        if (!StringUtils.equals(entityPassword,dlpassword)){
            throw new UmsException("密码不正确");
        }
        return memberEntity;
    }

}