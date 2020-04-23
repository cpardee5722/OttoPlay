package com.example.ottoplay.WaypointMap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.ClassDiagrams.StaticWaypoint;
import com.example.ottoplay.ClassDiagrams.User;
import com.example.ottoplay.ClassDiagrams.Waypoint;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class waypointSettingsActivity extends AppCompatActivity {

    private static EditText waypointName;
    private static RadioGroup editingSelection;
    private static RadioGroup visibilitySelection;

    private MyApplication app;
    private User currentUser;
    private static int globalId;

    private Waypoint swpGlob;

    private ReentrantLock connectorLock;

    private static int addPlaylist= R.id.addPlaylist;
    //array of possible playlists
    /*private static int[] playlists={R.id.playlist1,
                                    R.id.playlist2,
                                    R.id.playlist3,
                                    R.id.playlist4,
                                    R.id.playlist5,
                                    R.id.playlist6,
                                    R.id.playlist7,
                                    R.id.playlist8,
                                    R.id.playlist9,
                                    R.id.playlist10,
                                    R.id.playlist11,
                                    R.id.playlist12,
                                    R.id.playlist13,
                                    R.id.playlist14,
                                    R.id.playlist15,
                                    R.id.playlist16,
                                    R.id.playlist17,
                                    R.id.playlist18,
                                    R.id.playlist19,
                                    R.id.playlist20};*/

    private static ToggleButton genre1Button,genre2Button,genre3Button,genre4Button,genre5Button,genre6Button,genre7Button,genre8Button,genre9Button;

    private Waypoint swp;
    //name, <playlistid, spotifyid>
    private HashMap<String, Pair<Integer,String>> playlistIds;
    private ArrayList<String> playlistList;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //TODO
        setContentView(R.layout.waypointsettings);

        app = (MyApplication) getApplication();//can put this code in onCreate
        swp = app.getWaypoint();
        swpGlob = swp;
        globalId = swp.getGlobalId();
        connectorLock = app.getLock();
        playlistIds = app.getPlaylistIds();
        currentUser = app.getUser();

        //Load Settings Upon UI open
        setWaypointName(swp.getWaypointName());
        //Update UI with Coordinates
        if (swp instanceof StaticWaypoint) setWaypointCoordinates(swp.getLocation());
        //Update UI with Visibility Settings
        setWaypointVisibility(swp.getVisSetting());
        //Update UI with editing Setting
        if (swp instanceof StaticWaypoint) {
            StaticWaypoint swp1 = (StaticWaypoint) swp;
            setWaypointEditing(swp1.getEditSetting());
        }
        else {
            setWaypointEditing(Waypoint.EditingSetting.valueOf("SOLO"));
        }
        //Add all playlists present in waypoint to screen
        setWaypointPlaylists(swp.getAllPlaylists());

        //Iterate through Genres array and check buttons accordingly
        for(int i=0;i<swp.getAllGenres().size();i++){
            setWaypointGenre(swp.getGenre(i),true);
        }
        // Set Creation Date label appropriately
        if (swp instanceof StaticWaypoint) {
            StaticWaypoint swp1 = (StaticWaypoint) swp;
            setCreationDate(swp1.getCreationDate());
        }
        //-----------------------------------------------------------------
        waypointName = (EditText) findViewById(R.id.waypointNameLabel);

        //Editing Settings Selection
        editingSelection = findViewById(R.id.editingSelection);
        if (!(swp instanceof StaticWaypoint)) {
            ((RadioButton) editingSelection.getChildAt(0)).setEnabled(false);
            ((RadioButton) editingSelection.getChildAt(1)).setEnabled(false);
            ((RadioButton) editingSelection.getChildAt(2)).setEnabled(false);
        }

        //Visibility Settings Selection
        visibilitySelection = findViewById(R.id.visibilitySelection);

        genre1Button= findViewById(R.id.genre1);
        genre2Button= findViewById(R.id.genre2);
        genre3Button= findViewById(R.id.genre3);
        genre4Button= findViewById(R.id.genre4);
        genre5Button= findViewById(R.id.genre5);
        genre6Button= findViewById(R.id.genre6);
        genre7Button= findViewById(R.id.genre7);
        genre8Button= findViewById(R.id.genre8);
        genre9Button= findViewById(R.id.genre9);

        if (!(swp instanceof StaticWaypoint)) {
            genre1Button.setEnabled(false);
            genre2Button.setEnabled(false);
            genre3Button.setEnabled(false);
            genre4Button.setEnabled(false);
            genre5Button.setEnabled(false);
            genre6Button.setEnabled(false);
            genre7Button.setEnabled(false);
            genre8Button.setEnabled(false);
            genre9Button.setEnabled(false);
        }

        //----------------------------------------------------------------------------- UI Listeners

        waypointName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String newName = waypointName.getText().toString();
                //Update Database
                updateWaypointName(globalId,newName);
                //Update Local copy of name
                swp.setWaypointName(newName);
                return false;
            }
        });

           visibilitySelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selection = visibilitySelection.getCheckedRadioButtonId();
                Waypoint.VisibilitySetting vis;
                String storedVis;
                switch(selection) {
                    case R.id.visibilityPublic:
                        vis= Waypoint.VisibilitySetting.PUBLIC;
                        storedVis = "PUBLIC";
                        break;
                    case R.id.visibilityPrivate:
                        vis = Waypoint.VisibilitySetting.PRIVATE;
                        storedVis = "PRIVATE";
                        break;
                    case R.id.visibilityHidden:
                        vis = Waypoint.VisibilitySetting.HIDDEN;
                        storedVis = "HIDDEN";
                        break;
                    default :
                        vis =Waypoint.VisibilitySetting.PUBLIC;
                        storedVis="PUBLIC";
                        break;
                }
                updateWaypointVisibility(storedVis);
                swp.setVisSetting(vis);
            }
        });

         editingSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selection = editingSelection.getCheckedRadioButtonId();
                Waypoint.EditingSetting edit;
                String storedEdit;
                switch(selection) {
                    case R.id.editingSolo:
                        edit= Waypoint.EditingSetting.SOLO;
                        storedEdit = "SOLO";
                        break;
                    case R.id.editingShared:
                        edit = Waypoint.EditingSetting.SHARED;
                        storedEdit = "SHARED";
                        break;
                    case R.id.editingWild:
                        edit = Waypoint.EditingSetting.WILD;
                        storedEdit = "WILD";
                        break;
                    default :
                        edit =Waypoint.EditingSetting.SOLO;
                        storedEdit="PUBLIC";
                        break;
                }
                updateWaypointEditing(storedEdit);
                if (swp instanceof StaticWaypoint) {
                    StaticWaypoint swp1 = (StaticWaypoint) swp;
                    swp1.setEditSetting(edit);
                }
            }
        });


        final ListView playlists = (ListView) findViewById(R.id.playlistsScrollBar);

        ArrayList<Playlist> pl = swp.getAllPlaylists();

        playlistList = new ArrayList<>();
        for (int i = 0; i < pl.size(); i++) {
            playlistList.add(pl.get(i).getPlaylistName());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playlistList);
        playlists.setAdapter(arrayAdapter);
        playlists.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final Button removeButton = (Button) findViewById(R.id.removePlaylist);
        removeButton.setEnabled(false);

        final int currentPosition = -1;
        playlists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                //do not enable this button if this waypoint belongs to the current user
                /*if (wp.getOwnerUserId() != currentUser.getUserId()) {
                    addToPlaylistsButton.setEnabled(true);
                }
                String selected = (String) playlists.getItemAtPosition(position);
                addToPlaylistsButton.setTag((Object) selected);*/

                String selected = (String) playlists.getItemAtPosition(position);
                selected += " " + Integer.toString(position);
                removeButton.setTag((Object) selected);
                removeButton.setEnabled(true);

                playlists.setSelector(R.color.pressed_color);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String tag = (String) v.getTag();
                String[] args = tag.split(" ");
                ArrayList<Playlist> pls = swp.getAllPlaylists();
                Playlist p = new Playlist();
                for (int i = 0; i < pls.size(); i++) {
                    if (args[0].compareTo(pls.get(i).getPlaylistName()) == 0) {
                        p = pls.get(i);
                        swp.removePlaylist(pls.get(i));
                    }
                }

                Thread t = new Thread(new RemovePlaylistThread(p));
                t.start();
                try {
                    t.join();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                arrayAdapter.remove(args[0]);
                playlists.setAdapter(arrayAdapter);//unselects item
                removeButton.setEnabled(false);
            }
        });
    }

    public void showMenu(View v) {
        final ArrayList<Playlist> userPlaylists = currentUser.getSyncedPlaylists();
        final PopupMenu popup = new PopupMenu(waypointSettingsActivity.this, v);
        popup.inflate(R.menu.add_playlist_menu);
        Menu m = popup.getMenu();
        for (int i = 0; i < userPlaylists.size(); i++) {
            m.add(0,i,0, userPlaylists.get(i).getPlaylistName());
        }
        //m.add()

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Playlist p = userPlaylists.get(item.getItemId());
                Thread t = new Thread(new AddPlaylistThread(p));
                t.start();
                try {
                    t.join();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                ArrayList<Playlist> pls = swp.getAllPlaylists();
                boolean contains = false;
                for (int j = 0; j < pls.size(); j++) {
                    if (pls.get(j).getPlaylistId() == p.getPlaylistId()) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) swp.addPlaylist(p);

                ListView playlists = (ListView) findViewById(R.id.playlistsScrollBar);
                playlistList.add(p.getPlaylistName());
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(waypointSettingsActivity.this, android.R.layout.simple_list_item_1, playlistList);
                playlists.setAdapter(arrayAdapter);

                return false;
            }
        });


        //popup.inflate(R.menu.waypoint_settings_menu);

        //initializeMenu(popup);

        popup.show();

    }

    class AddPlaylistThread implements Runnable {
        Playlist p;
        AddPlaylistThread(Playlist playlist) {
            p = playlist;
        }

        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            dbc.requestData("15:" + Integer.toString(p.getPlaylistId()) + "," + Integer.toString(swp.getGlobalId()));
        }
    }


    class RemovePlaylistThread implements Runnable {
        Playlist p;
        RemovePlaylistThread(Playlist playlist) {
            p = playlist;
        }

        public void run() {
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            dbc.requestData("16:" + Integer.toString(p.getPlaylistId()) + "," + Integer.toString(swp.getGlobalId()));
        }
    }


    //Playlist code
    public void buttonPlaylistClicked(View view){
        //Jump to Playlist Viewing based on view.getId()
    }

    public void buttonAddPlaylistClicked(View view){
        //Jump to Playlist Viewing based on view.getId()
    }

    public void buttonGenreClicked(View view){
        switch(view.getId()){
            case R.id.genre1:
                if(genre1Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.HIPHOP);
                    new Thread(new updateDatabase("36:" + globalId + "," + "HIPHOP")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.HIPHOP);
                    new Thread(new updateDatabase("37:" + globalId + "," + "HIPHOP")).start();
                }
                setWaypointGenre(Waypoint.Genre.HIPHOP,genre1Button.isChecked());
                break;
            case R.id.genre2:
                if(genre2Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.POP);
                    new Thread(new updateDatabase("36:" + globalId + "," + "POP")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.POP);
                    new Thread(new updateDatabase("37:" + globalId + "," + "POP")).start();
                }
                setWaypointGenre(Waypoint.Genre.POP,genre2Button.isChecked());
                break;
            case R.id.genre3:
                if(genre3Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.JAZZ);
                    new Thread(new updateDatabase("36:" + globalId + "," + "JAZZ")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.JAZZ);
                    new Thread(new updateDatabase("37:" + globalId + "," + "JAZZ")).start();
                }
                setWaypointGenre(Waypoint.Genre.JAZZ,genre3Button.isChecked());
                break;
            case R.id.genre4:
                if(genre4Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.BLUES);
                    new Thread(new updateDatabase("36:" + globalId + "," + "BLUES")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.BLUES);
                    new Thread(new updateDatabase("37:" + globalId + "," + "BLUES")).start();
                }
                setWaypointGenre(Waypoint.Genre.BLUES,genre4Button.isChecked());
                break;
            case R.id.genre5:
                if(genre5Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.COUNTRY);
                    new Thread(new updateDatabase("36:" + globalId + "," + "COUNTRY")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.COUNTRY);
                    new Thread(new updateDatabase("37:" + globalId + "," + "COUNTRY")).start();
                }
                setWaypointGenre(Waypoint.Genre.COUNTRY,genre5Button.isChecked());
                break;
            case R.id.genre6:
                if(genre6Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.ROCK);
                    new Thread(new updateDatabase("36:" + globalId + "," + "ROCK")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.ROCK);
                    new Thread(new updateDatabase("37:" + globalId + "," + "ROCK")).start();
                }
                setWaypointGenre(Waypoint.Genre.ROCK,genre6Button.isChecked());
                break;
            case R.id.genre7:
                if(genre7Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.METAL);
                    new Thread(new updateDatabase("36:" + globalId + "," + "METAL")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.METAL);
                    new Thread(new updateDatabase("37:" + globalId + "," + "METAL")).start();
                }
                setWaypointGenre(Waypoint.Genre.METAL,genre7Button.isChecked());
                break;
            case R.id.genre8:
                if(genre8Button.isChecked()) {
                    //add
                    swpGlob.addGenre(Waypoint.Genre.EDM);
                    new Thread(new updateDatabase("36:" + globalId + "," + "EDM")).start();
                }
                else{
                    //remove
                    swpGlob.removeGenre(Waypoint.Genre.EDM);
                    new Thread(new updateDatabase("37:" + globalId + "," + "EDM")).start();
                }
                setWaypointGenre(Waypoint.Genre.EDM,genre8Button.isChecked());
                break;
            case R.id.genre9:
                if(genre9Button.isChecked()) {
                    //add
                    //new Thread(new updateDatabase("28:" + globalId + "," + "OTHER")).start();
                }
                else{
                    //remove
                    //new Thread(new updateDatabase("29:" + globalId + "," + "OTHER")).start();
                }
                //setWaypointGenre(Waypoint.Genre.OTHER,genre9Button.isChecked());
                break;
            default:
                break;

        }
    }

    public void setWaypointName(String Name) {
        //((EditText) findViewById(R.id.waypointNameLabel)).setText(Name);
        EditText wpNameLabel = (EditText) findViewById(R.id.waypointNameLabel);
        wpNameLabel.setText(Name);
    }

    public void updateWaypointName(int waypointId, String Name) {
        new Thread(new updateDatabase("19:" + waypointId + "," + Name)).start();
    }

    public void setWaypointCoordinates(String GPS){
        String [] Coordinates =GPS.split(" ");
        int trim = 10;
        if (Coordinates[0].length() < 10) trim = Coordinates[0].length();
        ((TextView) findViewById(R.id.latitude)).setText(Coordinates[0].substring(0,trim));
        ((TextView) findViewById(R.id.longitude)).setText(Coordinates[1].substring(0,trim));
    }

    public void setWaypointEditing(StaticWaypoint.EditingSetting Editing) {
        if(Editing == StaticWaypoint.EditingSetting.SOLO){
            ((RadioGroup) findViewById(R.id.editingSelection)).check(R.id.editingSolo);
        } else if (Editing == StaticWaypoint.EditingSetting.SHARED){
            ((RadioGroup) findViewById(R.id.editingSelection)).check(R.id.editingShared);
        } else if (Editing == StaticWaypoint.EditingSetting.WILD){
            ((RadioGroup) findViewById(R.id.editingSelection)).check(R.id.editingWild);
        } else {
            //do nothing
        }
    }

    public void updateWaypointEditing(String Editing) {
        new Thread(new updateDatabase("20" , Integer.toString(globalId), Editing)).start();
    }

    public void setWaypointVisibility(Waypoint.VisibilitySetting Visibility) {
        if(Visibility == Waypoint.VisibilitySetting.PUBLIC){
            ((RadioGroup) findViewById(R.id.visibilitySelection)).check(R.id.visibilityPublic);
        } else if (Visibility == Waypoint.VisibilitySetting.PRIVATE){
            ((RadioGroup) findViewById(R.id.visibilitySelection)).check(R.id.visibilityPrivate);
        } else if (Visibility == Waypoint.VisibilitySetting.HIDDEN){
            ((RadioGroup) findViewById(R.id.visibilitySelection)).check(R.id.visibilityHidden);
        } else{
            //do nothing
        }
    }

    public void updateWaypointVisibility(String Visibility) {
        new Thread(new updateDatabase("21" ,Integer.toString(globalId), Visibility)).start();
    }

    public void setWaypointPlaylists(ArrayList<Playlist> Playlists) {
        //Update Playlists list
       /* for(int i=0; i<Playlists.size();i++){
            ((Button) findViewById(playlists[i])).setVisibility(View.VISIBLE);
            ((Button) findViewById(playlists[i])).setText(Playlists.get(i).getPlaylistName()); //Playlist class needs name attribute
        }
        //If we have maximum playlists, remove add playlist button
        if(Playlists.size()>=20){
            ((Button) findViewById(addPlaylist)).setVisibility(View.GONE);
        }*/
    }

    public void setWaypointGenre(Waypoint.Genre Genre, boolean value) {
        switch(Genre) {
            case HIPHOP:
            ((ToggleButton) findViewById(R.id.genre1)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case POP:
            ((ToggleButton) findViewById(R.id.genre2)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case JAZZ:
            ((ToggleButton) findViewById(R.id.genre3)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case BLUES:
            ((ToggleButton) findViewById(R.id.genre4)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case COUNTRY:
            ((ToggleButton) findViewById(R.id.genre5)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case ROCK:
            ((ToggleButton) findViewById(R.id.genre6)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case METAL:
            ((ToggleButton) findViewById(R.id.genre7)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            case EDM:
            ((ToggleButton) findViewById(R.id.genre8)).setChecked(value);//(genre.valueOf("ROCK")
                break;
            //((ToggleButton) findViewById(R.id.genre9)).setChecked(false);//(genre.valueOf("ROCK")
        }
    }

    public void updateWaypointGenre (Waypoint.Genre Genre, int waypointId) {
        new Thread(new updateDatabase("28:" +  waypointId + "," + Genre.name())).start();
    }

    public void setCreationDate (Date CreationDate) {
        SimpleDateFormat pattern = new SimpleDateFormat("MM-dd-yyyy");
        ((TextView) findViewById(R.id.dateCreatedLabel)).setText("Creation Date:    " + pattern.format(CreationDate));
    }

    class updateDatabase implements Runnable {
        private String queryID;
        private String[] args;
        private int arg;

        public updateDatabase(String queryID, String arg1, String arg2, String arg3, String arg4, String arg5) {
            this.queryID=queryID;
            this.args= new String[]{arg1,arg2,arg3,arg4,arg5};
            this.arg=5;
        }
        public updateDatabase(String queryID, String arg1, String arg2, String arg3, String arg4) {
            this.queryID=queryID;
            this.args= new String[]{arg1, arg2, arg3, arg4};
            this.arg=4;
        }
        public updateDatabase(String queryID, String arg1, String arg2, String arg3) {
            this.queryID=queryID;
            this.args= new String[]{arg1, arg2, arg3};
            this.arg=3;
        }
        public updateDatabase(String queryID, String arg1, String arg2) {
            this.queryID=queryID;
            this.args= new String[]{arg1, arg2};
            this.arg=2;
        }
        public updateDatabase(String queryID, String arg1) {
            this.queryID=queryID;
            this.args= new String[]{arg1};
            this.arg=1;
        }
        public updateDatabase(String queryID) {
            this.queryID=queryID;
            this.arg=0;
        }
        public updateDatabase() {
        }

        @Override
        public void run() {
            //check if waypoint exists else break
            DatabaseConnector dbc = new DatabaseConnector(connectorLock);
            ArrayList<ArrayList<String>> Result;
            switch(arg){
                case 0:
                    dbc.requestData(queryID);
                    break;
                case 1:
                    dbc.requestData(queryID +":" + args[0]);
                    break;
                case 2:
                    dbc.requestData(queryID +":" + args[0] + "," + args[1]);
                    break;
                case 3:
                    dbc.requestData(queryID +":" + args[0] + "," + args[1] + "," + args[2]);
                    break;
                case 4:
                    dbc.requestData(queryID +":" + args[0] + "," + args[1] + "," + args[2] + "," + args[3]);
                    break;
                case 5:
                    dbc.requestData(queryID +":" + args[0] + "," + args[1] + "," + args[2] + "," + args[3] + "," + args[4]);
                    break;
                default:
                    break;

            }
        }
    }
}




