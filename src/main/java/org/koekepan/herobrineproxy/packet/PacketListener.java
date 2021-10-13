package org.koekepan.herobrineproxy.packet;

import com.github.steveice10.packetlib.packet.Packet;

public interface PacketListener {
	public void packetReceived(Packet packet);
	public void sendPacket(Packet packet);
	//public void packetSent(Packet packet);
}
