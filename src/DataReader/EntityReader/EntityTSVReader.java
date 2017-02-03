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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads an "n-triples-like" file, which may not be valid, where s-p-o are separated by tabs.
 * Assumptions: the triples are sorted by subject (triples with the same subject are consequent).
 * @author vefthym
 */
public class EntityTSVReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityTSVReader.class.getName());    
    private final String SEPARATOR = " ";

    public EntityTSVReader(String filePath) {
        super(filePath);
    }

    @Override
    public List<EntityProfile> getEntityProfiles() {        
        if (!entityProfiles.isEmpty()) {
            return entityProfiles;
        }
        entityProfiles = new LinkedList<>(); //LinkedList does not require the time needed to resize an ArrayList -> O(1) in worst-case vs O(n) for ArrayLists
        
        if (inputFilePath == null) {
            LOGGER.log(Level.SEVERE, "Input file path has not been set!");
            return null;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) { //CSVReader has bugs!
            String line;

            int entityCounter = 0;
            //read entity profiles                       
            String[] nextLine; //a line of the form subject\tpredicate\tobject
            String previousEntityURL = ""; //check if this line is about the same entity as the previous line
            String currentEntityURL = " "; //check if this line is about the same entity as the previous line
            EntityProfile e = null; //keep the last entity profile in memory and add it when a new profile appears
            while ((line = br.readLine()) != null) {
                nextLine = line.split(SEPARATOR);
                currentEntityURL = nextLine[0];
                if (!currentEntityURL.equals(previousEntityURL)) {
                    if (e != null) {
                        entityProfiles.add(e);
                    }
                    e = new EntityProfile(currentEntityURL);
                    if (++entityCounter % 1000000 == 0) {
                        System.out.println(entityCounter/1000000+"M entities have been written so far...");
                    }
                }                
                previousEntityURL = currentEntityURL;
                
                if (nextLine.length < 3) { 
                    LOGGER.log(Level.WARNING, "Line in non-triple format : {0}", Arrays.toString(nextLine));
                    continue;
                }                
                    
                readEntity(e, nextLine);
            }
            
            if (currentEntityURL.equals(previousEntityURL) && e != null) { //for the case the last  line was about the same entity as the line before
                entityProfiles.add(e);
            }

            return entityProfiles;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getMethodInfo() {
        return "TSV Reader: converts a tsv file into a set of entity profiles.";
    }

    @Override
    public String getMethodParameters() {
        return "TO BE COMPLETED";
        
    }

    private void readEntity(EntityProfile e, String[] currentLine) throws IOException {
        String entityId = currentLine[0];
        if (e == null) {
            e = new EntityProfile(entityId);
        }
        if (!entityId.equals(e.getEntityUrl())) {
            LOGGER.log(Level.WARNING, "Entity profile and current line subjects should match! Skipping...");
            return;
        }
        if (currentLine.length > 3) {
            for (int i = 3; i < currentLine.length; i++) {
                currentLine[2] += " "+currentLine[i];
            }
        }
//        System.out.println("Adding the fact "+currentLine[1]+":"+currentLine[2]+" for entity "+entityId);
        e.addAttribute(currentLine[1], currentLine[2]);
    }
}
