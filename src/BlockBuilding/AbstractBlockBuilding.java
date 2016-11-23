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
import DataModel.Attribute;
import DataModel.BilateralBlock;
import DataModel.EntityProfile;
import DataModel.UnilateralBlock;
import Utilities.Converter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author gap2
 */
public abstract class AbstractBlockBuilding implements IBlockBuilding {

    private static final Logger LOGGER = Logger.getLogger(AbstractBlockBuilding.class.getName());

    protected double noOfEntitiesD1;
    protected double noOfEntitiesD2;

    protected final List<AbstractBlock> blocks;
    protected Directory indexDirectoryD1;
    protected Directory indexDirectoryD2;
    protected List<EntityProfile> entityProfilesD1;
    protected List<EntityProfile> entityProfilesD2;

    public AbstractBlockBuilding() {
        blocks = new ArrayList<>();
        entityProfilesD1 = null;
        entityProfilesD2 = null;
    }

    protected void buildBlocks() {
        setMemoryDirectory();

        IndexWriter iWriter1 = openWriter(indexDirectoryD1);
        indexEntities(iWriter1, entityProfilesD1);
        closeWriter(iWriter1);

        if (indexDirectoryD2 != null) {
            IndexWriter iWriter2 = openWriter(indexDirectoryD2);
            indexEntities(iWriter2, entityProfilesD2);
            closeWriter(iWriter2);
        }
    }

    protected void closeReader(IndexReader iReader) {
        try {
            iReader.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    protected void closeWriter(IndexWriter iWriter) {
        try {
            iWriter.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    protected abstract Set<String> getBlockingKeys(String attributeValue);

    @Override
    public List<AbstractBlock> getBlocks(List<EntityProfile> profiles) {
        return this.getBlocks(profiles, null);
    }
    
    @Override
    public List<AbstractBlock> getBlocks(List<EntityProfile> profilesD1,
            List<EntityProfile> profilesD2) {
        if (profilesD1 == null) {
            LOGGER.log(Level.SEVERE, "First list of entity profiles is null! "
                    + "The first argument should always contain entities.");
            return null;
        }

        entityProfilesD1 = profilesD1;
        noOfEntitiesD1 = entityProfilesD1.size();
        if (profilesD2 != null) {
            entityProfilesD2 = profilesD2;
            noOfEntitiesD2 = entityProfilesD2.size();
        }

        buildBlocks();
        return readBlocks();
    }

    public double getBruteForceComparisons() {
        if (entityProfilesD2 == null) {
            return noOfEntitiesD1 * (noOfEntitiesD1 - 1) / 2;
        }
        return noOfEntitiesD1 * noOfEntitiesD2;
    }

    protected int[] getDocumentIds(IndexReader reader) {
        int[] documentIds = new int[reader.numDocs()];
        for (int i = 0; i < documentIds.length; i++) {
            try {
                Document document = reader.document(i);
                documentIds[i] = Integer.parseInt(document.get(DOC_ID));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return documentIds;
    }
    
    public double getTotalNoOfEntities() {
        if (entityProfilesD2 == null) {
            return noOfEntitiesD1;
        }
        return noOfEntitiesD1 + noOfEntitiesD2;
    }

    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                for (Attribute attribute : profile.getAttributes()) {
                    getBlockingKeys(attribute.getValue()).stream().filter((key) -> (0 < key.trim().length())).forEach((key) -> {
                        doc.add(new StringField(VALUE_LABEL, key.trim(), Field.Store.YES));
                    });
                }
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static IndexReader openReader(Directory directory) {
        try {
            return DirectoryReader.open(directory);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    protected IndexWriter openWriter(Directory directory) {
        try {
            Analyzer analyzer = new SimpleAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            return new IndexWriter(directory, config);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    protected Map<String, int[]> parseD1Index(IndexReader d1Index, IndexReader d2Index) {
        try {
            int[] documentIds = getDocumentIds(d1Index);
            final Map<String, int[]> hashedBlocks = new HashMap<>();
            Fields fields = MultiFields.getFields(d1Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator();
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    // check whether it is a common term
                    int d2DocFrequency = d2Index.docFreq(new Term(field, text));
                    if (d2DocFrequency == 0) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    PostingsEnum pe = MultiFields.getTermDocsEnum(d1Index, field, text);
                    int doc;
                    while ((doc = pe.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Converter.convertCollectionToArray(entityIds);
                    hashedBlocks.put(text.utf8ToString(), idsArray);
                }
            }
            return hashedBlocks;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected void parseD2Index(IndexReader d2Index, Map<String, int[]> hashedBlocks) {
        try {
            int[] documentIds = getDocumentIds(d2Index);
            Fields fields = MultiFields.getFields(d2Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator();
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    if (!hashedBlocks.containsKey(text.utf8ToString())) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    PostingsEnum pe = MultiFields.getTermDocsEnum(d2Index, field, text);
                    int doc;
                    while ((doc = pe.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Converter.convertCollectionToArray(entityIds);
                    int[] d1Entities = hashedBlocks.get(text.utf8ToString());
                    blocks.add(new BilateralBlock(d1Entities, idsArray));
                }
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    protected void parseIndex(IndexReader d1Index) {
        try {
            int[] documentIds = getDocumentIds(d1Index);
            Fields fields = MultiFields.getFields(d1Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator();
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    if (termsEnum.docFreq() < 2) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    PostingsEnum pe = MultiFields.getTermDocsEnum(d1Index, field, text);
                    int doc;
                    while ((doc = pe.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Converter.convertCollectionToArray(entityIds);
                    UnilateralBlock block = new UnilateralBlock(idsArray);
                    blocks.add(block);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    //read blocks from Lucene index
    public List<AbstractBlock> readBlocks() {
        IndexReader iReaderD1 = openReader(indexDirectoryD1);
        if (entityProfilesD2 == null) { //Dirty ER
            parseIndex(iReaderD1);
        } else {
            IndexReader iReaderD2 = openReader(indexDirectoryD2);
            Map<String, int[]> hashedBlocks = parseD1Index(iReaderD1, iReaderD2);
            parseD2Index(iReaderD2, hashedBlocks);
            closeReader(iReaderD2);
        }
        closeReader(iReaderD1);
        
        return blocks;
    }

    protected void setMemoryDirectory() {
        indexDirectoryD1 = new RAMDirectory();
        if (entityProfilesD2 != null) {
            indexDirectoryD2 = new RAMDirectory();
        }
    }
}