package com.listjonas.teamSmith.commands;

import com.listjonas.teamSmith.TeamSmith;
import com.listjonas.teamSmith.commands.handlers.*;
import com.listjonas.teamSmith.manager.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor,TabCompleter{
    private final TeamSmith plugin;
    private final TeamManager teamManager;
    private final Map<String,SubCommandExecutor> handlers;

    public static final String MSG_PREFIX=ChatColor.GOLD+"[TeamSmith] "+ChatColor.RESET;
    public static final String SUCCESS_COLOR=ChatColor.GREEN.toString();
    public static final String ERROR_COLOR=ChatColor.RED.toString();
    public static final String INFO_COLOR=ChatColor.YELLOW.toString();
    public static final String ACCENT_COLOR=ChatColor.AQUA.toString();

    public TeamCommand(TeamSmith plugin,TeamManager teamManager){
        this.plugin=plugin;this.teamManager=teamManager;handlers=new HashMap<>();
        handlers.put("create",new CreateTeamHandler());
        handlers.put("delete",new DeleteTeamHandler());
        handlers.put("disband",handlers.get("delete"));
        handlers.put("invite",new InvitePlayerHandler());
        handlers.put("kick",new KickPlayerHandler());
        handlers.put("leave",new LeaveTeamHandler());
        handlers.put("transfer",new TransferOwnershipHandler());
        handlers.put("prefix",new SetPrefixHandler());
        handlers.put("prefixcolor",new SetPrefixColorHandler());
        handlers.put("info",new TeamInfoHandler());
        handlers.put("setrole",new SetRoleHandler());
        handlers.put("motd",new SetMotdHandler());
        handlers.put("friendlyfire",new FriendlyFireHandler());
        handlers.put("ff",handlers.get("friendlyfire"));
    }

    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(!(sender instanceof Player)){sender.sendMessage(MSG_PREFIX+ERROR_COLOR+"Only players can use this command.");return true;}
        Player player=(Player)sender;
        if(args.length==0){sendHelpMessage(player);return true;}
        String sub=args[0].toLowerCase();
        SubCommandExecutor h=handlers.get(sub);
        if(h!=null) return h.execute(player,Arrays.copyOfRange(args,1,args.length),teamManager);
        sendHelpMessage(player);return true;
    }

    private void sendHelpMessage(Player p){
        p.sendMessage(MSG_PREFIX+ACCENT_COLOR+"--- TeamSmith Help ---");
        handlers.forEach((k,h)->p.sendMessage(INFO_COLOR+"/team "+k+(h.getArgumentUsage().isEmpty()?"":" "+h.getArgumentUsage())+ChatColor.GRAY+" - "+h.getDescription()));
        p.sendMessage(MSG_PREFIX+ACCENT_COLOR+"--------------------");
    }

    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){
        if(args.length==1){
            return handlers.keySet().stream().filter(k->k.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        String sub=args[0].toLowerCase();
        SubCommandExecutor h=handlers.get(sub);
        if(h instanceof TabCompleter) return ((TabCompleter)h).onTabComplete(sender,command,alias,Arrays.copyOfRange(args,1,args.length));
        return Collections.emptyList();
    }
}