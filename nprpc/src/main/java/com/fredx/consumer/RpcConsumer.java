package com.fredx.consumer;

import com.fredx.provider.RpcMetaProto;
import com.fredx.provider.RpcProvider;
import com.fredx.util.ZkClientUtils;
import com.google.protobuf.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

public class RpcConsumer implements RpcChannel {
    private static final String ZK_SERVER = "zookeeper";
    private String zkServer;
    public RpcConsumer(String file){
        Properties pro = new Properties();
        try{
            pro.load(RpcProvider.Builder.class.getClassLoader().getResourceAsStream(file));
            this.zkServer = pro.getProperty(ZK_SERVER);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * stub代理对象，需要接收一个实现了RpcChannel的对象，当stub调用任意rpc方法的时候，全部调用了
     * @param methodDescriptor
     * @param rpcController
     * @param message
     * @param message1
     * @param rpcCallback
     */
    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor, RpcController rpcController, Message message, Message message1, RpcCallback<Message> rpcCallback) {
         Descriptors.ServiceDescriptor sd = methodDescriptor.getService();
         String serviceName = sd.getName();
         String methodName = methodDescriptor.getName();

         //todo... 在zookeeper上查询serviceName-methodName在哪个主机上 ip和port
        String ip = "";
        int port = 0;
        ZkClientUtils zk = new ZkClientUtils(zkServer);
        String path = "/" + serviceName + "/" + methodName;
        String hostStr = zk.read(path);
        zk.close();
        if(hostStr == null){
            rpcController.setFailed("read path: " + path + " from zk failed!");
            rpcCallback.run(message1);
            return;
        }else{
            String[] host = hostStr.split(":");
            ip = host[0];
            port = Integer.parseInt(host[1]);
        }

         //序列化头部信息
        RpcMetaProto.RpcMeta.Builder meta_builder = RpcMetaProto.RpcMeta.newBuilder();
        meta_builder.setServiceName(serviceName);
        meta_builder.setMethodName(methodName);
        byte[] metabuf = meta_builder.build().toByteArray();

        //参数
        byte[] argbuf = message.toByteArray();

        //组织rpc参数信息
        ByteBuf buf = Unpooled.buffer(4 + metabuf.length + argbuf.length);
        buf.writeInt(metabuf.length);
        buf.writeBytes(metabuf);
        buf.writeBytes(argbuf);

        //待发送的数据
        byte[] sendbuf = buf.array();

        //通过网络发送rpc调用请求信息
        Socket client = null;
        OutputStream out = null;
        InputStream in = null;

        client = new Socket();
        try {
            client.connect(new InetSocketAddress(ip, port));
            out = client.getOutputStream();
            in = client.getInputStream();
            out.write(sendbuf);
            out.flush();
            //wait等待rpc调用响应
            ByteArrayOutputStream recvbuf = new ByteArrayOutputStream();
            byte[] rbuf = new byte[1024];
            int size = in.read(rbuf);
            /**
             * 这里的size有可能是0，因为rpcProvider封装response响应参数的时候如果响应参数的成员变量的值都是默认值，
             * 实际上rpcProvider递给rpcServer的就是一个空数据
             */
            if(size > 0){
                recvbuf.write(rbuf, 0, size);
                rpcCallback.run(message1.getParserForType().parseFrom(recvbuf.toByteArray()));
            }else{
                rpcCallback.run(message1.getParserForType().parseFrom(new byte[0]));
            }

        } catch (IOException e) {
            rpcController.setFailed("server connect error, check server!");
            rpcCallback.run(message1);
        } finally {

            try {
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (client != null){
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }
}
