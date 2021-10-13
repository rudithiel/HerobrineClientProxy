package org.koekepan.herobrineproxy.packet;

import java.io.IOException;

import com.github.steveice10.mc.protocol.util.ReflectionToString;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class EstablishConnectionPacket implements Packet {
	
	boolean establishConnection;
	String username;
	
	public EstablishConnectionPacket() {}
	
	
	public EstablishConnectionPacket(String username, boolean establishConnection) {
		this.username = username;
		this.establishConnection = establishConnection;		
	}
	
	
	public String getUsername() {
		return this.username;
	}
	

	public boolean establishConnection() {
		return this.establishConnection;
	}
	
	
	@Override
	public void read(NetInput in) throws IOException {
		this.username = in.readString();
		byte data = in.readByte();
		this.establishConnection = (data == 1);
	}

	
	@Override
	public void write(NetOutput out) throws IOException {
		out.writeString(this.username);
		byte[] data = new byte[1];
		data[0] = (byte)(establishConnection ? 1 : 0);
		out.writeBytes(data);
	}

	
	@Override
	public boolean isPriority() {
		return false;
	}
	

	@Override
	public String toString() {
		return ReflectionToString.toString(this);
	}
}