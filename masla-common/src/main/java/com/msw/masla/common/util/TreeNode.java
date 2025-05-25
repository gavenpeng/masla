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
 * @ClassName: com.tree.TreeNode 
 * @Description: 节点 
 * @author Gavin.peng
 * @date 2014-12-5 下午4:43:24 
 *  
 */  
public class TreeNode {  
  
    // 左节点  
    private TreeNode lefTreeNode;  
    // 右节点  
    private TreeNode rightNode;  
    // 数据  
    private int value;  
  
    private boolean isDelete;  
  
    public TreeNode getLefTreeNode() {  
        return lefTreeNode;  
    }  
  
    public void setLefTreeNode(TreeNode lefTreeNode) {  
        this.lefTreeNode = lefTreeNode;  
    }  
  
    public TreeNode getRightNode() {  
        return rightNode;  
    }  
  
    public void setRightNode(TreeNode rightNode) {  
        this.rightNode = rightNode;  
    }  
  
    public int getValue() {  
        return value;  
    }  
  
    public void setValue(int value) {  
        this.value = value;  
    }  
  
    public boolean isDelete() {  
        return isDelete;  
    }  
  
    public void setDelete(boolean isDelete) {  
        this.isDelete = isDelete;  
    }  
  
    public TreeNode() {  
        super();  
    }  
  
    public TreeNode(int value) {  
        this(null, null, value, false);  
    }  
  
    public TreeNode(TreeNode lefTreeNode, TreeNode rightNode, int value,  
            boolean isDelete) {  
        super();  
        this.lefTreeNode = lefTreeNode;  
        this.rightNode = rightNode;  
        this.value = value;  
        this.isDelete = isDelete;  
    }  
  
    @Override  
    public String toString() {  
        return "TreeNode [lefTreeNode=" + lefTreeNode + ", rightNode="  
                + rightNode + ", value=" + value + ", isDelete=" + isDelete  
                + "]";  
    }  
  
}  