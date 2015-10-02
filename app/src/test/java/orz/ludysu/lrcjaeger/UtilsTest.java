package orz.ludysu.lrcjaeger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void getFolder() {
        String res = Utils.getFolder("/storage/sdcard0/images/");
        assertEquals("/storage/sdcard0/images", res);

        res = Utils.getFolder("/storage/sdcard0/images/aa.pic");
        assertEquals("/storage/sdcard0/images", res);
    }
}
