/*
 * Copyright 2011 Jin Kwon <jinahya at gmail.com>.
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


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
@XmlRootElement
@XmlType(
    propOrder = {
        "tableSchem",
        // ---------------------------------------------------------------------
        "functions", "procedures", "tables", "userDefinedTypes"
    }
)
public class Schema {


    @Override
    public String toString() {

        return super.toString() + "{" + "tableCatalog=" + tableCatalog
               + ", tableSchem=" + tableSchem + '}';
    }


    // ------------------------------------------------------------ tableCatalog
    public String getTableCatalog() {

        return tableCatalog;
    }


    public void setTableCatalog(final String tableCatalog) {

        this.tableCatalog = tableCatalog;
    }


    Schema tableCatalog(final String tableCatalog) {

        setTableCatalog(tableCatalog);

        return this;
    }


    // -------------------------------------------------------------- tableSchem
    public String getTableSchem() {

        return tableSchem;
    }


    public void setTableSchem(final String tableSchem) {

        this.tableSchem = tableSchem;
    }


    Schema tableSchem(final String tableSchem) {

        setTableSchem(tableSchem);

        return this;
    }


    // ----------------------------------------------------------------- catalog
    public Catalog getCatalog() {

        return catalog;
    }


    //@XmlAttribute
    @Deprecated
    private String getCatalogString() {

        return catalog == null ? null : catalog.toString();
    }


    public void setCatalog(final Catalog catalog) {

        this.catalog = catalog;
    }


    Schema catalog(final Catalog catalog) {

        setCatalog(catalog);

        return this;
    }


    // --------------------------------------------------------------- functions
    public List<Function> getFunctions() {

        if (functions == null) {
            functions = new ArrayList<Function>();
        }

        return functions;
    }


    // -------------------------------------------------------------- procedures
    public List<Procedure> getProcedures() {

        if (procedures == null) {
            procedures = new ArrayList<Procedure>();
        }

        return procedures;
    }


    // ------------------------------------------------------------------ tables
    public List<Table> getTables() {

        if (tables == null) {
            tables = new ArrayList<Table>();
        }

        return tables;
    }


    public List<String> getTableNames() {

        final List<String> tableNames
            = new ArrayList<String>(getTables().size());

        for (final Table table : getTables()) {
            tableNames.add(table.getTableName());
        }

        return tableNames;
    }


    public Table getTableByName(final String tableName) {

        if (tableName == null) {
            throw new NullPointerException("null tableName");
        }

        for (final Table table : getTables()) {
            if (tableName.equals(table.getTableName())) {
                return table;
            }
        }

        return null;
    }


    // -------------------------------------------------------- userDefinedTypes
    public List<UserDefinedType> getUserDefinedTypes() {

        if (userDefinedTypes == null) {
            userDefinedTypes = new ArrayList<UserDefinedType>();
        }

        return userDefinedTypes;
    }


    // -------------------------------------------------------------------------
    @Label("TABLE_CATALOG")
    @NillableBySpecification
    @XmlAttribute
    private String tableCatalog;


    @Label("TABLE_SCHEM")
    @XmlElement(required = true)
    private String tableSchem;


    @XmlTransient
    private Catalog catalog;


    @Invocation(
        name = "getFunctions",
        types = {String.class, String.class, String.class},
        argsarr = {
            @InvocationArgs({":tableCatalog", ":tableSchem", "null"})
        }
    )
    @XmlElementRef
    private List<Function> functions;


    @Invocation(
        name = "getProcedures",
        types = {String.class, String.class, String.class},
        argsarr = {
            @InvocationArgs({":tableCatalog", ":tableSchem", "null"})
        }
    )
    @XmlElementRef
    private List<Procedure> procedures;


    @Invocation(
        name = "getTables",
        types = {String.class, String.class, String.class, String[].class},
        argsarr = {
            @InvocationArgs({":tableCatalog", ":tableSchem", "null", "null"})
        }
    )
    @XmlElementRef
    private List<Table> tables;


    @Invocation(
        name = "getUDTs",
        types = {String.class, String.class, String.class, int[].class},
        argsarr = {
            @InvocationArgs({":tableCatalog", ":tableSchem", "null", "null"})
        }
    )
    @XmlElementRef
    private List<UserDefinedType> userDefinedTypes;


}
