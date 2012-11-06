/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bridgedb.rdf.StatementReader;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

/**
 *
 * @author Christian
 */
public class ResourceMetaData extends HasChildrenMetaData implements MetaData{

    private final URI resourceType;
    private final Set<ResourceMetaData> parents = new HashSet<ResourceMetaData>();
    private boolean isParent = false;
    private boolean directlyLinkedTo = false;
    private static String UNSPECIFIED = "unspecified";
    private String label; 

    ResourceMetaData(URI type) {
        super(type.getLocalName(), UNSPECIFIED, RequirementLevel.SHOULD, new ArrayList<MetaDataBase>());
        childMetaData.add(PropertyMetaData.getTypeProperty(type.getLocalName()));
        this.resourceType = type;
        label = "(" + type + ") ";
    }
    
    ResourceMetaData(URI type, List<MetaDataBase> childMetaData) {
        super(type.getLocalName(), type.getLocalName(), RequirementLevel.SHOULD, childMetaData);
        childMetaData.add(PropertyMetaData.getTypeProperty(type.getLocalName()));
        this.resourceType = type;
        label = "(" + type + ") ";
    }
        
    private ResourceMetaData(ResourceMetaData other) {
        super(other);
        this.resourceType = other.resourceType;
        label = "(" + type + ") ";
    }

    /*private ResourceMetaData(String name){
        super(name, new ArrayList<MetaDataBase>());
        type = null;
    }*/
    
    @Override
    public void loadValues(Resource id, Set<Statement> data, MetaDataCollection collection) {
        super.loadValues(id, data, collection);
        Set<URI> predicates = getUsedPredicates(data);
        for (URI predicate:predicates){
            PropertyMetaData metaData = PropertyMetaData.getUnspecifiedProperty(predicate, type);
            metaData.loadValues(id, data, collection);
            childMetaData.add(metaData);
        }
    }

    void setupValues(Resource id){
        super.setupValues(id);
        if (id.stringValue().startsWith(StatementReader.DEFAULT_BASE_URI)){
            label = id.stringValue().substring(StatementReader.DEFAULT_BASE_URI.length());
        } else {
            label = id.stringValue();
        }    
        if (!UNSPECIFIED.equals(type)){
            label = "(" + type + ") " + label;
        }
    }
    private Set<URI> getUsedPredicates(Set<Statement> data){
        Set<URI> results = new HashSet<URI>();
        for (Statement statement: data){
            if (statement.getSubject().equals(id)){
                results.add(statement.getPredicate());
            }
        }        
        return results;
    }
    
    @Override
    public void appendSchema(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        builder.append("Resource ");
        builder.append(name);
        newLine(builder, tabLevel + 1);
        builder.append("rdf:type ");
        builder.append(resourceType);
        newLine(builder);
        for (MetaDataBase child:childMetaData){
            child.appendSchema(builder, tabLevel + 1);
        }
    }

    URI getType() {
        return resourceType;
    }

    public ResourceMetaData getSchemaClone() {
        return new ResourceMetaData(this);
    }

    @Override
    boolean hasValues() {
        //A Resource has values if any of the children have values.
        for (MetaDataBase child:childMetaData){
            if (child.hasValues()) { 
                return true; 
            }
        }
        return false;
    }

    @Override
    public void appendValidityReport(StringBuilder builder, boolean checkAllpresent, boolean includeWarnings, int tabLevel) {
        appendSpecific(builder, tabLevel);
        if (type.equals(UNSPECIFIED)){
            if (includeWarnings){
                tab(builder, tabLevel+1);
                builder.append("INFO: ");
                builder.append(" No requirments have been specified for this type.");
                newLine(builder);
            }
        } else {
            int before = builder.length();
            for (MetaDataBase child:childMetaData){
                child.appendValidityReport(builder, checkAllpresent, includeWarnings, tabLevel+1);
            }
            if (before == builder.length()){
                tab(builder, tabLevel+1);
                builder.append("All Required values found and correctly typed. ");
                newLine(builder);               
            }
        }
    }

    @Override
    void appendSpecific(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        builder.append(label);
        newLine(builder);  
    }
     
    public void appendParentValidityReport(StringBuilder builder, boolean checkAllpresent, boolean includeWarnings, int tabLevel){
        if (!isParent){
            //Wrong method called 
            appendValidityReport(builder, checkAllpresent, includeWarnings, tabLevel);
        } else if (!this.hasCorrectTypes()) {
            //Report all to pick up the incorrect type
            tab(builder, tabLevel);
            builder.append(label);
            newLine(builder);
            tab(builder, tabLevel+1);
            builder.append(" has a type ");
            builder.append(resourceType);
            newLine(builder);
            for (MetaDataBase child:childMetaData){
                child.appendValidityReport(builder, checkAllpresent, includeWarnings, tabLevel);
            }
        } else if (includeWarnings) {
            appendSpecific(builder, tabLevel);
            if (hasRequiredValues()) {
                tab(builder, tabLevel+1);
                builder.append("All Required values found and correctly typed. ");
                newLine(builder);
            } else {
                tab(builder, tabLevel+1);
                builder.append("WARNING: Incomplete so can only be used as a superset ");
                newLine(builder);                
            }
        } else {
            //Not including warnings so keep report small
        }
    }
    
    void addParent(ResourceMetaData parent) {
        parents.add(parent);
        Set<LeafMetaData> leaves = getLeaves();
        for (LeafMetaData leaf: leaves){
            URI predicate = leaf.getPredicate();
            LeafMetaData parentLeaf = parent.getLeafByPredicate(predicate);
            leaf.addParent(parentLeaf);
        }
        parent.isParent = true;
    }

    boolean isSuperset() {
        return isParent;
    }

    public void appendSummary(StringBuilder builder, int tabLevel) {
        tab(builder, tabLevel);
        if (type.equals(UNSPECIFIED)){
            builder.append(label);
            builder.append(" has an unspecified type of ");
            builder.append(resourceType);
        } else {
            tab(builder, tabLevel);
            builder.append(label);
            if (this.hasCorrectTypes()){
                if (this.hasRequiredValues()){
                    builder.append(" OK!");
                } else if (this.hasRequiredValues()){
                    builder.append(" has missing MUST values.");
                } else if (isParent){
                    builder.append(" can only be used as a superset.");
                } else {
                    builder.append(" incomplete!");
                }
            } else {
                builder.append(" has incorrect typed statements.");
            }
        }
        newLine(builder);        
    }

    public Resource getId() {
        return id;
    }

    void addChildren(List<MetaDataBase> childMetaData) {
        super.addChildren(childMetaData);
    }
}
