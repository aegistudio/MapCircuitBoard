package net.aegistudio.mcb.unit;

import java.io.InputStream;
import java.io.OutputStream;

import net.aegistudio.mcb.Data;

public class Voltage implements Data {
	int voltage = 0;
	
	public Voltage() {}
	
	public Voltage(int voltage) {
		this.voltage = voltage;
	}
	
	@Override
	public Data duplicate() {
		return new Voltage(this.voltage);
	}
	
	public static Voltage load(InputStream inputStream) throws Exception {
		return new Voltage(inputStream.read());
	}
	
	public void save(OutputStream outputStream) throws Exception {
		outputStream.write(voltage);
	}
}