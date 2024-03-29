package com.example.davidka;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@androidx.room.Dao
public interface SpeakButtonDao {

//    @Query("select speak from SpeakButton where position = :pos")
//    String getSpeak(int pos);
//
//    @Query("select picture from SpeakButton where position = :pos")
//    String getPicture(int pos);

    @Insert
    void addSpeakButton(SpeakButton speakButton);

    @Update
    void updateSpeakButton(SpeakButton speakButton);

    @Delete
    void deleteSpeakButton(SpeakButton speakButton);

//    @Query("SELECT TOP 8 * FROM SpeakButton WHERE picture = :uri")
//    int isPicturePresent(String uri);

    @Query("DELETE FROM SpeakButton where position = :position")
    void deleteButtonByPosition(int position);

    @Query("select * from SpeakButton")
    List<SpeakButton> getAllButtons();
}
