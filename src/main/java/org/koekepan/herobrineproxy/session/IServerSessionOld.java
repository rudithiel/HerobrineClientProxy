package org.koekepan.herobrineproxy.session;

import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;

import com.github.steveice10.packetlib.packet.Packet;

public interface IServerSessionOld extends ISession {
	public void setUsername(String username);
	public String getUsername();
	public String getHost();
	public int getPort();
	
	public void connect();
	public boolean isConnected();
	public void disconnect();
	
	public void sendPacket(Packet packet);

		
//	public boolean isMigrating();
//	public void setMigrating(boolean migrating);

//	public boolean hasJoined();
	public void setJoined(boolean joined);
	//public PacketSession getPacketSession();
	void setPacketBehaviours(BehaviourHandler<Packet> behaviours);
	void registerClientForChannels();
}
	
