package com.ymj.zookeeper.conf;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author : yemingjie
 * @date : 2021/5/15 15:32
 */
public class ZKUtils {
    private static ZooKeeper zk;

    private static String address = "192.168.195.132:2181,192.168.195.133:2181,192.168.195.134:2181,192.168.195.135:2181/testConf";

    private static DefaultWatch watch = new DefaultWatch();

    private static CountDownLatch init  = new CountDownLatch(1);

    public static ZooKeeper getZk() {
        try {
            zk = new ZooKeeper(address, 1000, watch);
            watch.setCd(init);
            init.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zk;
    }
}
