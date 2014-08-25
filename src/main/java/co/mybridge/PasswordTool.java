package co.mybridge;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.Random;

public class PasswordTool {
	private static final int _saltSize = 4;
	private static Random rg = null; 
	
    private static String getSalt()
    {
    	try {
    		if (rg == null) {
    			rg = new Random();
    		}
	        int r = Math.abs(rg.nextInt());
	        
	        NumberFormat nf = NumberFormat.getNumberInstance();
	        nf.setMinimumIntegerDigits(_saltSize);
	        nf.setMaximumIntegerDigits(_saltSize);
	        nf.setGroupingUsed(false);	       
	        return nf.format(r);
    	} catch (Exception x) {
    		System.out.println("Something is wrong with salt generation, reset Random Generator, use static salt for this one ");
    		x.printStackTrace();
    		rg = null;
    		
    		return "1234";  // this should have the same length as _saltSize
    	}
    }
    
    /**
     * This will return a salted SHA-256 hash for a password, with first 6 
     *  
     * @param origPassword
     * @return 
     */
    public static String generatePasswordHash(String origPassword) {
    	 String salt = getSalt();
         return encryptWithSalt(salt, origPassword);
    }
    
    /**
     * 
     * @param inputPassword
     * @param savedPassword
     * @return
     */
    public static boolean verifyPassword(String inputPassword, String savedPassword) {
    	String salt = savedPassword.substring(0, _saltSize);
    	String generatedPassword = encryptWithSalt(salt, inputPassword);
    	return (generatedPassword.equalsIgnoreCase(savedPassword));
    }
    
    private static String encryptWithSalt(String salt, String inputPassword) {
    	String generatedPassword = null;
    	
    	try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            
            byte[] bytes = md.digest(inputPassword.trim().getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = salt + sb.toString();
            
            //System.out.println("password hash: orig=" + inputPassword + "; salt=" + salt + "; generated=" + generatedPassword);
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
            generatedPassword = salt + inputPassword;            
        }
    	return generatedPassword;
    }
}
