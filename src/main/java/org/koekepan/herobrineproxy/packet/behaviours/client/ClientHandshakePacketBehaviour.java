package org.koekepan.herobrineproxy.packet.behaviours.client;

import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;
import org.koekepan.herobrineproxy.session.IClientSession;

import com.github.steveice10.mc.protocol.data.handshake.HandshakeIntent;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.packetlib.packet.Packet;

public class ClientHandshakePacketBehaviour implements Behaviour<Packet>{

	private IClientSession clientSession;
	
	@SuppressWarnings("unused")
	private ClientHandshakePacketBehaviour() {}
		
	public ClientHandshakePacketBehaviour(IClientSession clientSession) {
		this.clientSession = clientSession;
	}

	
	@Override
	public void process(Packet packet) {	
		HandshakePacket handshakePacket = (HandshakePacket)packet;
		HandshakeIntent intent = handshakePacket.getIntent();
		if (intent == HandshakeIntent.LOGIN) {
			ConsoleIO.println("Received Handshake from <" + clientSession.getHost() + ":" + clientSession.getPort() + ">");
		}
	}

}
