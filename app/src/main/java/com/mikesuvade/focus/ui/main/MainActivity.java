package com.mikesuvade.focus.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.mikesuvade.focus.databinding.ActivityMainBinding; // ← ИМПОРТ БИНДИНГА

public class MainActivity extends AppCompatActivity {

    // Объявляем переменную для биндинга
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Инициализируем биндинг (вместо setContentView(R.layout...))
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Теперь можно обращаться к элементам через binding
        // binding.yourTextView.setText("Привет!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Освобождаем биндинг, чтобы избежать утечек памяти
        binding = null;
    }
}