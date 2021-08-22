package com.xxxx.seckill.controller;

import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.vo.DetailVo;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private ThymeleafViewResolver thymeleafViewResolver;
//windows优化前QPS:180
	//linux优化前QPS：18

	@RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
	@ResponseBody
	public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
		ValueOperations valueOperations=redisTemplate.opsForValue();
		String html= (String) valueOperations.get("goodList");
		if (!StringUtils.isEmpty(html)){
			return html;
		}
		model.addAttribute("user",user);
		model.addAttribute("goodsList",goodsService.findGoodsVo());
		WebContext context=new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
		html=thymeleafViewResolver.getTemplateEngine().process("goodsList",context);
		if (!StringUtils.isEmpty(html)){
			valueOperations.set("goodsList",html,60, TimeUnit.SECONDS);
		}
		return html;

	}

	/*@RequestMapping(value = "/toDetail2/{goodsId}",produces = "text/html;charset=utf-8")
	@ResponseBody
	public String toDetail2(Model model, User user, @PathVariable long goodsId,HttpServletResponse response,HttpServletRequest request){
		ValueOperations valueOperations = redisTemplate.opsForValue();
		String html =(String) valueOperations.get("goodsDetail:" + goodsId);
		if (!StringUtils.isEmpty(html)){
			return html;
		}
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
		WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),model.asMap());
		html=thymeleafViewResolver.getTemplateEngine().process("goodsDetail",context);
		if (!StringUtils.isEmpty(html)){
			valueOperations.set("goodsDetail:"+goodsId,html,60,TimeUnit.SECONDS);

		}return html;
	}*/


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
	}

}