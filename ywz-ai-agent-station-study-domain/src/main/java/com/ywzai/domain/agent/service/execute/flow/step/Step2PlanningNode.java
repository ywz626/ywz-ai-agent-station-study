package com.ywzai.domain.agent.service.execute.flow.step;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz @CreateTime: 2025-10-15 @Description: 第二个节点执行计划编排 @Version: 1.0
 */
@Service
@Slf4j
public class Step2PlanningNode extends AbstractExecuteSupport {
  @Resource private Step3ParseStepsNode step3ParseStepsNode;

  /**
   * 执行计划编排步骤，调用AI客户端生成任务执行规划。
   *
   * <p>该方法完成以下主要操作：
   *
   * <ul>
   *   <li>设置当前执行状态为 "PLANNING"
   *   <li>获取用于计划编排的AI客户端配置
   *   <li>构建并优化提示词(prompt)
   *   <li>通过AI客户端生成具体的执行计划
   *   <li>记录和传递中间结果，并推进流程到下一步
   * </ul>
   *
   * @param executeCommandEntity 当前指令执行实体，包含会话、任务等上下文信息
   * @param dynamicContext 动态上下文对象，用于在流程中共享数据与状态
   * @return 下一阶段的路由结果（由 {@code router} 方法决定）
   * @throws Exception 在执行过程中可能抛出的异常，如网络错误、解析失败等
   */
  @Override
  protected String doApply(
      ExecuteCommandEntity executeCommandEntity,
      DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext)
      throws Exception {
    log.info("\n--- 步骤2: 计划编排 ---");

    // 设置当前状态为 PLANNING
    dynamicContext.setValue("status", "PLANNING");

    // 获取计划编排所使用的 AI 客户端配置
    AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO =
        dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.PLANNING_CLIENT.getCode());
    String planningClientId = aiAgentClientFlowConfigVO.getClientId();

    // 初始化计划编排客户端
    ChatClient planningClient = getClientById(planningClientId);

    // 获取用户请求及 MCP 工具分析结果
    String userRequest = dynamicContext.getCurrentTask();
    String mcpToolsAnalysis = dynamicContext.getValue("mcpToolsAnalysis");

    // 构建结构化的计划提示词
    String planningPrompt = buildStructuredPlanningPrompt(userRequest, mcpToolsAnalysis);

    // 增加工具映射校验反馈要求，增强 prompt 的准确性
    String refinedPrompt =
        planningPrompt
            + "\n\n## ⚠️ 工具映射验证反馈\n"
            + "\n\n**请根据上述验证反馈重新生成规划，确保：**\n"
            + "1. 只使用验证报告中列出的有效工具\n"
            + "2. 工具名称必须完全匹配（区分大小写）\n"
            + "3. 每个步骤明确指定使用的MCP工具\n"
            + "4. 避免使用不存在或无效的工具";

    // 调用AI模型进行推理，得到规划结果
    String planningResult = planningClient.prompt().user(refinedPrompt).call().content();

    log.info("\n⚡ 步骤2: 规划结果: {}\n%s", planningResult);

    // 将规划结果存入动态上下文供后续使用
    dynamicContext.setValue("planningResult", planningResult);

    // 创建子分析结果实体并发送 SSE 实时更新
    AutoAgentExecuteResultEntity autoAgentExecuteResultEntity =
        AutoAgentExecuteResultEntity.createAnalysisSubResult(
            dynamicContext.getStep(),
            "analysis_strategy",
            planningResult,
            executeCommandEntity.getSessionId());
    sendSseResult(dynamicContext, autoAgentExecuteResultEntity);

    // 推进流程步数并进入下一阶段
    dynamicContext.setStep(dynamicContext.getStep() + 1);
    return router(executeCommandEntity, dynamicContext);
  }

  @Override
  public StrategyHandler<
          ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String>
      get(
          ExecuteCommandEntity executeCommandEntity,
          DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext)
          throws Exception {
    return step3ParseStepsNode;
  }

  private String buildStructuredPlanningPrompt(String userRequest, String mcpToolsAnalysis) {

    // 1. 任务分析部分 - 通用化用户需求分析

    String prompt =
        "# 智能执行计划生成\n\n"
            + "## 📋 用户需求分析\n"
            + "**完整用户请求：**\n"
            + "```\n"
            + userRequest
            + "\n```\n\n"
            + "**⚠️ 重要提醒：** 在生成执行计划时，必须完整保留和传递用户请求中的所有详细信息，包括但不限于：\n"
            + "- 任务的具体目标和期望结果\n"
            + "- 涉及的数据、参数、配置等详细信息\n"
            + "- 特定的业务规则、约束条件或要求\n"
            + "- 输出格式、质量标准或验收条件\n"
            + "- 时间要求、优先级或其他执行约束\n\n"
            + "**🚫 严禁执行或调用任何MCP工具。** 此阶段仅生成计划、描述和分析，不允许调用、模拟、推测或触发任何工具操作。\n\n"
            +

            // 2. 工具能力分析
            "## 🔧 MCP工具能力分析结果\n"
            + mcpToolsAnalysis
            + "\n\n"
            + "**注意：** 以下分析结果仅供规划使用，禁止直接执行或触发其中任何工具调用。\n\n"
            +

            // 3. 工具映射验证 - 使用动态获取的工具信息
            "## ✅ 工具映射验证要求\n"
            + "**重要提醒：** 在生成执行步骤时，必须严格遵循以下工具映射规则：\n\n"
            + "### 工具选择原则\n"
            + "- **精确匹配**: 每个步骤必须使用上述工具清单中的确切函数名称（仅用于说明，不得实际调用）\n"
            + "- **功能对应**: 根据MCP工具分析结果中的匹配度选择最适合的工具（仅分析，不执行）\n"
            + "- **参数完整**: 确保每个工具调用都包含必需的参数说明（仅规划用途）\n"
            + "- **依赖关系**: 考虑工具间的数据流转和依赖关系（分析层面，不执行）\n\n"
            +

            // 4. 执行计划要求
            "## 📝 执行计划要求\n"
            + "请基于上述用户详细需求、MCP工具分析结果和工具映射验证要求，生成精确的执行计划：\n\n"
            + "### 核心要求\n"
            + "1. **完整保留用户需求**: 必须将用户请求中的所有详细信息完整传递到每个执行步骤中\n"
            + "2. **严格遵循MCP分析结果**: 必须根据工具能力分析中的匹配度和推荐方案制定步骤\n"
            + "3. **精确工具映射**: 每个步骤必须使用确切的函数名称（仅作为说明，不实际调用）\n"
            + "4. **参数完整性**: 所有工具调用必须包含用户原始需求中的完整参数信息（仅分析参数结构，不执行）\n"
            + "5. **依赖关系明确**: 基于MCP分析结果中的执行策略建议安排步骤顺序\n"
            + "6. **合理粒度**: 避免过度细分，每个步骤应该是完整且独立的功能单元\n"
            + "7. **禁止工具调用**: 所有涉及工具的描述均为“理论分析”，不允许AI尝试调用或触发任何函数。\n\n"
            +

            // 4. 格式规范 - 通用化任务格式
            "### 格式规范\n"
            + "请使用以下Markdown格式生成3-5个执行步骤（仅规划，不执行）：\n"
            + "```markdown\n"
            + "# 执行步骤规划\n\n"
            + "[ ] 第1步：[步骤描述]\n"
            + "[ ] 第2步：[步骤描述]\n"
            + "[ ] 第3步：[步骤描述]\n"
            + "...\n\n"
            + "## 步骤详情\n\n"
            + "### 第1步：[步骤描述]\n"
            + "- **优先级**: [HIGH/MEDIUM/LOW]\n"
            + "- **预估时长**: [分钟数]分钟\n"
            + "- **使用工具**: [必须使用确切的函数名称，仅为说明用途，禁止实际调用]\n"
            + "- **工具匹配度**: [引用MCP分析结果中的匹配度评估]\n"
            + "- **依赖步骤**: [前置步骤序号，如无依赖则填写'无']\n"
            + "- **执行方法**: [基于MCP分析结果的理论执行策略，仅文字说明，不执行任何操作]\n"
            + "- **工具参数**: [详细的参数说明和示例值，必须包含用户原始需求中的所有相关信息]\n"
            + "- **需求传递**: [明确说明如何将用户的详细要求传递到此步骤中]\n"
            + "- **预期输出**: [期望的最终结果]\n"
            + "- **成功标准**: [判断任务完成的标准]\n"
            + "- **MCP分析依据**: [引用具体的MCP工具分析结论]\n\n"
            + "```\n\n"
            +

            // 5. 动态规划指导原则
            "### 规划指导原则\n"
            + "请根据用户详细请求和可用工具能力，动态生成合适的执行步骤：\n"
            + "- **需求完整性原则**: 确保用户请求中的所有详细信息都被完整保留和传递\n"
            + "- **步骤分离原则**: 每个步骤应该专注于单一功能，避免混合不同类型的操作\n"
            + "- **工具映射原则**: 每个步骤应明确使用哪个具体的MCP工具（仅文本描述，不调用）\n"
            + "- **参数传递原则**: 确保用户的详细要求能够准确传递到工具参数中（分析层面）\n"
            + "- **依赖关系原则**: 合理安排步骤顺序，确保前置条件得到满足\n"
            + "- **结果输出原则**: 每个步骤都应有明确的输出结果和成功标准\n"
            + "- **禁止执行原则**: 本阶段仅限规划、分析与文档生成，任何执行行为均被视为错误。\n\n"
            +

            // 6. 步骤类型指导
            "### 步骤类型指导\n"
            + "根据可用工具和用户需求，常见的步骤类型包括（仅分析，不执行）：\n"
            + "- **数据获取步骤**: 使用搜索、查询等工具获取所需信息（仅说明，不调用）\n"
            + "- **数据处理步骤**: 对获取的信息进行分析、整理和加工（理论说明）\n"
            + "- **内容生成步骤**: 基于处理后的数据生成目标内容（仅描述）\n"
            + "- **结果输出步骤**: 将生成的内容发布、保存或传递给用户（仅规划）\n"
            + "- **通知反馈步骤**: 向用户或相关方发送执行结果通知（仅分析，不执行）\n\n"
            +

            // 7. 执行要求
            "### 执行要求\n"
            + "1. **步骤编号**: 使用第1步、第2步、第3步...格式\n"
            + "2. **Markdown格式**: 严格按照上述Markdown格式输出\n"
            + "3. **步骤描述**: 每个步骤描述要清晰、具体、可执行（理论层面）\n"
            + "4. **优先级**: 根据步骤重要性和紧急程度设定\n"
            + "5. **时长估算**: 基于步骤复杂度合理估算\n"
            + "6. **工具选择**: 从可用工具中选择最适合的（仅分析，不执行）\n"
            + "7. **依赖关系**: 明确步骤间的先后顺序\n"
            + "8. **执行细节**: 提供具体可操作的方法（文字形式），包含详细的参数说明和用户需求传递\n"
            + "9. **需求传递**: 确保用户的所有详细要求都能准确传递到相应的执行步骤中\n"
            + "10. **功能独立**: 确保每个步骤功能独立，避免混合不同类型的操作\n"
            + "11. **工具映射**: 每个步骤必须明确指定使用的MCP工具函数名称（仅文本引用）\n"
            + "12. **质量标准**: 设定明确的完成标准\n"
            + "13. **禁止实际执行**: 本阶段仅限文字规划与逻辑设计，不得尝试执行、调用、或模拟任何MCP工具。\n\n"
            +

            // 7. 步骤类型指导
            "### 常见步骤类型指导\n"
            + "- **信息获取步骤**: 使用搜索工具，关注关键词选择和结果筛选（仅说明）\n"
            + "- **内容处理步骤**: 基于获取的信息进行分析、整理和创作（仅理论）\n"
            + "- **结果输出步骤**: 使用相应平台工具发布或保存处理结果（仅规划）\n"
            + "- **通知反馈步骤**: 使用通信工具进行状态通知或结果反馈（仅描述）\n"
            + "- **数据处理步骤**: 对获取的信息进行分析、转换和处理（仅文字说明）\n\n"
            +

            // 8. 质量检查
            "### 质量检查清单\n"
            + "生成计划后请确认：\n"
            + "- [ ] 每个步骤都有明确的序号和描述\n"
            + "- [ ] 使用了正确的Markdown格式\n"
            + "- [ ] 步骤描述清晰具体\n"
            + "- [ ] 优先级设置合理\n"
            + "- [ ] 时长估算现实可行\n"
            + "- [ ] 工具选择恰当（仅文本说明）\n"
            + "- [ ] 依赖关系清晰\n"
            + "- [ ] 执行方法具体可操作（理论层面）\n"
            + "- [ ] 成功标准明确可衡量\n"
            + "- [ ] 🚫 未调用或触发任何MCP服务\n\n"
            + "现在请开始生成Markdown格式的执行步骤规划（仅分析规划，不执行任何操作）：\n";

    return prompt;
  }
}
