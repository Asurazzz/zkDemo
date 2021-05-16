package com.ymj.zookeeper.lock;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author : yemingjie
 * @date : 2021/5/15 20:17
 */
public class TestLock {

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

    @Test
    public void zkLockTest() {
        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);
                    String name = Thread.currentThread().getName();
                    watchCallBack.setThreadName(name);
                    // 每一个线程  抢锁
                    watchCallBack.tryLock();
                    // 干活
                    System.out.println(name + "  working.......");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 释放锁, 不释放的话就是永远是第一个节点持有锁
                    watchCallBack.unLock();
                }
            }.start();
        }

        while (true) {

        }

    }
}
