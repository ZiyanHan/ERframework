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

package BlockBuilding;

import DataModel.BilateralBlock;
import DataModel.UnilateralBlock;
import Utilities.Converter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

/**
 *
 * @author gap2
 */
public class ExtendedSortedNeighborhoodBlocking extends SortedNeighborhoodBlocking {

    private static final Logger LOGGER = Logger.getLogger(ExtendedSortedNeighborhoodBlocking.class.getName());
    
    public ExtendedSortedNeighborhoodBlocking() {
        this(2);
        LOGGER.log(Level.INFO, "Using default configuration for Extended Sorted Neighborhood Blocking.");
    }
    
    public ExtendedSortedNeighborhoodBlocking(int w) {
        super(w);
    }

    @Override
    public String getMethodInfo() {
        return "Extended Sorted Neighborhood: it improves Sorted Neighborhood by sliding the window over the sorted list of blocking keys.";
    }

    @Override
    public String getMethodParameters() {
        return "Extended Sorted Neighborhood involves a single parameter, due to its unsupervised, schema-agnostic blocking keys:\n"
                + "w, the fixed size of the window that slides over the sorted list of blocking keys.\n"
                + "Default value: 2.";
    }
    
    @Override
    protected void parseIndex(IndexReader iReader) {
        final Set<String> blockingKeysSet = getTerms(iReader);
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        //slide window over the sorted list of blocking keys
        int upperLimit = sortedTerms.length - windowSize;
        int[] documentIds = getDocumentIds(iReader);
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                entityIds.addAll(getTermEntities(documentIds, iReader, sortedTerms[i + j]));
            }

            if (1 < entityIds.size()) {
                int[] idsArray = Converter.convertCollectionToArray(entityIds);
                UnilateralBlock uBlock = new UnilateralBlock(idsArray);
                blocks.add(uBlock);
            }
        }
    }

    @Override
    protected void parseIndices(IndexReader iReader1, IndexReader iReader2) {
        final Set<String> blockingKeysSet = getTerms(iReader1);
        blockingKeysSet.addAll(getTerms(iReader2));
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        //slide window over the sorted list of blocking keys
        int upperLimit = sortedTerms.length - windowSize;
        int[] documentIdsD1 = getDocumentIds(iReader1);
        int[] documentIdsD2 = getDocumentIds(iReader2);
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds1 = new HashSet<>();
            final Set<Integer> entityIds2 = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                try {
                    int docFrequency = iReader1.docFreq(new Term(VALUE_LABEL, sortedTerms[i + j]));
                    if (0 < docFrequency) {
                        entityIds1.addAll(getTermEntities(documentIdsD1, iReader1, sortedTerms[i + j]));
                    }

                    docFrequency = iReader2.docFreq(new Term(VALUE_LABEL, sortedTerms[i + j]));
                    if (0 < docFrequency) {
                        entityIds2.addAll(getTermEntities(documentIdsD2, iReader2, sortedTerms[i + j]));
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }

            if (!entityIds1.isEmpty() && !entityIds2.isEmpty()) {
                int[] idsArray1 = Converter.convertCollectionToArray(entityIds1);
                int[] idsArray2 = Converter.convertCollectionToArray(entityIds2);
                BilateralBlock bBlock = new BilateralBlock(idsArray1, idsArray2);
                blocks.add(bBlock);
            }
        }
    }
}
