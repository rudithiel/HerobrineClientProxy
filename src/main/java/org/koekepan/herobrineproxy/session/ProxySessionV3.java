package org.koekepan.herobrineproxy.session;


import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.packet.behaviours.ClientSessionPacketBehaviours;
import org.koekepan.herobrineproxy.packet.behaviours.ServerSessionPacketBehaviours;

import com.github.steveice10.packetlib.packet.Packet;

public class ProxySessionV3 implements IProxySessionNew {

	IClientSession clientSession;
	IServerSession serverSession;
	IServerSession newServerSession;
	
	ClientSessionPacketBehaviours clientPacketBehaviours;
	ServerSessionPacketBehaviours serverPacketBehaviours;
	ServerSessionPacketBehaviours newServerPacketBehaviours;
	
	
	public ProxySessionV3(IClientSession clientSession, String serverHost, int serverPort) {
		this.clientSession = clientSession;
		this.clientPacketBehaviours = new ClientSessionPacketBehaviours(this);
		this.clientPacketBehaviours.registerDefaultBehaviours(clientSession);
		this.clientSession.setPacketBehaviours(clientPacketBehaviours);
		this.serverSession = new ServerSession(serverHost, serverPort);
	}
	
		
	@Override
	public String getUsername() {
		return clientSession.getUsername();
	}
	
	
	@Override
	public void setUsername(String username) {
		clientSession.setUsername(username);
		serverSession.setUsername(username);
	}
	

	@Override
	public void sendPacketToClient(Packet packet) {
		//ConsoleIO.println("ProxySessionV3::sendPacketToClient => Sending packet <"+packet.getClass().getSimpleName()+"> to client <"+clientSession.getHost()+":"+clientSession.getPort()+">");
		clientSession.sendPacket(packet);
	}

	
	@Override
	public void sendPacketToServer(Packet packet) {
		//ConsoleIO.println("ProxySessionV3::sendPacketToServer => Sending packet <"+packet.getClass().getSimpleName()+"> to server <"+serverSession.getHost()+":"+serverSession.getPort()+">");		
		serverSession.sendPacket(packet);
	}
	

	@Override
	public void setServerHost(String host) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public void setServerPort(int port) {
		// TODO Auto-generated method stub
	}


	@Override
	public void connect(String host, int port) {
		this.clientPacketBehaviours.registerForwardingBehaviour();
		this.serverPacketBehaviours = new ServerSessionPacketBehaviours(this, serverSession);
//		this.serverPacketBehaviours.clearBehaviours();
		this.serverPacketBehaviours.registerForwardingBehaviour();
//		this.serverPacketBehaviours.registerDefaultBehaviours();
		this.serverSession.setPacketBehaviours(serverPacketBehaviours);	
		serverSession.connect();
	}
	
	
	@Override
	public boolean isConnected() {
		return serverSession.isConnected();
	}
	
	
	@Override
	public void disconnect() {
		disconnectFromServer();
		disconnectFromClient();
	}
	
	
	@Override
	public void disconnectFromServer() {
		if (isConnected() ) {
			serverSession.disconnect();
		}
	}
	
	
	private void disconnectFromClient() {
		if (clientSession.isConnected()) {
			clientSession.disconnect();
		}
	}




	@Override
	public String getServerHost() {
		return serverSession.getHost();
	}


	@Override
	public int getServerPort() {
		return serverSession.getPort();
	}


	@Override
	public void migrate(String host, int port) {
		ConsoleIO.println("ProxySessionV3::migrate => Migrating player <"+getUsername()+"> to new server <"+host+":"+port+">");
		newServerSession = new ServerSession(getUsername(), host, port);
		this.newServerPacketBehaviours = new ServerSessionPacketBehaviours(this, newServerSession);
		this.newServerPacketBehaviours.registerMigrationBehaviour();
		this.newServerSession.setPacketBehaviours(newServerPacketBehaviours);	
		newServerSession.connect();
	}
	
	
	@Override
	public void switchServer() {
		serverSession = newServerSession;
		this.serverPacketBehaviours.clearBehaviours();
		this.serverPacketBehaviours = this.newServerPacketBehaviours;
		this.newServerPacketBehaviours = null;
	}
	
	
	@Override 
	public void setPacketForwardingBehaviour() {
		this.serverPacketBehaviours.registerForwardingBehaviour();
	}
	
	
	@Override 
	public void registerForPluginChannels() {
		this.serverSession.registerClientForChannels();
	}
}
