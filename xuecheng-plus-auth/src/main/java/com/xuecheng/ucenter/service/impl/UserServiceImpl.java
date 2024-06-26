package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName com.xuecheng.ucenter.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className UserServiceImpl
 * @date 2024/4/5 13:15
 * @description 用户登录接口
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private XcMenuMapper xcMenuMapper;


    /***
     * @description 用户登录校验
     * @param s 用户认证信息json（username | wxcode | ，，，，type类型）
     * @return UserDetails
     * @author Q
     * @date 2024/4/5 13:16
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        String authType = authParamsDto.getAuthType();
        if (StringUtils.isBlank(authType)) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        AuthService authService = applicationContext.getBean(authType + "_authService", AuthService.class);
        XcUserExt execute = authService.execute(authParamsDto);
        return getUserPrincipal(execute);
    }


    /***
     * @description 封装UserDetails返回
     * @param user 用户信息
     * @return UserDetails
     * @author Q
     * @date 2024/4/5 17:29
     */
    public UserDetails getUserPrincipal(XcUserExt user) {
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());
        ArrayList<String> authoritieList = new ArrayList<>();
        String[] authorities = {"test"};
        xcMenus.forEach(xcMenu -> {
            authoritieList.add(xcMenu.getCode());
        });
        if (!authoritieList.isEmpty()) {
            authorities = authoritieList.toArray(new String[0]);
        }
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        return User.withUsername(userString).password("").authorities(authorities).build();
    }
}
