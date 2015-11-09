/*
 * Copyright 2015 Jin Kwon &lt;jinahya_at_gmail.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.jinahya.sql.database.metadata.bind;


import static java.beans.Introspector.decapitalize;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class MetadataContext {


    private static final Logger logger = getLogger(Metadata.class.getName());


    private static final Map<Class<?>, Class<?>> WRAPPERS;


    static {
        final Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>(9);
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(double.class, Double.class);
        map.put(float.class, Float.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(short.class, Short.class);
        map.put(void.class, Void.class);
        WRAPPERS = Collections.unmodifiableMap(map);
    }


    private static Class<?> wrapper(final Class<?> primitive) {

        if (!primitive.isPrimitive()) {
            throw new IllegalArgumentException("not primitive: " + primitive);
        }

        return WRAPPERS.get(primitive);
    }


    private static String capitalize(final String name) {

        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }


    private static String getterName(final String fieldName) {

        return "get" + capitalize(fieldName);
    }


    private static String getterName(final Field field) {

        return getterName(field.getName());
    }


    private static String setterName(final String fieldName) {

        return "set" + capitalize(fieldName);
    }


    private static String setterName(final Field field) {

        return setterName(field.getName());
    }


    private static String suppression(final Field field, final Class<?> klass) {

        if (field == null) {
            throw new NullPointerException("null field");
        }

        if (klass == null) {
            throw new NullPointerException("null klass");
        }

        if (!field.getDeclaringClass().isAssignableFrom(klass)) {
            throw new IllegalArgumentException(
                "klass(" + klass
                + ") is not assignable to the specified field(" + field
                + ")'s declaring class(" + field.getDeclaringClass() + ")");
        }

        return decapitalize(klass.getSimpleName()) + "/" + field.getName();
    }


    private static String suppression(final Field field) {

        return suppression(field, field.getDeclaringClass());
    }


    private static <T> void parent(final Class<T> type,
                                   List<? super T> children,
                                   final Object parent)
        throws IllegalAccessException {

        // set parent
        for (final Field field : type.getDeclaredFields()) {
            if (field.getType() != parent.getClass()) {
                continue;
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
//            if (children.isEmpty()) {
//                logger.warning("children empty!!");
//            }
            for (final Object element : children) {
                field.set(element, parent);
            }
            break;
        }
    }


    public MetadataContext(final DatabaseMetaData database) {

        super();

        if (database == null) {
            throw new NullPointerException("null database");
        }

        this.database = database;
    }


    private boolean addSuppression(final String suppression) {

        if (suppression == null) {
            throw new NullPointerException("null suppression");
        }

        if (suppressions == null) {
            suppressions = new TreeSet<String>();
        }

        return suppressions.add(suppression);
    }


    /**
     * Add suppression paths.
     *
     * @param suppression the first suppression
     * @param otherSuppressions other suppressions
     *
     * @return this
     */
    public MetadataContext addSuppressions(
        final String suppression, final String... otherSuppressions) {

        addSuppression(suppression);

        if (otherSuppressions != null) {
            for (final String otherSuppression : otherSuppressions) {
                addSuppression(otherSuppression);
            }
        }

        return this;
    }


    private boolean suppressed(final String suppression) {

        if (suppression == null) {
            throw new NullPointerException("null suppression");
        }

        if (suppressions == null) {
            return false;
        }

        return suppressions.contains(suppression);
    }


    private Object fieldValue(final Field field, final Object obj)
        throws ReflectiveOperationException {

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        return field.get(obj);
    }


    private Object fieldValue(final Class<?> type, final String name,
                              final Object obj)
        throws ReflectiveOperationException {

        return fieldValue(type.getDeclaredField(name), obj);
    }


    private void fieldValue(final Field field, final Object obj, Object value,
                            final Object[] args)
        throws ReflectiveOperationException, SQLException {

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        final Class<?> fieldType = field.getType();
        try {
            field.set(obj, value);
            return;
        } catch (final IllegalArgumentException iae) {
        }
        final Class<?> valueType = value == null ? null : value.getClass();
        if (fieldType == Boolean.TYPE) {
            if (Number.class.isInstance(value)) {
                value = ((Number) value).intValue() != 0;
            }
            if (!Boolean.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            field.setBoolean(obj, (Boolean) value);
            return;
        }
        if (fieldType == Boolean.class) {
            if (Number.class.isInstance(value)) {
                value = ((Number) value).intValue() != 0;
            }
            if (value != null && !Boolean.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            field.set(obj, value);
            return;
        }
        if (fieldType == Short.TYPE) {
            if (value == null || !Number.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            field.setShort(obj, ((Number) value).shortValue());
            return;
        }
        if (fieldType == Short.class) {
            if (value != null && !Number.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            if (value != null && !Short.class.isInstance(value)) {
                value = ((Number) value).shortValue();
            }
            field.set(obj, value);
            return;
        }
        if (fieldType == Integer.TYPE) {
            if (value == null || !Number.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            if (Number.class.isInstance(value)) {
                value = ((Number) value).intValue();
            }
            field.setInt(obj, (Integer) value);
            return;
        }
        if (fieldType == Integer.class) {
            if (value != null && !Number.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            if (value != null && !Integer.class.isInstance(value)) {
                value = ((Number) value).intValue();
            }
            field.set(obj, value);
            return;
        }
        if (fieldType == Long.TYPE) {
            if (value == null || !Number.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            if (Number.class.isInstance(value)) {
                value = ((Number) value).longValue();
            }
            field.setLong(obj, (Long) value);
            return;
        }
        if (fieldType == Long.class) {
            if (value != null && !Number.class.isInstance(value)) {
                logger.log(Level.WARNING, "cannot set {0}({1}) to {2}",
                           new Object[]{value, valueType, field});
                return;
            }
            if (value != null && !Long.class.isInstance(value)) {
                value = ((Number) value).longValue();
            }
            field.set(obj, value);
            return;
        }
        if (fieldType == List.class) {
            @SuppressWarnings("unchecked")
            final List<Object> list = (List<Object>) field.getDeclaringClass()
                .getMethod(getterName(field)).invoke(obj);
//            final Type genericType = field.getGenericType();
//            if (!ParameterizedType.class.isInstance(genericType)) {
//                logger.log(Level.WARNING, "not a ParameterizedType({0}): {1}",
//                           new Object[]{genericType, field});
//            }
//            final Type elementType = ((ParameterizedType) genericType)
//                .getActualTypeArguments()[0];
            //final String typeName = elementType.getTypeName();
            //final Class<?> typeClass = Class.forName(typeName);
            final Class<?> type
                = (Class<?>) ((ParameterizedType) field.getGenericType())
                .getActualTypeArguments()[0];
            if (ResultSet.class.isInstance(value)) {
                //bindAll((ResultSet) value, typeClass, list);
                bindAll((ResultSet) value, type, list);
                parent(type, list, obj);
                return;
            }
//            list.add(typeClass
//                .getMethod("valueOf", Object[].class, Object.class)
//                .invoke(null, args, value));
            list.add(type
                .getDeclaredMethod("valueOf", Object[].class, Object.class)
                .invoke(null, args, value));
            parent(type, list, obj);
            return;
        }

        logger.log(Level.WARNING, "value({0}) not handled: {1}",
                   new Object[]{value, field});
    }


    private <T> T bindSingle(final ResultSet results, final Class<T> type,
                             final T obj)
        throws SQLException, ReflectiveOperationException {

        // set labeled fields
        if (results != null) {
            for (final Field field : type.getDeclaredFields()) {
                final String suppressionPath = suppression(field);
                if (suppressed(suppressionPath)) {
                    continue;
                }
                final Label label = field.getAnnotation(Label.class);
                if (label == null) {
                    continue;
                }
                final Object value;
                try {
                    value = results.getObject(label.value());
                } catch (final SQLException sqle) {
                    sqle.printStackTrace(System.err);
                    continue;
                }
                fieldValue(field, obj, value, null);
            }
        }
        // set invocation fields
        for (final Field field : type.getDeclaredFields()) {
            final String suppressionPath = suppression(field);
            if (suppressed(suppressionPath)) {
                continue;
            }
            final Invocation invocation = field.getAnnotation(Invocation.class);
            if (invocation == null) {
                continue;
            }
            final Class<?>[] types = invocation.types();
            final Method method = DatabaseMetaData.class.getMethod(
                invocation.name(), invocation.types());
            for (final InvocationArgs invocationArgs : invocation.argsarr()) {
                final String[] names = invocationArgs.value();
                final Object[] args = new Object[names.length];
                for (int i = 0; i < names.length; i++) {
                    final String name = names[i];
                    if ("null".equals(name)) {
                        args[i] = null;
                        continue;
                    }
                    if (name.startsWith(":")) {
                        args[i] = fieldValue(
                            type.getDeclaredField(name.substring(1)), obj);
                        continue;
                    }
                    if (types[i] == String.class) {
                        args[i] = name;
                        continue;
                    }
                    if (types[i].isPrimitive()) {
                        types[i] = wrapper(types[i]);
                    }
                    args[i] = types[i].getMethod("valueOf", String.class)
                        .invoke(null, name);
                }
                final Object value;
                try {
                    value = method.invoke(database, args);
                } catch (final Exception e) {
                    e.printStackTrace(System.err);
                    continue;
                }
                fieldValue(field, obj, value, args);
            }
        }

        return obj;
    }


    private <T> T bindSingle(final ResultSet results, final Class<T> type)
        throws SQLException, ReflectiveOperationException {

        return bindSingle(results, type, type.newInstance());
    }


    private <T> List<? super T> bindAll(final ResultSet results,
                                        final Class<T> type,
                                        final List<? super T> list)
        throws SQLException, ReflectiveOperationException {

        while (results.next()) {
            list.add(bindSingle(results, type, type.newInstance()));
        }

        return list;
    }


    private <T> List<? super T> bindAll(final ResultSet results,
                                        final Class<T> type)
        throws SQLException, ReflectiveOperationException {

        return bindAll(results, type, new ArrayList<T>());
    }


    /**
     * Binds all information.
     *
     * @return a Metadata
     *
     * @throws SQLException if a database occurs.
     * @throws ReflectiveOperationException if a reflection erorr occurs
     */
    public Metadata getMetadata()
        throws SQLException, ReflectiveOperationException {

        final Metadata metadata = bindSingle(null, Metadata.class);

        if (!suppressed("metadata/catalogs")) {
            final List<Catalog> catalogs = metadata.getCatalogs();
            if (catalogs.isEmpty()) {
                final Catalog catalog
                    = new Catalog().tableCat("").metadata(metadata);
                logger.log(Level.INFO, "adding an empty catalog: {0}",
                           new Object[]{catalog});
                catalogs.add(catalog);
                bindSingle(null, Catalog.class, catalog);
            }
            if (!suppressed("category/schemas")) {
                for (final Catalog catalog : catalogs) {
                    final List<Schema> schemas = catalog.getSchemas();
                    if (schemas.isEmpty()) {
                        final Schema schema = new Schema()
                            .tableCatalog(catalog.getTableCat())
                            .tableSchem("").catalog(catalog);
                        logger.log(Level.INFO, "adding an empty schema: {0}",
                                   new Object[]{schema});
                        schemas.add(schema);
                        bindSingle(null, Schema.class, schema);
                    }
                }
            }
        }

        return metadata;
    }


    public List<Attribute> getAttributes(final String catalog,
                                         final String schemaPattern,
                                         final String typeNamePattern,
                                         final String attributeNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<Attribute> list = new ArrayList<Attribute>();

        final ResultSet results = database.getAttributes(
            catalog, schemaPattern, typeNamePattern, attributeNamePattern);
        try {
            bindAll(results, Attribute.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<BestRowIdentifier> getBestRowIdentifier(
        final String catalog, final String schema, final String table,
        final int scope, final boolean nullable)
        throws SQLException, ReflectiveOperationException {

        final List<BestRowIdentifier> list = new ArrayList<BestRowIdentifier>();

        final ResultSet results = database.getBestRowIdentifier(
            catalog, schema, table, scope, nullable);
        try {
            bindAll(results, BestRowIdentifier.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<Catalog> getCatalogs()
        throws SQLException, ReflectiveOperationException {

        final List<Catalog> list = new ArrayList<Catalog>();

        final ResultSet results = database.getCatalogs();
        try {
            bindAll(results, Catalog.class, list);
        } finally {
            results.close();
        }

        if (false && list.isEmpty()) {
            final Catalog catalog = new Catalog();
            catalog.setTableCat("");
            bindSingle(null, Catalog.class, catalog);
            list.add(catalog);
        }

        return list;
    }


    public List<ClientInfoProperty> getClientInfoProperties()
        throws SQLException, ReflectiveOperationException {

        final List<ClientInfoProperty> list
            = new ArrayList<ClientInfoProperty>();

        final ResultSet results = database.getClientInfoProperties();
        try {
            bindAll(results, ClientInfoProperty.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<Column> getColumns(final String catalog,
                                   final String schemaPattern,
                                   final String tableNamePattern,
                                   final String columnNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<Column> list = new ArrayList<Column>();

        final ResultSet resultSet = database.getColumns(
            catalog, schemaPattern, tableNamePattern, columnNamePattern);
        try {
            bindAll(resultSet, Column.class, list);
        } finally {
            resultSet.close();
        }

        return list;
    }


    public List<ColumnPrivilege> getColumnPrivileges(
        final String catalog, final String schema, final String table,
        final String columnNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<ColumnPrivilege> list = new ArrayList<ColumnPrivilege>();

        final ResultSet results = database.getColumnPrivileges(
            catalog, schema, table, columnNamePattern);
        try {
            bindAll(results, ColumnPrivilege.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<FunctionColumn> getFunctionColumns(
        final String catalog, final String schemaPattern,
        final String functionNamePattern, final String columnNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<FunctionColumn> list = new ArrayList<FunctionColumn>();

        final ResultSet results = database.getFunctionColumns(
            catalog, schemaPattern, functionNamePattern, columnNamePattern);
        try {
            bindAll(results, FunctionColumn.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<Function> getFunctions(final String catalog,
                                       final String schemaPattern,
                                       final String functionNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<Function> list = new ArrayList<Function>();

        final ResultSet results = database.getFunctions(
            catalog, schemaPattern, functionNamePattern);
        try {
            bindAll(results, Function.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<ExportedKey> getExportedKeys(
        final String catalog, final String schema, final String table)
        throws SQLException, ReflectiveOperationException {

        final List<ExportedKey> list = new ArrayList<ExportedKey>();

        final ResultSet results = database.getExportedKeys(
            catalog, schema, table);
        try {
            bindAll(results, ExportedKey.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<ImportedKey> getImportedKeys(
        final String catalog, final String schema, final String table)
        throws SQLException, ReflectiveOperationException {

        final List<ImportedKey> list = new ArrayList<ImportedKey>();

        final ResultSet results = database.getImportedKeys(
            catalog, schema, table);
        try {
            bindAll(results, ImportedKey.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<IndexInfo> getIndexInfo(
        final String catalog, final String schema, final String table,
        final boolean unique, final boolean approximate)
        throws SQLException, ReflectiveOperationException {

        final List<IndexInfo> list = new ArrayList<IndexInfo>();

        final ResultSet results = database.getIndexInfo(
            catalog, schema, table, unique, approximate);
        try {
            bindAll(results, IndexInfo.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<PrimaryKey> getPrimaryKeys(
        final String catalog, final String schema, final String table)
        throws SQLException, ReflectiveOperationException {

        final List<PrimaryKey> list = new ArrayList<PrimaryKey>();

        final ResultSet results = database.getPrimaryKeys(
            catalog, schema, table);
        try {
            bindAll(results, PrimaryKey.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<ProcedureColumn> getProcedureColumns(
        final String catalog, final String schemaPattern,
        final String procedureNamePattern, final String columnNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<ProcedureColumn> list = new ArrayList<ProcedureColumn>();

        final ResultSet results = database.getProcedureColumns(
            catalog, schemaPattern, procedureNamePattern, columnNamePattern);
        try {
            bindAll(results, ProcedureColumn.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<Procedure> getProcedures(final String catalog,
                                         final String schemaPattern,
                                         final String procedureNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<Procedure> list = new ArrayList<Procedure>();

        final ResultSet results = database.getProcedures(
            catalog, schemaPattern, procedureNamePattern);
        try {
            bindAll(results, Procedure.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<PseudoColumn> getPseudoColumns(final String catalog,
                                               final String schemaPattern,
                                               final String tableNamePattern,
                                               final String columnNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<PseudoColumn> list = new ArrayList<PseudoColumn>();

        final ResultSet results = database.getPseudoColumns(
            catalog, schemaPattern, tableNamePattern, columnNamePattern);
        try {
            bindAll(results, PseudoColumn.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<SchemaName> getSchemas()
        throws SQLException, ReflectiveOperationException {

        final List<SchemaName> list = new ArrayList<SchemaName>();

        final ResultSet results = database.getSchemas();
        try {
            bindAll(results, SchemaName.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<Schema> getSchemas(final String catalog,
                                   final String schemaPattern)
        throws SQLException, ReflectiveOperationException {

        final List<Schema> list = new ArrayList<Schema>();

        final ResultSet results = database.getSchemas(
            catalog, schemaPattern);
        try {
            bindAll(results, Schema.class, list);
        } finally {
            results.close();
        }

        if (list.isEmpty()) {
            final Schema schema = new Schema();
            schema.setTableSchem("");
            list.add(schema);
        }

        return list;
    }


    public List<Table> getTables(final String catalog,
                                 final String schemaPattern,
                                 final String tableNamePattern,
                                 final String[] types)
        throws SQLException, ReflectiveOperationException {

        final List<Table> list = new ArrayList<Table>();

        final ResultSet results = database.getTables(
            catalog, schemaPattern, tableNamePattern, types);
        try {
            bindAll(results, Table.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<TablePrivilege> getTablePrivileges(
        final String catalog, final String schemaPattern,
        final String tableNamePattern)
        throws SQLException, ReflectiveOperationException {

        final List<TablePrivilege> list = new ArrayList<TablePrivilege>();

        final ResultSet results = database.getTablePrivileges(
            catalog, schemaPattern, tableNamePattern);
        try {
            bindAll(results, TablePrivilege.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<TableType> getTableTypes()
        throws SQLException, ReflectiveOperationException {

        final List<TableType> list = new ArrayList<TableType>();

        final ResultSet results = database.getTableTypes();
        try {
            bindAll(results, TableType.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<TypeInfo> getTypeInfo()
        throws SQLException, ReflectiveOperationException {

        final List<TypeInfo> list = new ArrayList<TypeInfo>();

        final ResultSet results = database.getTypeInfo();
        try {
            bindAll(results, TypeInfo.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<UserDefinedType> getUDTs(
        final String catalog, final String schemaPattern,
        final String typeNamePattern, final int[] types)
        throws SQLException, ReflectiveOperationException {

        final List<UserDefinedType> list = new ArrayList<UserDefinedType>();

        final ResultSet results = database.getUDTs(
            catalog, schemaPattern, typeNamePattern, types);
        try {
            bindAll(results, UserDefinedType.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    public List<VersionColumn> getVersionColumns(final String catalog,
                                                 final String schema,
                                                 final String table)
        throws SQLException, ReflectiveOperationException {

        final List<VersionColumn> list = new ArrayList<VersionColumn>();

        final ResultSet results = database.getVersionColumns(
            catalog, schema, table);
        try {
            bindAll(results, VersionColumn.class, list);
        } finally {
            results.close();
        }

        return list;
    }


    private final DatabaseMetaData database;


    private Set<String> suppressions;


}

