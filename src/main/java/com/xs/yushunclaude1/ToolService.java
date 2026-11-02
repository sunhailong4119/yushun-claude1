package com.xs.yushunclaude1;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    //告诉大模型工具的作用
    @Tool(description = "执行shel命令")
    public String shellCommandTool(
            @ToolParam(description = "命令") String command){
        //执行shell命令
        System.out.println("执行shell命令："+command);
        return "执行成功";
    }
}
