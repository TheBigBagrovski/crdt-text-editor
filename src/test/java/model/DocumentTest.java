package model;

import okp.nic.model.Char;
import okp.nic.model.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentTest {

    @Test
    void testClearDocument_thenEmptyDocument() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.clearDocument();
        assertEquals(2, document.length());
        assertEquals("", document.getContent());
    }

    @Test
    void testGetContent_thenCorrectContent() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.insertChar("user1", 2, 'b');
        document.insertChar("user1", 3, 'c');
        assertEquals("abc", document.getContent());
    }

    @Test
    void testIthVisible_thenCorrectVisibleChar() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.insertChar("user1", 2, 'b');
        document.insertChar("user1", 3, 'c');
        assertEquals('b', document.ithVisible(2).getValue());
        assertEquals('!', document.ithVisible(4).getValue());
    }

    @Test
    void testLength_thenCorrectLength() {
        Document document = new Document();
        assertEquals(2, document.length());
        document.insertChar("user1", 1, 'a');
        assertEquals(3, document.length());
    }

    @Test
    void testIncrementLocalClock_thenIncrementedClock() {
        Document document = new Document();
        assertEquals(0, document.getLocalClock());
        document.incrementLocalClock();
        assertEquals(1, document.getLocalClock());
    }

    @Test
    void testPosition_thenCorrectPosition() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        String charId = document.getChars().get(1).getId();
        assertEquals(2, document.position(charId));
    }

    @Test
    void testNonexistentPosition_thenMinusOne() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        assertEquals(-1, document.position("nonexistent"));
    }

    @Test
    void testFind_thenCorrectChar() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        String charId = document.getChars().get(1).getId();
        assertEquals('a', document.find(charId).getValue());
    }

    @Test
    void testSubsequence_thenCorrectSubsequence() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.insertChar("user1", 2, 'b');
        document.insertChar("user1", 3, 'c');
        Char startChar = document.find(document.getChars().get(1).getId());
        Char endChar = document.find(document.getChars().get(3).getId());
        List<Char> subsequence = document.subsequence(startChar, endChar);
        assertEquals(2, subsequence.size());
        assertEquals('b', subsequence.get(0).getValue());
    }

    @Test
    void testSubsequence_thenIllegalArgumentException() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.insertChar("user1", 2, 'b');
        document.insertChar("user1", 3, 'c');
        Char startChar = document.find(document.getChars().get(1).getId());
        Char endChar = document.find(document.getChars().get(3).getId());
        assertThrows(IllegalArgumentException.class, () -> document.subsequence(endChar, startChar));
    }

    @Test
    void testLocalInsert_thenCorrectInsert() {
        Document document = new Document();
        Char charToInsert = new Char("user1:1", true, 'a', "start", "end");
        document.localInsert(charToInsert, 1);
        assertEquals("a", document.getContent());
    }

    @Test
    void testLocalInsert_thenIndexOutOfBoundsException() {
        Document document = new Document();
        Char charToInsert = new Char("user1:1", true, 'a', "start", "end");
        document.localInsert(charToInsert, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> document.localInsert(charToInsert, 3));
    }

    @Test
    void testLocalInsert_thenIllegalArgumentException() {
        Document document = new Document();
        Char charToInsert = new Char("user1:1", true, 'a', "start", "end");
        document.localInsert(charToInsert, 1);
        assertThrows(IllegalArgumentException.class, () -> document.localInsert(new Char("", true, 'a', "", ""), 1));
    }

    @Test
    void testIntegrateInsert_thenCorrectText() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.insertChar("user1", 2, 'c');
        Char charToInsert = new Char("user1:3", true, 'b', document.getChars().get(1).getId(), document.getChars().get(2).getId());
        document.integrateInsert(charToInsert, document.getChars().get(1), document.getChars().get(2));
        assertEquals("abc", document.getContent());
    }

    @Test
    void testGenerateInsert_thenCorrectText() {
        Document document = new Document();
        document.generateInsert("user1", 1, 'a');
        assertEquals("a", document.getContent());
        document.generateInsert("user1", 0, 'b');
        assertEquals("ba", document.getContent());
    }

    @Test
    void testIntegrateDelete_thenCorrectText() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.integrateDelete(document.getChars().get(1));
        assertEquals("", document.getContent());
    }

    @Test
    void testGenerateDelete_thenEmptyText() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.generateDelete(1);
        assertEquals("", document.getContent());
    }

    @Test
    void testInsertChar_thenCorrectText() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        assertEquals("a", document.getContent());
    }

    @Test
    void testDeleteChar_thenEmptyText() {
        Document document = new Document();
        document.insertChar("user1", 1, 'a');
        document.deleteChar(1);
        assertEquals("", document.getContent());
    }

    @Test
    void testInsertTextBlock_thenCorrectText() {
        Document document = new Document();
        document.insertTextBlock("user1", 1, "abc");
        assertEquals("abc", document.getContent());
    }

    @Test
    void testDeleteRange_thenCorrectText() {
        Document document = new Document();
        document.insertTextBlock("user1", 1, "abc");
        document.deleteRange(0, 2);
        assertEquals("c", document.getContent());
    }

}