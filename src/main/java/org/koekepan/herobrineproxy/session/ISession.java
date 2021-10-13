package org.koekepan.herobrineproxy.session;

import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;

import com.github.steveice10.packetlib.packet.Packet;

public interface ISession {
	public void setUsername(String username);
	public String getUsername();
	public String getHost();
	public int getPort();
	
	public boolean isConnected();
	public void disconnect();
	
	public void sendPacket(Packet packet);
	public void packetReceived(Packet packet);
	public void setPacketBehaviours(BehaviourHandler<Packet> behaviours);
}
