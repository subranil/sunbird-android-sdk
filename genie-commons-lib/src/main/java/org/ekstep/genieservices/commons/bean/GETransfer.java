package org.ekstep.genieservices.commons.bean;

import org.ekstep.genieservices.commons.ITelemetry;
import org.ekstep.genieservices.commons.utils.GsonUtil;

import java.util.List;

/**
 * Created by swayangjit on 2/5/17.
 */

public class GETransfer extends BaseTelemetry  {

    private final String eid = "GE_TRANSFER";

    public GETransfer(String gameID, String gameVersion, GETransferEventKnowStructure eks) {
        super(gameID, gameVersion);
        setEks(eks);
    }

    @Override
    public String getEID() {
        return eid;
    }

}

