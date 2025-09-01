/*
 Privacy Friendly Finance Manager is licensed under the GPLv3.
 Copyright (C) 2023 MaxIsV, k3b

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 General Public License as published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this program.
 If not, see http://www.gnu.org/licenses/.

 Additionally icons from Google Design Material Icons are used that are licensed under Apache
 License Version 2.0.
 */

package org.secuso.privacyfriendlyfinance.activities.helper;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


/** Android specific file helpers */
public class FileHelper {
    private static final String FILENAME = "export.csv";

    private static final String MIME_CSV = "text/csv";

    private static File getSharedDir(Context context) {
        File sharedDir = new File(context.getFilesDir(), "shared");
        sharedDir.mkdirs();

        return sharedDir;
    }

    public static File getCsvFile(Context context, String filename) {
        File outFile = new File(getSharedDir(context), filename);
        return outFile;
    }

    private static Uri getCsvFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context, "org.secuso.privacyfriendlyfinance", file);
    }

    public static  boolean sendCsv(Context context, String chooserLabel, File file) {
        Uri outUri = getCsvFileUri(context, file);
        Log.d("TAG", chooserLabel +
                ": " + outUri);

        if (outUri != null) {
            Intent childSend = new Intent();

            childSend
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_STREAM, outUri)

                    .setType(MIME_CSV);

            childSend.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip = ClipData.newUri(context.getContentResolver(), outUri.toString(), outUri);
                childSend.setClipData(clip);
            }

            final Intent execIntent = Intent.createChooser(childSend, chooserLabel);

            context.startActivity(execIntent);
            return true;
        }
        return false;
    }
    
    public static boolean driveCsv(Context context, File file, String password) {
        try {
            // Encrypt first
            File encryptedFile = encryptFile(context, file, password);

            // Then upload encrypted file
            Uri outUri = getCsvFileUri(context, encryptedFile);
            Log.d("TAG", "Exporting encrypted CSV to Google Drive: " + outUri);

            if (outUri != null) {
                Intent driveIntent = new Intent(Intent.ACTION_SEND);
                driveIntent.setType(MIME_CSV); 
                driveIntent.putExtra(Intent.EXTRA_STREAM, outUri);
                driveIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = ClipData.newUri(context.getContentResolver(), outUri.toString(), outUri);
                    driveIntent.setClipData(clip);
                }

                driveIntent.setPackage("com.google.android.apps.docs");

                context.startActivity(driveIntent);
                return true;
            }
        } catch (Exception e) {
            Log.e("TAG", "Encryption or Drive export failed", e);
        }
        return false;
    }
    
    public static File encryptFile(Context context, File inputFile, String password) throws Exception {
        // Where encrypted file will be stored
        File encryptedFile = new File(getSharedDir(context), inputFile.getName() + ".enc");

        // Random salt (for key derivation)
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        // Derive AES key from password
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Random IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
            // Write salt + iv at the beginning (needed for decryption later)
            fos.write(salt);
            fos.write(iv);

            try (CipherOutputStream cos = new CipherOutputStream(fos, cipher);
                 FileInputStream fis = new FileInputStream(inputFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    cos.write(buffer, 0, read);
                }
            }
        }

        return encryptedFile;
    }
}




