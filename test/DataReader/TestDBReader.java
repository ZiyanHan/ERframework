package DataReader;

import DataModel.Attribute;
import DataModel.EntityProfile;
import DataReader.EntityReader.EntityDBReader;

import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class TestDBReader {
    public static void main(String[] args) {
    	String url = "postgresql://localhost/bench_parallel";
    	EntityDBReader DBReader = new EntityDBReader(url);
    	DBReader.setTable("pgbench_tellers");
    	DBReader.setUser("postgres");
    	DBReader.setPassword("");
    	DBReader.setSSL(true);
		List<EntityProfile> profiles = DBReader.getEntityProfiles();
		for (EntityProfile profile : profiles) {
            System.out.println("\n\n" + profile.getEntityUrl());
            for (Attribute attribute : profile.getAttributes()) {
                System.out.print(attribute.toString());
                System.out.println();
            }
        }
//		String url = "mysql://localhost/domesDB";
//    	EntityDBReader DBReader = new EntityDBReader(url);
//    	DBReader.setTable("new_table");
//    	DBReader.setUser("root");
//    	DBReader.setPassword("");
//		List<EntityProfile> profiles = DBReader.getEntityProfiles();
//		for (EntityProfile profile : profiles) {
//            System.out.println("\n\n" + profile.getEntityUrl());
//            for (Attribute attribute : profile.getAttributes()) {
//                System.out.print(attribute.toString());
//                System.out.println();
//            }
//        }
    }
}