package com.ymj.zookeeper.conf;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * @author : yemingjie
 * @date : 2021/5/15 15:41
 */
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    ZooKeeper zk;
    MyConf myConf;
    CountDownLatch cc = new CountDownLatch(1);

    public MyConf getMyConf() {
        return myConf;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {

        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                // 节点被创建
                zk.getData("/AppConf", this, this, "ssds");
                break;
            case NodeDeleted:
                // 容忍性
                myConf.setConf("");
                cc = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                // 节点中数据被变更了
                zk.getData("/AppConf", this, this, "ssds");
                break;
            case NodeChildrenChanged:
                break;
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if (data != null) {
            String s = new String(data);
            myConf.setConf(s);
            // 取完数据之后再减
            cc.countDown();
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat != null) {
            // 取数据
            zk.getData("/AppConf", this, this, "sdfs");
        }
    }


    public void aWait() {
        zk.exists("/AppConf", this, this, "ABC");
        try {
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
