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

package EntityMatching;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruningWithMatching;
import BlockProcessing.ComparisonRefinement.ReciprocalCardinalityNodePruning;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.Attribute;
import DataModel.Comparison;
import DataModel.EntityProfile;
import DataModel.SimilarityPairs;
import Utilities.BlocksPerformance;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import Utilities.TextModels.AbstractModel;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import Utilities.Enumerations.WeightingScheme;
import Utilities.TextModels.CharacterNGrams;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */

public class ProfileWithNeighborMatcher extends AbstractEntityMatching {

    private static final Logger LOGGER = Logger.getLogger(ProfileWithNeighborMatcher.class.getName());
    
    private boolean cleanCleanER = false; 
    
    protected AbstractModel[] entityModelsD1;
    protected AbstractModel[] entityModelsD2;
    
    protected AbstractModel[] neighborModelsD1;
    protected AbstractModel[] neighborModelsD2;
    
    protected AbstractDuplicatePropagation duplicatePropagation;
    
    public ProfileWithNeighborMatcher (RepresentationModel model, SimilarityMetric sMetric) {
        super(model, sMetric);
        LOGGER.log(Level.INFO, "Initializing profile matcher with : {0}", model);
    }

    ProfileWithNeighborMatcher(RepresentationModel repModel, SimilarityMetric sMetric, AbstractDuplicatePropagation duplicatePropagation) {
        this(repModel, sMetric);
        this.duplicatePropagation = duplicatePropagation;
    }
    
    @Override
    public SimilarityPairs executeComparisons(List<AbstractBlock> blocks, 
            List<EntityProfile> profilesD1, List<EntityProfile> profilesD2) {
        if (profilesD1 == null) {
            LOGGER.log(Level.SEVERE, "First list of entity profiles is null! "
                    + "The first argument should always contain entities.");
            System.exit(-1);
        }
        
        entityModelsD1 = getModels(profilesD1);
        neighborModelsD1 = getNeighborModels(profilesD1);
        if (profilesD2 != null) {
            cleanCleanER = true;
            entityModelsD2 = getModels(profilesD2);
            neighborModelsD2 = getNeighborModels(profilesD2);
        }
        
        //meta-blocking
        IBlockProcessing comparisonCleaningMethod = 
                new CardinalityNodePruningWithMatching(WeightingScheme.CBS, entityModelsD1, entityModelsD2, neighborModelsD1, neighborModelsD2);
                //new ReciprocalCardinalityNodePruning(WeightingScheme.CBS, entityModelsD1, entityModelsD2, neighborModelsD1, neighborModelsD2);
        blocks = comparisonCleaningMethod.refineBlocks(blocks);
        
        BlocksPerformance blp = new BlocksPerformance(blocks, duplicatePropagation);
        blp.setStatistics();
        blp.printStatistics();
        
        SimilarityPairs simPairs = new SimilarityPairs(cleanCleanER, blocks);
        for (AbstractBlock block : blocks) {
            final Iterator<Comparison> iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison currentComparison = iterator.next();
                double sim = getSimilarity(currentComparison);
                currentComparison.setUtilityMeasure(sim);
                simPairs.addComparison(currentComparison);
            }
        }
        return simPairs;
    }
    
    private AbstractModel[] getModels(List<EntityProfile> profiles) {
//        if (1 == 1) return null; //only added for debugging
        int counter = 0;
        AbstractModel[] models  = new AbstractModel[profiles.size()];
        for (EntityProfile profile : profiles) {
            models[counter] = RepresentationModel.getModel(representationModel, simMetric, profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                models[counter].updateModel(attribute.getValue());
            }
            counter++;
        }
        return models;
    }
    
    private AbstractModel[] getNeighborModels(List<EntityProfile> profiles) {
        int counter = 0;
        
        Map<String,Set<String>> profilesURLs = new HashMap<>(profiles.size()); //key: entityURL, value: entity values
        for (EntityProfile profile : profiles) {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        }
                
        AbstractModel[] models  = new AbstractModel[profiles.size()];
        for (EntityProfile profile : profiles) {
            models[counter] = new CharacterNGrams(2, RepresentationModel.CHARACTER_BIGRAMS, SimilarityMetric.JACCARD_SIMILARITY, profile.getEntityUrl());
            for (String neighbor: profile.getAllValues()) {
                Set<String> values = profilesURLs.get(neighbor);
                if (values != null) { //then this value is an entity id
                    for (String value : values) { //update the model with its values
                        models[counter].updateModel(value);
                    }
                }
            }
            counter++;
        }
        return models;
    }

    @Override
    public String getMethodInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public double getSimilarity(Comparison comparison) {
        final double a = 0.66;
        double profile_similarity =0;        
        double neighbor_similarity;        
        
        if (cleanCleanER) {
            profile_similarity =  entityModelsD1[comparison.getEntityId1()].getSimilarity(entityModelsD2[comparison.getEntityId2()]);
            neighbor_similarity = neighborModelsD1[comparison.getEntityId1()].getSimilarity(neighborModelsD2[comparison.getEntityId2()]);
        } else {
            profile_similarity = entityModelsD1[comparison.getEntityId1()].getSimilarity(entityModelsD1[comparison.getEntityId2()]);
            neighbor_similarity = neighborModelsD1[comparison.getEntityId1()].getSimilarity(neighborModelsD1[comparison.getEntityId2()]);
        }
        
//        if (neighbor_similarity > 0)
//            System.out.println("The neighbor similarity of "+comparison.getEntityId1()+" and "+comparison.getEntityId2()+" is "+neighbor_similarity);
//        
        return a * profile_similarity + (1-a) * neighbor_similarity;
    }
}