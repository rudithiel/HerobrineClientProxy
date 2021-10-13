package org.koekepan.herobrineproxy.packet;

import org.koekepan.herobrineproxy.sps.ISPSConnection;
import org.koekepan.herobrineproxy.sps.SPSPacket;

import com.github.steveice10.packetlib.packet.Packet;

public class SPSPacketSession implements IPacketSession {
	
	private String username;
	private String channel;
	
	private ISPSConnection session;
	
	public SPSPacketSession(ISPSConnection session) {
		this.session = session;
	}
		
	
	public SPSPacketSession(ISPSConnection client, String username, String channel) {
		this.session = client;
		this.username = username;
		this.channel = channel;
	}
	
	@Override
	public void send(Packet packet) {
		SPSPacket spsPacket = new SPSPacket(packet, username, channel);
		session.publish(spsPacket);
	}
	
	
	public void setUsername(String username) {
		this.username = username;
	}


	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public ISPSConnection getSession() {
		return this.session;
	}

}
