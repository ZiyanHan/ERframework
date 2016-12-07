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

import DataModel.AbstractBlock;
import DataModel.Attribute;
import DataModel.Comparison;
import DataModel.EntityProfile;
import DataModel.SimilarityPairs;
import Utilities.TextModels.AbstractModel;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class ProfileMatcher extends AbstractEntityMatching {

    private static final Logger LOGGER = Logger.getLogger(ProfileMatcher.class.getName());

    protected AbstractModel[] entityModelsD1;
    protected AbstractModel[] entityModelsD2;

    public ProfileMatcher(RepresentationModel model, SimilarityMetric simMetric) {
        super(model, simMetric);

        LOGGER.log(Level.INFO, "Initializing profile matcher with : {0}", model);
    }
    
    public ProfileMatcher(RepresentationModel model) {
        this(model,SimilarityMetric.getModelDefaultSimMetric(model));
    }

    @Override
    public SimilarityPairs executeComparisons(List<AbstractBlock> blocks,
            List<EntityProfile> profilesD1, List<EntityProfile> profilesD2) {
        if (profilesD1 == null) {
            LOGGER.log(Level.SEVERE, "First list of entity profiles is null! "
                    + "The first argument should always contain entities.");
            System.exit(-1);
        }

        isCleanCleanER = false;
        entityModelsD1 = getModels(profilesD1);
        if (profilesD2 != null) {
            isCleanCleanER = true;
            entityModelsD2 = getModels(profilesD2);
        }

        final SimilarityPairs simPairs = new SimilarityPairs(isCleanCleanER, blocks);
        for (AbstractBlock block : blocks) {
            final Iterator<Comparison> iterator = block.getComparisonIterator();
            while (iterator.hasNext()) {
                Comparison currentComparison = iterator.next();
                currentComparison.setUtilityMeasure(getSimilarity(currentComparison));
                simPairs.addComparison(currentComparison);
            }
        }
        return simPairs;
    }

    private AbstractModel[] getModels(List<EntityProfile> profiles) {
        int counter = 0;
        final AbstractModel[] models = new AbstractModel[profiles.size()];
        for (EntityProfile profile : profiles) {
            models[counter] = RepresentationModel.getModel(representationModel, simMetric, profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                models[counter].updateModel(attribute.getValue());
            }
            counter++;
        }
        return models;
    }

    @Override
    public String getMethodInfo() {
        return "ProfileMatcher : it aggregates all attribute values of each entity profile "
                + "into a representation model and compares them according to the given similarity metric.";
    }

    @Override
    public String getMethodParameters() {
        return "The Profile Matcher involves 2 parameters:\n"
             + "1) representation model : character- or token-based bag or graph model.\n"
             + "It determines the building modules that form the model of all attribute values in an entity profile.\n"
             + "2) similarity metric : bag or graph similarity metric.\n"
             + "It determines the measure that estimates the similarity of two entity profiles.\n";
    }

    public double getSimilarity(Comparison comparison) {
        if (isCleanCleanER) {
            return entityModelsD1[comparison.getEntityId1()].getSimilarity(entityModelsD2[comparison.getEntityId2()]);
        }

        return entityModelsD1[comparison.getEntityId1()].getSimilarity(entityModelsD1[comparison.getEntityId2()]);
    }
}
