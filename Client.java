// A Java program for a Client
import java.net.*;
import java.io.*;
import java.util.*; 
import javax.crypto.Cipher; 
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchProviderException;
import java.security.NoSuchAlgorithmException;

public class Client
{
	static Cipher cipher;
    public static void main(String args[]) throws Exception
    {
    	
    	//login();
    	if(login() == true)
    	{
    		try
    		{
        	//initialize input and output streams
        	Scanner scan = new Scanner(System.in);
        	InetAddress ip = InetAddress.getByName("localhost");
        	Socket s = new Socket(ip, 5000);
        	DataInputStream input = new DataInputStream(s.getInputStream());
        	DataOutputStream output = new DataOutputStream(s.getOutputStream());
        	
        	
        	//Diffie-Hellman Key Exchange...
        	System.out.println("Exchanging secret keys...");
    		//////////////////////////////////////////////////
        	String pstr, gstr, Astr; //strings of following numbers
        	int p = 23;
        	int g = 43;
        	int a = 4; //client's private key
        	int symmetricSecret, serverB;
        	pstr = Integer.toString(p);
        	output.writeUTF(pstr);
        	
        	gstr = Integer.toString(g);
        	output.writeUTF(gstr);
        	
        	int A = (int) ((Math.pow(g,a)) % p); //client's public key
        	Astr = Integer.toString(A);
        	output.writeUTF(Astr); //sending public key!
        	serverB = Integer.parseInt(input.readUTF());
        	symmetricSecret = (int) ((Math.pow(serverB,  a)) % p);
        	System.out.println("Crypto complete.");
        	System.out.println();
        	
        	
        	
        	while(true) //encrypted communication between client and server
        	{
        		String message = input.readUTF();
        		String decryptedMessage = decrypt(message,symmetricSecret);
        		System.out.println(decryptedMessage);
        		message = scan.nextLine();
        		//encrypt message before sending out
        		String messageE = encrypt(message,symmetricSecret); //messageE is encrypted message
        		
        		String message1;
        		
        		
        		
        		if(message.equals("deposit")) //if message is deposit
        		{
        			output.writeUTF("" + messageE); //send 'deposit'
        			System.out.println("How much would you like to deposit?");
        			message1 = scan.nextLine(); //get amount to deposit
        			String encryptedMessage1 = encrypt(message1,symmetricSecret);
        			output.writeUTF("" + encryptedMessage1); //encrypt and send amount to deposit
        		}
        		if(message.equals("withdraw")) //if message is withdraw
        		{
        			output.writeUTF("" + messageE); //send 'withdraw'
        			System.out.println("How much would you like to withdraw?");
        			message1 = scan.nextLine(); //get amount to withdraw
        			String encryptedMessage1 = encrypt(message1,symmetricSecret); 
        			output.writeUTF("" + encryptedMessage1); //encrypt and send amount to withdraw
        		}
        		
        		if(message.equals("logout")) //if message is logout close connection
        		{
        			String logoutMessage = "logout";
        			output.writeUTF(encrypt(logoutMessage,symmetricSecret));
        			System.out.println("Logging out...");
        			s.close();
        			System.out.println("Successfully logged out.");
        			break;
        		}
        		if(message.equals("balance")) //if message is balance send to server
        			output.writeUTF(messageE); 
        		if(!message.equals("balance") && !message.equals("withdraw") && !message.equals("deposit") && !message.equals("logout"))
        			output.writeUTF(messageE);
        		
        		String serversays = input.readUTF();
        		serversays = decrypt(serversays,symmetricSecret);
        		System.out.println("Server: " + serversays);
        	}
        
        		
        	//close resources
        	scan.close();
        	input.close();
        	output.close();
        
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
    }
    	System.out.println("See you later!");
    }


    public static String encrypt(String plainText, int key) throws Exception
    {
    	String encryptedText = "";
    	key=(char)key;
    	int length = plainText.length();
    	for(int i=0; i<length;i++)
    	{
    		encryptedText = encryptedText + Character.toString((char)(plainText.charAt(i)^key));
    	}
        return encryptedText;
    }
    public static String decrypt(String cipherText, int key) throws Exception 
    {
    	//exactly the same as encrypt method... simple XOR to key number ... basically a ceasar cipher
    	String decryptedText = "";
    	key=(char)key;
    	int length = cipherText.length();
    	for(int i=0; i<length;i++)
    	{
    		decryptedText = decryptedText + Character.toString((char)(cipherText.charAt(i)^key));
    	}
        return decryptedText;
    }
    public static boolean login() throws Exception
    {
    	Scanner sc = new Scanner(System.in);
    	System.out.println("Welcome to the banking client. \n Please enter a username");
    	String username = sc.next(); //entered username
    	byte[] salt = receiveSalt();
    	username = getHash(username, salt); //hash entered username
    	
    	//System.out.println("Hashed username: " + username);  //debugging
    	
    	System.out.println("Please enter the password to that account.");
    	String password = sc.next(); //entered password
    	password = getHash(password, salt); //hashed entered password
    	
    	//System.out.println("Hashed password: " + password); //debugging
    	
    	String hshUsername = "";
    	String hshPassword = "";
    	Scanner fileScan = new Scanner(new File("credentials.txt"));
		while(fileScan.hasNext()) //check hashed username and password in file
		{
			hshUsername = fileScan.nextLine(); 
			hshPassword = fileScan.nextLine();
		}

		boolean login = false; //login boolean to decide if to connect to server
		if(username.equals(hshUsername) && password.equals(hshPassword)) //if entered username and password match hashes in file
		{
			System.out.println("Access granted.. connecting to server.");
			login = true;
		}
		
		else
		{
			System.out.println("You've been shot and killed");
			login = false;
		}

		fileScan.close();
    	return login;
    	
    }

    public static byte[] receiveSalt() throws NoSuchAlgorithmException  
    {  
 
    // Create an array for the salt  
    byte[] s = new byte[15];  
    for(int i=0;i<15;i++)
    {
    	s[i] = 0;
    }
    //return the salt  
    return s;  
    }  
    public static String getHash(String input,byte[] salt)
    {
    	 try {
    		  
             // Static getInstance method is called with hashing MD5
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(salt); //add salt to digest
             
             // digest() method is called to calculate message digest
             //  of an input digest() return array of byte
             byte[] messageDigest = md.digest(input.getBytes());
             
             // Convert byte array into signum representation
             BigInteger no = new BigInteger(1, messageDigest);
   
             // Convert message digest into hex value
             String hashtext = no.toString(16);
             while (hashtext.length() < 32) {
                 hashtext = "0" + hashtext;
             }
             return hashtext;
         } 
   
         // For specifying wrong message digest algorithms
         catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }   
}