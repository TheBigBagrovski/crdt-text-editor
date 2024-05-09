package okp.nic.network.operation;

import lombok.Getter;

@Getter
public abstract class Operation {
    protected final int position;

    public Operation(int position) {
        this.position = position;
    }

}