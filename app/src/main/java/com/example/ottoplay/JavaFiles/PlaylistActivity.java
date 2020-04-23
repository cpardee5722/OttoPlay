package com.example.ottoplay.JavaFiles;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ottoplay.ClassDiagrams.Playlist;
import com.example.ottoplay.Connectors.PlaylistsService;
import com.example.ottoplay.Connectors.TrackService;
import com.example.ottoplay.DatabaseConnector;
import com.example.ottoplay.Models.Playlists;
import com.example.ottoplay.Models.Song;
import com.example.ottoplay.MyApplication;
import com.example.ottoplay.R;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    private TextView userView;
    private TextView songView;
    private TextView playlistInfo;
    private ListView playlistView;
    private Button addBtn;
    private Song song;
    private Playlists playlists;
    private PlaylistsService playlistsService;
    private TrackService trackService;
    private ArrayList<Song> playlistTracks;
    private ArrayList<Playlists> userPlaylists;
    private ArrayAdapter<String> adapter;
    private TextView sampleText;
    private static String playlistName;
    private static String playlistId;
    private static String owner_id;
    private String playlistNameToPass;
    private String playlistIdToPass;
    private static boolean clearCounter=false;
    private MyApplication app;
    private boolean playlistExists;
    //private Button addBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity);
        app = (MyApplication) getApplication();
        sampleText = (TextView) findViewById(R.id.sample);
        playlistInfo = (TextView) findViewById(R.id.playlistDetails);
        playlistInfo.setMovementMethod(new ScrollingMovementMethod());
        trackService = new TrackService(getApplicationContext());
        playlistsService = new PlaylistsService(getApplicationContext());
        //userView = (TextView) findViewById(R.id.user);
        songView = (TextView) findViewById(R.id.song);
        songView.setText("Click on a Playlist to add it to Ottoplay");
        songView.setText("Click on a Playlist to add it to Ottoplay");
        playlistView = (ListView) findViewById(R.id.playlist);
        //addBtn = (Button) findViewById(R.id.add);
        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        //userView.setText(sharedPreferences.getString("userid", "No User"));
        owner_id = sharedPreferences.getString("userid", "No User");
        getPlaylists();



    }


    private void getTracks(String playlistId) {

        if(clearCounter) {
            playlistTracks.clear();
        }
        trackService.setPlaylistId(playlistId);
        trackService.getPlaylistSongs(() -> {
            playlistTracks = trackService.getSongs(playlistId);
            //playlistInfo.setText(null);
            updatePlaylistTracks(playlistId);
        });
    }

    private void updatePlaylistTracks(String playlistId) {
        //playlistInfo.setText(null);
        for (int i = 0; i < playlistTracks.size(); i++) {
            playlistInfo.append("\n " + playlistTracks.get(i).getName());//.getId());
            clearCounter = true;
            Thread t = new Thread(new DatabaseSong(playlistTracks.get(i).getName(), playlistId, playlistTracks.get(i).getId()));
            t.start();
            try {
                t.join();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void getPlaylists() {
        playlistsService.getUsersPlaylists(() -> {
            userPlaylists = playlistsService.getPlaylists();
            updatePlaylist();
        });
    }

    private void updatePlaylist() {
        if (userPlaylists.size() > 0) {
            int count = 0;

            if (userPlaylists.size() > 0) {

                String[] playlist_items = new String[userPlaylists.size()];
                for (int i = 0; i < userPlaylists.size(); i++){
                    playlist_items[i] = userPlaylists.get(i).getName();
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playlist_items);
                }
                playlistView.setAdapter(adapter);
                playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //TODO: Implement OnClick Listener
                        playlistInfo.setText(null);
                        System.out.println("Playlist Name: "+ userPlaylists.get(position).getName() + "| Playlist Id: "+ userPlaylists.get(position).getId());
                        playlistName = userPlaylists.get(position).getName();
                        playlistId = userPlaylists.get(position).getId();

                        playlistExists = false;
                        Thread t = new Thread(new DatabasePlaylist(playlistName, playlistId, owner_id));
                        t.start();
                        try {
                            t.join();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        if (!playlistExists) getTracks(playlistId);

                        Toast.makeText(getApplicationContext(), "The database has been updated", Toast.LENGTH_SHORT).show();



                    }
                });
            }


          System.out.println("Total count of playlists is"+userPlaylists.size());


        }
    }

    class DatabasePlaylist implements Runnable {


        private final String playlistName;
        private final String playlistId;
        private final String ownerId;


        public DatabasePlaylist(String playlistName, String playlistId, String ownerId){
            this.playlistName = playlistName;
            this.playlistId = playlistId;
            this.ownerId = ownerId;
        }


        @Override
        public void run() {

            boolean checkUpdate;
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            DatabaseConnector dbc2 = new DatabaseConnector(app.getLock());
            ArrayList<ArrayList<String>> createPlaylist;
            ArrayList<ArrayList<String>> checkPlaylist;
            ArrayList<ArrayList<String>> checkPlaylist2;
            ArrayList<ArrayList<String>> createWaypoint;
            ArrayList<ArrayList<String>> getId;
            System.out.println("Entered the database thread");
            System.out.println(playlistName+" "+playlistId+" "+ownerId);
            checkPlaylist = dbc.requestData("42:"); //returns [[id, username, password]]
            ArrayList<ArrayList<String>> existingPT = checkPlaylist;
            System.out.println ("Length of table:"+existingPT.size());
            System.out.println("First value:"+existingPT.get(2));

            checkUpdate = true;
            System.out.println("Playlist id is"+ playlistId);
            for (int i = 1; i < checkPlaylist.size(); i++) {
                if(checkPlaylist.get(i).get(2).equals(playlistId))
                {
                    System.out.println("Playlist already exists");
                    //      Toast.makeText(getApplicationContext(), "Playlist Already Exists", Toast.LENGTH_SHORT).show();
                    checkUpdate=false;
                    playlistExists = true;
                }
            }

            if (checkUpdate)
            {
                int a = 1;
                System.out.println("Update is Possible");


                int id = app.getUser().getUserId();

                createPlaylist = dbc2.requestData("14:"+ playlistId + "," + playlistName +","+id);
                int lastIdx = createPlaylist.size() - 1;
                app.getUser().addToSyncedPlaylist(new Playlist(createPlaylist.get(lastIdx).get(2), Integer.parseInt(createPlaylist.get(lastIdx).get(0)), createPlaylist.get(lastIdx).get(1)));
                //System.out.println(createPlaylist);
            }

            //Thread.currentThread().interrupt();
            return;
        }

    }


    class DatabaseSong implements Runnable {

        private final String songName;
        private final String playlistId;
        private final String songId;


        public DatabaseSong(String songName, String playlistId, String songId){
            this.songName = songName;
            this.playlistId = playlistId;
            this.songId = songId;
        }


        @Override
        public void run() {

            boolean checkUpdateSong;
            DatabaseConnector dbc = new DatabaseConnector(app.getLock());
            DatabaseConnector dbc2 = new DatabaseConnector(app.getLock());
            ArrayList<ArrayList<String>> checkSong;
            ArrayList<ArrayList<String>> createSong;
            ArrayList<ArrayList<String>> playlistIdsInTable;
            StringBuffer sb = new StringBuffer();
            System.out.println("Entered the song thread");
            System.out.println(songName+" "+playlistId+" "+songId);
            //checkSong = dbc.requestData("51:0"); //returns [[id, username, password]]
            playlistIdsInTable= dbc.requestData("55:"+playlistId);
            System.out.println("These are the unique playlist ids"+playlistIdsInTable);
            //ArrayList<ArrayList<String>> existingPT = checkSong;
            //System.out.println(checkSong);
            //System.out.println ("Length of table:"+existingPT.size());

            //checkUpdateSong = true;
            //if(!playlistIdsInTable.isEmpty())
            //{
            //    System.out.println("Song already exists");
            //    checkUpdateSong=false;
           // }


            //if (checkUpdateSong)
           // {
                sb.append(playlistIdsInTable.get(0).get(0));
                String str= sb.toString();
                int playlistPosition = Integer.parseInt(str);
                System.out.println("Update is Possible");
                createSong = dbc2.requestData("50:"+ playlistPosition + "," + songName +","+songId);
            //}

            //Thread.currentThread().interrupt();
            return;
        }
    }

}





