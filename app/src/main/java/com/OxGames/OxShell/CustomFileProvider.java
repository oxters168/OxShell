package com.OxGames.OxShell;

import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class CustomFileProvider extends DocumentsProvider {
    private final String ROOT_FOLDER_ID = "roots";

    /* Default projection for a root when none supplied */
    private static final String[] DEFAULT_ROOT_PROJECTION = {
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
//            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
    };
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = {
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
    };

    public CustomFileProvider() {
    }

    @Override
    public boolean onCreate() {
        return false;
    }
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Query roots " + (projection != null ? projection : "null"));
        // If user is not logged in, return an empty root cursor.  This removes our
        // provider from the list entirely.
//        if (!isUserLoggedIn()) {
//            return result;
//        }

        // It's possible to have multiple roots (e.g. for multiple accounts in the
        // same app) -- just add multiple cursor rows.
//        final MatrixCursor.RowBuilder row = result.newRow();
//        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT);

        // FLAG_SUPPORTS_CREATE means at least one directory under the root supports
        // creating documents. FLAG_SUPPORTS_RECENTS means your application's most
        // recently used documents will show up in the "Recents" category.
        // FLAG_SUPPORTS_SEARCH allows users to search all documents the application
        // shares.
//        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_CREATE | DocumentsContract.Root.FLAG_SUPPORTS_RECENTS | DocumentsContract.Root.FLAG_SUPPORTS_SEARCH);

//        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
//        final MatrixCursor.RowBuilder row = result.newRow();
//        row.add(DocumentsContract.Root.COLUMN_TITLE, "OxShell");
//        row.add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher);
//        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "file/*");
//        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, "def");
//        row.add(DocumentsContract.Root.COLUMN_SUMMARY, "Ox Games");
//        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_EJECT);
//        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "mno");
//        return result;
//        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_FOLDER_ID);
//        row.add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher);
//        row.add(DocumentsContract.Root.COLUMN_TITLE, "OxShell");
//        row.add(DocumentsContract.Root.COLUMN_FLAGS, 0);
//        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_FOLDER_ID);
//        return result;
        return null;
    }
    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Query " + documentId + " " + (projection != null ? projection : "null"));
        //reference: https://github.com/jcraane/DocumentsProviderExample/blob/master/app/src/main/java/nl/jcraane/myapplication/provider/LocalDocumentsProvider.kt
//        PagedActivity currentActivity = ActivityManager.GetCurrentActivity();
//        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
//        if (documentId.equals(ROOT_FOLDER_ID)) {
//            Log.d("DocumentsProvider", "Creating row for roots query");
//            final MatrixCursor.RowBuilder row = result.newRow();
//            row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, ROOT_FOLDER_ID);
//            row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR);
//            row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, currentActivity.getPackageName());
//            row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null);
//            row.add(DocumentsContract.Document.COLUMN_FLAGS, 0);
//            row.add(DocumentsContract.Document.COLUMN_SIZE, null);
//        }
//        return result;
        return null;
    }
    @Override
    public Cursor queryChildDocuments(String s, String[] strings, String s1) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Query Child " + s + " " + (strings != null ? strings : "null") + " " + s1);
        return null;
    }
    @Override
    public ParcelFileDescriptor openDocument(String s, String s1, @Nullable CancellationSignal cancellationSignal) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Open " + s + " " + s1);
//        ParcelFileDescriptor data = null;
//        try {
//            data = ParcelFileDescriptor.open(new File(s), ParcelFileDescriptor.MODE_READ_ONLY);
//        } catch (FileNotFoundException ex) {
//            Log.d("FileChooser", ex.getMessage());
//        }
//        return data;
        return null;
    }

    public static Uri getUriForFile(String path) {
        PagedActivity currentActivity = ActivityManager.GetCurrentActivity();
//        Uri fileUri = FileProvider.getUriForFile(currentActivity, BuildConfig.DOCUMENTS_AUTHORITY, new File(path));
//        ParcelFileDescriptor data = null;
//        try {
//            data = ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_ONLY);
//        } catch (FileNotFoundException ex) {
//            Log.d("FileChooser", ex.getMessage());
//        }

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("content");
        uriBuilder.encodedAuthority(BuildConfig.DOCUMENTS_AUTHORITY);
        uriBuilder.encodedPath(path);
        Uri fileUri = uriBuilder.build();
        currentActivity.grantUriPermission(currentActivity.getPackageName(), fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

//        ContentResolver resolver = currentActivity.getContentResolver();
//        Cursor curses = resolver.query(Uri.parse(path), null, null, null);
//        String documentId = DocumentsContract.getDocumentId(fileUri);
//        Uri contractUri = DocumentsContract.buildDocumentUri(BuildConfig.DOCUMENTS_AUTHORITY, documentId);
        return fileUri;
    }

    // defining authority so that other application can access it
//    static final String PROVIDER_NAME = "com.demo.user.provider";
//
//    // defining content URI
//    static final String URL = "content://" + PROVIDER_NAME + "/users";
//
//    // parsing the content URI
//    static final Uri CONTENT_URI = Uri.parse(URL);
//
//    static final String id = "id";
//    static final String name = "name";
//    static final int uriCode = 1;
//    static final UriMatcher uriMatcher;
//    private static HashMap<String, String> values;
//
//    static {
//
//        // to match the content URI
//        // every time user access table under content provider
//        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//
//        // to access whole table
//        uriMatcher.addURI(PROVIDER_NAME, "users", uriCode);
//
//        // to access a particular row
//        // of the table
//        uriMatcher.addURI(PROVIDER_NAME, "users/*", uriCode);
//    }
//    @Override
//    public String getType(Uri uri) {
//        switch (uriMatcher.match(uri)) {
//            case uriCode:
//                return "vnd.android.cursor.dir/users";
//            default:
//                throw new IllegalArgumentException("Unsupported URI: " + uri);
//        }
//    }
    // creating the database
//    @Override
//    public boolean onCreate() {
//        Context context = getContext();
////        DatabaseHelper dbHelper = new DatabaseHelper(context);
////        db = dbHelper.getWritableDatabase();
////        if (db != null) {
////            return true;
////        }
//        return false;
//    }
//    @Override
//    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
//        qb.setTables(TABLE_NAME);
//        switch (uriMatcher.match(uri)) {
//            case uriCode:
//                qb.setProjectionMap(values);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//        if (sortOrder == null || sortOrder == "") {
//            sortOrder = id;
//        }
//        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
//                null, sortOrder);
//        c.setNotificationUri(getContext().getContentResolver(), uri);
//        return c;
//    }

    // adding data to the database
//    @Override
//    public Uri insert(Uri uri, ContentValues values) {
//        long rowID = db.insert(TABLE_NAME, "", values);
//        if (rowID > 0) {
//            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
//            getContext().getContentResolver().notifyChange(_uri, null);
//            return _uri;
//        }
//        throw new SQLiteException("Failed to add a record into " + uri);
//    }

//    @Override
//    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        int count = 0;
//        switch (uriMatcher.match(uri)) {
//            case uriCode:
//                count = db.update(TABLE_NAME, values, selection, selectionArgs);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//        getContext().getContentResolver().notifyChange(uri, null);
//        return count;
//    }

//    @Override
//    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        int count = 0;
//        switch (uriMatcher.match(uri)) {
//            case uriCode:
//                count = db.delete(TABLE_NAME, selection, selectionArgs);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//        getContext().getContentResolver().notifyChange(uri, null);
//        return count;
//    }
}
