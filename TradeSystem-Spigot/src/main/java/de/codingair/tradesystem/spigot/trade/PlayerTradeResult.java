package de.codingair.tradesystem.spigot.trade;

import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PlayerTradeResult extends TradeResult {
    private final Trade trade;
    private final Player player;

    public PlayerTradeResult(@NotNull Trade trade, @NotNull Player player, int playerId) {
        super(playerId);
        this.trade = trade;
        this.player = player;
    }

    @NotNull
    List<String> buildItemReport() {
        return buildItemReport(itemToNameMapper());
    }

    @NotNull
    public static Function<ItemStack, String> itemToNameMapper() {
        return item -> {
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                assert meta != null;

                if (meta.hasDisplayName()) return ChatColor.stripColor(meta.getDisplayName());
            }

            return properName(item.getType().name());
        };
    }

    /**
     * @param mapper A function that maps an {@link ItemStack} to a {@link String} that is used in the report.
     * @return An unsorted list of lines that can be used in a report. Will be sorted lexicographically before printing.
     */
    @NotNull
    public List<String> buildItemReport(Function<ItemStack, String> mapper) {
        List<String> lines = new ArrayList<>();

        Map<ItemStack, Integer> receiving = new HashMap<>();
        Map<ItemStack, Integer> sending = new HashMap<>();

        for (Map.Entry<ItemStack, Boolean> e : items.entrySet()) {
            ItemStack item = e.getKey();
            boolean receive = e.getValue();

            int amount = item.getAmount();
            item.setAmount(1);

            if (receive) receiving.merge(item, amount, Integer::sum);
            else sending.merge(item, amount, Integer::sum);
        }

        receiving.forEach((item, amount) -> {
            item.setAmount(amount);

            String itemName = Lang.get("Trade_Finish_Report_Object_Item", player,
                    new Lang.P("amount", amount + ""),
                    new Lang.P("item", mapper.apply(item))
            );

            Lang.P info = new Lang.P("object", itemName);
            lines.add(Lang.get("Trade_Finish_Report_Receive", player, info));
        });

        sending.forEach((item, amount) -> {
            item.setAmount(amount);

            String itemName = Lang.get("Trade_Finish_Report_Object_Item", player,
                    new Lang.P("amount", amount + ""),
                    new Lang.P("item", mapper.apply(item))
            );

            Lang.P info = new Lang.P("object", itemName);
            lines.add(Lang.get("Trade_Finish_Report_Give", player, info));
        });

        return lines;
    }

    @NotNull
    List<String> buildEconomyReport() {
        List<String> lines = new ArrayList<>();

        for (EconomyIcon<?> icon : economyIcons) {
            BigDecimal diff = icon.getOverallDifference(trade, playerId);
            if (diff.signum() == 0) continue;
            boolean receive = diff.signum() > 0;

            boolean singular = diff.equals(BigDecimal.ONE);
            if (receive) {
                Lang.P info = new Lang.P("object",
                        EconomyIcon.makeFancyString(diff, icon.isDecimal()) + " " + icon.getName(player, singular));
                lines.add(Lang.get("Trade_Finish_Report_Receive", player, info));
            } else {
                Lang.P info = new Lang.P("object",
                        EconomyIcon.makeFancyString(diff.negate(), icon.isDecimal()) + " " + icon.getName(player, singular));
                lines.add(Lang.get("Trade_Finish_Report_Give", player, info));
            }
        }

        return lines;
    }

    /**
     * Returns a capitalized version of the given string with underscores replaced with spaces.
     *
     * @param input the input string to be converted.
     * @return a capitalized version of the input string.
     */
    public static String properName(final String input) {
        return capitalized(input.replace("_", " "));
    }

    /**
     * Returns a capitalized version of the given string with the first letter of each word capitalized
     * and all other letters in lowercase.
     *
     * @param input the input string to be capitalized.
     * @return a capitalized version of the input string.
     */
    public static String capitalized(final String input) {
        final StringBuilder builder = new StringBuilder();
        final String[] words = input.split(" ");
        for (final String word : words) {
            builder.append(word.charAt(0))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }
        return builder.toString().trim();
    }

    /**
     * @return The current player.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The trade.
     */
    @NotNull
    public Trade getTrade() {
        return trade;
    }
}
