/******************************************************************************
* GedcomNode
* A generic node for a gedcom line. 
* Holds level, tag, reference, data and children nodes.
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealoj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Generic node for holding and traversing through a GEDCOM line
 */
public class GedcomNode {
  private int m_level;
  private String m_tag;
  private String m_reference = null;
  private String m_data = null;
  private Map<String, List<GedcomNode> > m_children = new HashMap<String, List<GedcomNode> >();

  /**
   * Constructors
   * Basic: level and tag
   * Alternate: level, tag, data
   * Alternate: level, tag, data, reference
   */
  public GedcomNode(int level, String tag) {
    m_level = level;
    m_tag = tag;
  }

  public GedcomNode(int level, String tag, String data) {
    m_level = level;
    m_tag = tag;
    m_data = data;
  }

  public GedcomNode(int level, String tag, String data, String reference) {
    m_level = level;
    m_tag = tag;
    m_data = data;
    m_reference = reference;
  }

  /**
   * @return level of this node
   */
  public int level() {
    return m_level;
  }

  /**
   * @return tag of this node
   */
  public String tag() {
    return m_tag;
  }

  /**
   * Set the reference for this node
   * @param ref the reference element of this node
   */
  public void reference(String ref) {
    m_reference = ref;
  }

  /**
   * @return reference for this node
   */
  public String reference() {
    return m_reference;
  }

  /**
   * Set the data for this node
   * @param data the data element of this node
   */
  public void data(String data) {
    m_data = data;
  }

  /**
   * @return data element of this node
   */
  public String data() {
    return m_data;
  }

  /**
   * Add a GedcomNode as a child of this node
   * @param child the node that is to be set as child
   */
  public void addChild(GedcomNode child) {
    List<GedcomNode> nodes = getChildren(child.tag());
    if(nodes == null) {
      m_children.put(child.tag(), nodes = new ArrayList<GedcomNode>());
    }
    nodes.add(child);
  }

  /**
   * Get the list of all children nodes given a tag
   * @param tag the tag to retrieve on
   * @return List<GedcomNode> of nodes matching tag;
   *         null if no matches found
   */
  public List<GedcomNode> getChildren(String tag) {
    return m_children.get(tag);
  }

  /**
   * Build a string representation of this node and its children
   * This builds a string in the GEDCOM format just as it was input
   * but will not necessarily return in the same order as it was given
   * @return string representation of this node and children
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(m_level);
    sb.append(" ");
    // if this node is at level=0 and has a reference, it should be 
    // printed reference before tag
    if(m_level == 0 && m_reference != null) {
      sb.append(m_reference);
      sb.append(" ");
      sb.append(m_tag);
      sb.append(" ");
    } else {
      // otherwise always print tag before reference
      sb.append(m_tag);
      sb.append(" ");
      if(m_reference != null) {
        sb.append(m_reference);
        sb.append(" ");
      }
    }
    // print the data last 
    if(m_data != null) {
      sb.append(m_data);
    }

    // iterate through children and recurse
    Iterator nodes = m_children.entrySet().iterator();
    while(nodes.hasNext()) {
      sb.append("\n");
      sb.append(((Map.Entry)nodes.next()).getValue().toString());
    }

    return sb.toString();
  }
}
