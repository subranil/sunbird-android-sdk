package org.ekstep.genieservices.profile;

import org.ekstep.genieservices.BaseService;
import org.ekstep.genieservices.ISummarizerService;
import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.SummaryRequest;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.LearnerAssessmentDetails;
import org.ekstep.genieservices.commons.bean.LearnerAssessmentSummary;
import org.ekstep.genieservices.commons.bean.telemetry.Telemetry;
import org.ekstep.genieservices.commons.utils.DateUtil;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.profile.db.model.LearnerAssessmentSummaryModel;
import org.ekstep.genieservices.profile.db.model.LearnerAssessmentDetailsModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the implementation of {@link ISummarizerService}
 *
 */
public class SummarizerServiceImpl extends BaseService implements ISummarizerService {


    private static final String TAG = SummarizerServiceImpl.class.getSimpleName();

    public SummarizerServiceImpl(AppContext appContext) {
        super(appContext);
    }

    @Override
    public GenieResponse<List<LearnerAssessmentSummary>> getSummary(SummaryRequest summaryRequest) {
        LearnerAssessmentSummaryModel learnerAssessmentSummaryModel = null;
        GenieResponse<List<LearnerAssessmentSummary>> response;
        String methodName = "getSummary@LearnerAssessmentsServiceImpl";
        HashMap params = new HashMap();
        params.put("logLevel", "2");

        if (summaryRequest.getUid() != null){
            learnerAssessmentSummaryModel = LearnerAssessmentSummaryModel.findChildProgressSummary(mAppContext.getDBSession(), summaryRequest.getUid());
        }else if(summaryRequest.getContentId() != null){
            learnerAssessmentSummaryModel = LearnerAssessmentSummaryModel.findContentProgressSummary(mAppContext.getDBSession(), summaryRequest.getContentId());
        }

        //if the assembleMap list size is 0 then their was some error
        if (learnerAssessmentSummaryModel.getAssessmentMap().size() == 0) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.PROCESSING_ERROR, ServiceConstants.ErrorMessage.UNABLE_TO_FIND_SUMMARY, TAG);
            return response;
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(learnerAssessmentSummaryModel.getAssessmentMap());
        return response;
    }

    @Override
    public GenieResponse<List<LearnerAssessmentDetails>> getLearnerAssessmentDetails(SummaryRequest summaryRequest) {
        GenieResponse<List<LearnerAssessmentDetails>> response;
        String methodName = "getLearnerAssessmentDetails@LearnerAssessmentsServiceImpl";
        HashMap params = new HashMap();
        params.put("logLevel", "2");
        LearnerAssessmentDetailsModel learnerAssessmentDetailsModel = LearnerAssessmentDetailsModel.findAssessmentByRequest(mAppContext.getDBSession(), summaryRequest);
        if (learnerAssessmentDetailsModel.getAllAssesments().size() == 0) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.PROCESSING_ERROR, ServiceConstants.ErrorMessage.UNABLE_TO_FIND_SUMMARY, TAG);
            return response;
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        response.setResult(learnerAssessmentDetailsModel.getAllAssesments());
        return response;
    }

    @Override
    public GenieResponse<Void> saveLearnerAssessmentDetails(Telemetry telemetry) {
        GenieResponse<Void> response;
        String methodName = "saveLearnerAssessmentDetails@LearnerAssessmentsServiceImpl";
        HashMap params = new HashMap();
        params.put("logLevel", "2");
        LearnerAssessmentDetails learnerAssessmentDetails = mapTelemtryToLearnerAssessmentData(telemetry);
        LearnerAssessmentDetailsModel learnerAssessmentDetailsModel = LearnerAssessmentDetailsModel.build(mAppContext.getDBSession(), learnerAssessmentDetails);
        learnerAssessmentDetailsModel.save();

        if (learnerAssessmentDetailsModel.getInsertedId() == -1) {
            response = GenieResponseBuilder.getErrorResponse(ServiceConstants.ErrorCode.PROCESSING_ERROR, ServiceConstants.ErrorMessage.UNABLE_TO_SAVE_LEARNER_ASSESSMENT, TAG);
            return response;
        }

        response = GenieResponseBuilder.getSuccessResponse(ServiceConstants.SUCCESS_RESPONSE);
        return response;
    }

    private LearnerAssessmentDetails mapTelemtryToLearnerAssessmentData(Telemetry telemetry) {
        LearnerAssessmentDetails learnerAssessmentDetails = new LearnerAssessmentDetails();
        learnerAssessmentDetails.setUid(telemetry.getUid());
        learnerAssessmentDetails.setContentId(telemetry.getGdata().getId());
        ;
        Map<String, Object> eks = (Map<String, Object>) telemetry.getEData().get("eks");
        learnerAssessmentDetails.setQid((String) eks.get("qid"));
        learnerAssessmentDetails.setQindex((Double) eks.get("qindex"));
        String pass = (String) eks.get("pass");
        learnerAssessmentDetails.setCorrect(("Yes".equalsIgnoreCase(pass) ? 1 : 0));
        learnerAssessmentDetails.setScore((Double) eks.get("score"));
        learnerAssessmentDetails.setTimespent((Double) eks.get("length"));
        if ("2.0".equalsIgnoreCase(telemetry.getVer())) {
            learnerAssessmentDetails.setTimestamp((Long) telemetry.getEts());
            learnerAssessmentDetails.setRes(GsonUtil.toJson(eks.get("resvalues")));
        } else {
            learnerAssessmentDetails.setTimestamp(DateUtil.dateToEpoch(telemetry.getTs()));
            learnerAssessmentDetails.setRes(GsonUtil.toJson(eks.get("res")));
        }
        learnerAssessmentDetails.setQdesc((String) eks.get("qdesc"));
        learnerAssessmentDetails.setQtitle((String) eks.get("qtitle"));
        return learnerAssessmentDetails;
    }


}
