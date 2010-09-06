/******************************************************************************
* GenealoJ
* The main parsing and container class
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealoj;

import com.msbmsb.genealoj.GedcomNode;
import com.msbmsb.genealoj.IndividualNode;
import com.msbmsb.genealoj.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class GenealoJ {
  /**
   * m_parseRoot is the GedcomNode for all level=0 nodes
   */
  private GedcomNode m_parseRoot = new GedcomNode(-1, "ROOT");

  /**
   * File source for current gedcom parse
   */
  private File m_gedcomFile = null;

  /**
   * Check for valid initialization of input file
   */
  private boolean m_initialized = false;

  /**
   * Constructor. Given a file name, parses the file and builds a hierarchy
   * of GEDCOM lines represented by the GedcomNode class.
   * @param file String filename of input GEDCOM file to parse
   */
  public GenealoJ(String file) {
    m_gedcomFile = new File(file);
    m_initialized = init();
  }

  /**
   * Constructor. Given a File object, parses the file and builds a hierarchy
   * of GEDCOM lines represented by the GedcomNode class.
   * @param file File of input GEDCOM file to parse
   */
  public GenealoJ(File file) {
    m_gedcomFile = file;
    m_initialized = init();
  }

  /**
   * Private initialization method for loading input GEDCOM file
   * Private to enforce a one-parser-per-file contract
   * and setting any necessary parameters
   * @return boolean of initialized status
   */
  private boolean init() {
    boolean initialized = true;
    initialized &= load(m_gedcomFile);

    return initialized;
  }

  /**
   * Private method for loading the input GEDCOM file
   * @param file File to load
   * @return boolean of successful load
   */
  private boolean load(File file) {
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(file));
      // begin parsing at the m_parseRoot level=-1 node
      parseGedcom(br, m_parseRoot);
      br.close();
      // now link individuals in the genealogy graph through their families
      linkIndividuals(m_parseRoot);
      return true;
    } catch(FileNotFoundException fne) {
      System.err.println("Input file: " + file + " not found!");
      return false;
    } catch(IOException ioe) {
      System.err.println("IOException parsing file: " + file);
      ioe.printStackTrace();
      return false;
    }
  }

  /**
   * Get level=0 nodes of the given tag type
   * @param tag the tag of nodes to return
   * @return List<GedcomNode> list of nodes matching;
   *         null if none found with given tag
   */
  public List<GedcomNode> getNodes(String tag) {
    return m_parseRoot.getChildrenWithTag(tag);
  }
  
  /**
   * Parse given the input Reader and current node.
   * For any level, l &gt; node.level(), creates a new GedcomNode and adds as 
   * child to node. Otherwise, returns leaving the Reader pointing at the
   * line containing the ignored level.
   * @param br BufferedReader pointing to the next file line
   * @param node GedcomNode current node in which to add children nodes
   */
  public void parseGedcom(BufferedReader br, GedcomNode node) 
    throws IOException {
    while(br.ready()) {
      // store current place in stream for peeking
      br.mark(1);
      // peek ahead for the next level
      int nextLevel = Integer.parseInt(String.valueOf((char)br.read()));

      // sanity check
      if(nextLevel < 0) {
        throw new IOException("Error parsing file, level < 0 encountered");
      }

      // reset to front of line
      br.reset();
      // only parse levels greater than this node
      if(nextLevel > node.level()) {
        // here, advance the BufferedReader position
        GedcomNode child = buildGedcomNode(br.readLine().trim());
        // add new node as child to current node and recurse on new node
        parseGedcom(br, child);
        node.addChildNode(child);
        child.finalize();
      } else {
        // backtrack to parent node
        return;
      }
    }
  }

  /**
   * Given a GEDCOM file line, return a GedcomNode object containing
   * the appropriate data members set.
   * @param line the line of gedcom data to digest
   * @return GedcomNode object initialized appropriately
   */
  public GedcomNode buildGedcomNode(String line) {
    String[] toks = line.split(" ");
    assert(toks.length > 1);
    int level = Integer.parseInt(toks[0]);
    if(toks.length == 2) {
      // basic constructor
      return new GedcomNode(level, toks[1]);
    } else {
      // is this a reference
      if(Utils.isReference(toks[1])) {
        // if it is, use contructor with reference
        if(Utils.isIndividual(toks[2])) {
          return new IndividualNode(level, toks[2], restFromTok(line, 2), toks[1]);
        } else {
          return new GedcomNode(level, toks[2], restFromTok(line, 2), toks[1]);
        }
      } else {
        // else just construct with data
        return new GedcomNode(level, toks[1], restFromTok(line, 1));
      }
    }
  }

  /**
   * Build the data portion of a gedcom line by ignoring tokens up to the
   * index of token tokIndex
   * For example:
   *   line = 1 PLAC Saturn
   *   tokIndex = 1
   * will return 'Saturn'. 'PLAC' is the space-delimited token at tokIndex=1.
   * @param line the whole gedcom line
   * @param tokIndex the index of the space-delimited token to begin with
   * @return the string from tokIndex to end of string
   */
  public String restFromTok(String line, int tokIndex) {
    int i=0;
    int index = line.indexOf(" ");
    while(i<tokIndex && index > 0) {
      index = line.indexOf(" ", index+1);
      i++;
    }

    try {
      return (index > 0) ? line.substring(index+1) : "";
    } catch(IndexOutOfBoundsException e) {
      return "";
    }
  }

  /**
   * Link all the individuals found in the given root through their families
   * @param root the GedcomNode to begin with
   */
  public void linkIndividuals(GedcomNode root) {
    List<GedcomNode> families = Utils.getFamilies(root);

    for(GedcomNode family : families) {
      // build a list of parents and children nodes. 
      // These nodes are not the individuals, but are the references to 
      // individuals in the FAM nodes
      // calling linkFamily will handle getting the actual individuals
      List<GedcomNode> parents = new ArrayList<GedcomNode>();
      List<GedcomNode> children = new ArrayList<GedcomNode>();

      // get all the necessary reference nodes
      parents.addAll(family.getChildrenWithTag(Utils.HUSBAND_TAG));
      parents.addAll(family.getChildrenWithTag(Utils.WIFE_TAG));
      children.addAll(family.getChildrenWithTag(Utils.CHILD_TAG));

      // now link the IndividualNodes through the family
      linkFamily(root, family, parents, children);
    }
  }

  /**
   * Given the parameters, link the IndividualNodes for individuals in the
   * genealogy graph with each other through the references in the FAM
   * family nodes
   * @param root the GedcomNode to be used as the root location of level=0
   * @param family GedcomNode of the FAM node
   * @param parents list of reference GedcomNodes pointing to husband &amp; wife
   * @param children list of reference GedcomNodes pointint to children
   */
  public void linkFamily(GedcomNode root, GedcomNode family, 
                          List<GedcomNode> parents, 
                          List<GedcomNode> children) {
    // use Utils to get the IndividualNodes for the members of this family
    List<IndividualNode> pIndi = Utils.getIndividualsFromFamRef(root, parents);
    List<IndividualNode> cIndi = Utils.getIndividualsFromFamRef(root, children);

    // for each parent/husband/wife of family:
    // add family reference
    // add children list
    // add rest of spouses
    for(IndividualNode p : pIndi) {
      p.addFamily(family);
      p.addChildren(cIndi);
      p.addSpouses(pIndi);
    }

    // for each child:
    // add family reference
    // add parent list
    for(IndividualNode c : cIndi) {
      c.addFamily(family);
      c.addParents(pIndi);
    }
  }

  /**
   * Return a string representation of the m_parseRoot node
   */
  public String toString() {
    return m_parseRoot.toString();
  }
}
