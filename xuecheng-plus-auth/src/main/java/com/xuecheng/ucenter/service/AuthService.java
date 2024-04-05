package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * packageName com.xuecheng.ucenter.service
 *
 * @author Q
 * @version JDK 8
 * @className AuthService
 * @date 2024/4/5 16:55
 * @description 认证接口
 */
public interface AuthService {

    /***
     * @description 认证方法
     * @param authParamsDto 用户请求信息
     * @return XcUserExt
     * @author Q
     * @date 2024/4/5 16:57
    */
    public XcUserExt execute(AuthParamsDto authParamsDto);
}
