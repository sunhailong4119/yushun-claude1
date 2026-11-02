package com.xs.yushunclaude1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.RecursiveSummarizationCompactionStrategy;
import org.springframework.ai.session.compaction.SlidingWindowCompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfiguration {

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                //.chatMemoryRepository()
                .chatMemoryRepository(chatMemoryRepository).build();
    }

    @Bean
    SessionMemoryAdvisor sessionMemoryAdvisor() {
        SessionService service =
                DefaultSessionService.builder().sessionRepository(
                                InMemorySessionRepository.builder().build())
                        .build();

        return SessionMemoryAdvisor.builder(service)
                .defaultUserId("xushu")
                // 触发器 超过1轮对话    对话前:
                // 你好--(获取event 0>1)--> LLM--->你好，有什么能帮助     turn：0+1 不会触发
                // 我叫什么--(获取event 1>1)--> LLM   不会触发    turn：1+1
                // 我叫什么--(获取event 2>1)--> LLM   触发
                .compactionTrigger(new TurnCountTrigger(1))
                // 压缩策略
                .compactionStrategy(SlidingWindowCompactionStrategy.builder().maxEvents(1).build())
                .build();
    }

    @Bean
    SessionMemoryAdvisor sessionMemorySummarizationAdvisor(DeepSeekChatModel deepSeekChatModel) {
        SessionService service =
                DefaultSessionService.builder().sessionRepository(
                                InMemorySessionRepository.builder().build())
                        .build();

        return SessionMemoryAdvisor.builder(service)
                .defaultUserId("xushu")
                .compactionTrigger(new TurnCountTrigger(10))
                .compactionStrategy(RecursiveSummarizationCompactionStrategy.builder(
                        ChatClient.builder(deepSeekChatModel).build()).maxEventsToKeep(10)
                        .overlapSize(2).build())
                .build();
    }

}
