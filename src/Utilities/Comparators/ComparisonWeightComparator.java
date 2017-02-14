package Utilities.Comparators;

import DataModel.Comparison;
import java.util.Comparator;

/**
 * Compares two Comparison objects using their utilityMeasure field, unless the
 * two comparisons have the same entityId1 and entityId2. If they have the same
 * entity ids, it returns 0 (equality). This helps equality checks in 
 * containment methods. 
 * @author vefthym
 */
public class ComparisonWeightComparator implements Comparator<Comparison> {

    @Override
    public int compare(Comparison o1, Comparison o2) {
        //first check if the two comparisons are between the same entities
        if ((o1.getEntityId1() == o2.getEntityId1()) && (o1.getEntityId2() == o2.getEntityId2())) {
            return 0;
        }
        double test = o2.getUtilityMeasure()-o1.getUtilityMeasure(); 
        if (0 < test) {
            return -1;
        }

        if (test < 0) {
            return 1;
        }

        return 0;
    }
    
}