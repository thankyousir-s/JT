package com.jt.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jt.mapper.OrderItemMapper;
import com.jt.mapper.OrderMapper;
import com.jt.mapper.OrderShippingMapper;
import com.jt.pojo.Order;
import com.jt.pojo.OrderItem;
import com.jt.pojo.OrderShipping;

@Service
public class OrderServiceImpl implements DubboOrderService {
	
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderShippingMapper orderShippingMapper;
	@Autowired
	private OrderItemMapper orderItemMapper;
	
	@Transactional	//控制数据库事务
	@Override
	public String saveOrder(Order order) {
		
		String orderId = ""+order.getUserId()+System.currentTimeMillis();//登录用户id+当前时间戳
		Date date = new Date();
		//1.订单入库
		order.setOrderId(orderId)
			 .setStatus(1)	//未支付状态
			 .setCreated(date)
			 .setUpdated(date);
		orderMapper.insert(order);
		System.out.println("入库订单成功!!!!");
		
		//2.入库物流信息
		OrderShipping shipping = order.getOrderShipping();
		shipping.setOrderId(orderId)
				.setCreated(date)
				.setUpdated(date);
		orderShippingMapper.insert(shipping);
		System.out.println("订单物流入库完成!!!");
		
		//3.订单商品入库
		List<OrderItem> orderItems = order.getOrderItems();
		for (OrderItem orderItem : orderItems) {
			orderItem.setOrderId(orderId)
					 .setCreated(date)
					 .setUpdated(date);
			orderItemMapper.insert(orderItem);
		}
		System.out.println("订单入库完成");
		return orderId;
	}

	@Override
	public Order findOrderById(String id) {
		
		Order order = orderMapper.selectById(id);
		OrderShipping orderShipping = orderShippingMapper.selectById(id);
		QueryWrapper<OrderItem> queryWrapper = new QueryWrapper<OrderItem>();
		queryWrapper.eq("order_id", id);
		List<OrderItem> orderItems = 
				orderItemMapper.selectList(queryWrapper);
		return order.setOrderShipping(orderShipping).setOrderItems(orderItems);
	}
}
