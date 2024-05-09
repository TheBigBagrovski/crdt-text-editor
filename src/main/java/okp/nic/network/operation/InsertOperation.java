package okp.nic.network.operation;

import lombok.Getter;

@Getter
public class InsertOperation extends Operation {
    private final char data;

    public InsertOperation(char data, int position) {
        super(position);
        this.data = data;
    }

    @Override
    public String getType() {
        return "INSERT:";
    }

}
