package com.fredx;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * 描述：原来是本地服务方法，现在要发布成RPC方法
 *
 */
public class UserServiceImpl extends UserServiceProto.UserServiceRpc {  //先继承一下这个类，然后重写一下login方法
    public boolean login(String name, String pwd){
        System.out.println("call UserServiceImpl -> login");
        System.out.println("name:" + name);
        System.out.println("pwd:" + pwd);
        return true;
    }

    public boolean reg(String name, String pwd, int age, String sex, String phone){
        System.out.println("call UserServiceImpl -> reg");
        System.out.println("name:" + name);
        System.out.println("pwd:" + pwd);
        System.out.println("age:" + age);
        System.out.println("sex:" + sex);
        System.out.println("phone" + phone);


        return true;
    }

    /**
     * login的rpc代理方法
     * @param controller  可以接收方法的执行状态  先忽略
     * @param request
     * @param done
     */
    @Override
    public void login(RpcController controller, UserServiceProto.LoginRequest request, RpcCallback<UserServiceProto.Response> done) {
        //把一个本地方法变成rpc方法，仅需以下四步：
        //1.从request里面读取到远程rpc调用的参数
        String name = request.getName();
        String pwd = request.getPwd();
        //2.根据解析的参数，做本地业务
        boolean result = login(name, pwd);

        //3.填写方法的响应值
        UserServiceProto.Response.Builder response_builder = UserServiceProto.Response.newBuilder();
        response_builder.setErrno(0);   //假设一切正常
        response_builder.setErrinfo("");
        response_builder.setResult(result);

        //4.把response对象给到nprpc框架，由框架负责处理发送rpc调用响应值
        done.run(response_builder.build());

    }

    @Override
    public void reg(RpcController controller, UserServiceProto.RegRequest request, RpcCallback<UserServiceProto.Response> done) {

    }
}
