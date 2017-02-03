package DataReader.EntityReader;

import DataReader.*;
import DataModel.Attribute;
import DataModel.EntityProfile;
import DataReader.EntityReader.EntityRDFReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author G.A.P. II
 */

public class TestRdfReader {
    public static void main(String[] args) {
        String filePath = "G:\\VASILIS\\bbcMusic\\dbpedia37New.nt";
        String formalFilePath = "G:\\VASILIS\\bbcMusic\\dbpedia37NewFormal.nt";
        if (args.length > 0) { //override default path
            filePath = args[0];
        } 
        if (args.length == 2) {
            formalFilePath = args[1];
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
                BufferedWriter bw = new BufferedWriter(new FileWriter(formalFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().endsWith(".")) { //if one ends with '.' all end with '.'
                    break;
                } else {
                    bw.write(line.trim()+" .");
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TestRdfReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        EntityRDFReader n3reader = new EntityRDFReader(formalFilePath);
        List<EntityProfile> profiles = n3reader.getEntityProfiles();
        System.out.println("Found "+ profiles.size()+ " profiles");
        n3reader.storeSerializedObject(profiles, filePath.replaceAll("\\..*", "Profiles"));
        System.out.println(profiles.size()+" profiles written in "+filePath.replaceAll("\\..*", "Profiles"));
    }
}