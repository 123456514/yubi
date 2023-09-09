package com.yupi.yubi_backend.manager;

import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;


@Service
public class AIManage {

    public String doChat(long biManage,String message){
        String accessKey = "h199uglohqiidxu4ug2tjgc3f8aoloqx";
        String secretKey = "pmt7koz5z8agihc01ux0n5d28nc5ci8m";
        YuCongMingClient client = new YuCongMingClient(accessKey, secretKey);
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(biManage);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if(response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 响应数据异常");
        }
        System.out.println(response.getData().getContent());
        return response.getData().getContent();
    }
}
