package me.SuperRonanCraft.BetterRTP.references.database;

import lombok.Getter;
import lombok.NonNull;
import me.SuperRonanCraft.BetterRTP.BetterRTP;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueData;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueGenerator;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.QueueHandler;
import me.SuperRonanCraft.BetterRTP.references.rtpinfo.worlds.RTPWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class DatabaseQueue extends SQLite {

    public DatabaseQueue() {
        super(DATABASE_TYPE.QUEUE);
    }

    @Override
    public List<String> getTables() {
        List<String> list = new ArrayList<>();
        list.add("Queue");
        return list;
    }

    public enum COLUMNS {
        ID("id", "integer PRIMARY KEY AUTOINCREMENT"),
        //Location Data
        X("x", "long"),
        Z("z", "long"),
        WORLD("world", "varchar(32)"),
        GENERATED("generated", "long")
        ;

        public final String name;
        public final String type;

        COLUMNS(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    /*public List<QueueData> getAll() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<QueueData> queueDataList = new ArrayList<>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + tables.get(0));

            rs = ps.executeQuery();
            while (rs.next()) {
                long x = rs.getLong(COLUMNS.X.name);
                long z = rs.getLong(COLUMNS.Z.name);
                String worldName = rs.getString(COLUMNS.WORLD.name);
                int id = rs.getInt(COLUMNS.ID.name);
                long generated = rs.getLong(COLUMNS.GENERATED.name);
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    queueDataList.add(new QueueData(new Location(world, x, 69, z), generated, id));
                }
            }
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, rs, conn);
        }
        return queueDataList;
    }*/

    @Override public void load() {
        if (QueueHandler.isEnabled())
            super.load();
    }

    @Override
    public void initialize() {
        super.initialize();
        createIndexes();
    }

    public List<QueueData> getInRange(QueueRangeData range) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<QueueData> queueDataList = new ArrayList<>();
        try {
            conn = getSQLConnection();
            int count = getRangeCount(conn, range);
            if (count <= 0)
                return queueDataList;
            int limit = QueueGenerator.queueMax + 1;
            int maxOffset = Math.max(0, count - limit);
            int offset = maxOffset > 0 ? new Random().nextInt(maxOffset + 1) : 0;
            ps = conn.prepareStatement("SELECT * FROM " + tables.get(0) + " WHERE "
                    + COLUMNS.WORLD.name + " = ? AND "
                    + COLUMNS.X.name + " BETWEEN ? AND ?"
                    + " AND " + COLUMNS.Z.name + " BETWEEN ? AND ?"
                    + " LIMIT ? OFFSET ?"
            );
            ps.setString(1, range.getWorld().getName());
            ps.setInt(2, range.getXLow());
            ps.setInt(3, range.getXHigh());
            ps.setInt(4, range.getZLow());
            ps.setInt(5, range.getZHigh());
            ps.setInt(6, limit);
            ps.setInt(7, offset);

            //BetterRTP.getInstance().getLogger().info(ps.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                long x = rs.getLong(COLUMNS.X.name);
                long z = rs.getLong(COLUMNS.Z.name);
                String worldName = rs.getString(COLUMNS.WORLD.name);
                int id = rs.getInt(COLUMNS.ID.name);
                long generated = rs.getLong(COLUMNS.GENERATED.name);
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    queueDataList.add(new QueueData(new Location(world, x, 69 /*giggity*/, z), generated, id));
                    //queueDataList.add(data);
                }
            }
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, rs, conn);
        }
        return queueDataList;
    }

    private int getRangeCount(Connection conn, QueueRangeData range) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM " + tables.get(0) + " WHERE "
                    + COLUMNS.WORLD.name + " = ? AND "
                    + COLUMNS.X.name + " BETWEEN ? AND ?"
                    + " AND " + COLUMNS.Z.name + " BETWEEN ? AND ?"
            );
            ps.setString(1, range.getWorld().getName());
            ps.setInt(2, range.getXLow());
            ps.setInt(3, range.getXHigh());
            ps.setInt(4, range.getZLow());
            ps.setInt(5, range.getZHigh());
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            close(ps, rs, null);
        }
    }

    private void createIndexes() {
        Connection conn = null;
        Statement statement = null;
        try {
            conn = getSQLConnection();
            statement = conn.createStatement();
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_queue_world_x_z ON " + tables.get(0)
                    + " (" + COLUMNS.WORLD.name + ", " + COLUMNS.X.name + ", " + COLUMNS.Z.name + ")");
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    Error.close(BetterRTP.getInstance(), ex);
                }
            }
            close(null, null, conn);
        }
    }

    //Set a queue to save
    public QueueData addQueue(Location loc) {
        String pre = "INSERT INTO ";
        String sql = pre + tables.get(0) + " ("
                + COLUMNS.X.name + ", "
                + COLUMNS.Z.name + ", "
                + COLUMNS.WORLD.name + ", "
                + COLUMNS.GENERATED.name + " "
                //+ COLUMNS.USES.name + " "
                + ") VALUES(?, ?, ?, ?)";
        List<Object> params = new ArrayList<Object>() {{
                add(loc.getBlockX());
                add(loc.getBlockZ());
                add(loc.getWorld().getName());
                add(System.currentTimeMillis());
                //add(data.getUses());
        }};
        //return sqlUpdate(sql, params);
        int database_id = createQueue(sql, params);
        if (database_id >= 0)
            return new QueueData(loc, System.currentTimeMillis(), database_id);
        return null;
    }

    public int getCount() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + tables.get(0));

            rs = ps.executeQuery();
            count = rs.getFetchSize();
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, rs, conn);
        }
        return count;
    }

    private int createQueue(String statement, @NonNull List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        int id = -1;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
            Iterator<Object> it = params.iterator();
            int paramIndex = 1;
            while (it.hasNext()) {
                ps.setObject(paramIndex, it.next());
                paramIndex++;
            }
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            BetterRTP.getInstance().getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, null, conn);
        }
        return id;
    }

    public boolean removeLocation(Location loc) {
        String sql = "DELETE FROM " + tables.get(0) + " WHERE "
                + COLUMNS.X.name + " = ? AND "
                + COLUMNS.Z.name + " = ? AND "
                + COLUMNS.WORLD.name + " = ?"
                ;
        List<Object> params = new ArrayList<Object>() {{
            add(loc.getBlockX());
            add(loc.getBlockZ());
            add(loc.getWorld().getName());
        }};
        return sqlUpdate(sql, params);
    }

    public static class QueueRangeData {

        @Getter int xLow, xHigh;
        @Getter int zLow, zHigh;
        @Getter World world;

        public QueueRangeData(RTPWorld rtpWorld) {
            this.xLow = rtpWorld.getCenterX() - rtpWorld.getMaxRadius();
            this.xHigh = rtpWorld.getCenterX() + rtpWorld.getMaxRadius();
            this.zLow = rtpWorld.getCenterZ() - rtpWorld.getMaxRadius();
            this.zHigh = rtpWorld.getCenterZ() + rtpWorld.getMaxRadius();
            this.world = rtpWorld.getWorld();
        }

    }
}
