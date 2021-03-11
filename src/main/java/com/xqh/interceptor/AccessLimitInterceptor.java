package com.xqh.interceptor;

import com.alibaba.fastjson.JSON;
import com.xqh.annotation.AccessLimit;
import com.xqh.entity.Result;
import com.xqh.entity.StatusCode;
import com.xqh.utils.ApplicationContextUtils;
import com.xqh.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 访问控制拦截器
 */

@Component
public class AccessLimitInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    RedisUtil redisUtil;

    // 解决@Component注解下@Autowired的类为null
    // 第一种方式：声明一个此类的静态变量，用以保存bean
    public static AccessLimitInterceptor accessLimitInterceptor;


    // 将需要注入的类添加到静态变量中
    @PostConstruct
    public void init() {
        accessLimitInterceptor = this;
        accessLimitInterceptor.redisUtil = this.redisUtil;
    }

    // 模拟数据存储，实际业务中可以自定义实现方式
    private static Map<String, AccessInfo> accessInfoMap = new HashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 第二种方式：编写工具类实现ApplicationContextAware接口，重写setApplicationContext方法
        RedisUtil redisUtil = (RedisUtil) ApplicationContextUtils.getBean("redisUtil");

        //判断请求是否属于方法的请求
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;

            //获取方法中的注解，看是否有该注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }

            // 限制周期(单位：秒)
            int seconds = accessLimit.seconds();
            //规定周期内限制次数
            int maxCount = accessLimit.maxCount();
            // 是否需要登录
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            //如果需要登录
            if (needLogin) {
                //获取登录的session进行判断
                //……
                key += "-userA";//这里假设用户是userA，实际项目中可以改为userId
            }

            //模拟从redis中获取数据,暂时存储进入内存
//            AccessInfo accessInfo = accessInfoMap.get(key);
//            if (accessInfo == null) {
//                //第一次访问
//                accessInfo = new AccessInfo();
//                accessInfo.setFirstVisitTimestamp(System.currentTimeMillis());
//                accessInfo.setAccessCount(1);
//                accessInfoMap.put(key, accessInfo);
//            } else if (accessInfo.getAccessCount() < maxCount) {
//                //访问次数加1
//                accessInfo.setAccessCount(accessInfo.getAccessCount() + 1);
//                accessInfoMap.put(key, accessInfo);
//            } else {
//                //超出访问次数，判断时间是否超出设定时间
//                if ((System.currentTimeMillis() - accessInfo.getFirstVisitTimestamp()) <= seconds * 1000) {
//                    //如果还在设定时间内，则为不合法请求，返回错误信息
//                    render(response, "达到访问限制次数，请稍后重试！");
//                    return false;
//                } else {
//                    //如果超出设定时间，则为合理的请求，将之前的请求清空，重新计数
//                    accessInfo.setFirstVisitTimestamp(System.currentTimeMillis());
//                    accessInfo.setAccessCount(1);
//                    accessInfoMap.put(key, accessInfo);
//                }
//            }

            //模拟从redis中获取数据
            // 已访问次数
            Object o = redisUtil.get(key);
            if(Objects.isNull(o)) {
                // 第一次访问
                redisUtil.incr(key, 1);
                redisUtil.expire(key, seconds);
            } else {
                // 获取单位时间内已访问次数
                Integer count = Integer.valueOf(redisUtil.get(key).toString());
                if(maxCount > count) {
                    // 没超出访问限制
                    redisUtil.incr(key, 1);
                } else {
                    //如果还在设定时间内，则为不合法请求，返回错误信息
                    render(response, "达到访问限制次数，请稍后重试！");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 向页面发送消息
     *
     * @param response
     * @param msg
     * @throws Exception
     */
    private void render(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(new Result(true, StatusCode.ACCESSERROR, msg));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    /**
     * 封装的访问信息对象
     */
    class AccessInfo {

        /**
         * 一个计数周期内第一次访问的时间戳
         */
        private long firstVisitTimestamp;
        /**
         * 访问次数统计
         */
        private int accessCount;

        public long getFirstVisitTimestamp() {
            return firstVisitTimestamp;
        }

        public void setFirstVisitTimestamp(long firstVisitTimestamp) {
            this.firstVisitTimestamp = firstVisitTimestamp;
        }

        public int getAccessCount() {
            return accessCount;
        }

        public void setAccessCount(int accessCount) {
            this.accessCount = accessCount;
        }

        @Override
        public String toString() {
            return "AccessInfo{" +
                    "firstVisitTimestamp=" + firstVisitTimestamp +
                    ", accessCount=" + accessCount +
                    '}';
        }


    }


}
