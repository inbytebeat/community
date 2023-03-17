package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 上午11:59
 * @Description:
 */
@Controller
@RequestMapping("/user")
public class UserController {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 文件上传路径
     */
    @Value("${community.path.upload}")
    private String uploadPath;

    /**
     * 项目域名
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 访问项目路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /***
     * 用于获取当前用户信息
     */
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 返回修改用户信息页面
     * @return 页面rul
     */
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 上传用户头像
     * @param headerImage 用户头像图片
     * @param model 模型用于存储数据
     * @return 对应结果的返回路径
     */
    @LoginRequired
    @RequestMapping(path = "upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null) {
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        // 获取文件原始名
        String fileName = headerImage.getOriginalFilename();
        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件的格式错误");
            return "/site/setting";
        }
        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 指定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 将文件写入到指定目录下
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败 服务器异常",e);
        }

        // 更新当前用户头像路径 （web访问路径） http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headUrl);
        // 最后重定向到首页
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response) {
        // 服务器存放图片的路径为
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 进行图片的响应
        response.setContentType("image/" + suffix);
        // 为了进行关闭 在try中加个括号 java7语法规定 如果括号中的变量有close方法，则会自动在finally中调用close关闭
        try (
                ServletOutputStream outputStream = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileName);)
        {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        return "/site/profile";
    }



    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    @LoginRequired
    public String updatePassword(Model model, String password, String newPassword, String confirmPassword) {
        User user  = hostHolder.getUser();
        if(StringUtils.isBlank(password) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(confirmPassword)) {
            model.addAttribute("passwordMsg","请同时输入旧密码,格式正确的新密码,以及确认密码");
            return "site/setting";
        }
        if(!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordMsg","两次密码不一致");
            return "site/setting";
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if(user.getPassword().equals(password)) {
            newPassword = CommunityUtil.md5(newPassword + user.getSalt());
            userService.updatePassword(user.getId(),newPassword);
        }
        return "redirect:/index";
    }

}
