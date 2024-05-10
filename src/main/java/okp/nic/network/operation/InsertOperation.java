package okp.nic.network.operation;

import lombok.Getter;

@Getter
public class InsertOperation extends Operation {
    private final char data;

    public InsertOperation(int startPos, char data) {
        super(startPos);
        this.data = data;
    }

    @Override
    public String getType() {
        return "INSERT:";
    }

}
