import java.net.*;
import java.io.*;
import java.util.*;
import javax.crypto.Cipher;  

public class Server
{

    public static void main(String args[]) throws IOException
    {
        ServerSocket server = new ServerSocket(5000);
        
        while(true)
        {
        	Socket s = null;
        	try
        	{
        		s = server.accept();
        		System.out.println("Client connected : " + s);
        		
        		DataInputStream input = new DataInputStream(s.getInputStream());
        		DataOutputStream output = new DataOutputStream(s.getOutputStream());
        		
        		System.out.println("Assigning new thread for client");
        		
        		Thread t = new ClientThread(s,input,output);
        		t.start();
        	}
        	catch (Exception e)
        	{
        		s.close();
        		e.printStackTrace();
        	}
        }
    }
        
}
class ClientThread extends Thread
{
	public static int balance;
	static Cipher cipher;
	final DataInputStream input;
	final DataOutputStream output;
	final Socket s;

	
	public ClientThread(Socket s, DataInputStream input, DataOutputStream output)
	{
		this.s = s;
		this.input = input;
		this.output = output;
	}
	public void run()
	{
		String clientsays;
		
		try //DH key exchange
		{
			
			int b = 6; //server's private key
			int clientP, clientG, clientA, B, symmetricSecret;
			String Bstr;
			System.out.println("Diffie-Hellman Key Exchange...");
			System.out.println("Server's private key: " + b);
			clientP = Integer.parseInt(input.readUTF());
			System.out.println("From Client: P = " + clientP);
			clientG = Integer.parseInt(input.readUTF());
			System.out.println("From Client: G = " + clientG);
			clientA = Integer.parseInt(input.readUTF());
			System.out.println("Client's public key = " + clientA);
			B = (int) ((Math.pow(clientG, b)) % clientP); //calculate B using G and server's private key
			Bstr = Integer.toString(B);
			output.writeUTF(Bstr); //send B to client
			
			symmetricSecret = (int) ((Math.pow(clientA, b)) % clientP); // calculate symmetric secret key using clientA and private key
			System.out.println("Secret key to perform symmetric encryption = " + symmetricSecret);
			System.out.println();
		
		
		while(true)
		{
			//switch cases based on user input
			try
			{
				String sendBalance = "Your balance is: ";
				String invalidAmount = "Invalid amount.";
				String headlineMessage = ("What action would you like to perform on your account? [Balance | Deposit | Withdraw]..\n" + "Type 'logout' to logout.");
				
				balance = checkBalance(balance); //initial Balance of account
				output.writeUTF(encrypt(headlineMessage,symmetricSecret));
				clientsays = input.readUTF();
				System.out.println("Encrypted Client: " + clientsays);
				clientsays = decrypt(clientsays,symmetricSecret);
				System.out.println("Decrypted Client: " + clientsays);
				System.out.println("Client says: " + clientsays);
				if(clientsays.equals("logout"))
				{
					String logout = "Logging out...";
					output.writeUTF(logout);
					System.out.println("Client " + this.s + " is logging out...");
					System.out.println("Closing connection...");
					this.s.close();
					System.out.println("Connection closed.");
					break;
				}
				
				//switch cases for account will go below here
				switch(clientsays)
				{
				case "balance" :
					balance = checkBalance(balance);
					output.writeUTF(encrypt(sendBalance + balance,symmetricSecret));
					break;
				case "deposit" :
					clientsays = input.readUTF();
					System.out.println("Encrypted amount: " + clientsays);
					clientsays = decrypt(clientsays,symmetricSecret);
					int depositMessage = Integer.valueOf(clientsays);
					if(depositMessage > 0)
					{
						System.out.println("Client wants to deposit: " + depositMessage);
						deposit(depositMessage);
						System.out.println("Client's new balance = " + balance);
						output.writeUTF(encrypt(sendBalance + balance,symmetricSecret));
					}
					else
						output.writeUTF(encrypt(invalidAmount,symmetricSecret));
					
					break;
				case "withdraw" :
					clientsays = input.readUTF();
					System.out.println("Encrypted amount: " + clientsays);
					clientsays = decrypt(clientsays,symmetricSecret);
					int withdrawMessage = Integer.valueOf(clientsays);
					if(withdrawMessage > 0)
					{
						System.out.println("Client wants to withdraw: " + withdrawMessage);
						withdraw(withdrawMessage);
						System.out.println("Client's new balance = " + balance);
						output.writeUTF(encrypt(sendBalance + balance,symmetricSecret));
					}
					else
						output.writeUTF(invalidAmount);
					break;
				default :
					output.writeUTF(encrypt("Invalid input: " + clientsays,symmetricSecret));
					break;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			this.input.close();
			this.output.close();
			System.out.println("Stream closed.");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		}
	public static int checkBalance(int balance) throws Exception
	{
		//System.out.println("Checking client's balance.");
		Scanner fileScan = new Scanner(new File("AccountBalance.txt"));
		while(fileScan.hasNextInt())
		{
			balance = fileScan.nextInt();
		}
		//System.out.println("Client's current balance = " + balance);
		fileScan.close();
		return balance;
	}
	public static int deposit(int depositAmount) throws Exception
	{
		//checkBalance(balance);
		PrintStream ps = new PrintStream("AccountBalance.txt");
		System.out.println("Depositing funds...");
		balance = checkBalance(balance) + depositAmount;
		ps.println(balance);
		ps.close();
		return balance;
	}
	public static int withdraw(int withdrawAmount) throws Exception
	{
		//checkBalance(balance);
		PrintStream ps = new PrintStream("AccountBalance.txt");
		System.out.println("Withdrawing funds...");
		balance = checkBalance(balance) - withdrawAmount;
		ps.println(balance);
		ps.close();
		return balance;
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
    	String decryptedText = "";
    	key=(char)key;
    	int length = cipherText.length();
    	for(int i=0; i<length;i++)
    	{
    		decryptedText = decryptedText + Character.toString((char)(cipherText.charAt(i)^key));
    	}
        return decryptedText;
    }
}
    
   
