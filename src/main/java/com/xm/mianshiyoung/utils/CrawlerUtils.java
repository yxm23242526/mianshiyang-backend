package com.xm.mianshiyoung.utils;


import cn.dev33.satoken.stp.StpUtil;
import com.xm.mianshiyoung.common.ErrorCode;
import com.xm.mianshiyoung.exception.BusinessException;
import com.xm.mianshiyoung.manager.CounterManager;
import com.xm.mianshiyoung.model.entity.User;
import com.xm.mianshiyoung.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


/**
 * 检测爬虫工具类
 */
@Component
public class CrawlerUtils {

    @Resource
    private CounterManager counterManager;

    @Resource
    private UserService userService;

    /**
     * 检测爬虫
     *
     * @param loginUserId
     */
    public void crawlerDetect(long loginUserId) {
        // 调用多少次时告警
        final int WARN_COUNT = 10;
        // 超过多少次封号
        final int BAN_COUNT = 20;
        // 拼接访问 key
        String key = String.format("user:access:%s", loginUserId);
        // 一分钟内访问次数，180 秒过期
        long count = counterManager.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 180);
        // 是否封号
        if (count > BAN_COUNT) {
            // 踢下线
            StpUtil.kickout(loginUserId);
            // 封号
            User updateUser = new User();
            updateUser.setId(loginUserId);
            updateUser.setUserRole("ban");
            userService.updateById(updateUser);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "访问太频繁，已被封号");
        }
        // 是否告警
        if (count == WARN_COUNT) {
            // 可以改为向管理员发送邮件通知
            throw new BusinessException(110, "警告访问太频繁");
        }
    }

}
