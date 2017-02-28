package DataPatterns;

import java.util.Objects;

/**
 *
 * @author vefthym
 */
public class PropertyPair {
    String property1;
    String property2;
    double score; //typically (but not restricted to) fMeasure of support and discriminability
    public PropertyPair(String label1, String label2, double score) {
        this.property1 = label1;
        this.property2 = label2;
        this.score = score;
    }

    public String getProperty1() {
        return property1;
    }
    
    public String getProperty2() {
        return property2;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return property1+", "+property2+": "+score;
    }
    
    @Override
    public boolean equals(Object other) {        
        if (other != null && other instanceof PropertyPair) {
            PropertyPair otherPair = (PropertyPair) other;
            return property1.equals(otherPair.getProperty1()) && property2.equals(otherPair.getProperty2());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + property1.hashCode();
        hash = 83 * hash + property2.hashCode();
//        hash = 83 * hash + (int) (Double.doubleToLongBits(this.score) ^ (Double.doubleToLongBits(this.score) >>> 32));
        return hash;
    }
}
