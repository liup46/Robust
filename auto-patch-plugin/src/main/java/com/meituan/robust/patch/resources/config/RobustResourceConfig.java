package com.meituan.robust.patch.resources.config;

import com.meituan.robust.common.FileUtil;
import com.meituan.robust.common.StringUtil;
import com.meituan.robust.patch.resources.APKStructure;
import com.meituan.robust.patch.resources.diff.APKDiffUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Created by hedingxu on 17/6/2.
 */
public class RobustResourceConfig {

    // case : app/build/outputs/robust/
    public String robustOutputsFolder;

    // case : app/build/outputs/robust/resources
    public static final String RESOURCE_OUTPUTS = "resources";

    // case : app/build/outputs/robust/resources
    public File apkDiffOutDir;

    // case : app/build/outputs/robust/apk_name_old
    public File oldApkUnZipDir;
    // case : app/build/outputs/robust/apk_name_new
    public File newApkUnZipDir;

    // case : app/build/outputs/robust/patch_resources.apk
    public static final String PATCH_RESOURCES_APK = "patch_resources.apk";

    public static final String APK_SUFFIX = ".apk";
    public static final int BUFFER_SIZE = 8 * 1024;

    public String oldApkPath;
    public String newApkPath;

    public File oldApkFile;
    public File newApkFile;

    // default big file size floor
    public int bigFileSizeAtLeast = 100 * 1024;

    public HashSet<Pattern> resIncludePatterns;
    public HashSet<Pattern> resExcludePatterns;

    public HashSet<Pattern> libIncludePatterns;
    public HashSet<Pattern> libExcludePatterns;

    public HashSet<Pattern> assetsIncludePatterns;
    public HashSet<Pattern> assetsExcludePatterns;


    public RobustResourceConfig(RobustXmlResourceInfo robustXmlResourceInfo) {
        resIncludePatterns = strings2Patterns(robustXmlResourceInfo.resIncludeStrings);
        resExcludePatterns = strings2Patterns(robustXmlResourceInfo.resExcludeStrings);

        libIncludePatterns = strings2Patterns(robustXmlResourceInfo.libIncludeStrings);
        libExcludePatterns = strings2Patterns(robustXmlResourceInfo.libExcludeStrings);

        assetsIncludePatterns = strings2Patterns(robustXmlResourceInfo.assetsIncludeStrings);
        assetsExcludePatterns = strings2Patterns(robustXmlResourceInfo.assetsExcludeStrings);

        bigFileSizeAtLeast = robustXmlResourceInfo.bigFileSizeAtLeast;

        oldApkPath = robustXmlResourceInfo.oldApkPath;
        oldApkFile = new File(oldApkPath);

        newApkPath = robustXmlResourceInfo.newApkPath;
        newApkFile = new File(newApkPath);
        robustOutputsFolder = robustXmlResourceInfo.robustOutputDirPath;
        FileUtil.deleteAllFile(robustOutputsFolder);
        configOutputDirectory();
    }

    private void configOutputDirectory() {
        apkDiffOutDir = new File(robustOutputsFolder + File.separator + RESOURCE_OUTPUTS);
        FileUtil.deleteAllFile(apkDiffOutDir.getAbsolutePath());
        apkDiffOutDir.mkdirs();

        String oldApkName = oldApkFile.getName();
        if (!oldApkName.endsWith(APK_SUFFIX)) {
            return;
        }

        String newApkName = newApkFile.getName();
        if (!newApkName.endsWith(APK_SUFFIX)) {
            return;
        }

        String tempOldName = oldApkName.substring(0, oldApkName.indexOf(APK_SUFFIX));


        String tempNewName = newApkName.substring(0, newApkName.indexOf(APK_SUFFIX));

        if (tempNewName.equalsIgnoreCase(tempOldName)) {
            tempOldName += "_old";
            tempNewName += "_new";
        }

        oldApkUnZipDir = new File(robustOutputsFolder, tempOldName);
        newApkUnZipDir = new File(robustOutputsFolder, tempNewName);
    }

    private static HashSet<Pattern> strings2Patterns(HashSet<String> strings) {
        HashSet<Pattern> patterns = new HashSet<Pattern>();
        if (strings == null || strings.size() == 0) {
            return patterns;
        }
        for (String str : strings) {
            str = StringUtil.trim(str);
            String patternString = StringUtil.convertToPatternString(str);
            Pattern pattern = Pattern.compile(patternString);
            patterns.add(pattern);
        }
        return patterns;
    }

    /**
     * For test
     *
     * @param args
     */
    public static void main(String[] args) {

        RobustXmlResourceInfo xmlResourceInfo = new RobustXmlResourceInfo();

        //todo
//        String oldApkPath = "/Users/hedingxu/Downloads/aimeituan_513_huawei.apk";
        String oldApkPath = "/Users/hedingxu/Downloads/aimeituan-stage-6661.apk";
        String newApkPath = "/Users/hedingxu/Downloads/aimeituan_513_lenovo.apk";
        HashSet<String> assetsExcludeStrings = new HashSet<>();
        assetsExcludeStrings.add(APKStructure.Assets_Type + "/robust.apkhash");
        HashSet<String> assetsIncludeStrings = new HashSet<>();
        assetsIncludeStrings.add(APKStructure.Assets_Type + "/*");
        HashSet<String> resExcludeStrings = new HashSet<>();
        HashSet<String> resIncludeStrings = new HashSet<>();
        resIncludeStrings.add(APKStructure.Res_Type + "/*");
        HashSet<String> libExcludeStrings = new HashSet<>();
        HashSet<String> libIncludeStrings = new HashSet<>();
        libIncludeStrings.add(APKStructure.Lib_Type + "/*");

        xmlResourceInfo.oldApkPath = oldApkPath;
        xmlResourceInfo.newApkPath = newApkPath;
        xmlResourceInfo.robustOutputDirPath = "/Users/hedingxu/robust-github/Robust/app/build/outputs/robust";
        xmlResourceInfo.assetsExcludeStrings = assetsExcludeStrings;
        xmlResourceInfo.assetsIncludeStrings = assetsIncludeStrings;
        xmlResourceInfo.resExcludeStrings = resExcludeStrings;
        xmlResourceInfo.resIncludeStrings = resIncludeStrings;
        xmlResourceInfo.libExcludeStrings = libExcludeStrings;
        xmlResourceInfo.libIncludeStrings = libIncludeStrings;

        RobustResourceConfig config = new RobustResourceConfig(xmlResourceInfo);
        try {
            long time = System.currentTimeMillis();
            APKDiffUtils.execute(config);
            System.out.println("APKDiffUtils.execute(config) spend:" + (System.currentTimeMillis() - time));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

