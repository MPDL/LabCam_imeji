package de.mpg.mpdl.labcam.code.mvp.view;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.base.BaseView;

/**
 * Created by yingli on 3/29/17.
 */

public interface ImejiView extends BaseView{
    void getItemsSuc(ItemMessage model);
    void getItemsFail(Throwable e);

    void getCollectionsSuc(CollectionMessage model);
    void getCollectionsFail(Throwable e);
}
