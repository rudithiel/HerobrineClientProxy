package org.koekepan.herobrineproxy.packet.behaviours.login;

import com.github.steveice10.packetlib.packet.Packet;


import org.koekepan.herobrineproxy.ConsoleIO;
import org.koekepan.herobrineproxy.behaviour.Behaviour;

public class LoginSetCompressionPacketBehaviour implements Behaviour<Packet> {
		
	public LoginSetCompressionPacketBehaviour() {}
	
	
	@Override
	public void process(Packet packet) {
		ConsoleIO.println("LoginSetCompressionPacketBehaviour::process => Received setCompressionPacket");		
	}
}