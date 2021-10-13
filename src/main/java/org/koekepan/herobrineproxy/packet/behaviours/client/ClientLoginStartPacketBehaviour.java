package org.koekepan.herobrineproxy.packet.behaviours.client;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IProxySessionNew;

import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.packet.Packet;

public class ClientLoginStartPacketBehaviour implements Behaviour<Packet>{

	private IProxySessionNew proxySession;
	
	@SuppressWarnings("unused")
	private ClientLoginStartPacketBehaviour() {}
	
	
	public ClientLoginStartPacketBehaviour(IProxySessionNew proxySession) {
		this.proxySession = proxySession;
	}

	
	@Override
	public void process(Packet packet) {
		LoginStartPacket loginPacket = (LoginStartPacket)packet;
		String username = loginPacket.getUsername();
		String serverHost = proxySession.getServerHost();
		int serverPort = proxySession.getServerPort();
		proxySession.setUsername(username);
		ConsoleIO.println("Player \"" + username + "\" is connecting to <" + serverHost + ":" + serverPort + ">");
		proxySession.connect(serverHost, serverPort);
	}

}
