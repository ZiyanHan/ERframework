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
package DataReader.EntityReader;

import DataModel.EntityProfile;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class EntityCSVReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityCSVReader.class.getName());
    
    private boolean attributeNamesInFirstRow;
    private char separator;
    private int idIndex;
    private String[] attributeNames; // FIX set this!!!
    private final Set<Integer> attributesToExclude;

    public EntityCSVReader(String filePath) {
        super(filePath);
        attributeNamesInFirstRow = false;
        attributeNames = null;
        idIndex = -1;
        separator = ',';
        attributesToExclude = new HashSet<>();
    }

    @Override
    public List<EntityProfile> getEntityProfiles() {
        if (!entityProfiles.isEmpty()) {
            return entityProfiles;
        }
        
        if (inputFilePath == null) {
            LOGGER.log(Level.SEVERE, "Input file path has not been set!");
            return null;
        }
        
        try {
            //creating reader
            CSVReader reader = new CSVReader(new FileReader(inputFilePath), separator);

            //getting first line
            String[] firstLine = reader.readNext();
            int noOfAttributes = firstLine.length;
            if (noOfAttributes - 1 < idIndex) {
                LOGGER.log(Level.SEVERE, "Id index does not correspond to a valid column index! Counting starts from 0.");
                return null;
            }

            //setting attribute names
            int entityCounter = 0;
            if (attributeNamesInFirstRow) {
                attributeNames = Arrays.copyOf(firstLine, noOfAttributes);
            } else { // no attribute names in csv file
                attributeNames = new String[noOfAttributes];
                for (int i = 0; i < noOfAttributes; i++) {
                    attributeNames[i] = "attribute" + (i + 1);
                }
                
                entityCounter++; //first line corresponds to entity
                readEntity(entityCounter, firstLine);
            }

            //read entity profiles
            String[] nextLine = null;
            while ((nextLine = reader.readNext()) != null) {
                entityCounter++;
                
                if (nextLine.length < attributeNames.length - 1) { 
                    LOGGER.log(Level.WARNING, "Line with missing attribute names : {0}", Arrays.toString(nextLine));
                    continue;
                }                
                    
                readEntity(entityCounter, nextLine);
            }

            return entityProfiles;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getMethodInfo() {
        return "CSV Reader: converts a csv file into a set of entity profiles.";
    }

    @Override
    public String getMethodParameters() {
        return "The CSV Reader involves 4 parameters:\n"
                + "1) attributeNamesInFirstRow : boolean, default value: false.\n"
                + "If true, it reades the attribute name from the first line of the CSV file.\n"
                + "If false, the first line is converted into an entity profile.\n"
                + "2) separator : character, default value: ','.\n"
                + "It determines the character used to tokenize every line into attribute values.\n"
                + "3) Id index: integer parameter, default value: -1. Counting starts from 0.\n"
                + "If id>0, the values of corresponding column are used for assigning the id of every entity.\n"
                + "If the given id is larger than the number of columns, an exception is thrown and getEntityProfiles() returns null.\n"
                + "If id<0, an auto-incremented integer is assigned as id to every entity.\n"
                + "4) attributesToExclude: int[], default value: empty. Counting starts from 0.\n"
                + "The column ids assigned to this parameter will be ignored during the creation of entity profiles.\n";
    }

    private void readEntity(int index, String[] currentLine) throws IOException {
        String entityId;
        if (idIndex < 0) {
            entityId = "id" + index;
        } else {
            entityId = currentLine[idIndex];
        }
        
        EntityProfile newProfile = new EntityProfile(entityId);
        for (int i = 0; i < currentLine.length; i++) {
            if (attributesToExclude.contains(i)) {
                continue;
            }
            if (!currentLine[i].isEmpty()) {
                newProfile.addAttribute(attributeNames[i], currentLine[i]);

            }
        }
        entityProfiles.add(newProfile);
    }

    public void setAttributesToExclude(int[] attributesIndicesToExclude) {
        for (int attributeIndex : attributesIndicesToExclude) {
            attributesToExclude.add(attributeIndex);
        }
    }

    public void setAttributeNamesInFirstRow(boolean attributeNamesInFirstRow) {
        this.attributeNamesInFirstRow = attributeNamesInFirstRow;
    }

    public void setIdIndex(int idIndex) {
        this.idIndex = idIndex;
        attributesToExclude.add(idIndex);
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }
}
