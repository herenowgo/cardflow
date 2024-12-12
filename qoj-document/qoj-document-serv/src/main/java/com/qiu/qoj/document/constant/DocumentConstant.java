package com.qiu.qoj.document.constant;

import java.util.HashSet;

public interface DocumentConstant {
    String USER_ID = "userId";
    Long MAX_FILE_SIZE = 1024 * 50L;
    HashSet<String> ALLOW_FILE_TYPE = new HashSet<String>() {{
        add("txt");
        add("pdf");
        add("doc");
        add("docx");
        add("ppt");
        add("pptx");
        add("xls");
        add("xlsx");
        // 图片
        add("jpg");
        add("png");
        add("jpeg");
    }};
    String USER_PREFIX = "user/";
}
