package com.ywzai.trigger.http.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ywzai.api.IAiAgentDrawAdminService;
import com.ywzai.api.dto.AiAgentDrawConfigQueryRequestDTO;
import com.ywzai.api.dto.AiAgentDrawConfigRequestDTO;
import com.ywzai.api.dto.AiAgentDrawConfigResponseDTO;
import com.ywzai.api.response.Response;
import com.ywzai.infrastructure.dao.IAiAgentDao;
import com.ywzai.infrastructure.dao.IAiAgentDrawConfigDao;
import com.ywzai.infrastructure.dao.IAiAgentFlowConfigDao;
import com.ywzai.infrastructure.dao.IAiClientConfigDao;
import com.ywzai.infrastructure.dao.po.AiAgent;
import com.ywzai.infrastructure.dao.po.AiAgentDrawConfig;
import com.ywzai.infrastructure.dao.po.AiAgentFlowConfig;
import com.ywzai.infrastructure.dao.po.AiClientConfig;
import com.ywzai.trigger.http.admin.util.DrawConfigParser;
import com.ywzai.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: ywz @CreateTime: 2025-10-17 @Description: 画布配置保存 @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-agent-draw")
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {
      RequestMethod.GET,
      RequestMethod.POST,
      RequestMethod.PUT,
      RequestMethod.DELETE,
      RequestMethod.OPTIONS
    })
public class AiAgentDrawAdminController implements IAiAgentDrawAdminService {

  @Resource private IAiAgentDao aiAgentDao;
  @Resource private IAiAgentDrawConfigDao aiAgentDrawConfigDao;
  @Resource private IAiClientConfigDao aiClientConfigDao;
  @Resource private IAiAgentFlowConfigDao aiAgentFlowConfigDao;

  /**
   * 查询拖拉拽流程图配置列表接口
   *
   * <p>根据传入的查询条件（如configId、configName、agentId、status等）从数据库中查询符合条件的配置信息，
   * 并支持内存分页处理。最终将结果转换为DTO对象并封装成统一响应格式返回。
   *
   * @param request 包含查询条件的数据传输对象，包括configId、configName、agentId、status以及分页参数pageNum和pageSize
   * @return 返回一个包含AiAgentDrawConfigResponseDTO列表的统一响应对象，若发生异常则返回错误码和提示信息
   */
  @Override
  @PostMapping("/query-list")
  public Response<List<AiAgentDrawConfigResponseDTO>> queryDrawConfigList(
      @RequestBody AiAgentDrawConfigQueryRequestDTO request) {
    try {
      log.info("查询拖拉拽流程图配置列表请求：{}", request);

      List<AiAgentDrawConfig> configs;

      // 条件查询：根据不同的查询字段优先级依次进行查询
      if (StringUtils.hasText(request.getConfigId())) {
        AiAgentDrawConfig cfg = aiAgentDrawConfigDao.getByConfigId(request.getConfigId());
        configs = cfg != null ? List.of(cfg) : List.of();
      } else if (StringUtils.hasText(request.getConfigName())) {
        configs = aiAgentDrawConfigDao.queryByConfigName(request.getConfigName());
      } else if (StringUtils.hasText(request.getAgentId())) {
        AiAgentDrawConfig cfg = aiAgentDrawConfigDao.queryByAgentId(request.getAgentId());
        configs = cfg != null ? List.of(cfg) : List.of();
      } else if (request.getStatus() != null) {
        if (request.getStatus() == 1) {
          configs = aiAgentDrawConfigDao.queryEnabledConfigs();
        } else {
          configs = aiAgentDrawConfigDao.queryAll();
        }
      } else {
        configs = aiAgentDrawConfigDao.queryAll();
      }

      // 简单分页（内存分页）：在内存中对查询结果进行分页截取
      if (request.getPageNum() != null && request.getPageSize() != null) {
        int pageNum = Math.max(1, request.getPageNum());
        int pageSize = Math.max(1, request.getPageSize());
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, configs.size());
        if (start < configs.size()) {
          configs = configs.subList(start, end);
        } else {
          configs = List.of();
        }
      }

      // PO 转 DTO：将查询到的实体对象转换为对外暴露的响应数据传输对象
      List<AiAgentDrawConfigResponseDTO> responseDTOs = new ArrayList<>();
      for (AiAgentDrawConfig config : configs) {
        AiAgentDrawConfigResponseDTO dto = new AiAgentDrawConfigResponseDTO();
        BeanUtils.copyProperties(config, dto);
        responseDTOs.add(dto);
      }

      return Response.<List<AiAgentDrawConfigResponseDTO>>builder()
          .code(ResponseCode.SUCCESS.getCode())
          .info(ResponseCode.SUCCESS.getInfo())
          .data(responseDTOs)
          .build();
    } catch (Exception e) {
      log.error("查询拖拉拽流程图配置列表失败", e);
      return Response.<List<AiAgentDrawConfigResponseDTO>>builder()
          .code(ResponseCode.UN_ERROR.getCode())
          .info(ResponseCode.UN_ERROR.getInfo())
          .data(null)
          .build();
    }
  }

  /**
   * 保存AI Agent画布配置信息接口。
   *
   * <p>主要功能包括： 1. 校验请求参数合法性（如agentId、configName、configData）； 2. 若未提供agentId，则自动生成一个； 3.
   * 解析configData中的agent相关信息并插入或更新到ai_agent表中； 4. 插入或更新ai_agent_draw_config表中的画布配置数据； 5.
   * 解析并保存客户端连接关系至ai_client_config表； 6. 解析并保存agent与client之间的流程配置至ai_agent_flow_config表。
   *
   * @param request 包含画布配置信息的请求对象，必须包含configName和configData字段
   * @return 返回统一响应结构体，其中data为保存成功的configId；若失败则返回错误码及描述
   */
  @Override
  @PostMapping("/save-config")
  @Transactional(rollbackFor = Exception.class)
  public Response<String> saveDrawConfig(@RequestBody AiAgentDrawConfigRequestDTO request) {
    try {
      log.info("开始保存画布信息: {}", request.getConfigName());
      // 参数校验：配置名称不能为空
      if (!StringUtils.hasText(request.getConfigName())) {
        return Response.<String>builder()
            .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
            .info("配置名称不能为空")
            .build();
      }
      // 参数校验：配置数据不能为空
      if (!StringUtils.hasText(request.getConfigData())) {
        return Response.<String>builder()
            .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
            .info("配置数据不能为空")
            .build();
      }
      String agentId = request.getAgentId();
      AiAgent aiAgent = aiAgentDao.queryByAgentId(agentId);
      if (aiAgent == null) {
        // 解析配置数据中的agent基本信息，并插入ai_agent表
        String[] agentInfo = parseAgentInfoFromJson(request.getConfigData());
        String agentName = agentInfo[0];
        String description = agentInfo[1];
        String channel = agentInfo[2];
        String strategy = agentInfo[3];
        aiAgentDao.insert(
            AiAgent.builder()
                .agentId(agentId)
                .agentName(agentName)
                .description(description)
                .channel(channel)
                .strategy(strategy)
                .status(1)
                .build());
      }

      /*
       * 处理画布配置表(ai_agent_draw_config)的操作逻辑：
       * - 如果没有传入configId，则生成一个新的UUID作为configId；
       * - 查询是否存在该configId对应的旧记录；
       * - 存在则执行更新操作，版本号+1；
       * - 否则执行新增操作。
       */
      String configId = request.getConfigId();
      if (!StringUtils.hasText(configId)) {
        configId = UUID.randomUUID().toString().replace("-", "");
      }
      AiAgentDrawConfig existingConfig = aiAgentDrawConfigDao.getByConfigId(configId);
      AiAgentDrawConfig aiAgentDrawConfig = new AiAgentDrawConfig();
      BeanUtils.copyProperties(request, aiAgentDrawConfig);
      aiAgentDrawConfig.setConfigId(configId);
      aiAgentDrawConfig.setVersion(1);
      aiAgentDrawConfig.setStatus(1);
      int result = 0;
      if (existingConfig != null) {
        aiAgentDrawConfig.setId(existingConfig.getId());
        aiAgentDrawConfig.setUpdateTime(LocalDateTime.now());
        aiAgentDrawConfig.setVersion(existingConfig.getVersion() + 1);
        result = aiAgentDrawConfigDao.updateByConfigId(aiAgentDrawConfig);
        log.info("更新流程图配置，configId: {}, result: {}", configId, result);
      } else {
        aiAgentDrawConfig.setCreateTime(LocalDateTime.now());
        aiAgentDrawConfig.setUpdateTime(LocalDateTime.now());
        result = aiAgentDrawConfigDao.insert(aiAgentDrawConfig);
      }
      // 判断主表是否更新/插入成功，再处理关联子表
      if (result > 0) {
        // 解析并保存客户端连接关系到 ai_client_config 表
        try {
          log.info("开始更新Client关联表数据");
          List<AiClientConfig> configRelations =
              DrawConfigParser.parseConfigData(request.getConfigData());
          for (AiClientConfig config : configRelations) {
            // 检查是否已经存在相同的记录
            List<AiClientConfig> existingConfigs =
                aiClientConfigDao.queryByConditions(
                    config.getSourceType(),
                    config.getSourceId(),
                    config.getTargetType(),
                    config.getTargetId());
            if (existingConfigs.isEmpty()) {
              aiClientConfigDao.insert(config);
            } else {
              log.info("已存在相同的记录, 不需要重复插入: {}", config);
            }
          }
        } catch (Exception e) {
          log.error("解析和保存配置关系数据失败，configId: {}", configId, e);
          // 这里不影响主流程，只记录错误日志
        }
        // 解析并保存agent-client流程关系到 ai_agent_flow_config 表
        try {
          log.info("开始更新agent_flow_config表数据");
          List<AiAgentFlowConfig> agentFlowConfigs =
              parseAgentFlowConfig(request.getConfigData(), agentId);
          if (!agentFlowConfigs.isEmpty()) {
            // 先判断是否已经存在,更新操作时先把原来的删除
            if (existingConfig != null) {
              aiAgentFlowConfigDao.deleteByAgentId(agentId);
            }
            for (AiAgentFlowConfig agentFlowConfig : agentFlowConfigs) {
              aiAgentFlowConfigDao.insert(agentFlowConfig);
              log.info("插入agent_flow_config表数据: {}", agentFlowConfig);
            }
          }
        } catch (Exception e) {
          log.error("解析和保存agent-client关系数据失败，agentId: {}", agentId, e);
          // 这里不影响主流程，只记录错误日志
        }
        // 成功返回结果
        return Response.<String>builder()
            .code(ResponseCode.SUCCESS.getCode())
            .info(ResponseCode.SUCCESS.getInfo())
            .data(configId)
            .build();
      } else {
        // 数据库操作失败
        return Response.<String>builder()
            .code(ResponseCode.UN_ERROR.getCode())
            .info("保存失败")
            .build();
      }
    } catch (Exception e) {
      log.error("保存流程图配置失败", e);
      return Response.<String>builder()
          .code(ResponseCode.UN_ERROR.getCode())
          .info("保存失败：" + e.getMessage())
          .build();
    }
  }

  private List<AiAgentFlowConfig> parseAgentFlowConfig(String configData, String agentId)
      throws JsonProcessingException {
    List<AiAgentFlowConfig> agentFlowConfigs = new ArrayList<>();
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode configNode = mapper.readTree(configData);
      JsonNode nodes = configNode.get("nodes");
      String clientId = null;
      String clientName = null;
      String clientType = null;
      String stepPrompt = null;
      Integer sequence = null;
      for (JsonNode node : nodes) {
        if (node.get("type").asText().equals("client")) {
          JsonNode data = node.get("data");
          if (data != null) {
            JsonNode inputsValuesNode = data.get("inputsValues");
            clientType = extractValueFromInputs(inputsValuesNode, "clientType");
            clientId = extractValueFromInputs(inputsValuesNode, "clientId");
            clientName = extractValueFromInputs(inputsValuesNode, "clientName");
            sequence = extractIntegerValueFromInputs(inputsValuesNode, "sequence");
            stepPrompt = extractValueFromInputs(inputsValuesNode, "stepPrompt");
            agentFlowConfigs.add(
                AiAgentFlowConfig.builder()
                    .agentId(agentId)
                    .clientId(clientId)
                    .clientName(clientName)
                    .clientType(clientType)
                    .stepPrompt(stepPrompt)
                    .sequence(sequence)
                    .createTime(LocalDateTime.now())
                    .status(1)
                    .build());
          }
        }
      }
    } catch (Exception e) {
      log.error("解析配置数据失败: {}", e.getMessage());
      throw e;
    }
    return agentFlowConfigs;
  }

  /**
   * 从输入的JSON节点中提取指定字段的整数值
   *
   * @param inputsValuesNode 包含输入值的JSON节点
   * @param sequenceNode 需要提取的字段名称
   * @return 提取到的整数值，如果无法提取则返回null
   */
  private Integer extractIntegerValueFromInputs(JsonNode inputsValuesNode, String sequenceNode) {
    JsonNode node = inputsValuesNode.get(sequenceNode);
    log.info("提取字段:{} ,{}", node, sequenceNode);
    Integer sequence = null;

    // 处理数组类型的节点
    if (node.isArray()) {
      JsonNode firstNode = node.get(0);
      if (firstNode != null) {
        JsonNode value = firstNode.get("value");
        if (value != null) {
          if (value.isObject()) {
            sequence = value.get("content").asInt();
            log.debug("字段类型: {} 为数组中的value对象: {}", node, sequence);
            return sequence;
          } else if (value.isTextual()) {
            sequence = Integer.valueOf(value.asText());
            log.debug("字段类型: {} 为数组中的value字符串: {}", node, sequence);
            return sequence;
          } else if (value.isNumber()) {
            sequence = value.asInt();
            log.debug("字段类型: {} 为数组中的value数字: {}", node, sequence);
            return sequence;
          }
        }
      }
      // 处理字符串类型的节点
    } else if (node.isTextual()) {
      sequence = node.asInt();
      log.debug("字段类型: {} 为数字: {}", node, sequence);
    }
    return sequence;
  }

  /**
   * 从JSON配置数据中解析agent信息
   *
   * @param configData JSON格式的配置数据字符串
   * @return 包含agent信息的字符串数组，数组包含4个元素：agent名称、描述、渠道、策略
   */
  private String[] parseAgentInfoFromJson(String configData) {
    String[] agentInfo = new String[] {"", "", "", ""};
    ObjectMapper mapper = new ObjectMapper();
    try {
      JsonNode configNode = mapper.readTree(configData);
      JsonNode nodes = configNode.get("nodes");
      if (nodes != null && nodes.isArray()) {
        // 遍历所有节点，查找agent节点并提取相关信息
        for (JsonNode node : nodes) {
          String type = node.get("type").asText();
          // 只判断agent节点
          if ("agent".equals(type)) {
            JsonNode dataNode = node.get("data");
            if (dataNode != null) {
              JsonNode inputsValues = dataNode.get("inputsValues");
              if (inputsValues != null) {
                String agentName = extractValueFromInputs(inputsValues, "agentName");
                String description = extractValueFromInputs(inputsValues, "description");
                String channel = extractValueFromInputs(inputsValues, "channel");
                String strategy = extractValueFromInputs(inputsValues, "strategy");

                agentInfo[0] = agentName == null ? "" : agentName;
                agentInfo[1] = description == null ? "" : description;
                agentInfo[2] = channel == null ? "" : channel;
                agentInfo[3] = strategy == null ? "" : strategy;

                log.info(
                    "解析到agent信息: agentName={}, description={}, channel={}, strategy={}",
                    agentName,
                    description,
                    channel,
                    strategy);
                break; // 找到第一个agent节点就退出
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("解析画布配置数据失败: {}", e.getMessage());
    }
    return agentInfo;
  }

  /**
   * 从输入的JSON节点中提取指定名称的值
   *
   * @param inputsValues 包含输入数据的JSON节点
   * @param name 要提取值的字段名称
   * @return 提取到的字符串值，如果未找到或解析失败则返回null
   */
  private String extractValueFromInputs(JsonNode inputsValues, String name) {
    JsonNode node = inputsValues.get(name);
    log.info("提取字段:{} ,{}", node, name);
    String text = null;

    // 处理数组类型的节点
    if (node.isArray()) {
      JsonNode firstNode = node.get(0);
      if (firstNode != null) {
        JsonNode value = firstNode.get("value");
        if (value != null) {
          if (value.isObject()) {
            text = value.get("content").asText();
            log.debug("字段类型: {} 为数组中的value对象: {}", node, text);
            return text;
          } else if (value.isTextual()) {
            text = value.asText();
            log.debug("字段类型: {} 为数组中的value字符串: {}", node, text);
            return text;
          }
        }
      }
      // 处理字符串类型的节点
    } else if (node.isTextual()) {
      text = node.asText();
      log.debug("字段类型: {} 为字符串: {}", node, text);
    }
    return text;
  }

  @Override
  @GetMapping("/get-config/{configId}")
  public Response<AiAgentDrawConfigResponseDTO> getDrawConfig(
      @PathVariable("configId") String configId) {
    try {
      log.info("获取流程图配置请求，configId: {}", configId);

      if (!StringUtils.hasText(configId)) {
        return Response.<AiAgentDrawConfigResponseDTO>builder()
            .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
            .info("配置ID不能为空")
            .build();
      }

      AiAgentDrawConfig drawConfig = aiAgentDrawConfigDao.getByConfigId(configId);

      if (drawConfig == null) {
        return Response.<AiAgentDrawConfigResponseDTO>builder()
            .code(ResponseCode.UN_ERROR.getCode())
            .info("配置不存在")
            .build();
      }

      AiAgentDrawConfigResponseDTO responseDTO = new AiAgentDrawConfigResponseDTO();
      BeanUtils.copyProperties(drawConfig, responseDTO);

      return Response.<AiAgentDrawConfigResponseDTO>builder()
          .code(ResponseCode.SUCCESS.getCode())
          .info(ResponseCode.SUCCESS.getInfo())
          .data(responseDTO)
          .build();

    } catch (Exception e) {
      log.error("获取流程图配置失败", e);
      return Response.<AiAgentDrawConfigResponseDTO>builder()
          .code(ResponseCode.UN_ERROR.getCode())
          .info("获取失败：" + e.getMessage())
          .build();
    }
  }

  @Override
  @DeleteMapping("/delete-config/{configId}")
  @Transactional(rollbackFor = Exception.class)
  public Response<String> deleteDrawConfig(@PathVariable("configId") String configId) {
    try {
      log.info("删除流程图配置请求，configId: {}", configId);

      if (!StringUtils.hasText(configId)) {
        return Response.<String>builder()
            .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
            .info("配置ID不能为空")
            .build();
      }

      // 1. 先查询配置详情获取agentId
      AiAgentDrawConfig drawConfig = aiAgentDrawConfigDao.getByConfigId(configId);
      if (drawConfig == null) {
        return Response.<String>builder()
            .code(ResponseCode.UN_ERROR.getCode())
            .info("删除失败，配置不存在")
            .build();
      }

      String agentId = drawConfig.getAgentId();
      log.info("删除流程图配置，configId: {}, agentId: {}", configId, agentId);

      // 2. 删除拖拉拽配置
      int drawConfigResult = aiAgentDrawConfigDao.deleteByConfigId(configId);
      log.info("删除拖拉拽配置结果: {}", drawConfigResult);

      // 3. 删除智能体配置
      if (StringUtils.hasText(agentId)) {
        int agentResult = aiAgentDao.deleteByAgentId(agentId);
        log.info("删除智能体配置结果: {}", agentResult);

        // 4. 删除智能体流程配置
        int flowConfigResult = aiAgentFlowConfigDao.deleteByAgentId(agentId);
        log.info("删除智能体流程配置结果: {}", flowConfigResult);
      }

      if (drawConfigResult > 0) {
        return Response.<String>builder()
            .code(ResponseCode.SUCCESS.getCode())
            .info(ResponseCode.SUCCESS.getInfo())
            .data("删除成功")
            .build();
      } else {
        return Response.<String>builder()
            .code(ResponseCode.UN_ERROR.getCode())
            .info("删除失败，配置不存在")
            .build();
      }

    } catch (Exception e) {
      log.error("删除流程图配置失败", e);
      return Response.<String>builder()
          .code(ResponseCode.UN_ERROR.getCode())
          .info("删除失败：" + e.getMessage())
          .build();
    }
  }
}
