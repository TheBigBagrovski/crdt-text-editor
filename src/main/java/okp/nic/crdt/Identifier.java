package okp.nic.crdt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Identifier implements Comparable<Identifier> {
    private int digit;
    private String siteId;

    public int compareTo(Identifier other) {
        if (this.digit < other.digit) {
            return -1;
        } else if (this.digit > other.digit) {
            return 1;
        } else {
            return this.siteId.compareTo(other.siteId);
        }
    }
}
