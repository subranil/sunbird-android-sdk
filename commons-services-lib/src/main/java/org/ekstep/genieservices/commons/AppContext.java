package org.ekstep.genieservices.commons;

import org.ekstep.genieservices.commons.db.cache.IKeyValueStore;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.network.IConnectionInfo;
import org.ekstep.genieservices.commons.network.IHttpClient;

import java.io.File;

/**
 * Created on 14/4/17.
 *
 * @author shriharsh
 */
public abstract class AppContext<C> {

    private C mContext;

    private String mAppPackage;
    private String mKey;
    private String mGDataId;
    private String mGDataVersionName;

    protected AppContext(C context, String appPackage, String key, String gDataId) {
        this.mContext = context;
        this.mAppPackage = appPackage;
        this.mKey = key;
        this.mGDataId = gDataId;
        this.mGDataVersionName = "";
    }

    public String getGDataId() {
        return mGDataId;
    }

    public String getGDataVersionName() {
        return mGDataVersionName;
    }

    public C getContext() {
        return mContext;
    }

    public String getAppPackage() {
        return mAppPackage;
    }

    public String getKey() {
        return mKey;
    }

    public abstract IDBSession getDBSession();

    public abstract IDBSession getSummarizerDBSession();

    public abstract Void setDBSession(IDBSession session);

    public abstract Void setSummarizerDBSession(IDBSession session);

    public abstract IKeyValueStore getKeyValueStore();

    public abstract IConnectionInfo getConnectionInfo();

    public abstract IHttpClient getHttpClient();

    public abstract IDeviceInfo getDeviceInfo();

    public abstract IParams getParams();

    public abstract ILocationInfo getLocationInfo();

    public abstract File getPrimaryFilesDir();

}
