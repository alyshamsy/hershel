package file;

import java.security.*;
import java.io.*;

/*
 * Created by: Shadanan Sharma
 * Created on: 4-Jan-2006
 * E-Mail: shadanan@gmail.com
 * Web Site: http://www.convergence2000.com
 */

public class SHA1utils {
	/**
	 * Returns the SHA1 hash of a file.
	 * @param file - The file to calculate the SHA1 hash of.
	 * @return SHA1 hash of a file or null if the file is a folder or doesn't exist.
	 */
	public static byte[] getSHA1Digest(File file) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			FileInputStream fs = new FileInputStream(file);
			byte[] data = new byte[1024];
			int read = fs.read(data,0,1024); 
			while (read != -1) {
				md.update(data, 0, read);
				read = fs.read(data,0,1024);
			}
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("SHA1 Algorithm Not Found");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("File could not be found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Unable to read from file.");
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getSHA1Digest(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			return md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			System.out.println("SHA1 Algorithm Not Found");
			e.printStackTrace();
		}
		return null;
	}
	
	public static String digestToHexString(byte[] digest) {
		String result = "";
		for (int i = 0; i < digest.length; i++) {
			result += Integer.toHexString(digest[i] & 0xff);
		}
		return result;
	}
}