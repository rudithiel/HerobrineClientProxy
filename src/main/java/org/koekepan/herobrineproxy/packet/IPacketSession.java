package org.koekepan.herobrineproxy.packet;

import com.github.steveice10.packetlib.packet.Packet;

public interface IPacketSession {
	public void send(Packet packet);
}
