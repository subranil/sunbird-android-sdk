package org.ekstep.genieservices.profile.chained.export;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.ImportContext;
import org.ekstep.genieservices.commons.bean.Profile;
import org.ekstep.genieservices.commons.chained.IChainable;
import org.ekstep.genieservices.commons.db.contract.LearnerAssessmentsEntry;
import org.ekstep.genieservices.commons.db.contract.LearnerContentSummaryEntry;
import org.ekstep.genieservices.commons.db.contract.MetaEntry;
import org.ekstep.genieservices.commons.db.contract.ProfileEntry;
import org.ekstep.genieservices.commons.db.contract.UserEntry;
import org.ekstep.genieservices.commons.db.model.CustomReadersModel;
import org.ekstep.genieservices.commons.db.operations.IDBSession;
import org.ekstep.genieservices.commons.db.operations.IDBTransaction;
import org.ekstep.genieservices.commons.utils.Logger;
import org.ekstep.genieservices.commons.utils.StringUtil;
import org.ekstep.genieservices.profile.db.model.UserModel;
import org.ekstep.genieservices.profile.db.model.UserProfileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created on 6/10/2017.
 *
 * @author anil
 */
public class CleanupExportedFile implements IChainable {

    private static final String TAG = CleanupExportedFile.class.getSimpleName();
    private IChainable nextLink;

    private List<String> userIds;
    private String destinationDBFilePath;

    public CleanupExportedFile(String destinationDBFilePath, List<String> userIds) {
        this.destinationDBFilePath = destinationDBFilePath;
        this.userIds = userIds;
    }

    @Override
    public GenieResponse<Void> execute(AppContext appContext, ImportContext importContext) {

        List<String> allTables = getAllTables(importContext.getDBSession());
        List<String> allTableToExclude = getAllTableToExclude();

        removeTables(importContext.getDBSession(), allTables, allTableToExclude);

        deleteUnwantedProfilesAndUsers(importContext.getDBSession());
        deleteUnwantedProfileSummary(importContext.getDBSession());

        try {
            removeJournalFile();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());

            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.EXPORT_FAILED, e.getMessage(), TAG);
        }

        if (nextLink != null) {
            return nextLink.execute(appContext, importContext);
        } else {
            return GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.EXPORT_FAILED, "Export profile failed", TAG);
        }
    }

    @Override
    public IChainable then(IChainable link) {
        nextLink = link;
        return link;
    }

    private List<String> getAllTables(IDBSession dbSession) {
        List<String> tables;

        String allTablesQuery = String.format(Locale.US, "select name from sqlite_master where type='%s'", "table");
        CustomReadersModel customReadersModel = CustomReadersModel.find(dbSession, allTablesQuery);
        if (customReadersModel != null) {
            tables = customReadersModel.getDataList();
        } else {
            tables = new ArrayList<>();
        }

        return tables;
    }

    private List<String> getAllTableToExclude() {
        List<String> tablesToExclude = new ArrayList<>();

        tablesToExclude.add(MetaEntry.TABLE_NAME);
        tablesToExclude.add(UserEntry.TABLE_NAME);
        tablesToExclude.add(ProfileEntry.TABLE_NAME);
        tablesToExclude.add(LearnerAssessmentsEntry.TABLE_NAME);
        tablesToExclude.add(LearnerContentSummaryEntry.TABLE_NAME);

        return tablesToExclude;
    }

    private void removeTables(IDBSession dbSession, List<String> allTables, List<String> allTableToExclude) {
        for (String table : allTables) {
            if (allTableToExclude.contains(table)) {
                continue;
            }
            String dropTableQuery = String.format(Locale.US, "DROP TABLE IF EXISTS %s", table);
            dbSession.execute(dropTableQuery);
        }
    }

    private void deleteUnwantedProfilesAndUsers(IDBSession dbSession) {
        List<Profile> profilesToRetain = new ArrayList<>();
        List<UserModel> userToRetain = new ArrayList<>();

        for (String uid : userIds) {
            UserProfileModel userProfileModel = UserProfileModel.find(dbSession, uid);
            if (userProfileModel != null) {
                profilesToRetain.add(userProfileModel.getProfile());
            }

            UserModel userModel = UserModel.findByUserId(dbSession, uid);
            if (userModel != null) {
                userToRetain.add(userModel);
            }
        }

        cleanTable(dbSession, ProfileEntry.TABLE_NAME);
        cleanTable(dbSession, UserEntry.TABLE_NAME);

        for (Profile profile : profilesToRetain) {
            UserProfileModel userProfileModel = UserProfileModel.build(dbSession, profile);
            userProfileModel.save();
        }

        for (UserModel user : userToRetain) {
            UserModel userModel = UserModel.build(dbSession, user.getUid());
            userModel.save();
        }
    }

    private void cleanTable(IDBSession dbSession, String tableName) {
        String deleteProfilesTable = String.format(Locale.US, "Delete from %s", tableName);
        dbSession.execute(deleteProfilesTable);
    }

    private void deleteUnwantedProfileSummary(IDBSession dbSession) {
        dbSession.executeInTransaction(new IDBTransaction() {
            @Override
            public Void perform(IDBSession dbSession) {
                String commaSeparatedUids = "'" + StringUtil.join("','", userIds) + "'";
                String delLearnerAssesmentQuery = "DELETE FROM " + LearnerAssessmentsEntry.TABLE_NAME + " WHERE " + LearnerAssessmentsEntry.COLUMN_NAME_UID + "NOT IN(" + commaSeparatedUids + ")";
                String delLearnerSummaryQuery = "DELETE FROM " + LearnerContentSummaryEntry.TABLE_NAME + " WHERE " + LearnerAssessmentsEntry.COLUMN_NAME_UID + "NOT IN(" + commaSeparatedUids + ")";

                dbSession.execute(delLearnerAssesmentQuery);
                dbSession.execute(delLearnerSummaryQuery);
                return null;
            }
        });
    }

    private void removeJournalFile() throws Exception {
        File file = new File(destinationDBFilePath + "-journal");
        file.delete();
    }
}