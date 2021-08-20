package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.Goods;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huhao
 * @since 2021-08-19
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, Goods goods);

}
