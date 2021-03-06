package com.ymj.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );
        // zk是有session概念的，但是没有连接池的概念
        // watch：回调
        // 第一类：new zk的时候，传入的watch，这个watch是session级别的，跟path，node没有关系

        final CountDownLatch cd = new CountDownLatch(1);

        final ZooKeeper zk = new ZooKeeper("192.168.195.132:2181,192.168.195.133:2181,192.168.195.134:2181,192.168.195.135:2181",
                3000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                Event.KeeperState state = event.getState();
                Event.EventType type = event.getType();
                String path = event.getPath();
                System.out.println("new zk watch =========> " + event.toString());

                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
                        cd.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                }

                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                }
            }
        });


        cd.await();
        ZooKeeper.States states = zk.getState();
        switch (states) {
            case CONNECTING:
                System.out.println("ing......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed......");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }


        String pathName = zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        final Stat stat = new Stat();
        byte[] node = zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watch  ===========> " + event.toString());

                // true   default Watch 被重新注册 new zk的那个watchz
                try {
                    zk.getData("/ooxx", this, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }, stat);

        System.out.println(new String(node));

        // 触发回调
        Stat stat1 = zk.setData("/ooxx", "newdata".getBytes(), 0);
        // 还会触发吗？
        Stat stat2 = zk.setData("/ooxx", "newdata01".getBytes(), stat1.getVersion());


        // 非阻塞 先start 再over 再回调
        System.out.println("====================async start=====================");
        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("====================async call back=====================");
                System.out.println(ctx.toString());
                // 返回newdata01
                System.out.println(new String(data));
            }
        }, "abc");
        System.out.println("====================async over=====================");


        Thread.sleep(222222);

    }
}
