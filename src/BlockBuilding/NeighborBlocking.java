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
        entities.stream().forEach((profile) -> {
            profilesURLs.put(profile.getEntityUrl(), profile.getAllValues());
        });
        
        try {
            int counter = 0;
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                
                /*
                //simple blocking on the values
                profile.getAllValues().stream()
                        .flatMap(value -> getBlockingKeys(value).stream())
                        .map((key)->key.trim())
                        .filter((key) -> (!key.isEmpty()))
                        .forEach((key) -> {
                            doc.add(new StringField(VALUE_LABEL, key, Field.Store.YES));
                        });
                */
                
                //now add this entity to one block for each token in the values of its neighbors
                profile.getAllValues().stream() //for each value of the current profile (possible neighbor)
                        .filter((neighbor) -> profilesURLs.containsKey(neighbor)) //keep only entity neighbors and skip other literals & URIs
                        .flatMap((neighbor) -> profilesURLs.get(neighbor).stream()) //for each value of a neighbor
                        .flatMap((value) -> getBlockingKeys(value).stream()) //get the blocking keys (tokens) of this value
                        .map((key)->key.trim().toLowerCase()) //trim each token
                        .filter((key) -> (!key.isEmpty())) //keep non-emtpy tokens
                        .forEach((key) -> { //add each trimmed token to a corresponding block
                            doc.add(new StringField(VALUE_LABEL, key, Field.Store.YES));
//                            System.out.println("Adding the value "+key+" from the neighbors of "+profile.getEntityUrl());
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
        return "Neighbor Blocking: it creates one block for every token "//in the attribute values of at least two entities, "
                + " in the attribute values of an entity's neighbors.";
    }

    @Override
    public String getMethodParameters() {
        return "Neighbor Blocking is a parameter-free method, as it uses unsupervised, schema-agnostic blocking keys:\n"
                + "every token in the values and in each neighbor's values is a blocking key.";
    }
}