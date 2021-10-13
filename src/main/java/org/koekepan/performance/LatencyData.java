package org.koekepan.performance;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LatencyData {
	public UUID playerUUID;
	public long sequenceNumber;
	public long serverSentTime;
	public long clientReceiveTime;
	public long clientSentTime;
	public long serverReceiveTime;
	//public long serverRTT;

	
	public LatencyData() {}

	
	public LatencyData(UUID playerUUID, long sequenceNumber, long serverSentTime) {
		this.playerUUID = playerUUID;
		this.sequenceNumber = sequenceNumber;
		this.serverSentTime = serverSentTime;
		this.clientReceiveTime = -1;
		this.clientSentTime = -1;
		this.serverReceiveTime = -1;
	//	this.RTT = 0;
	}
	
	
	public byte[] convertToBytes() {
    	ByteBuf buff = Unpooled.buffer();
    	buff.writeLong(playerUUID.getMostSignificantBits());
    	buff.writeLong(playerUUID.getLeastSignificantBits());
        buff.writeLong(sequenceNumber);
        buff.writeLong(serverSentTime);
        buff.writeLong(clientReceiveTime);
        buff.writeLong(clientSentTime);
        buff.writeLong(serverReceiveTime);
        //buff.writeLong(RTT);
    	return buff.array();
	}
	
	
	public void reconstructFromBytes(byte[] data) {
		ByteBuf buffer = Unpooled.wrappedBuffer(data);
		long mostSignificantBits = buffer.readLong();
		long leastSignificantBits = buffer.readLong();
		this.playerUUID = new UUID(mostSignificantBits, leastSignificantBits);
		this.sequenceNumber = buffer.readLong();
		this.serverSentTime = buffer.readLong();
		this.clientReceiveTime = buffer.readLong();
		this.clientSentTime = buffer.readLong();
		this.serverReceiveTime = buffer.readLong();
//		this.RTT = buffer.readLong();
	}
}
