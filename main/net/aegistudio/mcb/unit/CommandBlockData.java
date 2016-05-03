package net.aegistudio.mcb.unit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import net.aegistudio.mcb.Data;

public class CommandBlockData implements Data, CommandSender {
	public final CommandBlockEditor editor;
	public boolean nonTick = true;
	public String command = "";
	public String translated = ""; 	// Volatile
	
	public String lastOutput = "";
	public boolean lastOutputState = false;
	
	public String lastEdited = "";
	public boolean lastInputState = false;
	
	public CommandBlockData(CommandBlockEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public Data duplicate() {
		CommandBlockData data = new CommandBlockData(this.editor);
		data.command = this.command;
		data.lastOutput = this.lastOutput;
		data.lastOutputState = this.lastOutputState;
		data.lastEdited = this.lastEdited;
		data.lastInputState = this.lastInputState;
		return data;
	}
	
	public void write(OutputStream output) throws IOException {
		DataOutputStream dout = new DataOutputStream(output);
		dout.writeByte(5);
		dout.writeUTF(command);
		dout.writeUTF(lastOutput);
		dout.writeBoolean(lastOutputState);
		dout.writeUTF(lastEdited);
		dout.writeBoolean(lastInputState);
	}
	
	public static CommandBlockData read(CommandBlockEditor editor, InputStream input) throws IOException {
		DataInputStream din = new DataInputStream(input);
		CommandBlockData result = new CommandBlockData(editor);
		byte section = din.readByte();
		if(section > 0) result.command = din.readUTF();
		if(section > 1) result.lastOutput = din.readUTF();
		if(section > 2) result.lastOutputState = din.readBoolean();
		if(section > 3) result.lastEdited = din.readUTF();
		if(section > 4) result.lastInputState = din.readBoolean();
		return result;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {	return null;	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {		return null;	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {		return null;	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {		return null;	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {		return null;	}

	@Override
	public boolean hasPermission(String arg0) {		return true;	}

	@Override
	public boolean hasPermission(Permission arg0) {		return true;	}

	@Override
	public boolean isPermissionSet(String arg0) {	return false;	}

	@Override
	public boolean isPermissionSet(Permission arg0) {	return false;	}

	@Override
	public void recalculatePermissions() {			}

	@Override
	public void removeAttachment(PermissionAttachment arg0) {		}

	@Override
	public boolean isOp() {
		return true;
	}

	@Override
	public void setOp(boolean arg0) {
		// no
	}

	@Override
	public String getName() {
		return "@";
	}

	@Override
	public Server getServer() {
		return editor.getServer();
	}

	@Override
	public void sendMessage(String arg0) {
		this.lastOutput = arg0;
	}

	@Override
	public void sendMessage(String[] arg0) {
		if(arg0.length == 0) return;
		this.lastOutput = arg0[arg0.length - 1];
	}
}
