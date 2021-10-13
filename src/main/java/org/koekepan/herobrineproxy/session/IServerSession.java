package org.koekepan.herobrineproxy.session;

public interface IServerSession extends ISession {
	public void connect();	
	public void setJoined(boolean joined);
	void registerClientForChannels();
	
//	public boolean isMigrating();
//	public void setMigrating(boolean migrating);

//	public boolean hasJoined();
	//public PacketSession getPacketSession();
}
	
