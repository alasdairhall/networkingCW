/*
 *  (c) K.Bryson, Dept. of Computer Science, UCL (2016)
 *  
 *  YOU MAY MODIFY THIS CLASS TO IMPLEMENT Stop & Wait ARQ PROTOCOL.
 *  (You will submit this class to Moodle.)
 *  
 */

package physical_network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Encapsulates the data for a network 'data frame'.
 * At the moment this just includes a payload byte array.
 * This may need to be extended to include necessary header information.
 * 
 * @author kevin-b
 *
 */

public class DataFrame {
	
	public final byte[] payload;
	private int destination = 0;
	private int source;
	private int ack = 0;
	private int sequence = 0;
		
	public DataFrame(String payload) {
		this.payload = payload.getBytes();
	}
	
	public DataFrame(String payload, int destination) {
		this.payload = payload.getBytes();
		this.destination = destination;
	}

	public DataFrame(byte[] payload) {
		this.payload = payload;
	}
	
	public DataFrame(byte[] payload, int destination) {
		this.payload = payload;
		this.destination = destination;
	}
	
	public int getDestination() {
		return destination;
	}

	public byte[] getPayload() {
		return payload;
	}

	public String toString() {
		return new String(payload);		
	}

	/*
	 * A factory method that can be used to create a data frame
	 * from an array of bytes that have been received.
	 */
	public static DataFrame createFromReceivedBytes(byte[] byteArray) {
		int dest = byteArray[0];
		int source = byteArray[1];
		// CheckSum bytes 2 and 3
		int ack = byteArray[4];
		int seq = byteArray[5];
		byte[] payload = Arrays.copyOfRange(byteArray, 6, byteArray.length);

		DataFrame created = new DataFrame(payload, dest);
		created.setSource(source);
		created.setSequence(seq);
		if(ack == 1) created.makeAcknowledgement();
		
		return created;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getSource() {
		return this.source;
	}

	public void makeAcknowledgement() {
		this.ack = 1;
	}

	public boolean isAcknowledgement() {
		return this.ack == 1;
	}

	private byte[] getHeader() {
		byte[] tempHead = {
				(byte) (destination & 0xFF),
				(byte) (source & 0xFF),
				(byte) (ack & 0xFF),
				(byte) (sequence & 0xFF)
		};

		byte[] temp = new byte[tempHead.length + payload.length];

		System.arraycopy(tempHead, 0, temp, 0, tempHead.length);
		System.arraycopy(payload, 0, temp, tempHead.length, payload.length);

		byte[] checksum = calcChecksum(temp);

		byte[] header = {
				(byte) (destination & 0xFF),
				(byte) (source & 0xFF),
				checksum[0],
				checksum[1],
				(byte) (ack & 0xFF),
				(byte) (sequence & 0xFF)
		};

		return header;
	}

	public static byte[] calcChecksum(byte[] data) {
		int checksum = 0;
		int length = data.length;
		int i = 0;

		while(length > 1) {
			int word = ((data[i] << 8) | (data[i+1] & 0xFF));
			checksum += word;

			length -= 2;
			i += 2;

			// Check for carry
			if((checksum & 0xFFFF0000) > 0) {
				checksum = checksum & 0xFFFF;
				checksum += 1;
			}
		}

		// In the case there is an odd number of bytes
		if(length == 1) {
			int word = ((data[i] << 8) & 0xFF00);
			checksum += word;
			if((checksum & 0xFFFF0000) > 0) {
				checksum = checksum & 0xFFFF;
				checksum += 1;
			}
		}
		// Flip every bit
		checksum = ~checksum;
		checksum = checksum & 0xFFFF;

		byte[] cs = new byte[2];

		cs[0] = (byte)((checksum >> 8) & 0x00FF);
		cs[1] = (byte) (checksum & 0x00FF);

		return cs;
	}

	public static boolean confirmChecksum(byte[] cs) {
		int check = ((cs[0] << 8) | cs[1]);
		return check == 0;
	}

	/*
         * This method should return the byte sequence of the transmitted bytes.
         * At the moment it is just the payload data ... but extensions should
         * include needed header information for the data frame.
         * Note that this does not need sentinel or byte stuffing
         * to be implemented since this is carried out as the data
         * frame is transmitted and received.
         */
	public byte[] getTransmittedBytes() {
		byte[] header = getHeader();
		int length = payload.length + header.length;
		byte[] transmittedBytes = new byte[length];

		System.arraycopy(header, 0, transmittedBytes, 0, header.length);
		System.arraycopy(payload, 0, transmittedBytes, header.length, payload.length);

		return transmittedBytes;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getSequence() {
		return this.sequence;
	}
}