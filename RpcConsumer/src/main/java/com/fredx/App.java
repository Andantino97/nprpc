package com.fredx;

import com.fredx.consumer.RpcConsumer;
import com.fredx.controller.NrpcController;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        /**
         * 模拟rpc方法调用者
         */
        UserServiceProto.UserServiceRpc.Stub stub = UserServiceProto.UserServiceRpc.newStub(new RpcConsumer("config.properties"));
        UserServiceProto.LoginRequest.Builder login_builder = UserServiceProto.LoginRequest.newBuilder();
        login_builder.setName("zhang san");
        login_builder.setPwd("888888");
        NrpcController con = new NrpcController();
        stub.login(con, login_builder.build(), response->{
            /**
             * 这里就是rpc调用的返回值
             */
            if(con.failed()){ //Rpc方法没有调用成功
                System.out.println(con.errorText());
            }
            System.out.println("receive rpc call response");
            if(response.getErrno() == 0){ //调用正常
                System.out.println(response.getResult());
            }else{//调用出错
                System.out.println(response.getErrinfo());
            }
        });
    }
}
