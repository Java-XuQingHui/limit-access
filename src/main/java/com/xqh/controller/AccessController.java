package com.xqh.controller;

import com.xqh.annotation.AccessLimit;
import com.xqh.entity.Result;
import com.xqh.entity.StatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>测试类</p>
 *
 * @author xuqinghui
 * @create 2021/3/9 15:49
 */
@RestController
@RequestMapping("/access")
public class AccessController {


    @AccessLimit(seconds = 5, maxCount = 2, needLogin = true)//访问控制，5秒内只能访问2次
    @GetMapping
    public Result access() {
        return new Result(true, StatusCode.OK, "访问成功！");
    }

}
