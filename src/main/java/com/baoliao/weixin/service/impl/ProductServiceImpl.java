package com.baoliao.weixin.service.impl;

import com.baoliao.weixin.Constants;
import com.baoliao.weixin.bean.Product;
import com.baoliao.weixin.bean.User;
import com.baoliao.weixin.dao.ProductDao;
import com.baoliao.weixin.service.ProductService;
import com.baoliao.weixin.util.Utils;
import com.baoliao.weixin.util.WeixinIntefaceUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019\3\4 0004.
 */
@Service
public class ProductServiceImpl implements ProductService {
    @Value("${domain_name}")
    private String domainName;
    private Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    ProductDao productDao;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmsssss");

    @Override
    public String saveProduct(HttpServletRequest request, Product vo) {
        String code = vo.getCode();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String openId = "dev";
        if (user != null) {
            openId = user.getOpenId();
        } else {
            String oauth2_token_url = Constants.URL.OAUTH2_ACCESS_TOKEN.replace("APPID", Constants.WECHAT_PARAMETER.APPID).replace("SECRET", Constants.WECHAT_PARAMETER.APPSECRET).replace("CODE", code);
            JSONObject jsonObject = WeixinIntefaceUtil.httpRequest(oauth2_token_url, "GET", null);
            openId = jsonObject.getString("openid");
            String access_token = jsonObject.getString("access_token");
            String userinfourl = Constants.URL.OAUTH2_USERINFO_URL.replace("ACCESS_TOKEN", access_token).replace("OPENID", openId);
            jsonObject = WeixinIntefaceUtil.httpRequest(userinfourl, "GET", null);
            String nickName = jsonObject.getString("nickname");
            String sex = jsonObject.getString("sex");
            String language = jsonObject.getString("language");
            String city = jsonObject.getString("city");
            String province = jsonObject.getString("province");
            String country = jsonObject.getString("country");
            String headImgUrl = jsonObject.getString("headimgurl");

            user = new User();
            user.setOpenId(openId);
            user.setNickName(nickName);
            user.setSex(Integer.parseInt(sex));
            user.setLanguage(language);
            user.setCity(city);
            user.setProvince(province);
            user.setCountry(country);
            user.setHeadimgUrl(headImgUrl);
            // 由于code只能使用一次，所以将用户信息存入session
            session.setAttribute("user", user);
        }
        Map<String, Object> model = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(openId)) {
            vo.setOpenId(openId);
        }

        int i = productDao.saveProduct(vo);
        if (i > 0) {
            model.put("result", i);
            model.put("msg", "成功");
        } else {
            model.put("result", i);
            model.put("msg", "保存数据出现异常:" + i);
        }
        log.info("读取的域名配置是:" + domainName);
        File file = null;
        try {
            file = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String path = file.getParentFile().getParentFile() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "QRCodeImg" + File.separator;
        String logoPath = file.getParentFile().getParentFile() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "logo.png";
        int fileName = Utils.zxingCodeCreate(domainName + "/product/detailInfo?id=" + vo.getId() + "&price=" + vo.getPrice(), path, 250, logoPath);
        log.info("生成的二维码名称:" + fileName);
        session.setAttribute("fileName", fileName + ".jpg");
        session.setAttribute("product", vo);
        JSONObject jObject = JSONObject.fromObject(model);
        return jObject.toString();
    }

    @Override
    public String uploadImgByBase64(HttpServletRequest request, String imgData, String format) throws FileNotFoundException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String currDateStr = sdf.format(new Date());
        resultMap.put("success", true);
        resultMap.put("data", currDateStr);
        File file = new File(ResourceUtils.getURL("classpath:").getPath());
        String path = file.getParentFile().getParentFile() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "userImg" + File.separator;
        Utils.GenerateImage(imgData, currDateStr, path, format);
        JSONObject jObject = JSONObject.fromObject(resultMap);
        return jObject.toString();
    }
}
