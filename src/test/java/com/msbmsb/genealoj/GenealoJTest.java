/******************************************************************************
* GenealoJTest
* Simple junit test for the GenealoJ class
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealoj;

import com.msbmsb.genealoj.GenealoJ;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for GenealoJ.
 */
public class GenealoJTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GenealoJTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GenealoJTest.class );
    }

    /**
     * Rigourous Test
     */
    public void testGenealoJ()
    {
        String testFile = "example/example.ged";
        GenealoJ gedcom = new GenealoJ(testFile);
        System.out.println("Output: ");
        System.out.println(gedcom);
        System.out.println("-------------------------------");
        List<IndividualNode> roots = Utils.getRootIndividuals(gedcom.getRoot());
        for(IndividualNode r : roots) {
          System.out.println(r);
        }
        assertTrue(gedcom.getNodes("INDI").size() == 3);
    }
}

