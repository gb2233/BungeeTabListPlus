/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.OptionalInt;

public abstract class AbstractFillPlayersSection extends Section {

    private final OptionalInt vAlign;
    private final SlotTemplate prefix;
    private final SlotTemplate suffix;
    protected List<IPlayer> players;
    protected final PlayerSorter sorter;
    private final int maxPlayers;
    private final List<SlotTemplate> playerLines;
    private final List<SlotTemplate> morePlayerLines;

    public AbstractFillPlayersSection(int vAlign, SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        this.playerLines = playerLines;
        this.morePlayerLines = morePlayerLines;
        this.vAlign = vAlign == -1 ? OptionalInt.empty() : OptionalInt.of(vAlign);
        this.prefix = prefix;
        this.suffix = suffix;
        this.sorter = sorter;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public void precalculate(TabListContext context) {
        players = getPlayers(context.getViewer(), context);
        sorter.sort(context, players);
    }

    protected abstract List<IPlayer> getPlayers(ProxiedPlayer player, TabListContext context);

    @Override
    public int getMinSize() {
        return getEffectiveSize(0);
    }

    @Override
    public int getMaxSize() {
        int m = players.size();
        if (m > this.maxPlayers) {
            m = this.maxPlayers;
        }
        return m * playerLines.size();
    }

    @Override
    public boolean isSizeConstant() {
        return false;
    }

    @Override
    public int getEffectiveSize(int proposedSize) {
        int playersToShow = players.size();
        if (playersToShow > this.maxPlayers) {
            playersToShow = this.maxPlayers;
        }
        if (playersToShow * playerLines.size() > proposedSize) {
            playersToShow = (proposedSize - morePlayerLines.size()) / playerLines.size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = players.size() - playersToShow;
        return playersToShow * playerLines.size() + (other_count > 0 ? morePlayerLines.size() : 0);
    }

    @Override
    public Slot getSlotAt(TabListContext context, int pos, int size) {
        int playersToShow = players.size();
        if (playersToShow > this.maxPlayers) {
            playersToShow = this.maxPlayers;
        }
        if (playersToShow * playerLines.size() > size) {
            playersToShow = (size - morePlayerLines.size()) / playerLines.size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = players.size() - playersToShow;

        if (pos < playersToShow * playerLines.size()) {
            int playerIndex = pos / playerLines.size();
            int playerLinesIndex = pos % playerLines.size();
            IPlayer player = players.get(playerIndex);
            return SlotTemplate.of(SlotTemplate.skin(player.getSkin()), SlotTemplate.ping(player.getPing()),
                    prefix, playerLines.get(playerLinesIndex), suffix)
                    .buildSlot(context.setPlayer(player));
        } else if (other_count > 0) {
            int morePlayerLinesIndex = pos - playersToShow * playerLines.size();
            return SlotTemplate.of(prefix, morePlayerLines.get(morePlayerLinesIndex), suffix).buildSlot(context.setOtherCount(other_count));
        } else {
            return null;
        }
    }

    @Override
    public OptionalInt getStartColumn() {
        return vAlign;
    }

}
