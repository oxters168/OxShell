package com.OxGames.OxShell.Data;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.OxGames.OxShell.OxShellApp;

public class Metadata {
    private Bitmap albumArt;
    public Bitmap getAlbumArt() {
        return albumArt;
    }
    private String trackNumber;
    public String getTrackNumber() {
        return trackNumber;
    }
    private String album;
    public String getAlbum() {
        return album;
    }
    private String artist;
    public String getArtist() {
        return artist;
    }
    private String author;
    public String getAuthor() {
        return author;
    }
    private String composer;
    public String getComposer() {
        return composer;
    }
    private String date;
    public String getDate() {
        return date;
    }
    private String genre;
    public String getGenre() {
        return genre;
    }
    private String title;
    public String getTitle() {
        return title;
    }
    private String year;
    public String getYear() {
        return year;
    }
    private String duration;
    public String getDuration() {
        return duration;
    }
    private String numTracks;
    public String getNumTracks() {
        return numTracks;
    }
    private String writer;
    public String getWriter() {
        return writer;
    }
    private String mimeType;
    public String getMimeType() {
        return mimeType;
    }
    private String albumArtist;
    public String getAlbumArtist() {
        return albumArtist;
    }
    private String discNumber;
    public String getDiscNumber() {
        return discNumber;
    }
    private String compilation;
    public String getCompilation() {
        return compilation;
    }
    private String hasAudio;
    public String getHasAudio() {
        return hasAudio;
    }
    private String hasVideo;
    public String getHasVideo() {
        return hasVideo;
    }
    private String videoWidth;
    public String getVideoWidth() {
        return videoWidth;
    }
    private String videoHeight;
    public String getVideoHeight() {
        return videoHeight;
    }
    private String bitrate;
    public String getBitrate() {
        return bitrate;
    }
    private String location;
    public String getLocation() {
        return location;
    }
    private String videoRotation;
    public String getVideoRotation() {
        return videoRotation;
    }
    private String captureFramerate;
    public String getCaptureFramerate() {
        return captureFramerate;
    }
    private String hasImage;
    public String getHasImage() {
        return hasImage;
    }
    private String imageCount;
    public String getImageCount() {
        return imageCount;
    }
    private String imagePrimary;
    public String getImagePrimary() {
        return imagePrimary;
    }
    private String imageWidth;
    public String getImageWidth() {
        return imageWidth;
    }
    private String imageHeight;
    public String getImageHeight() {
        return imageHeight;
    }
    private String imageRotation;
    public String getImageRotation() {
        return imageRotation;
    }
    private String videoFrameCount;
    public String getVideoFrameCount() {
        return videoFrameCount;
    }
    private String exifOffset;
    public String getExifOffset() {
        return exifOffset;
    }
    private String exifLength;
    public String getExifLength() {
        return exifLength;
    }
    private String colorStandard;
    public String getColorStandard() {
        return colorStandard;
    }
    private String colorTransfer;
    public String getColorTransfer() {
        return colorTransfer;
    }
    private String colorRange;
    public String getColorRange() {
        return colorRange;
    }
    private String sampleRate;
    public String getSampleRate() {
        return sampleRate;
    }
    private String bitsPerSample;
    public String getBitsPerSample() {
        return bitsPerSample;
    }
    private String xmpOffset;
    public String getXmpOffset() {
        return xmpOffset;
    }
    private String xmpLength;
    public String getXmpLength() {
        return xmpLength;
    }

    private Metadata() {}

    //    public void setTrackDetails() {
//        // First, make sure that you have the appropriate write permissions to the file.
//
//        ContentResolver resolver = OxShellApp.getContext().getContentResolver();
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//
//        // Set the title, artist, and album name.
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Audio.Media.TITLE, "New Title");
//        values.put(MediaStore.Audio.Media.ARTIST, "New Artist");
//        values.put(MediaStore.Audio.Media.ALBUM, "New Album");
//        resolver.update(uri, values, MediaStore.Audio.Media.DATA + "=?", new String[]{filePath});
//
//        // Set the album art.
//        Bitmap bitmap = BitmapFactory.decodeFile(albumArtPath);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        byte[] byteArray = byteArrayOutputStream.toByteArray();
//        values.clear();
//        values.put(MediaStore.Audio.Albums.ALBUM_ART, byteArray);
//        resolver.update(uri, values, MediaStore.Audio.Media.DATA + "=?", new String[]{filePath});
//    }
    
    public static Metadata getMediaMetadata(DataRef dataRef) {
        Metadata metadata = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            AssetFileDescriptor afd = null;
            if (dataRef.getLocType() == DataLocation.asset) {
                afd = OxShellApp.getContext().getAssets().openFd((String)dataRef.getLoc());
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } else if (dataRef.getLocType() == DataLocation.file)
                retriever.setDataSource((String)dataRef.getLoc());
            else if (dataRef.getLocType() == DataLocation.resolverUri)
                retriever.setDataSource(OxShellApp.getContext(), (Uri)dataRef.getLoc());
            else
                throw new UnsupportedOperationException("Cannot load data type: " + dataRef.getLocType());

            metadata = new Metadata();

            metadata.trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            metadata.album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            metadata.artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            metadata.author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
            metadata.composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
            metadata.date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
            metadata.genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            metadata.title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            metadata.year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            metadata.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            metadata.numTracks = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS);
            metadata.writer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER);
            metadata.mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            metadata.albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
            metadata.discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER);
            metadata.compilation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION);
            metadata.hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            metadata.hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            metadata.videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            metadata.videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            metadata.bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            metadata.location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
            metadata.videoRotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            metadata.captureFramerate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                metadata.hasImage = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE);
                metadata.imageCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_COUNT);
                metadata.imagePrimary = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_PRIMARY);
                metadata.imageWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH);
                metadata.imageHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT);
                metadata.imageRotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION);
                metadata.videoFrameCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                metadata.exifOffset = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_EXIF_OFFSET);
                metadata.exifLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_EXIF_LENGTH);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metadata.colorStandard = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_STANDARD);
                metadata.colorTransfer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_TRANSFER);
                metadata.colorRange = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COLOR_RANGE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                metadata.sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE);
                metadata.bitsPerSample = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE);
                metadata.xmpOffset = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_XMP_OFFSET);
                metadata.xmpLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_XMP_LENGTH);
            }

            byte[] albumArtData = retriever.getEmbeddedPicture();
            if (albumArtData != null)
                metadata.albumArt = BitmapFactory.decodeByteArray(albumArtData, 0, albumArtData.length);

            if (afd != null)
                afd.close();
            retriever.release();
        } catch (Exception e) {
            Log.e("Metadata", "Failed to get metadata: " + e);
        }
        return metadata;
    }
    public static String getTitle(DataRef dataRef) {
        String title = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            AssetFileDescriptor afd = null;
            if (dataRef.getLocType() == DataLocation.asset) {
                afd = OxShellApp.getContext().getAssets().openFd((String)dataRef.getLoc());
                retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } else if (dataRef.getLocType() == DataLocation.file)
                retriever.setDataSource((String)dataRef.getLoc());
            else if (dataRef.getLocType() == DataLocation.resolverUri)
                retriever.setDataSource(OxShellApp.getContext(), (Uri)dataRef.getLoc());
            else
                throw new UnsupportedOperationException("Cannot load data type: " + dataRef.getLocType());

            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

            if (afd != null)
                afd.close();
            retriever.release();
        } catch (Exception e) {
            Log.e("Metadata", "Failed to get metadata: " + e);
        }
        return title;
    }
}
