package com.xxxx.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
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
public class SecKillController {

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private ISeckillOrderService seckillOrderService;
	@Autowired
	private IOrderService orderService;
	@Autowired
	private RedisTemplate redisTemplate;
//	@Autowired
//	private MQSender mqSender;
//	@Autowired
//	private RedisScript<Long> script;

	private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

//window优化前QPS:258
//linux优化前QPS：

	@RequestMapping("/doSeckill")
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
		SeckillOrder seckillOrder =
				seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id",
						goodsId));
		if (seckillOrder != null) {
			model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
			return "secKillFail";
		}
		Order order = orderService.seckill(user, goods);
		model.addAttribute("order", order);
		model.addAttribute("goods", goods);
		return "orderDetail";
	}


	/*

	@RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
	@ResponseBody
	public RespBean doSeckill(@PathVariable String path, User user, Long goodsId) {
		if (user == null) {
			return RespBean.error(RespBeanEnum.SESSION_ERROR);
		}
		ValueOperations valueOperations = redisTemplate.opsForValue();
		boolean check = orderService.checkPath(user, goodsId, path);
		if (!check) {
			return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
		}
		//判断是否重复抢购
		SeckillOrder seckillOrder =
				(SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
		if (seckillOrder != null) {
			return RespBean.error(RespBeanEnum.REPEATE_ERROR);
		}
		//内存标记，减少Redis的访问
		if (EmptyStockMap.get(goodsId)) {
			return RespBean.error(RespBeanEnum.EMPTY_STOCK);
		}
		//预减库存
		// Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
		Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId),
				Collections.EMPTY_LIST);
		if (stock < 0) {
			EmptyStockMap.put(goodsId, true);
			valueOperations.increment("seckillGoods:" + goodsId);
			return RespBean.error(RespBeanEnum.EMPTY_STOCK);
		}
		SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
		mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
		return RespBean.success(0);


		/*
		GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
		//判断库存
		if (goods.getStockCount() < 1) {
			return RespBean.error(RespBeanEnum.EMPTY_STOCK);
		}
		//判断是否重复抢购
		// SeckillOrder seckillOrder =
		// 		seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id",
		// 				goodsId));
		SeckillOrder seckillOrder =
				(SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
		if (seckillOrder != null) {
			return RespBean.error(RespBeanEnum.REPEATE_ERROR);
		}
		Order order = orderService.seckill(user, goods);
		return RespBean.success(order);
		 */
	/*}



	@RequestMapping(value = "/result", method = RequestMethod.GET)
	@ResponseBody
	public RespBean getResult(User user, Long goodsId) {
		if (user == null) {
			return RespBean.error(RespBeanEnum.SESSION_ERROR);
		}
		Long orderId = seckillOrderService.getResult(user, goodsId);
		return RespBean.success(orderId);
	}



	@AccessLimit(second = 5, maxCount = 5, needLogin = true)
	@RequestMapping(value = "/path", method = RequestMethod.GET)
	@ResponseBody
	public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
		if (user == null) {
			return RespBean.error(RespBeanEnum.SESSION_ERROR);
		}
		boolean check = orderService.checkCaptcha(user, goodsId, captcha);
		if (!check) {
			return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
		}
		String str = orderService.createPath(user, goodsId);
		return RespBean.success(str);
	}


	@RequestMapping(value = "/captcha", method = RequestMethod.GET)
	public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
		if (user == null || goodsId < 0) {
			throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
		}
		//设置请求头为输出图片的类型
		response.setContentType("image/jpg");
		response.setHeader("Pargam", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		//生成验证码，将结果放入Redis
		ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
		redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300,
				TimeUnit.SECONDS);
		try {
			captcha.out(response.getOutputStream());
		} catch (IOException e) {
			log.error("验证码生成失败", e.getMessage());
		}
	}



	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> list = goodsService.findGoodsVo();
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		list.forEach(goodsVo -> {
					redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
					EmptyStockMap.put(goodsVo.getId(), false);
				}
		);
	}*/
}