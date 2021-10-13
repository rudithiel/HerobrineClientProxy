package org.koekepan.herobrineproxy.session;

import com.github.steveice10.packetlib.packet.Packet;

public interface IProxySessionNew {
	
	public String getUsername();
	public String getServerHost();
	public int getServerPort();
	
	public void setUsername(String username);
	public void setServerHost(String host);
	public void setServerPort(int port);

	public void connect(String host, int port);
	public boolean isConnected();
	public void disconnect();
	public void disconnectFromServer();
	
	public void sendPacketToClient(Packet packet);
	public void sendPacketToServer(Packet packet);
	
	public void migrate(String host, int port);
	public void switchServer();
	void setPacketForwardingBehaviour();
	void registerForPluginChannels();
}
