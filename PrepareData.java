import javax.print.attribute.standard.Destination;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


class PrepareData extends Rover{
    
    
    private static byte[] bytePacket = new byte[956];// for sending the header and data in packet
    
    private static List<Byte> header = new ArrayList<>();// for updating the header
   
    private static DatagramPacket packet;

    private static String destinationAddress;
    private static long destID = 0; // rover id of the destination.
    private static int port = 2112;
    private static  byte[] readFile = new byte[946] ;// to be made 946 for reading file
    private static boolean setFINbit = false;
   
    
    private static int  sequenceNumber = 1;

    private static boolean toSend = true;
    private static byte[] ackHeader = new byte[12];
    private static DatagramSocket socket;

    /*  private static boolean toSend = true;
        private static byte[] ackHeader = new byte[12];
        private static DatagramSocket socket;
        */
    private static boolean calcRoute = false; // made true in calcRoute if a route to dest exists.

    /**
     * A constructor to initialize values and flags.
     *
     * @param flag
     * @param destination
     */
    public PrepareData(boolean flag,String destination) {
        
        toSend = flag;
        destinationAddress = destination;
        
        //System.out.println("toSend: " + toSend);
        if(toSend)
            destID = createAHeader(destination,toSend);

    }




    /**
     * It updates the FIN bit and the sequence number in the header for each received packet.
     *
     */
    public static void updateFINbitAndSeqNumber() {
        // the entire header is of 54 bytes including the IP and UDP header
        if (setFINbit) {

            // Last bit set to 1
            header.add((byte) 1);
        } else{

            // Last bit set to 0
            header.add((byte) 0);
        }
        // set the required sequence number
        header.add((byte)sequenceNumber);
       
        for(int i = 0;i < header.size();i++){
            bytePacket[i] = header.get(i);
        }
        header.clear();
    }





    /**
     * It checks the routing table to find a route to the destination and
     * returns the next hop of the rover ID which is the destination.
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
     * It forwards the received packet to the next destination.
     *
     * @param data
     * @param address
     */
    public static void forwardPacket(byte[] data,InetAddress address){
        
        int i = 0, index = 10;
        if (sequenceNumber == 8){
            data = data;
        }
        for(i = 0; i < data.length; i++)
        {
            try {
                bytePacket[index] = data[i];
                index++;
            }catch (Exception e){
                System.err.println("Error in size of packet or file length");
            }
        }
        
        packet = new DatagramPacket(bytePacket,bytePacket.length,address,2012);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    /**
     * It creates or prepares the source and the destination address in the header.
     *
     * @param Destination
     * @param toSend
     * @return
     */
    public static long createAHeader(String Destination, boolean toSend){

        String[] dest,src;
        dest  = splitAddress(Destination);
        String source = multicastIP;

        src = splitAddress(source);

        destID = Long.parseLong(dest[1]);

        int index = 0;
        /*
        while(index < dest.length){
            header.add((byte)Integer.parseInt(dest[i]));
            index++;
        }
        i = 0;
        if(toSend) {
            while(index < dest.length) {
                header.add((byte) Integer.parseInt(src[i]));
                index++;
            }
        }
        */

        for (int i = 0;i < dest.length;i++){
            header.add((byte)Integer.parseInt(dest[i]));
        }
        if(toSend) {
            for (int i = 0; i < dest.length; i++) {
                header.add((byte) Integer.parseInt(src[i]));
            }
        }
        return Long.parseLong(dest[1]);
    }
    
    
    
    
   
    
    
    
    public void run(){
        try {
            if (toSend) {

                File file = new File(fileName);
                InetAddress address;
                FileInputStream in = new FileInputStream(file);


                long FileLength = file.length();
                long totalPackets = (FileLength/946) + 1;
                long lastPacketSize = (FileLength - (946 * (totalPackets - 1))) + 10;
                int next;

                socket = new DatagramSocket(port);


                String add = "";

                while ((next = in.read(readFile)) != -1) {
                    // keep scanning routing table till route is found.

                    while( !calcRoute) {
                        add = calcRoute(destID); // find the route to the destination
                    }

                    calcRoute = false;
                    // get next hop IP
                    address = InetAddress.getByName(add);
                    // update header of custom protocol
                    updateFINbitAndSeqNumber();
                    boolean timeout = true;

                    System.out.println(sequenceNumber + " is the Sequence number");
                    System.out.println("Sent address: " + address);
                    forwardPacket(readFile, address); // data packet

                    // timer for receiving ACK
                    socket.setSoTimeout(2500);
                    packet = new DatagramPacket(ackHeader, ackHeader.length); // change the size of packet for the last packet.

                    //System.out.println(packet);
                    if ( sequenceNumber == totalPackets - 1) {
                        bytePacket = new byte[(int) lastPacketSize];
                        readFile = new byte[(int)lastPacketSize - 10];
                        setFINbit = true;
                    }

                    // wait for ACK

                    while(timeout) {

                        try {
                            socket.receive(packet); // waiting for ACK
                            System.out.println("ACK received: " + packet.getAddress());
                            timeout = false;

                            bytePacket = new byte[956];

                            createAHeader(destinationAddress,toSend);
                        } catch (SocketTimeoutException e) {
                            System.out.println("Timeout... Packet is retransmitting.");
                            timeout = true;

                        }
                        // resend the previous packet if ACK not received.

                        if (timeout) {
                            add = calcRoute(destID);
                            System.out.println("Sending to: " + add);
                            address = InetAddress.getByName(add);// get next hop IP
                            forwardPacket(readFile, address);

                        }
                    }

                    // only increment if ACK received, else send previous message again.
                    sequenceNumber += 1;


                }


                bytePacket = new byte[10];     // ACK for the FIN bit ACK from the receiver
                                              // This closes the connection b/w sender and receiver.
                header.clear();
                createAHeader(DestinationIP,true);
                updateFINbitAndSeqNumber();

                while( !calcRoute) {
                    add = calcRoute(destID); // find the route to the destination
                }

                calcRoute = false;

                address = InetAddress.getByName(add);
                DatagramPacket packet = new DatagramPacket(bytePacket,bytePacket.length,address,2103);
                socket.send(packet);

                System.out.println("ACK for FIN bit sent...");
                socket.close();

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}