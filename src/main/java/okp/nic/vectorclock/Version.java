package okp.nic.vectorclock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class Version {
    private String siteId;
    private int counter;
    private final List<Integer> exceptions = new ArrayList<>();

    public Version(String siteId) {
        this.siteId = siteId;
        this.counter = 0;
    }

    public void incrementCounter() {
        counter++;
    }

    public void update(Version version) {
        int incomingCounter = version.getCounter();
//        if (incomingCounter <= this.counter) {
        if (incomingCounter < this.counter) {
            int index = this.exceptions.indexOf(incomingCounter);
            if (this.exceptions.contains(index)) {
                this.exceptions.remove(index);
            } else {
                System.out.println("[Version->update] index not found at exception! (psst.. your code is probably incorrect, but if it works, it works)");
            }
        } else if (incomingCounter == this.counter + 1) {
            this.counter++;
        } else {
            for (int i = this.counter + 1; i < incomingCounter; i++) {
                this.exceptions.add(i);
            }
            this.counter = incomingCounter;
        }
    }
}
