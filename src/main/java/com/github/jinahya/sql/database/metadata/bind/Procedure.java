/*
 * Copyright 2013 Jin Kwon <onacit at gmail.com>.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

/**
 * An entity class for binding the result of
 * {@link java.sql.DatabaseMetaData#getProcedures(java.lang.String, java.lang.String, java.lang.String)}.
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
@XmlRootElement
@XmlType(propOrder = {
    "procedureName", "remarks", "procedureType", "specificName",
    // ---------------------------------------------------------------------
    "procedureColumns"
})
public class Procedure implements Serializable {

    private static final long serialVersionUID = -6262056388403934829L;

    // -------------------------------------------------------------------------
    private static final Logger logger = getLogger(Procedure.class.getName());

    // -------------------------------------------------------------------------
    @Override
    public String toString() {
        return super.toString() + "{"
               + "procedureCat=" + procedureCat
               + ",procedureSchem=" + procedureSchem
               + ",procedureName=" + procedureName
               + ",remarks=" + remarks
               + ",procedureType=" + procedureType
               + ",specificName=" + specificName
               + "}";
    }

    // ------------------------------------------------------------ procedureCat
//    public String getProcedureCat() {
//        return procedureCat;
//    }
//
//    public void setProcedureCat(final String procedureCat) {
//        this.procedureCat = procedureCat;
//    }
    // ---------------------------------------------------------- procedureSchem
//    public String getProcedureSchem() {
//        return procedureSchem;
//    }
//
//    public void setProcedureSchem(final String procedureSchem) {
//        this.procedureSchem = procedureSchem;
//    }
    // ----------------------------------------------------------- procedureName
//    public String getProcedureName() {
//        return procedureName;
//    }
//
//    public void setProcedureName(final String procedureName) {
//        this.procedureName = procedureName;
//    }
    // ----------------------------------------------------------------- remarks
//    public String getRemarks() {
//        return remarks;
//    }
//
//    public void setRemarks(final String remarks) {
//        this.remarks = remarks;
//    }
    // ----------------------------------------------------------- procedureType
//    public short getProcedureType() {
//        return procedureType;
//    }
//
//    public void setProcedureType(final short procedureType) {
//        this.procedureType = procedureType;
//    }
    // ------------------------------------------------------------ specificName
//    public String getSpecificName() {
//        return specificName;
//    }
//
//    public void setSpecificName(final String specificName) {
//        this.specificName = specificName;
//    }
    // -------------------------------------------------------- procedureColumns
    public List<ProcedureColumn> getProcedureColumns() {
        if (procedureColumns == null) {
            procedureColumns = new ArrayList<ProcedureColumn>();
        }
        return procedureColumns;
    }

//    @Deprecated
//    public void setProcedureColumns(
//            final List<ProcedureColumn> procedureColumns) {
//        this.procedureColumns = procedureColumns;
//    }

    // -------------------------------------------------------------------------
    @XmlAttribute
    @Labeled("PROCEDURE_CAT")
    @Nillable
    @Getter
    @Setter
    private String procedureCat;

    @XmlAttribute
    @Labeled("PROCEDURE_SCHEM")
    @Nillable
    @Getter
    @Setter
    private String procedureSchem;

    @XmlElement(required = true)
    @Labeled("PROCEDURE_NAME")
    @Getter
    @Setter
    private String procedureName;

    @XmlElement(required = true)
    @Labeled("REMARKS")
    @Getter
    @Setter
    private String remarks;

    @XmlElement(required = true)
    @Labeled("PROCEDURE_TYPE")
    @Getter
    @Setter
    private short procedureType;

    @XmlElement(required = true)
    @Labeled("SPECIFIC_NAME")
    @Getter
    @Setter
    private String specificName;

    @Invokable(name = "getProcedureColumns",
               types = {String.class, String.class, String.class, String.class},
               args = {
                   @Literals({":procedureCat", ":procedureSchem",
                              ":procedureName", "null"})
               }
    )
    @XmlElementRef
    private List<ProcedureColumn> procedureColumns;
}
