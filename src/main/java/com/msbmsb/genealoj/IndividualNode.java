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

  /**
   * Quick-access container for the individual's surname only
   */
  private String m_surname = "";

  public IndividualNode(int level, String tag, String data, String reference) {
    super(level, tag, data, reference);
  }

  /**
   * Get this individual's surname. Build it if it hasn't been built yet.
   * @return the surname if available, or "" otherwise
   */
  private String getSurname() {
    List<GedcomNode> names = getChildrenWithTag("NAME");
    if(names != null && names.size() > 0) {
      if(m_surname.length() == 0) {
        String fullName = names.get(0).data().trim();
        if(fullName.charAt(fullName.length() - 1) == '/') {
          int surnameBegin = fullName.indexOf('/');
          m_surname = fullName.substring(++surnameBegin, fullName.length() - 1);
        }
      } 
    }

    return m_surname;
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
}