package io.jboot.test.mq.rocketmq;


import io.jboot.Jboot;
import io.jboot.app.JbootApplication;
import io.jboot.components.mq.JbootMqMessageInfo;
import io.jboot.components.mq.JbootmqMessageListener;

public class RocketmqReceiver2 {

    public static void main(String[] args) {

        //Undertow端口号配置
        JbootApplication.setBootArg("undertow.port", "8002");

        //设置 mq 的相关信息
        JbootApplication.setBootArg("jboot.mq.type", "rocketmq");
        JbootApplication.setBootArg("jboot.mq.channel", "channel1,channel2,myChannel");
        JbootApplication.setBootArg("jboot.mq.rocket.namesrvAddr", "127.0.0.1:9876");

        //启动应用程序
        JbootApplication.run(args);

        //添加监听
        Jboot.getMq().addMessageListener(new JbootmqMessageListener() {
            @Override
            public void onMessage(String channel, Object message, JbootMqMessageInfo info) {
                System.out.println("Receive msg: " + message + ", from channel: " + channel);
            }
        });

        // 只监听 myChannel 这个通道
        Jboot.getMq().addMessageListener(new JbootmqMessageListener() {
            @Override
            public void onMessage(String channel, Object message, JbootMqMessageInfo info) {
                System.out.println("Receive msg: " + message + ", from channel: " + channel);
            }
        },"myChannel");

        Jboot.getMq().startListening();

        System.out.println("RedisMqReceiver1 started.");
    }
}
