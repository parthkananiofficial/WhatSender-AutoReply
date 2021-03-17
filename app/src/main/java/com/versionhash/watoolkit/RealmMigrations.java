package com.versionhash.watoolkit;

import java.lang.reflect.Field;
import java.util.Set;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class RealmMigrations implements RealmMigration {

    private Class[] _models;

    public RealmMigrations(Class[] models) {
        _models = models;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        for (Class classModel : _models) {
            final RealmObjectSchema userSchema = schema.get(classModel.getSimpleName());

            Field[] fields = classModel.getDeclaredFields();
            Set<String> fieldsDatabase = userSchema.getFieldNames();

            for (String fieldDatabase : fieldsDatabase) {

                Boolean removed = true;

                for (Field field : fields) {
                    if (field.getName().compareToIgnoreCase(fieldDatabase) == 0) {
                        removed = false;
                        break;
                    }
                }

                if (removed) {
                    userSchema.removeField(fieldDatabase);
                }
            }

            for (Field field : fields) {
                Class type = field.getType();
                String name = field.getName();

                if (!userSchema.hasField(name)){
                    userSchema.addField(name, GetClassOfType(type));
                }
            }
        }
    }

    private Class GetClassOfType(Class type) {
        String name = type.getSimpleName();

        if (name.compareToIgnoreCase("string") == 0) {
            return String.class;
        } else if (name.compareToIgnoreCase("int") == 0) {
            return int.class;
        } else if (name.compareToIgnoreCase("boolean") == 0) {
            return Boolean.class;
        }  else if (name.compareToIgnoreCase("byte") == 0) {
            return byte.class;
        }  else if (name.compareToIgnoreCase("byte[]") == 0) {
            return byte[].class;
        }

        return Boolean.class;
    }
}
