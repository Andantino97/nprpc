package com.fredx.util;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: nprpc_framework
 * @description: 和zookeeper通信用的工具类
 * @author: fredx
 * @create: 2022-01-22 11:50
 **/
public class ZkClientUtils {
    private static String rootpath = "/nprpc";
    private ZkClient zkClient;
    private Map<String, String> ephemeralMap = new HashMap<>();
    /**
     * 通过zkserver字符串信息连接zkserver
     * @param servers
     */


    public ZkClientUtils(String servers) {
        this.zkClient = new ZkClient(servers, 3000);
        // 如果root节点不存在，才创建
        if(!this.zkClient.exists(rootpath))
        {  //znode节点不存在，才创建
            this.zkClient.createPersistent(rootpath, null);
        }
    }

    /**
     * 关闭和zkServer的连接
     */
    public void close(){
        this.zkClient.close();
    }

    /**
     * zk创建临时性节点
     * @param path
     * @param data
     */
    public void createEphemeral(String path, String data){
        path = rootpath + path;
        ephemeralMap.put(path, data);
        if(!this.zkClient.exists(path)){  //znode节点不存在，才创建
            this.zkClient.createEphemeral(path, data);

        }
    }

    /**
     * zk创建永久性节点
     * @param path
     * @param data
     */
    public void createPersistent(String path, String data){
        path = rootpath + path;
        if(!this.zkClient.exists(path)){
            this.zkClient.createPersistent(path, data);
        }
    }

    /**
     * 读取znode节点的值
     * @param path
     * @return
     */
    public String read(String path){
        return this.zkClient.readData(rootpath + path, null);
    }

    /**
     * 给zk上指定的znode添加监听
     * @param path
     */
    public void addWatcher(String path){
        this.zkClient.subscribeDataChanges(rootpath + path, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            /**
             * 一定要设置znode节点监听，因为如果zkClient断掉，由于zookeeper server无法及时获知zkClient的关闭状态，所以zkServer会等待session timeout
             * 时间以后，会把zkClient创建的临时节点全部删除掉，但是如果在session timeout时间内，又启动了同样的zkClient，那么此时等待session timeout时间超时以后，
             * 原先创建的临时节点都不存在了
             * @param
             * @throws Exception
             */
            @Override
            public void handleDataDeleted(String path) throws Exception {
                System.out.println("watcher -> handleDataDeleted" + path);
                //把删除掉的临时性节点重新创建一下
                String str = ephemeralMap.get(path);
                if (str != null){
                    zkClient.createEphemeral(path, str);
                }

            }
        });

    }
    public static String getRootpath() {
        return rootpath;
    }

    public static void setRootpath(String rootpath) {
        ZkClientUtils.rootpath = rootpath;
    }

    public static void main(String[] args) {
        ZkClientUtils zk = new ZkClientUtils("127.0.0.1:2181");
        zk.createPersistent("/ProductService", "123456");
        System.out.println(zk.read("/ProductService"));
        zk.close();
    }
}


