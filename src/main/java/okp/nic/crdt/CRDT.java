package okp.nic.crdt;

import lombok.Getter;
import okp.nic.network.Controller;
import okp.nic.vectorclock.VersionVector;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CRDT {
    private final String siteId;
    private final List<Char> struct = new ArrayList<>();
    private final Controller controller;
    private final VersionVector versionVector;

    public CRDT(String siteId, Controller controller) {
        this.siteId = siteId;
        this.controller = controller;
        this.versionVector = controller.getVersionVector();
    }

    public void clearText() {
        struct.clear();
        versionVector.reset();
    }

    public void loadText(char[] text) {
        clearText();
        for (int i = 0; i < text.length; i++) {
            localInsert(text[i], i);
        }
    }

    public Char localInsert(char value, int index) {
        versionVector.incrementLocalVersion();
        Char curChar = generateChar(value, index);
        struct.add(index, curChar);
        return curChar;
    }

    public void remoteInsert(Char c) {
        int index = findInsertIndex(c);
        struct.add(index, c);
        controller.insertToTextEditor(c.getValue(), index);
    }

    public int findInsertIndex(Char val) {
        int left = 0;
        int right = struct.size() - 1;

        if (struct.isEmpty() || val.compareTo(struct.get(left)) < 1) {
            return left;
        } else if (val.compareTo(struct.get(right)) > 0) {
            return struct.size();
        }

        while ((left + 1) < right) {
            int mid = (int) Math.floor(left + (double) (right - left) / 2);
            int compareNum = val.compareTo(struct.get(mid));

            if (compareNum == 0) {
                return mid;
            } else if (compareNum > 0) {
                left = mid;
            } else {
                right = mid;
            }
        }

        if (val.compareTo(struct.get(left)) == 0) {
            return left;
        } else {
            return right;
        }
    }

    public Char localDelete(int index) {
        versionVector.incrementLocalVersion();
        Char c = struct.get(index - 1);
        struct.remove(index - 1);
//        printString();
        return c;
    }

    public void remoteDelete(Char c) {
        int index = findPosition(c);
        if (index == -1) {
            System.out.println("no matching index found");
            return;
        }
        struct.remove(index);
        controller.deleteToTextEditor(index);
//        printString();
    }

    public int findPosition(Char c) {
        for (int i = 0; i < struct.size(); i++) {
            if (c.compareTo(struct.get(i)) == 0) {
                return i;
            }
        }
        return -1;
    }

    public Char generateChar(char value, int index) {
        List<Identifier> posBefore;
        if (((index - 1) >= 0) && ((index - 1) < struct.size())) {
            posBefore = struct.get(index - 1).getPosition();
        } else {
            posBefore = new ArrayList<>();
        }

        List<Identifier> posAfter;
        if (((index) >= 0) && ((index) < struct.size())) {
            posAfter = struct.get(index).getPosition();
        } else {
            posAfter = new ArrayList<>();
        }

        List<Identifier> newPos = new ArrayList<>();
        generatePosBetween(posBefore, posAfter, newPos, 0);
        return new Char(value, newPos, siteId, versionVector.getLocalVersion().getCounter());
    }

    public void generatePosBetween(List<Identifier> posBefore,
                                   List<Identifier> posAfter,
                                   List<Identifier> newPos,
                                   int level) {
        int base = 32;
        int newBase = (int) Math.pow(2, level) * base;
        char boundaryStrategy = retrieveStrategy();

        Identifier idBefore;
        if (!posBefore.isEmpty()) {
            idBefore = posBefore.get(0);
        } else {
            idBefore = new Identifier(0, siteId);
        }
        Identifier idAfter;
        if (!posAfter.isEmpty()) {
            idAfter = posAfter.get(0);
        } else {
            idAfter = new Identifier(newBase, siteId);
        }

        if ((idAfter.getDigit() - idBefore.getDigit()) > 1) {
            int newDigit = generateIdBetween(idBefore.getDigit(),
                    idAfter.getDigit(),
                    boundaryStrategy);
            newPos.add(new Identifier(newDigit, siteId));
        } else if ((idAfter.getDigit() - idBefore.getDigit()) == 1) {
            newPos.add(idBefore);
            if (!posBefore.isEmpty()) {
                posBefore = posBefore.subList(1, posBefore.size());
            }
            generatePosBetween(posBefore,
                    new ArrayList<>(),
                    newPos,
                    level + 1);
        } else if (idBefore.getDigit() == idAfter.getDigit()) {
            int comSiteId = idBefore.getSiteId().compareTo(idAfter.getSiteId());
            if (comSiteId < 0) {
                newPos.add(idBefore);
                if (!posBefore.isEmpty()) {
                    posBefore = posBefore.subList(1, posBefore.size());
                }
                generatePosBetween(posBefore,
                        new ArrayList<>(),
                        newPos,
                        level + 1);
            } else if (comSiteId == 0) {
                newPos.add(idBefore);
                if (!posBefore.isEmpty()) {
                    posBefore = posBefore.subList(1, posBefore.size());
                }
                if (!posAfter.isEmpty()) {
                    posAfter = posAfter.subList(1, posAfter.size());
                }
                generatePosBetween(posBefore,
                        posAfter,
                        newPos,
                        level + 1);
            } else {
                throw new Error("u no gud at coding");
            }
        }
    }

    public char retrieveStrategy() {
        if (Math.round(Math.random()) == 1) {
            return '+';
        } else {
            return '-';
        }
    }

    public int generateIdBetween(int min, int max, char boundaryStrategy) {
        int boundary = 10;
        if ((max - min) < boundary) {
            min = min + 1;
        } else {
            if (boundaryStrategy == '-') {
                min = max - boundary;
            } else {
                min = min + 1;
                max = min + boundary;
            }
        }
        return (int) Math.floor(Math.random() * (max - min)) + min;
    }

//    public void printString() {
//        for (Char c : struct) {
//            System.out.print(c.getValue());
//        }
//        System.out.println();
//    }
}





/*
    public void printLocation(Char c) {
        System.out.print("[printLocation] loc = [");
        List<Identifier> location = c.getPosition();
        for (int i = 0; i < location.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(location.get(i).getDigit());
        }
        System.out.println("]");
    }


        public int findInsertPosition(Char c) {
        int maxIndex = struct.size() - 1;
        if (struct.isEmpty() || c.compareTo(struct.get(0)) <= 0) {
            return 0;
        }
        Char lastChar = struct.get(maxIndex);
        if (c.compareTo(lastChar) > 0) {
            return struct.size();
        }
        for (int i = 1; i < (struct.size() - 1); i++) {
            Char leftChar = struct.get(i - 1);
            Char rightChar = struct.get(i + 1);
            if (c.compareTo(rightChar) == 0) {
                return i;
            }
            if ((c.compareTo(leftChar) > 0) && (c.compareTo(rightChar) < 0)) {
                return i;
            }
        }
        return 0;
    }
 */