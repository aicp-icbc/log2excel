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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: liuxincheng01
 * @description:
 * @date：Created in 2019-08-22 18:09
 * @modified By liuxincheng01
 */
public class ConverForVoiceMultipleInCall {

    private static final String regex = "\\{.+\\}";
    private static Pattern pattern = Pattern.compile(regex);

    public static void run(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("conversation.log")),
                    "UTF-8"));
            String lineTxt = null;
            List<Conversation> conversationList = new ArrayList<>();
            //获取配置文件信息
            //获取token
            String tokenFileName = "log2excel.conf";
            String timeBegin = "2019-09-18 07:54:21";
            String timeEnd = "2019-09-18 23:54:21";
            List<String> actionBank = null;
            List<String> actionBankMean = null;
            FileReader fr = null;
            try {

                try {
                    fr = new FileReader(tokenFileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedReader buff = new BufferedReader(fr);
                List<String> list = buff.lines().collect(Collectors.toList());
                timeBegin = list.get(0);
                timeEnd = list.get(1);
                actionBank = Arrays.asList(list.get(2).split(","));
                actionBankMean = Arrays.asList(list.get(3).split(","));
                try {
                    buff.close();
                    fr.close();
                } catch (IOException e) {

                }
            }catch (Exception e1){

            }

            while ((lineTxt = br.readLine()) != null) {
                if (!"".equals(lineTxt)) {
                    Matcher match = pattern.matcher(lineTxt);
                    String matchText = "";
                    while (match.find()) {
                        matchText = match.group();
                    }
                    //转换为JSON字段
                    JSONObject perLineJsonObject = JSON.parseObject(matchText);
                    String welcome = "";
                    String query_text = "";
                    String suggest_answer = "";
                    String enter_top_node_name = "";
                    String session_id = "";
                    String phoneNum = "";
                    String source = "";
                    String actionStr = "";
                    //用户按键输入值
                    String InputKeys = "";
                    //标答 -- 标准问
                    String standardQuestion = "";
                    //回复答案
                    String responseAnswer = "";
                    //澄清 -- 建议问
                    String suggested_questions = "";
                    //agent_id
                    String agent_id = "";

                    //取session_id
                    if (perLineJsonObject.containsKey("session_id")) {
                        session_id = (String) perLineJsonObject.get("session_id");
                    }
                    //取电话号码
                    if (perLineJsonObject.containsKey("channel")) {
                        String phoneNumStr = ((String) perLineJsonObject.get("channel"));
                        if(!StringUtils.isEmpty(phoneNumStr) && phoneNumStr.length() >= 7){
                            phoneNum = phoneNumStr.replace("IVRGIL-","");
                        }else {
                            phoneNum = phoneNumStr;
                        }
                    }
                    //外呼字段取电话号码
                    if (perLineJsonObject.containsKey("answer")) {
                        JSONObject answer =  perLineJsonObject.getJSONObject("answer");
                        if(answer!= null && answer.containsKey("context")){
                            JSONObject context =  answer.getJSONObject("context");
                            if(context!= null && context.containsKey("sSerialNo")){
                                String phoneNumStrOutCall = ((String) context.get("sSerialNo"));
                                if(!StringUtils.isEmpty(phoneNumStrOutCall) && phoneNumStrOutCall.length() >= 7){
                                    if(phoneNumStrOutCall.indexOf("IVRGIL-93090")> 0){
                                        phoneNum = phoneNumStrOutCall.replace("IVRGIL-93090", "");
                                    }else {
                                        phoneNum = phoneNumStrOutCall.substring(phoneNumStrOutCall.length() - 11, phoneNumStrOutCall.length());
                                    }
                                }else if(!StringUtils.isEmpty(phoneNumStrOutCall)){
                                    phoneNum = phoneNumStrOutCall;
                                }
                            }
                        }
                    }
                    //取欢迎语
                    if (perLineJsonObject.containsKey("welcome")) {
                        welcome = perLineJsonObject.getString("welcome");
                    }
                    //取询问问法
                    if (perLineJsonObject.containsKey("query_text")) {
                        query_text = perLineJsonObject.getString("query_text");
                    }
                    //取建议回答
                    if (perLineJsonObject.containsKey("suggest_answer")) {
                        suggest_answer = perLineJsonObject.getString("suggest_answer");
                    }
                    //取命中标准问
                    if (perLineJsonObject.containsKey("answer")) {
                        JSONObject answer = perLineJsonObject.getJSONObject("answer");
                        if(answer!= null && answer.containsKey("standardQuestion")){
                            standardQuestion = answer.getString("standardQuestion");
                        }
                    }


                    //命中澄清 --- 取建议问 -- 没有回复
                    if (perLineJsonObject.containsKey("confirm_questions")) {
                        //澄清 -- 建议问题们
                        JSONArray confirm_questions = perLineJsonObject.getJSONArray("confirm_questions");
                        for (int i = 0; i < confirm_questions.size(); i++) {
                            if(i == 0){
                                suggested_questions += ((JSONObject)confirm_questions.get(i)).getString("question");
                            }else {
                                suggested_questions += "、" + ((JSONObject)confirm_questions.get(i)).getString("question");
                            }
                        }

                    }
                    //取场景名
                    if (perLineJsonObject.containsKey("enter_top_node_name")) {
                        enter_top_node_name = perLineJsonObject.getString("enter_top_node_name").trim().replaceAll(" ","");
                    }
                    //取场景名
                    if (perLineJsonObject.containsKey("agent_id")) {
                        agent_id = (String) perLineJsonObject.get("agent_id");
//                        System.out.println(agent_id);
                    }
                    //取回复类型
                    if (perLineJsonObject.containsKey("source")) {
                        source = perLineJsonObject.getString("source");
                    }

                    //取多轮会话中的指令
                    if ("task_based".equals(source)) {
                        if (perLineJsonObject.containsKey("answer")) {
                            //取最外围answer
                            JSONObject answer = perLineJsonObject.getJSONObject("answer");
                            if (answer != null && answer.containsKey("answer")) {
                                //取JSON数组
                                JSONArray answerArray = answer.getJSONArray("answer");
                                if (answerArray.size() > 0) {
                                    //取单个answer
                                    for (int ai = 0; ai < answerArray.size(); ai++) {
                                        JSONObject perAnswer = answerArray.getJSONObject(ai);
                                        //获取action
                                        if (perAnswer != null && perAnswer.containsKey("action")) {
                                            if (!StringUtils.isEmpty(perAnswer.getString("action"))) {
                                                String actionName = perAnswer.getString("action");
                                                //判断是否为行方指令
                                                if (actionBank.contains(actionName)) {
                                                    String actionMeanStr = actionBankMean.get(actionBank.indexOf(actionName));
                                                    if (StringUtils.isEmpty(actionStr)) {
                                                        actionStr += actionName;
                                                        //设置指令名称
                                                        if((!StringUtils.isEmpty(actionMeanStr)) && (!" ".equals(actionMeanStr))&& (!"-".equals(actionMeanStr))){
                                                            actionStr += "(" + actionMeanStr +")";
                                                        }
                                                    } else {
                                                        actionStr += "、" + actionName;
                                                        //设置指令名称
                                                        if((!StringUtils.isEmpty(actionMeanStr)) && (!" ".equals(actionMeanStr))&& (!"-".equals(actionMeanStr))){
                                                            actionStr += "(" + actionMeanStr +")";
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //取多轮对话中用户按键输入值
                    if ("task_based".equals(source)) {
                        if (perLineJsonObject.containsKey("answer")) {
                            //取最外围answer
                            JSONObject answer = perLineJsonObject.getJSONObject("answer");
                            if (answer != null && answer.containsKey("context")) {
                                //取context域
                                JSONObject context = answer.getJSONObject("context");
                                if (context != null && context.containsKey("InputKeys")) {
                                    //取用户按键输入的信息
                                    InputKeys = context.getString("InputKeys");
//                                    System.out.println(InputKeys);
                                }
                            }
                        }
                    }

                    //-----------------设值---------------------
                    Conversation conversation = new Conversation();
                    //设置session_id
                    conversation.setSession_id(session_id);
                    //设置电话号码
                    conversation.setPhoneNum(phoneNum);
                    //欢迎语
                    conversation.setWelcome(welcome);
                    //设置时间
                    conversation.setTime(perLineJsonObject.getString("answer_time"));
                    //设置场景名
                    if(!StringUtils.isEmpty(agent_id)){
                        if(agent_id.equals("38f7f7a0-b321-40e2-bb21-a6939124b02a")){
                            enter_top_node_name = "外呼_账单分期";
//                            System.out.println(enter_top_node_name);
                        }
                        if(agent_id.equals("72653e5f-25ce-4cb2-91dc-7b52113f8592")){
                            enter_top_node_name = "外呼_信用卡催收";
//                            System.out.println(enter_top_node_name);
                        }
                    }
                    conversation.setEnter_top_node_name(enter_top_node_name);
                    //设置询问问法 -- 用户输入为空
                    if(StringUtils.isEmpty(query_text)){
                        //-- inputKeys不空
                        if(!StringUtils.isEmpty(InputKeys)){
                            conversation.setQuery_text(InputKeys);
                        }else {
                            conversation.setQuery_text("");
                        }

                    }else {
                        conversation.setQuery_text(query_text);
                    }

                    //设置触发的标准问或者建议问
                    if(!StringUtils.isEmpty(suggest_answer)){
                        //非澄清 -- 返回命中标准问
                        conversation.setStandardQuestion(standardQuestion);
                    }else if(!StringUtils.isEmpty(suggested_questions)){
                        //澄清 -- 返回空答案
                        conversation.setStandardQuestion(suggested_questions);
                    }

                    //设置回答字段 -- 区别澄清问答
                    if(!StringUtils.isEmpty(suggest_answer)){
                        //非澄清 -- 返回建议回答
                        conversation.setResponseAnswer(suggest_answer);
                    }else if(!StringUtils.isEmpty(suggested_questions)){
                        //澄清 -- 返回空答案
                        conversation.setResponseAnswer("");
                    }
                    //设置命中行方指令
                    conversation.setActionStr(actionStr);
                    //设置用户输入信息
                    conversation.setInputKeys(InputKeys);

                    //转换回复类型
                    if(!StringUtils.isEmpty(source)){
                        if("task_based".equals(source)){
                            source = "多轮会话";
                        }
                        if("faq".equals(source)){
                            source = "标准回复";
                        }
                        if("chitchat".equals(source)){
                            source = "闲聊";
                        }
                        if("clarity".equals(source)){
                            source = "建议问";
                        }
                        if("none".equals(source)){
                            //子回复类型  -- 建议问 -- 默认回复
                            if(!StringUtils.isEmpty(suggested_questions)){
                                source = "建议问";
                            }else {
                                source = "默认回复";
                            }

                        }

                    }
                    conversation.setSource(source);

                    //添加数组
                    conversationList.add(conversation);
                }
            }
            //根据session_id对list进行排序
            List<Conversation> conversationSortList = new ArrayList<>();
            Integer talkNumSort = 0;
            String sessionIdSord = conversationList.get(0).getSession_id();
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
                        if(sessionIdSord.equals(perConversation.getSession_id())){
                            perConversation.setTalkNum(talkNumSort);
                            childList.add(perConversation);
                            iterator.remove();
                        }else {
                            //如果sessionID 不同， 保存另外的sessionID作为下次list迁移的判断条件
                            sessionIdOther = perConversation.getSession_id();
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
            //设置单元格样式  --- 空文本
            XSSFCellStyle cellStyleEmpty = workbook.createCellStyle();
            cellStyleEmpty.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyleEmpty.setAlignment(HorizontalAlignment.CENTER);
            cellStyleEmpty.setBorderBottom(BorderStyle.THIN);
            cellStyleEmpty.setBorderLeft(BorderStyle.THIN);
            cellStyleEmpty.setBorderRight(BorderStyle.THIN);
            cellStyleEmpty.setBorderTop(BorderStyle.THIN);
            cellStyleEmpty.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            cellStyleEmpty.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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
            sheet.setColumnWidth(7, 30 * 256);
            sheet.setColumnWidth(8, 20 * 256);
            sheet.setColumnWidth(9, 20 * 256);


            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("ID");
            row.createCell(2).setCellValue("电话号码");
            row.createCell(3).setCellValue("场景名称");
            row.createCell(4).setCellValue("时间");
            row.createCell(5).setCellValue("客户问题");
            row.createCell(6).setCellValue("返回答案");
            row.createCell(7).setCellValue("触发IVR指令");
            row.createCell(8).setCellValue("返回结果类型");
            row.createCell(9).setCellValue("触发的标准问或建议问");
            for (int i = 0; i < 10; i++) {
                if(row.getCell(i) != null){
                    row.getCell(i).setCellStyle(headStyle);
                }
            }
            int rowNum = 1;
            int outSerialNo = 0;
            int outPrintNum = 0;
            String outSessionID = "--temp--for--begin--";
            Boolean newTalk = true;
            //记录导出Excel中新会话的row起始结束 -- 合并序号
            Integer talkFromNum = 1;
            Integer talkEndNum = 1;


            //填充一个空的Conversation用于合并最后一次会话
            Conversation tempConversation = new Conversation();
            tempConversation.setQuery_text("--temp--for--merge--");
            tempConversation.setEnter_top_node_name("--temp--for--merge--");
            tempConversation.setSession_id("--temp--for--merge--");
            conversationSortList.add(tempConversation);

            for (Conversation conversation : conversationSortList) {
                if(true){
                    //移除欢迎语对话 -- 询问字段问空
//                    if(!StringUtils.isEmpty(conversation.getQuery_text())){
                    LocalDateTime dateTimeOut = null;
//                    LocalDateTime dateTimeIn = LocalDateTime.parse("2019-09-18 12:54:21", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime dateTimeBegin = LocalDateTime.parse(timeBegin, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    LocalDateTime dateTimeEnd = LocalDateTime.parse(timeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    try {
                        dateTimeOut = LocalDateTime.parse(conversation.getTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }catch (Exception e){
                        dateTimeOut = LocalDateTime.parse("2019-09-18 12:55:21",DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                    if(dateTimeOut.isAfter(dateTimeBegin) && dateTimeOut.isBefore(dateTimeEnd)){
//                    if(true){
                        if(!outSessionID.equals(conversation.getSession_id())){
                            outSerialNo ++;
                            outSessionID = conversation.getSession_id();
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
                        if(!"--temp--for--merge--".equals(conversation.getQuery_text())){
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
//                            currRow.createCell(5).setCellValue(conversation.getQuery_text());
                            currRow.createCell(5).setCellValue(conversation.getQuery_text());
                            currRow.createCell(6).setCellValue(conversation.getResponseAnswer());
                            currRow.createCell(7).setCellValue(conversation.getActionStr());
                            currRow.createCell(8).setCellValue(conversation.getSource());
                            currRow.createCell(9).setCellValue(conversation.getStandardQuestion());
                            //设置表格样式
                            for (int j = 0; j < 10 ; j++) {
                                if(currRow.getCell(j) != null){
                                    currRow.getCell(j).setCellStyle(cellStyle);
                                }
                            }
                            //设置 返回答案 抱歉，我没听清，您能再说一遍吗？ 为绿底
                            if("抱歉，我没听清，您能再说一遍吗？".equals(conversation.getResponseAnswer())){
                                currRow.getCell(6).setCellStyle(cellStyleEmpty);
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

        private String welcome;
        private String query_text;
        private String suggest_answer;
        private String time;
        //顶层节点
        private String enter_top_node_name;
        private String session_id;
        //回复类型
        private String source;
        private int talkNum;
        //澄清触发的建议问
        private String confirm_questions;
        //标准问
        private String standardQuestion;
        //返回答案
        private String responseAnswer;
        //电话号码
        private String phoneNum;
        //action -- 指令名称
        private String actionStr;
        //用户输入信息
        private String InputKeys;


        public String getSession_id() {
            return session_id;
        }

        public void setSession_id(String session_id) {
            this.session_id = session_id;
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

        public String getQuery_text() {
            return query_text;
        }

        public void setQuery_text(String query_text) {
            this.query_text = query_text;
        }

        public String getSuggest_answer() {
            return suggest_answer;
        }

        public void setSuggest_answer(String suggest_answer) {
            this.suggest_answer = suggest_answer;
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

        public String getActionStr() {
            return actionStr;
        }

        public void setActionStr(String actionStr) {
            this.actionStr = actionStr;
        }

        public String getInputKeys() {
            return InputKeys;
        }

        public void setInputKeys(String inputKeys) {
            InputKeys = inputKeys;
        }
    }


}
