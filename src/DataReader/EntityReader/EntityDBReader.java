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

import java.sql.Connection;
import java.sql.DriverManager;

import DataModel.EntityProfile;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public class EntityDBReader extends AbstractEntityReader {

    private static final Logger LOGGER = Logger.getLogger(EntityDBReader.class.getName());

    private boolean ssl;
    
    private String password;
    private String table;
    private String user;
    
    private final Set<String> attributesToExclude;

    public EntityDBReader(String dbURL) {
        super(dbURL);
        password = null;
        ssl = true;
        table = null;
        user = null;
        attributesToExclude = new HashSet<>();
    }

    @Override
    public List<EntityProfile> getEntityProfiles() {
        if (!entityProfiles.isEmpty()) {
            return entityProfiles;
        }

        if (inputFilePath == null) {
            LOGGER.log(Level.SEVERE, "Database url has not been set!");
            return null;
        }

        //inputFilePath is assigned the Database URL
        try {
            if (user == null) {
                LOGGER.log(Level.SEVERE, "Database user has not been set!");
                return null;
            }
            if (password == null) {
                LOGGER.log(Level.SEVERE, "Database password has not been set!");
                return null;
            }
            if (table == null) {
                LOGGER.log(Level.SEVERE, "Database table has not been set!");
                return null;
            }
            
            Connection conn = null;
            if (inputFilePath.startsWith("mysql")) {
                conn = getMySQLconnection(inputFilePath);
            } else if (inputFilePath.startsWith("postgresql")) {
                conn = getPostgreSQLconnection(inputFilePath);
            } else {
                LOGGER.log(Level.SEVERE, "Only MySQL and PostgreSQL are supported for the time being!");
                return null; 
            }

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);//retrieve the appropriate table
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNum = rsmd.getColumnCount();
            String[] columns = new String[columnsNum];
            for (int i = 0; i < columnsNum; i++) {
                columns[i] = rsmd.getColumnName(i + 1);//get attribute names
            }
            
            //Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                String id = rs.getString(columns[0]);
                EntityProfile newProfile = new EntityProfile(id);//create a new profile for each record
                entityProfiles.add(newProfile);
                for (int i = 1; i < columnsNum; i++) {
                    String attributeName = columns[i];
                    if (attributesToExclude.contains(attributeName)) {
                        continue;
                    }
                    
                    String value = rs.getString(columns[i]);
                    newProfile.addAttribute(attributeName, value);
                }
            }
            rs.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }

        return entityProfiles;
    }

    @Override
    public String getMethodInfo() {
        return "DB Reader: converts a relational (MySQL or PostgreSQL) database into a set of entity profiles.";
    }

    @Override
    public String getMethodParameters() {
        return "The DB Reader involves 4 parameters:\n"
                + "1) dbURL : String, Default value: null.\n"
                + "It contains the URL and the port of the relational database, from which the entity profiles will be retrieved.\n"
                + "2) table : String, default value: null.\n"
                + "It determines the name of the database table that contains the entity profiles.\n"
                + "3) user : String, default value: null.\n"
                + "It determines the username that corresponds to an database account with access rights to the specified database and table.\n"
                + "4) password: String, default value: null.\n"
                + "It determines the password that corresponds to the username with access rights to the specified database and table.\n"
                + "5) attributesToExclude: String[], default value: empty.\n"
                + "The names of the columns that will be ignored during the creation of entity profiles.\n"
                + "6) ssl : boolean, default value: true.\n"
                + "It determines whether the database connection will use SSL to encrypt client/server communications for increased security.\n";
    }

    private Connection getMySQLconnection(String dbURL) throws IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:" + dbURL + "?user=" + user + "&password=" + password);
            return conn;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Connection getPostgreSQLconnection(String dbURL) throws IOException {
        try {
            Properties props = new Properties();
            if (!(user == null)) {
                props.setProperty("user", user);
            }
            if (!(password == null)) {
                props.setProperty("password", password);
            }
            if (ssl) {
                props.setProperty("ssl", "true");
            }
            return DriverManager.getConnection("jdbc:" + dbURL, props);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void setAttributesToExclude(String[] attributesNamesToExclude) {
        attributesToExclude.addAll(Arrays.asList(attributesNamesToExclude));
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public void setTable(String table) {
        this.table = table;
    }
    
    public void setSSL(boolean ssl) {
        this.ssl = ssl;
    }
    
    public void setUser(String user) {
        this.user = user;
    }    
}
