package org.koekepan.herobrineproxy.packet;

import org.koekepan.herobrineproxy.ConsoleIO;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;

import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

public class PacketAdapter extends SessionAdapter {

	private PacketListener listener;
	
	public PacketAdapter(PacketListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void packetReceived(PacketReceivedEvent event) {
	//	ConsoleIO.println("PacketAdapter::packetReceived => Received packet <"+event.getPacket().getClass().getSimpleName()+"> from host <"+event.getSession().getHost()+":"+event.getSession().getPort()+">");
	//	ConsoleIO.println(event.getPacket().toString());
		Session session = event.getSession();
		 if (session.getPacketProtocol() instanceof MinecraftProtocol) {
			 MinecraftProtocol protocol = (MinecraftProtocol)session.getPacketProtocol();
			// ConsoleIO.println("PacketAdapter::packetReceived => Protocol status <"+protocol.getSubProtocol().toString()+"> and CompressionThreshold <"+session.getCompressionThreshold()+">");
		 }
		listener.packetReceived(event.getPacket());
	}
	
	
	 @Override
	 public void disconnected(DisconnectedEvent event) {
		 Session session = event.getSession();
		 if (session.getPacketProtocol() instanceof MinecraftProtocol) {
			 MinecraftProtocol protocol = (MinecraftProtocol)session.getPacketProtocol();
			// ConsoleIO.println("PacketAdapter::disconnected => Protocol status <"+protocol.getSubProtocol().toString()+"> and CompressionThreshold <"+session.getCompressionThreshold()+">");
		 }
		 ConsoleIO.println("PacketAdapter::disconnected => Session <"+session.getClass().getSimpleName()+"> with listener <"+listener.getClass().getSimpleName()+"> has disconnected from host <"+session.getHost()+":"+session.getPort()+"> with reason "+event.getReason());
	 }
}
