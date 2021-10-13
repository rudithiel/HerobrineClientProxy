package org.koekepan.herobrineproxy;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.packet.handshake.client.HandshakePacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSetCompressionPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;

// this class describes the packet protocol that is used by the proxy and tracks protocol state changes.
// one instance per client or server connection.
public class HerobrineProxyProtocol extends MinecraftProtocol {

	public HerobrineProxyProtocol() {
		super(SubProtocol.STATUS);
	}


	// add listener for a server connection (proxy <-> server)
	@Override
	public void newClientSession(Client client, Session session) {
		setSubProtocol(SubProtocol.HANDSHAKE, true, session); // start in handshake protocol state

		session.addListener(new SessionAdapter() {
			// client-bound packets
			@Override
			public void packetReceived(PacketReceivedEvent event) {
				processSubProtocolState(this, event.getSession(), event.getPacket(), true, true);
			}

			// server-bound packets
			@Override
			public void packetSent(PacketSentEvent event) {
				processSubProtocolState(this, event.getSession(), event.getPacket(), true, false);
			}	
		});
	}


	// add listener for a client connection (client <-> proxy)
	@Override
	public void newServerSession(Server server, Session session) {
		setSubProtocol(SubProtocol.HANDSHAKE, false, session); // start in handshake protocol state

		session.addListener(new SessionAdapter() {
			// server-bound packets
			@Override
			public void packetReceived(PacketReceivedEvent event) {
				processSubProtocolState(this, event.getSession(), event.getPacket(), false, false);
			}

			// client-bound packets
			@Override
			public void packetSent(PacketSentEvent event) {
				processSubProtocolState(this, event.getSession(), event.getPacket(), false, true);
			}
		});
	}

	// process protocol state changes from server-bound or client-bound packets
	private void processSubProtocolState(SessionListener listener, Session session, Packet packet, boolean client, boolean clientbound) {
		HerobrineProxyProtocol protocol = (HerobrineProxyProtocol) session.getPacketProtocol();

		if (clientbound) {
			// client-bound packets
			if (protocol.getSubProtocol() == SubProtocol.LOGIN) {
				if (packet instanceof LoginSetCompressionPacket) {
					session.setCompressionThreshold(((LoginSetCompressionPacket)packet).getThreshold());
				} else if (packet instanceof LoginSuccessPacket) {
					protocol.setSubProtocol(SubProtocol.GAME, client, session);
					session.removeListener(listener);
				}
			}
		} else { 
			// server-bound packets
			if (protocol.getSubProtocol() == SubProtocol.HANDSHAKE) {
				if (packet instanceof HandshakePacket) {
					HandshakePacket handshake = (HandshakePacket) packet;
					switch (handshake.getIntent()) {
					case STATUS:
						protocol.setSubProtocol(SubProtocol.STATUS, client, session);
						session.removeListener(listener);
						break;
					case LOGIN:
						protocol.setSubProtocol(SubProtocol.LOGIN, client, session);
						if (handshake.getProtocolVersion() > MinecraftConstants.PROTOCOL_VERSION) {
							session.disconnect("Outdated server! I'm still on " + MinecraftConstants.GAME_VERSION + ".");
						} else if (handshake.getProtocolVersion() < MinecraftConstants.PROTOCOL_VERSION) {
							session.disconnect("Outdated client! Please use " + MinecraftConstants.GAME_VERSION + ".");
						}
						break;
					default:
						throw new UnsupportedOperationException("Invalid client intent: " + handshake.getIntent());
					}
				}
			}
		}
	}

}
