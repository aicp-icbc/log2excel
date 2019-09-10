package com.aicp.icbc.log2excle;

import com.aicp.icbc.log2excle.domain.ConversationRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.Thread.*;

@SpringBootApplication
public class Log2excleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Log2excleApplication.class, args);
        System.out.println("开----------------------------------日志文件转写-----------------------------------始");
        ConversationRecord.run(args);
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("完----------------------------------日志文件转写-----------------------------------成");
    }

}
