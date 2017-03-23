package de.mpg.mpdl.labcam.code.mvp.view;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.base.BaseView;

/**
 * Created by yingli on 3/23/17.
 */

public interface RemoteCollectionSettingsView extends BaseView{

    void getCollectionsSuc(CollectionMessage model);
    void getCollectionsFail(Throwable e);
}
