package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: XTY~
 * @CreateTime: 14/3/2023 下午4:17
 * @Description: 敏感词过滤器
 */

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符号
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    // 设置该方法在该bean被容器实例化的时候就先调用，相当于对该bean的初始化
    public void init() {
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                ) {
            String keyword;
            while ((keyword = bufferedReader.readLine()) != null) {
                // 添加到前缀树中去
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }

    }

    /**
     * 将一个敏感词添加到前缀树中去，即前缀树的初始化
     * @param keyword 敏感词
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null) {
                // 判断子节点是否为空 如果为空则进行初始化,赋予初值并且挂载当前节点下
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            // 一次初始化完毕后 直接指向子节点，进入循环
            tempNode = subNode;
            if(i == keyword.length() - 1) {
                // 当遍历到该单词的最后一个时，则设置这个单词的最后一个字符的节点状态为true，表示这是这条路径的叶子结点。
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 带过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 创建指针1
        TrieNode tempNode = rootNode;
        // 创建指针2
        int begin = 0;
        // 创建指针3
        int position = 0;
        // 保存过滤后的文本
        StringBuilder stringBuilder = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            //如果是该字符是符号
            if(isSymbol(c)) {
                // 若指针1处于根节点，则将此字符计入返回结果(忽略符号)，让指针2直接向下走
                if(tempNode == rootNode) {
                    stringBuilder.append(c);
                    begin++;
                }
                // 无论符号在开头还是中间，指针三都向下走
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null) {
                // 以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeyWordEnd()) {
                // 发现敏感词，将begin~position字符串替换掉
                stringBuilder.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
            } else {
                // 检查下一个字符
                position++;
            }
        }
        // 将最后一批字符计入结果
        stringBuilder.append((text.substring(begin)));
        return stringBuilder.toString();
    }

    /**
     * 判断字符是否为特殊符号
     * @param c 字符
     * @return 判断结果
     */
    private boolean isSymbol(Character c) {
        // 0x2E80 ~ 0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     * 前缀树
     */
    private class TrieNode {

        // 关键词结束标识 默认为false表示不是叶子结点
        private boolean isKeyWordEnd = false;

        // 子节点（key是下级字符，value是下级节点）
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            this.isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode  node) {
            subNodes.put(c,node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
