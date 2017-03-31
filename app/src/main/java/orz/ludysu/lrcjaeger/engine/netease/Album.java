
package orz.ludysu.lrcjaeger.engine.netease;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Album {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("copyrightId")
    @Expose
    private Integer copyrightId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("artist")
    @Expose
    private Artist artist;
    @SerializedName("publishTime")
    @Expose
    private Integer publishTime;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("size")
    @Expose
    private Integer size;

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

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Integer getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Integer publishTime) {
        this.publishTime = publishTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
