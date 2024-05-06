package okp.nic.crdt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class Char implements Comparable<Char> {
    private char value;
    private double position;
    private String siteId;
    private int counter;

    @Override
    public int compareTo(Char other) {
        if (this.position < other.position) {
            return -1;
        } else if (this.position > other.position) {
            return 1;
        } else {
            return Integer.compare(this.counter, other.counter);
        }
    }
}

//@AllArgsConstructor
//@Getter
//@Setter
//public class Char implements Comparable<Char> {
//    private char value;
//    private Identifier position; // Identifier remains the same
//    private String siteId;
//    private int counter;
//
//    public int compareTo(Char other) {
//        // Comparison logic remains the same
//        return this.position.compareTo(other.position);
//    }
//}

//@Getter
//@Setter
//public class Char implements Comparable<Char> {
//    private char value;
//    private Double position;
//    private String siteId;
//    private int counter;
//
//    public int compareTo(Char other) {
//        return Double.compare(this.position, other.position);
//    }
//}

//@AllArgsConstructor
//@Getter
//@Setter
//public class Char implements Comparable<Char> {
//    private char value;
//    private long position; // Use a single long for position
//    private String siteId;
//    private int counter;
//
//    public int compareTo(Char other) {
//        int posComparison = Long.compare(this.position, other.position);
//        if (posComparison != 0) {
//            return posComparison;
//        }
//        return this.siteId.compareTo(other.siteId);
//    }
//
//     Helper method to extract the level from the position
//    public int getLevel() {
//        return (int) (position & 0b11111); // Extract the 5 least significant bits
//    }
//
//     Helper method to extract the digit from the position
//    public int getDigit() {
//        return (int) ((position >>> 5) & 0x1FFFFFF); // Extract the next 27 bits
//    }
//}

//@AllArgsConstructor
//@Getter
//@Setter
//public class Char implements Comparable<Char> {
//    private char value;
//    private List<Identifier> position;
//    private String siteId;
//    private int counter;
//
//    public int compareTo(Char other) {
//        List<Identifier> thisPosition = this.position;
//        List<Identifier> otherPosition = other.position;
//
//        int thisPosSize = thisPosition.size();
//        int otherPosSize = otherPosition.size();
//
//        int minPosSize = Math.min(thisPosSize, otherPosSize);
//        for (int i = 0; i < minPosSize; i++) {
//            Identifier thisIndex = thisPosition.get(i);
//            Identifier otherIndex = otherPosition.get(i);
//            if (thisIndex.compareTo(otherIndex) != 0) {
//                return thisIndex.compareTo(otherIndex);
//            }
//        }
//
//        return Integer.compare(thisPosSize, otherPosSize);
//    }
//
//}
