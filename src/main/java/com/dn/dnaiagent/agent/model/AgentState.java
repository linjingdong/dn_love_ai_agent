package com.dn.dnaiagent.agent.model;

/**
 * 代理执行状态的枚举类
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/19 14:18
 **/
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}

