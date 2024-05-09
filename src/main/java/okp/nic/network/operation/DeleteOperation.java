package okp.nic.network.operation;

import lombok.Getter;

@Getter
public class DeleteOperation extends Operation {

    public DeleteOperation(int position) {
        super(position);
    }

    @Override
    public String getType() {
        return "DELETE:";
    }

}