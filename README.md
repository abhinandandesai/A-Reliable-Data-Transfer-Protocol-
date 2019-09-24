# A-Reliable-Data-Transfer-Protocol- #

## The Protocol ## 
    There are 3 concepts it will follow:
* **Sequence Numbers:** There will be an in-order increasing sequence number with each packet that is sent over the network. 
                         This will keep a check that the packets are sent in order. If the receiver receives an out of order sequence                              number packet then it will be dropped.

*	**Acknowledgement:** Whenever a receiver will receive a packet, it will check the sequence number and accordingly send an ACK back to the Sender. It will not send an ACK if the sequence number is out of order for the received packet.

* **Timer:** A timer is implemented. The timer will reset if, after sending a packet, it receives an ACK for it from the receiver. 
             If the timer runs out and no ACK is received for the packet then it will retransmit the packet. 

This protocol will ensure that all the fragments of the file are received and stored by the receiver and also ensures that they will be received in-order with retransmissions for the lost packets.

      There are 3 JAVA files present:

* **Rover.java:** The sender rover will have 5 arguments and others will have 3 arguments.
* **PrepareData.java:** It will prepare the file by converting the data into bytes and then according to the MTU will send the file to                             receiver in packets or fragments.
* **ProcessData.java:** This will handle the packets that will be received. It will also send ACKs whenever the packets are received in                           order. It will also print the data into a text file ready for display.
