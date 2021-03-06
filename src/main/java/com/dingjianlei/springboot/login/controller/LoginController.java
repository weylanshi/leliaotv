package com.dingjianlei.springboot.login.controller;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dingjianlei.springboot.constants.Constant;
import com.dingjianlei.springboot.dto.ResultObject;
import com.dingjianlei.springboot.entity.ChatUser;
import com.dingjianlei.springboot.entity.Favorites;
import com.dingjianlei.springboot.entity.Room;
import com.dingjianlei.springboot.service.ChatUserService;
import com.dingjianlei.springboot.service.FavoritesService;
import com.dingjianlei.springboot.service.RoomService;

/**
 * 登录 注册 controller
 * 
 * @author Zlyj
 *
 */
@Controller("loginController")
public class LoginController {
	@Autowired
	private ChatUserService chatUserService;
	@Autowired
	private FavoritesService favoritesService;
	@Autowired
	private RoomService roomService;
	/** 
	 * 跳到首页
	 * @return
	 */
	@RequestMapping("/")
	public String getIndex(ModelMap map) {
		List<Room> roomList = roomService.findRoomFillIndex();
		List<Room> roomListTop=roomList.subList(0, Integer.parseInt(Constant.INDEX_ROOM_COUNT)/2-1);
		List<Room> roomListBottom=roomList.subList(Integer.parseInt(Constant.INDEX_ROOM_COUNT)/2,Integer.parseInt(Constant.INDEX_ROOM_COUNT));
		map.put("roomListTop", roomListTop);
		map.put("roomListBottom", roomListBottom);
		return "index";
	}
	/**
	 * 登录 controller
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	@SuppressWarnings("finally")
	@RequestMapping("login")
	public String getLogin(String username, String password, ModelMap modelMap,HttpServletRequest request,HttpServletResponse response) {
		ChatUser chatUser = chatUserService.login(username, password);
		// 无论出错都必须跳转到首页
		
		if (chatUser != null) {
			try {// 登陆时增加积分 最好用消息队列 这里用httpclent模拟						
				chatUserService.updateScoreByChatUserId(chatUser.getId(), Constant.LOGIN_ADD_SCORE);
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			    Cookie cookie = new Cookie(Constant.LOGIN_COOKIE,chatUser.getId());//创建一个cookie，cookie的名字是lastAccessTime
			       //将cookie对象添加到response对象中，这样服务器在输出response对象中的内容时就会把cookie也输出到客户端浏览器		       
			    modelMap.addAttribute("chatUserId", chatUser.getId());
		        response.addCookie(cookie);
				List<Room> roomList = roomService.findRoomFillIndex();
				List<Room> roomListTop=roomList.subList(0, Integer.parseInt(Constant.INDEX_ROOM_COUNT)/2-1);
				List<Room> roomListBottom=roomList.subList(Integer.parseInt(Constant.INDEX_ROOM_COUNT)/2,Integer.parseInt(Constant.INDEX_ROOM_COUNT));
				modelMap.put("roomListTop", roomListTop);
				modelMap.put("roomListBottom", roomListBottom);
				return "index";
			}
		} else {
			// 如果登陆失败，必须返回到login页面
			return "login";
		}
	}
	
	
	/**
	 * 登录 controller
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	@SuppressWarnings("finally")
	@RequestMapping("singleLogin")
	public String singleLogin(String username, String password, ModelMap modelMap,HttpServletRequest request,HttpServletResponse response,String roomId) {
		ChatUser chatUser = chatUserService.login(username, password);
		// 无论出错都必须跳转到首页
		
		if (chatUser != null) {
			try {// 登陆时增加积分 最好用消息队列 这里用httpclent模拟
				List<Favorites> favList = fillFavoritesListByUsername(favoritesService, username);
				
				modelMap.addAttribute("favoritesList", favList);
				
				chatUserService.updateScoreByChatUserId(chatUser.getId(), Constant.LOGIN_ADD_SCORE);
				
				Room room = roomService.findById(roomId);
				List<ChatUser> chatUserList = chatUserService.getRank();
				if(chatUser!=null)
					modelMap.addAttribute("chatUser", chatUser);
				modelMap.addAttribute("chatUserList", chatUserList);
				 //获取浏览器访问访问服务器时传递过来的cookie数组
				       Cookie[] cookies = request.getCookies();
		        //如果用户是第一次访问，那么得到的cookies将是null
			        if (cookies!=null) {
				           for (int i = 0; i < cookies.length; i++) {
			                Cookie cookie = cookies[i];
			              if (cookie.getName().equals(Constant.LOGIN_COOKIE)) {
			            	  modelMap.put("loginStatus", "yes");
			              }
			             }
			        }else {
			        	modelMap.put("loginStatus", "no");
			       }		
			        modelMap.put("room", room);
			        modelMap.put("chatUserId", chatUser.getId());
			        modelMap.put("roomId", roomId);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			    Cookie cookie = new Cookie(Constant.LOGIN_COOKIE,chatUser.getId());//
			       //将cookie对象添加到response对象中，这样服务器在输出response对象中的内容时就会把cookie也输出到客户端浏览器		       
			    modelMap.addAttribute("chatUserId", chatUser.getId());
		        response.addCookie(cookie);
				return "single";
			}
		} else {
			// 如果登陆失败，必须返回到login页面
			return "login";
		}
	}

	/**
	 * 登录成功后将所喜欢的主播查找出来
	 * 
	 * @param favoritesService
	 * @param username
	 * @return
	 */
	private List<Favorites> fillFavoritesListByUsername(FavoritesService favoritesService, String username) {
		// TODO Auto-generated method stub
		return favoritesService.findFavoritesListByUsername(username);
	}

	/**
	 * 检查邮箱验证码
	 * 
	 * @param email
	 * @param checkCode
	 * @return
	 */
	@RequestMapping("checkEmailCode")
	@ResponseBody
	public boolean checkEmailCode(String email, String checkCode) {

		return true;
	}

	/**
	 * 
	 * @param 注册账号chatUser
	 * @return 注册成功返回到登录界面
	 * 
	 */
	@RequestMapping("register")
	public String getRegister(ChatUser chatUser) {
		chatUserService.insertChatUser(chatUser);
		return "dispatcherLogin";
	}
	
	/**跳转注册界面
	 * @param chatUser
	 * @return
	 */
	@RequestMapping("dispatcherRegister")
	public String dispatcherRegister(ChatUser chatUser) {
		chatUserService.insertChatUser(chatUser);
		return "register";
	}
	/**
	 * 
	 * @param 跳转登录界面
	 */
	@RequestMapping("dispatcherLogin")
	public String dispatcherLogin() {
		return "login";
	}
	/**
	 * @param userName
	 * @param type
	 *            1 用户名校验 0 邮箱校验
	 * @return
	 */
	@RequestMapping("/checkInfo/{type}")
	@ResponseBody
	public ResultObject getCheckInfo(String username, @PathVariable String type, String email) {
		ResultObject resultObject = null;
		try {
			// false表示不存在这个邮箱或者用户名，表示可以使用
			resultObject = chatUserService.checkChatUser(type, username, email);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultObject;
	}
}
