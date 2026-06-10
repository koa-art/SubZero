package com.subzero.app.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.subzero.app.db.StorageManager;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportHelper {
    public static boolean exportToFile(Context context) {
        try {
            StorageManager store = StorageManager.getInstance(context);
            String json = store.exportToJson();

            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "SubZero");
            if (!dir.exists()) dir.mkdirs();

            String filename = "subzero_backup_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".json";
            File file = new File(dir, filename);

            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();

            Toast.makeText(context, "数据已导出到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
