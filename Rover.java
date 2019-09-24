import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Rover  extends Thread{


    public static int roverID ; 
    public static Map route = new HashMap<String,List<String>>(); // A HashMap to store routing table
    public static ArrayList<Byte> array = new ArrayList<>(); // arraylist to build the array.
    public static String multiAddress;
    
    
    public static byte[] messageSend;
    public static byte[] message = new byte[124];
    
    
    static List<String> tableID = new ArrayList<>();
    static HashMap<Integer,List<Integer>> tableEntry = new HashMap<>();

    public static String multicastIP = "";
   
    public MulticastSocket msocket = null;

    private static boolean changeInTable = false;
    public boolean flg;
    static long sourceID;
    public static boolean[] currentEntries = new boolean[10]; // flip bits for entries in the table

    public static String fileName;
    public static String[] addrList = new String[]{"0","0","0","0","0","0","0","0","0","0","0"}; // store the address of roverID 
    public static int port ;// port to send and receive.

    
    public InetAddress group;  


  
    public static String DestinationIP;
  


    public Rover(){}

    public Rover(boolean flag){
        flg = flag; // value decides if a sender or receiver.


    }


    
    /**
     * It changes a string address into a String array.
     * @param address
     * @return
     */
    public static String[] splitAddress(String address){
        
        String[] s;
        if (address.contains("."))
            s = address.split("\\.");
        else{
            String[] s2 = new String[1];
            s2[0] = address;
            return s2;
        }
        return s;
    }

    
    
    /**
     * It creates the RIP data packet and stores it into a byte array, ready to send.
     */
    public static void create(){
        
        int count = 0;
        String[] s = splitAddress(tableID.get(0));
        String[] s2 = splitAddress(tableID.get(1));
        long i = 1;
        // this part adds the entry of the current rover itself as the
        // first entry in the RIP packet
        List<String> table;
        array.set(count++,(byte)2);
        array.set(count++,(byte)2);
        array.set(count++,(byte)route.size()); // to mention the number of entries the RIP packet.
        array.set(count++,(byte)0);

        array.set(count++, (byte) 0);
        array.set(count++, (byte) 2);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        // the destination address

        array.set(count++, (byte) Integer.parseInt(s[0]));
        array.set(count++, (byte) Integer.parseInt(s[1]));
        array.set(count++, (byte) Integer.parseInt(s[2]));
        array.set(count++, (byte) Integer.parseInt(s[3]));

        // subnet Mask
        array.set(count++, (byte) 255);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);

        // Next Hop IP address
        array.set(count++, (byte) Integer.parseInt(s2[0]));
        array.set(count++, (byte) Integer.parseInt(s2[1]));
        array.set(count++, (byte) Integer.parseInt(s2[2]));
        array.set(count++, (byte) Integer.parseInt(s2[3]));


        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);

        // Metric for the given entry will always be 0

        array.set(count++, (byte) Integer.parseInt(tableID.get(2)));
        // Loop through the routing table to get all entries and
        // put them in the RIP packet.
        while(i < 10) {
            if (i != roverID) {
                if (route.containsKey(i)) {

                    table = (List<String>) route.get(i);
                    s = splitAddress(table.get(0));

                    array.set(count++, (byte) 0);
                    array.set(count++, (byte) 2);
                    array.set(count++, (byte) 0);
                    array.set(count++, (byte) 0);

                    array.set(count++, (byte) Integer.parseInt(s[0]));
                    array.set(count++, (byte) Integer.parseInt(s[1]));
                    array.set(count++, (byte) Integer.parseInt(s[2]));
                    array.set(count++, (byte) Integer.parseInt(s[3]));

                    array.set(count++, (byte) 240);
                    array.set(count++, (byte) 240);
                    array.set(count++, (byte) 240);
                    array.set(count++, (byte) 240);

                    s = splitAddress(table.get(1));

                    array.set(count++, (byte) Integer.parseInt(s[0]));
                    array.set(count++, (byte) Integer.parseInt(s[1]));
                    array.set(count++, (byte) Integer.parseInt(s[2]));
                    array.set(count++, (byte) Integer.parseInt(s[3]));

                    array.set(count++, (byte) 0);
                    array.set(count++, (byte) 0);
                    array.set(count++, (byte) 0);
                    array.set(count++, (byte) Integer.parseInt(table.get(2)));

                    i++;
                } else
                    i++;
            }else{
                i ++;
            }
        }


    }

    
/*
    public void initialize(){
     int count = 0;
        String[] s = splitAddress(tableID.get(0));
        String[] s2 = splitAddress(tableID.get(1));
        long i = 1;
        // this part adds the entry of the current rover itself as the
        // first entry in the RIP packet
        List<String> table;
        array.set(count++,(byte)2);
        array.set(count++,(byte)2);
        array.set(count++,(byte)route.size()); // to mention the number of entries the RIP packet.
        array.set(count++,(byte)0);

        array.set(count++, (byte) 0);
        array.set(count++, (byte) 2);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        // the destination address

        array.set(count++, (byte) Integer.parseInt(s[0]));
        array.set(count++, (byte) Integer.parseInt(s[1]));
        array.set(count++, (byte) Integer.parseInt(s[2]));
        array.set(count++, (byte) Integer.parseInt(s[3]));

        // subnet Mask
        array.set(count++, (byte) 255);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);

        // Next Hop IP address
        array.set(count++, (byte) Integer.parseInt(s2[0]));
        array.set(count++, (byte) Integer.parseInt(s2[1]));
        array.set(count++, (byte) Integer.parseInt(s2[2]));
        array.set(count++, (byte) Integer.parseInt(s2[3]));


        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) 0);
        array.set(count++, (byte) Integer.parseInt(tableID.get(2)));
        }
 */


    /**
     * A function to convert binary numbers to decimal numbers.
     * 
     * @param numOfBytes
     * @param start
     * @param content
     * @return
     */
    public static long binaryToDecimal(int numOfBytes,int start,byte[] content){
        int  j = 15;
        long total=0;
        if (numOfBytes == 2) {
            // for converting 2 bytes into decimal
            for (int i = 7; i >= 0; i--) {
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;

            }
            start += 1;
            for (int i = 7; i >= 0; i--) {
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;
            }
            return total;
        }
        else if (numOfBytes == 0){
            j = 3;
            for (int i = 7;i >=4; i--){
                if ((content[start] &(1 << i)) != 0){
                    total += Math.pow(2,j);
                    j -= 1;
                }
            }
            return total;
        }
        else if (numOfBytes == 1){
            // for converting half a byte into decimal
            // for converting 1 byte into decimal
            j= 7;
            for (int i = 7; i >= 0; i--) {
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;
            }
            return total;
         
        }
        else {
            // for converting 4 bytes into decimal
            j = 31;
            for (int i = 7; i>=0; i--){
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;
            }
            start += 1;
            for (int i = 7; i>=0; i--){
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;
            }
            start += 1;
            for (int i = 7; i>=0; i--){
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;
            }
            start += 1;
            for (int i = 7; i>=0; i--){
                if ((content[start] & (1 << i)) != 0)
                    total += Math.pow(2, j);
                j -= 1;
            }
            return total;
        }
    }

    



    /**
     * It deletes the entries in the table which are no longer reachable
     * or available after a certain amount of time.
     */
    public static void delete(){
        
        for(int i = 0; i < 10;i++){
           
            if(tableEntry.containsKey(i)){
                
                for( int j = 0 ;j < 10;j ++){
                    
                    if (route.containsKey(i)){
                        
                        if (!tableEntry.containsKey((long)i)){
                            route.remove(i);
                            changeInTable = true;
                        
                        } 
                    }
                }
            }
        }
    }


    



    /**
     * 
     * Checks the table for any changes or new developments.
     * 
     * @throws Exception
     */
    public static void checktable() throws Exception {
        for (int i =1;i < currentEntries.length;i++){
            
            if (!addrList[i].equals(InetAddress.getLocalHost().getHostAddress()))
                
                if (!currentEntries[i]){
                    
                    if (route.containsKey((long)i)) {
                        
                        List<String> table = (List<String>) route.get((long) i);
                        
                        if (table.get(2).equals("1"))
                        
                            route.remove((long) i);
                        
                        changeInTable = true;
                  
                    }
                
                }
            
            currentEntries[i] = false;
        
        }
    }






    /**
     * This function creates and maintains the routing table with new entries and old entries.
     * 
     * @param id
     * @param address
     * @param cost
     * @param packetSource
     * @param dest
     */
    public static void routingTable(long id,String address, String cost,InetAddress packetSource, String dest){
        
        List<Integer> destintn = new ArrayList<>();
        List<String> table = new ArrayList<>();
        List<String> table1 = new ArrayList<>();
        
        // this sets the cost to 1 if it is 0
        // also initialies the sourceID for later use.
        
        
        if(!addrList[roverID].equals(dest)) {
            if (cost.equals("0")) {
                cost = "1";
                currentEntries[(int) id] = true;
                // to remember the id of the current packet sender
                sourceID = id;
                if (addrList[(int) id].equals("0"))
                    addrList[(int) id] = packetSource.toString().substring(1);
            }

            if (route.containsKey(id)) {
                table = (List<String>) route.get(id);
                // to see if the current entry is about the source id itself
                if (packetSource.toString().substring(1).equals(addrList[(int) id])) {
                    if (!cost.equals(table.get(2))) {
                        changeInTable = true;
                        table.set(0, address);
                        table.set(1, String.valueOf(packetSource).substring(1));
                        table.set(2, cost);
                        route.put(id, table);
                        destintn.add((int)sourceID);
                        tableEntry.put((int)sourceID,destintn);
                    }
                }

                else {
                    // to get the cost between this rover and the rover that has sent this packet from the routing table
                    table1 = (List<String>) route.get(sourceID);
                    // to check whether the newCost from this rover is less than the cost present in the routing table
                    String newCost = String.valueOf(Integer.parseInt(table1.get(2)) + Integer.parseInt(cost));
                    if (Integer.parseInt(table.get(2)) > Integer.parseInt(newCost)) {
                        changeInTable = true;
                        table.set(0, address);
                        table.set(1, String.valueOf(packetSource).substring(1));
                        table.set(2, cost);
                        route.put(id, table);
                        destintn.add((int)id);
                        tableEntry.put((int)sourceID,destintn);

                    }
                }
            }

            else {
                // to check if the entry is not for the source and if it has not been previously visited
                if ((!packetSource.toString().substring(1).equals(addrList[(int) id])) && !currentEntries[(int) id]) {
                    changeInTable = true;
                    table = (List<String>) route.get(sourceID);
                    String new_cost = String.valueOf(Integer.parseInt(table.get(2)) + Integer.parseInt(cost));
                    table1.add(0, address);
                    table1.add(1, String.valueOf(packetSource).substring(1));
                    table1.add(2, new_cost);
                    route.put(id, table1);
                    destintn.add((int)id);
                    tableEntry.put((int)sourceID,destintn);
                }
                // if the entry is for the source then just add
                else if (packetSource.toString().substring(1).equals(addrList[(int) id])) {
                    changeInTable = true;
                    table.add(0, address);
                    table.add(1, String.valueOf(packetSource).substring(1));
                    table.add(2, cost);
                    route.put(id, table);
                    destintn.add((int)id);
                    tableEntry.put((int)sourceID,destintn);
                }
            }
        }

    }



    /**
     * This method receives the message or sends the message depending
     * on the parameters provided and whether the value of flg is true or false.
     * 
     */
    public void run(){
        try {

            msocket = new MulticastSocket(port);
            group = InetAddress.getByName(multiAddress);
            msocket.joinGroup(group);
            if (flg) {
                while(true) {
                    DatagramPacket packet = new DatagramPacket(message, message.length);
                    msocket.receive(packet);
                    // If the IP address is same as the localhost then entry into the table is avoided
                    // this will avoid any loop formations in the route.

                    if(!InetAddress.getLocalHost().getHostAddress().equals(packet.getAddress().toString().substring(1))) {
                        InetAddress packetSource = packet.getAddress();
                        decode(packetSource);

                    }
                }

            }
            else{
                // create a new byte array that is of the length of
                // the updated values of the routing table
                createbyteArray(route.size());

                // create the data that is to be sent

                create();
                printRoutingTable();

                messageSend = new byte[array.size()];

                for (int i = 0; i < array.size(); i++) {
                    messageSend[i] = array.get(i);
                }

                System.out.println();
                group = InetAddress.getByName(multiAddress);
                DatagramPacket packet = new DatagramPacket(messageSend, messageSend.length, group, port);
                msocket.send(packet);





            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * This function understands or decodes the byte array received and makes it 
     * readable for us and these values are then used to populate the routing table.
     * 
     * @param packetSource
     */
    public void decode( InetAddress packetSource){
        
      
        int count = 0;
        count += 2;
      
        int numOfEntries;
        numOfEntries = (int)binaryToDecimal(1,count,message);
        count++;
        count++;
        String dest_address = "";
        // this loop is to
        // calculate all the values of the packet entries
        for (int i = 1; i<=numOfEntries;i++) {
            count += 4;
            String address = "";
            // Getting the Multicast Address of the destination
            
            address += binaryToDecimal(1, count,message) + ".";
            count++;
            
            address += binaryToDecimal(1, count,message) + ".";
            // determining the id of the destination rover.
            
            long id = binaryToDecimal(1, count,message);
            count++;
            
            address += binaryToDecimal(1, count,message) + ".";
            count++;
            
            address += String.valueOf(binaryToDecimal(1, count,message));
                count += 5;
            // determining the destination rover of the received entry.
            
            dest_address += binaryToDecimal(1, count,message) + ".";
            count++;
            
            dest_address += binaryToDecimal(1, count,message) + ".";
            count++;

            dest_address += binaryToDecimal(1, count,message) + ".";
            count++;

            dest_address += String.valueOf(binaryToDecimal(1, count,message));
                count += 4;
            
            // get the cost/metric of the path.
            
            String cost = String.valueOf(binaryToDecimal(1, count,message));
            count++;// method to create the routing table from the values.
            
            routingTable(id, address, cost, packetSource, dest_address);
        }
    }

    
    
    
    /**
     * This creates a byte array from the the routing table.
     * 
     * @param numOfEntries
     */
    public static void createbyteArray(int numOfEntries){
        
        // initialize the new size of the routing table
        int newsize = 4 + (20 * numOfEntries);
        
        // check if the current size is less than the new size
        // if yes then add the difference in size to the byte array list
        
        if (array.size() < newsize){
            int addExtra = newsize - array.size();
            for(int i =0;i < addExtra;i++){
                array.add((byte)0);
            }
        }
    }


    
    
    
    
    /**
     * This function prints the routing table.
     */
    public static void printRoutingTable(){

        // If the routing table was updated only then
        // the table gets printed and the 
        // value of the flag is reset to false.
        
        
        if (changeInTable){
         //   System.out.println("Address\t\tNext Hop\t\tCost");
            System.out.println("\n-------------------- Routing Table --------------------\n");
            for(int i = 1;i <= 10;i++) {
                if (route.containsKey((long)i)) {
                    List<String> table = (List<String>) route.get((long) i);
                        System.out.println("Neighbour: "+ i);
                        System.out.println("Address: " + table.get(0) + "\n" + "Next Hop: " + table.get(1) + "\n" + "Cost: " + table.get(2) + "\n");

                }
            }
            changeInTable = false;
        }

    }

    public static void main(String[] args) throws Exception {


        roverID = Integer.parseInt(args[2]);
        addrList[roverID] = InetAddress.getLocalHost().getHostAddress();

        multicastIP = "10." + roverID + ".0." + "0";
        tableID.add("10." + roverID + ".0." + "0");


        multiAddress = args[0];
        port = Integer.parseInt(args[1]);


        for (int i = 0;i < 24;i ++){
            array.add((byte)0);
        }


        InetAddress addr = InetAddress.getLocalHost();
        tableID.add(String.valueOf(addr.getHostAddress()));
        tableID.add("0");



        route.put((long)roverID,tableID);
        changeInTable = true;
        printRoutingTable();



        


        int check = 0;

        Rover r = new Rover(true);
        Rover s = new Rover(false);
        r.start();
        
        
        ProcessData receiveObj = new ProcessData();
        receiveObj.start();

        sleep(4000);


        if(args.length > 3) {
            DestinationIP = args[3];
            fileName = args[4];
            PrepareData sendObj = new PrepareData(true,DestinationIP);
            sendObj.start();
        }
        
        // Using sleep as a timer for  sending and checking of messages.
        
        while (true){

            sleep(5000);
            checktable();
            delete();


            check = 0;
            if (check == 0 ){
                check =1;
            }
            if (check == 1){
                s.run();

            }
        }



    }
}
