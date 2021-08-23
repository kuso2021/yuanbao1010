package com.xxxx.seckill.controller;


import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
* @Auther:huhao
* @Date:2021/8/18-18:16
* @Description:com.xxxx.generator
* @version:1.0
*/
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }

    @ResponseBody
    @RequestMapping("/mq")
    public void mq(){
        mqSender.send("hello");
    }

    @ResponseBody
    @RequestMapping("/mq/fanout")
    public void mq01(){
        mqSender.send("hello");
    }

    @ResponseBody
    @RequestMapping("/mq/direct01")
    public void mq02(){
        mqSender.send01("hello,red");
    }

    @ResponseBody
    @RequestMapping("/mq/direct02")
    public void mq03(){
        mqSender.send02("hello,green");
    }

    @ResponseBody
    @RequestMapping("/mq/topic01")
    public void mq04(){
        mqSender.send03("hello,red");
    }

    @ResponseBody
    @RequestMapping("/mq/topic02")
    public void mq05(){
        mqSender.send02("hello,green");
    }

    @ResponseBody
    @RequestMapping("/mq/header01")
    public void mq06(){
        mqSender.send05("hello,header01");
    }

    @ResponseBody
    @RequestMapping("/mq/header02")
    public void mq07(){
        mqSender.send06("hello,header02");
    }
}
