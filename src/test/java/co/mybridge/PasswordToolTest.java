package co.mybridge;

import junit.framework.TestCase;

public class PasswordToolTest extends TestCase {
	public PasswordToolTest (String name) {
		super(name);
	}
	
    public void testPasswordEncryption() 
    {
    	String p1 = "abcd12345";
    	String p2 = "Abcd12345"; // capitalization not match
    	String p3 = "abcd 12345";  // space within should not match
    	String p4 = "abcd12345 ";  // trailing spaces should match
    	assertTrue(PasswordTool.verifyPassword(p1, PasswordTool.generatePasswordHash(p1)));
    	assertFalse(PasswordTool.verifyPassword(p2, PasswordTool.generatePasswordHash(p1)));
    	assertFalse(PasswordTool.verifyPassword(p3, PasswordTool.generatePasswordHash(p1)));
    	assertTrue(PasswordTool.verifyPassword(p4, PasswordTool.generatePasswordHash(p1)));
    }
}
