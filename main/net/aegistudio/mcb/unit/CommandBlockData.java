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
	public boolean nonTick = true;
	public String command = "";
	public boolean lastOutput = false;
	public CommandBlockData() {
		this("", false);
	}
	
	public CommandBlockData(String command, boolean lastOutput) {
		this.command = command;
		this.lastOutput = lastOutput;
	}
	
	@Override
	public Data duplicate() {
		return new CommandBlockData(this.command, this.lastOutput);
	}
	
	public void write(OutputStream output) throws IOException {
		DataOutputStream dout = new DataOutputStream(output);
		dout.writeUTF(command);
		dout.writeBoolean(lastOutput);
	}
	
	public static CommandBlockData read(InputStream input) throws IOException {
		DataInputStream din = new DataInputStream(input);
		String command = din.readUTF();
		boolean lastOutput = din.readBoolean();
		return new CommandBlockData(command, lastOutput);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0) {
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
		return null;
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return null;
	}

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
		return null;
	}

	@Override
	public Server getServer() {
		return null;
	}

	@Override
	public void sendMessage(String arg0) {			}

	@Override
	public void sendMessage(String[] arg0) {			}
}
