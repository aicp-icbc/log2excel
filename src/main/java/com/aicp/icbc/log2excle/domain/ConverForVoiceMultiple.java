package com.aicp.icbc.log2excle.domain;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: liuxincheng01
 * @description:
 * @date：Created in 2019-08-22 18:09
 * @modified By liuxincheng01
 */
public class ConverForVoiceMultiple {

    private static final String regex = "\\{.+\\}";
    private static Pattern pattern = Pattern.compile(regex);

    public static void run(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("conversation.log")),
                    "UTF-8"));
            String lineTxt = null;
            List<Conversation> conversationList = new ArrayList<>();
            while ((lineTxt = br.readLine()) != null) {
                if (!"".equals(lineTxt)) {
                    Matcher match = pattern.matcher(lineTxt);
                    String matchText = "";
                    while (match.find()) {
                        matchText = match.group();
                    }
                    //转换为JSON字段
                    JSONObject perLineJsonObject = JSON.parseObject(matchText);
                    String botId= "";
                    String sessionId= "";
                    String queryTime= "";
                    String answerTime= "";
                    String queryText= "";
                    String channel= "";
                    Boolean solved= false;
                    Boolean firstDialogTurn= false;
                    String source= "";
                    String intentName= "";
                    String userId= "";
                    String dialog_node_name= "";
                    String extendQuestion= "";
                    //标答 -- 标准问
                    String standardQuestion = "";
                    //回复答案
                    String responseAnswer = "";
                    //澄清 -- 建议问
                    String suggestAnswer = "";
                    String welcome = "";

                    if (perLineJsonObject.containsKey("welcome")) {
                        welcome = (String) perLineJsonObject.get("welcome");
                    }
                    //botId
                    if (perLineJsonObject.containsKey("botId")) {
                        botId = (String) perLineJsonObject.get("botId");
                    }
                    //取sessionId
                    if (perLineJsonObject.containsKey("sessionId")) {
                        sessionId = (String) perLineJsonObject.get("sessionId");
                    }
                    //取询问时间
                    if (perLineJsonObject.containsKey("queryTime")) {
                        queryTime = perLineJsonObject.getString("queryTime");
                    }
                    //取回答时间
                    if (perLineJsonObject.containsKey("answerTime")) {
                        answerTime = perLineJsonObject.getString("answerTime");
                    }
                    //取询问问法
                    if (perLineJsonObject.containsKey("queryText")) {
                        queryText = perLineJsonObject.getString("queryText");
                    }
                    //取渠道
                    if (perLineJsonObject.containsKey("channel")) {
                        channel = ((String) perLineJsonObject.get("channel"));
                    }
                    //取渠道
                    if (perLineJsonObject.containsKey("solved")) {
                        solved = ((Boolean) perLineJsonObject.get("solved"));
                    }
                    //取渠道
                    if (perLineJsonObject.containsKey("firstDialogTurn")) {
                        firstDialogTurn = ((Boolean) perLineJsonObject.get("firstDialogTurn"));
                    }
                    //取建议回答
                    if (perLineJsonObject.containsKey("suggestAnswer")) {
                        suggestAnswer = perLineJsonObject.getString("suggestAnswer");
                    }
                    //取回复类型
                    if (perLineJsonObject.containsKey("source")) {
                        source = perLineJsonObject.getString("source");
                    }
                    //
                    if (perLineJsonObject.containsKey("intentName")) {
                        source = perLineJsonObject.getString("intentName");
                    }
                    //
                    if (perLineJsonObject.containsKey("ext")) {
                        JSONObject ext = perLineJsonObject.getJSONObject("ext");
                        if (ext.containsKey("userId")) {
                            userId = ext.getString("userId");
                        }
                    }
                    //
                    if (perLineJsonObject.containsKey("dialog_node_name")) {
                        dialog_node_name = perLineJsonObject.getString("dialog_node_name");
                    }
                    //
                    if (perLineJsonObject.containsKey("answer")) {
                        JSONObject answer = perLineJsonObject.getJSONObject("answer");
                        if (answer.containsKey("faq")) {
                            JSONObject faq = perLineJsonObject.getJSONObject("answer");
                            if (faq.containsKey("standardQuestion")) {
                                standardQuestion = perLineJsonObject.getString("standardQuestion");
                            }
                            if (faq.containsKey("extendQuestion")) {
                                extendQuestion = perLineJsonObject.getString("extendQuestion");
                            }
                        }
                    }

                    //-----------------设值---------------------
                    Conversation conversation = new Conversation();
                    conversation.setWelcome(welcome);
                    conversation.setBotId(botId);
                    //设置sessionId
                    conversation.setSessionId(sessionId);
                    //
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateQueryTime = new Date();
                    dateQueryTime.setTime(Long.valueOf(queryTime));
                    conversation.setQueryTime(formatter.format(dateQueryTime));
                    Date dateAnswerTime = new Date();
                    dateAnswerTime.setTime(Long.valueOf(answerTime));
                    conversation.setAnswerTime(formatter.format(dateAnswerTime));
                    //
                    conversation.setQueryText(queryText);
                    //
                    conversation.setChannel(channel);
                    //
                    conversation.setSolved(solved);
                    //
                    conversation.setFirstDialogTurn(firstDialogTurn);
                    //
                    conversation.setSuggestAnswer(suggestAnswer);
                    //
                    conversation.setSource(source);
                    //
                    conversation.setIntentName(intentName);
                    //
                    conversation.setUserId(userId);
                    //
                    conversation.setDialogNodeName(dialog_node_name);
                    //
                    conversation.setStandardQuestion(standardQuestion);
                    //
                    conversation.setExtendQuestion(extendQuestion);

                    //添加数组
                    conversationList.add(conversation);
                }
            }
            //根据sessionId对list进行排序
            List<Conversation> conversationSortList = new ArrayList<>();
            Integer talkNumSort = 0;
            String sessionIdSord = conversationList.get(0).getSessionId();
            String sessionIdOther = "--";
            List<Conversation> childList = new ArrayList<>();
            //循环判断  ---  迁移 list
            while (conversationList.size() > 0){
                //如果还有其它的sessionID
                if(!"--".equals(sessionIdOther)){
                    sessionIdSord = sessionIdOther;
                }
                Iterator<Conversation> iterator = conversationList.iterator();
                //从conversationList第一个元素开始
                while (iterator.hasNext()) {
                    Conversation perConversation = iterator.next();
                    if(sessionIdSord != null){
                        //判断是否为同一个sessionID--添加childList
                        if(sessionIdSord.equals(perConversation.getSessionId())){
                            perConversation.setTalkNum(talkNumSort);
                            childList.add(perConversation);
                            iterator.remove();
                        }else {
                            //如果sessionID 不同， 保存另外的sessionID作为下次list迁移的判断条件
                            sessionIdOther = perConversation.getSessionId();
                            talkNumSort ++;
                        }
                    }
                }
                //反序并添加
                Collections.reverse(childList);
                conversationSortList.addAll(childList);
                childList.clear();

            }
            Collections.reverse(conversationSortList);

            // 开始输出到excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            // 设置表头样式  // 竖向居中  // 横向居中 // 边框  //黄色
            XSSFCellStyle headStyle = workbook.createCellStyle();
            headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headStyle.setAlignment(HorizontalAlignment.CENTER);
            headStyle.setBorderBottom(BorderStyle.THIN);
            headStyle.setBorderLeft(BorderStyle.THIN);
            headStyle.setBorderRight(BorderStyle.THIN);
            headStyle.setBorderTop(BorderStyle.THIN);
            headStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            //设置单元格样式
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);


            //新建表头
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);

            sheet.setColumnWidth(0, 7 * 256);
            sheet.setColumnWidth(1, 38 * 256);
            sheet.setColumnWidth(2, 15 * 256);
            sheet.setColumnWidth(3, 20 * 256);
            sheet.setColumnWidth(4, 25 * 256);
            sheet.setColumnWidth(5, 25 * 256);
            sheet.setColumnWidth(6, 50 * 256);
            sheet.setColumnWidth(7, 15 * 256);
            sheet.setColumnWidth(8, 20 * 256);


            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("ID");
            row.createCell(2).setCellValue("电话号码");
            row.createCell(3).setCellValue("场景名称");
            row.createCell(4).setCellValue("时间");
            row.createCell(5).setCellValue("客户问题");
            row.createCell(6).setCellValue("返回答案");
            row.createCell(7).setCellValue("返回结果类型");
            row.createCell(8).setCellValue("触发的标准问或建议问");
            for (int i = 0; i < 9; i++) {
                if(row.getCell(i) != null){
                    row.getCell(i).setCellStyle(headStyle);
                }
            }
            int rowNum = 1;
            int outSerialNo = 1;
            int outPrintNum = 0;
            String outSessionID = conversationSortList.get(0).getSessionId();
            Boolean newTalk = true;
            //记录导出Excel中新会话的row起始结束 -- 合并序号
            Integer talkFromNum = 1;
            Integer talkEndNum = 1;


            //填充一个空的Conversation用于合并最后一次会话
            Conversation tempConversation = new Conversation();
            tempConversation.setQueryText("--temp--for--merge--");
            tempConversation.setEnter_top_node_name("--temp--for--merge--");
            tempConversation.setSessionId("--temp--for--merge--");
            conversationSortList.add(tempConversation);

            for (Conversation conversation : conversationSortList) {
                if(true){
                    //移除欢迎语对话 -- 询问字段问空
//                    if(!StringUtils.isEmpty(conversation.getQueryText())){
                    if(true){
                        if(!outSessionID.equals(conversation.getSessionId())){
                            outSerialNo ++;
                            outSessionID = conversation.getSessionId();
                            //进入新的对话 设置标识
                            newTalk = true;
                            //开启新会话 -- 合并上一次会话 -- 合并序号列单元格
                            //System.out.println((rowNum - 1) +" "+ outSerialNo + " " +talkFromNum+ " " +talkEndNum);
                            if((talkEndNum - talkFromNum) > 0){
                                sheet.addMergedRegion(new CellRangeAddress(talkFromNum, talkEndNum, 0, 0));
                                sheet.addMergedRegion(new CellRangeAddress(talkFromNum, talkEndNum, 1, 1));
                                sheet.addMergedRegion(new CellRangeAddress(talkFromNum, talkEndNum, 2, 2));
                            }
                            //开启新会话 -- 记录起始行号
                            talkFromNum = rowNum ;
                        }else {
                            //对话更新 更新会话结束的行号
                            talkEndNum = rowNum ;
                        }

                        //判断是否为填充列
                        if(!"--temp--for--merge--".equals(conversation.getQueryText())){
                            //新增一行记录
                            XSSFRow currRow = sheet.createRow(rowNum++);
                            if(newTalk){
                                //改变新会话标识
                                newTalk = false;
                                currRow.createCell(0).setCellValue(outSerialNo);
                            }
                            currRow.createCell(1).setCellValue(outSessionID);
                            currRow.createCell(2).setCellValue(conversation.getPhoneNum());
                            currRow.createCell(3).setCellValue(conversation.getEnter_top_node_name());
                            currRow.createCell(4).setCellValue(conversation.getTime());
                            currRow.createCell(5).setCellValue(conversation.getQueryText());
                            currRow.createCell(6).setCellValue(conversation.getResponseAnswer());
                            currRow.createCell(7).setCellValue(conversation.getSource());
                            currRow.createCell(8).setCellValue(conversation.getStandardQuestion());
                            //设置表格样式
                            for (int j = 0; j < 9 ; j++) {
                                if(currRow.getCell(j) != null){
                                    currRow.getCell(j).setCellStyle(cellStyle);
                                }
                            }
                        }
                    }
                }
                //打印进度条
                String tu = "";
                Integer scheduleNum = (new Double(((outPrintNum*1.0) / (conversationSortList.size())) * 100).intValue());
                Integer j = 0;
                for (; j < scheduleNum/5; j += 1) {
                    tu += "●";
                }
                for (; j < 20; j += 1) {
                    tu += "○";
                }
                if(outPrintNum == conversationSortList.size() - 1){
                    System.out.print("\r读取进度：" + 100  + "%\t" + "●●●●●●●●●●●●●●●●●●●●" + "\t" + conversationSortList.size() + "/" + conversationSortList.size() );
                }else {
                    System.out.print("\r读取进度：" + scheduleNum  + "%\t" + tu + "\t" + outPrintNum + "/" + conversationSortList.size());
                }
                outPrintNum ++;
            }
            System.out.println("\t" + "开始写入Excel");
            FileOutputStream fos = new FileOutputStream("conversation.xlsx");
            workbook.write(fos);
            try {
                br.close();
                fos.flush();
            }catch (Exception e){

            }finally {
                if(br != null){
                    br.close();
                }
                if (fos != null){
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    static class Conversation {

        private String botId;
        private String welcome;
        private String queryText;
        private String suggestAnswer;
        private String time;
        private String queryTime;
        private String answerTime;
        private Boolean solved;
        private Boolean firstDialogTurn;
        private String intentName;
        private String userId;
        private String dialogNodeName;
        private String extendQuestion;
        //顶层节点
        private String enter_top_node_name;
        private String sessionId;
        //回复类型
        private String source;
        private String channel;
        private int talkNum;
        //澄清触发的建议问
        private String confirm_questions;
        //标准问
        private String standardQuestion;

        //返回答案
        private String responseAnswer;

        private String phoneNum;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public int getTalkNum() {
            return talkNum;
        }

        public void setTalkNum(int talkNum) {
            this.talkNum = talkNum;
        }


        public String getWelcome() {
            return welcome;
        }

        public void setWelcome(String welcome) {
            this.welcome = welcome;
        }

        public String getQueryText() {
            return queryText;
        }

        public void setQueryText(String queryText) {
            this.queryText = queryText;
        }

        public String getSuggest_answer() {
            return suggestAnswer;
        }

        public void setSuggest_answer(String suggestAnswer) {
            this.suggestAnswer = suggestAnswer;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getEnter_top_node_name() {
            return enter_top_node_name;
        }

        public void setEnter_top_node_name(String enter_top_node_name) {
            this.enter_top_node_name = enter_top_node_name;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getConfirm_questions() {
            return confirm_questions;
        }

        public void setConfirm_questions(String confirm_questions) {
            this.confirm_questions = confirm_questions;
        }

        public String getStandardQuestion() {
            return standardQuestion;
        }

        public void setStandardQuestion(String standardQuestion) {
            this.standardQuestion = standardQuestion;
        }

        public String getResponseAnswer() {
            return responseAnswer;
        }

        public void setResponseAnswer(String responseAnswer) {
            this.responseAnswer = responseAnswer;
        }

        public String getPhoneNum() {
            return phoneNum;
        }

        public void setPhoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
        }

        public String getQueryTime() {
            return queryTime;
        }

        public void setQueryTime(String queryTime) {
            this.queryTime = queryTime;
        }

        public String getAnswerTime() {
            return answerTime;
        }

        public void setAnswerTime(String answerTime) {
            this.answerTime = answerTime;
        }

        public Boolean getSolved() {
            return solved;
        }

        public void setSolved(Boolean solved) {
            this.solved = solved;
        }

        public Boolean getFirstDialogTurn() {
            return firstDialogTurn;
        }

        public void setFirstDialogTurn(Boolean firstDialogTurn) {
            this.firstDialogTurn = firstDialogTurn;
        }

        public String getIntentName() {
            return intentName;
        }

        public void setIntentName(String intentName) {
            this.intentName = intentName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getDialogNodeName() {
            return dialogNodeName;
        }

        public void setDialogNodeName(String dialogNodeName) {
            this.dialogNodeName = dialogNodeName;
        }

        public String getExtendQuestion() {
            return extendQuestion;
        }

        public void setExtendQuestion(String extendQuestion) {
            this.extendQuestion = extendQuestion;
        }

        public String getBotId() {
            return botId;
        }

        public void setBotId(String botId) {
            this.botId = botId;
        }

        public String getSuggestAnswer() {
            return suggestAnswer;
        }

        public void setSuggestAnswer(String suggestAnswer) {
            this.suggestAnswer = suggestAnswer;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }
    }


}
