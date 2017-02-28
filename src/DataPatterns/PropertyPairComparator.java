package DataPatterns;

import java.util.Comparator;

public class PropertyPairComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof PropertyPair) || !(o2 instanceof PropertyPair)) {
            throw new IllegalArgumentException("cannot use PropertyPairComparator for non-PropertyPair instances.");
        }
        PropertyPair lp1 = (PropertyPair) o1;
        PropertyPair lp2 = (PropertyPair) o2;

        if (lp1.getProperty1().equals(lp2.getProperty1()) && lp1.getProperty2().equals(lp2.getProperty2())) {
            return 0;
        }

        double score1 = lp1.getScore();
        double score2 = lp2.getScore();

        if (score1 > score2) {
            return 1;
        } else if (score1 < score2) {
            return -1;
        } else {
            return 0;
        }            
    }

}
