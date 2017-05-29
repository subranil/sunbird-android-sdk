package org.ekstep.genieproviders.partner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import org.ekstep.genieproviders.IUriHandler;
import org.ekstep.genieproviders.util.Constants;
import org.ekstep.genieservices.GenieService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.PartnerData;
import org.ekstep.genieservices.commons.utils.GsonUtil;

import java.util.Locale;

/**
 * Created on 25/5/17.
 * shriharsh
 */

public class EndPartnerUriHandler implements IUriHandler {

    private String authority;
    private Context context;
    private String selection;
    private GenieService genieService;

    public EndPartnerUriHandler(String authority, Context context, String selection, String[] selectionArgs, GenieService genieService) {
        this.authority = authority;
        this.context = context;
        this.selection = selection;
        this.genieService = genieService;
    }

    @Override
    public Cursor process() {
        PartnerData partnerData = GsonUtil.fromJson(selection, PartnerData.class);
        GenieResponse response = genieService.getPartnerService().terminatePartnerSession(partnerData);
        if (response.getStatus()) {
            return convertToCursor(response);
        }

        return null;
    }

    private Cursor convertToCursor(GenieResponse response) {
        String[] partnerColumns = {Constants.PARTNER_CURSOR_KEY};
        MatrixCursor cursor = new MatrixCursor(partnerColumns);
        return populate(cursor, response);
    }

    public Cursor populate(MatrixCursor cursor, GenieResponse response) {
        cursor.addRow(new Object[]{GsonUtil.toJson(response)});
        return cursor;
    }

    @Override
    public boolean canProcess(Uri uri) {
        String urlPath = String.format(Locale.US, "content://%s/endPartnerSession", authority);
        return uri != null && urlPath.equals(uri.toString());
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
}
