package org.koekepan.herobrineproxy.session;

import org.koekepan.herobrineproxy.ConsoleIO;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;

public class PacketForwarder extends SessionAdapter {
	private Session session = null;
	
	public PacketForwarder(Session session) {
		this.session = session;
	}

	@Override
	public void packetReceived(PacketReceivedEvent event) {
		
		if (session.isConnected()) {
			session.send(event.getPacket());
		} else {
			ConsoleIO.println("PacketForwarder::packetReceived => Not connected to host <"+session.getHost()+":"+session.getPort()+">");
		}
	}
	
	
	@Override
	public void packetSent(PacketSentEvent event) {
		Packet packet = event.getPacket();
		ConsoleIO.println("PacketForwarder::packetSent => Sent packet <"+packet.getClass().getSimpleName()+"> to host <"+event.getSession().getHost()+":"+event.getSession().getPort()+">");		
		ConsoleIO.println(event.getPacket().toString());
	}
}
