/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2021-present Arcade Data Ltd (info@arcadedata.com)
 * SPDX-License-Identifier: Apache-2.0
 */
/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import java.util.Map;

public class SimpleNode implements Node {
  protected Node      parent;
  protected Node[]    children;
  protected int       id;
  protected Object    value;
  protected SqlParser parser;
  protected Token     firstToken;
  protected Token     lastToken;

  public SimpleNode() {
    id = -1;
  }

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(SqlParser p, int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(Node n) {
    parent = n;
  }

  public Node jjtGetParent() {
    return parent;
  }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node[] c = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  public void jjtSetValue(Object value) {
    this.value = value;
  }

  public Object jjtGetValue() {
    return value;
  }

  public Token jjtGetFirstToken() {
    return firstToken;
  }

  public void jjtSetFirstToken(Token token) {
    this.firstToken = token;
  }

  public Token jjtGetLastToken() {
    return lastToken;
  }

  public void jjtSetLastToken(Token token) {
    this.lastToken = token;
  }

  /**
   * Accept the visitor.
   **/
  public final Object jjtAccept(final SqlParserVisitor visitor, final Object data) {
    return visitor.visit(this, data);
  }

  /**
   * Accept the visitor.
   **/
  public Object childrenAccept(SqlParserVisitor visitor, Object data) {
    if (children != null) {
        for (Node child : children) {
            child.jjtAccept(visitor, data);
        }
    }
    return data;
  }

  /*
   * You can override these two methods in subTypes of SimpleNode to customize the way the node appears when the tree is dumped.
   * If your output uses more than one line you should override toString(String), otherwise overriding toString() is probably all
   * you need to do.
   */

  public String toString() {
    StringBuilder result = new StringBuilder();
    toString(null, result);
    return result.toString();
  }

  public String toString(String prefix) {
    return prefix + this;
  }

  /*
   * Override this method if you want to customize how the node dumps out its children.
   */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
        for (Node child : children) {
            SimpleNode n = (SimpleNode) child;
            if (n != null) {
                n.dump(prefix + " ");
            }
        }
    }
  }

  public void toString(Map<String, Object> params, StringBuilder builder) {
    throw new UnsupportedOperationException("not implemented in " + getClass().getSimpleName());
  }

  public Object getValue() {
    return value;
  }

  public SimpleNode copy() {
    throw new UnsupportedOperationException();
  }
}

/* JavaCC - OriginalChecksum=d5ed710e8a3f29d574adbb1d37e08f3b (do not edit this line) */
