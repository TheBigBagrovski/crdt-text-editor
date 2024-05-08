package okp.nic.crdt;

import lombok.Getter;
import lombok.Setter;
import okp.nic.network.Controller;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Document {
    private final List<Char> chars;
    private final Controller controller;

    public Document(Controller controller) {
        this.controller = controller;
        chars = new ArrayList<>();
        chars.add(Char.startChar());
        chars.add(Char.endChar());
    }

    public void clearDocument() {
        chars.clear();
        chars.add(Char.startChar());
        chars.add(Char.endChar());
    }

    public String content() {
        StringBuilder sb = new StringBuilder();
        for (Char c : chars) {
            if (c.isVisible()) {
                sb.append(c.getValue());
            }
        }
        return sb.toString();
    }

    public Char ithVisible(int position) {
        int count = 0;
        for (Char c : chars) {
            if (c.isVisible()) {
                if (count == position - 1) {
                    return c;
                }
                count++;
            }
        }
        return new Char("-1", false, '!', "", "");
    }

    public int length() {
        return chars.size();
    }

    public int position(String charID) {
        for (int i = 0; i < chars.size(); i++) {
            if (chars.get(i).getId().equals(charID)) {
                return i + 1;
            }
        }
        return -1;
    }

    public Char find(String id) {
        for (Char c : chars) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return new Char("-1", false, '!', "", "");
    }

    public List<Char> subseq(Char wCharStart, Char wCharEnd) {
        int startPosition = position(wCharStart.getId());
        int endPosition = position(wCharEnd.getId());

        if (startPosition == -1 || endPosition == -1) {
            throw new IllegalArgumentException("Subsequence bound(s) not present");
        }

        if (startPosition > endPosition) {
            throw new IllegalArgumentException("Subsequence bound(s) not present");
        }

        if (startPosition == endPosition) {
            return new ArrayList<>();
        }

        return chars.subList(startPosition, endPosition);
    }

    public Document localInsert(Char charToInsert, int position) {
        if (position < 0 || position >= length()) {
            System.out.println(position);
            System.out.println(length());
            throw new IndexOutOfBoundsException("Position out of bounds");
        }

        if (charToInsert.getId().isEmpty()) {
            throw new IllegalArgumentException("Empty Char ID provided");
        }

        chars.add(position, charToInsert);

        chars.get(position - 1).setIdNext(charToInsert.getId());
        chars.get(position + 1).setIdPrevious(charToInsert.getId());

        return this;
    }

    public Document integrateInsert(Char charToInsert, Char charPrev, Char charNext) {
        List<Char> subsequence = subseq(charPrev, charNext);
        int position = position(charNext.getId());
        if (subsequence.isEmpty()) {
            return localInsert(charToInsert, position);
        }
        if (subsequence.size() == 1) {
            return localInsert(charToInsert, position - 1);
        }
        int i = 1;
        while (i < subsequence.size() - 1 && subsequence.get(i).getId().compareTo(charToInsert.getId()) < 0) {
            i++;
        }
        return integrateInsert(charToInsert, subsequence.get(i - 1), subsequence.get(i));
    }

    public void generateInsert(String from, int position, char value) {
        controller.incrementLocalClock();

        Char charPrev = ithVisible(position);
        Char charNext = ithVisible(position + 1);

        if (charPrev.getId().equals("-1")) {
            charPrev = find("start");
        }
        if (charNext.getId().equals("-1")) {
            charNext = find("end");
        }

        Char charToInsert = new Char(
                from + controller.getLocalClock(),
                true,
                value,
                charPrev.getId(),
                charNext.getId()
        );

        integrateInsert(charToInsert, charPrev, charNext);
    }

    public void integrateDelete(Char charToDelete) {
        int position = position(charToDelete.getId());
        if (position == -1) {
            return;
        }
        chars.get(position - 1).setVisible(false);
    }

    public void generateDelete(int position) {
        Char charToDelete = ithVisible(position);
        integrateDelete(charToDelete);
    }

    public void insert(String from, int position, char value) {
        try {
            generateInsert(from, position, value);
        } catch (Exception e) {
            content();
        }
    }

    public void delete(int position) {
        generateDelete(position);
    }

    public Document insertBlock(String from, int position, String text) {
        for (char c : text.toCharArray()) {
            insert(from, position++, c);
        }
        return this;
    }

}