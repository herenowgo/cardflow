package com.qiu.cardflow.ai.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ChatControllerTest {


    @Value("${spring.ai.zhipuai.api-key}")
    private String zhiPuAiApiToken;

    @Autowired
    private ZhiPuAiChatModel chatModel;


    @Test
    void testClient() {
        System.out.println(chatModel);
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        System.out.println(chatClient);

        System.out.println(chatClient.prompt()
                .user("Q: 对于在业务上能保证数据唯一性的数据表，使用InnoDB 的普通索引和唯一索引在查询时的性能差距有多大？\n" +
                        "A: 答：没有多大\n" +
                        "1. **磁盘**上的 IO 操作是一样的：都需要从磁盘把数据页读取到内存中\n" +
                        "2. 在**内存**中的操作有所不同：普通索引读取到符合条件的数据后，还会再往后读取数据直到不符合条件（耗时非常少）。如果读到了数据页的末尾，还要从磁盘读取下一个数据页（可能性比较小，一个数据页一般可以存 1000 条记录）。我的理解对吗？")
                .call().content());

        ;
    }


//    @Test
//    void testCards() {
//        System.out.println(chatModel);
//        ChatClient chatClient = ChatClient.builder(chatModel).build();
//        System.out.println(chatClient);
//
//        Cards cards = chatClient.prompt()
//                .options(ZhiPuAiChatOptions.builder().withModel("glm-4-flash").build())
//                .system("# 角色：  \n" +
//                        "您是一位资深的教育专家，擅长从文本中提炼关键知识点，并设计成高效的学习卡片。\n" +
//                        "\n" +
//                        "# 背景信息：  \n" +
//                        "您将接收一段文本内容，需要从中提取出核心知识点，并制作成Anki学习卡片。卡片包括问题、答案和知识点标签。\n" +
//                        "\n" +
//                        "# 工作流程/工作任务：  \n" +
//                        "1. 阅读并理解用户提供的文本内容。\n" +
//                        "2. 提炼文本中的关键知识点。\n" +
//                        "3. 使用中文设计问题，确保问题能够引导用户思考并回忆知识点。\n" +
//                        "4. 使用中文编写答案，确保答案准确、全面，并能够清晰地传达知识点。\n" +
//                        "5. 为每个知识点分配中文标签，便于用户分类和检索。\n" +
//                        "6. 检查卡片内容的正确性和完整性，确保用户能够正确、全面地掌握知识点。\n" +
//                        "7. 确保卡片语言流畅、有逻辑，适合在Anki中高效学习和复习。\n" +
//                        "\n" +
//                        "# 输出示例：  \n" +
//                        "- **问题**：在计算机科学中，什么是“算法”？\n" +
//                        "  **答案**：算法是一系列定义明确的操作步骤，用于解决特定问题或执行特定任务。\n" +
//                        "  **知识点标签**：计算机科学、算法基础。\n" +
//                        "\n" +
//                        "- **问题**：解释“机器学习”中的“监督学习”和“非监督学习”的区别。\n" +
//                        "  **答案**：监督学习是指使用带有标签的训练数据来训练模型，使其能够对新的数据进行分类或回归预测。非监督学习则使用未标记的数据，让模型自己找出数据中的模式和结构，常用于聚类或降维任务。\n" +
//                        "  **知识点标签**：机器学习、数据科学。\n" +
//                        "\n" +
//                        "# 注意事项：  \n" +
//                        "- 卡片的主要内容用中文的。\n" +
//                        "- 确保问题的设计能够引导用户深入理解和回忆知识点。\n" +
//                        "- 答案需要准确无误，同时提供足够的信息，帮助用户全面理解知识点。\n" +
//                        "- 知识点标签应具有描述性和区分度。简洁、准确,使用中文。应该反映内容的核心主题、领域、技术栈等关键信息。对相似内容要生成一致的标签。便于用户进行知识管理和构建知识图谱。\n" +
//                        "- 卡片内容应简洁明了，避免冗余信息，以提高学习效率。\n")
//                .user("Tokens serve as the building blocks of how an AI model works. On input, models convert words to tokens. On output, they convert tokens back to words.\n" +
//                        "\n" +
//                        "In English, one token roughly corresponds to 75% of a word. For reference, Shakespeare’s complete works, totaling around 900,000 words, translate to approximately 1.2 million tokens.\n" +
//                        "\n" +
//                        "Tokens\n" +
//                        "Perhaps more important is that Tokens = Money. In the context of hosted AI models, your charges are determined by the number of tokens used. Both input and output contribute to the overall token count.\n" +
//                        "\n" +
//                        "Also, models are subject to token limits, which restrict the amount of text processed in a single API call. This threshold is often referred to as the \"context window\". The model does not process any text that exceeds this limit.\n" +
//                        "\n" +
//                        "For instance, ChatGPT3 has a 4K token limit, while GPT4 offers varying options, such as 8K, 16K, and 32K. Anthropic’s Claude AI model features a 100K token limit, and Meta’s recent research yielded a 1M token limit model.\n" +
//                        "\n" +
//                        "To summarize the collected works of Shakespeare with GPT4, you need to devise software engineering strategies to chop up the data and present the data within the model’s context window limits. The Spring AI project helps you with this task.")
//                .call()
//                .entity(Cards.class);
//
//        System.out.println(cards);
//
//        ;
//    }
//
//    @Test
//    void generate() {
//        var zhiPuAiApi = new ZhiPuAiApi(zhiPuAiApiToken);
//
//        var chatModel = new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
//                .withModel(ZhiPuAiApi.ChatModel.GLM_4_Flash.getValue())
//                .withTemperature(0.7)
////                .withMaxTokens(200)
//                .build());
//
//
//        ChatClient chatClient = ChatClient.builder(chatModel)
//                .defaultSystem("你的任务是分析用户的笔记，提取出标签（中文）用于知识管理")
//                .build();
//
//        Flux<String> content = chatClient.prompt()
//                .user("Q: 对于在业务上能保证数据唯一性的数据表，使用InnoDB 的普通索引和唯一索引在查询时的性能差距有多大？\n" +
//                        "A: 答：没有多大\n" +
//                        "1. **磁盘**上的 IO 操作是一样的：都需要从磁盘把数据页读取到内存中\n" +
//                        "2. 在**内存**中的操作有所不同：普通索引读取到符合条件的数据后，还会再往后读取数据直到不符合条件（耗时非常少）。如果读到了数据页的末尾，还要从磁盘读取下一个数据页（可能性比较小，一个数据页一般可以存 1000 条记录）。")
//                .stream()
//                .content();
//
//
//        content.doOnNext(data -> {
//            // 在此处可以记录每个流传输的数据
//            System.out.println("Received chunk of AI response: " + data);
//        }).subscribe();  // 订阅并开始消费流
//
////        System.out.println(chatClient.prompt()
////                .user("Q: 对于在业务上能保证数据唯一性的数据表，使用InnoDB 的普通索引和唯一索引在查询时的性能差距有多大？\n" +
////                        "A: 答：没有多大\n" +
////                        "1. **磁盘**上的 IO 操作是一样的：都需要从磁盘把数据页读取到内存中\n" +
////                        "2. 在**内存**中的操作有所不同：普通索引读取到符合条件的数据后，还会再往后读取数据直到不符合条件（耗时非常少）。如果读到了数据页的末尾，还要从磁盘读取下一个数据页（可能性比较小，一个数据页一般可以存 1000 条记录）。")
////                .call()
////                .entity(TagList.class)
////                .toString());
//    }


    @Test
    void generateStream() {
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class TagList {


    private List<String> tags;
}