<%--
  Created by IntelliJ IDEA.
  User: CCQ
  Date: 2019-3-17
  Time: 22:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>提现明细</title>
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <style type="text/css">
        body {
            margin: 8px;
            padding: 8px;
        }

        .user_icon_e.ab {
            width: 26px;
            height: 26px;
            float: right;
        }

        .yj {
            border-radius: 50%;
            -moz-border-radius: 50%;
            -webkit-border-radius: 50%
        }

        .cz {
            vertical-align: middle !important
        }
    </style>
</head>
<body>
<c:forEach var="trade" items="${sessionScope.depositList}">
    <div style="border-bottom: 1px solid #F4F4F4;margin-top: 10px;">
        <div style="margin-bottom: 6px">
            <span style="font-size: 10px;color: #BCBCBC;"><fmt:formatDate value="${trade.createTime}"
                                                                          pattern="yyyy-MM-dd HH:mm:ss"/></span>
                <%--<span style="float:right;font-size: 10px;color: #BCBCBC;">寻梦依然</span>--%>
                <%--<img class='user_icon_e yj cz ab' src="http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eqfAA1AJAgRCFthEdAvqzMSut19A09ibzBVv5lkjdia643BGmXrLKeZZJ5sXptUyjrHyILcJHcax58A/132">--%>
                <%-- <span style="float:right;font-size: 10px;color: #BCBCBC;">${sessionScope.user.nickName}</span>
                 <img class='user_icon_e yj cz ab' src="${sessionScope.user.headImgUrl}" alt="">--%>
        </div>
        <div>
            <span style="color: #EBC49D;padding-right: 4px;">提现</span>
            <span style="float: right;font-size: 13px;">
                <span style="color: #EBC49D;">
                    <%--<c:out value="${sessionScope.user.openId}"></c:out>
                    <c:out value="${trade.buyerOpenId}"></c:out>--%>
                            - ${trade.money}
                            <span style="color: grey">元</span>
                </span>
            </span>
        </div>
    </div>
</c:forEach>
<div>
    <p style="text-align: center;color: #797979;font-size: 15px;">亲，没有更多数据了--！</p>
</div>
</body>
</html>
