package okp.nic.network.operation;

import lombok.Getter;

@Getter
public class DeleteOperation extends Operation {

    public DeleteOperation(int startPos) {
        super(startPos);
    }

    @Override
    public String getType() {
        return "DELETE:";
    }

}