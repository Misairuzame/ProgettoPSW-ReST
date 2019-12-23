package com.gb.restApp;

import static spark.Spark.*;
import com.gb.db.SQLiteJDBC.SQLiteJDBCImpl;
import com.gb.modelObject.Music;
import com.gb.modelObject.SearchFilter;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import java.io.*;
import java.lang.reflect.Type;
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

        before(Main::applyFilters);

        get("/", Main::getHomepage);

        path("/music", () -> {
            get("",  Main::getMusic);

            put("",  Main::addMany);
            put("/", Main::addMany);

            post("",  Main::addOne);
            post("/", Main::addOne);

            delete("",  Main::deleteAll);
            delete("/", Main::deleteAll);

            get("/:id", Main::getMusicById);

            put("/:id", Main::updateOne);

            delete("/:id", Main::deleteOne);
        });

        get("/favicon.ico", Main::favicon);

        notFound(Main::handleNotFound);

    }

    private static void applyFilters(Request req, Response res) {
        /**
         * Toglie lo slash finale, se presente.
         * Il redirect funziona solamente con richieste GET,
         * motivo per cui viene fatto il "doppio matching"
         * nel metodo main.
         */
        String path = req.pathInfo();
        if (req.requestMethod().equals("GET") && path.endsWith("/") && !path.equals("/")) {
            res.redirect(path.substring(0, path.length() - 1));
        }

        /**
         * Mette il content-type della Response a "application/json"
         */
        res.raw().setContentType(APPLICATION_JSON);

        /**
         * Logga la Request
         */
        StringBuilder text = new StringBuilder();
        String message = "Received request: " + req.requestMethod() + " " + req.url();
        text.append(message);
        if(!req.queryParams().isEmpty()) {
            text.append("?");
            text.append(req.queryString());
        }
        if(req.headers(CONT_TYPE) != null && !req.headers(CONT_TYPE).equals("")) {
            text.append("\n");
            text.append("Request content-type:\n");
            text.append(req.headers(CONT_TYPE));
        }
        if(req.body() != null && !req.body().equals("")) {
            text.append("\n");
            text.append("Request body:\n");
            text.append(req.body());
        }
        logger.info(text.toString());
    }

    private static String handleNotFound(Request req, Response res) {
        res.status(SC_NOT_FOUND);
        String jsonString = formatMessage("Risorsa o collezione non trovata.",
                SC_NOT_FOUND, FAILURE);
        info(jsonString);
        return jsonString;
    }

    private static String handleInternalError(Request req, Response res) {
        res.status(SC_INTERNAL_SERVER_ERROR);
        String jsonString = formatMessage("Si e' verificato un errore.",
                SC_INTERNAL_SERVER_ERROR, FAILURE);
        info(jsonString);
        return jsonString;
    }

    private static String getHomepage(Request req, Response res) {
        res.status(SC_OK);
        String jsonString = formatMessage("Benvenuto nella ReST API Music.",
                SC_OK, SUCCESS);
        info(jsonString);
        return jsonString;
    }

    private static String getMusic(Request req, Response res) {
        int pageNum = 0;

        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            return handleInternalError(req, res);
        }

        if(req.queryParams("page") != null) {
            if(!isNumber(req.queryParams("page"))) {
                res.status(SC_BAD_REQUEST);
                String jsonString = formatMessage("Specificare la pagina in maniera corretta.",
                        SC_BAD_REQUEST, FAILURE);
                info(jsonString);
                return jsonString;
            } else {
                pageNum = Integer.parseInt(req.queryParams("page"));
            }
        }

        /**
         * Parte di codice che permette di cercare musica
         * secondo diversi parametri
         */
        boolean hasTitle = req.queryParams("title") != null;
        boolean hasAuthor = req.queryParams("author") != null;
        boolean hasAlbum = req.queryParams("album") != null;
        boolean hasYear = req.queryParams("year") != null;
        boolean hasGenre = req.queryParams("genre") != null;
        List<Music> musicList;
        if( hasTitle || hasAuthor || hasAlbum ||
            hasYear  || hasGenre) {
            SearchFilter filter = new SearchFilter();
            filter.setTitle(req.queryParams("title"));
            filter.setAuthor(req.queryParams("author"));
            filter.setAlbum(req.queryParams("album"));
            filter.setYear(req.queryParams("year"));
            filter.setGenre(req.queryParams("genre"));

            musicList = db.getMusicByParams(filter, pageNum);
        } else {
            musicList = db.getAllMusic(pageNum);
        }
        if (musicList == null) {
            return handleInternalError(req, res);
        }

        String msg;
        if (musicList.isEmpty()) {
            msg = "La lista di musica in questa pagina e' vuota.";
        } else msg = "Lista contenente musica sul database (pagina "+pageNum+").";

        res.status(SC_OK);
        String jsonString = formatMusic(musicList, SC_OK, SUCCESS, msg);
        info(jsonString);
        return jsonString;
    }

    private static String getMusicById(Request req, Response res) {
        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            return handleInternalError(req, res);
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
            return handleInternalError(req, res);
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
            return handleInternalError(req, res);
        }

        Music musicToAdd = new Gson().fromJson(req.body(), Music.class);
        if(musicToAdd == null) {
            return handleInternalError(req, res);
        }
        int result = db.addOneMusic(musicToAdd);
        if(result < 0) {
            if(result == -2) {
                return handleInternalError(req, res);
            }
            if(result == -1) {
                res.status(SC_CONFLICT);
                String jsonString = formatMessage("Esiste gia' una musica con id "+musicToAdd.getId()+".",
                        SC_CONFLICT, FAILURE);
                info(jsonString);
                return jsonString;
            }
        }

        res.status(SC_CREATED);
        String jsonString = formatMessage("Musica con id "+musicToAdd.getId()+" aggiunta con successo.",
                    SC_CREATED, SUCCESS);
        info(jsonString);
        return jsonString;
    }

    private static String addMany(Request req, Response res) {
        if(req.contentType() == null || !req.contentType().equals(APPLICATION_JSON)) {
            res.status(SC_UNSUPPORTED_MEDIA_TYPE);
            String jsonString = formatMessage("Unsupported media type.", SC_UNSUPPORTED_MEDIA_TYPE, FAILURE);
            info(jsonString);
            return jsonString;
        }

        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            return handleInternalError(req, res);
        }

        Type listType = new TypeToken<List<Music>>() {}.getType();
        List<Music> musicToAdd = new Gson().fromJson(req.body(), listType);
        if(musicToAdd == null) {
            return handleInternalError(req, res);
        }
        int result = db.addManyMusic(musicToAdd);
        if(result < 0) {
            if(result == -1) {
                return handleInternalError(req, res);
            }
        }

        res.status(SC_CREATED);
        String jsonString = formatMessage("Musica/musiche aggiunta/e con successo.",
                SC_CREATED, SUCCESS);
        info(jsonString);
        return jsonString;
    }

    private static String updateOne(Request req, Response res) {
        if(req.contentType() == null || !req.contentType().equals(APPLICATION_JSON)) {
            res.status(SC_UNSUPPORTED_MEDIA_TYPE);
            String jsonString = formatMessage("Unsupported media type.", SC_UNSUPPORTED_MEDIA_TYPE, FAILURE);
            info(jsonString);
            return jsonString;
        }

        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            return handleInternalError(req, res);
        }

        Music musicToUpdate = new Gson().fromJson(req.body(), Music.class);
        if(musicToUpdate == null) {
            return handleInternalError(req, res);
        }
        int result = db.updateOneMusic(musicToUpdate);
        if(result < 0) {
            if(result == -2) {
                return handleInternalError(req, res);
            }
            if(result == -1) {
                res.status(SC_BAD_REQUEST);
                String jsonString = formatMessage("Non esiste una musica con id "+musicToUpdate.getId()+", " +
                                "impossibile aggiornarla.", SC_BAD_REQUEST, FAILURE);
                info(jsonString);
                return jsonString;
            }
        }

        res.status(SC_OK);
        String jsonString = formatMessage("Musica con id "+musicToUpdate.getId()+" modificata con successo.",
                SC_OK, SUCCESS);
        info(jsonString);
        return jsonString;
    }

    private static String deleteOne(Request req, Response res) {
        SQLiteJDBCImpl db = SQLiteJDBCImpl.getInstance();
        if (db == null) {
            return handleInternalError(req, res);
        }

        if(req.params("id") == null || !isNumber(req.params("id"))) {
            res.status(SC_BAD_REQUEST);
            String jsonString = formatMessage("Specificare un id nel formato corretto.",
                    SC_BAD_REQUEST, FAILURE);
            info(jsonString);
            return jsonString;
        }

        long musicId = Long.parseLong(req.params("id"));
        int result = db.deleteOneMusic(musicId);
        if(result < 0) {
            if(result == -2) {
                return handleInternalError(req, res);
            }
            if(result == -1) {
                res.status(SC_BAD_REQUEST);
                String jsonString = formatMessage("Non esiste una musica con id "+musicId+", " +
                        "impossibile eliminarla.", SC_BAD_REQUEST, FAILURE);
                info(jsonString);
                return jsonString;
            }
        }

        res.status(SC_OK);
        String jsonString = formatMessage("Musica con id "+musicId+" eliminata con successo.",
                SC_OK, SUCCESS);
        info(jsonString);
        return jsonString;

    }

    private static String deleteAll(Request req, Response res) {
        res.status(SC_FORBIDDEN);
        String jsonString = formatMessage("L'eliminazione dell'intera collezione e' vietata.",
                SC_FORBIDDEN, SUCCESS);
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
