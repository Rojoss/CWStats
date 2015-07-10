package com.clashwars.cwstats.stats;

import com.clashwars.cwstats.CWStats;
import com.clashwars.cwstats.stats.internal.Game;
import com.clashwars.cwstats.stats.internal.Stat;
import com.clashwars.cwstats.stats.internal.StatCategory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataManager {

    private CWStats cws;
    private HashMap<Integer, Game> games = new HashMap<Integer, Game>();
    private HashMap<Integer, Stat> stats = new HashMap<Integer, Stat>();
    private HashMap<Integer, StatCategory> categories = new HashMap<Integer, StatCategory>();

    public DataManager(CWStats cws) {
        this.cws = cws;
        if (cws.getSql() != null) {
            loadGames();
            loadStats();
            loadCategories();
        }
    }


    /* Methods to load data from database */
    public void loadGames() {
        try {
            HashMap<Integer, Game> newGamesMap = new HashMap<Integer, Game>();
            PreparedStatement loadGamesPS = cws.getSql().prepareStatement("SELECT game_id,date,game_type FROM Games;");
            ResultSet gamesData = loadGamesPS.executeQuery();

            while (gamesData.next()) {
                newGamesMap.put(gamesData.getInt("game_id"), new Game(
                        gamesData.getInt("game_id"),
                        gamesData.getTimestamp("date"),
                        gamesData.getString("game_type")
                ));
            }

            games = newGamesMap;
            cws.log("Loaded in " + games.size() + " games from the database!");
        } catch (SQLException e) {
            cws.logError("Failed at loading games from database!");
        }
    }

    public void loadStats() {
        try {
            HashMap<Integer, Stat> newStatsMap = new HashMap<Integer, Stat>();
            PreparedStatement loadStatsPS = cws.getSql().prepareStatement("SELECT stat_id,category_id,name,short_name,description,item,displayed,calculated,calculation FROM Stats;");
            ResultSet statsData = loadStatsPS.executeQuery();

            while (statsData.next()) {
                newStatsMap.put(statsData.getInt("stat_id"), new Stat(
                        statsData.getInt("stat_id"),
                        statsData.getInt("category_id"),
                        statsData.getString("name"),
                        statsData.getString("short_name"),
                        statsData.getString("description"),
                        statsData.getString("item"),
                        statsData.getBoolean("displayed"),
                        statsData.getBoolean("calculated"),
                        statsData.getString("calculation")
                ));
            }

            stats = newStatsMap;
            cws.log("Loaded in " + stats.size() + " stats from the database!");
        } catch (SQLException e) {
            cws.logError("Failed at loading stats list from database!");
        }
    }

    public void loadCategories() {
        try {
            HashMap<Integer, StatCategory> newCatsMap = new HashMap<Integer, StatCategory>();
            PreparedStatement loadCatsPS = cws.getSql().prepareStatement("SELECT category_id,name,item FROM StatCategories;");
            ResultSet catsData = loadCatsPS.executeQuery();

            while (catsData.next()) {
                newCatsMap.put(catsData.getInt("category_id"), new StatCategory(
                        catsData.getInt("category_id"),
                        catsData.getString("name"),
                        catsData.getString("item")
                ));
            }

            categories = newCatsMap;
            cws.log("Loaded in " + categories.size() + " stat categories from the database!");
        } catch (SQLException e) {
            cws.logError("Failed at loading stat categories from database!");
        }
    }


    /* Games */
    public int getGameCount() {
        return games.size();
    }

    public Game getGame(int gameID) {
        if (games.containsKey(gameID)) {
            return games.get(gameID);
        }
        return null;
    }

    public List<Integer> getGamesBetween(Timestamp startTimeStamp, Timestamp endTimeStamp) {
        if (startTimeStamp.after(endTimeStamp)) {
            final Timestamp endStamp = endTimeStamp;
            endTimeStamp = startTimeStamp;
            startTimeStamp = endStamp;
        }

        List<Integer> gamesList = new ArrayList<Integer>();
        for (Game game : games.values()) {
            if ((game.date.equals(startTimeStamp) && game.date.equals(endTimeStamp)) || (game.date.after(startTimeStamp) && game.date.before(endTimeStamp))) {
                gamesList.add(game.game_id);
            }
        }

        return gamesList;
    }

    public Game getClosestGame(Timestamp timestamp) {
        int closestDiff = -1;
        Game closest = null;
        for (Game game : games.values()) {
            int difference = Math.abs(game.date.compareTo(timestamp));
            if (closestDiff == -1 || difference <= closestDiff) {
                closestDiff = difference;
                closest = game;
            }
        }
        return closest;
    }

    public List<Integer> getGameIds() {
        return Arrays.asList(games.keySet().toArray(new Integer[]{}));
    }

    public Game[] getGames() {
        return games.values().toArray(new Game[] {});
    }

    public List<Game> getGamesList() {
        return Arrays.asList(getGames());
    }


    /* Stats */
    public int getStatCount() {
        return stats.size();
    }

    public Stat getStat(int statID) {
        if (stats.containsKey(statID)) {
            return stats.get(statID);
        }
        return null;
    }

    public Stat getStatByName(String name) {
        for (Stat stat : stats.values()) {
            if (stat.name.equalsIgnoreCase(name)) {
                return stat;
            }
        }
        return null;
    }

    public Stat getStatByShortName(String shortName) {
        for (Stat stat : stats.values()) {
            if (stat.short_name.equalsIgnoreCase(shortName)) {
                return stat;
            }
        }
        return null;
    }

    public int getStatID(String statName) {
        for (Stat stat : stats.values()) {
            if (stat.name.equalsIgnoreCase(statName)) {
                return stat.stat_id;
            }
        }
        return -1;
    }

    public Stat[] getStats() {
        return stats.values().toArray(new Stat[] {});
    }

    public List<Stat> getStatsList() {
        return Arrays.asList(getStats());
    }

    public List<Stat> getStats(int category_id) {
        List<Stat> statsList = new ArrayList<Stat>();
        for (Stat stat : stats.values()) {
            if (stat.category_id == category_id) {
                statsList.add(stat);
            }
        }
        return statsList;
    }


    /* Stat categories */
    public int getCatCount() {
        return categories.size();
    }

    public StatCategory getCat(int catID) {
        if (categories.containsKey(catID)) {
            return categories.get(catID);
        }
        return null;
    }

    public StatCategory getCatByName(String name) {
        for (StatCategory cat : categories.values()) {
            if (cat.name.equalsIgnoreCase(name)) {
                return cat;
            }
        }
        return null;
    }

    public StatCategory[] getCats() {
        return categories.values().toArray(new StatCategory[] {});
    }

    public List<StatCategory> getCatsList() {
        return Arrays.asList(getCats());
    }

}
