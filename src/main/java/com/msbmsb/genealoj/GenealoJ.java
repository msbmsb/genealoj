/******************************************************************************
* GenealoJ
* The main parsing and container class
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealoj;

import com.msbmsb.genealoj.GedcomNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.List;

public class GenealoJ {
  /**
   * root is the GedcomNode for all level=0 nodes
   */
  public GedcomNode root = new GedcomNode(-1, "ROOT");

  /**
   * Constructor. Given a file name, parses the file and builds a hierarchy
   * of GEDCOM lines represented by the GedcomNode class.
   * @param file filename of input GEDCOM file to parse
   */
  public GenealoJ(String file) {
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(file));
      // begin parsing at the root level=-1 node
      parseGedcom(br, root);
      br.close();
    } catch(FileNotFoundException fne) {
      System.err.println("Input file: " + file + " not found!");
    } catch(IOException ioe) {
      System.err.println("IOException parsing file: " + file);
      ioe.printStackTrace();
    }
  }

  /**
   * Get level=0 nodes of the given tag type
   * @param tag the tag of nodes to return
   * @return List<GedcomNode> list of nodes matching;
   *         null if none found with given tag
   */
  public List<GedcomNode> getNodes(String tag) {
    return root.getChildren(tag);
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
        node.addChild(child);
        parseGedcom(br, child);
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
      if(isReference(toks[1])) {
        // if it is, use contructor with reference
        return new GedcomNode(level, toks[2], restFromTok(line, 2), toks[1]);
      } else {
        // else just construct with data
        return new GedcomNode(level, toks[1], restFromTok(line, 1));
      }
    }
  }

  /**
   * Is this token of a gedcom line a reference?
   * @param tok the token to check
   * @return true if token is a reference;
   *         false otherwise
   */
  public boolean isReference(String tok) {
    return tok.startsWith("@") && tok.endsWith("@");
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
   * Return a string representation of the root node
   */
  public String toString() {
    return root.toString();
  }
}
