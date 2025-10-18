package com.ywzai.trigger.http.admin.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ywzai.infrastructure.dao.po.AiClientConfig;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-17
 * @Description: 画布配置传过来的JSON解析类
 * @Version: 1.0
 */
@Slf4j
public class DrawConfigParser {
    private final static ObjectMapper mapper = new ObjectMapper();

    /**
     * 解析配置数据，生成AI客户端配置列表
     *
     * @param configData 配置数据字符串，应为JSON格式，包含nodes和edges信息
     * @return 解析后的AiClientConfig配置列表
     */
    public static List<AiClientConfig> parseConfigData(String configData) {
        List<AiClientConfig> configList = new ArrayList<>();
        try {
            // 解析JSON配置数据
            JsonNode rootNode = mapper.readTree(configData);
            if (rootNode != null) {
                JsonNode nodes = rootNode.get("nodes");
                JsonNode edges = rootNode.get("edges");
                // 检查配置信息完整性
                if (nodes == null || edges == null) {
                    log.error("配置信息不完整");
                    return configList;
                }
                // 构建节点信息映射表
                HashMap<String, NodeInfo> nodeInfoMap = buildNodeMap(nodes);
                // 解析边信息并生成配置列表
                parseEdges(edges, nodeInfoMap, configList);
                // 验证解析结果并记录日志
                validateAndLogResults(nodeInfoMap, configList, edges.size());

                log.info("解析配置数据完成，生成{}条关系", configList.size());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return configList;
    }


    /**
     * 验证并记录节点与配置关系的统计信息。
     * <p>
     * 该方法会对传入的节点信息和配置列表进行统计分析，并将结果输出到日志中。
     * 主要包括：各节点类型的数量、含有引用ID的节点数量、配置中的关系类型及数量，
     * 以及整体的节点数、边数和有效配置关系数。
     *
     * @param nodeInfoMap 节点信息映射，键为节点ID，值为对应的NodeInfo对象
     * @param configList  AI客户端配置列表，用于统计节点之间的关系
     * @param totalEdges  总边数，表示图结构中所有连接的数量
     */
    private static void validateAndLogResults(HashMap<String, NodeInfo> nodeInfoMap, List<AiClientConfig> configList, int totalEdges) {
        // 先统计节点数据
        HashMap<String, Integer> nodeTypeCount = new HashMap<>();
        HashMap<String, Integer> nodeWithRefIdCount = new HashMap<>();

        for (Map.Entry<String, NodeInfo> entry : nodeInfoMap.entrySet()) {
            NodeInfo nodeInfo = entry.getValue();
            nodeTypeCount.put(nodeInfo.getNodeType(), nodeTypeCount.getOrDefault(nodeInfo.getNodeType(), 0) + 1);
            if (nodeInfo.getRefId() != null && !nodeInfo.getRefId().trim().isEmpty()) {
                nodeWithRefIdCount.put(nodeInfo.getNodeType(), nodeWithRefIdCount.getOrDefault(nodeInfo.getNodeType(), 0) + 1);
            }
        }

        // 输出每种节点类型的总数及其含引用ID的数量
        log.info("节点类型统计结果");
        for (Map.Entry<String, Integer> entry : nodeTypeCount.entrySet()) {
            int count = nodeWithRefIdCount.get(entry.getKey());
            log.info("节点类型: {}, 数量: {} {}有引用id", entry.getKey(), entry.getValue(), count);
        }

        // 统计不同节点间的关系种类及出现次数
        log.info("节点配置关系统计结果");
        HashMap<String, Integer> relationTypeCount = new HashMap<>();
        for (AiClientConfig config : configList) {
            String relation = config.getSourceType() + " -> " + config.getTargetType();
            relationTypeCount.put(relation, relationTypeCount.getOrDefault(relation, 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : relationTypeCount.entrySet()) {
            log.info("关系类型: {}, 数量: {}", entry.getKey(), entry.getValue());
        }

        // 打印总体统计数据
        log.info("总计: {}个节点，{}条边，{}条有效配置关系",
                nodeInfoMap.size(), totalEdges, configList.size());
        log.info("=== 统计结束 ===");
    }


    /**
     * 解析边关系数据，根据源节点和目标节点的信息构建 AiClientConfig 配置对象，并加入到配置列表中。
     *
     * @param edges       包含所有边信息的 JsonNode 对象
     * @param nodeInfoMap 节点 ID 到 NodeInfo 映射表，用于查找节点详细信息
     * @param configList  存储生成的 AiClientConfig 配置对象的列表
     */
    private static void parseEdges(JsonNode edges, HashMap<String, NodeInfo> nodeInfoMap, List<AiClientConfig> configList) {
        log.info("开始解析边关系，总共{}条边", edges.size());
        int processedEdges = 0;
        int skippedEdges = 0;
        int validConfigs = 0;

        for (JsonNode edge : edges) {
            String sourceNodeID = edge.get("sourceNodeID").asText();
            String targetNodeID = edge.get("targetNodeID").asText();
            String sourcePortId = null;
            if (edge.has("sourcePortID")) {
                sourcePortId = edge.get("sourcePortID").asText();
            }
            log.debug("处理边关系[{}/{}]: {} -> {}, sourcePortId: {}",
                    processedEdges, edges.size(), sourceNodeID, targetNodeID, sourcePortId);

            // 获取源节点与目标节点信息
            NodeInfo sourceNodeInfo = nodeInfoMap.get(sourceNodeID);
            NodeInfo targetNodeInfo = nodeInfoMap.get(targetNodeID);
            if (targetNodeInfo == null || sourceNodeInfo == null) {
                log.warn("找不到节点信息，sourceNodeId: {}, targetNodeId: {}", sourceNodeID, targetNodeID);
                skippedEdges++;
                continue;
            }

            // 跳过涉及 start 类型节点的关系
            if (sourceNodeInfo.getNodeType().equals("start") || targetNodeInfo.getNodeType().equals("start")) {
                log.debug("跳过start节点: {} -> {}", sourceNodeInfo.getNodeType(), targetNodeInfo.getNodeType());
                skippedEdges++;
                continue;
            }
            if(sourceNodeInfo.getNodeType().equals("agent") || targetNodeInfo.getNodeType().equals("agent")){
                log.debug("跳过agent节点: {} -> {}", sourceNodeInfo.getNodeType(), targetNodeInfo.getNodeType());
                skippedEdges++;
                continue;
            }

            // 检查节点是否具有有效的引用 ID（refId）
            if (sourceNodeInfo.getRefId() == null || sourceNodeInfo.getRefId().trim().isEmpty() ||
                    targetNodeInfo.getRefId() == null || targetNodeInfo.getRefId().trim().isEmpty()) {
                log.warn("节点缺少有效的引用ID，跳过关系: {}({}) -> {}({})",
                        sourceNodeInfo.getNodeType(), sourceNodeInfo.getRefId(),
                        targetNodeInfo.getNodeType(), targetNodeInfo.getRefId());
                skippedEdges++;
                continue;
            }

            // 创建并添加配置项
            log.info("创建配置关系: {}({}) -> {}({}), sourcePortId: {}",
                    sourceNodeInfo.getNodeType(), sourceNodeInfo.getRefId(),
                    targetNodeInfo.getNodeType(), targetNodeInfo.getRefId(), sourcePortId);
            AiClientConfig config = createAiClientConfig(sourceNodeInfo, targetNodeInfo, sourcePortId);
            if (config != null) {
                configList.add(config);
                validConfigs++;
                log.info("成功创建配置关系[{}]: {} -> {}", validConfigs, config.getSourceType() + ":" + config.getSourceId(), config.getTargetType() + ":" + config.getTargetId());
            } else {
                skippedEdges++;
                log.warn("创建配置关系失败: {}({}) -> {}({})",
                        sourceNodeInfo.getNodeType(), sourceNodeInfo.getRefId(),
                        targetNodeInfo.getNodeType(), targetNodeInfo.getRefId());
            }
        }
        log.info("边关系解析完成，处理{}条边，跳过{}条，生成{}条有效配置关系",
                processedEdges, skippedEdges, validConfigs);
    }


    private static AiClientConfig createAiClientConfig(NodeInfo sourceNode, NodeInfo targetNode, String sourcePortId) {
        // 确保两个节点都有引用ID
        if (sourceNode.getRefId() == null || targetNode.getRefId() == null) {
            log.warn("节点缺少引用ID，source: {}, target: {}", sourceNode, targetNode);
            return null;
        }

        // 构建扩展参数，包含端口信息和节点标题
        String extParam = "{}";
        try {
            StringBuilder extParamBuilder = new StringBuilder("{");
            boolean hasParam = false;

            // 添加源端口ID
            if (sourcePortId != null && !sourcePortId.trim().isEmpty()) {
                extParamBuilder.append("\"sourcePortId\":\"").append(sourcePortId).append("\"");
                hasParam = true;
            }

            // 添加源节点标题
            if (sourceNode.getTitle() != null && !sourceNode.getTitle().trim().isEmpty()) {
                if (hasParam) extParamBuilder.append(",");
                extParamBuilder.append("\"sourceTitle\":\"").append(sourceNode.getTitle()).append("\"");
                hasParam = true;
            }

            // 添加目标节点标题
            if (targetNode.getTitle() != null && !targetNode.getTitle().trim().isEmpty()) {
                if (hasParam) extParamBuilder.append(",");
                extParamBuilder.append("\"targetTitle\":\"").append(targetNode.getTitle()).append("\"");
                hasParam = true;
            }

            extParamBuilder.append("}");
            extParam = extParamBuilder.toString();

        } catch (Exception e) {
            log.warn("构建扩展参数失败，使用默认值: {}", e.getMessage());
            extParam = "{}";
        }

        AiClientConfig config = AiClientConfig.builder()
                .sourceType(sourceNode.getNodeType())
                .sourceId(sourceNode.getRefId())
                .targetType(targetNode.getNodeType())
                .targetId(targetNode.getRefId())
                .extParam(extParam)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        log.debug("创建配置对象: sourceType={}, sourceId={}, targetType={}, targetId={}, extParam={}",
                config.getSourceType(), config.getSourceId(), config.getTargetType(), config.getTargetId(), config.getExtParam());

        return config;
    }


    /**
     * 构建节点信息映射表
     *
     * @param nodes 包含节点信息的JsonNode对象
     * @return 包含节点ID和节点信息映射关系的HashMap
     */
    private static HashMap<String, NodeInfo> buildNodeMap(JsonNode nodes) {
        HashMap<String, NodeInfo> nodeInfoMap = new HashMap<>();
        // 遍历所有节点，提取节点信息
        for (JsonNode node : nodes) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setNodeId(node.get("id").asText());
            nodeInfo.setNodeType(node.get("type").asText());
            JsonNode dataNode = node.get("data");
            if (dataNode != null) {
                nodeInfo.setTitle(dataNode.has("title") ? dataNode.get("title").asText() : "");
                JsonNode inputsValues = dataNode.get("inputsValues");
                if (inputsValues != null) {
                    extractRefId(inputsValues, nodeInfo);
                    nodeInfoMap.put(nodeInfo.getNodeId(), nodeInfo);
                }
            }
        }


        return nodeInfoMap;
    }


    /**
     * 根据节点类型提取对应的引用ID
     *
     * @param inputsValues 输入的JSON节点数据
     * @param nodeInfo     节点信息对象，包含节点类型等信息
     */
    private static void extractRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        String nodeType = nodeInfo.getNodeType();
        // 根据不同的节点类型调用相应的引用ID提取方法
        switch (nodeType) {
            case "client":
                extractClientRefId(inputsValues, nodeInfo);
                break;
            case "agent":
                extractAgentRefId(inputsValues, nodeInfo);
                break;
            case "tool_mcp":
                extractToolMcpRefId(inputsValues, nodeInfo);
                break;
            case "model":
                extractModelRefId(inputsValues, nodeInfo);
                break;
            case "prompt":
                extractPromptRefId(inputsValues, nodeInfo);
                break;
            case "advisor":
                extractAdvisorRefId(inputsValues, nodeInfo);
                break;
            default:
                // 其他类型节点暂不处理
                log.debug("未处理的节点类型: {}", nodeInfo.getNodeType());
                break;
        }
    }


    /**
     * 从输入的JSON节点中提取顾问引用ID并设置到节点信息对象中
     *
     * @param inputsValues 包含输入数据的JSON节点，预期包含advisorName字段
     * @param nodeInfo     节点信息对象，用于存储提取到的引用ID
     */
    private static void extractAdvisorRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        // 获取顾问名称节点
        JsonNode advisorName = inputsValues.get("advisorName");
        // 检查顾问名称节点是否存在、是否为数组且不为空
        if (advisorName != null && advisorName.isArray() && !advisorName.isEmpty()) {
            // 获取数组中的第一个节点
            JsonNode firstNode = advisorName.get(0);
            // 检查第一个节点是否包含value字段
            if (firstNode.has("value")) {
                // 提取value字段的文本值并设置为节点的引用ID
                nodeInfo.setRefId(firstNode.get("value").asText());
            }
        }
    }


    /**
     * 从输入值中提取提示引用ID并设置到节点信息中
     *
     * @param inputsValues 输入值的JSON节点，包含promptName等信息
     * @param nodeInfo     节点信息对象，用于存储提取到的引用ID
     */
    private static void extractPromptRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        // 获取promptName节点
        JsonNode promptName = inputsValues.get("promptName");
        // 检查promptName是否存在且为非空数组
        if (promptName != null && promptName.isArray() && !promptName.isEmpty()) {
            // 获取数组中的第一个节点
            JsonNode firstNode = promptName.get(0);
            // 检查第一个节点是否包含value字段
            if (firstNode.has("value")) {
                // 将value字段的文本值设置为节点的引用ID
                nodeInfo.setRefId(firstNode.get("value").asText());
            }
        }
    }


    /**
     * 从输入值中提取模型引用ID并设置到节点信息中
     *
     * @param inputsValues 包含输入数据的JSON节点，预期包含"modelName"字段
     * @param nodeInfo     节点信息对象，用于存储提取的引用ID
     */
    private static void extractModelRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        // 获取模型名称节点
        JsonNode modelName = inputsValues.get("modelName");
        // 检查模型名称是否存在且为非空数组
        if (modelName != null && modelName.isArray() && !modelName.isEmpty()) {
            // 获取数组中的第一个节点
            JsonNode firstNode = modelName.get(0);
            // 检查第一个节点是否包含"value"字段，并提取其值作为引用ID
            if (firstNode.has("value")) {
                nodeInfo.setRefId(firstNode.get("value").asText());
            }
        }
    }


    /**
     * 从输入值中提取工具MCP引用ID并设置到节点信息中
     *
     * @param inputsValues 包含输入数据的JSON节点
     * @param nodeInfo     节点信息对象，用于存储提取的引用ID
     */
    private static void extractToolMcpRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        JsonNode node = inputsValues.get("toolMcpName");
        if (node != null) {
            // 处理数组类型的toolMcpName节点
            if (node.isArray() && !node.isEmpty()) {
                JsonNode firstNode = node.get(0);
                if (firstNode.has("value")) {
                    JsonNode value = firstNode.get("value");
                    nodeInfo.setRefId(value.asText());
                }
                // 处理文本类型的toolMcpName节点
            } else if (node.isTextual()) {
                nodeInfo.setRefId(node.asText());
            }
        }
    }


    /**
     * 从输入值中提取代理引用ID并设置到节点信息中
     *
     * @param inputsValues 包含输入数据的JSON节点
     * @param nodeInfo     节点信息对象，用于存储提取的引用ID
     */
    private static void extractAgentRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        // 获取agentName 当作refId
        JsonNode node = inputsValues.get("agentName");
        if (node != null) {
            // 处理文本类型的agentName
            if (node.isTextual()) {
                nodeInfo.setRefId(node.asText());
                // 处理数组类型的agentName，取第一个元素的value值
            } else if (node.isArray()) {
                JsonNode firstNode = node.get(0);
                if (firstNode != null && firstNode.has("value")) {
                    JsonNode value = firstNode.get("value");
                    if (value != null && value.has("content")) {
                        nodeInfo.setRefId(value.get("content").asText());
                    } else if (value != null && value.isTextual()) {
                        nodeInfo.setRefId(firstNode.get("value").asText());
                    }
                }
            }
        }
    }


    /**
     * 从输入的JSON节点中提取客户端引用ID并设置到节点信息中
     * 优先使用clientId字段的值，如果不存在则尝试从clientName数组中提取第一个元素的value值
     *
     * @param inputsValues 包含输入数据的JSON节点
     * @param nodeInfo     要设置引用ID的节点信息对象
     */
    private static void extractClientRefId(JsonNode inputsValues, NodeInfo nodeInfo) {
        // 优先使用clientId,直接字符串值
        JsonNode clientIdNode = inputsValues.get("clientId");
        if (clientIdNode != null && clientIdNode.isTextual()) {
            nodeInfo.setRefId(clientIdNode.asText());
            return;
        }
        JsonNode clientNameNode = inputsValues.get("clientName");
        if (clientNameNode != null && clientNameNode.isArray() && !clientNameNode.isEmpty()) {
            JsonNode firstItem = clientNameNode.get(0);
            if (firstItem.has("value")) {
                nodeInfo.setRefId(firstItem.get("value").asText());
            }
        }
    }


    private static class NodeInfo {
        private String nodeId;
        private String nodeType;
        private String title;
        private String refId;

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

        @Override
        public String toString() {
            return "NodeInfo{" +
                    "nodeId='" + nodeId + '\'' +
                    ", nodeType='" + nodeType + '\'' +
                    ", title='" + title + '\'' +
                    ", refId='" + refId + '\'' +
                    '}';
        }
    }
}
