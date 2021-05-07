package com.example.todolist;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // here I set up public objects and variables to use later on. I create my listViewOb to refer to my list view,
    // my listItems arraylist to contain my list, my itemAdapter arrayadapter to utilize my arraylist with the listview conveniently,
    // textBox EditText to refer to my edit text, and my position integer to refer to list positions later on by press.
    public ListView listViewOb;
    public ArrayList<String> listItems;
    public ArrayAdapter<String> itemAdapter;
    public EditText textBox;
    public int position;
    public final String fileName = "list.txt";
    public TextToSpeech speaker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // here I simply set up my action bar so it has everything I need
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("To-Do List");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        // here I take those public objects and set them all up correctly, setting up the listViewOb for the ListView and
        // the listItems arraylist before using the itemadapter to connect the two. I also set up my text box call and start a method I create later.
        listViewOb = (ListView) findViewById(R.id.listItem);
        listItems = new ArrayList<String>();
        itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listViewOb.setAdapter(itemAdapter);
        textBox = (EditText)findViewById(R.id.newItem);
        // here I create the File object I will use to see if a file already exists with data
        File listFile = new File(getApplicationContext().getFilesDir(), fileName);
        createClickListen();
        // here I set up the speaker with an onInit that tells it to use US English
        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            public void onInit(int i) {
                speaker.setLanguage(Locale.US);
            }
        });// here I check if the list data file exists and if it does, I create some objects to get my BufferedReader
        if(listFile.exists()){

            try {
                InputStream in = openFileInput(fileName);
                InputStreamReader inputReader = new InputStreamReader(in);
                BufferedReader fileReader = new BufferedReader(inputReader);
                // I use a while loop to go through every line in the file and add it to my array before notifying the adapter to check for new info
                String line = fileReader.readLine();
                while(line != null) {
                    listItems.add(line);
                    line = fileReader.readLine();
                }
                itemAdapter.notifyDataSetChanged();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // here I use a switch statement to check for the different action bar parts one can press and I simply check for each case.
        switch (item.getItemId()) {
            case R.id.adder:
                // in the case of addition, I just take the text from the text box, and add it to the
                // end of the list with an integer based on the size of the list before resetting the box
                // and telling the adapter to update everything
                String textBoxText = textBox.getText().toString();
                // here I use my speaker to speak the text written to be added
                speaker.speak("Added " + textBoxText, TextToSpeech.QUEUE_FLUSH, null);
                listItems.add(Integer.toString(listItems.size()+1) + " " + textBoxText);
                textBox.setText("");
                itemAdapter.notifyDataSetChanged();
                return true;

            case R.id.deleter:
                // in the case of deleting, I remove the list item from the position which i get later on from the click
                // but I also loop through the arraylist to reset my positional numbers to be correct after the deletion, working backwards
                // I also use the speaker to say that it removed the list item at the position of removal without the number
                speaker.speak("Removed " + listItems.get(position).split(" ")[1], TextToSpeech.QUEUE_FLUSH, null);
                listItems.remove(position);
                for(int i=listItems.size();i>position;i--){
                    listItems.set(i-1,Integer.toString(i) + " " + listItems.get(i-1).split(" ")[1]);
                }
                // again I reset the text box and update everything at once for it to be seamless to the user
                textBox.setText("");
                itemAdapter.notifyDataSetChanged();
                return true;

            case R.id.updater:
                // in the case of updating I just get the text from the box and set the position it is at to the new text, keeping the correct number
                String textBoxUpdate = textBox.getText().toString();
                listItems.set(position,(Integer.toString(position+1) + " " + textBoxUpdate));
                // once again I reset the text box and update everything.
                textBox.setText("");
                itemAdapter.notifyDataSetChanged();

                return true;

            case R.id.save:
                // here i just call the saveFile() method
                saveFile();
                return true;

            case R.id.close:
                // here I do the same but I use finish() to close the application
                saveFile();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void createClickListen() {
        // here is how I get the position the user wants. I use an OnItemLongClickListener from the AdapterView
        // class to set the public integer position to the clicked position and to set the editable text box
        // with the item from the list clicked.
        listViewOb.setOnItemLongClickListener(
                (adapter, item, pos, id) -> {
                    position = pos;
                    textBox.setText(listItems.get(position).split(" ")[1]);
                    return true;
                });

    }

    private void saveFile(){
        // here I create my file saver. basically, I create an OutputStreamWriter and loop through my array, writing each item and \n for new lines
        try {
            OutputStreamWriter fileWriter = new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE));
            for(int i = 0;i<listItems.size();i++){
                fileWriter.write(listItems.get(i) + "\n");
            }
            // here I make a toast just to give a visual indication the save worked which is often helpful to users and close my file writer.
            Toast.makeText(getApplicationContext(), "Saved" , Toast.LENGTH_SHORT).show();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
