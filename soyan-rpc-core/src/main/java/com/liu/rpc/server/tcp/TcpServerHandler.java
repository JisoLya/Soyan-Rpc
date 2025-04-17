package com.liu.rpc.server.tcp;

import com.liu.rpc.model.RpcRequest;
import com.liu.rpc.model.RpcResponse;
import com.liu.rpc.protocol.*;
import com.liu.rpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            //接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("Server:消息解码错误");
            }
            //获取请求
            RpcRequest request = protocolMessage.getBody();
            //处理请求,构造响应对象
            RpcResponse response = new RpcResponse();
            try {
                String serviceName = request.getServiceName();
                Class<?> implClass = LocalRegistry.get(serviceName);
                Method method = implClass.getMethod(request.getMethodName(), request.getParamTypes());
                Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), request.getParams());

                response.setData(result);
                response.setMessage("ok");
                response.setDataType(method.getReturnType());

            } catch (Exception e) {
                e.printStackTrace();
                response.setException(e);
                response.setMessage(e.getMessage());
            }

            //响应
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            //封装响应信息
            ProtocolMessage<RpcResponse> responseMessage = new ProtocolMessage<>(header, response);
            try {
                //返回数据
                Buffer encode = ProtocolMessageEncoder.encode(responseMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("Server:协议返回编码错误！");
            }
        });
        netSocket.handler(tcpBufferHandlerWrapper);

        /*
         netSocket.handler(
         buffer -> {
         //接受请求，解码
         ProtocolMessage<RpcRequest> protocolMessage;
         try {
         protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
         } catch (IOException e) {
         throw new RuntimeException("Server:消息解码错误");
         }
         //获取请求
         RpcRequest request = protocolMessage.getBody();
         //处理请求,构造响应对象
         RpcResponse response = new RpcResponse();
         try {
         String serviceName = request.getServiceName();
         Class<?> implClass = LocalRegistry.get(serviceName);
         Method method = implClass.getMethod(request.getMethodName(),request.getParamTypes());
         Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), request.getParams());

         response.setData(result);
         response.setMessage("ok");
         response.setDataType(method.getReturnType());

         } catch (Exception e) {
         e.printStackTrace();
         response.setException(e);
         response.setMessage(e.getMessage());
         }

         //响应
         ProtocolMessage.Header header = protocolMessage.getHeader();
         header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
         //封装响应信息
         ProtocolMessage<RpcResponse> responseMessage = new ProtocolMessage<>(header, response);
         try{
         //返回数据
         Buffer encode = ProtocolMessageEncoder.encode(responseMessage);
         netSocket.write(encode);
         } catch (IOException e) {
         throw new RuntimeException("Server:协议返回编码错误！");
         }
         }
         );
         }
         */
    }
}
