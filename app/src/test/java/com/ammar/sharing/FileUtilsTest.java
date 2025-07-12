package com.ammar.sharing;


import com.ammar.sharing.common.utils.FileUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileUtilsTest {
    @Test
    public void testFilesUtils() {
        // test child dir
        File file1 = new File("/storage/emulated/0");
        File file2 = new File("/storage/emulated/0/Download");
        int depth1 = FileUtils.getFileDepthInDir(file1, file2);
        Assert.assertEquals(1, depth1);

        // test different dirs
        File file3 = new File("/storage/emulated/0/Pictures");
        int depth2 = FileUtils.getFileDepthInDir(file2, file3);
        Assert.assertEquals(-1, depth2);

        // test same dir
        int depth3 = FileUtils.getFileDepthInDir(file1, file1);
        Assert.assertEquals(0, depth3);

        File file4 = new File("/storage/emulated/0/Download/folder");
        int depth4 = FileUtils.getFileDepthInDir(file1, file4);
        Assert.assertEquals(2, depth4);


    }
}
