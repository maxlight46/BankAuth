# BankAuth

To start this program, first start the server.java file. This starts the server
Next, start the client.java file. This will prompt a login.
To log in, use username "bankuser" and password "password"
This input is validated using the text file "credentials.txt" which contains the salted hashes (md5) of these credentials.
Upon logging in, you are presented with the option to either type:
"balance" to check your balance
"deposit" to deposit funds. This will then prompt you an amount to type before being able to type the next command.
"withdraw" to withdraw funds. This will then prompt you an amount to withdraw before being able to type the next command.
The bank account's balance is stored in "AccountBalance.txt" which will be edited with every command, and keep previous amounts even through program close.
Once done, you can type "logout" on the client to remove connection to the server. The server will have to manually be terminated. 

This was compiled in the Java Eclipse IDE.
