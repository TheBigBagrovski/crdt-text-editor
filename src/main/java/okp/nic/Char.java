package okp.nic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class Char implements Comparable<Char> {
    private char value;
    private List<Identifier> position;
    private String siteId;
    private int counter;

    public int compareTo(Char other) {
        List<Identifier> thisPosition = this.position;
        List<Identifier> otherPosition = other.position;

        int thisPosSize = thisPosition.size();
        int otherPosSize = otherPosition.size();

        int minPosSize = Math.min(thisPosSize, otherPosSize);
        for (int i = 0; i < minPosSize; i++) {
            Identifier thisIndex = thisPosition.get(i);
            Identifier otherIndex = otherPosition.get(i);
            if (thisIndex.compareTo(otherIndex) != 0) {
                return thisIndex.compareTo(otherIndex);
            }
        }

        return Integer.compare(thisPosSize, otherPosSize);
    }

}
