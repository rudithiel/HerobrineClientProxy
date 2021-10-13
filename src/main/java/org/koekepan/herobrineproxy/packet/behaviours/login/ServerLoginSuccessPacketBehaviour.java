package org.koekepan.herobrineproxy.packet.behaviours.login;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.packet.Packet;


import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IProxySessionNew;

public class ServerLoginSuccessPacketBehaviour implements Behaviour<Packet> {
		
	private IProxySessionNew proxySession;
	
	@SuppressWarnings("unused")
	private ServerLoginSuccessPacketBehaviour() {}
	
	
	public ServerLoginSuccessPacketBehaviour(IProxySessionNew proxySession) {
		this.proxySession = proxySession;
	}

	
	@Override
	public void process(Packet packet) {
		LoginSuccessPacket loginSuccessPacket = (LoginSuccessPacket)packet;	
		ConsoleIO.println("ServerLoginSuccessPacketBehaviour::process => Player \""+loginSuccessPacket.getProfile().getName()+"\" has successfully logged into the server");		
		proxySession.sendPacketToClient(packet);
	}
}