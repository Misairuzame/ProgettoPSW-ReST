package com.gb.db.SQLiteJDBC;

import com.gb.DAO.MusicDAO;
import com.gb.modelObject.Music;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.gb.Constants.*;

public class SQLiteJDBCImpl implements MusicDAO {

    private static Connection conn = null;
    private static SQLiteJDBCImpl Database = null;
    private static Logger logger = LoggerFactory.getLogger(SQLiteJDBCImpl.class);

    private SQLiteJDBCImpl() {

        try {
            conn = DriverManager.getConnection(DB_PATH);
            logger.info("Database connection created successfully.");

            String sql =
                    " CREATE TABLE IF NOT EXISTS "
                    + TABLE_NAME+ " ("
                    + ID        + " INTEGER PRIMARY KEY, "
                    + TITLE     + " TEXT NOT NULL, "
                    + AUTHOR    + " TEXT NOT NULL, "
                    + ALBUM     + " TEXT NOT NULL, "
                    + YEAR      + " INTEGER NOT NULL, "
                    + GENRE     + " TEXT NOT NULL, "
                    + URL       + " TEXT NOT NULL )";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Exception during SQLiteJDBCImpl constructor: " + e.getMessage());
            conn = null;
        }

    }

    public static synchronized SQLiteJDBCImpl getInstance() {

        if(Database == null) {
            Database = new SQLiteJDBCImpl();
            if (conn == null) {
                return null;
            }
        }
        return Database;

    }

    @Override
    public List<Music> getAllMusic() {

        List<Music> musicList = new ArrayList<>();

        String sql =
                " SELECT * " +
                " FROM " + TABLE_NAME;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    musicList.add(new Music(rs));
                }
            }
            return musicList;
        } catch (SQLException e) {
            logger.error("Error in getAllMusic: {}", e.getMessage());
            return null;
        }

    }

    @Override
    public List<Music> getMusicById(long id) {

        List<Music> musicList = new ArrayList<>();

        String sql =
                " SELECT * " +
                " FROM "  + TABLE_NAME +
                " WHERE " + ID + " = ? ";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    musicList.add(new Music(rs));
                }
            }
            return musicList;
        } catch (SQLException e) {
            logger.error("Error in getMusicById: {}", e.getMessage());
            return null;
        }

    }

    @Override
    public List<Music> getMusicByParams(Object... params) {
        //TODO: Implementare
        return null;
    }

    @Override
    public int addOneMusic(Music music) {

        String check =
                " SELECT COUNT(*) " +
                " FROM "  + TABLE_NAME +
                " WHERE " + ID + " = ? ";

        boolean taken = false;

        try (PreparedStatement pStat = conn.prepareStatement(check)) {

            pStat.setLong(1, music.getId());

            try (ResultSet rs = pStat.executeQuery()) {
                if (rs.next()) {
                    taken = rs.getInt(1) > 0;
                }
                if (taken) {
                    logger.info("Esiste gia' una canzone con id {}, impossibile crearne una nuova.", music.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Exception in addOneMusic: " + e.getMessage());
            return -1;
        }


        String sql =
                " INSERT INTO " + TABLE_NAME +
                " ( " + ID + ", " + TITLE + ", " + AUTHOR + ", " + ALBUM + ", " +
                YEAR + ", " + GENRE + ", " + URL + " ) " + " VALUES " +
                "(?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, music.getId());
            ps.setString(2, music.getTitle());
            ps.setString(3, music.getAuthor());
            ps.setString(4, music.getAlbum());
            ps.setInt(5, music.getYear());
            ps.setString(6, music.getGenre());
            ps.setString(7, music.getUrl());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception in addOneMusic: " + e.getMessage());
            return -2;
        }

        return 0;
    }

    @Override
    public int updateOneMusic(Music music) {
        //TODO: Implementare
        return 0;
    }

    @Override
    public int deleteOneMusic(long id) {

        String sql =
                " DELETE FROM " + TABLE_NAME +
                " WHERE " + ID + " = ? ";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            return 0;
        } catch (SQLException e) {
            logger.error("Error in DeleteOneMusic: {}", e.getMessage());
            return -1;
        }
    }

}
