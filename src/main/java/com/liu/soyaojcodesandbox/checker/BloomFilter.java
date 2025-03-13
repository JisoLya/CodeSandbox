package com.liu.soyaojcodesandbox.checker;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BloomFilter implements Check {

    private final BitSet bitArray;
    private final int size;
    private final int numHashes;
    private final int[] primes;

    @Override
    public boolean checkExist(String code) {
        List<String> tokenList = generateTokenList(code);
        for (String token : tokenList) {
            if (this.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> generateTokenList(String code) {
        List<String> tokens = new ArrayList<>();
        String[] words = code.split("\\s+");
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+|[^\\w\\s])");
        for (String word : words) {
            Matcher matcher = pattern.matcher(word);
            if (matcher.find()) {
                tokens.add(word);
            }
        }
        return tokens;
    }


    // 默认构造函数，位数组大小为 1024，哈希函数数量为 3
    public BloomFilter() {
        this(1024, 3);
        String[] blackList = {"Runtime", "Reader", "exec", "Files", "Thread"};
        for (String black : blackList) {
            add(black);
        }
    }

    // 自定义构造函数
    public BloomFilter(int size, int numHashes) {
        this.size = size;
        this.numHashes = numHashes;
        this.bitArray = new BitSet(size);
        // 使用前几个质数作为哈希函数的系数
        int[] primeList = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293};
        this.primes = new int[numHashes];
        for (int i = 0; i < numHashes; i++) {
            primes[i] = primeList[i];
        }
    }

    // 添加字符串到布隆过滤器
    public void add(String s) {
        if (s == null) {
            throw new IllegalArgumentException("字符串不能为 null");
        }
        int hash = s.hashCode();
        int positiveHash = hash & 0x7FFFFFFF; // 确保哈希值为正
        for (int i = 0; i < numHashes; i++) {
            long longHash = (long) positiveHash * primes[i];
            int index = (int) (longHash % size);
            bitArray.set(index);
        }
    }

    // 检查字符串是否可能存在
    public boolean contains(String s) {
        if (s == null) {
            throw new IllegalArgumentException("字符串不能为 null");
        }
        int hash = s.hashCode();
        int positiveHash = hash & 0x7FFFFFFF; // 确保哈希值为正
        for (int i = 0; i < numHashes; i++) {
            long longHash = (long) positiveHash * primes[i];
            int index = (int) (longHash % size);
            if (!bitArray.get(index)) {
                return false;
            }
        }
        return true;
    }
}