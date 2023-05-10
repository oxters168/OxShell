package com.OxGames.OxShell.Data;

import android.util.Log;

import com.OxGames.OxShell.OxShellApp;
import com.OxGames.OxShell.R;

import java.util.HashMap;

public class ResImage {
    private static HashMap<Integer, ResImage> resources = null;

    private String id;
    private String name;

    public ResImage(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }

    public static ResImage get(int resId) {
        createResourcesIfNull();
        return resources.get(resId);
    }
    public static ResImage[] getResourceImages() {
        createResourcesIfNull();
        return resources.values().toArray(new ResImage[0]);
    }
    private static void createResourcesIfNull() {
        if (resources == null) {
            resources = new HashMap<>();
            resources.put(R.drawable.ic_baseline_accessibility_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_accessibility_24", "Accessibility"));
            resources.put(R.drawable.ic_baseline_add_circle_outline_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_add_circle_outline_24", "Plus Circle"));
            resources.put(R.drawable.ic_baseline_cancel_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_cancel_24", "Cross Circle"));
            resources.put(R.drawable.ic_baseline_auto_awesome_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_auto_awesome_24", "Stars"));
            resources.put(R.drawable.ic_baseline_block_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_block_24", "Block"));
            resources.put(R.drawable.ic_baseline_check_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_check_24", "Checkmark"));
            resources.put(R.drawable.ic_baseline_construction_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_construction_24", "Construction"));
            resources.put(R.drawable.ic_baseline_folder_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_folder_24", "Folder"));
            resources.put(R.drawable.ic_baseline_forum_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_forum_24", "Message Bubbles"));
            resources.put(R.drawable.ic_baseline_games_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_games_24", "Directional Pad"));
            resources.put(R.drawable.ic_baseline_headphones_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_headphones_24", "Headphones"));
            resources.put(R.drawable.ic_baseline_hide_image_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_hide_image_24", "Crossed Image"));
            resources.put(R.drawable.ic_baseline_home_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_home_24", "Home"));
            resources.put(R.drawable.ic_baseline_image_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_image_24", "Image"));
            resources.put(R.drawable.ic_baseline_map_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_map_24", "Map"));
            resources.put(R.drawable.ic_baseline_movie_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_movie_24", "Film"));
            resources.put(R.drawable.ic_baseline_newspaper_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_newspaper_24", "Newspaper"));
            resources.put(R.drawable.ic_baseline_photo_camera_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_photo_camera_24", "Camera"));
            resources.put(R.drawable.ic_baseline_question_mark_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_question_mark_24", "Question Mark"));
            resources.put(R.drawable.ic_baseline_send_time_extension_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_send_time_extension_24", "Send Puzzle Piece"));
            resources.put(R.drawable.ic_baseline_settings_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_settings_24", "Cog"));
            resources.put(R.drawable.ic_baseline_source_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_source_24", "Source Folder"));
            resources.put(R.drawable.ic_baseline_audio_file_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_audio_file_24", "Audio File"));
            resources.put(R.drawable.ic_baseline_video_file_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_video_file_24", "Video File"));
            resources.put(R.drawable.ic_baseline_view_list_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_view_list_24", "List"));
            resources.put(R.drawable.ic_baseline_work_24, new ResImage("com.OxGames.OxShell:drawable/ic_baseline_work_24", "Suitcase"));
            resources.put(R.drawable.baseline_info_24, new ResImage("com.OxGames.OxShell:drawable/baseline_info_24", "Info"));
            resources.put(R.drawable.baseline_adb_24, new ResImage("com.OxGames.OxShell:drawable/baseline_adb_24", "Android"));
            resources.put(R.drawable.baseline_person_24, new ResImage("com.OxGames.OxShell:drawable/baseline_person_24", "Person"));
            resources.put(R.drawable.baseline_library_music_24, new ResImage("com.OxGames.OxShell:drawable/baseline_library_music_24", "Music Library"));
            resources.put(R.drawable.baseline_video_library_24, new ResImage("com.OxGames.OxShell:drawable/baseline_video_library_24", "Video Library"));
            resources.put(R.drawable.baseline_play_arrow_24, new ResImage("com.OxGames.OxShell:drawable/baseline_play_arrow_24", "Play"));
            resources.put(R.drawable.baseline_pause_24, new ResImage("com.OxGames.OxShell:drawable/baseline_pause_24", "Pause"));
            resources.put(R.drawable.baseline_skip_next_24, new ResImage("com.OxGames.OxShell:drawable/baseline_skip_next_24", "Skip Next"));
            resources.put(R.drawable.baseline_skip_previous_24, new ResImage("com.OxGames.OxShell:drawable/baseline_skip_previous_24", "Skip Previous"));
            resources.put(R.drawable.baseline_close_24, new ResImage("com.OxGames.OxShell:drawable/baseline_close_24", "Cross"));
            resources.put(R.drawable.baseline_hourglass_empty_24, new ResImage("com.OxGames.OxShell:drawable/baseline_hourglass_empty_24", "Hourglass Empty"));
            resources.put(R.drawable.baseline_arrow_back_24, new ResImage("com.OxGames.OxShell:drawable/baseline_arrow_back_24", "Arrow Back"));
            resources.put(R.drawable.baseline_fast_forward_24, new ResImage("com.OxGames.OxShell:drawable/baseline_fast_forward_24", "Fast Forward"));
            resources.put(R.drawable.baseline_fast_rewind_24, new ResImage("com.OxGames.OxShell:drawable/baseline_fast_rewind_24", "Fast Rewind"));
            resources.put(R.drawable.baseline_fullscreen_24, new ResImage("com.OxGames.OxShell:drawable/baseline_fullscreen_24", "Fullscreen"));
        }
    }

    public static String oldResIdToNewId(int id) {
        switch (id) {
            case(2131165323):
                // Accessibility
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_accessibility_24);
            case(2131165324):
                // Plus Circle
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_add_circle_outline_24);
            case(2131165328):
                // Cross Circle
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_cancel_24);
            case(2131165326):
                // Stars
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_auto_awesome_24);
            case(2131165327):
                // Block
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_block_24);
            case(2131165329):
                // Checkmark
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_check_24);
            case(2131165330):
                // Construction
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_construction_24);
            case(2131165331):
                // Folder
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_folder_24);
            case(2131165332):
                // Message Bubbles
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_forum_24);
            case(2131165333):
                // Directional Pad
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_games_24);
            case(2131165334):
                // Headphones
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_headphones_24);
            case(2131165335):
                // Crossed Image
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_hide_image_24);
            case(2131165336):
                // Home
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_home_24);
            case(2131165337):
                // Image
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_image_24);
            case(2131165338):
                // Map
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_map_24);
            case(2131165339):
                // Film
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_movie_24);
            case(2131165340):
                // Newspaper
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_newspaper_24);
            case(2131165341):
                // Camera
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_photo_camera_24);
            case(2131165342):
                // Question Mark
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_question_mark_24);
            case(2131165343):
                // Send Puzzle Piece
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_send_time_extension_24);
            case(2131165344):
                // Cog
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_settings_24);
            case(2131165345):
                // Source Folder
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_source_24);
            case(2131165325):
                // Audio File
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_audio_file_24);
            case(2131165346):
                // Video File
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_video_file_24);
            case(2131165347):
                // List
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_view_list_24);
            case(2131165348):
                // Suitcase
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_work_24);
            case(2131165306):
                // Info
                return OxShellApp.getContext().getResources().getResourceName(R.drawable.baseline_info_24);
        }
        Log.w("ResImage", id + " not from version 1");
        return OxShellApp.getContext().getResources().getResourceName(R.drawable.ic_baseline_question_mark_24);
    }
}
