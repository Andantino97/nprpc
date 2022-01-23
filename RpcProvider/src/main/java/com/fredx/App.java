package com.fredx;

import com.fredx.provider.RpcProvider;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        /**
         * 启动一个可以提供rpc远程方法调用的server
         * 1. 需要一个rpcprovider（nprpc提供的）对象
         * 2. 向rpcprovider上面注册rpc方法 userserviceImpl.login  UserServiceImpl.reg（将服务对象名字、服务对象、服务方法名字、服务方法注册到框架去）
         * 3. 启动RpcProvider这个Server站点，阻塞等待远程rpc方法调用请求（此处的RpcProvider是框架里命名的一个类，和工程里的RpcProvider名字不是一回事）
         * */
        RpcProvider.Builder builder = RpcProvider.newBuilder();    //框架通过配置文件读取主机所在的ip地址和端口
        RpcProvider provider = builder.Build("config.properties");

        /**
         *  UserServiceImpl: 服务对象名称
         *  login、reg: 服务方法名称
         */
        provider.registerRpcService(new UserServiceImpl());

        /**
         * 启动RPC server站点，阻塞等待到远程rpc调用请求
         */
        provider.start();
    }
}
