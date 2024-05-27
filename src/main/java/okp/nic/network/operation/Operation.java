package okp.nic.network.operation;

import lombok.Getter;

@Getter
public abstract class Operation {
    protected final int startPos;

    public Operation(int startPos) {
        this.startPos = startPos;
    }

    public abstract String getType();

}
