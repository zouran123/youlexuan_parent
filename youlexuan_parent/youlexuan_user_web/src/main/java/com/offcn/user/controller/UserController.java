package com.offcn.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbUser;
import com.offcn.user.service.UserService;
import com.offcn.util.PhoneFormatCheckUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * usercontroller
 * @author senqi
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	@RequestMapping("/getName")
	public String getName() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return name;
	}


	/**
	 * 增加
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		try {

			if(!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
				return new Result(false, "手机号，你是在逗我吗");
			}
			userService.sendSms(phone);
			return new Result(true, "发送成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "网络异常，请稍后重试");
		}
	}

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int rows){			
		return userService.findPage(page, rows);
	}

	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user, String myCode){
		try {
			// 验证页面传递过来的验证码 和 redis中的是否一致
			if(!userService.checkCode(user.getPhone(), myCode)) {
				return new Result(false, "验证码输入错误");
			}

			user.setCreated(new Date());
			user.setUpdated(new Date());

			// 使用apache工具包进行md5加密
			user.setPassword(DigestUtils.md5Hex(user.getPassword()));

			userService.add(user);

			// 此处将redis中存储的验证码移除
			userService.removeCode(user.getPhone());

			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbUser findOne(Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	/**
	 * 查询+分页
	 * @param user
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbUser user, int page, int size){
		return userService.findPage(user, page, size);		
	}
	
}
