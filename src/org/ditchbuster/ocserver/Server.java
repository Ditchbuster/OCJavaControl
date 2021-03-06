package org.ditchbuster.ocserver;
/**
 * Created by CPearson on 9/4/2015.
 */

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;
    // an ArrayList to keep all the ServerThreads in
    private ArrayList<ServerThread> st;
    // to display time
    private ClientThread console;
    private SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;
    private World world;
    public enum ClientType {CONSOLE,ROBOT} //types of client, used so thread creates proper streams, used by ServerThread


    /*
     *  server constructor that receive the port to listen to for connection as parameter
     *  in console
     */

    public Server(int port) {

        // the port
        this.port = port;
        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        al = new ArrayList<ClientThread>();
        st = new ArrayList<ServerThread>();
        world = new WorldBuilder(50,50).makeCaves().build();
    }

    public void start() {
        keepGoing = true;
        //Server threads to handle multiports
        ServerThread t = new ServerThread(port,ClientType.ROBOT);
        st.add(t);
        t.start();
        t = new ServerThread(port+1,ClientType.CONSOLE);
        st.add(t);
        t.start();
        // infinite loop to wait for connections

        while(keepGoing)
        {
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e){
                display("Main thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
        // I was asked to stop
        try {
            for (ServerThread thread: st){
                thread.close();
            }
            for(int i = 0; i < al.size(); ++i) {
                ClientThread tc = al.get(i);
                try {
                    tc.in.close();
                    tc.out.close();
                    tc.socket.close();
                }
                catch(IOException ioE) {
                    // not much I can do
                }
            }
        }
        catch(Exception e) {
            display("Exception closing the server and clients: " + e);
        }
    }

    /*
     * Display an event (not a message) to the console or the GUI
     */
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;

        System.out.println(time);

    }
    /*
     *  to broadcast a message to all Clients
     */
    private synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = sdf.format(new Date());
        String messageLf = time + " " + message;
        // display message on console or GUI

        System.out.println(messageLf);

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = al.size(); --i >= 0;) {
            ClientThread ct = al.get(i);
            display("Sending to:" + ct.username);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(message)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    /*
     *  To run as a console application just open a console window and:
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java org.ditchbuster.ocserver.Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java org.ditchbuster.ocserver.Server [portNumber]");
                return;

        }
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    /** One instance of this thread will run for each client */ //TODO Move this to own class and diff between console and robot
    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        ObjectOutputStream Cout;
        ObjectInputStream Cin;
        String cm; //chat message
        // my unique id (easier for deconnection)
        int id;
        int temp; //just a temp number for now
        // the Username of the Client
        String username;
        // the date I connect
        String date;
        // robot this thread is connected to
        Robot myRobot;
        ClientType type;

        // Constructore
        ClientThread(Socket socket,ClientType type) {

            this.type=type;
            temp =0;
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
			/* Creating both Data Stream */
            System.out.println("Thread trying to create Input/Output Streams");
            try
            {
                // create output first
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // read the username
                //username = in.readLine();
                display(type + " just connected. ID:" + id);
                if(type==ClientType.ROBOT) {
                    //username = username.substring(7);
                    //myRobot = new Robot(username,world,this); //TODO set to new robot or check if it already exists..
                    display("Created robot");
                }
                else if(type==ClientType.CONSOLE){
                    if (console == null) {
                        console = this;
                        display("Created console");
                        out.println("SEND TEST");
                    }
                }
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which is an object)
                if(console==null) {
                    try {
                        cm = in.readLine();
                    } catch (IOException e) {
                        display(username + " Exception reading Streams: " + e);
                        break;
                    }

                    // the message part of the ChatMessage
                    if (cm == null || (cm != "" && cm.contains("logout"))) {
                        display(username + " disconnected with logout message.");
                        keepGoing = false;
                        break;
                    } else {
                        //display(cm);
                        //out.println("FWD " + temp++);
                        broadcast(cm);
                    }
                }else{
                    try {
                        in.readLine();
                    } catch (IOException e){
                        display(username + " Exception reading Streams: " + e);
                        break;
                    }
                }
                /* Switch on the type of message receive
                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        // scan al the users connected
                        for(int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }*/
            }
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(out != null) out.close();
            }
            catch(Exception e) {}
            try {
                if(in != null) in.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        /*
         * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream

                //out.write(msg);
            out.println(msg);

            return true;
        }
    }
    /** One instance per port to listen on  **/
    class ServerThread extends Thread{
        ServerSocket socket;
        boolean keepGoing;
        ClientType type;

        public ServerThread(int port,ClientType type){
            this.type = type;
            try {
                this.socket = new ServerSocket(port);
            }catch (IOException e) {
                    String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
                    display(msg);
            }
            keepGoing=true;
        }

        @Override
        public void run() {
            try
            {
                // infinite loop to wait for connections
                while(keepGoing)
                {
                    // format message saying we are waiting
                    display("Server waiting for Clients on port " + socket.getLocalPort() + ".");

                    Socket TmpSocket = socket.accept();  	// accept connection
                    // if I was asked to stop
                    if(!keepGoing)
                        break;
                    ClientThread t = new ClientThread(TmpSocket,type);  // make a thread of it //TODO this will close all clients, make a seperate list for CONSOLE and ROBOT
                    al.add(t);									// save it in the ArrayList
                    t.start();
                }
                // I was asked to stop
                try { //TODO move this to the close function
                    socket.close();
                    for(int i = 0; i < al.size(); ++i) {
                        ClientThread tc = al.get(i);
                        try {
                            tc.in.close();
                            tc.out.close();
                            tc.socket.close();
                        }
                        catch(IOException ioE) {
                            // not much I can do
                        }
                    }
                }
                catch(Exception e) {
                    display("Exception closing the server and clients: " + e);
                }
            }
            // something went bad
            catch (IOException e) {
                String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
                display(msg);
            }
        }

        public void close(){
                keepGoing=false;
        }
    }
}



