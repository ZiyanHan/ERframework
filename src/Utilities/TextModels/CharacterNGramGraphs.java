/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package Utilities.TextModels;

import Utilities.Enumerations.RepresentationModel;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramHGraph;

/**
 *
 * @author gap2
 */

public class CharacterNGramGraphs extends GraphModel {
    
    private final static int SEGMENTS_UNIT = 100;
    
    public CharacterNGramGraphs (int n, RepresentationModel model, String iName) {
        super(n, model, iName);
        
        graphModel = new DocumentNGramHGraph(nSize, nSize, nSize, nSize*SEGMENTS_UNIT);
    }
    
    @Override
    public void updateModel(String text) {
        final DocumentNGramGraph tempGraph = new DocumentNGramGraph(nSize, nSize, nSize);
        tempGraph.setDataString(text);
        
        noOfDocuments++;
        graphModel.merge(tempGraph, 1 - (noOfDocuments-1)/noOfDocuments);
    }
}