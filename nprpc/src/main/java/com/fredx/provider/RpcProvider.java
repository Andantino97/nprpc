package com.fredx.provider;

import com.fredx.callback.INotifyProvider;
import com.fredx.util.ZkClientUtils;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 描述：rpc方法发布的站点，只需要一个站点就可以发布当前主机上所有的rpc方法了  用 单例模式 设计RpcProvider
 */
public class RpcProvider implements INotifyProvider {
    private static final String SERVER_IP = "ip";
    private static final String SERVER_PORT = "port";
    private static final String ZK_SERVER = "zookeeper";
    private String serverIP;
    private int serverPort;
    private String zkServer;
    private ThreadLocal<byte[]> responsebuflocal;



    /**
     * 服务方法的类型信息
     */
    class ServiceInfo{
        public ServiceInfo(){
            this.service = null;
            this.methodMap = new HashMap<>();  //考虑到了线程安全，因为其中只涉及到读操作，因此使用HashMap也ok的
        }
        Service service;
        Map<String, Descriptors.MethodDescriptor> methodMap;
    }

    /**
     * 包含所有的服务对象和服务方法
     */
    private Map<String, ServiceInfo> serviceMap;

    /**
     * 启动rpc站点提供服务
     */
    public void start() {


        //todo... 把service和method都往zookeeper上注册一下
        ZkClientUtils zk = new ZkClientUtils(zkServer);
        serviceMap.forEach((k, v)->{
            String path = "/" + k;
            zk.createPersistent(path, null);
            v.methodMap.forEach((a, b)->{
                String createPath = path + "/" + a;
                zk.createEphemeral(createPath, serverIP+":"+serverPort);
                //给临时性节点添加监听器watcher
                zk.addWatcher(createPath);
                System.out.println("reg zk ->" + (createPath));
                });
        });

        System.out.println("rpc server start at:" + serverIP + ":" + serverPort);
        //启动rpc server网络服务
        RpcServer s = new RpcServer(this);
        s.start(serverIP, serverPort);
    }

    /**
     * notify是在多线程环境中被调用到的
     * 接受RpcServer网络模块上报的rpc调用相关信息参数，执行具体的rpc方法调用
     * @param serviceName
     * @param methodName
     * @param args
     * @return 把rpc方法调用完成以后的响应值进行返回
     */
    @Override
    public byte[] notify(String serviceName, String methodName, byte[] args) {
        ServiceInfo si = serviceMap.get(serviceName);
        Service service = si.service;  //获取服务对象
        Descriptors.MethodDescriptor method = si.methodMap.get(methodName);  //获取服务方法

        //从args反序列化出method方法的参数 LoginRequest RegRequest
        Message request = service.getRequestPrototype(method).toBuilder().build();
        try {
            request = request.getParserForType().parseFrom(args);  //反序列化操作
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        /**
         * rpc对象：service
         * rpc对象的方法：method
         * rpc方法的参数：request
         * 根据
         */
        service.callMethod(method, null, request, response ->
            responsebuflocal.set(response.toByteArray()));

        return responsebuflocal.get();    }

    /**
     * 注册rpc服务方法  只要支持rpc方法的类，都实现了com.google.protobuf.Service这个接口（所以该方法的参数才为Service 对象，而非UserServiceImpl对象）
     * @param service
     */
    public void registerRpcService(Service service) {
        Descriptors.ServiceDescriptor sd = service.getDescriptorForType();
        String serviceName = sd.getName();    //获取服务对象名称
        ServiceInfo si = new ServiceInfo();
        si.service = service;
        //获取服务对象的所有服务方法列表
        List<Descriptors.MethodDescriptor> methodList = sd.getMethods();
        methodList.forEach(method->{
            String method_name = method.getName();
            si.methodMap.put(method_name, method);
        });
    serviceMap.put(serviceName, si);
    }

    /**
     * 封装RpcProvider对象创建的细节
     */
    public static class Builder{
        private static RpcProvider INSTANCE = new RpcProvider();  //定义唯一的实例

        /**
         * 从配置文件中读取rpc server的ip和port，给INSTANCE对象初始化数据
         * 通过builder创建一个RpcProvider对象
         * @return
         */
        public RpcProvider Build(String file){
            Properties pro = new Properties();
            try{
                pro.load(Builder.class.getClassLoader().getResourceAsStream(file));
                INSTANCE.setServerIP(pro.getProperty(SERVER_IP));
                INSTANCE.setServerPort(Integer.parseInt(pro.getProperty(SERVER_PORT)));
                INSTANCE.setZkServer(pro.getProperty(ZK_SERVER));
                return INSTANCE;
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private RpcProvider(){
        this.serviceMap = new HashMap<>();
        this.responsebuflocal = new ThreadLocal<>();
    }
    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }
}
