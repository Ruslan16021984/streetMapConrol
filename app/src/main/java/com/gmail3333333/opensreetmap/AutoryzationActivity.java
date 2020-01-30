package com.gmail3333333.opensreetmap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail3333333.opensreetmap.api.AuthorizationService;
import com.gmail3333333.opensreetmap.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutoryzationActivity extends AppCompatActivity {
    @BindView(R.id.etLogin)
    EditText etLogin;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.btnLogin)
    Button btnLogin;
    @BindView(R.id.btnReg)
    Button btnReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autoryzation);
        ButterKnife.bind(this);


    }
    @OnClick({R.id.btnLogin, R.id.btnReg})
    public void onClickButton(View view){
        User user = new User();
        user.setUsername(etLogin.getText().toString());
        user.setPassword(etPassword.getText().toString());
        user.setActive(true);
        AuthorizationService authorizationService1;
        switch (view.getId()){
            case R.id.btnLogin:
                authorizationService1 = AuthorizationService.retrofit.create(AuthorizationService.class);
                authorizationService1.getUser(etPassword.getText().toString()).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        Log.d("TAG", ""+ response.body());
                        if (response.body()){
                            Intent intent = new Intent(AutoryzationActivity.this, MainActivity.class);
                            intent.putExtra("NEW_USER", user);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.d("TAG", "NOT Connect");
                    }
                });
                break;
            case R.id.btnReg:
                authorizationService1 = AuthorizationService.retrofit.create(AuthorizationService.class);
                authorizationService1.saveUser(user).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        Log.d("TAG", "SAve in Server"+ user.toString());
                        Intent intent = new Intent(AutoryzationActivity.this, MainActivity.class);
                        intent.putExtra("NEW_USER", user);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d("TAG", "NOT");
                    }
                });
                break;
        }
    }

}
