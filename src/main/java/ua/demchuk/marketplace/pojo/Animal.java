package ua.demchuk.marketplace.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.demchuk.marketplace.enums.AnimalCategory;


@Entity
@Data
@Table(name = "animals")
@JacksonXmlRootElement(localName = "animal")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @NotEmpty
    @JacksonXmlProperty(localName = "name")
    @JsonProperty("Name")
    private String name;

    @NotEmpty
    @JacksonXmlProperty(localName = "type")
    @JsonProperty("Type")
    private String type;

    @NotEmpty
    @JacksonXmlProperty(localName = "sex")
    @JsonProperty("Sex")
    private String sex;

    @NotNull
    @JacksonXmlProperty(localName = "weight")
    @JsonProperty("Weight")
    private Integer weight;

    @NotNull
    @JacksonXmlProperty(localName = "cost")
    @JsonProperty("Cost")
    private Integer cost;

    @JsonIgnore
    @Transient
    private AnimalCategory category;

    public void setCost(Integer cost) {
        this.cost = cost;
        updateCategory();
    }

    public Integer getCost() {
        updateCategory();
        return this.cost;
    }

    private void updateCategory() {
        this.category = AnimalCategory.getCategoryForCost(cost);
    }

    public AnimalCategory getCategory() {
        updateCategory();
        return category;
    }
}
