package okp.nic.crdt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Char implements Comparable<Char> {
    private String id;
    private boolean visible;
    private char value;
    private String idPrevious;
    private String idNext;

    public static Char startChar() {
        return new Char("start", false, '!', "", "end");
    }

    public static Char endChar() {
        return new Char("end", false, '!', "start", "");
    }

    @Override
    public int compareTo(Char o) {
        return this.id.compareTo(o.getId());
    }

}
