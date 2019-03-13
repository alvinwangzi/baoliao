package com.baoliao.weixin.service;

import com.baoliao.weixin.bean.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {
    List<User> goIndex() throws Exception;

    int updateUserInfo(HttpServletRequest request, String code) throws Exception;

    User queryMyInfo(HttpServletRequest request) throws Exception;
}
