package com.nokelock.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nokelock.nokelockble.R;
import com.nokelock.ui.CleanEditText;
import com.nokelock.utils.SystemUtils;

import org.litepal.tablemanager.Connector;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_version_control;
    private CleanEditText et_login_username;
    private CleanEditText et_login_password;
    private Button btn_login;
    private TextView tv_version;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();


    }

    private void initView() {
        Connector.getDatabase();
        iv_version_control = (ImageView) findViewById(R.id.iv_version_control);
        et_login_username = (CleanEditText) findViewById(R.id.et_login_username);
        et_login_password = (CleanEditText) findViewById(R.id.et_login_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_version = (TextView) findViewById(R.id.tv_version);

        btn_login.setOnClickListener(this);
        tv_version.setText(SystemUtils.getLocalVersionName(this));

        SharedPreferences preferences = getSharedPreferences("user", this.MODE_PRIVATE);
        String n = preferences.getString("name", "");
        String pwd = preferences.getString("password", "");
        et_login_username.setText(n);
        et_login_password.setText(pwd);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                submit();
                if (
                        (username.equals("test") && password.equals("1"))
                                || (username.equals("riti") && password.equals("1234qwer"))
                                || (username.equals("1821") && password.equals("vucyakrc"))
                                || (username.equals("1235") && password.equals("iulagwfe"))
                                || (username.equals("9082") && password.equals("ilgfuwea"))
                                || (username.equals("4532") && password.equals("huvbdewf"))
                                || (username.equals("1238") && password.equals("iuguylae"))
                                || (username.equals("5792") && password.equals("cnurargv"))
                                || (username.equals("7592") && password.equals("uqigfrcw"))
                                || (username.equals("5621") && password.equals("iubulwef"))
                                || (username.equals("4931") && password.equals("hivbayew"))

                        ) {
                    SharedPreferences.Editor editor = getSharedPreferences("user", this.MODE_PRIVATE).edit();
                    editor.putString("name", username);
                    editor.putString("password", password);
                    editor.commit();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
                break;
        }
    }

    private void submit() {
        // validate
        username = et_login_username.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        password = et_login_password.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "密码", Toast.LENGTH_SHORT).show();
            return;
        }


    }
}
