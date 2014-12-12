package com.chukong.sdk.dns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPConnectionResponser {

	private final DatagramSocket socket;
	private final DatagramPacket inDataPacket;

	public UDPConnectionResponser(DatagramSocket socket,
			DatagramPacket inDataPacket) {
		super();
		this.socket = socket;
		this.inDataPacket = inDataPacket;
	}

	public DatagramPacket getInDataPacket() {
		return inDataPacket;
	}

	public void response(byte[] response) {

		try {

			if (response == null) {
				return;
			}
			DatagramPacket outdp = new DatagramPacket(response,
					response.length, inDataPacket.getAddress(),
					inDataPacket.getPort());

			outdp.setData(response);
			outdp.setLength(response.length);
			outdp.setAddress(inDataPacket.getAddress());
			outdp.setPort(inDataPacket.getPort());

			try {
				socket.send(outdp);
			} catch (IOException e) {

			    System.err.println("Error sending UDP response to "
						+ inDataPacket.getAddress() + ", " + e);
			}

		} catch (Throwable e) {

			System.err.println("Error processing UDP connection from " + inDataPacket.getSocketAddress() + ", " + e);
		}
	}
}
