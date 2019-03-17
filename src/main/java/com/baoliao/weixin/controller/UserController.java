package com.baoliao.weixin.controller;

import com.baoliao.weixin.service.ProductService;
import com.baoliao.weixin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired

    private ProductService productService;

    /**
     * 初始化用户信息，返回index.jsp
     *
     * @param request
     * @return
     */
    @GetMapping("/goIndex")
    public String goIndex(HttpServletRequest request, HttpServletResponse response) {
        log.info("进入首页");
        String code = request.getParameter("code");
        try {
            // TODO
            // 必须先关注才能进入首页，所以此处只需要更新就行了
            if (true) {
//                return "not_subscribe";
            }
            userService.updateUserInfo(request, code);
        } catch (Exception e) {
            log.error("进入首页出错,错误信息;" + e);
        }
        return "index";
    }

    @GetMapping("/queryMyCode")
    public String queryMyCode(HttpServletRequest request) {
        log.info("进入我的料码页面");
        try {
            productService.getBuyProductList(request);
            log.info("查询我买的料成功");
            productService.getSellerProductList(request);
            log.info("查询我卖的料成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("进入我的料码页面出错。");
        }
        return "myCode";
    }

    @GetMapping("/queryMyInfo")
    public String queryMyInfo(HttpServletRequest request) {
        log.info("进入我的页面");
        try {
            userService.queryMyInfo(request);
        } catch (Exception e) {
            log.error("进入我页面出错,错误信息;" + e);
        }
        return "myInfo";
    }
}
