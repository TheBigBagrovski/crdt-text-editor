package model;

import okp.nic.model.Char;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharTest {

    @Test
    public void testStartChar_thenCorrectChar() {
        Char startChar = Char.startChar();
        assertNotNull(startChar);
        assertEquals("start", startChar.getId());
        assertFalse(startChar.isVisible());
        assertEquals('!', startChar.getValue());
        assertEquals("", startChar.getIdPrevious());
        assertEquals("end", startChar.getIdNext());
    }

    @Test
    public void testEndChar_thenCorrectChar() {
        Char endChar = Char.endChar();
        assertNotNull(endChar);
        assertEquals("end", endChar.getId());
        assertFalse(endChar.isVisible());
        assertEquals('!', endChar.getValue());
        assertEquals("start", endChar.getIdPrevious());
        assertEquals("", endChar.getIdNext());
    }

    @Test
    public void testCompareTo_thenCorrectComparisons() {
        Char char1 = new Char("char1", true, 'a', "prev", "next");
        Char char2 = new Char("char2", false, 'b', "prev", "next");
        Char char3 = new Char("char1", true, 'c', "prev", "next");

        assertTrue(char1.compareTo(char2) < 0);
        assertTrue(char2.compareTo(char1) > 0);
        assertEquals(0, char1.compareTo(char3));
    }

    @Test
    public void testToString_thenCorrectString() {
        Char testChar = new Char("char1", true, 'a', "prev", "next");
        String expected = "Char(id=char1, visible=true, value=a, idPrevious=prev, idNext=next)";
        assertEquals(expected, testChar.toString());
    }

}