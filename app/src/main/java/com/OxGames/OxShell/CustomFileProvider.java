package com.OxGames.OxShell;

import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class CustomFileProvider extends DocumentsProvider {
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

    public CustomFileProvider() {
    }

    @Override
    public boolean onCreate() {
        return false;
    }
    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
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

        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(DocumentsContract.Root.COLUMN_TITLE, "OxShell");
        row.add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher);
        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "file/*");
        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, "def");
        row.add(DocumentsContract.Root.COLUMN_SUMMARY, "Ox Games");
//        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_CREATE | DocumentsContract.Root.FLAG_SUPPORTS_RECENTS | DocumentsContract.Root.FLAG_SUPPORTS_SEARCH);
        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD);
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "mno");
//        row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, 16);
        return result;
    }
    @Override
    public Cursor queryDocument(String s, String[] strings) throws FileNotFoundException {
        return null;
    }
    @Override
    public Cursor queryChildDocuments(String s, String[] strings, String s1) throws FileNotFoundException {
        return null;
    }
    @Override
    public ParcelFileDescriptor openDocument(String s, String s1, @Nullable CancellationSignal cancellationSignal) throws FileNotFoundException {
        return null;
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
