package com.fredx;

import static org.junit.Assert.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * 测试protobuf的序列化和反序列化
     * Rigorous Test :-)
     */
    @Test
    public void test()
    {
        TestProto.LoginRequest.Builder login_builder = TestProto.LoginRequest.newBuilder();
        login_builder.setName("fred");
        login_builder.setPwd("282276");
        TestProto.LoginRequest request = login_builder.build();
        System.out.println(request.getName());
        System.out.println(request.getPwd());

        /**把LoginRequest对象序列化成字节流，通过网络发送出去
         * 此处的sendbuf就可以通过网络发送出去了
         */
        byte[] sendbuf = request.toByteArray();
        /**
         * protobuf从byte数组字节流反序列化生成loginrequest对象
         */
        try{
            TestProto.LoginRequest r = TestProto.LoginRequest.parseFrom(sendbuf);
            System.out.println(r.getName());
            System.out.println(r.getPwd());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试properties文件的加载
     */
    @Test
    public void Test2(){
        Properties pro = new Properties();
        try {
            pro.load(AppTest.class.getClassLoader().getResourceAsStream("config.properties"));
            System.out.println(pro.getProperty("IP"));
            System.out.println(pro.getProperty("PORT"));
            System.out.println(pro.getProperty("ZOOKEEPER"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
