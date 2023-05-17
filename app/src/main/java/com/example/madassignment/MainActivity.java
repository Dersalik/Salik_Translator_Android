package com.example.madassignment;

import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.AvailableLanguage;
import models.DetectedLanguage;
import models.LanguageDetails;
import models.TranslationResult;
import models.TranslationService;

public class MainActivity extends AppCompatActivity {
    private Spinner inputLanguagesSpinner;
    private Spinner outputLanguagesSpinner;
    private Button translateButton;
    private Button switchButton;

    AvailableLanguage availableLanguage;
    TranslationService service ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service =new TranslationService();
        // Add the following code to set the StrictMode policy
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        inputLanguagesSpinner = findViewById(R.id.input_language_spinner);
        outputLanguagesSpinner=findViewById(R.id.output_language_spinner);
        translateButton=findViewById(R.id.translate_button);
        switchButton=findViewById(R.id.switch_button);
        try {
             availableLanguage=service.getAvailableLanguages();
//            Set<String> languages = availableLanguage.translation.keySet();
            Set<String> languages = new HashSet<>();

            for (LanguageDetails details : availableLanguage.translation.values()) {
                languages.add(details.name);
            }
            List<String> languageList = new ArrayList<>(languages);


            ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getApplicationContext(), android.R.layout.simple_spinner_item,languageList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            inputLanguagesSpinner.setAdapter(adapter);
            outputLanguagesSpinner.setAdapter(adapter);

            TranslationResult[] translationResult= service.getTranslation("en","Hello","ar");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        EditText OutputText=findViewById(R.id.output_text);
        EditText editInputText=findViewById(R.id.input_text);

        inputLanguagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                translateButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        outputLanguagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                translateButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        editInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No need to implement anything here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable the Translate button if the input EditText is not empty
                translateButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to implement anything here
            }
        });

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String input =  inputLanguagesSpinner.getSelectedItem().toString();
                String output=  outputLanguagesSpinner.getSelectedItem().toString();

                String inputText=editInputText.getText().toString();

                String inputLanguageCode=findLanguageCode(input);
                String outputLanguageCode=findLanguageCode(output);




                try {
                    TranslationResult[] translationResult= service.getTranslation(inputLanguageCode,inputText,outputLanguageCode);
                    EditText outputText=findViewById(R.id.output_text);


                    if( inputLanguageCode!=translationResult[0].detectedlanguage.language){
                       String langName= findLanguageName(translationResult[0].detectedlanguage.language);
                       int lanPosition=getLanguagePositionInSpinnerByName(langName);

                        inputLanguagesSpinner.setSelection(lanPosition);
                    }
                    translateButton.setEnabled(false);
                    outputText.setText(translationResult[0].translations[0].text);



                } catch (IOException e) {
                    System.out.println("Something is wrong with handling translate button in main activity");
                    throw new RuntimeException(e);
                }


            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input =  inputLanguagesSpinner.getSelectedItem().toString();
                String output=  outputLanguagesSpinner.getSelectedItem().toString();

                int inputLangPosition=getLanguagePositionInSpinnerByName(input);
                int outputLangPosition=getLanguagePositionInSpinnerByName(output);

                inputLanguagesSpinner.setSelection(outputLangPosition);
                outputLanguagesSpinner.setSelection(inputLangPosition);

                OutputText.setText("");
                editInputText.setText("");
                translateButton.setEnabled(true);

            }
        });


            }
    private int getLanguagePositionInSpinnerByName(String desiredLanguage){
        int itemCount = inputLanguagesSpinner.getCount();
        for (int i = 0; i < itemCount; i++) {
            String language = (String) inputLanguagesSpinner.getItemAtPosition(i);
            if (language.equals(desiredLanguage)) {
                return i;
            }

        }
        return  -1;
    }

    private String findLanguageCode(String name){
        for (Map.Entry<String, LanguageDetails> entry : availableLanguage.translation.entrySet()) {
            if (entry.getValue().name.equals(name)) {
                return entry.getKey();

            }
        }

        return "en";
    }


    private String findLanguageName(String code) {
        for (Map.Entry<String, LanguageDetails> entry : availableLanguage.translation.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(code)) {
                return entry.getValue().name;
            }
        }

        return "English"; // Default language name if no match is found
    }
}
