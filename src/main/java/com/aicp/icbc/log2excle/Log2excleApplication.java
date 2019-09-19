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
//        ConverForVoiceSample.run(args);
//        ConverForVoiceMultiple.run(args);
//        ConverForVoiceMultipleOutCall.run(args);
        ConverForVoiceMultipleInCall.run(args);
        System.out.println("\n完----------------------------------日志文件转写-----------------------------------成");
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
