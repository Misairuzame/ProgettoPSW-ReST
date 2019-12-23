package com.gb.restApp;

import static spark.Spark.*;

import com.gb.db.SQLiteJDBC.SQLiteJDBCImpl;
import com.gb.modelObject.Music;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static javax.ws.rs.core.MediaType.*;
import static com.gb.Constants.*;
import static com.gb.utils.JSONUtils.*;
import static com.gb.utils.UtilFunctions.*;
/**
 * Documentazione per le costanti rappresentanti gli stati HTTP, fornite da Apache HTTP:
 * http://hc.apache.org/httpcomponents-core-ga/httpcore/apidocs/org/apache/http/HttpStatus.html
 *
 * Costanti rappresentanti vari Media Type fornite da JAX-RS:
 * https://docs.oracle.com/javaee/7/api/javax/ws/rs/core/MediaType.html
 */

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void info(String toLog) {
        logger.info("Returned the following JSON:\n{}", toLog);
    }

    public static void main(String[] args) {

        port(8080);

        before(Main::logRequest);

        //staticFileLocation("/web");

        //get("/", Main::getHome);

        //get("/create", Main::createDB);

        before(Main::checkForTrailingSlash);

        path("/music", () -> {
            get("", Main::getMusic);

            get("/:id", Main::getMusicById);

            post("", Main::addOne);

            delete("/:id", Main::deleteOne);
        });

        get("/favicon.ico", Main::favicon);

        notFound(Main::handleNotFound);

    }

    private static void logRequest(Request req, Response res) {
        StringBuilder text = new StringBuilder();
        String message = "Received request: " + req.requestMethod() + " " + req.url();
        text.append(message);
        if(!req.queryParams().isEmpty()) {
            text.append("?");
            text.append(req.queryString());
        }
        logger.info(text.toString());
    }

    private static void checkForTrailingSlash(Request req, Response res) {
        String path = req.pathInfo();
        if (path.endsWith("/"))
            res.redirect(path.substring(0, path.length() - 1));
    }

    /*
    private static String createDB(Request req, Response res) {
        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        return "Ok";
    }
    */

    /*
    private static String getHome(Request req, Response res) {
        res.status(SC_OK);
        String jsonString = formatMessage("Benvenuto nella home!", SC_OK, SUCCESS);
        info(jsonString);
        return jsonString;
    }
    */

    private static String handleNotFound(Request req, Response res) {
        res.status(SC_NOT_FOUND);
        String jsonString = formatMessage("Risorsa o collezione non trovata.", SC_NOT_FOUND, FAILURE);
        info(jsonString);
        return jsonString;
    }

    private static String getMusic(Request req, Response res) {
        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        List<Music> musicList = db.getAllMusic();
        if (musicList == null) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        String msg = "";
        if(musicList.isEmpty()) {
            msg = "La lista di musica e' vuota.";
        } else msg = "Lista contenente tutta la musica sul database.";

        res.status(SC_OK);
        String jsonString = formatMusic(musicList, SC_OK, SUCCESS, msg);
        info(jsonString);
        return jsonString;
    }

    private static String getMusicById(Request req, Response res) {
        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        if(req.params("id") == null || !isNumber(req.params("id"))) {
            res.status(SC_BAD_REQUEST);
            String jsonString = formatMessage("Specificare un id nel formato corretto.",
                    SC_BAD_REQUEST, FAILURE);
            info(jsonString);
            return jsonString;
        }

        long musicId = Long.parseLong(req.params("id"));
        List<Music> musicList = db.getMusicById(musicId);
        if (musicList == null) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }
        if (musicList.isEmpty()) {
            return handleNotFound(req, res);
        }
        res.status(SC_OK);
        String jsonString = formatMusic(musicList, SC_OK, SUCCESS, "Musica con id " + musicId + ".");
        info(jsonString);
        return jsonString;
    }

    private static String addOne(Request req, Response res) {
        if(req.contentType() == null || !req.contentType().equals(APPLICATION_JSON)) {
            res.status(SC_UNSUPPORTED_MEDIA_TYPE);
            String jsonString = formatMessage("Unsupported media type.", SC_UNSUPPORTED_MEDIA_TYPE, FAILURE);
            info(jsonString);
            return jsonString;
        }

        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        Music musicToAdd = new Gson().fromJson(req.body(), Music.class);
        if(db.addOneMusic(musicToAdd) < 0) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        res.status(SC_OK);
        String jsonString = formatMessage("Musica con id "+musicToAdd.getId()+" aggiunta con successo.",
                    SC_OK, SUCCESS);
        info(jsonString);
        return jsonString;
    }

    private static String deleteOne(Request req, Response res) {
        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        if(req.params("id") == null || !isNumber(req.params("id"))) {
            res.status(SC_BAD_REQUEST);
            String jsonString = formatMessage("Specificare un id nel formato corretto.",
                    SC_BAD_REQUEST, FAILURE);
            info(jsonString);
            return jsonString;
        }

        long musicId = Long.parseLong(req.params("id"));
        if(db.deleteOneMusic(musicId) < 0) {
            res.status(SC_INTERNAL_SERVER_ERROR);
            String jsonString = formatMessage("Si e' verificato un errore.",
                    SC_INTERNAL_SERVER_ERROR, FAILURE);
            info(jsonString);
            return jsonString;
        }

        res.status(SC_OK);
        String jsonString = formatMessage("Musica con id "+musicId+" eliminata con successo.",
                SC_OK, SUCCESS);
        info(jsonString);
        return jsonString;

    }

    /**
     * Funzione per fornire l'iconcina  di fianco al titolo.
     * Adattato dalla seguente fonte:
     * @author hamishmorgan
     * https://github.com/hamishmorgan/ERL/blob/master/src/test/java/spark/SparkExamples.java
     */
    private static String favicon(Request req, Response res) {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(new FileInputStream(".\\favicon.ico"));
                out = new BufferedOutputStream(res.raw().getOutputStream());
                res.raw().setContentType("image/x-icon");
                res.status(200);
                ByteStreams.copy(in, out);
                out.flush();
                return "";
            } finally {
                Closeables.close(in, true);
            }
        } catch (FileNotFoundException ex) {
            logger.warn(ex.getMessage());
            res.status(SC_NOT_FOUND);
            return ex.getMessage();
        } catch (IOException ex) {
            logger.warn(ex.getMessage());
            res.status(SC_INTERNAL_SERVER_ERROR);
            return ex.getMessage();
        }
    }

}
