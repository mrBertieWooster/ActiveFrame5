//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.09.12 at 04:26:56 PM MSK 
//


package ru.intertrust.cm.performance.dataset.jaxb;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for decimalType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="decimalType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="minValue" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="maxValue" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *       &lt;attribute name="noValueChance" type="{}doubleLimited" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "decimalType")
public class DecimalType extends FieldType {

    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "value")
    protected BigDecimal value;
    @XmlAttribute(name = "minValue")
    protected BigDecimal minValue;
    @XmlAttribute(name = "maxValue")
    protected BigDecimal maxValue;
    @XmlAttribute(name = "noValueChance")
    protected Double noValueChance;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Gets the value of the minValue property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMinValue() {
        return minValue;
    }

    /**
     * Sets the value of the minValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMinValue(BigDecimal value) {
        this.minValue = value;
    }

    /**
     * Gets the value of the maxValue property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the value of the maxValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMaxValue(BigDecimal value) {
        this.maxValue = value;
    }

    /**
     * Gets the value of the noValueChance property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getNoValueChance() {
        return noValueChance;
    }

    /**
     * Sets the value of the noValueChance property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setNoValueChance(Double value) {
        this.noValueChance = value;
    }

    /* (non-Javadoc)
     * @see ru.intertrust.cm.performance.dataset.jaxb.FieldType#validate()
     */
    @Override
    public void validate() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ru.intertrust.cm.performance.dataset.jaxb.FieldType#process()
     */
    @Override
    public void process() {
        // TODO Auto-generated method stub
        
    }

}
