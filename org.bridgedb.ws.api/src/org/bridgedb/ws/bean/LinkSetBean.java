/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bridgedb.ws.bean;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Christian
 */
@XmlRootElement(name="LinkSet")
public class LinkSetBean {
    
    private String id;
    private String predicate;
    private String sourceNameSpace;
    private String targetNameSpace;
    private Integer linkCount;
    private Boolean isTransitive;

    public LinkSetBean(){
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * @return the sourceNameSpace
     */
    public String getSourceNameSpace() {
        return sourceNameSpace;
    }

    /**
     * @param sourceNameSpace the sourceNameSpace to set
     */
    public void setSourceNameSpace(String sourceNameSpace) {
        this.sourceNameSpace = sourceNameSpace;
    }

    /**
     * @return the targetNameSpace
     */
    public String getTargetNameSpace() {
        return targetNameSpace;
    }

    /**
     * @param targetNameSpace the targetNameSpace to set
     */
    public void setTargetNameSpace(String targetNameSpace) {
        this.targetNameSpace = targetNameSpace;
    }

    /**
     * @return the linkCount
     */
    public Integer getLinkCount() {
        return linkCount;
    }

    /**
     * @param linkCount the linkCount to set
     */
    public void setLinkCount(Integer linkCount) {
        this.linkCount = linkCount;
     }

    /**
     * @return the isTransitive
     */
    public Boolean getIsTransitive() {
        return isTransitive;
    }

    /**
     * @param isTransitive the isTransitive to set
     */
    public void setIsTransitive(Boolean isTransitive) {
        this.isTransitive = isTransitive;
    }

 }
