package com.offcn.listen;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class DelPageListen implements MessageListener {

    @Autowired
    private ItemPageService pageService;


    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage obj = (ObjectMessage) message;
            Long[] ids = (Long[]) obj.getObject();

            pageService.delItemHtml(ids);

            System.out.println(">>>>>消息消费成功：" + Arrays.toString(ids) + "页面删除成功");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
