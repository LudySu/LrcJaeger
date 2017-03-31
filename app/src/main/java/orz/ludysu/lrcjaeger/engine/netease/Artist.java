
package orz.ludysu.lrcjaeger.engine.netease;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Artist {

    @SerializedName("alias")
    @Expose
    private List<Object> alias = null;
    @SerializedName("picUrl")
    @Expose
    private Object picUrl;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;

    public List<Object> getAlias() {
        return alias;
    }

    public void setAlias(List<Object> alias) {
        this.alias = alias;
    }

    public Object getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(Object picUrl) {
        this.picUrl = picUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
