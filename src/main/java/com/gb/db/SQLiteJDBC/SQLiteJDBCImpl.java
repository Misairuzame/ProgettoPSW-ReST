package com.gb.db.SQLiteJDBC;

import com.gb.DAO.MusicDAO;
import com.gb.modelObject.Music;
import com.gb.modelObject.SearchFilter;
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
    public List<Music> getAllMusic(int page) {

        List<Music> musicList = new ArrayList<>();

        String sql =
                " SELECT * " +
                " FROM " + TABLE_NAME +
                " LIMIT ? OFFSET ? ";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,PAGE_SIZE);
            ps.setInt(2,page*PAGE_SIZE);
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
    public List<Music> getMusicByParams(SearchFilter filter, int page) {

        List<Music> musicList = new ArrayList<>();

        boolean[] params = new boolean[5];
        for(int i=0; i<params.length; i++) {
            params[i] = false;
        }
        int index = 0;

        String sql = " SELECT * FROM " + TABLE_NAME + " WHERE ";
        if(filter.getTitle() != null && !filter.getTitle().equals("")) {
            sql += TITLE + " LIKE ? AND ";
            params[index] = true;
        }
        index++;
        if(filter.getAuthor() != null && !filter.getAuthor().equals("")) {
            sql += AUTHOR + " LIKE ? AND ";
            params[index] = true;
        }
        index++;
        if(filter.getAlbum() != null && !filter.getAlbum().equals("")) {
            sql += ALBUM + " LIKE ? AND ";
            params[index] = true;
        }
        index++;
        if(filter.getYear() != null && !filter.getYear().equals("")) {
            sql += YEAR + " = ? AND ";
            params[index] = true;
        }
        index++;
        if(filter.getGenre() != null && !filter.getGenre().equals("")) {
            sql += GENRE + " LIKE ? AND ";
            params[index] = true;
        }
        sql = sql.substring(0, sql.lastIndexOf("AND "));
        sql += " LIMIT ? OFFSET ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)) {
            int i=1;
            for(int j=0; j<params.length; j++) {
                if(params[j]) {
                    switch (j) {
                        case 0:
                            ps.setString(i, "%"+filter.getTitle()+"%");
                            break;
                        case 1:
                            ps.setString(i, "%"+filter.getAuthor()+"%");
                            break;
                        case 2:
                            ps.setString(i, "%"+filter.getAlbum()+"%");
                            break;
                        case 3:
                            ps.setInt(i, Integer.parseInt(filter.getYear()));
                            break;
                        case 4:
                            ps.setString(i, "%"+filter.getGenre()+"%");
                            break;
                    }
                    i++;
                }
            }
            ps.setInt(i, PAGE_SIZE); i++;
            ps.setInt(i, page*PAGE_SIZE);

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    musicList.add(new Music(rs));
                }
                return musicList;
            }
        } catch (Exception e) {
            logger.error("Exception in getMusicByParams: " + e.getMessage());
            return null;
        }

    }

    @Override
    public int addOneMusic(Music music) {

        String check =
                " SELECT COUNT(*) " +
                " FROM "  + TABLE_NAME +
                " WHERE " + ID + " = ? ";

        boolean exists = false;

        try (PreparedStatement pStat = conn.prepareStatement(check)) {

            pStat.setLong(1, music.getId());

            try (ResultSet rs = pStat.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
                if (exists) {
                    logger.info("Esiste gia' una canzone con id {}, impossibile crearne una nuova.", music.getId());
                    return -1;
                }
            }
        } catch (SQLException e) {
            logger.error("Exception in addOneMusic: " + e.getMessage());
            return -2;
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
    public int addManyMusic(List<Music> musicList) {

        try{
            conn.setAutoCommit(false);
            for(Music music : musicList) {
                if(addOneMusic(music) < 0) {
                    throw new SQLException("Errore in addManyMusic interno a addOneMusic.");
                }
            }
            conn.commit();
        } catch(SQLException e) {
            logger.error("Exception in addManyMusic: " + e.getMessage());
            try {
                conn.rollback();
            } catch(SQLException ex) {
                logger.error("Exception in addManyMusic: " + ex.getMessage());
            }
            return -1;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.error("Exception in addManyMusic: " + ex.getMessage());
            }
        }
        return 0;
    }

    @Override
    public int updateOneMusic(Music music) {

        String check =
                " SELECT COUNT(*) " +
                " FROM "  + TABLE_NAME +
                " WHERE " + ID + " = ? ";

        boolean exists = false;

        try (PreparedStatement pStat = conn.prepareStatement(check)) {

            pStat.setLong(1, music.getId());

            try (ResultSet rs = pStat.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
                if (!exists) {
                    logger.info("La canzone con id {} non esiste, impossibile aggiornarla.", music.getId());
                    return -1;
                }
            }
        } catch (SQLException e) {
            logger.error("Exception in updateOneMusic: " + e.getMessage());
            return -2;
        }

        String sql =
                " UPDATE " + TABLE_NAME + " SET " +
                TITLE + " = ?, " + AUTHOR + " = ?, " + ALBUM + " = ?, " +
                YEAR + " = ?, "  + GENRE  + " = ?, " + URL   + " = ? "  +
                " WHERE " + ID + " = ? ";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, music.getTitle());
            ps.setString(2, music.getAuthor());
            ps.setString(3, music.getAlbum());
            ps.setInt(4, music.getYear());
            ps.setString(5, music.getGenre());
            ps.setString(6, music.getUrl());
            ps.setLong(7, music.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception in updateOneMusic: " + e.getMessage());
            return -2;
        }

        return 0;
    }

    @Override
    public int deleteOneMusic(long id) {

        String check =
                " SELECT COUNT(*) " +
                " FROM "  + TABLE_NAME +
                " WHERE " + ID + " = ? ";

        boolean exists = false;

        try (PreparedStatement pStat = conn.prepareStatement(check)) {

            pStat.setLong(1, id);

            try (ResultSet rs = pStat.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
                if (!exists) {
                    logger.info("La canzone con id {} non esiste, impossibile eliminarla.", id);
                    return -1;
                }
            }
        } catch (SQLException e) {
            logger.error("Exception in updateOneMusic: " + e.getMessage());
            return -2;
        }

        String sql =
                " DELETE FROM " + TABLE_NAME +
                " WHERE " + ID  + " = ? ";

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
