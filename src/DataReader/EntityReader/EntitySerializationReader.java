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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class EntitySerializationReader extends AbstractEntityReader {
    
    private static final Logger LOGGER = Logger.getLogger(EntitySerializationReader.class.getName());
    
    public EntitySerializationReader(String filePath) {
        super(filePath);
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
        
        entityProfiles.addAll((List<EntityProfile>) loadSerializedObject(inputFilePath));
        return entityProfiles;
    }

    @Override
    public String getMethodInfo() {
        return "Serialization Reader: loads a file with Java serialized EntityProfile objects into memory.";
    }

    @Override
    public String getMethodParameters() {
        return "No other parameter is required, apart from the absolute file path";
    }
    
}
