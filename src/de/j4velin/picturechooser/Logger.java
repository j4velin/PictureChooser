package de.j4velin.picturechooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.database.Cursor;
import android.os.Environment;

public class Logger {

	public final static boolean LOG = false;

	private static FileWriter fw;
	private static Date date = new Date();
	private final static String APP = "GalleryLib";

	public static void log(Throwable ex) {
		log(ex.getMessage());
		for (StackTraceElement ste : ex.getStackTrace()) {
			log(ste.toString());
		}
	}

	public static void log(final Cursor c) {
		if (!Logger.LOG)
			return;
		c.moveToFirst();
		String title = "";
		for (int i = 0; i < c.getColumnCount(); i++)
			title += c.getColumnName(i) + " | ";
		log(title);
		while (!c.isAfterLast()) {
			title = "";
			for (int i = 0; i < c.getColumnCount(); i++)
				title += c.getString(i) + " | ";
			log(title);
			c.moveToNext();
		}
	}

	@SuppressWarnings("deprecation")
	public static void log(String msg) {
		if (!Logger.LOG)
			return;
		if (BuildConfig.DEBUG)
			android.util.Log.d(APP, msg);
		else {
			try {
				if (fw == null) {
					fw = new FileWriter(new File(Environment.getExternalStorageDirectory().toString() + "/" + APP + ".log"), true);
				}
				date.setTime(System.currentTimeMillis());
				fw.write(date.toLocaleString() + " - " + msg + "\n");
				fw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void finalize() throws Throwable {
		try {
			if (fw != null)
				fw.close();
		} finally {
			super.finalize();
		}
	}

}
