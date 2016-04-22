package net.aegistudio.mcb.unit;

import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Data;

public class RepeaterQueue implements Data {
	private byte queue;
	public RepeaterQueue(byte queue) {
		this.queue = queue;
	}
	
	public RepeaterQueue() {
		this((byte)0);
	}

	@Override
	public Data duplicate() {
		return new RepeaterQueue(queue);
	}
	
	public void save(OutputStream stream) throws Exception {
		stream.write(queue);
	}
	
	public static RepeaterQueue read(InputStream stream) throws Exception {
		return new RepeaterQueue((byte) stream.read());
	}
	
	public void enqueue(boolean state, int queueMask) {
		this.queue = (byte) ((this.queue << 1 | (state? 1 : 0)) & queueMask);
	}
	
	public boolean powered(int powerMask) {
		return (this.queue & powerMask) != 0;
	}
}
