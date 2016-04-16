package net.aegistudio.mcb;

import java.io.InputStream;
import java.io.OutputStream;

public enum Binary implements Data {
	FALSE(false), TRUE(true);
	
	public final boolean booleanValue;
	private Binary(boolean bool) {
		this.booleanValue = bool;
	}
	
	public static Binary load(InputStream input) throws Exception {
		return input.read() == 0? FALSE : TRUE;
	}
	
	public void save(OutputStream output) throws Exception {
		output.write(booleanValue? 1 : 0);
	}
	
	public Binary to(boolean bool) {
		return bool? TRUE : FALSE;
	}
	
	public Binary not() {
		return to(!booleanValue);
	}

	@Override
	public Binary duplicate() {
		return this;
	}
}
