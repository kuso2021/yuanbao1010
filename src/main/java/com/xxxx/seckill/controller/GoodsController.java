package com.xxxx.seckill.controller;

import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @Auther:huhao
 * @Date:2021/8/18-21:47
 * @Description:com.xxxx.seckill.controller
 * @version:1.0
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
	@Autowired
	private IUserService userService;
	@Autowired
	private IGoodsService goodsService;
//windows优化前QPS:180
	//linux优化前QPS：18

	@RequestMapping(value = "/toList")
	public String toList(Model model, User user) {
		model.addAttribute("user",user);
		model.addAttribute("goodsList",goodsService.findGoodsVo());
		return "goodsList";

	}

	@RequestMapping("/toDetail/{goodsId}")
	public String toDetail(Model model, User user, @PathVariable long goodsId){
		model.addAttribute("user",user);
		GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
		Date startDate=goodsVo.getStartDate();
		Date endDate=goodsVo.getEndDate();
		Date nowDate = new Date();
		int secKillStatus=0;
		int remainSeconds=0;
		if (nowDate.before(startDate)){
            remainSeconds=(int) ((startDate.getTime() - nowDate.getTime())/1000);
		}else if (nowDate.after(endDate)){
			secKillStatus=2;
			remainSeconds=-1;
		}else{
			secKillStatus=1;
			remainSeconds=0;
		}
		model.addAttribute("secKillStatus",secKillStatus);
		model.addAttribute("goods",goodsVo);
		model.addAttribute("remainSeconds",remainSeconds);
		return "goodsDetail";
	}





	/*@RequestMapping(value = "/toDetail2/{goodsId}", produces = "text/html;charset=utf-8")
	@ResponseBody
	public String toDetail2(Model model, User user, @PathVariable Long goodsId,
							HttpServletRequest request, HttpServletResponse response) {
		ValueOperations valueOperations = redisTemplate.opsForValue();
		//Redis中获取页面，如果不为空，直接返回页面
		String html = (String) valueOperations.get("goodsDetail:" + goodsId);
		if (!StringUtils.isEmpty(html)) {
			return html;
		}
		model.addAttribute("user", user);
		GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
		Date startDate = goodsVo.getStartDate();
		Date endDate = goodsVo.getEndDate();
		Date nowDate = new Date();
		//秒杀状态
		int secKillStatus = 0;
		//秒杀倒计时
		int remainSeconds = 0;
		//秒杀还未开始
		if (nowDate.before(startDate)) {
			remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
		} else if (nowDate.after(endDate)) {
			//	秒杀已结束
			secKillStatus = 2;
			remainSeconds = -1;
		} else {
			//秒杀中
			secKillStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("remainSeconds", remainSeconds);
		model.addAttribute("secKillStatus", secKillStatus);
		model.addAttribute("goods", goodsVo);
		WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
				model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
		if (!StringUtils.isEmpty(html)) {
			valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
		}
		return html;
		// return "goodsDetail";
	}




	@RequestMapping("/detail/{goodsId}")
	@ResponseBody
	public RespBean toDetail(User user, @PathVariable Long goodsId) {
		GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
		Date startDate = goodsVo.getStartDate();
		Date endDate = goodsVo.getEndDate();
		Date nowDate = new Date();
		//秒杀状态
		int secKillStatus = 0;
		//秒杀倒计时
		int remainSeconds = 0;
		//秒杀还未开始
		if (nowDate.before(startDate)) {
			remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
		} else if (nowDate.after(endDate)) {
			//	秒杀已结束
			secKillStatus = 2;
			remainSeconds = -1;
		} else {
			//秒杀中
			secKillStatus = 1;
			remainSeconds = 0;
		}
		DetailVo detailVo = new DetailVo();
		detailVo.setUser(user);
		detailVo.setGoodsVo(goodsVo);
		detailVo.setSecKillStatus(secKillStatus);
		detailVo.setRemainSeconds(remainSeconds);
		return RespBean.success(detailVo);
	}*/

}