package example.com.mpdlcamera.Model.TO;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 7/18/16.
 */

public class MetadataProfileTO {

    @Expose
    private String title;

    @Expose
    private List<StatementTO> statements = new ArrayList<StatementTO>();

    public MetadataProfileTO(String title, List<StatementTO> statements) {
        this.title = title;
        this.statements = statements;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<StatementTO> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementTO> statements) {
        this.statements = statements;
    }
}
