package com.liubin.access.Controller;

import com.liubin.access.service.AccessLimit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liubin
 * @date 2021/7/7
 */
@RestController
@RequestMapping("/access")
public class AccessController {

    @ResponseBody
    @RequestMapping("/accessLimit")
    @AccessLimit(maxCount = 5 , second = 10)
    public String accessLimit(){
        return "hello world";
    }
}
