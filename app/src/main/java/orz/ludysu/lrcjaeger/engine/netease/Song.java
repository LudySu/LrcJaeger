
package orz.ludysu.lrcjaeger.engine.netease;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Song {

    @SerializedName("album")
    @Expose
    private Album album;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("copyrightId")
    @Expose
    private Integer copyrightId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("mvid")
    @Expose
    private Integer mvid;
    @SerializedName("alias")
    @Expose
    private List<Object> alias = null;
    @SerializedName("artists")
    @Expose
    private List<Artist> artists = null;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("id")
    @Expose
    private Integer id;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCopyrightId() {
        return copyrightId;
    }

    public void setCopyrightId(Integer copyrightId) {
        this.copyrightId = copyrightId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMvid() {
        return mvid;
    }

    public void setMvid(Integer mvid) {
        this.mvid = mvid;
    }

    public List<Object> getAlias() {
        return alias;
    }

    public void setAlias(List<Object> alias) {
        this.alias = alias;
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
