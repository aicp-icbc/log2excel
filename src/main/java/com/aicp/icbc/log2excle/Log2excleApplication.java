package com.aicp.icbc.log2excle;

import com.aicp.icbc.log2excle.domain.ConverForVoiceMultiple;
import com.aicp.icbc.log2excle.domain.ConverForVoiceMultipleInCall;
import com.aicp.icbc.log2excle.domain.ConverForVoiceMultipleOutCall;
import com.aicp.icbc.log2excle.domain.ConverForVoiceSample;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.Thread.*;

@SpringBootApplication
public class Log2excleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Log2excleApplication.class, args);
        System.out.println("开----------------------------------日志文件转写-----------------------------------始\n");
        String tpye = ConverForVoiceMultipleInCall.type;
        if("1".equals(tpye)){
            //呼入
            ConverForVoiceMultipleInCall.run(args);
        }
        if("2".equals(tpye)){
            //外呼
            ConverForVoiceMultipleOutCall.run(args);
        }
        if("3".equals(tpye)){
            //多轮语音
            ConverForVoiceMultiple.run(args);
        }
        if("4".equals(tpye)){
            //单轮语音
            ConverForVoiceSample.run(args);
        }



        System.out.println("\n完----------------------------------日志文件转写-----------------------------------成");
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
