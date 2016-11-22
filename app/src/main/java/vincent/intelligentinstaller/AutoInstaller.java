package vincent.intelligentinstaller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by vincent on 2016/11/21.
 * email-address:674928145@qq.com
 * description:
 */

public class AutoInstaller {

    private Context mContext;
    private File mFile;

    public AutoInstaller(Context context, File file) {
        this.mContext = context;
        this.mFile = file;
    }

    public void runInstaller() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!installByRoot()) {//root的静默安装失败,进行非root的智能安装
                    if (!isAccessibilitySettingOn()) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        mContext.startActivity(intent);
                        showTips();
                        // TODO: 2016/11/22  询问是否开启辅助供能，如果不开启辅助功能，就直接进行普通安装
                    } else {
                        installByCommon();
                    }
                }
            }
        }).start();
    }

    public boolean isAccessibilitySettingOn() {
        int accessibility = 0;
        String serviceName = mContext.getPackageName() + "/" + ApkService.class.getCanonicalName();
        try {
            accessibility = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (accessibility == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            String[] arr = settingValue.split(":");
            if (arr != null && arr.length > 0) {
                for (String name : arr) {
                    if (serviceName.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * root状态下用静默安装
     *
     * @return
     */
    private boolean installByRoot() {
        boolean result = false;
        Process process = null;
        OutputStream outputStream = null;
        InputStream errorStream = null;
        BufferedReader bufferedReader;
        try {
            process = Runtime.getRuntime().exec("su");
            outputStream = process.getOutputStream();
            String cmd = "pm install -r" + mFile.getAbsolutePath() + "\n";
            outputStream.write(cmd.getBytes());
            outputStream.flush();
            outputStream.write("exit\n".getBytes());
            process.waitFor();//阻塞式函数
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            errorStream = process.getErrorStream();
            bufferedReader = new BufferedReader(new InputStreamReader(errorStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            if (!stringBuilder.toString().contains("Failure")) {
                result = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                process.destroy();
            }
            if (errorStream != null) try {
                errorStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                process.destroy();
            }
        }
        return result;
    }

    /**
     * 普通安装，如果已经开启了辅助服务，apkservice监测安装界面，隐式启动，模拟点击安装等按钮
     */
    public void installByCommon() {
        Uri uri = Uri.fromFile(mFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    private void showTips() {
        new Handler(mContext.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "请打开辅助/无障碍功能里面的开关以便以后实现智能安装", Toast.LENGTH_LONG).show();
            }
        });
    }
}
