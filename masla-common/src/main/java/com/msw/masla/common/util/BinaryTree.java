/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.common.util;

/**
 *  
 * @ClassName: com.tree.Tree 
 * @Description: 二叉树的定义 
 * @author Gavin.peng
 * @date 2014-12-8 上午7:51:49 
 *  
 */  
  
public class BinaryTree {  
  
    // 根节点  
    private TreeNode root;  
  
    public TreeNode getRoot() {  
        return root;  
    }  
  
    /** 
     * 插入操作 
     *  
     * @param value 
     */  
    public void insert(int value) {  
  
        TreeNode newNode = new TreeNode(value);  
  
        if (root == null) {  
            root = newNode;  
            root.setLefTreeNode(null);  
            root.setRightNode(null);  
        } else {  
  
            TreeNode currentNode = root;  
            TreeNode parentNode;  
  
            while (true) {  
  
                parentNode = currentNode;  
                // 往右放  
                if (newNode.getValue() > currentNode.getValue()) {  
                    currentNode = currentNode.getRightNode();  
                    if (currentNode == null) {  
                        parentNode.setRightNode(newNode);  
                        return;  
                    }  
                } else {  
                    // 往左放  
                    currentNode = currentNode.getLefTreeNode();  
                    if (currentNode == null) {  
                        parentNode.setLefTreeNode(newNode);  
                        return;  
                    }  
  
                }  
            }  
        }  
    }  
  
    /**
     * 
     * 查找 
     *
     * @param key 
     * @return 
     */  
    public TreeNode find(int key) {  
  
        TreeNode currentNode = root;  
  
        if (currentNode != null) {  
  
            while (currentNode.getValue() != key) {  
  
                if (currentNode.getValue() > key) {  
                    currentNode = currentNode.getLefTreeNode();  
                } else {  
                    currentNode = currentNode.getRightNode();  
                }  
  
                if (currentNode == null) {  
                    return null;  
                }  
  
            }  
  
            if (currentNode.isDelete()) {  
                return null;  
            } else {  
                return currentNode;  
            }  
  
        } else {  
            return null;  
        }  
  
    }


    /**
     * 二叉搜索树中（没有重复值时） 查找比target小的最大值
     * @param node
     * @param target
     */

    public Integer findMaxNodeLessThanTarget(TreeNode node, int target){
        if(node == null)return null;
        if(node.getValue() == target){
            return node.getValue();
        }
        if(node.getValue() > target){
            return findMaxNodeLessThanTarget(node.getLefTreeNode(), target);
        } else {
            Integer x= findMaxNodeLessThanTarget(node.getRightNode(), target);
            return (x == null) ? node.getValue() : x;
        }
    }

    /** 
     * 中序遍历 
     *  
     * @param treeNode 
     */  
    public void inOrder(TreeNode treeNode) {  
        if (treeNode != null && treeNode.isDelete() == false) {  
            inOrder(treeNode.getLefTreeNode());  
            System.out.print("--" + treeNode.getValue());
            inOrder(treeNode.getRightNode());  
        }  
    }

    public int height(TreeNode root){
        if (root == null ){
            return 0;
        }
        int r = height(root.getRightNode());
        int l = height(root.getLefTreeNode());
        return r>l? (r+1):(l+1);
    }


    public static void main(String[] args){
        BinaryTree tree = new BinaryTree();
        // 添加数据测试
        tree.insert(10);
        tree.insert(40);
        tree.insert(20);
        tree.insert(3);
        tree.insert(49);
        tree.insert(13);
        tree.insert(123);

        System.out.println("root=" + tree.getRoot().getValue());
        // 排序测试
        tree.inOrder(tree.getRoot());
        // 查找测试
        if (tree.find(10) != null) {
            System.out.println("找到了");
        } else {
            System.out.println("没找到");
        }



        int target = tree.findMaxNodeLessThanTarget(tree.getRoot(),4);
        System.out.println("找到:"+target);
//        // 删除测试
//        tree.find(40).setDelete(true);
//
//        if (tree.find(40) != null) {
//            System.out.println("找到了");
//        } else {
//            System.out.println("没找到");
//        }
    }
  
}  