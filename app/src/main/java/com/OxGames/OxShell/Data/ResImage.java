package com.OxGames.OxShell.Data;

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
            resources.put(R.drawable.baseline_play_arrow_24, new ResImage("com.OxGames.OxShell:drawable/baseline_play_arrow_24", "Play"));
            resources.put(R.drawable.baseline_pause_24, new ResImage("com.OxGames.OxShell:drawable/baseline_pause_24", "Pause"));
            resources.put(R.drawable.baseline_skip_next_24, new ResImage("com.OxGames.OxShell:drawable/baseline_skip_next_24", "Skip Next"));
            resources.put(R.drawable.baseline_skip_previous_24, new ResImage("com.OxGames.OxShell:drawable/baseline_skip_previous_24", "Skip Previous"));
            resources.put(R.drawable.baseline_close_24, new ResImage("com.OxGames.OxShell:drawable/baseline_close_24", "Cross"));
            resources.put(R.drawable.baseline_hourglass_empty_24, new ResImage("com.OxGames.OxShell:drawable/baseline_hourglass_empty_24", "Hourglass Empty"));
            resources.put(R.drawable.baseline_arrow_back_24, new ResImage("com.OxGames.OxShell:drawable/baseline_arrow_back_24", "Arrow Back"));
        }
    }
}
