package com.jt.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jt.pojo.Cart;
import com.jt.pojo.Order;
import com.jt.pojo.User;
import com.jt.service.DubboCartService;
import com.jt.service.DubboOrderService;
import com.jt.util.ThreadLocalUtil;
import com.jt.vo.SysResult;

@Controller
@RequestMapping("/order")
public class OrderController {
	
	@Reference(check=false)
	private DubboCartService cartService;
	@Reference(check=false)
	private DubboOrderService orderService;
	/**
	 * 跳转订单确认页面
	 * 根据userId查询购物车数据信息,页面${carts}
	 */
	@RequestMapping("/create")
	public String create(Model model) {
		Long userId = ThreadLocalUtil.getUser().getId();
		List<Cart> cartList = cartService.findCartListByUserId(userId);
		model.addAttribute("carts", cartList);
		return "order-cart";
	}
	
	
	/**
	 * url:/order/submit
	 * 参数:整合form表单
	 * 返回值结果: SysResult对象
	 */
	@RequestMapping("/submit")
	@ResponseBody
	public SysResult saveOrder(Order order) {
		Long userId = ThreadLocalUtil.getUser().getId();
		order.setUserId(userId);
		//要求入库之后,返回orderId的值
		String orderId = orderService.saveOrder(order);
		if(StringUtils.isEmpty(orderId)) {
			return SysResult.fail();
		}
		return SysResult.success(orderId);
	}
	
	/**
	 * 根据orderId查询订单信息
	 * url:http://www.jt.com/order/success.html?id=71582967663641
	 * 参数:id=xxxxx
	 * 返回值:success.html  成功页面
	 * 页面取值:${order.orderId}
	 */
	@RequestMapping("/success")
	public String findOrderById(String id,Model model) {
		
		Order order = orderService.findOrderById(id);
		model.addAttribute("order", order);
		return "success";
	}
	
	
	
	
	
	
	
	
	
}
