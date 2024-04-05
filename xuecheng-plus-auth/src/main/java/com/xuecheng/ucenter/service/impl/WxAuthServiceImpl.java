package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * packageName com.xuecheng.ucenter.service.impl
 *
 * @author Q
 * @version JDK 8
 * @className WxAuthServiceImpl
 * @date 2024/4/5 20:20
 * @description 微信认证
 */
@Slf4j
@Service("wx_authService")
public class WxAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private XcUserRoleMapper xcUserRoleMapper;

    @Autowired
    private WxAuthServiceImpl wxAuthService;

    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;


    public XcUser wxAuth(String code){
        //获取access_token
        Map<String, String> accessToken = getAccess_token(code);
        //获取用户信息
        String openid = accessToken.get("openid");
        String access_token = accessToken.get("access_token");

        Map<String, String> userinfo = getUserinfo(access_token, openid);
        //添加用户到数据库
        return wxAuthService.addWxUser(userinfo);
    }


    /***
     * @description 微信认证
     * @param authParamsDto 用户信息
     * @return XcUserExt
     * @author Q
     * @date 2024/4/5 20:21
    */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        LambdaQueryWrapper<XcUser> xcUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        xcUserLambdaQueryWrapper.eq(XcUser::getUsername,authParamsDto.getUsername());
        XcUser xcUser = xcUserMapper.selectOne(xcUserLambdaQueryWrapper);
        if (xcUser == null){
            throw new RuntimeException("用户不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }


    /***
     * @description 获取用户token
     * @param code 调用code
     * @return Map<String>
     * @author Q
     * @date 2024/4/5 21:47
    */
    private Map<String, String> getAccess_token(String code) {
        // 1. 请求路径模板，参数用%s占位符
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        // 2. 填充占位符：appid，secret，code
        String url = String.format(url_template, appid, secret, code);
        // 3. 远程调用URL，POST方式（详情参阅官方文档）
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        // 4. 获取相应结果，响应结果为json格式
        String result = exchange.getBody();
        // 5. 转为map
        return JSON.parseObject(result, Map.class);
    }


    /***
     * @description 获取微信用户数据
     * @param access_token 微信用户token
     * @param openid 微信用户id
     * @return Map<String>
     * @author Q
     * @date 2024/4/5 21:49
    */
    private Map<String,String> getUserinfo(String access_token,String openid) {

        String wxUrl_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, access_token,openid);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    /***
     * @description 保存用户信息
     * @param userInfo_map 微信用户信息
     * @return XcUser 系统用户信息
     * @author Q
     * @date 2024/4/5 21:57
    */
    @Transactional
    public XcUser addWxUser(Map userInfo_map){
        String unionid = userInfo_map.get("unionid").toString();
        //根据unionid查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if(xcUser!=null){
            return xcUser;
        }
        String userId = UUID.randomUUID().toString();
        xcUser = new XcUser();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionid);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfo_map.get("nickname").toString());
        xcUser.setUserpic(userInfo_map.get("headimgurl").toString());
        xcUser.setName(userInfo_map.get("nickname").toString());
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }
}
