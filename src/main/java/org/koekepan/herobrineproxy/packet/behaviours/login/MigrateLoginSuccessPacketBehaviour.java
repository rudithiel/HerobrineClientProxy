package org.koekepan.herobrineproxy.packet.behaviours.login;

import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.packet.Packet;


import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IProxySessionNew;

public class MigrateLoginSuccessPacketBehaviour implements Behaviour<Packet> {
		
	private IProxySessionNew proxySession;
	
	@SuppressWarnings("unused")
	private MigrateLoginSuccessPacketBehaviour() {}
	
	
	public MigrateLoginSuccessPacketBehaviour(IProxySessionNew proxySession) {
		this.proxySession = proxySession;
	}

	
	@Override
	public void process(Packet packet) {
		LoginSuccessPacket loginSuccessPacket = (LoginSuccessPacket)packet;	
		ConsoleIO.println("MigrateLoginSuccessPacketBehaviour::process => Player \""+loginSuccessPacket.getProfile().getName()+"\" has successfully logged into the server");		
		
		
		// disconnect old server (clientside and serverside)
		//proxySession.getServer().removeListener(proxySession.getServerPacketForwarder());
		//proxySession.setServerPacketForwarder(null);
		//proxySession.getClient().removeListener(proxySession.getClientPacketForwarder());
		//proxySession.setClientPacketForwarder(null);
		proxySession.disconnectFromServer();
		proxySession.switchServer();
		
	//	proxySession.getServer().disconnect("Finished.", true);

		// set proxy session server to new server
	//	proxySession.setServer(event.getSession());
	}
}