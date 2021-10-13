package org.koekepan.herobrineproxy.packet.behaviours.server;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.packet.Packet;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IProxySessionNew;
import org.koekepan.herobrineproxy.session.IServerSession;

public class MigrateJoinGamePacketBehaviour implements Behaviour<Packet> {
		
	private IProxySessionNew proxySession;
	private IServerSession serverSession;
	
	@SuppressWarnings("unused")
	private MigrateJoinGamePacketBehaviour() {}
	
	
	public MigrateJoinGamePacketBehaviour(IProxySessionNew proxySession, IServerSession serverSession) {
		this.proxySession = proxySession;
		this.serverSession = serverSession;
	}

	
	@Override
	public void process(Packet packet) {
		ServerJoinGamePacket serverJoinPacket = (ServerJoinGamePacket) packet;
		serverJoinPacket.getEntityId();
		//proxySession.setJoined(true);
		ConsoleIO.println("player \""+proxySession.getUsername()+"\" with entityID <"+serverJoinPacket.getEntityId()+"> has successfully joined world");		
		serverSession.setJoined(true);
		//proxySession.registerForPluginChannels();
		
		//proxySession.sendPacketToClient(packet);
		//proxySession.getJoinedCountDownLatch().countDown();
		//MinecraftProtocol protocol = (MinecraftProtocol)(proxySession.getServer().getPacketProtocol());
		//ConsoleIO.println("ServerJoinGamePacketBehaviour::process => Protocol status <"+protocol.getSubProtocol().name()+">");
	}
}