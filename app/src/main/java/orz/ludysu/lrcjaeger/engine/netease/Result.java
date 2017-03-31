
package orz.ludysu.lrcjaeger.engine.netease;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {

    @SerializedName("songCount")
    @Expose
    private Integer songCount;
    @SerializedName("songs")
    @Expose
    private List<Song> songs = null;

    public Integer getSongCount() {
        return songCount;
    }

    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

}
