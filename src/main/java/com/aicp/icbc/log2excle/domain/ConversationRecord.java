package com.aicp.icbc.log2excle.domain;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: liuxincheng01
 * @description:
 * @date：Created in 2019-08-22 18:09
 * @modified By liuxincheng01
 */
public class ConversationRecord {

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
                    JSONObject currConversation = JSON.parseObject(matchText);
                    String welcome = "";
                    String query_text = "";
                    String suggest_answer = "";
                    String clarify_questions = "";
                    String enter_top_node_name = "";
                    String session_id = "";
                    String source = "";
                    if (currConversation.containsKey("session_id")) {
                        session_id = (String) currConversation.get("session_id");
                    }
                    if (currConversation.containsKey("welcome")) {
                        welcome = currConversation.getString("welcome");
                    }
                    if (currConversation.containsKey("query_text")) {
                        query_text = currConversation.getString("query_text");
                    }
                    if (currConversation.containsKey("suggest_answer")) {
                        suggest_answer = currConversation.getString("suggest_answer");
                    }
                    if (currConversation.containsKey("clarify_questions")) {
                        JSONObject clarifyQuestions = (JSONObject) currConversation.get("clarify_questions");
                        JSONObject voice = (JSONObject) clarifyQuestions.get("voice");
                        String content = voice.getString("content");
                        clarify_questions = content;
                    }
                    if (currConversation.containsKey("enter_top_node_name")) {
                        enter_top_node_name = currConversation.getString("enter_top_node_name");
                    }
                    //取回复类型
                    if (currConversation.containsKey("source")) {
                        source = currConversation.getString("source");
                    }
                    Conversation conversation = new Conversation();
                    conversation.setWelcome(welcome);
                    conversation.setQuery_text(query_text);
                    //设置回答字段 -- 区别澄清问答
                    if(!StringUtils.isEmpty(suggest_answer)){
                        conversation.setSuggest_answer(suggest_answer);
                    }else if(!StringUtils.isEmpty(clarify_questions)){
                        conversation.setSuggest_answer(clarify_questions);
                    }
                    conversation.setTime(currConversation.getString("answer_time"));
                    conversation.setEnter_top_node_name(enter_top_node_name);
                    conversation.setSession_id(session_id);
                    //转换回复类型
                    if(!StringUtils.isEmpty(source)){
                        if("task_based".equals(source)){
                            source = "多轮会话";
                        }
                        if("FAQ".equals(source)){
                            source = "单轮问答";
                        }
                        if("chitchat".equals(source)){
                            source = "闲聊";
                        }
                        if("none".equals(source)){
                            //子回复类型  -- 建议问
                            if(!StringUtils.isEmpty(clarify_questions)){
                                source = "建议问";
                            }else {
                                source = "默认回复";
                            }

                        }

                    }
                    conversation.setSource(source);
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
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow row = sheet.createRow(0);
            sheet.setColumnWidth(1, 25 * 256);
            sheet.setColumnWidth(2, 40 * 256);
            sheet.setColumnWidth(3, 25 * 256);
            sheet.setColumnWidth(4, 25 * 256);
            sheet.setColumnWidth(5, 10 * 256);
            sheet.setColumnWidth(6, 220 * 256);

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            row.createCell(0).setCellValue("序号");
            row.createCell(1).setCellValue("场景");
            row.createCell(2).setCellValue("ID");
            row.createCell(3).setCellValue("时间");
            row.createCell(4).setCellValue("客户问题");
            row.createCell(5).setCellValue("回复类型");
            row.createCell(6).setCellValue("返回答案");
            int rowNum = 1;
            int serialNo = 0;
            int outSerialNo = 0;
            int keepNo = 52;
            int outPrintNum = 0;
            String outSessionID = conversationSortList.get(0).getSession_id();
            Boolean newTalk = true;
//            for (Conversation conversation : conversationList) {
            for (Conversation conversation : conversationSortList) {
//                if (!"".equals(conversation.getWelcome())) {
//                    serialNo++;
//                }
//                if(("2019-09-05 10:56:00").compareTo(conversation.getTime()) <= 0){
                if(true){
                    if(!StringUtils.isEmpty(conversation.getQuery_text())){
                        XSSFRow currRow = sheet.createRow(rowNum++);
                        if(!outSessionID.equals(conversation.getSession_id())){
                            outSerialNo ++;
                            outSessionID = conversation.getSession_id();
                            newTalk = true;
                        }
                        if(newTalk){
                            currRow.createCell(0).setCellValue(outSerialNo);
                            newTalk = false;
                        }
                        currRow.createCell(1).setCellValue(conversation.getEnter_top_node_name());
                        currRow.createCell(2).setCellValue(conversation.getSession_id());
                        currRow.createCell(3).setCellValue(conversation.getTime());
                        currRow.createCell(4).setCellValue("".equals(conversation.getQuery_text())?"":conversation.getQuery_text());
                        currRow.createCell(5).setCellValue(conversation.getSource());
                        currRow.createCell(6).setCellValue("".equals(conversation.getSuggest_answer())?conversation.getWelcome():conversation.getSuggest_answer());
                        //
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
                    System.out.print("\r读取进度：" + scheduleNum  + "%\t" + "●●●●●●●●●●●●●●●●●●●●" + "\t" + conversationSortList.size() + "/" + conversationSortList.size() );
                }else {
                    System.out.print("\r读取进度：" + scheduleNum  + "%\t" + tu + "\t" + outPrintNum + "/" + conversationSortList.size());
                }
                outPrintNum ++;
            }
            System.out.println("\t" + "开始写入Excel");
            FileOutputStream fos = new FileOutputStream("conversation.xlsx");
            workbook.write(fos);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("read errors :" + e);
        }

    }
    static class Conversation {

        private String welcome;
        private String query_text;
        private String suggest_answer;
        private String time;
        private String enter_top_node_name;
        private String session_id;
        private String source;
        private int talkNum;
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
    }

}
