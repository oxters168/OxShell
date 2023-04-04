package com.OxGames.OxShell;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

import android.provider.DocumentsContract;
import android.util.Log;

public class OxProvider extends DocumentsProvider {
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

    public OxProvider() {
    }

    @Override
    public boolean onCreate() {
        return true;
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

        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        final MatrixCursor.RowBuilder row = result.newRow();
//        row.add(DocumentsContract.Root.COLUMN_TITLE, "OxShell");
//        row.add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher);
//        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "file/*");
//        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, "def");
//        row.add(DocumentsContract.Root.COLUMN_SUMMARY, "Ox Games");
//        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_EJECT);
//        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, "mno");
//        return result;
        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_FOLDER_ID);
        row.add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher);
        row.add(DocumentsContract.Root.COLUMN_TITLE, "Ox Shell");
        row.add(DocumentsContract.Root.COLUMN_FLAGS, 0);
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_FOLDER_ID);
        return result;
        //return null;
    }
    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Query " + documentId + " " + (projection != null ? projection : "null"));
        //reference: https://github.com/jcraane/DocumentsProviderExample/blob/master/app/src/main/java/nl/jcraane/myapplication/provider/LocalDocumentsProvider.kt
//        PagedActivity currentActivity = ActivityManager.GetCurrentActivity();
        PagedActivity currentActivity = OxShellApp.getCurrentActivity();
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        if (documentId.equals(ROOT_FOLDER_ID)) {
            Log.d("DocumentsProvider", "Creating row for roots query");
            final MatrixCursor.RowBuilder row = result.newRow();
            row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, ROOT_FOLDER_ID);
            row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR);
            row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, currentActivity.getPackageName());
            row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null);
            row.add(DocumentsContract.Document.COLUMN_FLAGS, 0);
            row.add(DocumentsContract.Document.COLUMN_SIZE, null);
        }
        return result;
        //return null;
    }
    @Override
    public Cursor queryChildDocuments(String s, String[] strings, String s1) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Query Child " + s + " " + (strings != null ? strings : "null") + " " + s1);
        return null;
    }
    @Override
    public ParcelFileDescriptor openDocument(String s, String s1, @Nullable CancellationSignal cancellationSignal) throws FileNotFoundException {
        Log.d("DocumentsProvider", "Open " + s + " " + s1);
        ParcelFileDescriptor data = null;
        try {
            data = ParcelFileDescriptor.open(new File(s), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException ex) {
            Log.d("FileChooser", ex.getMessage());
        }
        return data;
        //return null;
    }
}

//public class OxProvider extends DocumentsProvider {
//    @Override
//    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
//        return null;
//    }
//    @Override
//    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
//        return null;
//    }
//    @Override
//    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
//        return null;
//    }
//    @Override
//    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
//        return null;
//    }
//    @Override
//    public boolean onCreate() {
//        return false;
//    }
//}
