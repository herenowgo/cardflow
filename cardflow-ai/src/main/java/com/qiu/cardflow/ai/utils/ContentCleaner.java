package com.qiu.cardflow.ai.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ContentCleaner {
    public static String cleanHtmlContent(String content) {
        if (content == null) {
            return "";
        }

        // 使用Jsoup解析HTML并保留换行
        Document doc = Jsoup.parse(content);
        doc.outputSettings(new Document.OutputSettings().prettyPrint(false)); // 禁用pretty print
        doc.select("br").before("\\n"); // 在<br>前添加换行
        doc.select("p").before("\\n"); // 在<p>前添加换行
        doc.select("code").before("\\n"); // 在<code>前添加换行

        String plainText = doc.text() // 获取文本内容
                .replace("\\n", "\n") // 将占位符替换为实际换行符
                .trim();

        return plainText;
    }
}
