package net.aegistudio.mcb;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.HelpCommand;
import org.bukkit.command.defaults.VanillaCommand;

import net.aegistudio.mcb.mcinject.MinecraftServer;
import net.aegistudio.mcb.reflect.clazz.SamePackageClass;
import net.aegistudio.mcb.reflect.method.AbstractExecutor;
import net.aegistudio.mcb.reflect.method.NamedExecutor;

@SuppressWarnings("deprecation")
public class VanillaExecutorProxy {
	public final net.aegistudio.mcb.reflect.clazz.Class vanillaCommandWrapper;
	public final AbstractExecutor dispatchVanillaCommand, getListener;
	
	public VanillaExecutorProxy(MinecraftServer server) {
		try {
			this.vanillaCommandWrapper = new SamePackageClass(server.getBukkitServerClass(), "command.VanillaCommandWrapper");
			this.dispatchVanillaCommand = new NamedExecutor(vanillaCommandWrapper.method(), "dispatchVanillaCommand");
			this.getListener = new NamedExecutor(vanillaCommandWrapper.method(), "getListener");
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public int executeSuccess(Command command, CommandSender sender, String commandLabel, String[] args) {
		if(command == null) return 0;
    	if(!command.testPermission(sender)) return 0;
    	
    	if(command instanceof VanillaCommand && !(command instanceof HelpCommand)) {
	        Object listener = getListener.invoke(command, sender);
	        return (int)dispatchVanillaCommand.invoke(command, sender, listener, args);
    	}
    	else return command.execute(sender, commandLabel, args)? 1 : 0;
    }
}
