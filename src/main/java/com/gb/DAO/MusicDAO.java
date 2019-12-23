package com.gb.DAO;

import com.gb.modelObject.Music;
import com.gb.modelObject.SearchFilter;

import java.util.List;

public interface MusicDAO {

    /**
     * GET /music
     * GET su collezione --> Restituisce l'intera collezione
     */
    List<Music> getAllMusic(int page);

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
    List<Music> getMusicByParams(SearchFilter filter, int page);

    /**
     * GET su attributo/i di risorsa --> Non implementato. Scelta di progetto
     * legata alla decisione di restituire un certo formato di JSON sempre uguale
     */

    /**
     * PUT /music
     * PUT su collezione --> Aggiunge elementi alla collezione
     */
    int addManyMusic(List<Music> musicList);

    /**
     * PUT /music/:id
     * PUT su risorsa --> Aggiorna la risorsa
     */
    int updateOneMusic(Music music);

    /**
     * POST /music
     * POST su collezione --> Inserisce un elemento nella collezione
     */
    int addOneMusic(Music music);

    /**
     * POST su risorsa --> Non implementato. Generalmente non fatto
     */

    /**
     * DELETE su collezione --> Non implementato.
     */

    /**
     * DELETE /music/:id
     * DELETE su risorsa --> Elimina la risorsa
     */
    int deleteOneMusic(long id);

}
