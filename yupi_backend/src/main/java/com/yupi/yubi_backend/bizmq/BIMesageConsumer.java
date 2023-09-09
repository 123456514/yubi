package com.yupi.yubi_backend.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.constant.BIConstant;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yubi_backend.manager.AIManage;
import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.model.enums.ChartStatusEnums;
import com.yupi.yubi_backend.service.ChartService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BIMesageConsumer {
    /**
     *  接收消息的方法
     * @param message 接收到的消息内容，是一个字符串类型
     * @param channel 消息所在的通道，可以通过该通道与RabbitMQ进行交互，例如手动确认消息
     * @param deliveryTag 消息的投递标签，用于唯一便是一条信息
     */
    @Resource
    private ChartService chartService;
    @Resource
    private AIManage aiManage;
    @SneakyThrows
    @RabbitListener(queues = {BIMqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        //校验message
        log.info("receiveMessage message = {}",message);
        if(StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if(chart == null){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息为空");
        }

        // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.running));
        if (!chartService.updateById(updateChart)) {
            log.info("chartId = {},更新图表执行中状态失败",chart.getId());
            return;
        }
        // 调用 AI
        String result = aiManage.doChat(BIConstant.BI_MODEL_ID,buildUserInput(chart));
        String[] splits = result.split(BIConstant.BI_SPLIT_STR);
        if (splits.length < 3) {
            log.info("chartId = {},AI生成错误",chart.getId());
            return;
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.succeed));
        if (!chartService.updateById(updateChartResult)) {
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表失败状态失败");
            log.info("chartId = {},更新图表执行中状态失败",chart.getId());
        }
        //确认消息
        channel.basicAck(deliveryTag,false);
    }
    private String buildUserInput(Chart chart){
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }
    public void handleChartUpdateError(long chartId,String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChartResult);
        if(!b){
            log.error("跟新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
