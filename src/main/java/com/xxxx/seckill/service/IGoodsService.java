package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.Goods;
import com.xxxx.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huhao
 * @since 2021-08-19
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();
    GoodsVo findGoodsVoByGoodsId(long goodsId);

}
