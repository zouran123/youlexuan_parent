package com.offcn.task;

import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class TestTask {

    // 秒 分 时 日 月 周 (年)
    // * 表示任意
    // @Scheduled(cron = "5/10 * * * * *")
    public void hello() {
        // System.out.println("我跑起来了...." + new Date());
    }

    //@Scheduled(cron = "0 0/5 9,18 * * *")
    public void say() {
        System.out.println("我说话了...." + new Date());
    }

}
