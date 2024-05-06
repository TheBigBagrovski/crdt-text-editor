package okp.nic.network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import okp.nic.crdt.Char;

@AllArgsConstructor
@Getter
@Setter
public class Operation {
    private char data;
    private String type;
    private int position;
}
