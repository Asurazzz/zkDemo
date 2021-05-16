package com.ymj.zookeeper.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author : yemingjie
 * @date : 2021/5/15 20:24
 */
public class WatchCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    ZooKeeper zk;
    String threadName;
    CountDownLatch cc = new CountDownLatch(1);
    String pathName;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void process(WatchedEvent event) {

        // 如果第一个锁释放了，只有第二个才收到了回调事件
        // 如果不是第一个，而是中间的某一个挂了，也能造成他后面的节点收到通知，从而让后面的节点去监控前面的
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                // 节点删除事件，得到锁的目录，但是不需要watch
                zk.getChildren("/", false, this, "sdf");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }


    /**
     *  持久化节点           PERSISTENT
     *  持久顺序节点          PERSISTENT_SEQUENTIAL
     *  临时节点             EPHEMERAL
     *  临时自动编号节点       EPHEMERAL_SEQUENTIAL
     */
    public void tryLock() {
        try {
            zk.create("/path", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL, this, "abc");
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            zk.delete(pathName, -1);
            System.out.println(threadName + " over work......");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null) {
            System.out.println(threadName + "---> create node: " + name);
            pathName = name;
            // 父节点不需要监控watch，所以填false
            zk.getChildren("/", false, this, "sdf");
        }
    }


    /**
     * getChildren call back
     * @param
     * @param s
     * @param o
     * @param
     * @param stat
     */
    @Override
    public void processResult(int rc, String s, Object o, List<String> children, Stat stat) {
        // 一定能看到自己前面的

        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));

        // 是不是第一个
        if (i == 0) {
            // yes
            System.out.println(threadName + "  i am first....");
            cc.countDown();
        } else {
            // no
            zk.exists("/" + children.get(i - 1), this, this, "sdf");

        }
    }

    @Override
    public void processResult(int i, String s, Object o, Stat stat) {

    }
}
