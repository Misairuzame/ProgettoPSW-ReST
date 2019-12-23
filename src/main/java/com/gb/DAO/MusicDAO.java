package com.gb.DAO;

import com.gb.modelObject.Music;
import java.util.List;

public interface MusicDAO {

    /**
     * GET /music
     * GET su collezione --> Restituisce l'intera collezione
     * TODO: Paginazione? start, end per definire LIMIT / OFFSET
     */
    List<Music> getAllMusic();

    /**
     * GET /music/:id
     * GET su risorsa --> Restituisce una rappresentazione della risorsa
     */
    List<Music> getMusicById(long id);

    /**
     * GET /music?params
     * GET su collezione con parametri --> Restituisce una rappresentazione
     * delle risorse con determinati parametri.
     */
    List<Music> getMusicByParams(Object... params);

    //TODO: GET su attributo/i di risorsa --> Implementare?

    /**
     * POST /music
     * POST su collezione --> Inserisce un elemento nella collezione
     */
    int addOneMusic(Music music);

    /**
     * POST su risorsa --> Non implementato. Generalmente non fatto
     */

    //TODO: PUT su collezione --> Rimpiazzare l'intera collezione o aggiungere elementi??? In ogni caso mettere autoCommit a false

    /**
     * PUT /music/:id
     * PUT su risorsa --> Aggiorna la risorsa
     */
    int updateOneMusic(Music music);

    /**
     * DELETE su collezione --> Non implementato.
     */

    /**
     * DELETE /music/:id
     * DELETE su risorsa --> Elimina la risorsa
     */
    int deleteOneMusic(long id);

}
