package com.ymj.zookeeper.conf;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author : yemingjie
 * @date : 2021/5/15 15:32
 */
public class TestConfig {

    ZooKeeper zk;

    @Before
    public void conn() {
        zk = ZKUtils.getZk();
    }



    @After
    public void close() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     *  操作步骤
     *  create /testConf ""
     *  create /testConf/AppConf "olddata"
     *  set /testConf/AppConf "newdata"
     */
    @Test
    public void getConf() {
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);
        MyConf myConf = new MyConf();
        watchCallBack.setMyConf(myConf);

        zk.exists("/AppConf", watchCallBack, watchCallBack, "ABC");
        // 先阻塞住然后再输出
        watchCallBack.aWait();
        // 1.节点不存在
        // 2.节点存在

        while (true) {
            if ("".equals(myConf.getConf())) {
                System.out.println("==============> conf配置丢失！");
                watchCallBack.aWait();
            } else {
                System.out.println(myConf.getConf());
            }


            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
