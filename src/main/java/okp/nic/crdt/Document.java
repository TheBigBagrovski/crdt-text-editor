package okp.nic.crdt;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Document {

    private final List<Char> chars;
    private int localClock = 0;

    public Document() {
        chars = new ArrayList<>();
        chars.add(Char.startChar());
        chars.add(Char.endChar());
    }

    public void clearDocument() {
        chars.clear();
        chars.add(Char.startChar());
        chars.add(Char.endChar());
    }

    public String getContent() {
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

    public void incrementLocalClock() {
        localClock++;
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

    public List<Char> subsequence(Char wCharStart, Char wCharEnd) {
        int startPosition = position(wCharStart.getId());
        int endPosition = position(wCharEnd.getId());

        if (startPosition == -1 || endPosition == -1 || startPosition > endPosition) {
            throw new IllegalArgumentException("Неверные границы подпоследовательности");
        }

        if (startPosition == endPosition) {
            return new ArrayList<>();
        }

        return chars.subList(startPosition, endPosition);
    }

    public Document localInsert(Char charToInsert, int position) {
        if (position < 0 || position >= length()) {
            throw new IndexOutOfBoundsException("Выход за границы");
        }

        if (charToInsert.getId().isEmpty()) {
            throw new IllegalArgumentException("Пустой Char ID");
        }

        chars.add(position, charToInsert);

        chars.get(position - 1).setIdNext(charToInsert.getId());
        chars.get(position + 1).setIdPrevious(charToInsert.getId());

        return this;
    }

    public Document integrateInsert(Char charToInsert, Char charPrev, Char charNext) {
        List<Char> subsequence = subsequence(charPrev, charNext);
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
        incrementLocalClock();

        Char charPrev = ithVisible(position);
        Char charNext = ithVisible(position + 1);

        if (charPrev.getId().equals("-1")) {
            charPrev = find("start");
        }
        if (charNext.getId().equals("-1")) {
            charNext = find("end");
        }

        Char charToInsert = new Char(
                from + ":" + localClock,
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

    public void insertChar(String from, int position, char value) {
        try {
            generateInsert(from, position, value);
        } catch (Exception e) {
            getContent();
        }
    }

    public void deleteChar(int position) {
        generateDelete(position);
    }

    public void updateContent(String from, String text) {
        for (int i = 0; i < text.length(); i++) {
            generateInsert(from, i, text.charAt(i));
        }
    }

}