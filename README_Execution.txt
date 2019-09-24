Name: Abhinandan Desai

A Reliable Data Transfer Protocol - README

-------------------------------------------------------------------------

There are 3 java files present.

1) Rover.java
2) PrepareData.java
3) ProcessData.java

Only Rover.java needs to be executed to run the project.

Rover.java will act as a sender and also as a receiver.
It will also function as a normal rover.

Rover as a Sender:
	There will be 5 arguments in the following order:
		1) Multicast IP Address (e.g. 225.0.0.1)
		2) RIP port - 520
		3) Rover ID (e.g. 1 )
		4) Destination IP Address ( e.g. 108.86.68.29)
		5) Name of the file that will be sent (e.g. test.txt)

A normal Rover will have 3 arguments in the following order:
		1) Multicast IP Address (e.g. 225.0.0.1)
		2) RIP port - 520
		3) Rover ID (e.g. 1 )

A Rover which will act as a receiver is the same as a normal Rover:
It will also have 3 arguments in the following order:		
		1) Multicast IP Address (e.g. 225.0.0.1)
		2) RIP port - 520
		3) Rover ID (e.g. 1 )


The destination ID will be "10.X.0.0" where X will be 1,2,3 according
to the rover ID.

Next Hop saves the actual IP address.

Split horizon has been implemented by ignoring any entry in a
packet which has it's next hop equal to the receiving rover.



	 