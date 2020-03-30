package com.jt.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jt.mapper.CartMapper;
import com.jt.pojo.Cart;
import com.jt.pojo.User;
import com.jt.util.ThreadLocalUtil;

@Service
public class DubboCartServiceImpl implements DubboCartService {
	
	@Autowired
	private CartMapper cartMapper;

	@Override
	public List<Cart> findCartListByUserId(Long userId) {
		QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
		queryWrapper.eq("user_id", userId);
		return cartMapper.selectList(queryWrapper);
	}

	/**
	 * 需求:新增购物车时,首先根据itemId和userId查询购物车数据.
	 * 	  如果查询的结果为null,表示用户第一次新增该商品,则直接入库.
	 * 	  如果查询的结果不为null,表示用户之前购买过该商品,则更新商品数量.
	 */
	@Override
	public void saveCart(Cart cart) {
		QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
		queryWrapper.eq("item_id", cart.getItemId())
					.eq("user_id", cart.getUserId());
		//数据库记录 包含主键
		Cart cartDB = cartMapper.selectOne(queryWrapper);
		if(cartDB == null) {
			//说明用户第一次购买
			cart.setCreated(new Date())
				.setUpdated(cart.getCreated());
			cartMapper.insert(cart);
		}else {
			//说明用户不是第一次购买,只做数量的更新
			//如果使用byId的方式进行更新,那么全部记录的字段除ID之外都要更新,效率较低
			//实质修改的数据num/updated whereid=xxx;
			int num = cartDB.getNum() + cart.getNum();
			Cart cartTemp = new Cart();
			cartTemp.setId(cartDB.getId()).setNum(num).setUpdated(new Date());
			cartMapper.updateById(cartTemp);
			//cartDB.setNum(num).setUpdated(new Date());
			//cartMapper.updateById(cartDB);
		}
	}

	//参数:userId itemId唯一确定数据信息  num
	@Override
	public void updateNum(Cart cart) {
		Cart cartTemp = new Cart();
		cartTemp.setNum(cart.getNum()).setUpdated(new Date());
		UpdateWrapper<Cart> updateWrapper = new UpdateWrapper<Cart>();
		updateWrapper.eq("item_id", cart.getItemId())
					 .eq("user_id", cart.getUserId());
		cartMapper.update(cartTemp, updateWrapper);
	}
	
	/**
	 * cart只有userId和itemId不为null
	 * 对象中不为null的属性充当where条件
	 */
	@Override
	public void deleteCart(Cart cart) {
		QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>(cart);
		cartMapper.delete(queryWrapper);
	}
	
}	
