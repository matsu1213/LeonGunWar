package net.azisaba.lgw.core.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * チームを表現するためだけに作られたEnumクラス
 *
 * @author siloneco
 *
 */
public enum BattleTeam {

    RED("赤チーム", Color.fromRGB(0x930000), ChatColor.DARK_RED),
    BLUE("青チーム", Color.fromRGB(0x0000A0), ChatColor.BLUE);

    private final String name;
    private final Color color;
    private final ChatColor chatColor;


    BattleTeam(String name,Color color,ChatColor chatColor) {
        this.name = name;
        this.color = color;
        this.chatColor = chatColor;
    }

    public String getTeamName() {
        return chatColor + name;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Color getColor() {
        return color;
    }
}
