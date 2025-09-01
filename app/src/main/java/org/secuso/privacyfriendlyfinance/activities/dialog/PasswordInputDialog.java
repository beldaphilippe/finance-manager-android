package org.secuso.privacyfriendlyfinance.activities.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

public class PasswordInputDialog {

    public interface PasswordCallback {
        void onPasswordEntered(String password);
    }

    public static void show(Context context, PasswordCallback callback) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(context)
                .setTitle("Enter Password")
                .setMessage("This password will be used to encrypt the CSV file.")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String password = input.getText().toString();
                    callback.onPasswordEntered(password);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }
}
