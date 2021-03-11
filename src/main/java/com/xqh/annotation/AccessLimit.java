package com.xqh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>访问控制注解(实现接口防刷功能)</p>
 *
 * @author xuqinghui
 * @create 2021/3/9 15:26
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {

    // 限制周期(单位：秒)
    int seconds();

    //规定周期内限制次数
    int maxCount();

    // 是否需要登录
    boolean needLogin() default false;

}
