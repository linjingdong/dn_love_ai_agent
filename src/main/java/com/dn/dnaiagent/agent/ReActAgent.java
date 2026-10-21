package com.dn.dnaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (reasoning and Acting) 模式的代理抽象类，实现了思考 - 行动的循环模式
 *
 * @author Lin_jd
 * @version V1.0.0
 * @since 2026/10/19 14:19
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {
    /**
     * 处理当前状态并决定执行下一步
     * @return 是否需要执行，true表示需要，false表示不需要
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤，先思考再执行
     * @return 执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = this.think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }

            return act();
        } catch (Exception e) {
            log.error("Error executing step", e);
            return "步骤执行失败：" + e.getMessage();
        }
    }
}
