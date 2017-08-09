package de.mpg.mpdl.labcam.code.mvp.view;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.base.BaseView;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;

/**
 * Created by yingli on 3/28/17.
 */

public interface RemoteCollectionDialogView extends BaseView{
    void getCollectionsSuc(CollectionMessage model);
    void getCollectionsFail(Throwable e);
    void noInternet();

    void createCollectionsSuc(ImejiFolderModel model);
    void createCollectionsFail(Throwable e);
}
