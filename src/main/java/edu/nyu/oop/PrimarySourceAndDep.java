package edu.nyu.oop;

import xtc.tree.GNode;
import java.util.ArrayList;
import edu.nyu.oop.util.JavaFiveImportParser;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;



//Phase one class to hande getting all possible sources and depend

public class PrimarySourceAndDep {

    public static List <GNode> getSourceAndDep(GNode n){
        //Holds all the JavaAST's head nodes found
        ArrayList <GNode> allJavaASTs = new ArrayList<GNode>();
        //Keep track of all the file names found
        ArrayList <String> fileNamesFound = new ArrayList<String>();

        //The nodes remaning to check
        Queue <GNode> remainingNodes = new LinkedList <GNode>();
        //Add first node to remaingNodes
        remainingNodes.add(n);
        while (!remainingNodes.isEmpty()){
            //Nodes exist to check
            //Get the file name of the nodes left to check
            GNode currentNodeToCheck = remainingNodes.poll();

            if (currentNodeToCheck == null)
                continue;
            //System.out.println("current node to check " + currentNodeToCheck);
            String currentFileName = currentNodeToCheck.getLocation().file;
            //Check if we have already gone through the current file
            if(!fileNamesFound.contains(currentFileName)){
                //File name not found, so we parse
                //Add the file name to fileNamesFound
                fileNamesFound.add(currentFileName);
                //Add the node to all JavaAst
                allJavaASTs.add(currentNodeToCheck);
                //Get all the dep for the node
                remainingNodes.addAll(JavaFiveImportParser.parse(currentNodeToCheck));
            }
        }
        return allJavaASTs;
    }
}
