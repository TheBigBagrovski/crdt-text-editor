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
            controller.onInsert(text[i], i);
        }
    }

//    public Char localInsert(char value, int index) {
//        versionVector.incrementLocalVersion();
//        Char curChar = generateChar(value, index);
//        struct.add(index, curChar);
//        return curChar;
//    }

    public Char localInsert(char value, int index) {
        versionVector.incrementLocalVersion();
        double newPosition = calculatePosition(index);
        Char curChar = new Char(value, newPosition, siteId, versionVector.getLocalVersion().getCounter());
        struct.add(index, curChar);
        return curChar;
    }

    private double calculatePosition(int index) {
        if (index == 0) {
            return 0.0;
        } else if (index == struct.size()) {
            return 1.0;
        } else {
            Char before = struct.get(index - 1);
            Char after = struct.get(index);
            return (before.getPosition() + after.getPosition()) / 2;
        }
    }

    public Char generateChar(char value, int index) {
        double position = calculatePosition(index);
        return new Char(value, position, siteId, versionVector.getLocalVersion().getCounter());
    }

    public int findInsertIndex(Char val) {
        int left = 0;
        int right = struct.size() - 1;

        if (struct.isEmpty() || val.getPosition() <= struct.get(left).getPosition()) {
            return left;
        } else if (val.getPosition() >= struct.get(right).getPosition()) {
            return struct.size();
        }

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (val.getPosition() <= struct.get(mid).getPosition()) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return right;
    }

    public void remoteInsert(Char c) {
        int index = findInsertIndex(c);
        struct.add(index, c);
        controller.insertToTextEditor(c.getValue(), index);
    }

    //    public int findInsertIndex(Char val) {
//        int left = 0;
//        int right = struct.size() - 1;
//
//        if (struct.isEmpty() || val.compareTo(struct.get(left)) < 1) {
//            return left;
//        } else if (val.compareTo(struct.get(right)) > 0) {
//            return struct.size();
//        }
//
//        while (left <= right) {
//            int mid = left + (right - left) / 2;
//            int compareNum = val.compareTo(struct.get(mid));
//
//            if (compareNum == 0) {
//                return mid;
//            } else if (compareNum > 0) {
//                left = mid + 1;
//            } else {
//                right = mid - 1;
//            }
//        }
//
//        return left;
//    }
//    public int findInsertIndex(Char val) {
//        double targetPos = val.getPosition().get(0).getDigit();
//
//        int left = 0;
//        int right = struct.size() - 1;
//
//        // Handle empty struct or target position at the boundaries
//        if (struct.isEmpty() || targetPos <= struct.get(left).getPosition().get(0).getDigit()) {
//            return left;
//        } else if (targetPos >= struct.get(right).getPosition().get(0).getDigit()) {
//            return struct.size();
//        }
//
//        // Binary search to find the insertion point
//        while (left <= right) {
//            int mid = left + (right - left) / 2;
//            double midPos = struct.get(mid).getPosition().get(0).getDigit();
//
//            if (midPos == targetPos) {
//                return mid; // Exact match found
//            } else if (midPos < targetPos) {
//                left = mid + 1; // Search right
//            } else {
//                right = mid - 1; // Search left
//            }
//        }
//
//        // Return the index where the character should be inserted
//        return left;
//    }
//    public int findInsertIndex(Char val) {
//        int left = 0;
//        int right = struct.size() - 1;
//
//        if (struct.isEmpty() || val.compareTo(struct.get(left)) < 1) {
//            return left;
//        } else if (val.compareTo(struct.get(right)) > 0) {
//            return struct.size();
//        }
//
//        while ((left + 1) < right) {
//            int mid = (int) Math.floor(left + (double) (right - left) / 2);
//            int compareNum = val.compareTo(struct.get(mid));
//
//            if (compareNum == 0) {
//                return mid;
//            } else if (compareNum > 0) {
//                left = mid;
//            } else {
//                right = mid;
//            }
//        }
//
//        if (val.compareTo(struct.get(left)) == 0) {
//            return left;
//        } else {
//            return right;
//        }
//    }

    public Char localDelete(int index) {
        versionVector.incrementLocalVersion();
        Char c = struct.get(index - 1);
        struct.remove(index - 1);
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
    }

    public int findPosition(Char c) {
        for (int i = 0; i < struct.size(); i++) {
            if (c.compareTo(struct.get(i)) == 0) {
                return i;
            }
        }
        return -1;
    }

//    public Char generateChar(char value, int index) {
//        List<Identifier> posBefore;
//        if (((index - 1) >= 0) && ((index - 1) < struct.size())) {
//            posBefore = struct.get(index - 1).getPosition();
//        } else {
//            posBefore = new ArrayList<>();
//        }
//
//        List<Identifier> posAfter;
//        if (((index) >= 0) && ((index) < struct.size())) {
//            posAfter = struct.get(index).getPosition();
//        } else {
//            posAfter = new ArrayList<>();
//        }
//
//        List<Identifier> newPos = new ArrayList<>();
//        generatePosBetween(posBefore, posAfter, newPos, 0);
//        return new Char(value, newPos, siteId, versionVector.getLocalVersion().getCounter());
//    }

//    public Char generateChar(char value, int index) {
//        long pos;
//        if (index > 0 && index <= struct.size()) {
//            pos = struct.get(index - 1).getPosition() + 1;
//        } else {
//            pos = 0;
//        }
//
//        return new Char(value, pos, siteId, versionVector.getLocalVersion().getCounter());
//    }

//    public Char generateChar(char value, double index) {
//        // Find the characters before and after the insertion point
//        Char charBefore = findCharBefore(index);
//        Char charAfter = findCharAfter(index);
//
//        // Extract their positions as fractional indices
//        double posBefore = charBefore != null ? charBefore.getPosition().getDigit() : 0.0;
//        double posAfter = charAfter != null ? charAfter.getPosition().get(0).getDigit() : 1.0;
//
//        // Calculate the new fractional index
//        double newPos = (posBefore + posAfter) / 2.0;
//
//        // Create and return the new Char object
//        return new Char(value, new Identifier(newPos, siteId), siteId, versionVector.getLocalVersion().getCounter());
//    }
//
//    // Helper methods to find characters before and after the index
//    private Char findCharBefore(double index) {
//        // ... (Implementation to find the character immediately before the given index)
//    }
//
//    private Char findCharAfter(double index) {
//        // ... (Implementation to find the character immediately after the given index)
//    }
//
//    public void generatePosBetween(double posBefore, double posAfter, double newPos) {
//        newPos = (posBefore + posAfter) / 2; // Simple averaging
//        // Or use a weighted average based on specific logic
//    }

//    public void generatePosBetween(List<Identifier> posBefore,
//                                   List<Identifier> posAfter,
//                                   List<Identifier> newPos,
//                                   int level) {
//        int base = 32;
//        int newBase = (int) Math.pow(2, level) * base;
//        char boundaryStrategy = retrieveStrategy();
//
//        Identifier idBefore;
//        if (!posBefore.isEmpty()) {
//            idBefore = posBefore.get(0);
//        } else {
//            idBefore = new Identifier(0, siteId);
//        }
//        Identifier idAfter;
//        if (!posAfter.isEmpty()) {
//            idAfter = posAfter.get(0);
//        } else {
//            idAfter = new Identifier(newBase, siteId);
//        }
//
//        if ((idAfter.getDigit() - idBefore.getDigit()) > 1) {
//            int newDigit = generateIdBetween(idBefore.getDigit(),
//                    idAfter.getDigit(),
//                    boundaryStrategy);
//            newPos.add(new Identifier(newDigit, siteId));
//        } else if ((idAfter.getDigit() - idBefore.getDigit()) == 1) {
//            newPos.add(idBefore);
//            if (!posBefore.isEmpty()) {
//                posBefore = posBefore.subList(1, posBefore.size());
//            }
//            generatePosBetween(posBefore,
//                    new ArrayList<>(),
//                    newPos,
//                    level + 1);
//        } else if (idBefore.getDigit() == idAfter.getDigit()) {
//            int comSiteId = idBefore.getSiteId().compareTo(idAfter.getSiteId());
//            if (comSiteId < 0) {
//                newPos.add(idBefore);
//                if (!posBefore.isEmpty()) {
//                    posBefore = posBefore.subList(1, posBefore.size());
//                }
//                generatePosBetween(posBefore,
//                        new ArrayList<>(),
//                        newPos,
//                        level + 1);
//            } else if (comSiteId == 0) {
//                newPos.add(idBefore);
//                if (!posBefore.isEmpty()) {
//                    posBefore = posBefore.subList(1, posBefore.size());
//                }
//                if (!posAfter.isEmpty()) {
//                    posAfter = posAfter.subList(1, posAfter.size());
//                }
//                generatePosBetween(posBefore,
//                        posAfter,
//                        newPos,
//                        level + 1);
//            } else {
//                throw new Error("u no gud at coding");
//            }
//        }
//    }

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

}