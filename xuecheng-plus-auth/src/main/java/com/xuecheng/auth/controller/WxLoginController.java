package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * packageName com.xuecheng.auth.controller
 *
 * @author Q
 * @version JDK 8
 * @className WxLoginController
 * @date 2024/4/5 20:15
 * @description 微信登录接口
 */
@Controller
public class WxLoginController {

    @Autowired
    private WxAuthServiceImpl wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        XcUser xcUser = wxAuthService.wxAuth(code);
        if (xcUser == null){
            //重定向到错误页面
             return "redirect:http://www.xuecheng-plus.com/error.html";
        }
        //重定向到登录
        String username = xcUser.getUsername();
        return "redirect:http://www.xuecheng-plus.com/sign.html?username="+username+"&authType=wx";
    }
}
