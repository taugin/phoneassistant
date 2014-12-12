package com.chukong.sdk.dns;

import java.net.DatagramPacket;

public class UDPConnectionWorker implements Runnable {

	private final UDPConnectionResponser responser;
	private final DatagramPacket inDataPacket;

	private QueryProcesser queryProcesser;

	public UDPConnectionWorker(DatagramPacket inDataPacket,
			QueryProcesser queryProcesser, UDPConnectionResponser responser) {
		super();
		this.responser = responser;
		this.inDataPacket = inDataPacket;
		this.queryProcesser = queryProcesser;
	}

	public void run() {

		try {

            RequestContextProcessor.processRequest(inDataPacket);
			byte[] response = queryProcesser.process(inDataPacket.getData());
			if (response != null) {
				responser.response(response);
			}
		} catch (Throwable e) {

			System.err.println(
					" UDPConnectionWorker Error processing UDP connection from "
							+ inDataPacket.getSocketAddress() + ", " + e);
		}
	}
}
