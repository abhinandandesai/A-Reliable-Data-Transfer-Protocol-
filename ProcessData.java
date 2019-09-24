import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessData extends Rover {


    private static DatagramSocket socket;
    private static DatagramPacket packet;
    
    
    private static long destID;
    private static long receivedSeq = 0;
    private static long FINbit;


    private static byte[] bytePacket = new byte[958];// for sending the header and data in packet
    private static List<Byte> header = new ArrayList<>();// for updating the header
    
    
   
    private static File file = new File("Result.txt");

    private static int port = 2103;




    /**
     * It forwards the received packet to the next hop of the current rover according to
     * the table entries
     *
     * @param destinationAddress
     * @throws IOException
     */
    public static void forwardPacket(String destinationAddress) throws IOException {

        InetAddress destination = InetAddress.getByName(destinationAddress);// change to destinationAddress for final
        DatagramPacket newPacket = new DatagramPacket(bytePacket,bytePacket.length,destination,port);
        socket.send(newPacket);
        System.out.println("New Packet created and moved towards the Next Hop");


    }


    /**
     * It goes through the routing table and finds the destination rover and
     * returns the next hop of the destination in the routing table.
     *
     * @param ID
     * @return
     */
    public static String calcRoute(long ID){

        List<String> values ;
        String address = "";

        if (route.containsKey(ID)){
            values = (List)route.get(ID);

            address = values.get(1);
        }
        return address;

    }


    /**
     * It creates the Acknowledgement header
     *
     * @param destination
     * @param source
     * @return
     */
    public static byte[] createAcKHeader(String destination, String source){
        try {
            String[] dest, src;

            byte[] ackHead = new byte[10];

            dest = splitAddress(destination);
            src = splitAddress(source);


            destID = Long.parseLong(dest[1]);

            // add source and destination address

            for (int i = 0; i < dest.length; i++) {
                header.add((byte) Integer.parseInt(dest[i]));
            }
            for (int i = 0; i < dest.length; i++) {
                header.add((byte) Integer.parseInt(src[i]));
            }
            int i;
            for (i = 0; i < header.size(); i++) {
                ackHead[i] = header.get(i);
            }

            header.clear();
            // set sequence and last bit

            ackHead[i++] = (byte) 0;// last BIT
            ackHead[i++] = (byte) receivedSeq;

            return ackHead;
        }catch (Exception e){
            
            System.out.println("Error");
            byte[] ack = new byte[1];
            ack[0] = 1;
            return ack;
        }

    }


    /**
     * It parses through the received packet and makes the byte array received
     * more readable and ready for the routing table entries.
     *
     * @param data
     * @throws Exception
     */
    public static void decode(byte[] data) throws Exception {

        int index = 0;
        long id,sourceid;
        long sequence;
        
        String address = ""; 
        String sourceAddr = "";
        String newDestinationAddress = "";


        address += binaryToDecimal(1,index, data)+ ".";
        // finding the rover id of the destination
        index ++;

        id = binaryToDecimal(1, index, data);

        address += binaryToDecimal(1, index, data) + ".";
        index++;

        address += binaryToDecimal(1, index, data) + ".";
        index ++;

        address += String.valueOf(binaryToDecimal(1, index, data));
        index++;
        // find the source Id of the packet.

        sourceAddr += binaryToDecimal(1, index, data) + ".";
        index++;

        sourceAddr += binaryToDecimal(1, index, data) + ".";
        sourceid = binaryToDecimal(1, index, data);
        index ++;

        sourceAddr += binaryToDecimal(1, index, data) + ".";
        index++;

        sourceAddr += String.valueOf(binaryToDecimal(1, index, data));
        index++;
        //System.out.println(sourceAddr);

        FINbit = binaryToDecimal(1, index, data);
        index++;

        sequence = binaryToDecimal(1, index, data);
        // read the file data if destination Id and rover id match
        if(id == roverID){
            if(sequence == receivedSeq + 1) {
                printFile(data);
                receivedSeq = sequence;
            }
            // send ACK to the source
            sendAck(sourceid, sourceAddr, address);
        }
        // forward the packet to the next hop if rover ID does not match
        else{
            // find route to the destination.
            newDestinationAddress = calcRoute(id);
            forwardPacket(newDestinationAddress);
        }


    }





    /**
     *
     * It sends the ACK for the packet it received if the current rover is the
     * destination rover.
     *
     * @param src_id
     * @param destination
     * @param source
     * @throws Exception
     */
    public static void sendAck(long src_id, String destination, String source) throws Exception {

        byte[] ackHead = createAcKHeader(destination, source);// source and destination flipped for sending ack
        String address = calcRoute(src_id);
        //System.out.println(address);
        InetAddress dest = InetAddress.getByName(address);// address of source from where data came
        DatagramPacket newPacket = new DatagramPacket(ackHead, ackHead.length, dest,2112);
        socket.send(newPacket);

    }






    public void run (){
        try {
            socket = new DatagramSocket(port);
            while(true) {
                // continuously receive packets.
                packet = new DatagramPacket(bytePacket, bytePacket.length);
                if (FINbit == 2){
                    socket = new DatagramSocket(port);
                    FINbit = 0;
                }
                if(FINbit == 0) {
                    socket.receive(packet);
                    bytePacket = new byte[956];
                    bytePacket = packet.getData();
                    decode(bytePacket);
                }
                // if FIN BIT is Set and receive last ACK within the set timer, then reset seq number
                else if (FINbit == 1){
                    try{
                        socket.setSoTimeout(3000);
                        socket.receive(packet);
                        
                        System.out.println("Data Transferred");
                        System.out.println("Reset receiver");
                        
                        socket.close();
                        
                        FINbit = 2;
                    }catch (Exception e){
                        System.out.println("Data Transferred ");
                        System.out.println("Reset receiver");
                        socket.close();
                        FINbit = 2;
                    }
                    receivedSeq = 0;
                }



            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * It reads the file content and prints it
     *
     * @param data
     */
    public static void printFile(byte[] data){
        int i = 0,index=10;

        byte[] fileData = new byte[data.length - 10];

        for(i = 0; i < fileData.length; i++) {
            fileData[i] = data[index];
            index++;
        }
        try {
            FileOutputStream out = new FileOutputStream(file,true);
            out.write(fileData);

        } catch (Exception e) {
            e.printStackTrace();
        }

        String print = new String(fileData);
        System.out.print(print);



    }

}