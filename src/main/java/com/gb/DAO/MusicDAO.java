package com.gb.DAO;

import com.gb.modelObject.Music;
import java.util.List;

public interface MusicDAO {

    List<Music> getAllMusic();

    Music getMusicById(long id);

    int addOneMusic(Music music);

}
