/******************************************************************************
* IndividualNode
* Extending GedcomNode, used to hold individual-specific information such as
* family links, name and personal information for wasy access and organization.
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealoj;

import com.msbmsb.genealoj.GedcomNode;

import java.util.List;
import java.util.ArrayList;

public class IndividualNode extends GedcomNode {
  /**
   * List of other IndividualNodes that are spouses
   */
  private List<IndividualNode> m_spouses = new ArrayList<IndividualNode>();

  /**
   * List of other IndividualNodes that are parents
   */
  private List<IndividualNode> m_parents = new ArrayList<IndividualNode>();

  /**
   * List of other IndividualNodes that are children
   */
  private List<IndividualNode> m_children = new ArrayList<IndividualNode>();

  /**
   * Quick-access list of family GedcomNodes that this individual is a part of
   */
  private List<GedcomNode> m_families = new ArrayList<GedcomNode>();
  private List<GedcomNode> m_familiesAsHead = new ArrayList<GedcomNode>();
  private List<GedcomNode> m_familiesAsChild = new ArrayList<GedcomNode>();

  /**
   * Quick-access container for the individual's surname only
   */
  private String m_surname = "";
  private String m_fullName = "";

  public IndividualNode(int level, String tag, String data, String reference) {
    super(level, tag, data, reference);
  }

  /**
   * Get this individual's surname. Build it if it hasn't been built yet.
   * @return the surname if available, or "" otherwise
   */
  public String getSurname() {
    List<GedcomNode> names = getChildrenWithTag("NAME");
    if(names != null && names.size() > 0) {
      if(m_surname.length() == 0) {
        m_fullName = names.get(0).data().trim();
        if(m_fullName.charAt(m_fullName.length() - 1) == '/') {
          int surnameBegin = m_fullName.indexOf('/');
          m_surname = m_fullName.substring(++surnameBegin, m_fullName.length() - 1);
        }
      } 
    }

    return m_surname;
  }

  public String getFullName() {
    return m_fullName;
  }

  /**
   * Once all children node have been finalized, extract individual information
   */
  public void finalize() {
    m_surname = getSurname();
  }

  /**
   * Add a family node to this individual
   * @param family
   */
  public void addFamily(GedcomNode family) {
    m_families.add(family);
  }

  /**
   * Add a family node to this individual 
   * as a head of family (husband/wife/parent)
   * @param family
   */
  public void addFamilyAsHead(GedcomNode family) {
    m_familiesAsHead.add(family);
  }

  /**
   * Add a family node to this individual
   * as a child of the family
   * @param family
   */
  public void addFamilyAsChild(GedcomNode family) {
    m_familiesAsChild.add(family);
  }

  /**
   * Add an individual node to this individual
   * @param spouse
   */
  public void addSpouse(IndividualNode spouse) {
    m_spouses.add(spouse);
  }

  /**
   * Add a list of individual nodes to this individual
   * @param spouses
   */
  public void addSpouses(List<IndividualNode> spouses) {
    for(IndividualNode s : spouses) {
      if(s != this) {
        m_spouses.add(s);
      }
    }
  }

  /**
   * Add a list of individual nodes to this individual
   * @param parents
   */
  public void addParents(List<IndividualNode> parents) {
    m_parents.addAll(parents);
  }

  /**
   * Add a list of individual nodes to this individual
   * @param children 
   */
  public void addChildren(List<IndividualNode> children) {
    m_children.addAll(children);
  }

  /**
   * Get list of spouses for this individual
   */
  public List<IndividualNode> getSpouses() {
    return m_spouses;
  }

  /**
   * Get list of parents for this individual
   */
  public List<IndividualNode> getParents() {
    return m_parents;
  }

  /**
   * Get list of children for this individual
   */
  public List<IndividualNode> getChildren() {
    return m_children;
  }

  /**
   * Return a representative location for this individual
   * Return birth location if available
   * else return death location
   * else return any other location
   */
  public GedcomNode getLocation() {
    GedcomNode loc = getLocation("BIRT");
    if(loc == null) {
      loc = getLocation("DEAT");
      if(loc == null) {
        List<GedcomNode> any_plac = getChildrenWithTag("PLAC");
        if(any_plac != null) {
          loc = any_plac.get(0);
        }
      }
    }

    return loc;
  }

  public GedcomNode getLocation(String type) {
    List<GedcomNode> top = getChildrenWithTag(type);
    if(top != null) {
      List<GedcomNode> top_plac = top.get(0).getChildrenWithTag("PLAC");
      if(top_plac != null) {
        return top_plac.get(0);
      }
    }
    return null;
  }
}
