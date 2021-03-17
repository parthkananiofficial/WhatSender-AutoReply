package com.versionhash.watoolkit;

import android.app.Application;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("mydb.realm")
                .migration(new RealmMigration() {  // Migration to run
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        RealmSchema schema = realm.getSchema();
                        RealmObjectSchema newRealmModel = schema.get("Rule"); // if a new Realm class is already added
                        if (!newRealmModel.hasField("conditionType")) {
                            newRealmModel.addField("conditionType", String.class);
                        }
                    }
                })
                .allowWritesOnUiThread(true)
                .schemaVersion(1)
                .build();

        Realm.setDefaultConfiguration(config);
        Realm.getInstance(config);
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }
}
