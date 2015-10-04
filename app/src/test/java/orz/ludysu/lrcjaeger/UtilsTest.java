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

    @Test
    public void getFileName() {
        String res = Utils.getFileNameWithoutExtension("/storage/sdcard0/images/abc.pic");
        assertEquals("abc", res);

        res = Utils.getFileNameWithoutExtension("/storage/sdcard0/images/abc");
        assertEquals("abc", res);

        res = Utils.getFileNameWithoutExtension("/storage/sdcard0/im.ages/abc.");
        assertEquals("abc", res);
    }
}
