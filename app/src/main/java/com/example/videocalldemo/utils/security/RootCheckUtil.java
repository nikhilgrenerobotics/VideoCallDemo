package com.example.videocalldemo.utils.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RootCheckUtil {
    public static boolean isRunningInRootedDevice() {
        return checkMethod1() || checkMethod2() || checkMethod3();
    }

    private static boolean checkMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkMethod2() {
        String superUser = "/system/app/Superuser.apk";
        String sys_bin_su = "/system/bin/su";
        String sbin_su = "/sbin/su";
        String xbin_su = "/system/xbin/su";
        String local_xbin_su = "/data/local/xbin/su";
        String local_bin_su = "/data/local/bin/su";
        String sd_xbin_su = "/system/sd/xbin/su";
        String failsafe_su = "/system/bin/failsafe/su";
        String data_local_su = "/data/local/su";
        String su_bin_su = "/su/bin/su";

        String[] paths = {superUser, sbin_su, sys_bin_su, xbin_su, local_xbin_su, local_bin_su, sd_xbin_su, failsafe_su, data_local_su, su_bin_su};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}
