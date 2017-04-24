package NewApproaches;

import static BlockBuilding.IBlockBuilding.DOC_ID;
import static BlockBuilding.IBlockBuilding.VALUE_LABEL;
import BlockBuilding.StandardBlocking;
import DataModel.Attribute;
import DataModel.EntityProfile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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
 * @author G.A.P. II
 */
public class YagoIMDbTokenBlocking extends StandardBlocking {

    private static final Logger LOGGER = Logger.getLogger(YagoIMDbTokenBlocking.class.getName());

    protected boolean yago;
    
    protected Set<String> labelPredicates;    

    public YagoIMDbTokenBlocking() {
        labelPredicates = new HashSet<>();                
        labelPredicates.add("rdfs:label");
        labelPredicates.add("label");
        labelPredicates.add("skos:prefLabel");
    }

    @Override
    protected void buildBlocks() {
        setMemoryDirectory();

        yago = true;
        
        IndexWriter iWriter1 = openWriter(indexDirectoryD1);
        indexEntities(iWriter1, entityProfilesD1);
        closeWriter(iWriter1);

        yago = false; 
        
        IndexWriter iWriter2 = openWriter(indexDirectoryD2);
        indexEntities(iWriter2, entityProfilesD2);
        closeWriter(iWriter2);
    }

    @Override
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
                    if (labelPredicates.contains(attribute.getName())) {
                        getBlockingKeys(attribute.getValue()).stream().filter((key) -> (0 < key.trim().length())).forEach((key) -> {
                            doc.add(new StringField(VALUE_LABEL, key.trim() + "_LP", Field.Store.YES));
                        });
                    }
                    
                }  
                /*
                //add infix blocks for yago
                if (yago) {
                    String yagoInfix = profile.getEntityUrl();
                    yagoInfix = yagoInfix.substring(1, yagoInfix.length()-1);
                    getBlockingKeys(yagoInfix).stream().filter((key) -> (0 < key.trim().length())).forEach((key) -> {
                        doc.add(new StringField(VALUE_LABEL, key.trim() + "_LP", Field.Store.YES));
                    });
                }*/
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
