package com.liu.soyaojcodesandbox.checker;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;

public class DictionaryTreeFilter implements Check {
    public static final WordTree dicTreeList;
    static {
        dicTreeList = new WordTree();
        dicTreeList.addWord("Files");
        dicTreeList.addWord("exec");
        dicTreeList.addWord("Runtime");
    }

    public boolean checkExist(String code) {
        FoundWord word = dicTreeList.matchWord(code);
        return word != null;
    }
}
