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

import DataModel.AbstractBlock;
import DataModel.BilateralBlock;
import DataModel.UnilateralBlock;
import Utilities.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author gap2
 */
public class SortedNeighborhoodBlocking extends StandardBlocking {

    private static final Logger LOGGER = Logger.getLogger(SortedNeighborhoodBlocking.class.getName());
    
    protected final int windowSize;

    public SortedNeighborhoodBlocking() {
        this(4);
        LOGGER.log(Level.INFO, "Using default configuration for Sorted Neighborhood Blocking.");
    }
   
    public SortedNeighborhoodBlocking(int w) {
        super();
        windowSize = w;
        LOGGER.log(Level.INFO, "Window size\t:\t{0}", windowSize);
    }

    @Override
    public String getMethodInfo() {
        return "Sorted Neighborhood: it creates blocks based on the similarity of the blocking keys of Standard Blocking:\n"
                + "it sorts the keys in alphabetical order, it sorts the entities accordingly and then, it slides a window over the sorted list of entities;\n"
                + "the entities that co-occur inside the window in every iteration form a block and are compared with each other.";
    }

    @Override
    public String getMethodParameters() {
        return "Sorted Neighborhood involves a single parameter, due to its unsupervised, schema-agnostic blocking keys:\n"
                + "w, the fixed size of the sliding window.\n"
                + "Default value: 4.";
    }
    
    protected Integer[] getSortedEntities(String[] sortedTerms, IndexReader iReader) {
        final List<Integer> sortedEntityIds = new ArrayList<>();

        int[] documentIds = getDocumentIds(iReader);
        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = getTermEntities(documentIds, iReader, blockingKey);
            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
        }

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }

    protected Integer[] getSortedEntities(String[] sortedTerms, IndexReader d1Reader, IndexReader d2Reader) {
        int datasetLimit = d1Reader.numDocs();
        final List<Integer> sortedEntityIds = new ArrayList<>();

        int[] documentIdsD1 = getDocumentIds(d1Reader);
        int[] documentIdsD2 = getDocumentIds(d2Reader);
        for (String blockingKey : sortedTerms) {
            List<Integer> sortedIds = new ArrayList<>();
            sortedIds.addAll(getTermEntities(documentIdsD1, d1Reader, blockingKey));

            getTermEntities(documentIdsD2, d2Reader, blockingKey).stream().forEach((entityId) -> {
                sortedIds.add(datasetLimit + entityId);
            });

            Collections.shuffle(sortedIds);
            sortedEntityIds.addAll(sortedIds);
        }

        return sortedEntityIds.toArray(new Integer[sortedEntityIds.size()]);
    }

    protected List<Integer> getTermEntities(int[] docIds, IndexReader iReader, String blockingKey) {
        try {
            Term term = new Term(VALUE_LABEL, blockingKey);
            List<Integer> entityIds = new ArrayList<>();
            int docFrequency = iReader.docFreq(term);
            if (0 < docFrequency) {
                BytesRef text = term.bytes();
                PostingsEnum pe = MultiFields.getTermDocsEnum(iReader, VALUE_LABEL, text);
                int doc;
                while ((doc = pe.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                    entityIds.add(docIds[doc]);
                }
            }

            return entityIds;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected Set<String> getTerms(IndexReader iReader) {
        Set<String> sortedTerms = new HashSet<>();
        try {
            Fields fields = MultiFields.getFields(iReader);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator();
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    sortedTerms.add(text.utf8ToString());
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return sortedTerms;
    }

    @Override
    protected void parseIndex(IndexReader iReader) {
        final Set<String> blockingKeysSet = getTerms(iReader);
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getSortedEntities(sortedTerms, iReader);

        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                entityIds.add(allEntityIds[i + j]);
            }

            if (1 < entityIds.size()) {
                int[] idsArray = Converter.convertCollectionToArray(entityIds);
                UnilateralBlock uBlock = new UnilateralBlock(idsArray);
                blocks.add(uBlock);
            }
        }
    }

    protected void parseIndices(IndexReader iReader1, IndexReader iReader2) {
        final Set<String> blockingKeysSet = getTerms(iReader1);
        blockingKeysSet.addAll(getTerms(iReader2));
        String[] sortedTerms = blockingKeysSet.toArray(new String[blockingKeysSet.size()]);
        Arrays.sort(sortedTerms);

        Integer[] allEntityIds = getSortedEntities(sortedTerms, iReader1, iReader2);

        int datasetLimit = iReader1.numDocs();
        //slide window over the sorted list of entity ids
        int upperLimit = allEntityIds.length - windowSize;
        for (int i = 0; i <= upperLimit; i++) {
            final Set<Integer> entityIds1 = new HashSet<>();
            final Set<Integer> entityIds2 = new HashSet<>();
            for (int j = 0; j < windowSize; j++) {
                if (allEntityIds[i + j] < datasetLimit) {
                    entityIds1.add(allEntityIds[i + j]);
                } else {
                    entityIds2.add(allEntityIds[i + j] - datasetLimit);
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

    @Override
    public List<AbstractBlock> readBlocks() {
        IndexReader iReaderD1 = openReader(indexDirectoryD1);
        if (entityProfilesD2 == null) { //Dirty ER
            parseIndex(iReaderD1);
        } else {
            IndexReader iReaderD2 = openReader(indexDirectoryD2);
            parseIndices(iReaderD1, iReaderD2);
            closeReader(iReaderD2);
        }
        closeReader(iReaderD1);
        
        return blocks;
    }
}
