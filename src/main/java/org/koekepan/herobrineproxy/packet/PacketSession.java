package org.koekepan.herobrineproxy.packet;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;

public class PacketSession implements IPacketSession {
	
	public Session session;
	
	public PacketSession(Session client) {
		this.session = client;
	}
	
	
	public void send(Packet packet) {
		session.send(packet);
	}
}
