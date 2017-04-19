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
public class BBCMusicTokenBlocking extends StandardBlocking {

    private static final Logger LOGGER = Logger.getLogger(BBCMusicTokenBlocking.class.getName());
    
    protected boolean bbc;

    protected Set<String> bbcPredicates;
    protected Set<String> dbpediaredicates;

    public BBCMusicTokenBlocking() {
        bbcPredicates = new HashSet<>();
        bbcPredicates.add("<http://purl.org/dc/elements/1.1/title>");
        bbcPredicates.add("<http://open.vocab.org/terms/sortLabel>");
        bbcPredicates.add("<http://xmlns.com/foaf/0.1/name>");

        dbpediaredicates = new HashSet<>();
        dbpediaredicates.add("<http://www.w3.org/2000/01/rdf-schema#label>");
        dbpediaredicates.add("<http://dbpedia.org/property/name>");
        dbpediaredicates.add("<http://xmlns.com/foaf/0.1/name>");
    }

    @Override
    protected void buildBlocks() {
        setMemoryDirectory();

        bbc = true;
        IndexWriter iWriter1 = openWriter(indexDirectoryD1);
        indexEntities(iWriter1, entityProfilesD1);
        closeWriter(iWriter1);

        bbc = false;
        IndexWriter iWriter2 = openWriter(indexDirectoryD2);
        indexEntities(iWriter2, entityProfilesD2);
        closeWriter(iWriter2);
    }

    @Override
    protected void indexEntities(IndexWriter index, List<EntityProfile> entities) {
        try {
            int counter = 0;
            final Suffix suffix = new Suffix();
            for (EntityProfile profile : entities) {
                Document doc = new Document();
                doc.add(new StoredField(DOC_ID, counter++));
                for (Attribute attribute : profile.getAttributes()) {
                    if (bbc) {
                        if (bbcPredicates.contains(attribute.getName())) {
                            suffix.setValue("BBC_LP");
                        } else {
                            suffix.setValue("");
                        }
                    } else {
                        if (dbpediaredicates.contains(attribute.getName())) {
                            suffix.setValue("DBP_LP");
                        } else {
                            suffix.setValue("");
                        }
                    }
                    getBlockingKeys(attribute.getValue()).stream().filter((key) -> (0 < key.trim().length())).forEach((key) -> {
                        doc.add(new StringField(VALUE_LABEL, key.trim()+suffix.getValue(), Field.Store.YES));
                    });
                }
                index.addDocument(doc);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    class Suffix {
        private String tokenSuffix = "";
        
        String getValue() { return tokenSuffix; }
        void setValue(String newValue) { tokenSuffix = newValue; }
    }
}
