package gg.minecrush.epicteams.commands.teamadmin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TeamAdminTabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<String>();
        if (args.length == 1) {
            list.add("delete");
            list.add("reload");
            list.add("help");
            return list;
        }
        return null;
    }
}
