package com.yupi.yubi_backend.manager;

import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AIManage {
    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChat(long biManage,String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1651468516836098050L);
        devChatRequest.setMessage("邓紫棋");
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if(response == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 响应数据异常");
        }
        System.out.println(response.getData().getContent());
        return response.getData().getContent();
    }
}

