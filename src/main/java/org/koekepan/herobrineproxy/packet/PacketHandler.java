package org.koekepan.herobrineproxy.packet;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.github.steveice10.packetlib.packet.Packet;

import org.koekepan.herobrineproxy.ConsoleIO;
//import org.apache.logging.log4j.LogManager;
import org.koekepan.herobrineproxy.behaviour.BehaviourHandler;

public class PacketHandler implements Runnable, PacketListener {
	
	private IPacketSession packetSession;

	private Queue<Packet> incomingPackets = new ConcurrentLinkedQueue<Packet>();
	private Queue<Packet> outgoingPackets = new ConcurrentLinkedQueue<Packet>();
	
	private BehaviourHandler<Packet> behaviours;

	public PacketHandler(BehaviourHandler<Packet> behaviours, IPacketSession packetSession) {
		this.behaviours = behaviours;
		this.packetSession = packetSession;
	}

	
	public void setPacketSession(IPacketSession packetSession) {
		this.packetSession = packetSession;
	}
	
	
	public Queue<Packet> getIncomingPackets() {
		return incomingPackets;
	}
	
	
	public Queue<Packet> getPackets() {
		return outgoingPackets;
	}


	
	public BehaviourHandler<Packet> getBehaviours() {
		return behaviours;
	}
	
	
	public void setBehaviours(BehaviourHandler<Packet> behaviours) {
		this.behaviours = behaviours;
		//behaviours.printBehaviours();
	}

	
	public void process(Packet packet) {
		this.behaviours.process(packet);
	}

	
	private void addPacketToIncomingQueue(Packet packet) {
		try {
		//	ConsoleIO.println("PacketHandler::addPacketToIncomingQueue => Attempting to add packet <"+packet.getClass().getSimpleName()+"> that has registered behaviour <"+behaviours.hasBehaviour(packet.getClass())+"> to queue");
			//behaviours.printBehaviours();
			if (behaviours.hasBehaviour(packet.getClass())) {
				//ConsoleIO.println("PacketHandler::addPacketToIncomingQueue => Has registered behaviour for packet <"+packet.getClass().getSimpleName()+">");
				incomingPackets.add(packet);
			}
		} catch (IllegalStateException e) {
			ConsoleIO.println("PacketHandler::addPacketToIncomingQueue => IllegalStateException!");
			ConsoleIO.println(e.getMessage());
			e.printStackTrace();
			ConsoleIO.println(packet.toString());
		}
	}
	
	
	private void addPacketToOutgoingQueue(Packet packet) {
		try {
		 	//ConsoleIO.println("PacketHandler::addPacketToOutgoingQueue => Adding packet <"+packet.getClass().getSimpleName()+"> to queue");
			//if (behaviours.hasBehaviour(packet.getClass())) {
			//ConsoleIO.println("PacketHandler::addPacketToQueue => Has registered behaviour for packet <"+packet.getClass().getSimpleName()+">");
			outgoingPackets.add(packet);
			//}
		} catch (IllegalStateException e) {
			ConsoleIO.println("PacketHandler::addPacketToOutgoingQueue => IllegalStateException!");
			//log.error("PacketHandler::addPacketToQueue => IllegalStateException!", e);
			e.printStackTrace();
			ConsoleIO.println(packet.toString());
		}
		//outgoingPackets.add(packet);
	}
	

	@Override
	public void packetReceived(Packet packet) {
		addPacketToIncomingQueue(packet);
	}

	
	@Override
	public void sendPacket(Packet packet) {
		this.addPacketToOutgoingQueue(packet);
	}
	
	
	
	@Override
	public void run() {
		Packet packet = null;
		try {
			packet = incomingPackets.poll();
			if (packet != null) {
				//ConsoleIO.println("PacketHandler::run => Processing packet <"+packet.getClass().getSimpleName()+">. Packets remaining in queue: "+incomingPackets.size());
				behaviours.process(packet);
			}
			
			
			packet = outgoingPackets.peek();
			//ConsoleIO.println("PacketHandler::run => <"+outgoingPackets.size()+"> packets in outgoing queue");
			if (packet != null && packetSession != null) {
			//	ConsoleIO.println("PacketHandler::run => Sending outgoing packet  <"+packet.getClass().getSimpleName()+">");
				
				packet = outgoingPackets.poll();
				packetSession.send(packet);
			}			
		} catch (Exception e) {
			ConsoleIO.println("PacketHandler::run => Exception occurred while processing packet <"+packet.getClass().getSimpleName()+">");
			ConsoleIO.println(e.getMessage());
			e.printStackTrace();
			ConsoleIO.println(packet.toString());
		}
	}

}
