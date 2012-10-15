/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.metadata;

import org.bridgedb.metadata.constants.SchemaConstants;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bridgedb.linkset.constants.OwlConstants;
import org.bridgedb.metadata.constants.RdfConstants;
import org.bridgedb.metadata.constants.RdfsConstants;
import org.bridgedb.metadata.constants.XsdConstants;
import org.bridgedb.metadata.type.*;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 *
 * @author Christian
 */
public class PropertyMetaData extends MetaDataBase implements MetaData, LeafMetaData{

    private final URI predicate;
    private final MetaDataType metaDataType;
    private final Set<Value> values = new HashSet<Value>();
    private final Set<PropertyMetaData> parents = new HashSet<PropertyMetaData>();
    private final Set<Statement> rawRDF = new HashSet<Statement>();
    
    public PropertyMetaData(URI predicate, String type, RequirementLevel requirementLevel, String objectClass) throws MetaDataException{
        super(predicate.getLocalName(), type, requirementLevel);
        this.predicate = predicate;
        metaDataType = getMetaDataType(objectClass);
    }
    
    public static PropertyMetaData getUnspecifiedProperty(URI predicate, String type){
        PropertyMetaData result = new PropertyMetaData(predicate, type);
        return result;
    }
    
    public static PropertyMetaData getTypeProperty(String type){
        PropertyMetaData result = new PropertyMetaData(type);
        return result;
    }

    private PropertyMetaData(PropertyMetaData other) {
        super(other);
        predicate = other.predicate;
        metaDataType = other.metaDataType;
    }
    
    private PropertyMetaData(URI predicate, String type){
        super(predicate.getLocalName(), type, RequirementLevel.UNSPECIFIED);
        this.predicate = predicate;
        metaDataType = null;
    }
    
    private PropertyMetaData(String type){
        super("Type", type, RequirementLevel.MUST);
        this.predicate = RdfConstants.TYPE_URI;
        metaDataType = new UriType();
    }

    @Override
    public void loadValues(Resource id, Set<Statement> data, MetaDataCollection collection) {
        setupValues(id);
        for (Iterator<Statement> iterator = data.iterator(); iterator.hasNext();) {
            Statement statement = iterator.next();
            if (statement.getSubject().equals(id) && statement.getPredicate().equals(predicate)){
                 iterator.remove();
                 rawRDF.add(statement);
                 values.add(statement.getObject());
            }
        }  
    }

    private MetaDataType getMetaDataType(String objectClass) throws MetaDataException{
        //if (SchemaConstants.CLASS_ALLOWED_URIS.equalsIgnoreCase(objectClass)){
        //    return new AllowedUriType(element);
        //}
        //if (SchemaConstants.CLASS_ALLOWED_VALUES.equalsIgnoreCase(objectClass)){
        //    return new AllowedValueType(element);
        //}
        //if (SchemaConstants.CLASS_DATE.equalsIgnoreCase(objectClass)){
        //    return new DateType();
        // }
        //if (SchemaConstants.CLASS_INTEGER.equalsIgnoreCase(objectClass)){
        //    return new IntegerType();
        //}
        //if (SchemaConstants.CLASS_STRING.equalsIgnoreCase(objectClass)){
        //    return new StringType();
        //}
        if (OwlConstants.THING.equalsIgnoreCase(objectClass)){
            return new UriType();
        }
        if (RdfsConstants.LITERAL.equalsIgnoreCase(objectClass)){
            return new LiteralType();
        }
        if (objectClass.startsWith("xsd:")){
            objectClass = XsdConstants.PREFIX + objectClass.substring(4);
        }
        if (objectClass.equalsIgnoreCase(XsdConstants.STRING)){
            return new StringType();
        }
        if (objectClass.startsWith(XsdConstants.PREFIX)){
            return new XsdType(objectClass);
        }
        throw new MetaDataException ("Unexpected " + SchemaConstants.CLASS + " " + objectClass);
    }
    
    @Override
    void appendShowAll(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        //builder.append(id);
        if (requirementLevel == RequirementLevel.UNSPECIFIED){
            builder.append("RawRDF ");            
            builder.append(predicate);
        } else {
            builder.append("Property ");
            builder.append(name);
        }
        if (values.isEmpty()){
            builder.append(" MISSING!  Set with ");
            builder.append(predicate);
        } else if (values.size() == 1){
            builder.append(" == ");
            builder.append(values.iterator().next());
        } else {
            for (Value value: values){
                newLine(builder, tabLevel + 1);
                builder.append(value);
            }
        }
        newLine(builder);
    }

    @Override
    public void appendSchema(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        builder.append("Property ");
        builder.append(name);
        newLine(builder, tabLevel + 1);
        builder.append("predicate ");
        builder.append(predicate);        
        newLine(builder, tabLevel + 1);
        if (requirementLevel == RequirementLevel.UNSPECIFIED){
            builder.append("Unspecified RDF found in the data. ");
            newLine(builder);
        } else {
            builder.append("class ");
            builder.append(metaDataType.getCorrectType());        
            newLine(builder);
        }
    }

    @Override
    public PropertyMetaData getSchemaClone() {
        return new PropertyMetaData(this);
    }

    @Override
    public boolean hasRequiredValues() {
        if (requirementLevel != RequirementLevel.MUST){
            return true;
        } else if (values.isEmpty()){
            return false;          
        } else {
            return true;
        }
    }

    @Override
    boolean hasValues() {
        return !values.isEmpty();
    }
    
    @Override
    public boolean hasCorrectTypes() {
        if (requirementLevel != RequirementLevel.UNSPECIFIED){
            for (Value value: values){
                if (!metaDataType.correctType(value)){
                    return false;
                }
            }
        }
        //If no incorrect values return true. Even if there are No values.
        return true;
    }

    @Override
    public void appendValidityReport(StringBuilder builder, boolean checkAllpresent, boolean includeWarnings, int tabLevel) {
        if (requirementLevel != RequirementLevel.UNSPECIFIED){
            if (checkAllpresent && values.isEmpty()){
                appendEmptyReport(builder, tabLevel);
            } else if (!hasCorrectTypes()){
                appendIncorrectTypeReport(builder, tabLevel);
            } else {
                //Ok so nothing to append
            }
        } else {
            appendUnspecifiedReport(builder, includeWarnings, tabLevel);            
        }
    }

    private void appendEmptyReport(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        builder.append("ERROR: ");
        appendLabel(builder, ":");
        builder.append(" is missing. ");
        newLine(builder, tabLevel + 1);
        builder.append("Please add a statement with the predicate ");
        builder.append(predicate);
        newLine(builder);
        addDocumentationLink(builder, tabLevel);
   }
    
    private void appendIncorrectTypeReport(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        builder.append("ERROR: Incorrect type for ");
        appendLabel(builder, ":");
        for (Value value: values){
            if (!metaDataType.correctType(value)){
                newLine(builder, tabLevel + 1);
                builder.append("Expected ");
                builder.append(metaDataType.getCorrectType());
                newLine(builder, tabLevel + 1);
                builder.append(" Found ");
                builder.append(value);
                builder.append(" Which is a  ");
                builder.append(value.getClass());
            }
        }
        newLine(builder);
        addDocumentationLink(builder, tabLevel);
    }
    
    private void appendUnspecifiedReport(StringBuilder builder, boolean includeWarnings, int tabLevel) {
        if (includeWarnings){
            tab(builder, tabLevel);
            builder.append("INFO: ");
            appendLabel(builder);
            builder.append(" has an extra Predicate ");
            builder.append(predicate);
            newLine(builder);
        }
    }
    
    @Override
    public boolean allStatementsUsed() {
        return requirementLevel != RequirementLevel.UNSPECIFIED;

    }
    
    @Override
    void appendUnusedStatements(StringBuilder builder) {
        if (requirementLevel == RequirementLevel.UNSPECIFIED){
            for (Statement statement: rawRDF){
                builder.append(statement);
                newLine(builder);
            }
        }
    }

    @Override
    public Set<Value> getValuesByPredicate(URI predicate) {
        if (this.predicate.equals(predicate)){
            return values;
        } else {
            return null;
        }
    }

    @Override
    public Set<ResourceMetaData> getResoucresByPredicate(URI predicate){
        return null;
    }
    
    @Override
    PropertyMetaData getLeafByPredicate(URI predicate) {
        if (this.predicate.equals( predicate)){
            return this;
        }
        return null;
    }

    @Override
    Set<PropertyMetaData> getLeaves() {
        HashSet<PropertyMetaData> results = new HashSet<PropertyMetaData>();
        results.add(this);
        return results;
    }

    @Override
    public URI getPredicate() {
        return predicate;
    }

    @Override
    public void addParent(LeafMetaData parentLeaf) {
        if (parentLeaf == null){
            return;
        }
        if (parentLeaf instanceof PropertyMetaData){
            PropertyMetaData pmd = (PropertyMetaData)parentLeaf;
            values.addAll(pmd.values);
        } else {
            throw new UnsupportedOperationException("Unexpected LeafMetaData type of " + parentLeaf.getClass());
        }
    }

    @Override
    public Set<Statement> getRDF() {
        return rawRDF;
    }


}
