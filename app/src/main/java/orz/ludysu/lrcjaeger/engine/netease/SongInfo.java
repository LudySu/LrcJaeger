
package orz.ludysu.lrcjaeger.engine.netease;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SongInfo {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("result")
    @Expose
    private Result result;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

}
