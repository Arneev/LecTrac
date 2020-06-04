package com.example.stringtomd5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    final static String START_SALT = "!@We#4";
    final static String  END_SALT = "HQWn98";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void Convert(View v) throws NoSuchAlgorithmException {



        TextView tvString = (TextView)findViewById(R.id.edtText);
        String string = tvString.getText().toString();
        Log.i("Perso",string);
        String hash = saltAndHash(string);

        TextView hashedText = (TextView)findViewById(R.id.edtHash);
        hashedText.setText(hash);

    }

    String StringToMD5(String string) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(string.getBytes());
        BigInteger bigInteger = new BigInteger(1,messageDigest);
        String hashString = bigInteger.toString(16);
        return hashString;
    }

    String salt(String string){
        String temp = "";
        temp = START_SALT + string + END_SALT;
        return temp;
    }

    String saltAndHash(String string) throws NoSuchAlgorithmException {
        String temp = StringToMD5(salt(string));
        return temp;
    }
}
