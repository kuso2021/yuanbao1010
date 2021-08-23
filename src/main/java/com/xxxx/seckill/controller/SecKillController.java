package com.xxxx.seckill.controller;

import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.utils.JsonUtil;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther:huhao
 * @Date:2021/8/18-21:47
 * @Description:com.xxxx.seckill.controller
 * @version:1.0
 */
@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ISeckillOrderService seckillOrderService;
	@Autowired
	private IOrderService orderService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private MQSender mqSender;
	/*@Autowired
	private RedisScript<Long> script;*/

	private Map<Long, Boolean> EmptyStockMap = new HashMap<>();



	@RequestMapping("/doSeckill2")
	public String doSeckill2(Model model, User user, Long goodsId) {
		if (user == null) {
			return "login";
		}
		model.addAttribute("user", user);
		GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
		//判断库存
		if (goods.getStockCount() < 1) {
			model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
			return "secKillFail";
		}
		//判断是否重复抢购
		SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
		if (seckillOrder != null) {
			model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
			return "secKillFail";
		}
		Order order = orderService.seckill(user, goods);
		model.addAttribute("order", order);
		model.addAttribute("goods", goods);
		return "orderDetail";

	}

	@RequestMapping(value = "/doSeckill",method = RequestMethod.POST)
	@ResponseBody
	public RespBean doSeckill(Model model, User user, Long goodsId) {
		if (user == null) {
			return RespBean.error(RespBeanEnum.SESSION_ERROR);
		}
		ValueOperations valueOperations=redisTemplate.opsForValue();
		SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
		if (seckillOrder!=null){
			return RespBean.error(RespBeanEnum.REPEATE_ERROR);
		}
		if (EmptyStockMap.get(goodsId)){
			return RespBean.error(RespBeanEnum.EMPTY_STOCK);
		}
		Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
		if (stock<0){
			EmptyStockMap.put(goodsId,true);
			valueOperations.increment("seckillGoods:"+goodsId);
			return RespBean.error(RespBeanEnum.EMPTY_STOCK);
		}
		SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
		mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
		return RespBean.success(0);



		/*GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
		//判断库存
		if (goods.getStockCount() < 1) {
			return RespBean.error(RespBeanEnum.EMPTY_STOCK);
		}
		//判断是否重复抢购
		SeckillOrder seckillOrder =
				seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id",
						goodsId));
		if (seckillOrder != null) {
			return RespBean.error(RespBeanEnum.REPEATE_ERROR);
		}
		Order order = orderService.seckill(user, goods);
		return RespBean.success(order);*/



	}

	@RequestMapping(value = "/result",method = RequestMethod.POST)
	@ResponseBody
	public RespBean getResult(User user, Long goodsId){
		if (user==null){
			return RespBean.error(RespBeanEnum.SESSION_ERROR);
		}
		Long orderId=seckillOrderService.getResult(user,goodsId);
		return RespBean.success(orderId);
	}




	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> list = goodsService.findGoodsVo();
		if (CollectionUtils.isEmpty(list)){
			return;
		}
		list.forEach(goodsVo ->{
			redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
			EmptyStockMap.put(goodsVo.getId(),false);
	}
	);
	}
}