/**
 *  Implementation for basic and compacted suffix trees.
 *
 *  @authors Silvia Usón: 681721 at unizar dot es
 *           Álvaro Monteagudo: 681060 at unizar dot es
 *
 *  @version 1.0
 *
 */

import java.util.ArrayList;

/**
 * Implementation of a basic suffix tree
 */
class SuffixTree {

    // Root of the tree
    private SuffixTreeNode root;

    /**
     * Suffix tree constructor
     * @param word from which the tree is built.
     */
    SuffixTree(String word) {
        root = new SuffixTreeNode(-1);

        addWord(word);
    }

    public void addWord(String word) {
        for (int i = 1; i < word.length() - 1; ++i) {
            SuffixTreeNode current = root;
            int len = 0;

            // Check how many characters are already in the tree
            while (true) {
                SuffixTreeNode lastValidNode = current;

                SuffixTreeNode children = current.getChildren(word.charAt(i + len), word);

                if (children != null) {
                    lastValidNode.updateLeftDiverse(i, word);
                    current = children;
                    len++;
                } else {
                    break;
                }
            }

            // Update if proceeds
            if (!current.isLeftDiverse) {
                current.updateLeftDiverse(i, word);
            }

            // Add new characters that are not in the tree yet.
            for (int j = i + len; j < word.length(); j++) {
                current = current.addChildren(j, i);
            }
        }
    }

    // Root of the tree
    public SuffixTreeNode getRoot() {
        return root;
    }
}

/**
 * Implementation of a basic compacted suffix tree
 */
class CompactSuffixTree {

    // Word that represents the treee
    private String string;

    // Root node of the tree
    private CompactSuffixTreeNode root;

    // List with nodes marked as maximals
    private ArrayList<CompactSuffixTreeNode> maximals;

    // Index of the word where longest repeated substring starts
    private int indexLongestSubstring = 0;

    // Node where longest repeated substring starts
    private CompactSuffixTreeNode nodeLongestSubstring = null;

    /**
     * Constructor for compacted suffix tree
     * @param word from which tree is built
     */
    CompactSuffixTree(String word) {
        string = "$" + word + "$";
        maximals = new ArrayList<>();
        // N CUADRADO
        /*SuffixTree tree = new SuffixTree(string);
        root = generateCompactSuffixTree(tree.getRoot(), 0);*/
        // N LOG N
        root = new CompactSuffixTreeNode(-1, -1, false, 0);
        for (int i = 1; i < string.length() - 1; i++) {
            insertNewNode(root, i, i);
        }
    }

    private void insertNewNode(CompactSuffixTreeNode root, int from, int startIndexInWord) {
        CompactSuffixTreeNode node = root;

        int charactersInTree = 0; // Characters already in the tree
        int idx = -1;

        for (int i = 0; i < root.children.size(); i++) {
            CompactSuffixTreeNode child = root.children.get(i);
            if (string.charAt(from) == string.charAt(child.begin)) {
                node = child;
                idx = i;
                while (child.begin + charactersInTree < child.end &&
                        string.charAt(child.begin + charactersInTree) ==
                        string.charAt(from + charactersInTree)) {
                        charactersInTree++;
                }
                break;
            }
        }

        // None child matched the character, insert new node
        if (idx == -1) {
            root.children.add(new CompactSuffixTreeNode(from, string.length() - 1, false, startIndexInWord));
        } else if (node.begin + charactersInTree <= node.end) {
            int start = from, end = from + charactersInTree - 1;
            char leftChar = (node.indexStartPath < 1) ? ' ' : string.charAt(node.indexStartPath - 1);
            char rightChar = (startIndexInWord < 1) ? ' ' : string.charAt(startIndexInWord - 1);
            boolean flag = node.isLeftDiverse || leftChar != rightChar;
            CompactSuffixTreeNode newNode = new CompactSuffixTreeNode(start, end, flag, startIndexInWord);
            
            if (newNode.isLeftDiverse) {
                maximals.add(newNode);
            }

            int newDepth = from + charactersInTree - startIndexInWord;
            if (newNode.children.size() != 0) {
                if (newDepth > indexLongestSubstring) {
                    indexLongestSubstring = newDepth;
                    nodeLongestSubstring = newNode;
                }
            }

            node.begin += charactersInTree;
            root.children.set(idx, newNode);
            newNode.children.add(node);

            insertNewNode(newNode, from + charactersInTree, startIndexInWord);
        
        } else {
            char leftChar = (node.indexStartPath < 1) ? ' ' : string.charAt(node.indexStartPath - 1);
            char rightChar = (startIndexInWord < 1) ? ' ' : string.charAt(startIndexInWord - 1);
            if (!node.isLeftDiverse && leftChar != rightChar) {
                node.isLeftDiverse = true;
                maximals.add(node);
            }
            insertNewNode(node, from + charactersInTree, startIndexInWord);
        }
    }

    /**
     * Constructor for compacted suffix tree
     * @param words from which tree is built
     */
    CompactSuffixTree(String ... words) {
        string = "$" + words[0] + "$";
        SuffixTree tree = new SuffixTree(string);
        for (int i = 1; i < words.length; i++) {
            string = "$" + words[i] + "$";
            tree.addWord(string);
        }
        maximals = new ArrayList<>();
        root = generateCompactSuffixTree(tree.getRoot(), 0);
    }

    /**
     * Creation of compacted tree
     * @param node root of suffix tree
     * @param depth we are in the tree
     * @return root of the compacted suffix tree
     */
    private CompactSuffixTreeNode generateCompactSuffixTree(SuffixTreeNode node, int depth) {
        CompactSuffixTreeNode result;

        int start = node.position;

        // Compact into one single node all consecutive nodes with one children
        while (node.children.size() == 1) {
            node = node.children.get(0);
        }

        int end = node.position;

        result = new CompactSuffixTreeNode(start, end, node.isLeftDiverse, node.indexStartPath);

        if (depth != 0 && node.children.size() > 0 && node.isLeftDiverse) {
            maximals.add(result);
        }

        int newDepth = depth + end - start;

        for (SuffixTreeNode children: node.children) {
            result.children.add(generateCompactSuffixTree(children, newDepth + 1));
        }

        if (result.children.size() != 0) {
            if (newDepth > indexLongestSubstring) {
                indexLongestSubstring = newDepth;
                nodeLongestSubstring = result;
            }
        }

        return result;
    }

    /**
     * @return longest repeated substring stored in the tree
     */
    String getLongestSubstring() {
        if (nodeLongestSubstring != null) {
            int start = nodeLongestSubstring.indexStartPath;
            int end =  nodeLongestSubstring.end;
            int len = end - start + 1;
            return string.substring(start, start + len);
        } else {
            return "";
        }
    }

    /**
     * @return list with all the maximals of the tree
     */
    ArrayList<String> getMaximals() {
        ArrayList<String> result = new ArrayList<>();
        for (CompactSuffixTreeNode node: maximals) {
            int start = node.indexStartPath;
            int end =  node.end;
            int len = end - start + 1;
            result.add(string.substring(start, start + len));
        }
        return result;
    }
}
