package example.com.mpdlcamera.Model.TO;

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
    private String id;

    @Expose
    private List<StatementTO> statements = new ArrayList<StatementTO>();

    public MetadataProfileTO(String title, String id, List<StatementTO> statements) {
        this.title = title;
        this.id = id;
        this.statements = statements;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
