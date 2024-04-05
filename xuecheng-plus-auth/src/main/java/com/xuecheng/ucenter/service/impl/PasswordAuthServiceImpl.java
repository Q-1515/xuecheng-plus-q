package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName com.xuecheng.ucenter.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className PasswordAuthServiceImpl
 * @date 2024/4/5 16:58
 * @description 账号密码认证接口实现
 */
@Slf4j
@Service("password_authService")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CheckCodeClient checkCodeClient;

    /***
     * @description 账号密码认证
     * @param authParamsDto 账号密码信息
     * @return XcUserExt
     * @author Q
     * @date 2024/4/5 17:01
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey)){
            log.info("验证码为空:{}", authParamsDto);
            throw new RuntimeException("验证码为空");
        }
        //校验验证码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (verify == null ||!verify){
            log.info("验证码错误:{}", authParamsDto);
            throw new RuntimeException("验证码错误");
        }

        //查询用户
        String username = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(userLambdaQueryWrapper);
        //账号不存在
        if (xcUser == null) {
            log.error("登录失败，账号{}：不存在", username);
            throw new RuntimeException("登录失败,账号密码错误");
        }
        //获取用户密码
        String password = xcUser.getPassword();
        String loginPassword = authParamsDto.getPassword();
        //校验密码
        boolean matches = passwordEncoder.matches(loginPassword, password);
        if (!matches){
            throw new RuntimeException("登录失败,账号密码错误");
        }
        //获取用户权限
        List<String> authorities = new ArrayList<>();
        authorities.add("p1");
        xcUser.setPassword(null);
        //封装数据
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setPermissions(authorities);
        return xcUserExt;
    }
}
