package org.ekstep.genieproviders.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.ekstep.genieproviders.BaseContentProvider;
import org.ekstep.genieproviders.IHandleUri;

import java.util.List;

public abstract class AbstractContentProvider extends BaseContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List<ContentUriHandler> handlers = ContentUriHandlerFactory.uriHandlers(getCompletePath(), getContext(), selection, selectionArgs);
        for (IHandleUri handler : handlers) {
            if (handler.canProcess(uri)) {
                return handler.process();
            }
        }

        return null;
    }


    @Override
    public String getType(Uri uri) {
        return String.format("vnd.android.cursor.item/%s.provider.content", getPackageName());
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        List<ContentUriHandler> handlers = ContentUriHandlerFactory.uriHandlers(getCompletePath(), getContext(), null, null);
        for (IHandleUri handler : handlers) {
            if (handler.canProcess(uri)) {
                return handler.insert(uri, values);
            }
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private String getCompletePath() {
        String CONTENT_PATH = "content";
        return String.format("%s.%s", getPackageName(), CONTENT_PATH);
    }

    public abstract String getPackageName();

    @Override
    public String getPackage() {
        return  getPackageName();
    }
}
