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
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;

/**
 *
 * @author gap2
 */
public abstract class GraphModel extends AbstractModel {

    protected DocumentNGramGraph graphModel;
    protected final static NGramCachedGraphComparator COMPARATOR = new NGramCachedGraphComparator();

    public GraphModel(int n, RepresentationModel model, String iName) {
        super(n, model, iName);
    }

    public DocumentNGramGraph getGraphModel() {
        return graphModel;
    }

    public void setModel(String text) {
        noOfDocuments++;
        graphModel.setDataString(text);
    }

    public double getValue(AbstractModel model1, AbstractModel model2) {
        final GraphModel graphModel1 = (GraphModel) model1;
        final GraphModel graphModel2 = (GraphModel) model2;

        final GraphSimilarity graphSimilarity = COMPARATOR.getSimilarityBetween(graphModel1.getGraphModel(), graphModel2.getGraphModel());
        return graphSimilarity.ValueSimilarity;
    }

    @Override
    public double getSimilarity(AbstractModel oModel) {//Value Similarity
        final GraphSimilarity graphSimilarity = COMPARATOR.getSimilarityBetween(this.getGraphModel(), ((GraphModel) oModel).getGraphModel());
        return graphSimilarity.ValueSimilarity;
    }

    public void updateModel(GraphModel model) {
        noOfDocuments++;
        graphModel.merge(model.getGraphModel(), 1.0 - (noOfDocuments - 1.0) / noOfDocuments);
    }
}
