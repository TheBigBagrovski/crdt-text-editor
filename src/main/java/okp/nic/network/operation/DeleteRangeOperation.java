package okp.nic.network.operation;

import lombok.Getter;

@Getter
public class DeleteRangeOperation extends Operation {
    private final int endPos;

    public DeleteRangeOperation(int startPos, int endPos) {
        super(startPos);
        this.endPos = endPos;
    }

    @Override
    public String getType() {
        return "DELETE_RANGE:";
    }
}
