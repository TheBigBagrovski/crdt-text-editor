package okp.nic.network.operation;

import lombok.Getter;

@Getter
public class InsertBlockOperation extends Operation {
    private final String data;

    public InsertBlockOperation(int position, String data) {
        super(position);
        this.data = data;
    }

    @Override
    public String getType() {
        return "INSERT_BLOCK:";
    }
}
