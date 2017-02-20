package de.mpg.mpdl.labcam.code.data.db;

import android.content.Context;
import android.text.TextUtils;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LiteOrmManager {

    private static volatile LiteOrmManager sInstance;

    private static LiteOrm liteOrm;

    private LiteOrmManager(Context context) {
        if (liteOrm == null) {
            DataBaseConfig config = new DataBaseConfig(context, "tsb.db");
            config.debugged = true; // open the log
            config.dbVersion = 1; // set database version
            liteOrm = LiteOrm.newSingleInstance(config);
        }
        liteOrm.setDebugged(true); // open the log
    }

    public static LiteOrmManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (LiteOrmManager.class) {
                if (null == sInstance) {
                    sInstance = new LiteOrmManager(context);
                }
            }
        }
        return sInstance;
    }

//    public LiteOrm getCascadeInstance() {
//        return liteOrm.cascade();
//    }

    public LiteOrm getSingleInstance() {
        return liteOrm.single();
    }

    public <T> int save(Collection<T> list, Class<T> clazz) {
        LiteOrm lOrm = liteOrm.single();
        lOrm.deleteAll(clazz);
        return lOrm.save(list);
    }

    public <T> List<T> query(Class<T> clazz) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.query(clazz);
    }

    public <T> int delete(T t) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.delete(t);
    }

    public <T> int update(T t) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.update(t);
    }

    public <T> long save(T t) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.save(t);
    }

    public <T> int delete(Class<T> clazz) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.delete(clazz);
    }

    public <T> int deleteByWhereBuilder(Class<T> clazz, String column, Object value) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.delete(new WhereBuilder(clazz).equals(column, value));
    }

    public <T> int deleteByWhereBuilder(Class<T> clazz, String column, Object value, String column1, Object value1) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.delete(new WhereBuilder(clazz).equals(column, value).and().equals(column1, value1));
    }

    public <T extends Object> T queryObject(Class<T> clazz) {
        List<T> list = query(clazz);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public <T> ArrayList<T> queryByEqual(String column, Object keyword, Class<T> clazz) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.query(new QueryBuilder<>(clazz).whereEquals(column, keyword).appendOrderDescBy("id"));
    }

    public <T extends Object> T queryObjectByEqual(String name, Object keyword, Class<T> clazz) {
        ArrayList<T> list = queryByEqual(name, keyword, clazz);
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public <T> ArrayList<T> queryByEqual(String column, Object keyword, Class<T> clazz, boolean asc, String ascKey) {
        LiteOrm lOrm = liteOrm.single();
        if (TextUtils.isEmpty(ascKey)) {
            return lOrm.query(new QueryBuilder<>(clazz).whereEquals(column, keyword));
        }
        if (asc) {
            return lOrm.query(new QueryBuilder<>(clazz).whereEquals(column, keyword).appendOrderAscBy(ascKey));
        } else {
            return lOrm.query(new QueryBuilder<>(clazz).whereEquals(column, keyword).appendOrderDescBy(ascKey));
        }
    }

    public <T> ArrayList<T> queryByEqual(String column, Object keyword, String column1, Object keyword1, Class<T> clazz) {
        LiteOrm lOrm = liteOrm.single();
        return lOrm.query(new QueryBuilder<>(clazz).where(column + "=?", keyword).whereAnd(column1 + "=?", keyword1));
    }

}
