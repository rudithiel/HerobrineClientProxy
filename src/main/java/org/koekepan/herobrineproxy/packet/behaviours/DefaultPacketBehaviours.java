package org.koekepan.herobrineproxy.packet.behaviours;

import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;
import com.github.steveice10.packetlib.packet.Packet;

public class DefaultPacketBehaviours extends BehaviourHandler<Packet> {
	
	public DefaultPacketBehaviours() {
		clearBehaviours();
	}
}
