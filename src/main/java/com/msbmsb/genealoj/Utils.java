/******************************************************************************
* Utils
* Utility class for operating on GedcomNodes
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealoj;

import com.msbmsb.genealoj.GedcomNode;

import java.util.List;
import java.util.ArrayList;

public class Utils {
  /**
   * List of tags often referred to
   */
  public static String INDIVIDUAL_TAG = "INDI";
  public static String FAMILY_TAG = "FAM";
  public static String HUSBAND_TAG = "HUSB";
  public static String WIFE_TAG = "WIFE";
  public static String CHILD_TAG = "CHIL";
  public static String PLACE_TAG = "PLAC";
  public static String NAME_TAG = "NAME";

  /**
   * This class is intended to contain static functions, so disable constructing
   */
  private Utils() { }

  /**
   * Get all the individuals found on the given root node
   * @param root the GedcomNode to use as a root for level=0 nodes
   * @return a List<GedcomNode> containing all individual nodes
   */
  public static List<GedcomNode> getIndividuals(GedcomNode root) {
    return root.getChildrenWithTag(INDIVIDUAL_TAG);
  }

  /**
   * Get all the individuals from this root node that have no parents
   * @param root the GedcomNode to use as a root for level=0 nodes
   * @return a List<GedcomNode> containing all individual from root
   *          that do not have parents
   */
  public static List<IndividualNode> getRootIndividuals(GedcomNode root) {
    List<IndividualNode> roots = new ArrayList<IndividualNode>();
    List<GedcomNode> indis = getIndividuals(root);
    for(GedcomNode g : indis) {
      try {
        // attempt to cast to IndividualNode
        IndividualNode i = (IndividualNode) g;
        if(i.getParents().size() == 0) {
          roots.add(i);
        }
      } catch(ClassCastException cce) {
        // if the cast fails, just move on
        continue;
      }
    }
    return roots;
  }

  /**
   * Get the farthest ancestor with the same surname from this node
   * Assumption: follow patrilineal family naming
   * @param node the node from which to search
   * @return IndividualNode for the ancestor
   *         return value will be equal to node param if 
   *         there are no parents or no parents share the surname
   */
  public static IndividualNode getSurnameRoot(IndividualNode node) {
    IndividualNode anc = node;

    List<IndividualNode> parents = node.getParents();
    for(IndividualNode p : parents) {
      if(p.getSurname() == node.getSurname()) {
        anc = getSurnameRoot(p);
        break;
      }
    }

    return anc;
  }

  /**
   * Get all the families found on the given root node
   * @param root the GedcomNode to use as a root for level=0 nodes
   * @return a List<GedcomNode> containing all family nodes
   */
  public static List<GedcomNode> getFamilies(GedcomNode root) {
    return root.getChildrenWithTag(FAMILY_TAG);
  }

  public static List<GedcomNode> getLocations(GedcomNode root) {
    return root.getDescendantsWithTag(PLACE_TAG);
  }

  /**
   * Is this token of a gedcom line a reference?
   * @param tok the token to check
   * @return true if token is a reference;
   *         false otherwise
   */
  public static boolean isReference(String tok) {
    return tok.startsWith("@") && tok.endsWith("@");
  }

  /**
   * Is this token of a gedcom line an individual?
   * @param tok the token to check
   * @return true if token is a individual;
   *         false otherwise
   */
  public static boolean isIndividual(String tok) {
    return tok.equals(INDIVIDUAL_TAG);
  }

  /**
   * Get a list of IndividualNodes using the references found in a family node
   * @param root the GedcomNode to use as a root for level=0 nodes
   * @param famRef list of GedcomNodes that are references in the family node
   */
  public static List<IndividualNode> getIndividualsFromFamRef(GedcomNode root,
                                                      List<GedcomNode> famRef) {
    List<IndividualNode> indis = new ArrayList<IndividualNode>();
    for(GedcomNode n : famRef) {
      String ref = n.data();
      if(isReference(ref)) {
        GedcomNode refNode = root.getReferencedNode(ref);
        if(refNode != null) {
          try {
            // attempt to cast to IndividualNode
            IndividualNode indi = (IndividualNode) refNode;
            indis.add(indi);
          } catch(ClassCastException cce) {
            // if the cast fails, just move on
            continue;
          }
        }
      }
    }

    return indis;
  }
}
