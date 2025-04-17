package com.liu.example.provider;

import com.liu.example.common.service.UserService;
import com.liu.rpc.bootstrap.ProviderBootStrap;
import com.liu.rpc.config.RpcConfig;
import com.liu.rpc.model.ServiceInfoRegister;
import com.liu.rpc.utils.ConfigUtils;


import java.util.ArrayList;
import java.util.List;

public class ExampleProvider {
    public static void main(String[] args) {
        //配置初始化
        List<ServiceInfoRegister<?>> serviceInfoRegisterList = new ArrayList<>();
        ServiceInfoRegister<UserServiceImpl> register = new ServiceInfoRegister<>(UserService.class.getName(), UserServiceImpl.class);

        serviceInfoRegisterList.add(register);
        ProviderBootStrap.init(serviceInfoRegisterList);
    }
}
