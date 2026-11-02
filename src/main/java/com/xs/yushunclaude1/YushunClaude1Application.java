package com.xs.yushunclaude1;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.Scanner;

@SpringBootApplication
public class YushunClaude1Application {

    public static void main(String[] args) {
        SpringApplication.run(YushunClaude1Application.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(DeepSeekChatModel deepSeekChatModel,
                                        //内部选择模型，只配置了一个模型的依赖
                                        ChatClient.Builder chatClientBuilder,
                                        ChatMemory chatMemory,
                                        SessionMemoryAdvisor sessionMemorySummarizationAdvisor,
                                        ToolService toolService,
                                        ToolCallbackProvider toolCallbackProvider
                                        ) {

        return args -> {

            //动态修改模型参数
            //对话代理（记忆、advisor大模型对话拦截器、结构化输入...）
            ChatClient chatClient = ChatClient.builder(deepSeekChatModel)
                    .defaultAdvisors(
                            //不带压缩功能
                            //MessageChatMemoryAdvisor.builder(chatMemory).build()
                            sessionMemorySummarizationAdvisor,
                            SimpleLoggerAdvisor.builder().build()
                    )
                    .defaultTools(
                            FileSystemTools.builder().build(),
                            ShellTools.builder().build()
                    )
                    .defaultTools(toolCallbackProvider)
                    .build();
            // Start the chat loop
            System.out.println("\n我是xushu-claude:.\n");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("\n> 你: ");
                    String userMessage = scanner.nextLine();
                    if (userMessage == null) {
                        break;
                    }
                    System.out.println("\n> AI: ");
                    try {
                        Flux<String> content = chatClient.prompt().user(userMessage)
                                .advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, "123"))
                                .stream().content();
                        // 阻塞等待流式结束；工具失败不应拖垮整个进程
                        content.doOnNext(System.out::print).blockLast();
                    } catch (Exception e) {
                        System.out.println("\n[调用失败] " + e.getMessage());
                    }
                    System.out.println();
                }
            }
        };
    }

}
