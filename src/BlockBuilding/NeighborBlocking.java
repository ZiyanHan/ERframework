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

import static BlockBuilding.IBlockBuilding.DOC_ID;
import static BlockBuilding.IBlockBuilding.VALUE_LABEL;
import DataModel.EntityProfile;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

/**
 *
 * @author vefthym
 */
public class NeighborBlocking extends AbstractBlockBuilding {

    private static final Logger LOGGER = Logger.getLogger(NeighborBlocking.class.getName());
    
    public NeighborBlocking() {
        super();
        LOGGER.log(Level.INFO, "Neighbor Blocking initiated");
    }
    
    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        Map<String,Set<String>> profilesURLs = new HashMap<>(entities.size()); //key: entityURL, value: entity values
        for (EntityProfile profile : entities) {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        }
        
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                
                //simple blocking on the values
                profile.getAllValues().stream()
                        .flatMap(value -> getBlockingKeys(value).stream())
                        .map((key)->key.trim())
                        .filter((key) -> (!key.isEmpty()))
                        .forEach((key) -> {
                            doc.add(new StringField(VALUE_LABEL, key, Field.Store.YES));
                        });
                
                //now add this to one block for each token in the values of its neighbors
                profile.getAllValues().stream() //for each value (possible neighbor)
                        .filter((neighbor) -> profilesURLs.containsKey(neighbor)) //keep only entity neighbors and skip other literals & URIs
                        .flatMap((neighbor) -> profilesURLs.get(neighbor).stream()) //for each value of a neighbor
                        .flatMap((value) -> getBlockingKeys(value).stream()) //get the blocking keys (tokens) of this value
                        .map((key)->key.trim()) //trim each token
                        .filter((key) -> (!key.isEmpty()))
                        .forEach((key) -> { //add each trimmed token to a corresponding block
                            doc.add(new StringField(VALUE_LABEL, key, Field.Store.YES));
                        });
                
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        return new HashSet<>(Arrays.asList(getTokens(attributeValue)));
    }
    
    protected String[] getTokens (String attributeValue) {
        return attributeValue.split("[\\W_]");
    }

    @Override
    public String getMethodInfo() {
        return "Neighbor Blocking: it creates one block for every token in the attribute values of at least two entities, "
                + "and in the attribute values of their neighbors.";
    }

    @Override
    public String getMethodParameters() {
        return "Neighbor Blocking is a parameter-free method, as it uses unsupervised, schema-agnostic blocking keys:\n"
                + "every token in the values and in each neighbor's values is a blocking key.";
    }
}